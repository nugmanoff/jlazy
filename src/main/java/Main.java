import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import analysis.ClassSetAnalysis;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.source.util.TaskListener;
import files.Cache;
import files.FileChange;
import files.FileManager;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Main {

    private static final String CLASSFILES_BASEPATH = "out/";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String JAVA_FILE_EXTENSION = ".java";

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
//            if (cmd.hasOption("cp")) {
//                String classpath = cmd.getOptionValue("cp");
//                System.out.println("We have --classpath option = " + classpath);
//            }
            if (cmd.hasOption("d")) {
                String directory = cmd.getOptionValue("d");
                Cache cache = new Cache(createCacheFileIfNeeded("cache", "fileHashes.bin"));
                FileManager fm = new FileManager();


                List<File> dirtyFiles = new ArrayList<>();
//                for (FileChange fc: fm.getFileChanges("src/", ".java")) {
//                    dirtyFiles.add(fc.getFile());
//                }
//                File outputDirectory = fm.createOutputDirectoryIfNeeded("out/");


//                List<File> filesToCompile = new ArrayList<>();
//
//                for(File file : FileManager.getAllFilesInDirectory(directory, ".java")) {
//
//                    if (actualDependencies.contains(file.getName())) {
//                        System.out.println("Add file to compile: " + file.getName());
//                        filesToCompile.add(file);
//                    }
//                }
//                compile(dirtyFiles, outputDirectory);
                Set<String> actualDependencies = analyzeAndGetActualDependents(dirtyFiles);

            }
        } catch (ParseException e) {
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
        if (files.isEmpty()) {
            System.out.println("Nothing to compile! Everything's great!");
            return;
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        try (StandardJavaFileManager mgr = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(files);
            mgr.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDirectory));
            JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, null, null, null, sources);

            task.call();
        }
    }

    private static Set<String> analyzeAndGetActualDependents(List<File> dirtyFiles) {
        ClassDependenciesAnalyzer cda = new ClassDependenciesAnalyzer();
        ClassDependentsAccumulator acc = new ClassDependentsAccumulator();

//        for (File classFile : FileManager.getAllFilesInDirectory("out/", ".class")) {
//            acc.addClass(cda.getClassAnalysis(classFile));
//        }
        ClassSetAnalysis csa = new ClassSetAnalysis(acc.getAnalysis());

        System.out.println(acc.getDependentsMap());

        List<String> classNames = new ArrayList<>();
//        for (File dirtyFile : dirtyFiles) {
//            String className = StringUtils.removeEnd(dirtyFile.getName(), JAVA_FILE_EXTENSION);
//            System.out.println(className);
//            classNames.add(className);
//        }

        return csa.getRelevantDependents(classNames, new HashSet<>()).getAllDependentClasses();
    }
}
