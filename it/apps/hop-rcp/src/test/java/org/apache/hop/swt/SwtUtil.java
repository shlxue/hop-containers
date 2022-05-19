package org.apache.hop.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class SwtUtil {
  static final long swtAutoCloseDelayMilli;

  static {
    String key = "SWT_AUTO_CLOSE_DELAY_MILLI";
    swtAutoCloseDelayMilli =
        Long.parseLong(
            Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse("0"));
  }

  public static void handleDialog(Consumer<Shell> shellConsumer) {
    Display display = new Display();
    Shell shell = new Shell();

    shellConsumer.accept(shell);

    if (swtAutoCloseDelayMilli > 0) {
      display.asyncExec(
          () -> {
            try {
              TimeUnit.MILLISECONDS.sleep(swtAutoCloseDelayMilli);
              shell.close();
              shell.dispose();
            } catch (InterruptedException ignore) {
            }
          });
    }

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }
}
