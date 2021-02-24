package analysis;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassSetAnalysisData {
    public static final String PACKAGE_INFO = "package-info";

    private final Set<String> classes;
    private final Map<String, DependentsSet> dependents;
    private final Map<String, Set<Integer>> classesToConstants;
    private final String fullRebuildCause;

    public ClassSetAnalysisData(Set<String> classes, Map<String, DependentsSet> dependents, Map<String, Set<Integer>> classesToConstants, String fullRebuildCause) {
        this.classes = classes;
        this.dependents = dependents;
        this.classesToConstants = classesToConstants;
        this.fullRebuildCause = fullRebuildCause;
    }

    public DependentsSet getDependents(String className) {
        if (fullRebuildCause != null) {
            return DependentsSet.dependencyToAll(fullRebuildCause);
        }
        if (className.endsWith(PACKAGE_INFO)) {
            String packageName = className.equals(PACKAGE_INFO) ? null : StringUtils.removeEnd(className, "." + PACKAGE_INFO);
            return getDependentsOfPackage(packageName);
        }
        DependentsSet dependentsSet = dependents.get(className);
        return dependentsSet == null ? DependentsSet.empty() : dependentsSet;
    }

    private DependentsSet getDependentsOfPackage(String packageName) {
        Set<String> typesInPackage = new HashSet<>();
        for (String type : classes) {
            int i = type.lastIndexOf(".");
            if (i < 0 && packageName == null || i > 0 && type.substring(0, i).equals(packageName)) {
                typesInPackage.add(type);
            }
        }
        return DependentsSet.dependentClasses(Collections.emptySet(), typesInPackage);
    }

    public Set<Integer> getConstants(String className) {
        return classesToConstants.get(className);
    }
}