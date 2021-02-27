package analysis;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassSetAnalysisData {
    private final Map<String, DependentsSet> dependents;
    private final Map<String, Set<Integer>> classesToConstants;
    private final String fullRebuildCause;

    public ClassSetAnalysisData(Map<String, DependentsSet> dependents, Map<String, Set<Integer>> classesToConstants, String fullRebuildCause) {
        this.dependents = dependents;
        this.classesToConstants = classesToConstants;
        this.fullRebuildCause = fullRebuildCause;
    }

    public DependentsSet getDependents(String className) {
        if (fullRebuildCause != null) {
            return DependentsSet.dependencyToAll(fullRebuildCause);
        }

        DependentsSet dependentsSet = dependents.get(className);
        return dependentsSet == null ? DependentsSet.empty() : dependentsSet;
    }


    public Set<Integer> getConstants(String className) {
        return classesToConstants.computeIfAbsent(className, clazzName -> new HashSet<>());
    }
}