package files;

import java.io.File;

public class FileChange {
    private final FileChangeType type;
    private final File file;

    public FileChange(FileChangeType type, File file) {
        this.type = type;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public FileChangeType getType() {
        return type;
    }
}