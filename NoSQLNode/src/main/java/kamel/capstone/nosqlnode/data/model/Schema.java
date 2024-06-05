package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Schema {
    @JsonProperty
    private String schema;

    public Schema() {}

    public Schema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
