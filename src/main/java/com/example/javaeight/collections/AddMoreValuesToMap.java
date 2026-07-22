package com.example.javaeight.collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AddMoreValuesToMap {
    public static void main(String [] args){
        Map<Integer, HashSet<String>> multiValueMap = new HashMap<>();
        multiValueMap.computeIfAbsent(1, k -> new HashSet<>()).add("Value1");
        multiValueMap.computeIfAbsent(1, k -> new HashSet<>()).add("Value2");
        multiValueMap.get(1);
    }
}
