package compilation;

import analysis.ClassAnalysis;
import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import analysis.ClassSetAnalysis;
import com.google.common.collect.Multimap;
import file.*;
import intermediate.ClassToSourceMapping;
import intermediate.IntermediateProductsManager;
import intermediate.PersistedClassSetAnalysis;
import intermediate.SourceFileHashes;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IncrementalCompilationStrategy implements CompilationStrategy {

    private FileManager fileManager;
    private CompilationConfiguration configuration;
    private IntermediateProductsManager intermediateProductsManager;

    public IncrementalCompilationStrategy(FileManager fileManager, CompilationConfiguration configuration, IntermediateProductsManager intermediateProductsManager) {
        this.fileManager = fileManager;
        this.configuration = configuration;
        this.intermediateProductsManager = intermediateProductsManager;
    }

    @Override
    public List<File> getFilesToCompile() {
        intermediateProductsManager.readAll();

        SourceFileHashes fileHashes = (SourceFileHashes) intermediateProductsManager.retrieve("hashes");
        Map<String, String> oldFiles = (Map<String, String>) fileHashes.getObject();

        List<File> allSourceFiles = fileManager.getAllFilesInDirectory(configuration.getSourcesDirectory().toPath(), ".java");
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

        System.out.println("Dirty files: " + dirtyFiles);

        List<String> dirtyClasses = new ArrayList<>();

        for (File dirtyFile : dirtyFiles) {
            Path srcPath = Path.of(configuration.getSourcesDirectory().getAbsolutePath());
            Path dirtyFilePath = Path.of(dirtyFile.getAbsolutePath());
            dirtyClasses.addAll(converter.getClassNames(configuration.getSourcesDirectory().getName() + "/" + srcPath.relativize(dirtyFilePath).toString()));
        }

        System.out.println("Dirty classes: " + dirtyClasses);

        List<File> allClassFiles = fileManager.getAllFilesInDirectory(configuration.getOutputDirectory().toPath(), ".class");
        ClassDependenciesAnalyzer cda = new ClassDependenciesAnalyzer();
        ClassDependentsAccumulator acc = new ClassDependentsAccumulator();

        for (File classFile : allClassFiles) {
            ClassAnalysis classAnalysis = cda.getClassAnalysis(classFile);
            acc.addClass(classAnalysis);
        }

        ClassSetAnalysis csa = new ClassSetAnalysis(acc.getAnalysis());
        Set<String> actualClassesToCompile = csa.getRelevantDependents(dirtyClasses, new HashSet<>()).getAllDependentClasses();

        Set<File> actualFilesToCompile = new HashSet<>();

        for (String actualClass : actualClassesToCompile) {
            converter.getRelativeSourcePath(actualClass).ifPresent(filePath -> actualFilesToCompile.add(new File(filePath)));
        }

        actualFilesToCompile.addAll(dirtyFiles);
        System.out.println("Recompiled files: " + actualFilesToCompile);

        return new ArrayList<>(actualFilesToCompile);
    }
}