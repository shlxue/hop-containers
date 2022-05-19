package org.apache.hop.it.template;

import org.apache.hop.it.HopExtension;
import org.apache.hop.it.HopRunner;
import org.apache.hop.it.LaunchCmd;
import org.apache.hop.it.TestFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(HopExtension.class)
public class DefaultProjectIT {
  TestFile testFile = new TestFile(new LaunchCmd());

  @Test
  @EnabledIfSystemProperty(named = "HOP_ENGINE", matches = "local")
  void runWorkflowStub() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.args("workflow-stub.hwf")));
  }

  @Test
  void runPipelineStub() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.args("pipeline-dummy.hpl")));
  }

  @Test
  void runPipelineDummy() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.args("dummy-sample.hpl")));
  }

  @Test
  void runPipelineRowGenerator() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.args("row-generator.hpl")));
  }
}
