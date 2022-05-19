package org.apache.hop.it;

import lombok.extern.slf4j.Slf4j;
import org.apache.hop.core.Const;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.config.plugin.ConfigPlugin;
import org.apache.hop.core.config.plugin.ConfigPluginType;
import org.apache.hop.core.config.plugin.IConfigOptions;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.JarCache;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.metadata.util.HopMetadataUtil;
import org.apache.hop.run.HopRun;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HopRunner extends HopRun {
  private static String[] appendEnvConfigs(String hopHome, String[] args) {
    Properties props = new Properties();
//    String hopPrefix = "HOP_";
//    for (String key : System.getenv().keySet()) {
//      if (key.startsWith(hopPrefix)) {
//        props.put(key, System.getenv(key));
//      }
//    }
//    for (String key : System.getProperties().stringPropertyNames()) {
//      if (key.startsWith(hopPrefix)) {
//        props.put(key, System.getProperty(key));
//      }
//    }
//    if (!props.isEmpty()) {
      String value = String.format("HOP_HOME=%s", hopHome);
//          props.stringPropertyNames().stream()
//              .filter(s -> !StringUtil.isEmpty(props.getProperty(s)))
//              .map(s -> String.format("%s=%s", s, props.getProperty(s)))
//              .collect(Collectors.joining(","));
      boolean exist = false;
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.startsWith("-s=") || arg.startsWith("--system-properties=")) {
          args[i] = arg + "," + value;
          exist = true;
          break;
        }
      }
      if (!exist) {
        args = Stream.concat(Stream.of(args), Stream.of("-s=" + value)).toArray(String[]::new);
      }
//    }
    return args;
  }

  private static void applyConfig(String hopHome) {
    if (StringUtil.isEmpty(getProperty("HOP_AUDIT_FOLDER"))) {
      System.setProperty("HOP_AUDIT_FOLDER", Paths.get(hopHome, "audit").toString());
    }
    if (StringUtil.isEmpty(getProperty("HOP_CONFIG_FOLDER"))) {
      System.setProperty("HOP_CONFIG_FOLDER", Paths.get(hopHome, "config").toString());
    }
  }

  static String getProperty(String key) {
    return System.getProperty(key, System.getenv(key));
  }

  public static void main(String[] args) {
    String hopHome = getProperty("HOP_HOME");
    if (hopHome == null) {
      hopHome = ".";
    }
    log.info("user.dir = {}", System.getProperty("user.dir"));
    log.info("HOP_HOME = {}", hopHome);

    System.setProperty(Const.HOP_DISABLE_CONSOLE_LOGGING, "Y");
    if (!".".equals(hopHome)) {
      applyConfig(hopHome);
      args = appendEnvConfigs(hopHome, args);
    }

    HopRunner hopRun = new HopRunner();
    try {
      CommandLine cmd = new CommandLine(hopRun);

      hopRun.applySystemProperties();

      HopEnvironment.init();

      hopRun.setVariables(Variables.getADefaultVariableSpace());

      HopLogStore.init();

      JarCache.getInstance().clear();

      hopRun.setMetadataProvider(
          HopMetadataUtil.getStandardHopMetadataProvider(hopRun.getVariables()));

      // Now add run configuration plugins...
      //
      List<IPlugin> configPlugins = PluginRegistry.getInstance().getPlugins(ConfigPluginType.class);
      for (IPlugin configPlugin : configPlugins) {
        // Load only the plugins of the "run" category
        if (ConfigPlugin.CATEGORY_RUN.equals(configPlugin.getCategory())) {
          IConfigOptions configOptions =
              PluginRegistry.getInstance().loadClass(configPlugin, IConfigOptions.class);
          cmd.addMixin(configPlugin.getIds()[0], configOptions);
        }
      }
      hopRun.setCmd(cmd);

      CommandLine.ParseResult parseResult = cmd.parseArgs(args);

      if (!CommandLine.printHelpIfRequested(parseResult)) {
        hopRun.run();
        if (!hopRun.isFinishedWithoutError()) {
          // TODO throw error
          System.exit(1);
        }
      }
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException(e);
    }
  }
}
