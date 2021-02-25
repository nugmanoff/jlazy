package compilation;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CleanCompilationStrategy extends CompilationStrategy {

    public CleanCompilationStrategy(JavaCompiler compiler, CompilationConfiguration configuration) {
        super(compiler, configuration);
    }

    @Override
    public List<File> getFilesToCompile() {
        /*
            1. Get `allSourceFiles`
            2. Return `allSourceFiles`
         */
        return null;
    }
}
