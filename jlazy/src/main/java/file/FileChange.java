package file;

import java.io.File;

public class FileChange {
    private final Type type;
    private final File file;

    public FileChange(Type type, File file) {
        this.type = type;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADD,
        MODIFY,
        REMOVE
    }
}