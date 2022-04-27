package org.apache.hop.it;

import java.util.ArrayList;
import java.util.List;

final class LaunchCmd {
  static final String HOP_RUN = "org.apache.hop.run.HopRun";

  private final String project;
  private final String hopEngine;

  private static String defaultHopEngine() {
    String key = "HOP_ENGINE";
    return System.getProperty(key, System.getenv(key));
  }

  public LaunchCmd() {
    this("default");
  }

  public LaunchCmd(String project) {
    this(project, defaultHopEngine());
  }

  public LaunchCmd(String project, String hopEngine) {
    this.project = project;
    this.hopEngine = hopEngine;
    System.out.println("==== " + hopEngine);
  }

  public String[] helpArgs() {
    return buildArgs(null, null);
  }

  public String[] hopArgs(String file) {
    return hopArgs(project, file);
  }

  public String[] hopArgs(String project, String file) {
    return buildArgs(project, file);
  }

  private String[] buildArgs(String project, String file) {
    List<String> args = new ArrayList<>();
    args.add("-main");
    args.add(HOP_RUN);
    args.add("--");
    if (project != null) {
      args.add(arg("j", project));
    }
    if (file != null) {
      args.add(arg("f", file));
      args.add(arg("r", hopEngine));
    } else {
      args.add("-h");
    }
    return args.toArray(String[]::new);
  }

  private String arg(String option, String value) {
    return String.format("-%s=%s", option, value);
  }
}
