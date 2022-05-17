package org.apache.hop.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Test;

class SwtSystemColorViewerTest {

  @Test
  void showSwtSystemColor() {
    SwtUtil.handleDialog(this::initSystemColorUI);
  }

  private void initSystemColorUI(Shell shell) {
    shell.setText("SWT System Color");
    shell.setMaximized(true);
    shell.setLayout(new GridLayout(5, false));

    new Label(shell, SWT.NONE).setText("Background");
    new Label(shell, SWT.NONE).setText("Foreground");
    new Label(shell, SWT.NONE).setText("Dynamic(RGB + Alpha)");
    new Label(shell, SWT.NONE).setText("Text sample");
    new Label(shell, SWT.NONE).setText("Value(Alpha + RGB)");

    createColor(shell, "COLOR_WHITE", SWT.COLOR_WHITE);
    createColor(shell, "COLOR_BLACK", SWT.COLOR_BLACK);
    createColor(shell, "COLOR_RED", SWT.COLOR_RED);
    createColor(shell, "COLOR_DARK_RED", SWT.COLOR_DARK_RED);
    createColor(shell, "COLOR_GREEN", SWT.COLOR_GREEN);
    createColor(shell, "COLOR_YELLOW", SWT.COLOR_YELLOW);
    createColor(shell, "COLOR_DARK_YELLOW", SWT.COLOR_DARK_YELLOW);
    createColor(shell, "COLOR_BLUE", SWT.COLOR_BLUE);
    createColor(shell, "COLOR_DARK_BLUE", SWT.COLOR_DARK_BLUE);
    createColor(shell, "COLOR_MAGENTA", SWT.COLOR_MAGENTA);
    createColor(shell, "COLOR_DARK_MAGENTA", SWT.COLOR_DARK_MAGENTA);
    createColor(shell, "COLOR_CYAN", SWT.COLOR_CYAN);
    createColor(shell, "COLOR_DARK_CYAN", SWT.COLOR_DARK_CYAN);
    createColor(shell, "COLOR_GRAY", SWT.COLOR_GRAY);
    createColor(shell, "COLOR_DARK_GRAY", SWT.COLOR_DARK_GRAY);

    createColor(shell, "COLOR_WIDGET_DARK_SHADOW", SWT.COLOR_WIDGET_DARK_SHADOW);
    createColor(shell, "COLOR_WIDGET_NORMAL_SHADOW", SWT.COLOR_WIDGET_NORMAL_SHADOW);
    createColor(shell, "COLOR_WIDGET_LIGHT_SHADOW", SWT.COLOR_WIDGET_LIGHT_SHADOW);
    createColor(shell, "COLOR_WIDGET_HIGHLIGHT_SHADOW", SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    createColor(shell, "COLOR_WIDGET_FOREGROUND", SWT.COLOR_WIDGET_FOREGROUND);
    createColor(shell, "COLOR_WIDGET_BACKGROUND", SWT.COLOR_WIDGET_BACKGROUND);
    createColor(shell, "COLOR_WIDGET_BORDER", SWT.COLOR_WIDGET_BORDER);

    createColor(shell, "COLOR_LIST_FOREGROUND", SWT.COLOR_LIST_FOREGROUND);
    createColor(shell, "COLOR_LIST_BACKGROUND", SWT.COLOR_LIST_BACKGROUND);
    createColor(shell, "COLOR_LIST_SELECTION", SWT.COLOR_LIST_SELECTION);
    createColor(shell, "COLOR_LIST_SELECTION_TEXT", SWT.COLOR_LIST_SELECTION_TEXT);

    createColor(shell, "COLOR_INFO_FOREGROUND", SWT.COLOR_INFO_FOREGROUND);
    createColor(shell, "COLOR_INFO_BACKGROUND", SWT.COLOR_INFO_BACKGROUND);

    createColor(shell, "COLOR_TITLE_FOREGROUND", SWT.COLOR_TITLE_FOREGROUND);
    createColor(shell, "COLOR_TITLE_BACKGROUND", SWT.COLOR_TITLE_BACKGROUND);
    createColor(shell, "COLOR_TITLE_BACKGROUND_GRADIENT", SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
    createColor(shell, "COLOR_TITLE_INACTIVE_FOREGROUND", SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
    createColor(shell, "COLOR_TITLE_INACTIVE_BACKGROUND", SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
    createColor(
        shell,
        "COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT",
        SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);

    createColor(shell, "COLOR_LINK_FOREGROUND", SWT.COLOR_LINK_FOREGROUND);
    createColor(shell, "COLOR_TRANSPARENT", SWT.COLOR_TRANSPARENT);

    createColor(shell, "COLOR_TEXT_DISABLED_BACKGROUND", SWT.COLOR_TEXT_DISABLED_BACKGROUND)
        .setEnabled(false);
    createColor(shell, "COLOR_WIDGET_DISABLED_FOREGROUND", SWT.COLOR_WIDGET_DISABLED_FOREGROUND)
        .setEnabled(false);
  }

  private Text createColor(Composite parent, String name, int id) {
    Display display = parent.getDisplay();

    Color c = display.getSystemColor(id);
    String colorText = String.format("%5d. %s", id, name);

    Text wBackground = new Text(parent, SWT.BORDER | SWT.RIGHT);
    wBackground.setText(name);
    wBackground.setToolTipText(name + "  " + id);
    wBackground.setBackground(c);
    wBackground.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Text wForeground = new Text(parent, SWT.BORDER);
    wForeground.setText(colorText);
    wForeground.setSelection(0, 6);
    wForeground.setToolTipText(name);
    wForeground.setForeground(c);
    wForeground.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite panel = new Composite(parent, SWT.NONE);
    initColorPanel(panel, c);

    Text wLabel = new Text(parent, SWT.NONE);
    wLabel.setText(c.toString());

    Label wColorValue = new Label(parent, SWT.NONE);
    wColorValue.setFont(new Font(parent.getDisplay(), "Monospaced", 14, SWT.NONE));
    wColorValue.setText(
        String.format("%-4d {%4d, %4d, %4d}", c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue()));
    return wForeground;
  }

  private void initColorPanel(Composite panel, Color color) {
    panel.setBackground(color);
    RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
    rowLayout.marginTop = 0;
    rowLayout.marginBottom = 0;
    rowLayout.spacing = 0;
    rowLayout.marginLeft = 50;
    rowLayout.center = true;
    panel.setLayout(rowLayout);

    createSpinner(panel, color.getRed());
    createSpinner(panel, color.getGreen());
    createSpinner(panel, color.getBlue());
    createSpinner(panel, color.getAlpha());
  }

  private void createSpinner(Composite parent, int defaultVal) {
    Spinner wSpinner = new Spinner(parent, SWT.NONE);
    wSpinner.setMinimum(0);
    wSpinner.setMaximum(255);
    wSpinner.setIncrement(5);
    wSpinner.setSelection(defaultVal);
  }
}
