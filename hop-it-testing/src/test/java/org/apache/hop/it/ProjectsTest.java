package org.apache.hop.it;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectsTest {
  @Test
  void testCfgPaths() {
      assertNotNull(Projects.ROOT_PATH);
      assertNotNull(Projects.HOP_HOME);
      assertNotNull(Projects.HOP_PROJECT);
      assertTrue(Files.exists(Projects.HOP_HOME.resolve("translator.xml")));
      assertTrue(Files.exists(Projects.HOP_CONFIG));
      assertTrue(Files.exists(Projects.HOP_PROJECT));
  }
}
