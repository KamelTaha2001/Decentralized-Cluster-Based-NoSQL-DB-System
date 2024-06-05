package kamel.capstone.nosqlnode.data.index;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import kamel.capstone.nosqlnode.data.broadcast.*;
import kamel.capstone.nosqlnode.data.model.Document;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleIndex.class, name = SimpleIndex.TYPE)
})
public interface Index extends Serializable {
    String getColumnName();
    List<String> get(String value);
    boolean add(Document document);
    boolean delete(Document document);
    String getPath();
}
