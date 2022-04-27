package org.apache.hop.tools;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

class TestConsole {
    static OutputStream stream = new ByteArrayOutputStream();
    static PrintStream originOut;
  @BeforeAll
  static void beforeAll() {
    originOut = System.out;
    System.setOut(new PrintStream(stream));
  }

  @AfterAll
  static void afterAll() {
    System.setOut(originOut);
  }

  @BeforeEach
  void setUp() {
  }
}
