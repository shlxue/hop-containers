package org.apache.hop.it;

import c2m.carbon.launcher.Launcher;
import org.junit.jupiter.api.Test;

class DirectEngineIT {
  LaunchCmd cmd = new LaunchCmd();

  @Test
  void hopRunHelp() throws Exception {
    Launcher.main(cmd.helpArgs());
  }

  @Test
  void runStubWorkflow() throws Exception {
    Launcher.main(cmd.hopArgs("workflow-stub.hwf"));
  }

  @Test
  void runStubPipeline() throws Exception {
    Launcher.main(cmd.hopArgs("pipeline-stub.hpl"));
  }
}
