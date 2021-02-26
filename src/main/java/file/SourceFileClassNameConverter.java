package file;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SourceFileClassNameConverter {
    private final Multimap<String, String> sourceClassesMapping;
    private final Map<String, String> classSourceMapping;

    public SourceFileClassNameConverter(Multimap<String, String> sourceClassesMapping) {
        this.sourceClassesMapping = sourceClassesMapping;
        this.classSourceMapping = constructReverseMapping(sourceClassesMapping);
    }

    private Map<String, String> constructReverseMapping(Multimap<String, String> sourceClassesMapping) {
        return sourceClassesMapping.entries().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public Collection<String> getClassNames(String sourceFileRelativePath) {
        return sourceClassesMapping.get(sourceFileRelativePath);
    }

    public boolean isEmpty() {
        return classSourceMapping.isEmpty();
    }

    public Optional<String> getRelativeSourcePath(String fqcn) {
        return Optional.ofNullable(classSourceMapping.get(fqcn));
    }
}
