package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.util.JsonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Document {
    @JsonIgnore
    private File file;
    @JsonProperty
    private String path;
    @JsonIgnore
    private String data;
    @JsonProperty
    private long id;
    @JsonProperty
    private Schema schema;

    public Document() {}

    public Document(File file, long id, Schema schema) throws IOException {
        this.file = file;
        this.path = file.getPath();
        this.id = id;
        FileInputStream fis = new FileInputStream(file);
        this.data = new String(fis.readAllBytes());
        fis.close();
        this.schema = schema;
    }

    public Document(File file, String data, long id, Schema schema) {
        this.file = file;
        this.path = file.getPath();
        this.id = id;
        this.data = data;
        this.schema = schema;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getData() {
        return data;
    }

    public void setData(String data, boolean save) {
        this.data = data;
        if (save)
            JsonUtils.saveToFile(data, file);
    }

    public boolean hasColumn(String column) {
        return JsonUtils.hasColumn(data, column);
    }

    public String getValue(String column) {
        return JsonUtils.getValue(data, column);
    }

    public void setValue(String column, String value) {
        data = JsonUtils.setValue(data, column, value, schema.getSchema());
        JsonUtils.saveToFile(data, file);
    }

    @Override
    public String toString() {
        return data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
