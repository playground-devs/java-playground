package com.example.javaeight.strings;

import java.util.List;
import java.util.stream.Collectors;

public class ExtractVowels {
    public static void main(String[] args){
        String str = "Hello World";

        List vowels = str.toLowerCase()
                .chars()
                .mapToObj(c -> (char)c)
                .filter(c -> "aeiou".indexOf(c) != -1)
                .collect(Collectors.toList());

        System.out.println(vowels);
    }
}
