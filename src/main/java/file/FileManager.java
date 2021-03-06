package file;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {

    // Создаёт файл, и необходимые родительские директории, если это необходимо
    public File createFile(Path path) throws IOException {
        if(path.toFile().exists()) {
            return path.toFile();
        }
        if(!path.getParent().toFile().exists()) {
            createDirectory(path.getParent());
        }
        return Files.createFile(path).toFile();
    }

    // Создаёт директорию, и необходимые родительские директории, если это необходимо
    public File createDirectory(Path path) {
        try {
            return Files.createDirectories(path).toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Достаёт из директории все файлы с определённым расширением
    public List<File> getAllFilesInDirectory(Path directory, String extension) {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk
                    .map(Path::toFile)
                    .filter(f -> f.getName().endsWith(extension))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
