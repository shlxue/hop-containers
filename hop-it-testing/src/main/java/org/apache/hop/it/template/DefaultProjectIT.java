package org.apache.hop.it.template;

import org.apache.hop.it.HopExtension;
import org.apache.hop.it.HopRunner;
import org.apache.hop.it.TestFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(HopExtension.class)
public class DefaultProjectIT {
  protected TestFile testFile = new TestFile();

  @Test
  @EnabledIfSystemProperty(named = "HOP_ENGINE", matches = "local")
  void runWorkflowStub() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.getArgs("workflow-stub.hwf")));
  }

  @Test
  void runPipelineStub() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.getArgs("pipeline-stub.hpl")));
  }

  @Test
  void runPipelineDummy() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.getArgs("dummy-sample.hpl")));
  }

  @Test
  void runPipelineRowGenerator() {
    assertDoesNotThrow(() -> HopRunner.main(testFile.getArgs("row-generator.hpl")));
  }
}
