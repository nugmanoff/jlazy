package analysis;

import java.util.Set;

public class ClassAnalysis {
    private final String className;
    private final Set<String> privateDependencies;
    private final Set<String> publicDependencies;
    private final boolean isDependencyToAll;

    public ClassAnalysis(String className, Set<String> privateClassDependencies, Set<String> publicDependencies, boolean isDependencyToAll) {
        this.className = className;
        this.privateDependencies = privateClassDependencies;
        this.publicDependencies = publicDependencies;
        this.isDependencyToAll = isDependencyToAll;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getPrivateDependencies() {
        return privateDependencies;
    }

    public Set<String> getPublicDependencies() {
        return publicDependencies;
    }


    public boolean isDependencyToAll() {
        return isDependencyToAll;
    }

    @Override
    public String toString() {
        return "analysis.ClassAnalysis {\n" +
                "className='" + className + '\'' + "\n" +
                "privateClassDependencies=" + privateDependencies + "\n" +
                "accessibleClassDependencies=" + publicDependencies + "\n" +
                "isDependencyToAll=" + isDependencyToAll + "\n" +
                '}';
    }
}
