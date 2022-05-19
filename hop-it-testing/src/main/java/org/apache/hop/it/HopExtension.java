package org.apache.hop.it;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.logging.HopLogStore;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HopExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
  private Hop hop;

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    hop = extensionContext.getRequiredTestClass().getAnnotation(Hop.class);

    if (!isLazyInit()) {
      applyConfig(getEngine());
      HopClientEnvironment.init();
      HopEnvironment.init();
      List<String> configs = hopConfigs();
      log.debug("Init {} hop configs", configs.size());
      if (log.isTraceEnabled()) {
        log.trace("Hop environments:\n\t{}", String.join("\n\t", configs));
      }
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (!isLazyInit()) {
      HopLogStore.getInstance().reset();
      HopEnvironment.reset();
      HopClientEnvironment.reset();
      cleanConfig();
    }
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {}

  @Override
  public void beforeEach(ExtensionContext extensionContext) {}

  private boolean isLazyInit() {
    return hop != null && hop.lazy();
  }

  private HopEngine getEngine() {
    if (hop != null) {
      return hop.value();
    }
    return HopEngine.local;
  }

  private void applyConfig(HopEngine engine) {
    System.setProperty("HOP_HOME", Projects.HOP_HOME.toString());
    System.setProperty("HOP_AUDIT_FOLDER", Projects.HOP_HOME.resolve("audit").toString());
    System.setProperty("HOP_CONFIG_FOLDER", Projects.HOP_HOME.resolve("config").toString());
    System.setProperty("HOP_ENGINE", engine.toString());
    System.setProperty("run_configuration", engine.toString());
    System.setProperty(Const.HOP_DISABLE_CONSOLE_LOGGING, "Y");
  }

  private void cleanConfig() {
    System.getProperties().stringPropertyNames().stream()
        .filter(this::isHopConfig)
        .forEach(System::clearProperty);
    System.clearProperty("run_configuration");
  }

  private boolean isHopConfig(String key) {
    return key.startsWith("HOP_");
  }

  private List<String> hopConfigs() {
    return System.getProperties().stringPropertyNames().stream()
        .filter(this::isHopConfig)
        .filter(s -> StringUtils.isNotBlank(System.getProperty(s)))
        .sorted()
        .map(s -> String.format("%s = %s", s, System.getProperty(s)))
        .collect(Collectors.toList());
  }
}
