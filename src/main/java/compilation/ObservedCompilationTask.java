package compilation;

import com.sun.source.util.JavacTask;
import intermediate.ClassToSourceMappingGenerator;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ObservedCompilationTask implements JavaCompiler.CompilationTask {

    private final Function<File, Optional<String>> relativize;
    private final JavacTask delegate;
    private final ObservedCompilationResultHandler handler;

    public ObservedCompilationTask(JavaCompiler.CompilationTask delegate,
                                   Function<File, Optional<String>> relativize,
                                   ObservedCompilationResultHandler handler) {
        this.relativize = relativize;
        this.handler = handler;
        if (delegate instanceof JavacTask) {
            this.delegate = (JavacTask) delegate;
        } else {
            throw new UnsupportedOperationException("Unexpected Java compile task : " + delegate.getClass().getName());
        }
    }

    @Override
    public void addModules(Iterable<String> moduleNames) {
        delegate.addModules(moduleNames);
    }

    public void setProcessors(Iterable<? extends Processor> processors) {
        delegate.setProcessors(processors);
    }

    @Override
    public void setLocale(Locale locale) {
        delegate.setLocale(locale);
    }

    @Override
    public Boolean call() {
        ClassToSourceMappingGenerator collector = new ClassToSourceMappingGenerator(delegate.getElements());
        delegate.addTaskListener(collector);
        try {
            return delegate.call();
        } finally {
            handler.handleCompilationResult(collector.getMapping());
        }
    }

}

