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
        HashMap<String, String> oldFiles = cache.get();
        HashMap<String, String> newFiles = new HashMap<>();

        for (File file: getAllFilesInDirectory(directory, extension)) {
            newFiles.put(file.getPath(), getHashOf(file));
        }
        cache.put(newFiles);
        cache.save();

        // Diffing

        List<FileChange> fileChanges = new ArrayList<>();
        MapDifference<String, String> diff = Maps.difference(oldFiles, newFiles);

        Map<String, MapDifference.ValueDifference<String>> entriesDiffering = diff.entriesDiffering();
        for (String diffEntryKey: entriesDiffering.keySet()) {
            fileChanges.add(new FileChange(FileChangeType.MODIFY, new File(diffEntryKey)));
        }

        Map<String, String> entriesOnlyOnRight = diff.entriesOnlyOnRight();
        for (String onlyOnRightKey: entriesOnlyOnRight.keySet()) {
            fileChanges.add(new FileChange(FileChangeType.ADD, new File(onlyOnRightKey)));
        }

        Map<String, String> entriesOnlyOnLeft = diff.entriesOnlyOnLeft();
        for (String onlyOnLeftKey: entriesOnlyOnLeft.keySet()) {
            fileChanges.add(new FileChange(FileChangeType.REMOVE, new File(onlyOnLeftKey)));
        }

        return fileChanges;
    }

    public Map<String, String> getHashesOfFiles(List<File> files) throws IOException {
        Map<String, String> hashes = new HashMap<>();
        for (File file: files) {
            hashes.put(file.getPath(), getHashOf(file));
        }
        return hashes;
    }

    public String getHashOf(File file) throws IOException {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            return DigestUtils.md5Hex(is);
        }
    }

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
