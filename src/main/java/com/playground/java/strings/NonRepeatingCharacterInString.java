package com.example.javaeight.strings;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NonRepeatingCharacterInString {
    public static void main(String[] args) {
        String str = "swiss";

        List resultSorted =str.chars().mapToObj(c -> (char)c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(1).collect(Collectors.toList());

        System.out.println(resultSorted);

        char result = firstNonRepeatingCharacter(str);
        if (result != '\0') {
            System.out.println("First non-repeating character: " + result);
        } else {
            System.out.println("All characters are repeating.");
        }
    }

    public static char firstNonRepeatingCharacter(String str) {
        int[] charCount = new int[256]; // Assuming ASCII character set

        // Count occurrences of each character
        for (char c : str.toCharArray()) {
            charCount[c]++;
        }

        // Find the first character with a count of 1
        for (char c : str.toCharArray()) {
            if (charCount[c] == 1) {
                return c;
            }
        }

        return '\0'; // Return null character if all characters are repeating
    }
}
