package analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassDependentsAggregator {

    private final Set<String> dependenciesToAll = new HashSet<>();
    private final Map<String, Set<String>> privateDependents = new HashMap<>();
    private final Map<String, Set<String>> publicDependents = new HashMap<>();

    public void addClass(ClassAnalysis classAnalysis) {
        addClass(classAnalysis.getClassName(), classAnalysis.isDependencyToAll(), classAnalysis.getPrivateDependencies(), classAnalysis.getPublicDependencies());
    }

    public void addClass(String className, boolean isDependencyToAll, Iterable<String> privateClassDependencies, Iterable<String> publicClassDependencies) {
        if (isDependencyToAll) {
            dependenciesToAll.add(className);
            privateDependents.remove(className);
            publicDependents.remove(className);
        }
        for (String dependency : privateClassDependencies) {
            if (!dependency.equals(className) && !dependenciesToAll.contains(dependency)) {
                addDependency(privateDependents, dependency, className);
            }
        }
        for (String dependency : publicClassDependencies) {
            if (!dependency.equals(className) && !dependenciesToAll.contains(dependency)) {
                addDependency(publicDependents, dependency, className);
            }
        }
    }

    public Map<String, DependentsSet> getDependentsMap() {
        if (dependenciesToAll.isEmpty() && privateDependents.isEmpty() && publicDependents.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DependentsSet> dependentsMap = new HashMap<>();
        for (String s : dependenciesToAll) {
            dependentsMap.put(s, DependentsSet.dependencyToAll());
        }
        Set<String> collected = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : publicDependents.entrySet()) {
            if (collected.add(entry.getKey())) {
                dependentsMap.put(entry.getKey(), DependentsSet.dependentClasses(privateDependents.getOrDefault(entry.getKey(), Collections.emptySet()), entry.getValue()));
            }
        }
        for (Map.Entry<String, Set<String>> entry : privateDependents.entrySet()) {
            if (collected.add(entry.getKey())) {
                dependentsMap.put(entry.getKey(), DependentsSet.dependentClasses(entry.getValue(), publicDependents.getOrDefault(entry.getKey(), Collections.emptySet())));
            }
        }
        return dependentsMap;
    }


    private Set<String> rememberClass(Map<String, Set<String>> dependents, String className) {
        return dependents.computeIfAbsent(className, k -> new HashSet<>());
    }

    private void addDependency(Map<String, Set<String>> dependentsMap, String dependency, String dependent) {
        Set<String> dependents = rememberClass(dependentsMap, dependency);
        dependents.add(dependent);
    }
}
