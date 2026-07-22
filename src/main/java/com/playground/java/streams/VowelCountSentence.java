package com.example.javaeight.streams;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class VowelCountSentence {
    public static void main(String[] args){
        String sentence = "Hello World this is Java";
        List<String> words = Arrays.stream(sentence.split("\\s+"))
                .filter(word -> word.matches(".*[aeiouAEIOU].*"))
                .collect(Collectors.toList());
        words.forEach(System.out::println);

        BiFunction<Integer, Integer, Integer> sum = (var x, var y) -> x + y ;
        System.out.println(sum.apply(5,10));
    }
}
