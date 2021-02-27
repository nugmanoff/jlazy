package intermediate;

import compilation.CompilationConfiguration;

import java.io.*;

public abstract class IntermediateProduct {

    final File file;
    Object object;

    public IntermediateProduct(File file, Object object) {
        this.file = file;
        this.object = object;
    }

    public boolean exists() {
        return file.exists();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void delete() {
        file.delete();
    }

    public void read() {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            setObject(obj);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
