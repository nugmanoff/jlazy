import analysis.ClassDependenciesAnalyzer;
import analysis.ClassDependentsAccumulator;
import analysis.ClassSetAnalysis;
import compilation.CompilationConfiguration;
import compilation.CompilationOrchestrator;
import file.FileManager;
import org.apache.commons.cli.*;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
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
            if (cmd.hasOption("d")) {
                String directory = cmd.getOptionValue("d");
                CompilationOrchestrator orchestrator = new CompilationOrchestrator(ToolProvider.getSystemJavaCompiler(), new FileManager(), CompilationConfiguration.defaultCompilationConfiguration());
                orchestrator.performCompilation(directory, ".");
            }
        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments!");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "jlazy", options );
            System.exit(1);
        }
    }


//    private static Set<String> analyzeAndGetActualDependents(List<File> dirtyFiles) {
//        ClassDependenciesAnalyzer cda = new ClassDependenciesAnalyzer();
//        ClassDependentsAccumulator acc = new ClassDependentsAccumulator();
//
////        for (File classFile : FileManager.getAllFilesInDirectory("out/", ".class")) {
////            acc.addClass(cda.getClassAnalysis(classFile));
////        }
//        ClassSetAnalysis csa = new ClassSetAnalysis(acc.getAnalysis());
//
//        System.out.println(acc.getDependentsMap());
//
//        List<String> classNames = new ArrayList<>();
////        for (File dirtyFile : dirtyFiles) {
////            String className = StringUtils.removeEnd(dirtyFile.getName(), JAVA_FILE_EXTENSION);
////            System.out.println(className);
////            classNames.add(className);
////        }
//
//        return csa.getRelevantDependents(classNames, new HashSet<>()).getAllDependentClasses();
//    }
}
