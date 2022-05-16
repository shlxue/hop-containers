package org.apache.hop.it;

import c2m.carbon.launcher.Launcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class DirectEngineIT {
  LaunchCmd cmd = new LaunchCmd();

  @Test
  void hopRunHelp() throws Exception {
    Launcher.main(cmd.helpArgs());
  }

  @Test
  @EnabledIfSystemProperty(named = "HOP_ENGINE", matches = "local")
  void runStubWorkflow() throws Exception {
    Launcher.main(cmd.hopArgs("actions/workflow-stub.hwf"));
  }

  @Test
  void runStubPipeline() throws Exception {
    Launcher.main(cmd.hopArgs("transforms/pipeline-stub.hpl"));
  }
}
