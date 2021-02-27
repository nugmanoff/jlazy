package compilation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import file.FileManager;
import intermediate.ClassToSourceMapping;
import intermediate.IntermediateProductsManager;

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

    public void performCompilation() {
        File sourceDirectory = compilationConfiguration.getSourcesDirectory();

        // 1. Проверяем не пустая ли папка компиляции
        if (Objects.requireNonNull(sourceDirectory.listFiles()).length == 0) {
            System.out.println("@ JLazy > Нечего компилировать!");
            return;
        }

        // 2. Достаём необходимые для сборки intermediate products
        if (!intermediateProductsManager.areConsistent()) {
            // Если не существует какой-то из intermediate products -> делаем полную перекомпиляцию
            System.out.println("@ JLazy > Стартует чистая компиляция!");
            compilationStrategy = new CleanCompilationStrategy(fileManager, compilationConfiguration, intermediateProductsManager);
        } else {
            // Если все intermediate products существует вот правильной форме  -> запускаем инкрементальную компиляцию
            System.out.println("@ JLazy > Стартует инкрементальная компиляция!");
            compilationStrategy = new IncrementalCompilationStrategy(fileManager, compilationConfiguration, intermediateProductsManager);
        }

        // 3. Достаём файлы нужные для компиляции
        List<File> filesToCompile = compilationStrategy.getFilesToCompile();

        if(filesToCompile.isEmpty()) {
            handleCompilationResult(new HashMap<>());
            System.out.println("@ JLazy > Нечего компилировать!");
            return;
        }

        // 4. Создаём задание компиляции и запускаем
        JavaCompiler.CompilationTask task = getCompilationTask(filesToCompile);
        task.call();
    }

    private JavaCompiler.CompilationTask getCompilationTask(List<File> filesToCompile) {
        Iterable<? extends JavaFileObject> sources = compilerFileManager.getJavaFileObjectsFromFiles(filesToCompile);
        try {
            compilerFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(compilationConfiguration.getOutputDirectory()));
            compilerFileManager.setLocation(StandardLocation.CLASS_PATH, Collections.singletonList(compilationConfiguration.getOutputDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JavaCompiler.CompilationTask task = compiler.getTask(null, compilerFileManager, null, null, null, sources);
        return new ObservedCompilationTask(task, null, this);
    }

    @Override
    public void handleCompilationResult(Map<String, Collection<String>> incrementalCompilationMapping) {
        List<String> deletedFileNames = new ArrayList<>();

        if (compilationStrategy instanceof IncrementalCompilationStrategy) {
            deletedFileNames = ((IncrementalCompilationStrategy) compilationStrategy).getDeletedFileNames();
        }

        ClassToSourceMapping mapping = (ClassToSourceMapping) intermediateProductsManager.retrieve("mapping");
        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        incrementalCompilationMapping.forEach(multimap::putAll);
        mapping.mergeIncrementalMappingsIntoOldMappings(deletedFileNames, multimap);

        intermediateProductsManager.saveAll();
    }
}
