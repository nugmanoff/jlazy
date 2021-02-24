package files;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    public List<File> getAllFilesInDirectory(String directory, String extension) {
        try (Stream<Path> walk = Files.walk(Path.of(directory))) {
            return walk
                    .map(Path::toFile)
                    .filter(f -> f.getName().endsWith(extension))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<File>();
        }
    }

    public File createOutputDirectoryIfNeeded(String directory) throws IOException {
        Path dir = Path.of(directory);
        if (directoryExists(directory)) {
            return dir.toFile();
        }
        return Files.createDirectory(dir).toFile();
    }

    private boolean directoryExists(String directory) {
        File dir = new File(directory);
        return dir.isDirectory() && dir.exists();
    }
}
