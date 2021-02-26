package compilation;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class CompilationStrategy {

    final JavaCompiler compiler;
    final CompilationConfiguration configuration;
    final StandardJavaFileManager compilerFileManager;

    public CompilationStrategy(JavaCompiler compiler, CompilationConfiguration configuration) {
        this.compiler = compiler;
        this.configuration = configuration;
        compilerFileManager = compiler.getStandardFileManager(null, null, null);
    }

    public abstract List<File> getFilesToCompile();

    public JavaCompiler.CompilationTask getCompilationTask() {
        List<File> filesToCompile = getFilesToCompile();
        if (filesToCompile.isEmpty()) {
            // TODO Replace with proper logging
            System.out.println("Nothing to compile! Everything's great!");
            return null;
        }
        Iterable<? extends JavaFileObject> sources = compilerFileManager.getJavaFileObjectsFromFiles(filesToCompile);
        try {
            compilerFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(configuration.getOutputDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new IncrementalCompilationTask(compiler.getTask(null, compilerFileManager, null, null, null, sources), null, null);
    }
}
