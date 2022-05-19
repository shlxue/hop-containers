package org.apache.hop.it;

import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class TestFile {
  private final LaunchCmd cmd;
  //  private final Path hopCfgPath;
  //  private final Path projectPath;
  //
  //  private final String projectName;
  //
  //  private final HopEngine hopEngine;
  //  private final List<String> cmdArgs = new ArrayList<>();
  //  private final ProjectsConfig projectsCfg;
  //  @Setter private Path file;

  //  private boolean printOptions;

  //  public TestFile() {
  //    this("default");
  //  }
  private String file;

  public TestFile(LaunchCmd cmd) {
    this.cmd = cmd;
  }

  //  public TestFile(String projectName) {
  //    this(projectName, HopEngine.local);
  //  }
  //
  //  public TestFile(String projectName, HopEngine hopEngine) {
  //    this(
  //        Projects.find(Projects.HOP_HOME, 3, path -> Projects.isProjectPath(path, projectName))
  //            .orElseThrow(
  //                () ->
  //                    new RuntimeException(
  //                        String.format(
  //                            "Not found [%s] project in %s", projectName, Projects.ROOT_PATH))),
  //        hopEngine);
  //  }

  //  public TestFile(Path projectPath, HopEngine hopEngine) {
  //    this.hopCfgPath = Projects.HOP_CONFIG;
  //    this.projectPath = projectPath;
  //    this.projectName = projectPath.getName(projectPath.getNameCount() - 1).toString();
  //    String runConfiguration = System.getProperty("run_configuration");
  //    this.hopEngine =
  //        StringUtil.isEmpty(runConfiguration) ? hopEngine : HopEngine.valueOf(runConfiguration);
  //    this.projectsCfg = new ProjectsConfig();
  //    projectsCfg.setDefaultProject(projectName);
  //  }

  //  public TestFile(Path projectPath, HopEngine hopEngine, Path file) {
  //    this(projectPath, hopEngine);
  //    this.file = file;
  //  }

  //  public String getProjectCfg() {
  //    return getProjectCfg(ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME);
  //  }

  //  public String getProjectCfg(String projectFile) {
  //    return projectPath.resolve(projectFile).toString();
  //  }

  private Path find(String fileName) {
    Predicate<Path> filter = path -> fileName.equals(path.getFileName().toString());
    List<Path> paths = Projects.scan(cmd.getProjectHome(), -1, filter).collect(Collectors.toList());

    if (paths.isEmpty()) {
      throw new RuntimeException("Not found file " + fileName);
    } else if (paths.size() > 1) {
      throw new RuntimeException(
          "Found multi same files: "
              + paths.stream().map(Path::toString).collect(Collectors.joining(",")));
    }
    return paths.get(0);
  }

  //  public String[] getArgs() {
  //    if (file != null) {
  //      return getArgs(file.toString());
  //    }
  //    return args(hopEngine).getCmdArgs().toArray(String[]::new);
  //  }
  //
  //  public String[] getArgs(String file) {
  //    return getArgs(file, hopEngine);
  //  }

  public String[] args() {
    return args(file);
  }

  public String[] args(String file) {
    Path hopFile = find(file);
    if (file.toLowerCase().endsWith(".hwf") && cmd.getHopEngine() != HopEngine.local) {
      return new String[0];
    }
    return cmd.hopArgs(cmd.getProjectHome().relativize(hopFile).toString());
  }

  public TestFile file(String file) {
    this.file = file;
    return this;
  }

  public TestFile param(String name, String value) {
    cmd.param(name, value);
    return this;
  }

  public TestFile sysProp(String key, String value) {
    cmd.sysProp(key, value);
    return this;
  }

  public TestFile env(String key, String value) {
    cmd.env(key, value);
    return this;
  }

  //  @Override
  //  public String toString() {
  //    Path path = hopCfgPath.getParent();
  //    return String.format("%s: %s", path.getFileName(), path.relativize(file));
  //  }

  //  private TestFile args(HopEngine engine) {
  //    cmdArgs.clear();
  //    if (printOptions) {
  //      arg("-o");
  //    }
  //    return arg("-j", projectName).arg("-r", engine.toString());
  //  }

  //  private TestFile arg(String key) {
  //    return arg(key, null);
  //  }

  //  private TestFile arg(String key, String value) {
  //    cmdArgs.add(key);
  //    if (!StringUtil.isEmpty(value)) {
  //      cmdArgs.add(value);
  //    }
  //    return this;
  //  }
}
