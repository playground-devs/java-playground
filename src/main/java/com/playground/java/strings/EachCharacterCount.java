package com.example.javaeight.strings;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EachCharacterCount {
    public static void main(String [] args) {

        String inputString = "Ramaraju Gaddam!";
        inputString.replaceAll("\\s", "").chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().forEach(System.out::println);
        Map<Character, Long> characterCountMap = inputString.chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        characterCountMap.forEach((character, count) -> System.out.println("Character: " + character + " count: " + count));
        inputString.replaceAll("\\s","").chars().mapToObj(c -> (char)c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((k,v)-> System.out.println(k+v));

    }
}
