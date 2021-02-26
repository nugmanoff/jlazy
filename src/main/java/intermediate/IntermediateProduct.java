package intermediate;

import compilation.CompilationConfiguration;

import java.io.*;

public abstract class IntermediateProduct {

    private final File file;
    private Object object;

    public IntermediateProduct(File file, Object object) {
        this.file = file;
        this.object = object;
    }

    public boolean exists() {
        return file.exists();
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void delete() {
        file.delete();
    }

    public Object read() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    public void write() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
