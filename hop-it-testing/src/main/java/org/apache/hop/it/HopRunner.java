package org.apache.hop.it;

import org.apache.hop.core.Const;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.config.plugin.ConfigPlugin;
import org.apache.hop.core.config.plugin.ConfigPluginType;
import org.apache.hop.core.config.plugin.IConfigOptions;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.JarCache;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.metadata.util.HopMetadataUtil;
import org.apache.hop.run.HopRun;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.List;
public class HopRunner extends HopRun {
  static {
    String key = "HOP_HOME";
    String value = System.getProperty(key, System.getenv(key));
    if (value != null && value.startsWith(System.getProperty("user.dir"))){
      value = Paths.get(value).relativize(Paths.get(System.getProperty("user.dir"))).toString();
      if (value.length() == 0){
        value = ".";
      }
    }
    if (value != null) {
      System.setProperty(key, value);
    }
    System.setProperty(Const.HOP_DISABLE_CONSOLE_LOGGING, "Y");
  }

  public static void main(String[] args) {

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
          System.exit(1);
        }
      }
    } catch (CommandLine.ParameterException e) {
      System.err.println(e.getMessage());
      hopRun.getCmd().usage(System.err);
      throw new RuntimeException(e);
    } catch (CommandLine.ExecutionException e) {
      System.err.println("Error found during execution!");
      System.err.println(Const.getStackTracker(e));

      throw new RuntimeException(e);
    } catch (Exception e) {
      System.err.println("General error found, something went horribly wrong!");
      System.err.println(Const.getStackTracker(e));
      throw new RuntimeException(e);
    }
  }
}
