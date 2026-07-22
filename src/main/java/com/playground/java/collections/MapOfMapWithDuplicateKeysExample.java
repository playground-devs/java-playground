package com.example.javaeight.collections;

import java.util.HashMap;
import java.util.Map;

public class MapOfMapWithDuplicateKeysExample {

    public static void main(String[] args) {
        Map<String, Map<String, String>> mapOfMap = new HashMap<>();

        // Adding values to the map
        addToMapOfMap(mapOfMap, "Key1", "InnerKey1", "Value1");
        addToMapOfMap(mapOfMap, "Key1", "InnerKey2", "Value2");
        addToMapOfMap(mapOfMap, "Key2", "InnerKey1", "Value3");

        // Displaying the contents of the map
        displayMapOfMap(mapOfMap);
    }

    private static void addToMapOfMap(Map<String, Map<String, String>> mapOfMap,
                                      String outerKey, String innerKey, String value) {
        // Using computeIfAbsent to ensure that an inner map is associated with the outer key
        mapOfMap.computeIfAbsent(outerKey, k -> new HashMap<>()).put(innerKey, value);
    }

    private static void displayMapOfMap(Map<String, Map<String, String>> mapOfMap) {
        // Iterating over the entries of the outer map and displaying the key-value pairs
        mapOfMap.forEach((outerKey, innerMap) -> {
            System.out.println("Outer Key: " + outerKey);
            displayInnerMap(innerMap);
        });
    }

    private static void displayInnerMap(Map<String, String> innerMap) {
        // Iterating over the entries of the inner map and displaying the key-value pairs
        innerMap.forEach((innerKey, value) ->
                System.out.println("  Inner Key: " + innerKey + ", Value: " + value));
    }
}
