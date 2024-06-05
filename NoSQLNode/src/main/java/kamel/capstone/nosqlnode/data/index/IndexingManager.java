package kamel.capstone.nosqlnode.data.index;

import kamel.capstone.nosqlnode.data.model.Collection;
import kamel.capstone.nosqlnode.data.model.ConditionType;
import kamel.capstone.nosqlnode.data.model.Document;
import kamel.capstone.nosqlnode.util.Constants;
import kamel.capstone.nosqlnode.util.JsonUtils;
import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class IndexingManager {
    private final Collection collection;

    public IndexingManager(Collection collection) {
        this.collection = collection;
    }

    public boolean contains(String column) {
        return new File(Constants.getCollectionIndex(collection.getName(), column)).exists();
    }

    public List<Document> get(String column, ConditionType operator, String threshold) throws IOException, ClassNotFoundException {
        Index index = loadIndex(column);
        return index.get(threshold).stream().map(path -> {
            File document = new File(path);
            String id = document.getName().split("\\.")[0];
            try {
                return new Document(document, JsonUtils.loadJson(document), Long.parseLong(id), collection.getSchema());
            } catch (IOException e) {
                throw new RuntimeException("Error loading JSON: " + e.getMessage());
            }

        }).toList();
    }

    public boolean add(Document document) {
        try {
            synchronized (this) {
                List<Index> indexes = loadAllIndexes();
                indexes.forEach(index -> {
                    String column = index.getColumnName();
                    if (document.hasColumn(column))
                        index.add(document);
                });
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    public boolean deleteDocument(Document document) {
        try {
            synchronized (this) {
                List<Index> indexes = loadAllIndexes();
                indexes.forEach(index -> {
                    String column = index.getColumnName();
                    if (document.hasColumn(column))
                        index.delete(document);
                });
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    public Index createIndex(String column, List<Document> collectionDocuments) throws IOException {
        synchronized (this) {
            File indexesDirectory = new File(Constants.getCollectionIndexesDirectory(collection.getName()));
            if (!indexesDirectory.exists())
                indexesDirectory.mkdir();
            File indexFile = new File(indexesDirectory, column);
            if (indexFile.exists()) throw new IOException("Index already exists!");
            Index index = new SimpleIndex(collection.getName(), collection.getDirectory().getPath(), column);
            collectionDocuments.forEach(index::add);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(indexFile));
            ois.writeObject(index);
            ois.close();
            return index;
        }
    }

    public boolean deleteIndex(String column) {
        synchronized (this) {
            if (contains(column))
                return new File(Constants.getCollectionIndexesDirectory(collection.getName())).delete();
            return false;
        }
    }

    public Document modify(String column, Document document, String newValue) {
        try {
            synchronized (this) {
                Index index = loadIndex(column);
                index.delete(document);
                String newJson = JsonUtils.updateProperties(document.getData(), Collections.singletonMap(column, newValue));
                document.setData(newJson, true);
                index.add(document);
                return document;
            }
        } catch (IOException | ClassNotFoundException e) {
            return document;
        }
    }

    private Index loadIndex(String column) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(Constants.getCollectionIndex(collection.getName(), column))));
        return (Index) ois.readObject();
    }

    public List<Index> loadAllIndexes() throws IOException, ClassNotFoundException  {
        List<Index> indexes = new LinkedList<>();
        for (File file : Objects.requireNonNull(new File(Constants.getCollectionIndexesDirectory(collection.getName())).listFiles())) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            indexes.add((Index) ois.readObject());
        }
        return indexes;
    }
}
