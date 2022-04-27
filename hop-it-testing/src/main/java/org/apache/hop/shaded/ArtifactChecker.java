package org.apache.hop.shaded;

import org.apache.hop.it.HopEngine;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArtifactChecker {
  private final Path basePath;
  private final List<Pattern> jarPatterns;
  private final List<Path> jars;
  private Map<Path, List<Artifact>> artifacts = Collections.emptyMap();

  public ArtifactChecker(Path basePath, String... jarPattern) {
    this.basePath = basePath;
    this.jarPatterns = Arrays.stream(jarPattern).map(Pattern::compile).collect(Collectors.toList());
    jars = findJars();
  }

  public boolean parse() {
    if (artifacts.isEmpty()) {
      artifacts =
          jars.stream()
              .parallel()
              .map(path -> new AbstractMap.SimpleEntry<>(path, list(path)))
              .collect(
                  Collectors.toMap(
                      AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
    //    artifacts.forEach(this::print);

    return !artifacts.isEmpty();
  }

  public void showRunner(HopEngine hopEngine) {
    List<Path> beamRunnerJars =
        jars.stream().filter(this::isBeamRunner).collect(Collectors.toList());
    List<Path> libJars = jars.stream().filter(this::isLibJar).collect(Collectors.toList());

    SortedMap<String, List<ArtifactInfo>> artifactJars = new TreeMap<>();
    merge(artifactJars, libJars);
    beamRunnerJars.stream()
        .filter(path -> path.getFileName().toString().contains("flink"))
        .findFirst()
        .ifPresent(path -> merge(artifactJars, Collections.singletonList(path)));
    artifactJars
        .values()
        .forEach(
            artifactInfos -> {
              if (artifactInfos.stream().map(ArtifactInfo::getVersion).distinct().count() > 1) {
                artifactInfos.forEach(artifactInfo -> artifactInfo.setDiffVer(true));
              }
            });
    print(artifactJars);
  }

  private void print(Map<String, List<ArtifactInfo>> artifactJars) {
    for (Map.Entry<String, List<ArtifactInfo>> entry : artifactJars.entrySet()) {
      if (entry.getKey().startsWith("org.apache.hop")) {
        continue;
      }
      if (entry.getValue().size() > 1) {
        System.out.printf(
            "\t%d  %s\t %s%n", entry.getValue().size(), entry.getKey(), entry.getValue());
      } else {
        System.out.printf("\t   %s%n", entry.getKey());
      }
    }
  }

  private void merge(Map<String, List<ArtifactInfo>> artifactJars, List<Path> jars) {
    jars.forEach(path -> fillJar(artifactJars, path, artifacts.get(path)));
  }

  private void fillJar(
      Map<String, List<ArtifactInfo>> artifactJars, Path jarPath, List<Artifact> artifacts) {
    for (Artifact artifact : artifacts) {
      String key = String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
      List<ArtifactInfo> paths = artifactJars.get(key);
      if (paths == null) {
        paths = new ArrayList<>();
        artifactJars.put(key, paths);
      }
      paths.add(new ArtifactInfo(artifact.getVersion(), jarPath.getFileName().toString()));
    }
  }

  private boolean isLibJar(Path path) {
    return "lib".equals(path.getName(path.getNameCount() - 2).toString());
  }

  private boolean isBeamRunner(Path path) {
    return path.getName(path.getNameCount() - 2).toString().startsWith("lib-");
  }

  private void print(Path path, List<Artifact> artifacts) {
    System.out.println(path);
    artifacts.forEach(this::print);
  }

  private void print(Artifact artifact) {
    System.out.println(String.format("\t%s", artifact));
  }

  private List<Artifact> list(Path path) {
    SortedSet<Artifact> artifacts = new TreeSet<>();
    try (ZipInputStream stream = new ZipInputStream(new FileInputStream(path.toFile()))) {
      while (true) {
        ZipEntry entry = stream.getNextEntry();
        if (entry == null) {
          break;
        }
        if (!isPomPropertyFile(entry.getName())) {
          continue;
        }
        getArtifact(stream.readAllBytes()).ifPresent(artifacts::add);
      }
    } catch (IOException ignore) {
    }
    return new ArrayList<>(artifacts);
  }

  private Optional<Artifact> getArtifact(byte[] buf) {
    Properties prop = new Properties();
    try {
      prop.load(new ByteArrayInputStream(buf));
      if (prop.size() >= 3) {
        if (!(prop.containsKey("version")
            && prop.containsKey("groupId")
            && prop.containsKey("artifactId"))) {
          System.out.println(prop);
        }
        return Optional.of(
            new DefaultArtifact(
                prop.getProperty("groupId"),
                prop.getProperty("artifactId"),
                prop.getProperty("version"),
                null,
                "jar",
                "",
                null));
      }
    } catch (IOException ignore) {
    }
    return Optional.empty();
  }

  private boolean isPomPropertyFile(String name) {
    return name.endsWith("/pom.properties");
  }

  public static void main(String[] args) {}

  private List<Path> findJars() {
    try (Stream<Path> stream = Files.walk(basePath)) {
      return stream
          .filter(this::isJarFile)
          .filter(this::match)
          .sorted()
          .collect(Collectors.toList());
    } catch (IOException ignore) {
    }
    return Collections.emptyList();
  }

  private boolean match(Path path) {
    String jarFileName = path.getFileName().toString().toLowerCase();
    return jarPatterns.stream().anyMatch(pattern -> pattern.matcher(jarFileName).matches());
  }

  private boolean isJarFile(Path path) {
    return path.getFileName().toString().toLowerCase().endsWith(".jar");
  }

  private static class ArtifactInfo {
    private final String version;
    private final String jarFile;
    private boolean diffVer;

    public ArtifactInfo(String version, String jarFile) {
      this.version = version;
      this.jarFile = jarFile;
    }

    public String getVersion() {
      return version;
    }

    public void setDiffVer(boolean diffVer) {
      this.diffVer = diffVer;
    }

    @Override
    public String toString() {
      if (diffVer) {
        return String.format("%s: %s", version, jarFile);
      }
      return jarFile;
    }
  }
}
