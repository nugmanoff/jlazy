package files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class FileChangeReporter {
    private final Cache cache;
    private final FileManager fm;

    public FileChangeReporter(Cache cache, FileManager fm) {
        this.cache = cache;
        this.fm = fm;
    }

    public List<FileChange> getFileChanges(String directory, String extension) throws IOException, ClassNotFoundException {
        cache.prepareToRead();
        Map<String, String> oldFiles = cache.get();
        Map<String, String> newFiles = FileHasher.getHashesOf(fm.getAllFilesInDirectory(Paths.get(directory), extension));
        cache.put(newFiles);
        cache.save();

        return FileChangeDetector.getFileChanges(oldFiles, newFiles);
    }
}
