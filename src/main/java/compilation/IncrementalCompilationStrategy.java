package compilation;

import analysis.*;
import com.google.common.collect.Multimap;
import file.*;
import intermediate.ClassToSourceMapping;
import intermediate.IntermediateProductsManager;
import intermediate.SourceFileHashes;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IncrementalCompilationStrategy implements CompilationStrategy {

    private FileManager fileManager;
    private CompilationConfiguration configuration;
    private IntermediateProductsManager intermediateProductsManager;

    private List<String> deletedFileNames;

    public IncrementalCompilationStrategy(FileManager fileManager, CompilationConfiguration configuration, IntermediateProductsManager intermediateProductsManager) {
        this.fileManager = fileManager;
        this.configuration = configuration;
        this.intermediateProductsManager = intermediateProductsManager;
    }

    @Override
    public List<File> getFilesToCompile() {
        intermediateProductsManager.readAll();

        File srcDirectory = configuration.getSourcesDirectory();
        SourceFileHashes fileHashes = (SourceFileHashes) intermediateProductsManager.retrieve("hashes");
        Map<String, String> oldFiles = (Map<String, String>) fileHashes.getObject();

        List<File> allSourceFiles = fileManager.getAllFilesInDirectory(srcDirectory.toPath(), ".java");
        Map<String, String> newFiles = FileHasher.getHashesOf(allSourceFiles);
        List<FileChange> fileChanges = FileChangeDetector.getFileChanges(oldFiles, newFiles);
        fileHashes.setObject(newFiles);

        ClassToSourceMapping classToSourceMapping = (ClassToSourceMapping) intermediateProductsManager.retrieve("mapping");
        Multimap<String, String> mapping = (Multimap<String, String>) classToSourceMapping.getObject();
        SourceFileClassNameConverter converter = new SourceFileClassNameConverter(mapping);

        List<File> dirtyFiles = fileChanges
                .stream()
                .filter(fileChange -> fileChange.getType() != FileChange.Type.REMOVE)
                .map(fileChange -> fileChange.getFile())
                .collect(Collectors.toList());

        Path srcPath = Path.of(srcDirectory.getAbsolutePath());
        List<String> deletedFileNames = fileChanges
                .stream()
                .filter(fileChange -> fileChange.getType() == FileChange.Type.REMOVE)
                .map(fileChange -> srcPath.relativize(Path.of(fileChange.getFile().getAbsolutePath())).toString())
                .map(deletedFileName -> srcDirectory.getName() + "/" + deletedFileName)
                .collect(Collectors.toList());

        this.deletedFileNames = deletedFileNames;

        System.out.println("@ JLazy > 'Грязные файлы >'" + dirtyFiles);

        List<String> dirtyClasses = new ArrayList<>();

        for (File dirtyFile : dirtyFiles) {
            Path dirtyFilePath = Path.of(dirtyFile.getAbsolutePath());
            dirtyClasses.addAll(converter.getClassNames(srcDirectory.getName() + "/" + srcPath.relativize(dirtyFilePath).toString()));
        }

        System.out.println("@ JLazy > 'Грязные классы >'" + dirtyClasses);

        List<File> allClassFiles = fileManager.getAllFilesInDirectory(configuration.getOutputDirectory().toPath(), ".class");

        ClassDependenciesExtractor cda = new ClassDependenciesExtractor();
        ClassDependentsAggregator acc = new ClassDependentsAggregator();

        for (File classFile : allClassFiles) {
            ClassAnalysis classAnalysis = cda.getClassAnalysis(classFile);
            acc.addClass(classAnalysis);
        }

        AggregatedAnalysisStrategy csa = new AggregatedAnalysisStrategy(acc.getDependentsMap());
        DependentsSet relevantDependents = csa.getRelevantDependents(dirtyClasses);

        if (relevantDependents.isDependencyToAll()) {
            System.out.println("@ JLazy > Требуется полная перекомпиляция, потому что найдена зависимость которая зависит от всего");
            System.out.println("@ JLazy > Файлы для комиляции >" + allSourceFiles);
            return allSourceFiles;
        }
        Set<String> actualClassesToCompile = relevantDependents.getAllDependentClasses();

        Set<File> actualFilesToCompile = new HashSet<>();

        for (String actualClass : actualClassesToCompile) {
            converter.getRelativeSourcePath(actualClass).ifPresent(filePath -> actualFilesToCompile.add(new File(filePath)));
        }

        actualFilesToCompile.addAll(dirtyFiles);
        System.out.println("@ JLazy > Файлы для комиляции >" + actualFilesToCompile);

        return new ArrayList<>(actualFilesToCompile);
    }

    public List<String> getDeletedFileNames() {
        return deletedFileNames;
    }
}