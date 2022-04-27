package org.apache.hop.it;

import lombok.SneakyThrows;
import org.apache.hop.core.Const;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.workflow.WorkflowMeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Projects {
  public static final Path ROOT_PATH;
  public static final Path HOP_CONFIG;
  public static final Path HOP_PROJECT;

  static final List<TestFile> pipelines = new ArrayList<>();
  static final List<TestFile> workflows = new ArrayList<>();

  static {
    Path basePath = Paths.get("it/config");
    if (!isProjectPath(basePath)) {
      basePath = Paths.get("target/it/config");
    }
    if (!isProjectPath(basePath)) {
      basePath = find(Paths.get("target"), 3, Projects::isHopCfgPath).orElse(null);
    }
    if (basePath == null) {
      basePath = find(Paths.get("."), 4, Projects::isHopCfgPath).orElse(null);
    }
    assert Objects.nonNull(basePath);

    ROOT_PATH = basePath.getParent();
    HOP_CONFIG = ROOT_PATH.resolve(Paths.get("config", Const.HOP_CONFIG));
    HOP_PROJECT =
        ROOT_PATH.resolve(
            Paths.get("projects/default", ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME));
  }

  public static Optional<Path> find(Path basePath, int maxDepth, Predicate<Path> filter) {
    return walk(basePath, maxDepth).filter(filter).findFirst();
  }

  public static Path getProjectCfg(String projectName) {
    return find(Projects.ROOT_PATH, 3, Projects::isHopProjectFolder).orElseThrow();
  }

  public static boolean isHopCfgPath(Path path) {
    return Files.isDirectory(path) && Files.exists(path.resolve(Const.HOP_CONFIG));
  }

  public static boolean isProjectPath(Path path) {
    return Files.isDirectory(path)
        && Files.exists(path.resolve(ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME));
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

  @SneakyThrows(IOException.class)
  private static List<Path> getProjectPaths() {
    Path rootPath = Files.exists(ROOT_PATH) ? ROOT_PATH : Paths.get("target").resolve(ROOT_PATH);
    if (!Files.exists(rootPath)) {
      return Collections.emptyList();
    }
    return Files.walk(rootPath, 1)
        .filter(Projects::isHopProjectFolder)
        .collect(Collectors.toList());
  }

  public static TestFile getPipeline(String file) {
    return getPipelines().stream()
        .filter(testFile -> testFile.getFile().toString().endsWith(file))
        .min(Comparator.comparingInt(o -> o.getFile().getNameCount()))
        .orElseThrow(() -> new RuntimeException("Not found file: " + file));
  }

  @SneakyThrows(IOException.class)
  private static List<Path> getPipelines(Path projectPath) {
    return Files.walk(projectPath)
        .filter(Projects::isPipeline)
        .sorted()
        .collect(Collectors.toList());
  }

  @SneakyThrows(IOException.class)
  private static List<Path> getWorkflows(Path projectPath) {
    return Files.walk(projectPath)
        .filter(Projects::isWorkflow)
        .sorted()
        .collect(Collectors.toList());
  }

  public static List<TestFile> getPipelines() {
    if (pipelines.isEmpty()) {
      pipelines.addAll(
          getProjectPaths().stream()
              .flatMap(
                  path ->
                      wrap(
                          path.resolve(HOP_CONFIG),
                          path.resolve(HOP_PROJECT),
                          () -> getPipelines(path)))
              .collect(Collectors.toList()));
    }
    return pipelines;
  }

  public static List<TestFile> getWorkflows() {
    if (workflows.isEmpty()) {
      workflows.addAll(
          getProjectPaths().stream()
              .flatMap(
                  path ->
                      wrap(
                          path.resolve(HOP_CONFIG),
                          path.resolve(HOP_PROJECT),
                          () -> getWorkflows(path)))
              .collect(Collectors.toList()));
    }
    return workflows;
  }

  private static Stream<TestFile> wrap(
      Path hopCfg, Path projectCfg, Supplier<List<Path>> fileConsumer) {
    return fileConsumer.get().stream().map(path -> new TestFile(projectCfg, HopEngine.local, path));
  }

  private static boolean isHopProjectFolder(Path path) {
    return Files.isDirectory(path)
        && Files.exists(path.resolve(HOP_CONFIG))
        && Files.exists(path.resolve(HOP_PROJECT));
  }

  private static boolean isPipeline(Path path) {
    return Files.isRegularFile(path) && path.toString().endsWith(PipelineMeta.PIPELINE_EXTENSION);
  }

  private static boolean isWorkflow(Path path) {
    return Files.isRegularFile(path)
        && path.toString().equalsIgnoreCase(WorkflowMeta.WORKFLOW_EXTENSION);
  }
}
