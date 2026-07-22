package com.example.javaeight.algorithms.strings;

import java.util.HashMap;
import java.util.Map;

public class FindLongestSubString {
    public static void main(String[] args){
        String s = "aabacbebebe";

        // Find longest substring without repeating characters
        String longestSubstring = findLongestSubstringWithoutRepeats(s);
        System.out.println("Longest substring without repeating characters: " + longestSubstring);

        // Find longest substring with at most K distinct characters
        int k = 3; // For example, find substring with at most 3 distinct characters
        String longestKDistinct = findLongestSubstringWithKDistinct(s, k);
        System.out.println("Longest substring with at most " + k + " distinct characters: " + longestKDistinct);

        // Find longest substring with at most 2 distinct characters
        k = 2;
        String longestK2Distinct = findLongestSubstringWithKDistinct(s, k);
        System.out.println("Longest substring with at most " + k + " distinct characters: " + longestK2Distinct);
    }

    // Find longest substring without repeating characters
    public static String findLongestSubstringWithoutRepeats(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }

        Map<Character, Integer> charIndexMap = new HashMap<>();
        int start = 0;
        int maxLength = 0;
        int maxStart = 0;

        for (int end = 0; end < s.length(); end++) {
            char currentChar = s.charAt(end);

            // If we've seen this character before and it's in our current window
            if (charIndexMap.containsKey(currentChar) && charIndexMap.get(currentChar) >= start) {
                // Move start to position after the last occurrence of current character
                start = charIndexMap.get(currentChar) + 1;
            }

            // Update the last position of current character
            charIndexMap.put(currentChar, end);

            // Update max length if current window is larger
            if (end - start + 1 > maxLength) {
                maxLength = end - start + 1;
                maxStart = start;
            }
        }

        return s.substring(maxStart, maxStart + maxLength);
    }

    // Find longest substring with at most K distinct characters
    public static String findLongestSubstringWithKDistinct(String s, int k) {
        if (s == null || s.length() == 0 || k <= 0) {
            return "";
        }

        Map<Character, Integer> charFreqMap = new HashMap<>();
        int start = 0;
        int maxLength = 0;
        int maxStart = 0;

        for (int end = 0; end < s.length(); end++) {
            char currentChar = s.charAt(end);

            // Add current character to frequency map
            charFreqMap.put(currentChar, charFreqMap.getOrDefault(currentChar, 0) + 1);

            // If map size exceeds k, shrink window from left
            while (charFreqMap.size() > k) {
                char leftChar = s.charAt(start);
                charFreqMap.put(leftChar, charFreqMap.get(leftChar) - 1);
                if (charFreqMap.get(leftChar) == 0) {
                    charFreqMap.remove(leftChar);
                }
                start++;
            }

            // Update max length if current window is larger
            if (end - start + 1 > maxLength) {
                maxLength = end - start + 1;
                maxStart = start;
            }
        }

        return s.substring(maxStart, maxStart + maxLength);
    }
}
