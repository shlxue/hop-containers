package org.apache.hop.shaded;

import lombok.extern.slf4j.Slf4j;
import org.apache.hop.it.HopEngine;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ArtifactChecker {
  private final Path basePath;
  private final List<Pattern> jarPatterns;
  private final List<Path> jars;
  private Map<Path, List<Artifact>> artifacts = Collections.emptyMap();
  private final SortedMap<String, List<ArtifactInfo>> libJarArtifacts = new TreeMap<>();

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

    return !artifacts.isEmpty();
  }

  public void showRunner(HopEngine hopEngine) {
    List<Path> beamRunnerJars =
        jars.stream().filter(this::isBeamRunner).collect(Collectors.toList());

    if (libJarArtifacts.isEmpty()) {
      List<Path> libJars = jars.stream().filter(this::isLibJar).collect(Collectors.toList());
      merge(libJarArtifacts, libJars);
    }
    SortedMap<String, List<ArtifactInfo>> artifactJars = new TreeMap<>();
    libJarArtifacts.forEach(
        (s, artifactInfos) ->
            artifactJars.put(
                s, artifactInfos.stream().map(ArtifactInfo::new).collect(Collectors.toList())));

    String runner = hopEngine.toString().toLowerCase();
    beamRunnerJars.stream()
        .filter(path -> path.getFileName().toString().contains(runner))
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
    AtomicInteger counter = new AtomicInteger();
    AtomicInteger duplicate = new AtomicInteger();
    log.info("Duplicate artifacts:");
    for (Map.Entry<String, List<ArtifactInfo>> entry : artifactJars.entrySet()) {
      counter.incrementAndGet();
      if (entry.getKey().startsWith("org.apache.hop")) {
        continue;
      }
      if (entry.getValue().size() > 1) {
        duplicate.incrementAndGet();
        log.warn("{}  {}\t {}", format(entry.getValue().size()), entry.getKey(), entry.getValue());
      } else {
        log.debug("       {}", entry.getKey());
      }
    }
    log.info("Summary:");
    log.info("{}  total: {}", format(duplicate.get()), counter.get());
    Map<String, Long> result =
        artifactJars.keySet().stream()
            .map(s -> s.split(":"))
            .collect(Collectors.groupingBy(strings -> strings[0], Collectors.counting()));
    log.info("GroupId summary:");
    result.entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue()))
        .forEach(entry -> log.info("{}  {}", format(entry.getValue()), entry.getKey()));
  }

  private String format(long value) {
    return String.format("%5s", value);
  }

  private void merge(Map<String, List<ArtifactInfo>> artifactJars, List<Path> jars) {
    jars.forEach(path -> fillJar(artifactJars, path, artifacts.get(path)));
  }

  private void fillJar(
      Map<String, List<ArtifactInfo>> artifactJars, Path jarPath, List<Artifact> artifacts) {
    for (Artifact artifact : artifacts) {
      String key = String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
      List<ArtifactInfo> paths = artifactJars.computeIfAbsent(key, k -> new ArrayList<>());
      paths.add(new ArtifactInfo(artifact.getVersion(), jarPath.getFileName().toString()));
    }
  }

  private boolean isLibJar(Path path) {
    return "lib".equals(path.getName(path.getNameCount() - 2).toString());
  }

  private boolean isBeamRunner(Path path) {
    return path.getName(path.getNameCount() - 2).toString().startsWith("lib-");
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

    public ArtifactInfo(ArtifactInfo artifactInfo) {
      this(artifactInfo.version, artifactInfo.jarFile);
      this.diffVer = artifactInfo.diffVer;
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
