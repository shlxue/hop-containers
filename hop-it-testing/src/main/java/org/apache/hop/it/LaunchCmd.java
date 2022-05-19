package org.apache.hop.it;

import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LaunchCmd {

  static final String HOP_RUN = "org.apache.hop.it.HopRunner";

  private final Logger logger = Logger.getLogger(HOP_RUN);
  @Getter private final String project;
  private Path projectHome;
  @Getter private final HopEngine hopEngine;

  private final List<String> args;
  private final Map<String, String> params;
  private final Map<String, String> sysProps;
  private final Map<String, String> env;

  private static String defaultHopEngine() {
    String key = "HOP_ENGINE";
    return Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse("local");
  }

  private static void applyDefaultConfigs(String hopHome, String hopEngine) {
    System.setProperty("system-property.HOP_HOME", hopHome);
    System.setProperty("system-property.HOP_ENGINE", hopEngine);
  }

  public LaunchCmd() {
    this("default");
  }

  public LaunchCmd(String project) {
    this(project, defaultHopEngine());
  }

  public LaunchCmd(String project, String hopEngine) {
    assert Projects.projects.containsKey(project);
    this.project = project;
    this.hopEngine = HopEngine.valueOf(hopEngine);
    applyDefaultConfigs(Projects.HOP_HOME.toString(), hopEngine);
    args = new ArrayList<>();
    params = new HashMap<>();
    sysProps = new HashMap<>();
    env = new HashMap<>();
  }

  public Path getProjectHome() {
    if (projectHome == null) {
      String value = Projects.projects.get(project);
      projectHome = Paths.get(parse(value));
    }
    return projectHome;
  }

  static Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");

  private static String parse(String value) {
    Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      value = matcher.replaceAll(result -> Projects.getProperty(result.group(), result.toString()));
    }
    return value;
  }

  public LaunchCmd param(String name, String value) {
    params.put(name, value);
    return this;
  }

  public LaunchCmd sysProp(String key, String value) {
    sysProps.put(key, value);
    return this;
  }

  public LaunchCmd env(String key, String value) {
    env.put(key, value);
    return this;
  }

  public String[] custom(String... programArgs) {
    return buildArgs(true, () -> args.addAll(Arrays.asList(programArgs)));
  }

  public String[] hopArgs(String file) {
    return args(false, project, file);
  }

  public String[] args(String file) {
    return args(project, file);
  }

  public String[] args(String project, String file) {
    return args(true, project, file);
  }

  private String[] args(boolean withLaunchOptions, String project, String file) {
    Runnable programArgs =
        () -> {
          if (project != null) {
            arg("j", project);
          }
          if (file != null) {
            arg("f", file).arg("r", hopEngine.toString());
          } else {
            arg("h");
          }
        };
    return buildArgs(withLaunchOptions, programArgs);
  }

  private String[] buildArgs(boolean withLaunchOptions, Runnable runnable) {
    args.clear();
    params.clear();
    sysProps.clear();
    env.clear();
    runnable.run();
    List<String> programArgs = new ArrayList<>(args);
    args.clear();
    if (withLaunchOptions) {
      arg("main", HOP_RUN, false, false)
          .arg("lib", libPaths(), false, true)
          .arg("cp", classPaths(), false, true);
      if (!programArgs.isEmpty()) {
        arg("-");
      }
    }
    args.addAll(programArgs);
    Function<Map<String, String>, String> join =
        map ->
            map.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    arg("p", join.apply(params), true, true);
    arg("s", join.apply(sysProps), true, true);
    arg("e", join.apply(env), true, true);

    logger.finer(() -> String.join(" ", args));
    return args.toArray(String[]::new);
  }

  private String libPaths() {
    String flinkPath = "opt/flink/lib";
    Predicate<Path> filter = path -> path.toString().endsWith(flinkPath);
    Path appPath =
        Projects.find(Projects.HOP_HOME, -1, filter)
            .orElse(
                Projects.find(Projects.HOP_HOME.resolve(".."), -1, filter)
                    .orElseThrow(() -> new RuntimeException("Not found flink path: " + flinkPath)))
            .getParent();
    return Stream.of(
            appPath.resolve("lib"),
            appPath.resolve("lib_" + hopEngine.toString().toLowerCase()),
            appPath.resolve("share"))
        .map(Path::toString)
        .collect(Collectors.joining(":"));
  }

  private String classPaths() {
    return null;
  }

  private LaunchCmd arg(String option) {
    return arg(option, null);
  }

  private LaunchCmd arg(String option, String value) {
    return arg(option, value, true, false);
  }

  private LaunchCmd arg(String option, String value, boolean equalJoin, boolean ignoreEmpty) {
    String join;
    if (value == null || "".equals(value.trim())) {
      if (ignoreEmpty) {
        return this;
      }
      join = "";
      value = "";
    } else {
      join = equalJoin ? "=" : " ";
    }
    args.add(String.format("-%s%s%s", option, join, value));
    return this;
  }
}
