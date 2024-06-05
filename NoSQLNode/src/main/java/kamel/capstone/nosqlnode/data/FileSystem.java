package kamel.capstone.nosqlnode.data;

import kamel.capstone.nosqlnode.data.model.Collection;
import kamel.capstone.nosqlnode.data.observer.CollectionsSubject;
import kamel.capstone.nosqlnode.data.observer.Subject;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Component
public class FileSystem {
    public final Subject<Collection> collectionsSubject = new CollectionsSubject();

    public boolean saveCollection(Collection collection) {
        boolean success = collection.init();
        if (success)
            collectionsSubject.notifySubscribers();
        return success;
    }

    public List<Collection> loadCollections() throws FileNotFoundException {
        List<Collection> result = new LinkedList<>();
        File collectionsDirectory = new File(Constants.COLLECTIONS_DIRECTORY);
        if (!collectionsDirectory.exists())
            collectionsDirectory.mkdirs();
        for (File file : Objects.requireNonNull(collectionsDirectory.listFiles())) {
            try {
                result.add(new Collection(new File(collectionsDirectory, file.getName())));
            } catch (IOException e) {
                throw new FileNotFoundException("Error: Could not find schema of " + file.getName());
            }
        }
        return result;
    }

    public void createDirectory(String path) {
        File root = new File(path);
        if (!root.exists())
            root.mkdirs();
    }

    public byte[] readFile(String path) {
        try(FileInputStream fis = new FileInputStream(path)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            return new byte[] {};
        }
    }

    public List<byte[]> readFiles(List<String> paths) {
        List<byte[]> result = new LinkedList<>();
        for (String path : paths) {
            try(FileInputStream fis = new FileInputStream(path)) {
                result.add(fis.readAllBytes());
            } catch (IOException e) {
                return Collections.emptyList();
            }
        }
        return result;
    }
}
