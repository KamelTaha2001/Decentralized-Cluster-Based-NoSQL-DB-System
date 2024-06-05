package kamel.capstone.nosqlnode.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.model.DataType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonUtils {
    public static String generateJsonSchema(Map<String, String> propertiesMap) {
        propertiesMap.remove("_id");
        propertiesMap.remove("_affinity");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schemaNode = mapper.createObjectNode();
        schemaNode.put("type", "object");

        ObjectNode propertiesNode = mapper.createObjectNode();
        schemaNode.put("additionalProperties", false);

        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            String propertyName = entry.getKey();
            String propertyType = entry.getValue();
            ObjectNode propertyDefinitionNode = mapper.createObjectNode();
            propertyDefinitionNode.put("type", propertyType);
            propertiesNode.set(propertyName, propertyDefinitionNode);
        }
        ObjectNode idNode = mapper.createObjectNode();
        idNode.put("type", "integer");
        propertiesNode.set("_id", idNode);
        ObjectNode affinityNode = mapper.createObjectNode();
        affinityNode.put("type", "string");
        propertiesNode.set("_affinity", affinityNode);
        schemaNode.set("properties", propertiesNode);
        return schemaNode.toString();
    }

    public static String generateDocument(Map<String, String> propertiesMap, String schemaJson) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode document = mapper.createObjectNode();
        try {
            JsonNode schema = JsonLoader.fromString(schemaJson);
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                String propertyName = entry.getKey();
                JsonNode properties = schema.findValue("properties");
                JsonNode typeNode = properties.findValue(propertyName);
                if (typeNode == null) return "";
                String typeString = typeNode.findValue("type").asText();
                switch (DataType.valueOf(typeString.toUpperCase())) {
                    case INTEGER -> {
                        int value = Integer.parseInt(entry.getValue());
                        document.put(propertyName, value);
                    }
                    case FLOAT -> {
                        float value = Float.parseFloat(entry.getValue());
                        document.put(propertyName, value);
                    }
                    case BOOLEAN -> {
                        boolean value = Boolean.parseBoolean(entry.getValue());
                        document.put(propertyName, value);
                    }
                    default -> document.put(propertyName, entry.getValue());
                }
            }
            return document.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static String extractDocument(String jsonBase, List<String> attributes) {
        try {
            if (attributes.isEmpty()) return jsonBase;
            JsonNode base = JsonLoader.fromString(jsonBase);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode document = mapper.createObjectNode();
            for (String attribute : attributes) {
                document.set(attribute, base.findValue(attribute));
            }
            return document.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static String updateProperties(String baseJson, Map<String, String> mods) {
        try {
            if (mods.isEmpty()) return baseJson;
            JsonNode base = JsonLoader.fromString(baseJson);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode document = mapper.createObjectNode();
            base.fields().forEachRemaining(field -> {
                if (!mods.containsKey(field.getKey()) || field.getKey().equals("_id") || field.getKey().equals("_affinity")) {
                    document.set(field.getKey(), field.getValue());
                }
            });
            mods.forEach((k, v) -> {
                if (!k.equals("_id") && !k.equals("_affinity")) {
                    document.put(k, v);
                }
            });
            return document.toString();
        } catch (IOException e) { return baseJson; }
    }

    public static boolean validate(String documentString, String schemaString) {
        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
            JsonNode schema = JsonLoader.fromString(schemaString);
            JsonSchema jsonSchema = schemaFactory.getJsonSchema(schema);
            JsonNode document = JsonLoader.fromString(documentString);
            return jsonSchema.validate(document).isSuccess();
        } catch (ProcessingException | IOException e) {
            return false;
        }
    }

    public static boolean doesPropertyExist(String propertyName, String schemaString) {
        try {
            getPropertyType(schemaString, propertyName);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static boolean saveToFile(String json, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    public static boolean hasColumn(String jsonString, String column) {
        try {
            JsonNode json = JsonLoader.fromString(jsonString);
            return json.has(column);
        } catch (IOException e) {
            return false;
        }
    }

    public static String getValue(String jsonString, String column) {
        try {
            JsonNode json = JsonLoader.fromString(jsonString);
            if (json.has(column))
                return json.findValue(column).asText();
            return  "";
        } catch (IOException e) {
            return "";
        }
    }

    public static String setValue(String jsonString, String column, String value, String jsonSchema) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonString);
            ObjectNode node = mapper.createObjectNode();
            Map<String, JsonNode> fields = StreamSupport.stream(Spliterators.spliteratorUnknownSize(json.fields(), Spliterator.ORDERED), false)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue
                            ));
            node.setAll(fields);
            node.remove(column);
            switch (getPropertyType(jsonSchema, column)) {
                case INTEGER -> {
                    Integer valueToPut = Integer.valueOf(value);
                    node.put(column, valueToPut);
                }
                case FLOAT -> {
                    Float valueToPut = Float.valueOf(value);
                    node.put(column, valueToPut);
                }
                case STRING -> {
                    node.put(column, value);
                }
                case BOOLEAN -> {
                    Boolean valueToPut = Boolean.valueOf(value);
                    node.put(column, valueToPut);
                }
            }
            return node.toString();
        } catch (IOException e) {
            return jsonString;
        }
    }

    public static DataType getPropertyType(String jsonSchema, String propertyName) throws NoSuchElementException {
        try {
            JsonNode schema = JsonLoader.fromString(jsonSchema);
            JsonNode property = schema.findValue("properties").findValue(propertyName);
            if (property == null)
                throw new NoSuchElementException("Property " + propertyName + " does not exist.");
            JsonNode field = property.findValue("type");
            return DataType.valueOf(field.asText().toUpperCase());
        } catch (IOException e) {
            return DataType.STRING;
        }
    }

    public static String loadJson(File file) throws IOException {
        return JsonLoader.fromFile(file).toString();
    }
}
