package analysis;

import java.util.Set;

public class ClassAnalysis {
    private final String className;
    private final Set<String> privateClassDependencies;
    private final Set<String> accessibleClassDependencies;
    private final boolean dependencyToAll;
    private final Set<Integer> constants;

    public ClassAnalysis(String className, Set<String> privateClassDependencies, Set<String> accessibleClassDependencies, boolean dependencyToAll, Set<Integer> constants) {
        this.className = className;
        this.privateClassDependencies = privateClassDependencies;
        this.accessibleClassDependencies = accessibleClassDependencies;
        this.dependencyToAll = dependencyToAll;
        this.constants = constants;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getPrivateClassDependencies() {
        return privateClassDependencies;
    }

    public Set<String> getAccessibleClassDependencies() {
        return accessibleClassDependencies;
    }

    public Set<Integer> getConstants() {
        return constants;
    }

    public boolean isDependencyToAll() {
        return dependencyToAll;
    }

    @Override
    public String toString() {
        return "analysis.ClassAnalysis {\n" +
                "className='" + className + '\'' + "\n" +
                ", privateClassDependencies=" + privateClassDependencies + "\n" +
                ", accessibleClassDependencies=" + accessibleClassDependencies + "\n" +
                ", dependencyToAll=" + dependencyToAll + "\n" +
                ", constants=" + constants + "\n" +
                '}';
    }
}
