package analysis;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DependentsSet {

    public static DependentsSet dependentClasses(Set<String> privateDependentClasses, Set<String> accessibleDependentClasses) {
        return dependents(privateDependentClasses, accessibleDependentClasses);
    }

    public static DependentsSet dependents(Set<String> privateDependentClasses, Set<String> accessibleDependentClasses) {
        if (privateDependentClasses.isEmpty() && accessibleDependentClasses.isEmpty()) {
            return empty();
        } else {
            return new DefaultDependentsSet(ImmutableSet.copyOf(privateDependentClasses), ImmutableSet.copyOf(accessibleDependentClasses));
        }
    }

    public static DependentsSet dependencyToAll() {
        return DependencyToAll.INSTANCE;
    }

    public static DependentsSet dependencyToAll(String reason) {
        return new DependencyToAll(reason);
    }

    public static DependentsSet empty() {
        return EmptyDependentsSet.INSTANCE;
    }

    public abstract boolean isEmpty();

    public abstract boolean hasDependentClasses();

    public abstract Set<String> getPrivateDependentClasses();

    public abstract Set<String> getAccessibleDependentClasses();

    public abstract boolean isDependencyToAll();

    public abstract @Nullable String getDescription();

    private DependentsSet() {
    }

    public abstract Set<String> getAllDependentClasses();

    @Override
    public String toString() {
        return "analysis.DependentsSet: \n" +
                "privateDependentClasses: " + getPrivateDependentClasses() + "\n" +
                "accessibleDependentClasses: " + getAccessibleDependentClasses() + "\n";
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
        public Set<String> getAccessibleDependentClasses() {
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

        @Nullable
        @Override
        public String getDescription() {
            return null;
        }
    }

    private static class DefaultDependentsSet extends DependentsSet {

        private final Set<String> privateDependentClasses;
        private final Set<String> accessibleDependentClasses;

        private DefaultDependentsSet(Set<String> privateDependentClasses, Set<String> accessibleDependentClasses) {
            this.privateDependentClasses = privateDependentClasses;
            this.accessibleDependentClasses = accessibleDependentClasses;
        }

        @Override
        public boolean isEmpty() {
            return !hasDependentClasses();
        }

        @Override
        public boolean hasDependentClasses() {
            return !privateDependentClasses.isEmpty() || !accessibleDependentClasses.isEmpty();
        }

        @Override
        public Set<String> getPrivateDependentClasses() {
            return privateDependentClasses;
        }

        @Override
        public Set<String> getAccessibleDependentClasses() {
            return accessibleDependentClasses;
        }

        @Override
        public Set<String> getAllDependentClasses() {
            if (privateDependentClasses.isEmpty()) {
                return accessibleDependentClasses;
            }
            if (accessibleDependentClasses.isEmpty()) {
                return privateDependentClasses;
            }
            Set<String> r = new HashSet<>(accessibleDependentClasses);
            r.addAll(privateDependentClasses);
            return r;
        }

        @Override
        public boolean isDependencyToAll() {
            return false;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private static class DependencyToAll extends DependentsSet {
        private static final DependencyToAll INSTANCE = new DependencyToAll();

        private final String reason;

        private DependencyToAll(String reason) {
            this.reason = reason;
        }

        private DependencyToAll() {
            this(null);
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("This instance of dependents set does not have dependent classes information.");
        }

        @Override
        public boolean hasDependentClasses() {
            throw new UnsupportedOperationException("This instance of dependents set does not have dependent classes information.");
        }

        @Override
        public Set<String> getPrivateDependentClasses() {
            throw new UnsupportedOperationException("This instance of dependents set does not have dependent classes information.");
        }

        @Override
        public Set<String> getAccessibleDependentClasses() {
            throw new UnsupportedOperationException("This instance of dependents set does not have dependent classes information.");
        }

        @Override
        public Set<String> getAllDependentClasses() {
            throw new UnsupportedOperationException("This instance of dependents set does not have dependent classes information.");
        }

        @Override
        public boolean isDependencyToAll() {
            return true;
        }

        @Override
        public String getDescription() {
            return reason;
        }
    }
}
