package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.affinity.AffinityManager;
import kamel.capstone.nosqlnode.data.affinity.DefaultAffinityManager;
import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.index.Index;
import kamel.capstone.nosqlnode.data.index.IndexingManager;
import kamel.capstone.nosqlnode.util.Constants;
import kamel.capstone.nosqlnode.util.GeneralUtils;
import kamel.capstone.nosqlnode.util.JsonUtils;

import java.io.*;
import java.util.*;

public class Collection {
    @JsonProperty
    private String name;
    @JsonIgnore
    private File collectionDirectory;
    @JsonProperty
    private String path;
    @JsonProperty
    private Schema schema;
    @JsonIgnore
    private IndexingManager indexingManager;
    @JsonProperty
    private Long nextDocumentId;
    @JsonIgnore
    private AffinityManager affinityManager;

    public Collection() {}

    public Collection(String name, File collectionDirectory, Schema schema) {
        this.name = name;
        this.collectionDirectory = collectionDirectory;
        this.path = collectionDirectory.getPath();
        this.schema = schema;
        this.indexingManager = new IndexingManager(this);
        nextDocumentId = loadNextId();
        affinityManager = new DefaultAffinityManager();
    }

    public Collection(File collectionDirectory) throws IOException {
        this.name = collectionDirectory.getName();
        this.collectionDirectory = collectionDirectory;
        this.path = collectionDirectory.getPath();
        this.schema = new Schema(JsonUtils.loadJson(new File(collectionDirectory, "meta/schema.json")));
        this.indexingManager = new IndexingManager(this);
        nextDocumentId = loadNextId();
        affinityManager = new DefaultAffinityManager();
    }

    public Result select(List<String> columns, List<Condition> conditions) {
        if (!validateColumns(columns))
            return new Result(false, Collections.emptyList());
        if (!validateColumns(conditions.stream().map(Condition::getColumn).toList()))
            return new Result(false, Collections.emptyList());

        List<Document> documents = getByConditions(conditions);
        return new Result(true, documents.stream()
                .peek(doc -> doc.setData(JsonUtils.extractDocument(doc.getData(), columns), false))
                .toList()
        );
    }

    public Result delete(List<Condition> conditions) {
        if (!validateColumns(conditions.stream().map(Condition::getColumn).toList()))
            return new Result(false, Collections.emptyList());

        List<Document> documents = getByConditions(conditions);
        String thisNodeAddress = DataSynchronizer.getInstance().getThisNodeAddress();
        boolean success = true;
        for (Document document : documents) {
            String affinity = document.getValue(Constants.AFFINITY_ATTRIBUTE_NAME);
            boolean isMyAffinity = thisNodeAddress.equals(affinity);
            if (isMyAffinity) {
                if (document.getFile().exists()) {
                    boolean deleted;
                    deleted = document.getFile().delete();
                    if (!deleted)
                        success = false;
                    else
                        indexingManager.deleteDocument(document);
                }
            }
        }
        List<Document> affinityDocuments = documents.stream()
                .filter(d -> d.getValue(Constants.AFFINITY_ATTRIBUTE_NAME).equals(thisNodeAddress))
                .toList();
        return new Result(success, affinityDocuments);
    }

    public Result insert(Map<String, String> values) {
        if (!validateColumns(values.keySet().stream().toList()))
            return new Result(false, Collections.emptyList());

        values.remove(Constants.ID_ATTRIBUTE_NAME);
        nextDocumentId = loadNextId();
        saveId(++nextDocumentId);
        values.put(Constants.ID_ATTRIBUTE_NAME, String.valueOf(nextDocumentId));
        values.remove(Constants.AFFINITY_ATTRIBUTE_NAME);
        values.put(Constants.AFFINITY_ATTRIBUTE_NAME, affinityManager.add());
        String documentData = JsonUtils.generateDocument(values, schema.getSchema());
        if (JsonUtils.validate(documentData, schema.getSchema())) {
            File documentFile = new File(Constants.getCollectionDataDirectory(name), values.get(Constants.ID_ATTRIBUTE_NAME) + ".json");
            boolean success = JsonUtils.saveToFile(documentData, documentFile);
            if (success)
                indexingManager.add(new Document(documentFile, documentData, nextDocumentId, schema));
            System.out.println("Inserted: " + documentData);
            return new Result(success, List.of(new Document(documentFile, documentData, nextDocumentId - 1, schema)));
        } else {
            System.out.println("Invalid schema");
            return new Result(false, null);
        }
    }

    public Result update(Map<String, String> modifications, List<Condition> conditions) {
        if (!validateColumns(modifications.keySet().stream().toList()))
            return new Result(false, Collections.emptyList());
        if (!validateColumns(conditions.stream().map(Condition::getColumn).toList()))
            return new Result(false, Collections.emptyList());

        List<Document> documents = getByConditions(conditions);
        String thisNodeAddress = DataSynchronizer.getInstance().getThisNodeAddress();
        documents.forEach(document -> {
            String affinity = document.getValue("_affinity");
            boolean isMyAffinity = thisNodeAddress.equals(affinity);
            if (isMyAffinity) {
                modifications.forEach((column, mod) -> {
                    if (indexingManager.contains(column)) {
                        indexingManager.modify(column, document, mod);
                    } else {
                        String modifiedJson = JsonUtils.updateProperties(document.getData(), Collections.singletonMap(column, mod));
                        document.setData(modifiedJson, true);
                    }
                });
            }
        });
        List<Document> affinityDocuments = documents.stream()
                .filter(d -> d.getValue(Constants.AFFINITY_ATTRIBUTE_NAME).equals(thisNodeAddress))
                .toList();
        return new Result(true, affinityDocuments);
    }

    public Index createIndex(String column) throws IOException {
        List<Document> collectionDocuments = Arrays.stream(Objects.requireNonNull(new File(collectionDirectory, "data").listFiles()))
                        .map(file -> {
                            try {
                                return new Document(file, Long.parseLong(file.getName().split("\\.")[0]), schema);
                            } catch (IOException e) {
                                throw new RuntimeException("Could not create index: " + e.getLocalizedMessage());
                            }
                        }).toList();
        return indexingManager.createIndex(column, collectionDocuments);
    }

    public boolean deleteIndex(String column) throws RuntimeException {
        return indexingManager.deleteIndex(column);
    }

    private long loadNextId() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(new File(Constants.getIdFilePath(name))))) {
            return dis.readLong();
        } catch (IOException e) {
            return 0;
        }
    }

    private boolean saveId(long id) {
        File idFile = new File(Constants.getIdFilePath(name));
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(idFile))) {
            dos.writeLong(id);
            return true;
        } catch (IOException e) { return false; }
    }

    private List<Document> getByConditions(List<Condition> conditions) {
        List<Condition> indexedColumns = conditions.stream()
                .filter( condition -> indexingManager.contains(condition.getColumn()))
                .toList();
        List<Condition> nonIndexedColumns = new ArrayList<>(Collections.nCopies(conditions.size(), null));
        Collections.copy(nonIndexedColumns, conditions);
        nonIndexedColumns.removeAll(indexedColumns);
        List<List<Document>> lists = indexedColumns.stream()
                .map(condition -> {
                    try {
                        return indexingManager.get(condition.getColumn(), condition.getConditionType(), condition.getValue());
                    } catch (IOException | ClassNotFoundException e) {
                        return new LinkedList<Document>();
                    }
                })
                .toList();
        if (lists.isEmpty())
            return getWithoutIndexing(conditions);
        List<Document> intersection = GeneralUtils.intersection(lists);
        return intersection.stream()
                .filter(document -> filterByCondition(document, nonIndexedColumns))
                .toList();
    }

    private List<Document> getWithoutIndexing(List<Condition> conditions) {
        return Arrays.stream(Objects.requireNonNull(new File(Constants.getCollectionDataDirectory(name)).listFiles()))
                .map(file -> {
                    try {
                        long id = Long.parseLong(file.getName().split("\\.")[0]);
                        return new Document(file, id, schema);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(document -> filterByCondition(document, conditions))
                .toList();
    }

    private boolean filterByCondition(Document document, List<Condition> conditions) {
        boolean included = true;
        for (Condition condition : conditions) {
            if (document.hasColumn(condition.getColumn())) {
                String value = document.getValue(condition.getColumn());
                switch (condition.getConditionType()) {
                    case EQUAL -> {
                        if (!value.equals(condition.getValue()))
                            included = false;
                    }
                    case NOT_EQUAL -> {
                        if (value.equals(condition.getValue()))
                            included = false;
                    }
                    default -> {
                        if (!value.equals(condition.getValue()))
                            included = false;
                    }
                }
            }
        }
        return included;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public File getDirectory() {
        return collectionDirectory;
    }

    public Schema getSchema() {
        return schema;
    }

    public boolean init() {
        File directory = new File(Constants.getCollectionDirectory(name));
        if (directory.exists() || !directory.mkdir())
            return false;
        if (indexingManager == null)
            indexingManager = new IndexingManager(this);
        new File(Constants.getCollectionDataDirectory(name)).mkdir();
        File metaDirectory = new File(Constants.getCollectionMetaDirectory(name));
        metaDirectory.mkdir();
        new File(Constants.getCollectionIndexesDirectory(name)).mkdirs();
        JsonUtils.saveToFile(schema.getSchema(), new File(metaDirectory, "schema.json"));
        try(OutputStream fos = new FileOutputStream(Constants.getIdFilePath(name))) {
            fos.write(1);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @JsonIgnore
    public List<Index> getIndexes() {
        try {
            return indexingManager.loadAllIndexes();
        } catch (IOException | ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }

    @JsonProperty
    public String getPath() {
        return path;
    }

    public void setCollectionDirectory(File collectionDirectory) {
        this.collectionDirectory = collectionDirectory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setIndexingManager(IndexingManager indexingManager) {
        this.indexingManager = indexingManager;
    }

    public Long getNextDocumentId() {
        return nextDocumentId;
    }

    public void setNextDocumentId(Long nextDocumentId) {
        this.nextDocumentId = nextDocumentId;
    }

    private boolean validateColumns(List<String> columns) {
        for (String column : columns) {
            if (!JsonUtils.doesPropertyExist(column, schema.getSchema()))
                return false;
        }
        return true;
    }
}
