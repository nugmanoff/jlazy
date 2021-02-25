package compilation;

import analysis.DependentsSet;

import java.io.File;

public abstract class CompilationConfiguration {

    public static CompilationConfiguration defaultCompilationConfiguration() {
        return DefaultCompilationConfiguration.INSTANCE;
    }

    public abstract Iterable<String> getCompilerOptions();

    public abstract File getMetadataDirectory();

    public abstract File getOutputDirectory();

    private static class DefaultCompilationConfiguration extends CompilationConfiguration {
        private static final CompilationConfiguration.DefaultCompilationConfiguration INSTANCE = new CompilationConfiguration.DefaultCompilationConfiguration();

        @Override
        public Iterable<String> getCompilerOptions() {
            return null;
        }

        @Override
        public File getMetadataDirectory() {
            return null;
        }

        @Override
        public File getOutputDirectory() {
            return null;
        }
    }
}