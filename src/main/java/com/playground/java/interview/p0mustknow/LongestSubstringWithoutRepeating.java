package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Strings / Sliding Window
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Find the length of the longest substring that has no repeating characters.
 */
public class LongestSubstringWithoutRepeating {

    // ================= PROBLEM =================
    // You get a string. You need to find the longest continuous piece (substring) of it
    // where no character repeats, and return the length of that piece.
    // Example: s = "abcabcbb" -> output = 3
    // because the longest substring without repeats is "abc", which has length 3.
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible substring (every start and every end position).
    // For each substring, check character by character whether any character repeats.
    // Keep track of the longest substring found that has no repeats.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Trying every substring is O(n^2) combinations, and checking each one for repeats
    // can take another O(n), giving O(n^3) in the worst case (or O(n^2) with a smarter check).
    // This is far too slow for long strings.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a "sliding window": two pointers, left and right, marking the current substring
    // being considered, which starts empty and grows from the right.
    // Move the right pointer forward one character at a time, adding characters to the window.
    // Keep a map of each character to the last index where it was seen.
    // If the character at the right pointer has been seen before AND that previous occurrence
    // is inside the current window, move the left pointer forward to just past that previous
    // occurrence (this removes the duplicate from the window).
    // After adjusting, calculate the current window length and update the maximum length seen.
    // This way, each character is visited a small, bounded number of times overall.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap (character -> last seen index) gives O(1) lookup to instantly know if a
    // character is already in the current window and where it was last seen.
    // This lets us jump the left pointer directly to the right spot instead of moving it
    // one step at a time and rechecking, which is what makes the sliding window O(n) instead of O(n^2).
    //
    // ================= EDGE CASES =================
    // - Empty string: longest substring length is 0.
    // - String with all identical characters, e.g. "bbbb": longest substring length is 1.
    // - String with all unique characters: the whole string is the answer.
    // - Single character string: length is 1.
    // - Repeated character appears before the current window (already slid past it): should not
    //   incorrectly shrink the window - must check that the previous index is >= left pointer.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - the right pointer moves forward n times, and the left pointer
    // also only moves forward, never backward, so total pointer movement is bounded by O(n).
    // Brute force is O(n^2) to O(n^3) depending on implementation.
    // Space Complexity: O(min(n, m)) where m is the size of the character set (e.g. 128 for ASCII) -
    // the map holds at most one entry per unique character type.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you also return the actual longest substring, not just its length?
    // - How would this change if you needed the longest substring with AT MOST K repeating characters allowed?
    // - What if the character set is Unicode instead of ASCII - does your solution still work efficiently?
    // - Why do we check that the previous index is >= left pointer before moving the window forward?
    // - How would you solve this using a fixed-size array instead of a HashMap, for performance?
    // - What if the string is a live stream and you need the answer to update as characters arrive?
    // - Can you extend this to find the longest substring with at most two distinct characters?

    // Brute force: check every substring for repeated characters.
    public static int lengthOfLongestSubstringBruteForce(String s) {
        int n = s.length();
        int maxLength = 0;
        for (int start = 0; start < n; start++) {
            java.util.Set<Character> seen = new java.util.HashSet<>();
            for (int end = start; end < n; end++) {
                char c = s.charAt(end);
                if (seen.contains(c)) {
                    break; // repeat found, stop extending this substring.
                }
                seen.add(c);
                maxLength = Math.max(maxLength, end - start + 1);
            }
        }
        return maxLength;
    }

    // Optimized: sliding window with a map of character -> last seen index.
    public static int lengthOfLongestSubstringOptimized(String s) {
        Map<Character, Integer> lastSeenIndex = new HashMap<>();
        int maxLength = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // If this character was seen before AND it's inside the current window,
            // shrink the window by moving left just past that previous occurrence.
            if (lastSeenIndex.containsKey(c) && lastSeenIndex.get(c) >= left) {
                left = lastSeenIndex.get(c) + 1;
            }
            lastSeenIndex.put(c, right);
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

    public static void main(String[] args) {
        String s1 = "abcabcbb";
        // Expected: 3 ("abc")
        System.out.println("Input: \"abcabcbb\"");
        System.out.println("Brute force output: " + lengthOfLongestSubstringBruteForce(s1));
        System.out.println("Optimized output: " + lengthOfLongestSubstringOptimized(s1));

        String s2 = "bbbbb";
        // Expected: 1 ("b")
        System.out.println("\nInput: \"bbbbb\" (all same character)");
        System.out.println("Optimized output: " + lengthOfLongestSubstringOptimized(s2));

        String s3 = "";
        // Expected: 0 (empty string)
        System.out.println("\nInput: \"\" (empty string)");
        System.out.println("Optimized output: " + lengthOfLongestSubstringOptimized(s3));

        String s4 = "pwwkew";
        // Expected: 3 ("wke")
        System.out.println("\nInput: \"pwwkew\"");
        System.out.println("Optimized output: " + lengthOfLongestSubstringOptimized(s4));
    }
}
