package com.playground.java.interview.strings;

import java.util.Arrays;

/**
 * PATTERN: Strings / Frequency Counting
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Determine whether two strings are anagrams of each other
 * (contain exactly the same characters with the same frequencies).
 */
public class ValidAnagram {

    // ================= PROBLEM =================
    // You get two strings. You need to check if they are anagrams of each other,
    // meaning one can be rearranged to form the other.
    // Example: s = "anagram", t = "nagaram" -> output = true
    // Example: s = "rat", t = "car" -> output = false
    //
    // ================= SIMPLE APPROACH =================
    // Sort the characters of both strings alphabetically.
    // If the two sorted strings are exactly equal, the original strings are anagrams.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting each string takes O(n log n) time. While this works, it does more
    // work than necessary - we don't actually need an ordering, just matching counts.
    //
    // ================= OPTIMIZED APPROACH =================
    // First check if the two strings have different lengths - if so, they cannot
    // be anagrams, so return false immediately.
    // Use a fixed-size array of 26 counters (for lowercase English letters).
    // Walk through the first string and increment the counter for each character.
    // Walk through the second string and decrement the counter for each character.
    // If the strings are anagrams, every counter should end up back at zero.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A fixed-size int array indexed by "character - 'a'" gives O(1) increment and
    // decrement per character, avoiding both the sorting cost and the overhead of a
    // general-purpose HashMap (no hashing, no boxing, just direct array indexing).
    //
    // ================= EDGE CASES =================
    // - Strings of different lengths: immediately not anagrams.
    // - Empty strings: two empty strings are anagrams of each other (trivially true).
    // - Strings with repeated characters: counts must match exactly, not just presence.
    // - Case sensitivity: decide whether "Listen" and "Silent" should match (this
    //   implementation is case-sensitive and assumes lowercase input).
    // - Strings with non-alphabetic characters: this simple 26-array version assumes
    //   lowercase a-z only; would need a HashMap for full Unicode support.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized frequency-count approach - two linear
    // passes over strings of length n. Brute force sorting is O(n log n).
    // Space Complexity: O(1) for the optimized approach - a fixed 26-element array
    // regardless of input size. Brute force sorting also uses O(n) space for the
    // character arrays created during sorting.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you handle Unicode characters instead of just lowercase a-z?
    // - Would you use a HashMap<Character, Integer> instead of an int[26] for general input, and why?
    // - How would you check if one string is an anagram of any substring of another (permutation in string)?
    // - What if you needed to group a list of strings into anagram groups efficiently?
    // - How would case sensitivity or whitespace affect your solution, and how would you normalize input?
    // - Can you solve this using a single pass instead of two separate passes?
    // - What if the strings were extremely large and you wanted to short-circuit as early as possible?

    // Brute force: sort both strings and compare. O(n log n).
    public static boolean isAnagramBruteForce(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        char[] sChars = s.toCharArray();
        char[] tChars = t.toCharArray();
        Arrays.sort(sChars);
        Arrays.sort(tChars);
        return Arrays.equals(sChars, tChars);
    }

    // Optimized: single frequency array of size 26. O(n).
    public static boolean isAnagramOptimized(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        int[] counts = new int[26];
        for (int i = 0; i < s.length(); i++) {
            // Increment for characters seen in s.
            counts[s.charAt(i) - 'a']++;
            // Decrement for characters seen in t.
            counts[t.charAt(i) - 'a']--;
        }
        // If truly anagrams, every counter must be back to zero.
        for (int count : counts) {
            if (count != 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        // Expected: true
        System.out.println("Input: s=\"anagram\", t=\"nagaram\"");
        System.out.println("Brute force output: " + isAnagramBruteForce("anagram", "nagaram"));
        System.out.println("Optimized output: " + isAnagramOptimized("anagram", "nagaram"));

        // Expected: false
        System.out.println("\nInput: s=\"rat\", t=\"car\"");
        System.out.println("Optimized output: " + isAnagramOptimized("rat", "car"));

        // Expected: true (both empty strings)
        System.out.println("\nInput: s=\"\", t=\"\" (empty strings)");
        System.out.println("Optimized output: " + isAnagramOptimized("", ""));
    }
}
