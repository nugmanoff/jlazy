package compilation;

import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import analysis.ClassSetAnalysis;
import file.FileHasher;
import file.FileManager;
import intermediate.IntermediateProductsManager;
import intermediate.SourceFileHashes;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CleanCompilationStrategy implements CompilationStrategy {

    private FileManager fileManager;
    private CompilationConfiguration configuration;
    private IntermediateProductsManager intermediateProductsManager;


    public CleanCompilationStrategy(FileManager fileManager, CompilationConfiguration configuration, IntermediateProductsManager intermediateProductsManager) {
        this.fileManager = fileManager;
        this.configuration = configuration;
        this.intermediateProductsManager = intermediateProductsManager;
    }

    @Override
    public List<File> getFilesToCompile() {
        intermediateProductsManager.deleteAll();
        intermediateProductsManager.createAll();

        fileManager.createDirectory(configuration.getOutputDirectory().toPath());
        List<File> allSourceFiles = fileManager.getAllFilesInDirectory(configuration.getSourcesDirectory().toPath(), ".java");

        Map<String, String> hashesMap = FileHasher.getHashesOf(allSourceFiles);
        SourceFileHashes fileHashes = (SourceFileHashes) intermediateProductsManager.retrieve("hashes");
        fileHashes.setObject(hashesMap);

        return allSourceFiles;
    }
}
