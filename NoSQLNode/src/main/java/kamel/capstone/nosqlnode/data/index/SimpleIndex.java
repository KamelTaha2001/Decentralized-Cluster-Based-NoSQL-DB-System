package kamel.capstone.nosqlnode.data.index;

import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.model.Document;
import kamel.capstone.nosqlnode.util.BTree;
import kamel.capstone.nosqlnode.util.Constants;

import java.io.*;
import java.util.List;

public class  SimpleIndex implements Index {
    @JsonProperty
    private String columnName;
    @JsonProperty
    private BTree tree;
    @JsonProperty
    private String collectionPath;
    @JsonProperty
    private String collectionName;
    public static final String TYPE = "simple_index";

    public SimpleIndex() {}

    public SimpleIndex(String collectionName, String collectionPath, String columnName) {
        this.columnName = columnName;
        tree = new BTree(3);
        this.collectionPath = collectionPath;
        this.collectionName = collectionName;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public List<String> get(String value) {
        return tree.search(value);
    }

    @Override
    public boolean add(Document document) {
        if (document.hasColumn(columnName)) {
            String value = document.getValue(columnName);
            tree.insert(value, document.getFile().getPath());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(collectionPath + "/meta/indexes", columnName)))) {
                oos.writeObject(this);
            } catch (IOException ignored){
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean delete(Document document) {
        if (document.hasColumn(columnName)) {
            String value = document.getValue(columnName);
            return tree.remove(value, document.getFile().getPath());
        }
        return false;
    }

    @Override
    public String getPath() {
        return Constants.getCollectionIndex(collectionName, columnName);
    }

    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setTree(BTree tree) {
        this.tree = tree;
    }
}
