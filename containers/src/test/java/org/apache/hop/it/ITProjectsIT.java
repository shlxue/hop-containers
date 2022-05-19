package org.apache.hop.it;

import c2m.carbon.launcher.Launcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class ITProjectsIT {
  LaunchCmd cmd = new LaunchCmd("kafka");

  @Test
  void runKafkaPipeline() throws Exception {
//    Launcher.main(cmd.args("0001-kafka-consumer-read-record-basic.hpl"));
  }
}
