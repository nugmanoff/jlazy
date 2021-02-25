package files;


import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    private final Cache cache;

    public FileManager(Cache cache) {
        this.cache = cache;
    }

    public List<FileChange> getFileChangesList(String directory, String extension) throws IOException, ClassNotFoundException {
        cache.prepareToRead();
        Map<String, String> oldFiles = cache.get();
        Map<String, String> newFiles = FileHasher.getHashesOf(getAllFilesInDirectory(directory, extension));
        cache.put(newFiles);
        cache.save();

        return FileChangeDetector.getFileChanges(oldFiles, newFiles);
    }

    public static List<File> getAllFilesInDirectory(String directory, String extension) {
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
