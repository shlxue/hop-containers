package org.apache.hop.tools;

import org.apache.hop.core.encryption.Encr;
import org.junit.jupiter.api.Test;

class EncrIT {
  @Test
  void testMain() throws Exception {
//    Encr.main(new String[0]);
    Encr.main(new String[]{"-hop", "123456"});
  }
}
