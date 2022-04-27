package org.apache.maven.plugins.shade;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.AppendingTransformer;
import org.jboss.jandex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JandexIndexTransformer extends AppendingTransformer {

    static final String INDEX_PATH = "META-INF/jandex.idx";
    private final Logger log = LoggerFactory.getLogger(JandexIndexTransformer.class);
    private final List<Index> indexList = new ArrayList<>();
    private int count;
    private long time = Long.MIN_VALUE;

    @Override
    public boolean canTransformResource(String r) {
        boolean isIndex = INDEX_PATH.equalsIgnoreCase(r);
        if (isIndex) {
            count++;
        }
        return isIndex;
    }

    @Override
    public void processResource(String resource, InputStream is, List<Relocator> relocators, long time) {
        try {
            indexList.add(new IndexReader(is).read());
            if (time > this.time) {
                this.time = time;
            }
        } catch (Throwable e) {
            log.error("Read jandex index file " + resource, e);
        }
    }

    @Override
    public boolean hasTransformedResource() {
        return count > 0;
    }

    @Override
    public void modifyOutputStream(JarOutputStream jos) throws IOException {
        if (indexList.isEmpty()) {
            return;
        }
        try {
            ZipEntry zipEntry = new ZipEntry(INDEX_PATH);
            zipEntry.setTime(time);
            jos.putNextEntry(zipEntry);
            IndexWriter indexWriter = new IndexWriter(jos);
            Index index = mergeIndexes(CompositeIndex.create(new HashSet<>(indexList)));
            indexWriter.write(index);
            jos.closeEntry();
        } catch (Throwable throwable) {
            throw new IOException("Write jandex index file", throwable);
        }

        indexList.clear();
        count = 0;
    }

    private Index mergeIndexes(CompositeIndex index) {
        Map<DotName, List<AnnotationInstance>> annotations = new TreeMap<>();
        Map<DotName, List<ClassInfo>> subclasses = new TreeMap<>();
        Map<DotName, List<ClassInfo>> implementors = new TreeMap<>();
        Map<DotName, ClassInfo> classes = new TreeMap<>();
        Set<AnnotationInstance> aTimes = new HashSet<>();
        Set<ClassInfo> sTimes = new HashSet<>();
        Set<ClassInfo> iTimes = new HashSet<>();
        Set<ClassInfo> cTimes = new HashSet<>();

        Set<DotName> dotNames = index.getKnownClasses().stream().map(ClassInfo::name).collect(Collectors.toSet());
        for (DotName dotName : dotNames) {
            Collection<AnnotationInstance> annotationInstances = index.getAnnotations(dotName);
            if (!annotationInstances.isEmpty()) {
                annotations.put(dotName, new ArrayList<>(annotationInstances));
                aTimes.addAll(annotationInstances);
            }
            Collection<ClassInfo> classInfos = index.getAllKnownSubclasses(dotName);
            if (!classInfos.isEmpty()) {
                subclasses.put(dotName, new ArrayList<>(classInfos));
                sTimes.addAll(classInfos);
            }
            classInfos = index.getAllKnownImplementors(dotName);
            if (!classInfos.isEmpty()) {
                implementors.put(dotName, new ArrayList<>(classInfos));
                iTimes.addAll(classInfos);
            }
            ClassInfo classInfo = index.getClassByName(dotName);
            if (classInfo != null) {
                classes.put(dotName, index.getClassByName(dotName));
                cTimes.add(classInfo);
            }
        }
        log.info("Merge jandex index from {} jars, index info: {}, {}, {}, {}", count, aTimes.size(), sTimes.size(),
                iTimes.size(), cTimes.size());
        return Index.create(annotations, subclasses, implementors, classes);
    }
}
