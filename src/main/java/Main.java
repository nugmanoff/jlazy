import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Main {

    private static final String COMPILATION_PATH_ROOT = "src/main/java/fixtures/";
    private static final String CLASSFILES_BASEPATH = "build/classes/java/main/fixtures/";
    private static final String JAVA_FILE_EXTENSION = ".java";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final List<String> CLASSFILES_TO_ANALYZE = ImmutableList.<String>builder()
            .add("AnotherClass")
            .add("DummyClass")
            .add("SomeInterface")
            .add("ClassWithClassConstant")
            .add("ClassWithPrimitiveConstant")
            .build();

    public static void main(String[] args) throws IOException, URISyntaxException {
        ClassDependenciesAnalyzer cda = new ClassDependenciesAnalyzer();
        ClassDependentsAccumulator acc = new ClassDependentsAccumulator();

        List<File> files = new ArrayList<>();

        for (String classFile : CLASSFILES_TO_ANALYZE) {
            files.add(new File(COMPILATION_PATH_ROOT + classFile + JAVA_FILE_EXTENSION));
            acc.addClass(cda.getClassAnalysis(new File(CLASSFILES_BASEPATH + classFile + CLASS_FILE_EXTENSION)));
        }

        System.out.println(acc.getDependentsMap());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> ds = new
                DiagnosticCollector<>();
        try (StandardJavaFileManager mgr = compiler.getStandardFileManager(ds, null, null)) {
            Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(files);
            JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, ds, null, null, sources);
            task.call();
        }
        for (Diagnostic<? extends JavaFileObject> d : ds.getDiagnostics()) {
            System.out.format("Line: %d, %s in %s", d.getLineNumber(), d.getMessage(null), d.getSource().getName());
        }
    }
}
