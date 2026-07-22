package com.example.javaeight.streams;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WordCount {
    public static void main(String[] args){
        String inputSentence = "Ramaraju is a very good boy";
        Map<String, Long> wordedCountMap = wordCountMap(inputSentence);
        wordedCountMap.forEach((word, count) ->
                System.out.println("Word: "+word+" count: "+count));
    }

    private static Map<String, Long> wordCountMap(String inputSentence) {
        return Arrays.stream(inputSentence.split("\\s+")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
