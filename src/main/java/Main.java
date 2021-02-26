import compilation.CompilationConfiguration;
import compilation.CompilationOrchestrator;
import file.FileManager;
import org.apache.commons.cli.*;

import javax.tools.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
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
        CommandLine cmd;

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
}
