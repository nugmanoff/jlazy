import compilation.CompilationConfiguration;
import compilation.CompilationOrchestrator;
import file.FileManager;
import org.apache.commons.cli.*;

import javax.tools.*;
import java.io.File;
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
            String directory = null;
            String classpath = null;
            if (cmd.hasOption("d")) {
                directory = cmd.getOptionValue("d");
            }
            if (cmd.hasOption("cp")) {
                classpath = cmd.getOptionValue("cp");
            }
            if (directory == null) {
                throw new ParseException("");
            }
            if (classpath == null) {
                classpath = ".";
            }
            String finalDirectory = directory;
            String finalClasspath = classpath;
            CompilationConfiguration compilationConfiguration = new CompilationConfiguration() {
                @Override
                public Iterable<String> getCompilerOptions() {
                    return null;
                }

                @Override
                public File getMetadataDirectory() {
                    return new File(".jlazy/");
                }

                @Override
                public File getOutputDirectory() {
                    return new File("out/");
                }

                @Override
                public File getSourcesDirectory() {
                    if (finalDirectory.endsWith("/")) {
                        return new File(finalDirectory);
                    } else {
                        return new File(finalDirectory + "/");
                    }
                }

                @Override
                public File getClasspathDirectory() {
                    return new File(finalClasspath);
                }
            };

            CompilationOrchestrator orchestrator = new CompilationOrchestrator(ToolProvider.getSystemJavaCompiler(), new FileManager(), compilationConfiguration);
            orchestrator.performCompilation();
        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments!");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "jlazy", options );
            System.exit(1);
        }
    }
}
