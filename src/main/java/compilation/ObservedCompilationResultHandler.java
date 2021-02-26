package compilation;

import java.util.Collection;
import java.util.Map;

public interface ObservedCompilationResultHandler {
    void handleCompilationResult(Map<String, Collection<String>> incrementalCompilationMapping);
}
