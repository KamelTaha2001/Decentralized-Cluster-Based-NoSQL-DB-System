package kamel.capstone.bootstrapnode.util;

import kamel.capstone.bootstrapnode.data.model.User;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IOUtils {
    public static <T> List<T> readObjectsFromFile(File file) {
        try(
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            List<T> objects = new LinkedList<>();
            while (true) {
                try {
                    T obj = (T)objectInputStream.readObject();
                    if (obj == null) {
                        System.out.println("Null object");
                        break;
                    }
                    objects.add(obj);
                } catch (IOException | ClassNotFoundException e) {
                    break;
                }
            }
            return objects;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static boolean saveObjectInFile(File file, Object obj) {
        List<User> users = readObjectsFromFile(file);
        if (users.contains(obj)) return false;
        file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) { return false; }
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            for (User user : users)
                objectOutputStream.writeObject(user);
            objectOutputStream.writeObject(obj);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean doesObjectExistInFile(File file, Object obj) {
        List<User> users = readObjectsFromFile(file);
        return users.contains(obj);
    }
}