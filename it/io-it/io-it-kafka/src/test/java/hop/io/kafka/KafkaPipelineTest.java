package hop.io.kafka;

import org.apache.hop.beam.run.MainBeam;
import org.apache.hop.core.Const;
import org.apache.hop.it.HopEngine;
import org.apache.hop.it.Projects;
import org.apache.hop.it.TestFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Arrays;

class KafkaPipelineTest {

  @BeforeEach
  void setUp() {
    init(Projects.ROOT_PATH);
  }

  @AfterEach
  void tearDown() {}

  static HopEngine[] getLocal() {
    return new HopEngine[] {HopEngine.local};
  }
  static HopEngine[] getFlink() {
    return new HopEngine[] {HopEngine.Flink};
  }

  static HopEngine[] getEngines() {
    return Arrays.stream(HopEngine.values())
        .filter(engine -> engine != HopEngine.Dataflow)
        .toArray(HopEngine[]::new);
  }

  @ParameterizedTest
  @MethodSource("getFlink")
  void runPipeline(HopEngine engine) {
    System.setProperty("run_configuration", engine.toString());
    TestFile testFile = new TestFile("samples");
    testFile.setFile(testFile.find("test2.hpl"));

    MainBeam.main(
        new String[] {
          testFile.getFile().toString(),
          testFile.getProjectCfg("export.json"),
          engine.toString(),
          "123:23"
        });
  }

  private void init(Path rootPath) {
    System.setProperty("HOP_AUDIT_FOLDER", rootPath.resolve("audit").toString());
    System.setProperty("HOP_CONFIG_FOLDER", rootPath.resolve("config").toString());
    System.setProperty(Const.HOP_DISABLE_CONSOLE_LOGGING, "Y");
  }
}
