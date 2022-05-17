package org.apache.hop.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

class TableViewTest {
  @Test
  void showTableView() {
    SwtUtil.handleDialog(this::initTableUI);
  }

  private void initTableUI(Shell shell) {
    shell.setLayout(new GridLayout());

    Table wTable = new Table(shell, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.FULL_SELECTION);
    wTable.setHeaderVisible(true);
    wTable.setLayoutData(new GridData(GridData.FILL_BOTH));
    wTable.addListener(SWT.EraseItem, this::eraseItemListener);

    createColumn(wTable, "Char");
    createColumn(wTable, "Value");

    AtomicInteger charValue = new AtomicInteger(33);
    IntStream.range(0, 100)
        .forEach(
            value -> {
              String c = Character.toString(charValue.getAndIncrement());
              new TableItem(wTable, SWT.CENTER).setText(new String[] {c, Integer.toString(value)});
            });
    wTable.setItemCount(100);
  }

  private void eraseItemListener(Event event) {
    if (!(event.widget instanceof Table)) {
      return;
    }
    Table table = (Table) event.widget;
    int index = table.indexOf((TableItem) event.item);
    if (index % 2 == 0) {
      Color oldBackground = event.gc.getBackground();
      event.gc.setBackground(event.display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      event.gc.fillRectangle(0, event.y, table.getClientArea().width, event.height);
      event.gc.setBackground(oldBackground);
    }
  }

  private void createColumn(Table table, String title) {
    TableColumn column = new TableColumn(table, SWT.NULL);
    column.setText(title);
    column.setWidth(180);
  }
}
