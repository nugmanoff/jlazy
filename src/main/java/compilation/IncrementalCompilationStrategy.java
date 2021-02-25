package compilation;

import files.Cache;
import files.FileManager;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IncrementalCompilationStrategy extends CompilationStrategy {

    public IncrementalCompilationStrategy(JavaCompiler compiler) {
        super(compiler);
    }

    @Override
    public JavaCompiler.CompilationTask getCompilationTask(List<File> files, File outputDirectory) {
        /*
         1. Get `dirtyFiles`
         2. Map `dirtyClasses` from `dirtyFiles`
         3. Get `actualDependentClasses` from `previousAnalysis` using `dirtyClasses`
         3. Map `actualDependentFiles` from `actualDependentClasses`
         4. Add `dirtyFiles` to `filesToCompile` except changeType == `DELETED`
         5. Add `actualDependentFiles` to `filesToCompile`
        */

        if (files.isEmpty()) {
            // TODO Replace with proper logging
            System.out.println("Nothing to compile! Everything's great!");
            return null;
        }
        StandardJavaFileManager compilerFileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> sources = compilerFileManager.getJavaFileObjectsFromFiles(files);
        // TODO Refactor to use common options thing
        try {
            compilerFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compiler.getTask(null, compilerFileManager, null, null, null, sources);
    }
}
