package org.apache.hop.it;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

class HopRunnerTest {

  @Test
  void showHelp() {
    assertDoesNotThrow(() -> HopRunner.main(new String[] {"-h"}));
  }

  @Test
  void testPipeline() {
    assertThrows(
        CommandLine.ExecutionException.class,
        () -> HopRunner.main(new LaunchCmd().hopArgs("non-exist.hpl")));
    assertDoesNotThrow(() -> HopRunner.main(new LaunchCmd().hopArgs("pipeline-dummy.hpl")));
  }
}
