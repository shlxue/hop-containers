package org.apache.hop.shaded;

import org.apache.hop.it.HopEngine;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactCheckerTest {
  ArtifactChecker checker = new ArtifactChecker(Path.of("target/opt/flink"), ".*.jar");

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
