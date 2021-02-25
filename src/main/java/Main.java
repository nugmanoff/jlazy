import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import com.google.common.collect.ImmutableList;
import files.Cache;
import files.FileChange;
import files.FileManager;
import org.apache.commons.cli.*;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final String COMPILATION_PATH_ROOT = "src/main/java/fixtures/";
    private static final String CLASSFILES_BASEPATH = "build/classes/java/main/fixtures/";
    private static final String JAVA_FILE_EXTENSION = ".java";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final List<String> CLASSFILES_TO_ANALYZE = ImmutableList.<String>builder()
            .add("AnotherClass")
            .add("DummyClass")
            .add("SomeInterface")
            .add("ClassWithClassConstant")
            .add("ClassWithPrimitiveConstant")
            .build();

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption(Option.builder("cp")
                .longOpt("classpath")
                .hasArg(true)
                .desc("Java classpath with dependencies")
                .required(false)
                .build());
        options.addOption(Option.builder("d")
                .longOpt("dir")
                .hasArg(true)
                .desc("Directory with Java sources files to compile")
                .required(true)
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("cp")) {
                String classpath = cmd.getOptionValue("cp");
                System.out.println("We have --classpath option = " + classpath);
            }
            if (cmd.hasOption("d")) {
                String directory = cmd.getOptionValue("d");
                Cache cache = new Cache(createCacheFileIfNeeded("cache", "fileHashes.bin"));
                FileManager fm = new FileManager(cache);
                for (FileChange fc: fm.getFileChangesList("src/", ".java")) {
                    System.out.println("Filename=" + fc.getFile().getName() + "; Type=" + fc.getType());
                }
                List<File> files = fm.getAllFilesInDirectory(directory, ".java");
                File outputDirectory = fm.createOutputDirectoryIfNeeded("out/");
                compile(files, outputDirectory);
            }
        } catch (ParseException | ClassNotFoundException pe) {
            System.out.println("Error parsing command-line arguments!");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "jlazy", options );
            System.exit(1);
        }
    }

    private static File createCacheFileIfNeeded(String directory, String filename) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(directory + "/" + filename);
        file.createNewFile();
        return file;
    }

    private static void compile(List<File> files, File outputDirectory) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        try (StandardJavaFileManager mgr = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(files);
            mgr.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDirectory));
            JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, null, null, null, sources);
            task.call();
        }
    }

    private static void analyze() throws IOException {
        ClassDependenciesAnalyzer cda = new ClassDependenciesAnalyzer();
        ClassDependentsAccumulator acc = new ClassDependentsAccumulator();

        for (String classFile : CLASSFILES_TO_ANALYZE) {
            acc.addClass(cda.getClassAnalysis(new File(CLASSFILES_BASEPATH + classFile + CLASS_FILE_EXTENSION)));
        }

        System.out.println(acc.getDependentsMap());
    }
}
