package compilation;

import file.FileHasher;
import file.FileManager;
import intermediate.IntermediateProductsManager;
import intermediate.SourceFileHashes;

import java.io.File;
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

        System.out.println("@ JLazy > Файлы для комиляции >" + allSourceFiles);

        return allSourceFiles;
    }
}
