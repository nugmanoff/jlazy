package analysis;

import java.util.*;

public class AggregatedAnalysisStrategy {

    private final Map<String, DependentsSet> dependentsMap;

    public AggregatedAnalysisStrategy(Map<String, DependentsSet> dependentsMap) {
        this.dependentsMap = dependentsMap;
    }

    public DependentsSet getRelevantDependents(Iterable<String> classes) {
        final Set<String> publicResultClasses = new LinkedHashSet<>();
        final Set<String> privateResultClasses = new LinkedHashSet<>();
        for (String cls : classes) {
            DependentsSet d = getRelevantDependents(cls);
            if (d.isDependencyToAll()) {
                return d;
            }
            if (d.isEmpty()) {
                continue;
            }
            Set<String> publicDependentClasses = d.getPublicDependentClasses();
            Set<String> privateDependentClasses = d.getPrivateDependentClasses();

            publicResultClasses.addAll(publicDependentClasses);
            privateResultClasses.addAll(privateDependentClasses);
        }
        return DependentsSet.dependents(privateResultClasses, publicResultClasses);
    }

    public DependentsSet getRelevantDependents(String className) {
        DependentsSet deps = getDependents(className);
        if (deps.isDependencyToAll()) {
            return deps;
        }
        if (!deps.hasDependentClasses()) {
            return deps;
        }
        Set<String> privateResultClasses = new HashSet<>();
        Set<String> publicResultClasses = new HashSet<>();
        processDependentClasses(new HashSet<>(), privateResultClasses, publicResultClasses, deps.getPrivateDependentClasses(), deps.getPublicDependentClasses());
        publicResultClasses.remove(className);
        privateResultClasses.remove(className);

        return DependentsSet.dependents(privateResultClasses, publicResultClasses);
    }

    private void processDependentClasses(Set<String> visitedClasses,
                                         Set<String> privateResultClasses,
                                         Set<String> publicResultClasses,
                                         Iterable<String> privateDependentClasses,
                                         Iterable<String> publicDependentClasses) {
        for (String privateDependentClass : privateDependentClasses) {
            if (!visitedClasses.add(privateDependentClass)) {
                continue;
            }
            privateResultClasses.add(privateDependentClass);
        }
        processTransitiveDependentClasses(visitedClasses, publicResultClasses, publicDependentClasses);
    }

    private void processTransitiveDependentClasses(Set<String> visitedClasses,
                                                   Set<String> publicResultClasses,
                                                   Iterable<String> publicDependentClasses) {
        Deque<String> remainingPublicDependentClasses = new ArrayDeque<>();
        for (String publicDependentClass : publicDependentClasses) {
            remainingPublicDependentClasses.add(publicDependentClass);
        }

        while (!remainingPublicDependentClasses.isEmpty()) {
            String publicDependentClass = remainingPublicDependentClasses.pop();
            if (!visitedClasses.add(publicDependentClass)) {
                continue;
            }
            publicResultClasses.add(publicDependentClass);
            DependentsSet currentDependents = getDependents(publicDependentClass);
            if (!currentDependents.isDependencyToAll()) {
                remainingPublicDependentClasses.addAll(currentDependents.getPublicDependentClasses());
            }
        }
    }

    private DependentsSet getDependents(String className) {
        DependentsSet dependentsSet = dependentsMap.get(className);
        DependentsSet dependents = dependentsSet == null ? DependentsSet.empty() : dependentsSet;
        
        if (dependents.isDependencyToAll()) {
            return dependents;
        }

        return DependentsSet.dependents(dependents.getPrivateDependentClasses(), dependents.getPublicDependentClasses());
    }
}
