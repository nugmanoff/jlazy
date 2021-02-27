package analysis;

import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;

public class ClassDependenciesAnalyzer extends ClassVisitor {

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

    private final static int ASM_API_CODE = Opcodes.ASM8;

    private final String className;
    private final Set<String> privateTypes;
    private final Set<String> accessibleTypes;
    private boolean dependencyToAll;

    private ClassDependenciesAnalyzer(String className, ClassReader reader) {
        super(ASM_API_CODE);
        this.privateTypes = new HashSet<>();
        this.accessibleTypes = new HashSet<>();
        this.className = className;
        collectRemainingClassDependencies(reader);
    }

    public static ClassAnalysis analyze(String className, ClassReader reader) {
        ClassDependenciesAnalyzer analyzer = new ClassDependenciesAnalyzer(className, reader);
        reader.accept(analyzer, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        analyzer.privateTypes.removeAll(analyzer.accessibleTypes);
        return new ClassAnalysis(className, analyzer.getPrivateClassDependencies(), analyzer.getAccessibleClassDependencies(), analyzer.isDependencyToAll());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        Set<String> types = isAccessible(access) ? accessibleTypes : privateTypes;
        String type = typeOfFromSlashyString(superName);
        addDependentTypeIfNeeded(types, type);

        for (String s : interfaces) {
            String interfaceType = typeOfFromSlashyString(s);
            addDependentTypeIfNeeded(types, interfaceType);
        }
    }

    private void collectRemainingClassDependencies(ClassReader reader) {
        char[] charBuffer = new char[reader.getMaxStringLength()];
        for (int i = 1; i < reader.getItemCount(); i++) {
            int itemOffset = reader.getItem(i);
            if (itemOffset > 0 && reader.readByte(itemOffset - 1) == 7) {
                String classDescriptor = reader.readUTF8(itemOffset, charBuffer);
                Type type = Type.getObjectType(classDescriptor);
                while (type.getSort() == Type.ARRAY) {
                    type = type.getElementType();
                }
                if (type.getSort() != Type.OBJECT) {
                    // Пропускаем примитивы
                    continue;
                }
                String name = type.getClassName();
                if (!accessibleTypes.contains(name)) {
                    addDependentTypeIfNeeded(privateTypes, name);
                }
            }
        }
    }

    protected void addDependentTypeIfNeeded(Set<String> types, String type) {
        if (test(type)) {
            types.add(type);
        }
    }

    protected boolean test(String className) {
        return !className.startsWith("java.")
                && !className.startsWith("javax.")
                && !className.startsWith("org.")
                && !className.startsWith("com.")
                && !this.className.equals(className)
                && !PRIMITIVES.contains(className);
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

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        Set<String> types = isAccessible(access) ? accessibleTypes : privateTypes;
        addDependentTypeIfNeeded(types, descTypeOf(desc));
        if (isAccessibleConstant(access, value)) {
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
        addDependentTypeIfNeeded(types, methodType.getReturnType().getClassName());
        for (Type argType : methodType.getArgumentTypes()) {
            addDependentTypeIfNeeded(types, argType.getClassName());
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
            super(ASM_API_CODE);
            this.types = types;
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            addDependentTypeIfNeeded(types, descTypeOf(desc));
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    private static class FieldVisitor extends org.objectweb.asm.FieldVisitor {
        protected FieldVisitor() {
            super(ASM_API_CODE);
        }
    }
}
