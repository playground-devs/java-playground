package com.example.javaeight.streams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestClass {
    public static void main(String[] args){
       String sentence = "I felt happy because I saw the others were happy and because I knew I should feel happy, but I wasn't really happy";
        String [] words = sentence.split("\\s");
        Map<String, Integer> hashMap = new HashMap<>();
        Arrays.stream(words).forEach( word -> hashMap.put(word, hashMap.getOrDefault(word, 0)+1));
        hashMap.entrySet().stream().filter( entry -> entry.getValue() > 1).forEach(e -> System.out.println(e.getKey()+" ---> "+e.getValue()));
//        System.out.println(hashMap);
    }
}
