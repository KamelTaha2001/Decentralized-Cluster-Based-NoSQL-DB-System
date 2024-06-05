package kamel.capstone.nosqlnode.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandUtils {
    public static Map<String, String> extractAttributesMap(String attributes) throws ProcessingException {
        return Arrays.stream(attributes.split(","))
                .map(s -> {
                    String[] splits = s.trim().split(":");
                    return Map.entry(splits[0].trim(), splits[1].trim());
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> extractParametersMap(String attributes) throws ProcessingException {
        String[] splits = attributes.split(",");
        return Arrays.stream(splits)
                .map(s -> {
                    String[] attrs = s.trim().split("=");
                    return Map.entry(attrs[0].trim(), attrs[1].trim());
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
