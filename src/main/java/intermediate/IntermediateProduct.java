package intermediate;

import java.io.*;

public abstract class IntermediateProduct {

    private final File file;
    private final Object object;

    protected IntermediateProduct(File file, Object object) {
        this.file = file;
        this.object = object;
    }

    boolean exists() {
        return file.exists();
    }

    Object read() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    void write() throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        fos.close();
    }
}
