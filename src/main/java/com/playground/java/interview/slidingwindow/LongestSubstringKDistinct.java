package com.playground.java.interview.slidingwindow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Sliding Window / HashMap
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Find the length of the longest substring that contains at most K
 * distinct characters.
 */
public class LongestSubstringKDistinct {

    // ================= PROBLEM =================
    // You get a string and a number K.
    // You need to find the length of the longest substring that contains at most
    // K different characters.
    // Example: s = "eceba", K = 2 -> output = 3
    // because the substring "ece" has only 2 distinct characters ('e', 'c') and length 3.
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible substring. For each substring, count how many distinct
    // characters it has (using a Set or Map). If it has at most K distinct characters
    // and it's longer than the best found so far, remember it.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // There are O(n^2) possible substrings, and counting distinct characters for
    // each one takes up to O(n) time, giving O(n^3) total time in the worst case -
    // far too slow for longer strings.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a sliding window with two pointers, left and right, and a HashMap that
    // counts how many times each character appears in the current window.
    // Expand the window by moving right forward, adding the new character to the map.
    // Whenever the map has more than K distinct characters, shrink the window from
    // the left: decrement the count of the leftmost character, and if its count
    // drops to zero, remove it from the map entirely, then move left forward.
    // After each expansion (and any needed shrinking), update the best (maximum)
    // window length seen so far.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap<Character, Integer> tracks character frequencies within the current
    // window in O(1) average time per update, and its size directly tells us the
    // number of distinct characters in the window - letting us grow and shrink the
    // window in a single O(n) pass instead of re-scanning substrings repeatedly.
    //
    // ================= EDGE CASES =================
    // - K is 0: no characters allowed, so the answer is 0.
    // - K is greater than or equal to the number of distinct characters in the string: whole string is the answer.
    // - Empty string: answer is 0.
    // - String with all identical characters: answer is the whole string's length regardless of K (as long as K >= 1).
    // - K larger than the string length: still just capped by the string's actual length.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the sliding window approach - both left and right
    // pointers move forward at most n times total across the whole algorithm.
    // Brute force is O(n^3) (or O(n^2) with a smarter running distinct-count).
    // Space Complexity: O(K) for the optimized approach - the HashMap holds at most
    // K+1 distinct characters at any time before shrinking. Brute force uses O(1) to
    // O(n) extra space depending on implementation.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the total movement of left and right pointers stay O(n) across the whole run?
    // - How would you modify this for "exactly K distinct characters" instead of "at most K"?
    // - What is the relationship between this problem and "Longest Substring Without Repeating Characters" (K=distinct count with no repeats)?
    // - How would you solve the "Fruit Into Baskets" problem (K=2 distinct) using this exact same pattern?
    // - What if K could change dynamically while scanning the string?
    // - How would you also return the actual substring, not just its length?
    // - How would you handle Unicode characters or emojis that span multiple chars/bytes?

    // Brute force: check every substring, count distinct characters. O(n^3) in the worst case.
    public static int longestSubstringKDistinctBruteForce(String s, int k) {
        if (s == null || s.isEmpty() || k == 0) {
            return 0;
        }
        int maxLength = 0;
        for (int start = 0; start < s.length(); start++) {
            Map<Character, Integer> freq = new HashMap<>();
            for (int end = start; end < s.length(); end++) {
                freq.put(s.charAt(end), freq.getOrDefault(s.charAt(end), 0) + 1);
                if (freq.size() <= k) {
                    maxLength = Math.max(maxLength, end - start + 1);
                } else {
                    break;
                }
            }
        }
        return maxLength;
    }

    // Optimized: sliding window with a character frequency map. O(n) time.
    public static int longestSubstringKDistinctOptimized(String s, int k) {
        if (s == null || s.isEmpty() || k == 0) {
            return 0;
        }
        Map<Character, Integer> charFrequency = new HashMap<>();
        int left = 0;
        int maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char rightChar = s.charAt(right);
            charFrequency.put(rightChar, charFrequency.getOrDefault(rightChar, 0) + 1);

            // Shrink the window while we have more than k distinct characters.
            while (charFrequency.size() > k) {
                char leftChar = s.charAt(left);
                charFrequency.put(leftChar, charFrequency.get(leftChar) - 1);
                if (charFrequency.get(leftChar) == 0) {
                    charFrequency.remove(leftChar);
                }
                left++;
            }

            // The window [left, right] now has at most k distinct characters.
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }

    public static void main(String[] args) {
        // Expected: 3 ("ece")
        System.out.println("Input: s=\"eceba\", k=2");
        System.out.println("Brute force output: " + longestSubstringKDistinctBruteForce("eceba", 2));
        System.out.println("Optimized output: " + longestSubstringKDistinctOptimized("eceba", 2));

        // Expected: 2 ("aa")
        System.out.println("\nInput: s=\"aa\", k=1");
        System.out.println("Optimized output: " + longestSubstringKDistinctOptimized("aa", 1));

        // Expected: 0 (k=0, no characters allowed)
        System.out.println("\nInput: s=\"abc\", k=0 (edge case)");
        System.out.println("Optimized output: " + longestSubstringKDistinctOptimized("abc", 0));
    }
}
