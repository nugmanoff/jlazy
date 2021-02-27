package compilation;

import java.io.File;
import java.util.List;

public interface CompilationStrategy {
    List<File> getFilesToCompile();
}
