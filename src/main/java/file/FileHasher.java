package file;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHasher {
    public static Map<String, String> getHashesOf(List<File> files) {
        Map<String, String> hashes = new HashMap<>();
        for (File file: files) {
            hashes.put(file.getPath(), getHashOf(file));
        }
        return hashes;
    }

    private static String getHashOf(File file) {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            return DigestUtils.md5Hex(is);
        }  catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
