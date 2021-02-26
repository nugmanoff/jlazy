package intermediate;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import file.FileChange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Read and write a file recording source file to class names mapping.
 *
 * The file format is:
 *
 * relative/path/to/source/root/MyGroovyClass.groovy
 * org.gradle.MyGroovyClass
 * org.gradle.MyGroovyClass$1
 * org.gradle.MyGroovyClass$Inner
 */

public class ClassToSourceMapping extends IntermediateProduct {

    protected ClassToSourceMapping(File file, Object object) {
        super(file, object);
    }

    private static final int BUFFER_SIZE = 65536;

    public static Multimap<String, String> readSourceClassesMappingFile(File mappingFile) {
        Multimap<String, String> sourceClassesMapping = MultimapBuilder.SetMultimapBuilder
                .hashKeys()
                .hashSetValues()
                .build();
        if (!mappingFile.isFile()) {
            return sourceClassesMapping;
        }

        try {
            Files.asCharSource(mappingFile, Charsets.UTF_8).readLines(new LineProcessor<Void>() {
                private String currentFile;

                @Override
                public boolean processLine(String line) throws IOException {
                    if (line.startsWith(" ")) {
                        sourceClassesMapping.put(currentFile, line.substring(1));
                    } else {
                        currentFile = line;
                    }

                    return true;
                }

                @Override
                public Void getResult() {
                    return null;
                }
            });
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        return sourceClassesMapping;
    }

    public static void writeSourceClassesMappingFile(File mappingFile, Map<String, Collection<String>> mapping) {
        try (Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mappingFile, false), StandardCharsets.UTF_8), BUFFER_SIZE)) {
            for (Map.Entry<String, Collection<String>> entry : mapping.entrySet()) {
                wrt.write(entry.getKey() + "\n");
                for (String className : entry.getValue()) {
                    wrt.write(" " + className + "\n");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeSourceClassesMappingFile(File mappingFile, Multimap<String, String> mapping) {
        writeSourceClassesMappingFile(mappingFile, mapping.asMap());
    }

    public static void mergeIncrementalMappingsIntoOldMappings(File sourceClassesMappingFile,
                                                               List<FileChange> fileChanges,
                                                               Multimap<String, String> oldMappings) {
        Multimap<String, String> mappingsDuringIncrementalCompilation = readSourceClassesMappingFile(sourceClassesMappingFile);

        StreamSupport.stream(fileChanges.spliterator(), false)
                .filter(fileChange -> fileChange.getType() == FileChange.Type.REMOVE)
//                .map(FileChange::getNormalizedPath)
                .forEach(oldMappings::removeAll);
        mappingsDuringIncrementalCompilation.keySet().forEach(oldMappings::removeAll);

        oldMappings.putAll(mappingsDuringIncrementalCompilation);

        writeSourceClassesMappingFile(sourceClassesMappingFile, oldMappings);
    }

}
