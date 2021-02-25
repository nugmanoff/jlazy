package files;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    private final File persistentFile;
    private Map<String, String> map = new HashMap<>();

    public Cache(File persistentFile) {
        this.persistentFile = persistentFile;
    }

    public void put(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> get() {
        return map;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void prepareToRead() throws IOException, ClassNotFoundException {
        try {
            FileInputStream fis = new FileInputStream(persistentFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap<String,String>)ois.readObject();
            ois.close();
            fis.close();
        } catch (EOFException e) {

        }
    }

    public void save() throws IOException {
        FileOutputStream fos=new FileOutputStream(persistentFile);
        ObjectOutputStream oos=new ObjectOutputStream(fos);
        oos.writeObject(map);
        oos.flush();
        oos.close();
        fos.close();
    }
}
