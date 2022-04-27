package org.apache.hop.it.template;

import org.apache.hop.it.HopExtension;
import org.apache.hop.it.HopRunner;
import org.apache.hop.it.Projects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(HopExtension.class)
public class HopIT {

  @Test
  void testPipelineDummy() {
    String pipeline = "pipeline-dummy.hpl";
    assertDoesNotThrow(
        () -> HopRunner.main(new String[] {"-f", getProjectFile(pipeline), "-r", "local"}));

    assertDoesNotThrow(() -> HopRunner.main(new String[] {"-f", pipeline, "-r", "local"}));
    assertDoesNotThrow(
        () -> HopRunner.main(new String[] {"-j", "default", "-f", pipeline, "-r", "local"}));
  }

  @Test
  @EnabledIfSystemProperty(named = "HOP_ENGINE", matches = "local")
  void testWorkflowDummy() {
    String workflow = "workflow-dummy.hwf";
    assertDoesNotThrow(
        () -> HopRunner.main(new String[] {"-f", getProjectFile(workflow), "-r", "local"}));
    assertDoesNotThrow(() -> HopRunner.main(new String[] {"-f", workflow, "-r", "local"}));
    assertDoesNotThrow(
        () -> HopRunner.main(new String[] {"-j", "default", "-f", workflow, "-r", "local"}));
  }

  private String getProjectFile(String file) {
    return Projects.HOP_PROJECT.getParent().resolve(file).toString();
  }
}
