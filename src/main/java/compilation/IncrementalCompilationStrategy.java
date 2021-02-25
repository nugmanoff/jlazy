package compilation;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class IncrementalCompilationStrategy extends CompilationStrategy {

    public IncrementalCompilationStrategy(JavaCompiler compiler, CompilationConfiguration configuration) {
        super(compiler, configuration);
    }

    @Override
    public List<File> getFilesToCompile() {
        /*
         1. Get `dirtyFiles`
         2. Map `dirtyClasses` from `dirtyFiles`
         3. Get `actualDependentClasses` from `previousAnalysis` using `dirtyClasses`
         3. Map `actualDependentFiles` from `actualDependentClasses`
         4. Add `dirtyFiles` to `filesToCompile` except changeType == `DELETED`
         5. Add `actualDependentFiles` to `filesToCompile`
         6. Return `filesToCompile`
        */
        return null;
    }
}
