package org.apache.hop.it;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.projects.config.ProjectsConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TestFile {
  private final Path hopCfgPath;
  private final Path projectPath;

  private final String projectName;

  private final HopEngine hopEngine;
  private final List<String> cmdArgs = new ArrayList<>();
  private final ProjectsConfig projectsCfg;
  @Setter private Path file;

  private boolean printOptions;

  public TestFile() {
    this("default");
  }

  public TestFile(String projectName) {
    this(projectName, HopEngine.local);
  }

  public TestFile(String projectName, HopEngine hopEngine) {
    this(
        Projects.find(Projects.ROOT_PATH, 3, path -> Projects.isProjectPath(path, projectName))
            .orElseThrow(
                () ->
                    new RuntimeException(
                        String.format(
                            "Not found [%s] project in %s", projectName, Projects.ROOT_PATH))),
        hopEngine);
  }

  public TestFile(Path projectPath, HopEngine hopEngine) {
    this.hopCfgPath = Projects.HOP_CONFIG;
    this.projectPath = projectPath;
    this.projectName = projectPath.getName(projectPath.getNameCount() - 1).toString();
    String runConfiguration = System.getProperty("run_configuration");
    this.hopEngine =
        StringUtil.isEmpty(runConfiguration) ? hopEngine : HopEngine.valueOf(runConfiguration);
    this.projectsCfg = new ProjectsConfig();
    projectsCfg.setDefaultProject(projectName);
  }

  public TestFile(Path projectPath, HopEngine hopEngine, Path file) {
    this(projectPath, hopEngine);
    this.file = file;
  }

  public String getProjectCfg() {
    return getProjectCfg(ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME);
  }

  public String getProjectCfg(String projectFile) {
    return projectPath.resolve(projectFile).toString();
  }

  public Path find(String fileName) {
    return Projects.find(projectPath, -1, path -> Projects.isProjectFile(path, fileName))
        .orElseThrow(() -> new RuntimeException("Not found file " + file));
  }

  public String[] getArgs() {
    if (file != null) {
      return getArgs(file.toString());
    }
    return args(hopEngine).getCmdArgs().toArray(String[]::new);
  }

  public String[] getArgs(String file) {
    return getArgs(file, hopEngine);
  }

  public String[] getArgs(String file, HopEngine engine) {
    Path hopFile = find(file);
    if (file.endsWith(".hwf") && engine != HopEngine.local) {
      return new String[] {"-h"};
    }
    return args(engine)
        .arg("-f", projectPath.relativize(hopFile).toString())
        .getCmdArgs()
        .toArray(String[]::new);
  }

  @Override
  public String toString() {
    Path path = hopCfgPath.getParent();
    return String.format("%s: %s", path.getFileName(), path.relativize(file));
  }

  private TestFile args(HopEngine engine) {
    cmdArgs.clear();
    if (printOptions) {
      arg("-o");
    }
    return arg("-j", projectName).arg("-r", engine.toString());
  }

  private TestFile arg(String key) {
    return arg(key, null);
  }

  private TestFile arg(String key, String value) {
    cmdArgs.add(key);
    if (!StringUtil.isEmpty(value)) {
      cmdArgs.add(value);
    }
    return this;
  }
}
