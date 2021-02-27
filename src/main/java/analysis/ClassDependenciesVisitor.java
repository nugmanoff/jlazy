package analysis;

import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ClassDependenciesVisitor extends ClassVisitor {

    private final static int API = Opcodes.ASM8;

    private final Set<Integer> constants;
    private final Set<String> privateTypes;
    private final Set<String> accessibleTypes;
    private final Predicate<String> typeFilter;
    private boolean dependencyToAll;

    private ClassDependenciesVisitor(Predicate<String> typeFilter, ClassReader reader) {
        super(API);
        this.constants = new HashSet<>();
        this.privateTypes = new HashSet<>();
        this.accessibleTypes = new HashSet<>();
        this.typeFilter = typeFilter;
        collectRemainingClassDependencies(reader);
    }

    public static ClassAnalysis analyze(String className, ClassReader reader) {
        ClassDependenciesVisitor visitor = new ClassDependenciesVisitor(new ClassRelevancyFilter(className), reader);
        reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        // Remove the "API accessible" types from the "privately used types"
        visitor.privateTypes.removeAll(visitor.accessibleTypes);

        return new ClassAnalysis(className, visitor.getPrivateClassDependencies(), visitor.getAccessibleClassDependencies(), visitor.isDependencyToAll(), visitor.getConstants());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        Set<String> types = isAccessible(access) ? accessibleTypes : privateTypes;
        if (superName != null) {
            // superName can be null if what we are analyzing is `java.lang.Object`
            // which can happen when a custom Java SDK is on classpath (typically, android.jar)
            String type = typeOfFromSlashyString(superName);
            maybeAddDependentType(types, type);
        }
        for (String s : interfaces) {
            String interfaceType = typeOfFromSlashyString(s);
            maybeAddDependentType(types, interfaceType);
        }
    }

    // performs a fast analysis of classes referenced in bytecode (method bodies)
    // avoiding us to implement a costly visitor and potentially missing edge cases
    private void collectRemainingClassDependencies(ClassReader reader) {
        char[] charBuffer = new char[reader.getMaxStringLength()];
        for (int i = 1; i < reader.getItemCount(); i++) {
            int itemOffset = reader.getItem(i);
            // see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4
            if (itemOffset > 0 && reader.readByte(itemOffset - 1) == 7) {
                // A CONSTANT_Class entry, read the class descriptor
                String classDescriptor = reader.readUTF8(itemOffset, charBuffer);
                Type type = Type.getObjectType(classDescriptor);
                while (type.getSort() == Type.ARRAY) {
                    type = type.getElementType();
                }
                if (type.getSort() != Type.OBJECT) {
                    // A primitive type
                    continue;
                }
                String name = type.getClassName();
                // Any class that hasn't been added yet, is used in method bodies, which are implementation details and not visible as an "API"1
                if (!accessibleTypes.contains(name)) {
                    maybeAddDependentType(privateTypes, name);
                }
            }
        }
    }

    protected void maybeAddDependentType(Set<String> types, String type) {
        if (typeFilter.test(type)) {
            types.add(type);
        }
    }

    protected String typeOfFromSlashyString(String slashyStyleDesc) {
        return Type.getObjectType(slashyStyleDesc).getClassName();
    }

    public Set<String> getPrivateClassDependencies() {
        return privateTypes;
    }

    public Set<String> getAccessibleClassDependencies() {
        return accessibleTypes;
    }

    public Set<Integer> getConstants() {
        return constants;
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        Set<String> types = isAccessible(access) ? accessibleTypes : privateTypes;
        maybeAddDependentType(types, descTypeOf(desc));
        if (isAccessibleConstant(access, value)) {
            // we need to compute a hash for a constant, which is based on the name of the constant + its value
            // otherwise we miss the case where a class defines several constants with the same value, or when
            // two values are switched
            constants.add((name + '|' + value).hashCode()); //non-private const
            dependencyToAll = true;
        }
        return new FieldVisitor();
    }

    protected String descTypeOf(String desc) {
        Type type = Type.getType(desc);
        if (type.getSort() == Type.ARRAY && type.getDimensions() > 0) {
            type = type.getElementType();
        }
        return type.getClassName();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        Set<String> types = isAccessible(access) ? accessibleTypes : privateTypes;
        Type methodType = Type.getMethodType(desc);
        maybeAddDependentType(types, methodType.getReturnType().getClassName());
        for (Type argType : methodType.getArgumentTypes()) {
            maybeAddDependentType(types, argType.getClassName());
        }
        return new MethodVisitor(types);
    }

    private static boolean isAccessible(int access) {
        return (access & Opcodes.ACC_PRIVATE) == 0;
    }

    private static boolean isAccessibleConstant(int access, Object value) {
        return isConstant(access) && isAccessible(access) && value != null;
    }

    private static boolean isConstant(int access) {
        return (access & Opcodes.ACC_FINAL) != 0 && (access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isDependencyToAll() {
        return dependencyToAll;
    }

    private class MethodVisitor extends org.objectweb.asm.MethodVisitor {
        private final Set<String> types;

        protected MethodVisitor(Set<String> types) {
            super(API);
            this.types = types;
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            maybeAddDependentType(types, descTypeOf(desc));
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    private static class FieldVisitor extends org.objectweb.asm.FieldVisitor {
        protected FieldVisitor() {
            super(API);
        }
    }
}
