package com.example.javaeight.fileutils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class JsonKeyCapitalizer {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Read JSON file as Map
        Map<String, Object> originalMap = mapper.readValue(
                new File("/Users/gramaraju/Documents/new_workspace/java-eight/src/main/resources/input.json"), new TypeReference<Map<String, Object>>() {}
        );

        // Capitalize all keys recursively
        Map<String, Object> updatedMap = capitalizeKeysRecursively(originalMap);

        // Print the transformed JSON
        String updatedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updatedMap);
        System.out.println(updatedJson);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> capitalizeKeysRecursively(Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String capitalizedKey = capitalize(entry.getKey());
            Object value = entry.getValue();

            if (value instanceof Map) {
                result.put(capitalizedKey, capitalizeKeysRecursively((Map<String, Object>) value));
            } else if (value instanceof List) {
                result.put(capitalizedKey, processList((List<Object>) value));
            } else {
                result.put(capitalizedKey, value);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> processList(List<Object> list) {
        List<Object> newList = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                newList.add(capitalizeKeysRecursively((Map<String, Object>) item));
            } else if (item instanceof List) {
                newList.add(processList((List<Object>) item));
            } else {
                newList.add(item);
            }
        }
        return newList;
    }

    private static String capitalize(String key) {
        if (key == null || key.isEmpty()) return key;
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }
}
