package compilation;

import java.util.Collection;
import java.util.Map;

public interface ObservedCompilationResultHandler {
    Map<String, Collection<String>> handleCompilationResult(Map<String, Collection<String>> incrementalCompilationMapping);
}
