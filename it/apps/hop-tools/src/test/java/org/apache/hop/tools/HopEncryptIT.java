package org.apache.hop.tools;

import org.apache.hop.encryption.HopEncrypt;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

class HopEncryptIT extends TestConsole {

  @Test
  void testMain() throws Exception {
      OutputStream stream = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stream));
//      HopEncrypt.main(new String[0]);
      HopEncrypt.main(new String[]{"-hop", "123456"});
  }
}
