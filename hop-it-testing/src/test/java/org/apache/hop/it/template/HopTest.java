package org.apache.hop.it.template;

import org.apache.hop.it.HopExtension;
import org.apache.hop.it.HopRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
public class HopTest {
  @Test
  void hopRunHelp() {
    Assertions.assertDoesNotThrow(() -> HopRunner.main(new String[] {"-h"}));
  }
}
