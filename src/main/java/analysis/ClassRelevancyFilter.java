package analysis;

import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.function.Predicate;

class ClassRelevancyFilter implements Predicate<String> {

    private static final Set<String> PRIMITIVES = ImmutableSet.<String>builder()
            .add("void")
            .add("boolean")
            .add("byte")
            .add("char")
            .add("short")
            .add("int")
            .add("long")
            .add("float")
            .add("double")
            .build();

    private String excludedClassName;

    public ClassRelevancyFilter(String excludedClassName) {
        this.excludedClassName = excludedClassName;
    }

    @Override
    public boolean test(String className) {
        return !className.startsWith("java.")
                && !className.startsWith("javax.")
                && !className.startsWith("org.")
                && !className.startsWith("com.")
                && !excludedClassName.equals(className)
                && !PRIMITIVES.contains(className);
    }
}
