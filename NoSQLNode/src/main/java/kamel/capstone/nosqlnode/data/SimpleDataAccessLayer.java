package kamel.capstone.nosqlnode.data;

import kamel.capstone.nosqlnode.data.broadcast.*;
import kamel.capstone.nosqlnode.data.index.Index;
import kamel.capstone.nosqlnode.data.model.Collection;
import kamel.capstone.nosqlnode.data.model.Condition;
import kamel.capstone.nosqlnode.data.model.Document;
import kamel.capstone.nosqlnode.data.model.Result;
import kamel.capstone.nosqlnode.data.model.Schema;
import kamel.capstone.nosqlnode.util.Constants;
import kamel.capstone.nosqlnode.util.GeneralUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component("Simple")
public class SimpleDataAccessLayer implements DataAccessLayer {
    private List<Collection> collections;
    private final FileSystem fileSystem;
    private final Object insertLock = new Object();
    private final Object updateLock = new Object();
    private final Object deleteDocumentLock = new Object();
    private final Object createCollectionLock = new Object();
    private final Object deleteCollectionLock = new Object();
    private final Object createIndexLock = new Object();

    public SimpleDataAccessLayer(FileSystem fileSystem) throws FileNotFoundException {
        this.fileSystem = fileSystem;
        subscribeToFileSystem();
        collections = fileSystem.loadCollections();
        initDirectories();
    }

    private void initDirectories() {
        fileSystem.createDirectory(Constants.ROOT_DIRECTORY);
        fileSystem.createDirectory(Constants.COLLECTIONS_DIRECTORY);
        fileSystem.createDirectory(Constants.META_DIRECTORY);
    }

    private void subscribeToFileSystem() {
        fileSystem.collectionsSubject.subscribe(() -> {
            try {
                collections = fileSystem.loadCollections();
            } catch (FileNotFoundException exception) {}
        });
    }

    @Override
    public Result select(String collectionName, List<String> columns, List<Condition> conditions) {
        Optional<Collection> collectionOptional = collections.stream().
                filter(collection -> collection.getName().equals(collectionName))
                .findFirst();
        if (collectionOptional.isPresent()) {
            return collectionOptional.get().select(columns, conditions);
        } else {
            return new Result(false, Collections.emptyList());
        }
    }

    @Override
    public Result insert(String collectionName, Map<String, String> attributes) {
        synchronized (insertLock) {
            Optional<Collection> collectionOptional = getCollectionByName(collectionName);
            if (collectionOptional.isPresent()) {
                Result result = collectionOptional.get().insert(attributes);
                if (result.isSuccess()) {
                    var dataToSync = new HashMap<>(Collections.singletonMap(result.getDocuments().get(0).getPath(), result.getDocuments().get(0).getData().getBytes()));
                    dataToSync.putAll(collectionOptional.get().getIndexes().stream().collect(Collectors.toMap(
                            Index::getPath,
                            i -> fileSystem.readFile(i.getPath())
                    )));
                    byte[] idValue = GeneralUtils.longToByteArray(collectionOptional.get().getNextDocumentId());
                    dataToSync.put(Constants.getIdFilePath(collectionName), idValue);
                    dataToSync.put(Constants.getAffinityFilePath(), fileSystem.readFile(Constants.getAffinityFilePath()));
                    broadcastData(new WriteFileSyncAction(dataToSync));
                }
                return result;
            } else {
                return new Result(false, Collections.emptyList());
            }
        }
    }

    @Override
    public Result update(String collectionName, Map<String, String> modifications, List<Condition> conditions) {
        synchronized (updateLock) {
            Optional<Collection> collectionOptional = getCollectionByName(collectionName);
            if (collectionOptional.isPresent()) {
                Result result = collectionOptional.get().update(modifications, conditions);
                if (result.isSuccess()) {
                    var docsAndIndexes = result.getDocuments().stream().collect(Collectors.toMap(
                            Document::getPath,
                            d -> d.getData().getBytes()
                    ));
                    if (!docsAndIndexes.isEmpty()) {
                        docsAndIndexes.putAll(collectionOptional.get().getIndexes().stream().collect(Collectors.toMap(
                                Index::getPath,
                                i -> fileSystem.readFile(i.getPath())
                        )));
                        broadcastData(new WriteFileSyncAction(docsAndIndexes));
                    }
                }
                return new Result(true, Collections.emptyList());
            } else {
                return new Result(false, Collections.emptyList());
            }
        }
    }

    @Override
    public Result delete(String collectionName, List<Condition> conditions) {
        synchronized (deleteDocumentLock) {
            Optional<Collection> collectionOptional = getCollectionByName(collectionName);
            if (collectionOptional.isPresent()) {
                Result result = collectionOptional.get().delete(conditions);
                result.getDocuments().forEach(System.out::println);
                if (result.isSuccess()) {
                    var docs = result.getDocuments().stream().map(Document::getPath).toList();
                    if (!docs.isEmpty()) {
                        broadcastData(new DeleteFileSyncAction(docs));
                        var indexes = collectionOptional.get().getIndexes().stream().collect(Collectors.toMap(
                                Index::getPath,
                                i -> fileSystem.readFile(i.getPath())
                        ));
                        broadcastData(new WriteFileSyncAction(indexes));
                    }
                }
                return new Result(true, Collections.emptyList());
            } else {
                return new Result(false, Collections.emptyList());
            }
        }
    }

    @Override
    public Result createCollection(String collectionName, String schema) {
        synchronized (createCollectionLock) {
            File collection = new File(Constants.getCollectionDirectory(collectionName));
            if (collection.exists()) return new Result(false, Collections.emptyList());
            Collection newCollection = new Collection(collectionName, collection, new Schema(schema));
            if (newCollection.init()) {
                collections.add(newCollection);
                broadcastData(new CreateCollectionAction(newCollection));
                return new Result(true, Collections.emptyList());
            } else {
                return new Result(false, Collections.emptyList());
            }
        }
    }

    @Override
    public Result deleteCollection(String name) {
        synchronized (deleteCollectionLock) {
            File collection = new File(Constants.getCollectionDirectory(name));
            if (collection.exists()) {
                try {
                    FileUtils.deleteDirectory(collection);
                    Optional<Collection> collectionOptional = collections.stream().filter(c -> c.getName().equals(name)).findFirst();
                    if (collectionOptional.isPresent()) {
                        broadcastData(new DeleteCollectionAction(collectionOptional.get()));
                        return new Result(true, Collections.emptyList());
                    } else {
                        return new Result(false, Collections.emptyList());
                    }
                } catch (IOException e) {
                    return new Result(false, Collections.emptyList());
                }
            } else {
                System.out.println("Collection " + collection.getPath() + " does not exist.");
                return new Result(false, Collections.emptyList());
            }
        }
    }

    @Override
    public Result createIndex(String collectionName, String columnName) {
        synchronized (createIndexLock) {
            Optional<kamel.capstone.nosqlnode.data.model.Collection> collectionOptional = getCollectionByName(collectionName);
            return collectionOptional
                    .map(collection -> {
                        try {
                            Index index = collection.createIndex(columnName);
                            broadcastData(new WriteFileSyncAction(Collections.singletonMap(index.getPath(), fileSystem.readFile(index.getPath()))));
                            return new Result(true, Collections.emptyList());
                        } catch (IOException e) {
                            return new Result(false, Collections.emptyList());
                        }
                    })
                    .orElseGet(() -> new Result(false, Collections.emptyList()));
        }
    }

    @Override
    public Result describe() {
        List<String> collectionsNames =  collections.stream().map(kamel.capstone.nosqlnode.data.model.Collection::getName).toList();
        Result result = new Result(true, Collections.emptyList());
        result.setResult(collectionsNames);
        return result;
    }

    private Optional<Collection> getCollectionByName(String collectionName) {
        return collections.stream().
                filter(collection -> collection.getName().equals(collectionName))
                .findFirst();
    }

    private void broadcastData(DataSyncingAction syncingAction) {
        DataSynchronizer.getInstance().broadcastData(syncingAction);
    }

    private Long extractFileId(String fileName) {
        return Long.parseLong(fileName.split("\\.")[0]);
    }
}
