package com.playground.java.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class GroupAndSortByFirstLetter {
    public static void main (String[] args){
        List<String> names= Arrays.asList("Aniketh", "Anush", "Cathane", "Daniel", "Babitha");
        Map<Character, List<String>> result = names.stream().collect(Collectors.groupingBy(name -> name.charAt(0), TreeMap::new, Collectors.toList()));
        result.forEach((k,v) -> System.out.println(k+"--->"+v));
    }
}
