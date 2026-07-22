package com.example.javaeight.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaEightRepeatedWordsExample {
    public static void main(String[] args){

        String sentence = "This is a sample sentence with some repeated words. This is a sample sentence.";
//        List wordsList = Arrays.stream(sentence.split("\\s+")).map(String::toLowerCase).collect(Collectors.toList());
//        // Use Collectors.groupingBy to count occurrences of each word
//        Map<String, Long> wordCounts  = (Map<String, Long>) wordsList.stream().collect(Collectors.groupingBy(word -> word, Collectors.counting()));
//        wordCounts.forEach((word, count) ->
//                System.out.println("Word: "+word+" count: "+count));



        Arrays.stream(sentence.split("\\s+")).map(String::toLowerCase).map(s -> s.replaceAll("\\.","")).collect(Collectors.groupingBy(word -> word, Collectors.counting())).entrySet().forEach(System.out::println);
    }
}
