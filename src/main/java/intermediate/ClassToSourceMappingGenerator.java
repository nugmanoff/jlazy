package intermediate;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

public class ClassToSourceMappingGenerator implements TaskListener {
    private final Map<String, Collection<String>> mapping = new HashMap<>();
    private final Elements elements;

    public ClassToSourceMappingGenerator(Elements elements) {
        this.elements = elements;
    }

    public Map<String, Collection<String>> getMapping() {
        return mapping;
    }

    @Override
    public void started(TaskEvent e) {
    }

    @Override
    public void finished(TaskEvent e) {
        JavaFileObject sourceFile = e.getSourceFile();
        if (isSourceFile(sourceFile)) {
            File asSourceFile = new File(sourceFile.getName());
            if (isClassGenerationPhase(e)) {
                processSourceFile(e, asSourceFile);
            }
        }
    }

    private void processSourceFile(TaskEvent e, File sourceFile) {
        Optional<String> relativePath = findRelativePath(sourceFile);
        if (relativePath.isPresent()) {
            String key = relativePath.get();
            String symbol = normalizeName(e.getTypeElement());
            registerMapping(key, symbol);
        }
    }

    private Optional<String> findRelativePath(File asSourceFile) {
        return Optional.ofNullable(asSourceFile.toPath().toString());
    }

    private String normalizeName(TypeElement typeElement) {
        String symbol = typeElement.getQualifiedName().toString();
        if (typeElement.getNestingKind().isNested()) {
            symbol = elements.getBinaryName(typeElement).toString();
        }
        return symbol;
    }

    private static boolean isSourceFile(JavaFileObject sourceFile) {
        return sourceFile != null && sourceFile.getKind() == JavaFileObject.Kind.SOURCE;
    }

    private static boolean isClassGenerationPhase(TaskEvent e) {
        return e.getKind() == TaskEvent.Kind.GENERATE;
    }

    public void registerMapping(String key, String symbol) {
        Collection<String> symbols = mapping.computeIfAbsent(key, k -> new TreeSet<>());
        symbols.add(symbol);
    }
}