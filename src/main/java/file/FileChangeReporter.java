package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class FileChangeReporter {
    private final FileManager fm;

    public FileChangeReporter(FileManager fm) {
        this.fm = fm;
    }

    public List<FileChange> getFileChanges(String directory, String extension) throws IOException, ClassNotFoundException {
        File cacheFile = new File("asd");
        Map<String, String> oldFiles = (Map<String, String>) FileSerializer.deserialize(cacheFile);
        Map<String, String> newFiles = FileHasher.getHashesOf(fm.getAllFilesInDirectory(Paths.get(directory), extension));
        FileSerializer.serialize(newFiles, cacheFile);

        return FileChangeDetector.getFileChanges(oldFiles, newFiles);
    }
}
