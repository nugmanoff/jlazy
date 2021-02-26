package intermediate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import compilation.CompilationConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IntermediateProductsManager {

    private List<IntermediateProduct> intermediateProducts;
    private CompilationConfiguration compilationConfiguration;

    private ClassToSourceMapping classToSourceMapping;
    private PersistedClassSetAnalysis persistedClassSetAnalysis;
    private SourceFileHashes sourceFileHashes;

    public IntermediateProductsManager(CompilationConfiguration compilationConfiguration) {
        this.intermediateProducts = new ArrayList<>();
        this.compilationConfiguration = compilationConfiguration;
        setup();
    }

    public boolean areConsistent() {
        // Использует наивную стратегию на существование всех файлов. В перспективе можно переделать на более умную проверку.
        return intermediateProducts.stream().anyMatch(intermediateProduct -> !intermediateProduct.exists());
    }

    public void setup() {
        classToSourceMapping = new ClassToSourceMapping(new File(""), ArrayListMultimap.<String, String>create());
        persistedClassSetAnalysis = new PersistedClassSetAnalysis(new File(""), null);
        sourceFileHashes = new SourceFileHashes(new File(""), null);
        intermediateProducts.add(classToSourceMapping);
        intermediateProducts.add(persistedClassSetAnalysis);
        intermediateProducts.add(sourceFileHashes);
    }

    public void deleteAll() {
        intermediateProducts.forEach(IntermediateProduct::delete);
    }

    public IntermediateProduct retrieve(String name) {
        return switch (name) {
            case "mapping" -> classToSourceMapping;
            case "hashes" -> sourceFileHashes;
            case "analysis" -> persistedClassSetAnalysis;
            default -> null;
        };
    }

    public void save() {
        intermediateProducts.forEach(IntermediateProduct::write);
    }
}

//    private void prepareIntermediateProducts() throws IOException {
//        File metadataDirectory = compilationConfiguration.getMetadataDirectory();
//
//        File classToSourceMappingFile = fileManager.createFile(Paths.get(metadataDirectory.getName() + "/" + "classToSourceMapping.lock"));
//        ClassToSourceMapping mapping = new ClassToSourceMapping(classToSourceMappingFile, ArrayListMultimap.<String, String>create());
//
//        File persistedClassSetAnalysisFile = fileManager.createFile(Paths.get(metadataDirectory.getName() + "/" + "classSetAnalysis.lock"));
////        PersistedClassSetAnalysis analysis = new PersistedClassSetAnalysis(persistedClassSetAnalysisFile)
//    }