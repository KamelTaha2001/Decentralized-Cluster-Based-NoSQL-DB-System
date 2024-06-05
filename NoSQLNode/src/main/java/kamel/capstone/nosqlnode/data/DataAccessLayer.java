package kamel.capstone.nosqlnode.data;

import kamel.capstone.nosqlnode.data.model.Condition;
import kamel.capstone.nosqlnode.data.model.Result;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public interface DataAccessLayer {
    Result select(String collectionName, List<String> columns, List<Condition> conditions);
    Result insert(String collectionName, Map<String, String> attributes);
    Result update(String collectionName, Map<String, String> modifications, List<Condition> conditions);
    Result delete(String collectionName, List<Condition> conditions);
    Result createCollection(String collectionName, String schema);
    Result deleteCollection(String collectionName);
    Result createIndex(String collectionName, String columnName);
    Result describe();
}
