package analysis;

import com.google.common.io.ByteStreams;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassDependenciesExtractor {

    public ClassAnalysis getClassAnalysis(InputStream input) throws IOException {
        ClassReader reader = new ClassReader(ByteStreams.toByteArray(input));
        String className = reader.getClassName().replace("/", ".");
        return ClassDependenciesAnalyzer.analyze(className, reader);
    }

    public ClassAnalysis getClassAnalysis(File file) {
        try (InputStream input = new FileInputStream(file)) {
            return getClassAnalysis(input);
        } catch (IOException e) {
            throw new RuntimeException("Не получилось загрузить классфайл " + file.toString());
        }
    }
}