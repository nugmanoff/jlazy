package analysis;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassDependentsAccumulator {

    private final Set<String> dependenciesToAll = new HashSet<>();
    private final Map<String, Set<String>> privateDependents = new HashMap<>();
    private final Map<String, Set<String>> accessibleDependents = new HashMap<>();
    private final ImmutableMap.Builder<String, Set<Integer>> classesToConstants = ImmutableMap.builder();
    private final Set<String> seenClasses = new HashSet<>();
    private String fullRebuildCause;

    public void addClass(ClassAnalysis classAnalysis) {
        addClass(classAnalysis.getClassName(), classAnalysis.isDependencyToAll(), classAnalysis.getPrivateClassDependencies(), classAnalysis.getAccessibleClassDependencies(), classAnalysis.getConstants());
    }

    public void addClass(String className, boolean dependencyToAll, Iterable<String> privateClassDependencies, Iterable<String> accessibleClassDependencies, Set<Integer> constants) {
        if (seenClasses.contains(className)) {
            // same classes may be found in different classpath trees/jars
            // and we keep only the first one
            return;
        }
        seenClasses.add(className);
        if (!constants.isEmpty()) {
            classesToConstants.put(className, constants);
        }
        if (dependencyToAll) {
            dependenciesToAll.add(className);
            privateDependents.remove(className);
            accessibleDependents.remove(className);
        }
        for (String dependency : privateClassDependencies) {
            if (!dependency.equals(className) && !dependenciesToAll.contains(dependency)) {
                addDependency(privateDependents, dependency, className);
            }
        }
        for (String dependency : accessibleClassDependencies) {
            if (!dependency.equals(className) && !dependenciesToAll.contains(dependency)) {
                addDependency(accessibleDependents, dependency, className);
            }
        }
    }

    private Set<String> rememberClass(Map<String, Set<String>> dependents, String className) {
        return dependents.computeIfAbsent(className, k -> new HashSet<>());
    }

    /*
        key -> className
        value -> analysis.DependentsSet (which is whether (mutually exclusive) DependencyToAll, or DefaultDependentsSet with Accessible & Private Dependents)
    */
    @VisibleForTesting
    public Map<String, DependentsSet> getDependentsMap() {
        if (dependenciesToAll.isEmpty() && privateDependents.isEmpty() && accessibleDependents.isEmpty()) {
            return Collections.emptyMap();
        }
        ImmutableMap.Builder<String, DependentsSet> builder = ImmutableMap.builder();
        for (String s : dependenciesToAll) {
            builder.put(s, DependentsSet.dependencyToAll());
        }
        Set<String> collected = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : accessibleDependents.entrySet()) {
            if (collected.add(entry.getKey())) {
                builder.put(entry.getKey(), DependentsSet.dependentClasses(privateDependents.getOrDefault(entry.getKey(), Collections.emptySet()), entry.getValue()));
            }
        }
        for (Map.Entry<String, Set<String>> entry : privateDependents.entrySet()) {
            if (collected.add(entry.getKey())) {
                builder.put(entry.getKey(), DependentsSet.dependentClasses(entry.getValue(), accessibleDependents.getOrDefault(entry.getKey(), Collections.emptySet())));
            }
        }
        return builder.build();
    }

    @VisibleForTesting
    Map<String, Set<Integer>> getClassesToConstants() {
        return classesToConstants.build();
    }

    private void addDependency(Map<String, Set<String>> dependentsMap, String dependency, String dependent) {
        Set<String> dependents = rememberClass(dependentsMap, dependency);
        dependents.add(dependent);
    }

    public void fullRebuildNeeded(String fullRebuildCause) {
        this.fullRebuildCause = fullRebuildCause;
    }

    public ClassSetAnalysisData getAnalysis() {
        return new ClassSetAnalysisData(ImmutableSet.copyOf(seenClasses), getDependentsMap(), getClassesToConstants(), fullRebuildCause);
    }

}
