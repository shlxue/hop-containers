package org.apache.hop.it;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LaunchCmdTest {

  @Test
  void testArgs() {
    assertThat(
        new LaunchCmd().custom(),
        allOf(
            arrayWithSize(2),
            arrayContaining(startsWith("-main"), startsWith("-lib"))));
    assertThat(new LaunchCmd().custom("-h"), arrayWithSize(4));
    assertThat(new LaunchCmd().args("a"), arrayWithSize(6));

    String[] args = new LaunchCmd("samples", "Flink").args("b");
    assertThat(args, arrayWithSize(6));
    assertThat(args[0], endsWith(HopRunner.class.getName()));
    assertThat(args[1], startsWith("-lib"));
    assertThat(args[4], equalTo("-f=b"));
  }
}
