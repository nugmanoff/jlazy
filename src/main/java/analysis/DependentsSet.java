package analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DependentsSet {

    public static DependentsSet dependentClasses(Set<String> privateDependentClasses, Set<String> publicDependentClasses) {
        return dependents(privateDependentClasses, publicDependentClasses);
    }

    public static DependentsSet dependents(Set<String> privateDependentClasses, Set<String> publicDependentClasses) {
        if (privateDependentClasses.isEmpty() && publicDependentClasses.isEmpty()) {
            return empty();
        } else {
            return new DefaultDependentsSet(privateDependentClasses, publicDependentClasses);
        }
    }

    public static DependentsSet dependencyToAll() {
        return DependencyToAll.INSTANCE;
    }
    
    public static DependentsSet empty() {
        return EmptyDependentsSet.INSTANCE;
    }

    public abstract boolean isEmpty();

    public abstract boolean hasDependentClasses();

    public abstract Set<String> getPrivateDependentClasses();

    public abstract Set<String> getPublicDependentClasses();

    public abstract boolean isDependencyToAll();
    
    private DependentsSet() {
    }

    public abstract Set<String> getAllDependentClasses();

    @Override
    public String toString() {
        return "analysis.DependentsSet: \n" +
                "privateDependentClasses: " + getPrivateDependentClasses() + "\n" +
                "publicDependentClasses: " + getPublicDependentClasses() + "\n";
    }

    private static class EmptyDependentsSet extends DependentsSet {
        private static final EmptyDependentsSet INSTANCE = new EmptyDependentsSet();

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean hasDependentClasses() {
            return false;
        }

        @Override
        public Set<String> getPrivateDependentClasses() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getPublicDependentClasses() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getAllDependentClasses() {
            return Collections.emptySet();
        }

        @Override
        public boolean isDependencyToAll() {
            return false;
        }
        
    }

    private static class DefaultDependentsSet extends DependentsSet {

        private final Set<String> privateDependentClasses;
        private final Set<String> publicDependentClasses;

        private DefaultDependentsSet(Set<String> privateDependentClasses, Set<String> publicDependentClasses) {
            this.privateDependentClasses = privateDependentClasses;
            this.publicDependentClasses = publicDependentClasses;
        }

        @Override
        public boolean isEmpty() {
            return !hasDependentClasses();
        }

        @Override
        public boolean hasDependentClasses() {
            return !privateDependentClasses.isEmpty() || !publicDependentClasses.isEmpty();
        }

        @Override
        public Set<String> getPrivateDependentClasses() {
            return privateDependentClasses;
        }

        @Override
        public Set<String> getPublicDependentClasses() {
            return publicDependentClasses;
        }

        @Override
        public Set<String> getAllDependentClasses() {
            if (privateDependentClasses.isEmpty()) {
                return publicDependentClasses;
            }
            if (publicDependentClasses.isEmpty()) {
                return privateDependentClasses;
            }
            Set<String> r = new HashSet<>(publicDependentClasses);
            r.addAll(privateDependentClasses);
            return r;
        }

        @Override
        public boolean isDependencyToAll() {
            return false;
        }
    }

    private static class DependencyToAll extends DependentsSet {
        private static final DependencyToAll INSTANCE = new DependencyToAll();

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("Операция недоступна");
        }

        @Override
        public boolean hasDependentClasses() {
            throw new UnsupportedOperationException("Операция недоступна");
        }

        @Override
        public Set<String> getPrivateDependentClasses() {
            throw new UnsupportedOperationException("Операция недоступна");
        }

        @Override
        public Set<String> getPublicDependentClasses() {
            throw new UnsupportedOperationException("Операция недоступна");
        }

        @Override
        public Set<String> getAllDependentClasses() {
            throw new UnsupportedOperationException("Операция недоступна");
        }

        @Override
        public boolean isDependencyToAll() {
            return true;
        }

    }
}
