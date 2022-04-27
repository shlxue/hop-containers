package org.apache.maven.plugins.shade;

import org.apache.hop.beam.pipeline.fatjar.FatJarBuilder;
import org.apache.hop.core.variables.Variables;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JandexIndexTransformerTest {

  @Test
  void testMergeJandexIndex() throws Exception {
    Path basePath = getTestClassPath();
    List<String> indexJars = listJars(basePath);

    String targetJar = mergeByFatJarBuilder(basePath, indexJars);
    String testJar = mergeByTransformer(basePath, indexJars);

    assertTrue(Files.exists(Paths.get(targetJar)));
    assertTrue(Files.exists(Paths.get(testJar)));

    assertJandexIndex(getJandexIndex(targetJar), getJandexIndex(testJar));
  }

  private byte[] getJandexIndex(String jarFile) throws IOException {
    try {
      JarInputStream stream = new JarInputStream(new FileInputStream(jarFile));
      while (true) {
        ZipEntry entry = stream.getNextEntry();
        if (entry == null) {
          break;
        }
        if (JandexIndexTransformer.INDEX_PATH.equals(entry.getName())) {
          return stream.readAllBytes();
        }
      }
    } catch (IOException ignore) {
      Files.delete(Paths.get(jarFile));
    }
    return new byte[0];
  }

  private void assertJandexIndex(byte[] expected, byte[] actual) throws IOException {
    assertNotNull(expected);
    assertNotNull(actual);

    assertIndex(
        new IndexReader(new ByteArrayInputStream(expected)).read(),
        new IndexReader(new ByteArrayInputStream(actual)).read());
  }

  private void assertIndex(Index expected, Index actual) {
    assertThat(actual.getKnownClasses(), hasSize(expected.getKnownClasses().size()));
    for (ClassInfo ci : actual.getKnownClasses()) {
      assertThat(
          actual.getAnnotations(ci.name()), hasSize(expected.getAnnotations(ci.name()).size()));
      assertThat(
          actual.getKnownDirectImplementors(ci.name()),
          hasSize(expected.getKnownDirectImplementors(ci.name()).size()));
      assertThat(
          actual.getKnownDirectSubclasses(ci.name()),
          hasSize(expected.getKnownDirectSubclasses(ci.name()).size()));
      assertThat(
          actual.getAllKnownImplementors(ci.name()),
          hasSize(expected.getAllKnownImplementors(ci.name()).size()));
      assertThat(
          actual.getAllKnownSubclasses(ci.name()),
          hasSize(expected.getAllKnownSubclasses(ci.name()).size()));
    }
  }

  private String mergeByTransformer(Path basePath, List<String> indexJars) {
    String testJar = basePath.resolve("transformer.zip").toString();
    if (!Files.exists(Paths.get(testJar))) {
      JandexIndexTransformer transformer = new JandexIndexTransformer();
      indexJars.forEach(s -> mockMojo(new File(s), transformer));
      if (transformer.hasTransformedResource()) {
        try {
          JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(testJar));
          transformer.modifyOutputStream(outputStream);
          outputStream.close();
        } catch (IOException ignore) {
        }
      }
    }
    return testJar;
  }

  private String mergeByFatJarBuilder(Path basePath, List<String> indexJars) throws Exception {
    String targetJar = basePath.resolve("fat-jar.zip").toString();
    if (!Files.exists(Paths.get(targetJar))) {
      FatJarBuilder fatJarBuilder = new FatJarBuilder(new Variables(), targetJar, indexJars);
      fatJarBuilder.buildTargetJar();
    }
    return targetJar;
  }

  private void mockMojo(File file, JandexIndexTransformer transformer) {
    try (JarFile jarFile = new JarFile(file)) {
      Enumeration<? extends ZipEntry> it = jarFile.entries();
      while (it.hasMoreElements()) {
        ZipEntry entry = it.nextElement();
        if (transformer.canTransformResource(entry.getName())) {
          transformer.processResource(
              entry.getName(), jarFile.getInputStream(entry), null, entry.getTime());
          break;
        }
      }
    } catch (IOException ignore) {
    }
  }

  private Path getTestClassPath() {
    return Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  private List<String> listJars(Path path) throws IOException {
    if (!Files.exists(path)) {
      return Collections.emptyList();
    }
    try (Stream<Path> stream = Files.walk(path, 1)) {
      return stream
          .map(Path::toString)
          .filter(p -> p.endsWith(".jar"))
          .collect(Collectors.toList());
    }
  }
}
