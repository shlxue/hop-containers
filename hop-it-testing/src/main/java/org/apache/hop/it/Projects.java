package org.apache.hop.it;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Projects {
  private static final String HOP_CONFIG_FILENAME = "hop-config.json";
  private static final String DEFAULT_PROJECT_CONFIG_FILENAME = "project-config.json";

  public static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir"));
  public static final Path HOP_HOME;
  public static final Path HOP_CONFIG;
  public static final Path HOP_PROJECT;

  static final Map<String, String> projects;

  static final String HOP_TRANSLATOR_CFG = "translator.xml";

  public static String getProperty(String key) {
    return getProperty(key, null);
  }

  public static String getProperty(String key, String defaultVal) {
    return Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse(defaultVal);
  }

  static {
    Predicate<Path> hopHomeFilter =
        path -> Files.isDirectory(path) && Files.exists(path.resolve(HOP_TRANSLATOR_CFG));
    int depth = 4;
    Path path =
        find(ROOT_PATH.resolve("target"), depth, hopHomeFilter)
            .orElse(
                find(ROOT_PATH, depth, hopHomeFilter)
                    .orElse(find(ROOT_PATH.getParent(), depth, hopHomeFilter).orElseThrow()));
    HOP_HOME = path.equals(ROOT_PATH) ? Paths.get(".") : ROOT_PATH.relativize(path);
    HOP_CONFIG = Paths.get(HOP_HOME.toString(), "config", HOP_CONFIG_FILENAME);
    HOP_PROJECT = HOP_HOME.resolve("projects/default").resolve(DEFAULT_PROJECT_CONFIG_FILENAME);
    applySystemProperties(HOP_HOME);
    projects = new HashMap<>();
    Gson gson = new Gson();
    try {
      Map<String, Map> map =
          gson.fromJson(new JsonReader(new FileReader(HOP_CONFIG.toFile())), Map.class);
      projects.putAll(readProjectNames(map.get("projectsConfig")));
    } catch (Throwable ignore) {
    }
    projects.putAll(searchProjectNames(HOP_HOME));
  }

  private static void applySystemProperties(Path hopHome) {
    BiConsumer<String, String> consumer =
        (key, value) -> {
          if (Projects.getProperty(key) == null) {
            System.setProperty(key, value);
          }
        };
    consumer.accept("HOP_HOME", hopHome.toString());
    consumer.accept("HOP_AUDIT_FOLDER", hopHome.resolve("audit").toString());
    consumer.accept("HOP_CONFIG_FOLDER", hopHome.resolve("config").toString());
  }

  private static Map<String, String> readProjectNames(Object projectsConfig) {
    if (projectsConfig instanceof Map) {
      Object list = ((Map<?, ?>) projectsConfig).get("projectConfigurations");
      if (list instanceof List) {
        return ((List<?>) list)
            .stream()
                .map(o -> (Map<String, String>) o)
                .map(
                    map ->
                        new AbstractMap.SimpleEntry<>(
                            map.get("projectName"), map.get("projectHome")))
                .collect(
                    Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
      }
    }
    return Collections.emptyMap();
  }

  private static Map<String, String> searchProjectNames(Path hopHome) {
    return scan(hopHome, 4, Projects::isHopProjectPath)
        .map(path -> new AbstractMap.SimpleEntry<>(path.getFileName().toString(), path.toString()))
        .collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

  public static Optional<Path> find(Path basePath, int maxDepth, Predicate<Path> filter) {
    return scan(basePath, maxDepth, filter).findFirst();
  }

  public static Stream<Path> scan(Path basePath, int maxDepth, Predicate<Path> filter) {
    return walk(basePath, maxDepth).filter(filter);
  }

  public static Path getProjectCfg(String projectName) {
    return find(Projects.ROOT_PATH, 3, Projects::isHopProjectFolder).orElseThrow();
  }

  public static boolean isHopConfigPath(Path path) {
    return Files.isDirectory(path) && Files.exists(path.resolve(HOP_CONFIG_FILENAME));
  }

  public static boolean isHopProjectPath(Path path) {
    return Files.isDirectory(path) && Files.exists(path.resolve(DEFAULT_PROJECT_CONFIG_FILENAME));
  }

  public static boolean isProjectPath(Path path) {
    return Files.isDirectory(path) && Files.exists(path.resolve(DEFAULT_PROJECT_CONFIG_FILENAME));
  }

  public static boolean isProjectPath(Path path, String projectName) {
    Path projectPath = Paths.get(projectName);
    return isProjectPath(path) && projectPath.equals(path.getFileName());
  }

  public static boolean isProjectFile(Path path, String fileName) {
    Path filePath = Paths.get(fileName);
    return Files.isRegularFile(path) && filePath.getFileName().equals(path.getFileName());
  }

  public static boolean file(Path path, String fileName) {
    return file(path, p -> fileName.equals(p.getFileName().toString()));
  }

  public static boolean file(Path path, Predicate<Path> filter) {
    return Files.isRegularFile(path) && filter.test(path);
  }

  public static boolean folder(Path path, String folderName) {
    return file(path, p -> folderName.equals(p.getFileName().toString()));
  }

  public static boolean folder(Path path, Predicate<Path> filter) {
    return Files.isDirectory(path) && filter.test(path);
  }

  public static Stream<Path> walk(Path basePath, int maxDepth) {
    try {
      if (Files.exists(basePath)) {
        return maxDepth >= 0 ? Files.walk(basePath, maxDepth) : Files.walk(basePath);
      }
    } catch (IOException ignore) {
    }
    return Stream.empty();
  }

  private static boolean isHopProjectFolder(Path path) {
    return Files.isDirectory(path)
        && Files.exists(path.resolve(HOP_CONFIG))
        && Files.exists(path.resolve(HOP_PROJECT));
  }
}
