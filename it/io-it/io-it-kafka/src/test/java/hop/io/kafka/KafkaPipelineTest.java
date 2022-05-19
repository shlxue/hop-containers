package hop.io.kafka;

import org.apache.hop.it.HopRunner;
import org.apache.hop.it.LaunchCmd;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class KafkaPipelineTest {
  LaunchCmd cmd = new LaunchCmd("kafka", "local");

  @Test
  void runPipeline() {
    // TODO: fix test after integration kafka service
    Assertions.assertThrows(
        CommandLine.ExecutionException.class,
        () -> HopRunner.main(cmd.hopArgs("prepare-kafka-test-basic.hpl")));
  }
}
