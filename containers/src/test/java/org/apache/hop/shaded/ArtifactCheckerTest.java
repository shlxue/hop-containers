package org.apache.hop.shaded;

import org.apache.hop.it.HopEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactCheckerTest {
  static ArtifactChecker checker;

  @BeforeAll
  static void beforeAll() {
    Path path = Path.of("target/opt/flink");
    if (!Files.exists(path)) {
      path = Path.of("../opt/flink");
    }
    checker = new ArtifactChecker(path, ".*.jar");
  }

  @Test
  void showDataflowContainer() {
    assertTrue(checker.parse());
    checker.showRunner(HopEngine.Dataflow);
  }

  @Test
  void showDirectContainer() {
    assertTrue(checker.parse());
    checker.showRunner(HopEngine.Direct);
  }

  @Test
  void showFlinkContainer() {
    assertTrue(checker.parse());
    checker.showRunner(HopEngine.Flink);
  }

  @Test
  void showSparkContainer() {
    assertTrue(checker.parse());
    checker.showRunner(HopEngine.Spark);
  }
}
