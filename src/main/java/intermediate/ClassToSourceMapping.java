package intermediate;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ClassToSourceMapping extends IntermediateProduct {

    public ClassToSourceMapping(File file, Object object) {
        super(file, object);
    }

    @Override
    public void read() {
        setObject(readSourceClassesMappingFile());
    }

    @Override
    public void write() {
        writeSourceClassesMappingFile((Multimap<String, String>) object);
    }

    public void mergeIncrementalMappingsIntoOldMappings(List<String> deletedFileNames, Multimap<String, String> mappingsDuringIncrementalCompilation) {
        read();
        Multimap<String, String> oldMappings = (Multimap<String, String>) object;
        System.out.println("@ JLazy > Маппинги удалённые во время мерджа >" + deletedFileNames);
        deletedFileNames.forEach(oldMappings::removeAll);

        mappingsDuringIncrementalCompilation.keySet().forEach(oldMappings::removeAll);

        oldMappings.putAll(mappingsDuringIncrementalCompilation);

        writeSourceClassesMappingFile(oldMappings);
    }

    private Multimap<String, String> readSourceClassesMappingFile() {
        Multimap<String, String> sourceClassesMapping = MultimapBuilder.SetMultimapBuilder
                .hashKeys()
                .hashSetValues()
                .build();

        if (!file.isFile()) {
            return sourceClassesMapping;
        }

        try {
            Files.asCharSource(file, Charsets.UTF_8).readLines(new LineProcessor<Void>() {
                private String currentFile;

                @Override
                public boolean processLine(String line)  {
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

    private void writeSourceClassesMappingFile(Multimap<String, String> mapping) {
        Map<String, Collection<String>> rawMapping = mapping.asMap();
        try (Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, Collection<String>> entry : rawMapping.entrySet()) {
                wrt.write(entry.getKey() + "\n");
                for (String className : entry.getValue()) {
                    wrt.write(" " + className + "\n");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

