package compilation;

import javax.tools.JavaCompiler;
import java.io.File;
import java.util.List;

public abstract class CompilationStrategy {

    final JavaCompiler compiler;

    public CompilationStrategy(JavaCompiler compiler) {
        this.compiler = compiler;
    }

    public abstract JavaCompiler.CompilationTask getCompilationTask(List<File> files, File outputDirectory);
}
