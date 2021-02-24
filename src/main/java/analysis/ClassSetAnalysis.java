package analysis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassSetAnalysis {

    private final ClassSetAnalysisData classAnalysis;

    public ClassSetAnalysis(ClassSetAnalysisData classAnalysis) {
        this.classAnalysis = classAnalysis;
    }

    public DependentsSet getRelevantDependents(Iterable<String> classes, Set<Integer> constants) {
        final Set<String> accessibleResultClasses = new LinkedHashSet<>();
        final Set<String> privateResultClasses = new LinkedHashSet<>();
        for (String cls : classes) {
            DependentsSet d = getRelevantDependents(cls, constants);
            if (d.isDependencyToAll()) {
                return d;
            }
            if (d.isEmpty()) {
                continue;
            }
            Set<String> accessibleDependentClasses = d.getAccessibleDependentClasses();
            Set<String> privateDependentClasses = d.getPrivateDependentClasses();

            accessibleResultClasses.addAll(accessibleDependentClasses);
            privateResultClasses.addAll(privateDependentClasses);
        }
        return DependentsSet.dependents(privateResultClasses, accessibleResultClasses);
    }

    public DependentsSet getRelevantDependents(String className, Set<Integer> constants) {
        DependentsSet deps = getDependents(className);
        if (deps.isDependencyToAll()) {
            return deps;
        }
        if (!constants.isEmpty()) {
            return DependentsSet.dependencyToAll();
        }

        if (!deps.hasDependentClasses()) {
            return deps;
        }

        Set<String> privateResultClasses = new HashSet<>();
        Set<String> accessibleResultClasses = new HashSet<>();
        processDependentClasses(new HashSet<>(), privateResultClasses, accessibleResultClasses, deps.getPrivateDependentClasses(), deps.getAccessibleDependentClasses());
        accessibleResultClasses.remove(className);
        privateResultClasses.remove(className);

        return DependentsSet.dependents(privateResultClasses, accessibleResultClasses);
    }

    public boolean isDependencyToAll(String className) {
        return classAnalysis.getDependents(className).isDependencyToAll();
    }

    /**
     * Accumulate dependent classes and resources. Dependent classes discovered can themselves be used to query
     * further dependents, while resources are just data accumulated along the way. Recurses for classes that
     * are "publicly accessbile", i.e. classes that are not just used privately in a class.
     */
    private void processDependentClasses(Set<String> visitedClasses,
                                         Set<String> privateResultClasses,
                                         Set<String> accessibleResultClasses,
                                         Iterable<String> privateDependentClasses,
                                         Iterable<String> accessibleDependentClasses) {

        for (String privateDependentClass : privateDependentClasses) {
            if (!visitedClasses.add(privateDependentClass)) {
                continue;
            }
            privateResultClasses.add(privateDependentClass);
            DependentsSet currentDependents = getDependents(privateDependentClass);
        }

        processTransitiveDependentClasses(visitedClasses, accessibleResultClasses, accessibleDependentClasses);
    }

    private void processTransitiveDependentClasses(Set<String> visitedClasses,
                                                   Set<String> accessibleResultClasses,
                                                   Iterable<String> accessibleDependentClasses) {
        Deque<String> remainingAccessibleDependentClasses = new ArrayDeque<>();
        for (String accessibleDependentClass : accessibleDependentClasses) {
            remainingAccessibleDependentClasses.add(accessibleDependentClass);
        }

        while (!remainingAccessibleDependentClasses.isEmpty()) {
            String accessibleDependentClass = remainingAccessibleDependentClasses.pop();
            if (!visitedClasses.add(accessibleDependentClass)) {
                continue;
            }
            accessibleResultClasses.add(accessibleDependentClass);
            DependentsSet currentDependents = getDependents(accessibleDependentClass);
            if (!currentDependents.isDependencyToAll()) {
                remainingAccessibleDependentClasses.addAll(currentDependents.getAccessibleDependentClasses());
            }
        }
    }

    private DependentsSet getDependents(String className) {
        DependentsSet dependents = classAnalysis.getDependents(className);
        if (dependents.isDependencyToAll()) {
            return dependents;
        }

        return DependentsSet.dependents(dependents.getPrivateDependentClasses(), dependents.getAccessibleDependentClasses());
    }

    public Set<Integer> getConstants(String className) {
        return classAnalysis.getConstants(className);
    }
}
