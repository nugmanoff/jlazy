package compilation;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CleanCompilationStrategy extends CompilationStrategy {

    public CleanCompilationStrategy(JavaCompiler compiler) {
        super(compiler);
    }

    @Override
    public JavaCompiler.CompilationTask getCompilationTask(List<File> files, File outputDirectory) {
        /*
          1. Basic compilationTask

         */
        if (files.isEmpty()) {
            // TODO Replace with proper logging
            System.out.println("Nothing to compile! Everything's great!");
            return null;
        }
        StandardJavaFileManager compilerFileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> sources = compilerFileManager.getJavaFileObjectsFromFiles(files);
        try {
            compilerFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compiler.getTask(null, compilerFileManager, null, null, null, sources);
    }
}
