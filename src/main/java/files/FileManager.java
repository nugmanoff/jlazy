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

    public File createFile(Path path) throws IOException {
        if(!path.getParent().toFile().exists()) {
            createDirectory(path);
        }
        return Files.createFile(path).toFile();
    }

    public File createDirectory(Path path) throws IOException {
        return Files.createDirectories(path).toFile();
    }

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
