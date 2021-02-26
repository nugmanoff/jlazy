package compilation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import file.FileHasher;
import file.FileManager;
import intermediate.ClassToSourceMapping;
import intermediate.IntermediateProductsManager;
import intermediate.SourceFileHashes;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CompilationOrchestrator implements ObservedCompilationResultHandler {

    final JavaCompiler compiler;
    final FileManager fileManager;
    final CompilationConfiguration compilationConfiguration;
    final StandardJavaFileManager compilerFileManager;
    final IntermediateProductsManager intermediateProductsManager;
    private ClassToSourceMapping mapping;

    private CompilationStrategy compilationStrategy;

    public CompilationOrchestrator(JavaCompiler compiler, FileManager fileManager, CompilationConfiguration compilationConfiguration) {
        this.compiler = compiler;
        this.fileManager = fileManager;
        this.compilationConfiguration = compilationConfiguration;
        compilerFileManager = compiler.getStandardFileManager(null, null, null);
        intermediateProductsManager = new IntermediateProductsManager(compilationConfiguration, fileManager);
    }

    public void performCompilation(String sourceDirectoryName, String classpath) {
        File sourceDirectory = new File(sourceDirectoryName);

        // 1. Проверяем не пустая ли папка компиляции
        if (Objects.requireNonNull(sourceDirectory.listFiles()).length == 0) {
            // TODO Replace with proper logging
            System.out.println("Nothing to compile!");
            return;
        }

        // 2. Достаём необходимые для сборки intermediate products
        if (!intermediateProductsManager.areConsistent()) {
            // Если не существует какой-то из intermediate products -> делаем полную перекомпиляцию
            compilationStrategy = new CleanCompilationStrategy(fileManager, compilationConfiguration, intermediateProductsManager);
        } else {
            // Если все intermediate products существует вот правильной форме  -> запускаем инкрементальную компиляцию
            compilationStrategy = new IncrementalCompilationStrategy(compiler, compilationConfiguration);
        }

        // 3. Достаём файлы нужные для компиляции
        List<File> filesToCompile = compilationStrategy.getFilesToCompile();

        // 4. Создаём задание компиляции и запускаем
        JavaCompiler.CompilationTask task = getCompilationTask(filesToCompile);
        task.call();
    }

    private void saveIntermediateProducts() {
    }

    private JavaCompiler.CompilationTask getCompilationTask(List<File> filesToCompile) {
        Iterable<? extends JavaFileObject> sources = compilerFileManager.getJavaFileObjectsFromFiles(filesToCompile);
        try {
            compilerFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(compilationConfiguration.getOutputDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JavaCompiler.CompilationTask task = compiler.getTask(null, compilerFileManager, null, null, null, sources);
//        new ObservedCompilationTask(task, null, (Consumer<Map<String, Collection<String>>>) mapping -> mapping.writeSourceClassesMappingFile(mappingFile, mapping));
        return new ObservedCompilationTask(task, null, this);
    }

    @Override
    public Map<String, Collection<String>> handleCompilationResult(Map<String, Collection<String>> incrementalCompilationMapping) {
        SourceFileHashes fileHashes = (SourceFileHashes) intermediateProductsManager.retrieve("hashes");
        ClassToSourceMapping mapping = (ClassToSourceMapping) intermediateProductsManager.retrieve("mapping");
        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        incrementalCompilationMapping.forEach(multimap::putAll);
        mapping.mergeIncrementalMappingsIntoOldMappings(new ArrayList<>(), multimap);
        fileHashes.write();
        return null;
    }
}
