package com.playground.java.interview.strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PATTERN: Strings / In-place Reversal
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Reverse the order of words in a sentence, trimming any extra
 * leading, trailing, or duplicate spaces between words.
 */
public class ReverseWordsInString {

    // ================= PROBLEM =================
    // You get a sentence (a string) that may have leading/trailing spaces and
    // multiple spaces between words. You need to reverse the order of the words,
    // with exactly one space between each word in the output, and no leading/trailing spaces.
    // Example: s = "  the sky   is blue  " -> output = "blue is sky the"
    //
    // ================= SIMPLE APPROACH =================
    // Split the string on whitespace to get a list of words (this naturally skips
    // empty tokens caused by multiple spaces if done carefully, e.g. with trim + split).
    // Reverse the order of the list.
    // Join the words back together with a single space between each.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This approach is actually fine in terms of time complexity (O(n)), but it
    // relies on extra data structures (a list and a new string builder for joining)
    // and doesn't show the in-place character manipulation technique that interviewers
    // often want to see for this specific problem (it's really testing string/array
    // reversal skills, not just library usage).
    //
    // ================= OPTIMIZED APPROACH =================
    // Convert the string to a character array.
    // Step 1: Reverse the entire character array.
    // Step 2: Reverse each individual word within the reversed array back to normal
    // (this restores the correct spelling of each word while keeping their new order).
    // Step 3: Clean up spaces - remove leading/trailing spaces and collapse multiple
    // spaces between words into exactly one, while building the final result.
    // This "reverse everything, then reverse each word" trick is a classic pattern
    // that works because reversing twice at different granularities re-orders words
    // while keeping letters within each word correct.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A char array allows direct in-place index manipulation for both the full
    // reversal and the per-word reversal, avoiding the overhead of creating many
    // intermediate String or List objects the way the split-and-join approach does.
    //
    // ================= EDGE CASES =================
    // - Leading and/or trailing spaces: must be trimmed from the final output.
    // - Multiple consecutive spaces between words: must collapse to a single space.
    // - Single word with no spaces: output is just that word.
    // - String that is all spaces or empty: output should be an empty string.
    // - Words with punctuation: treated as part of the word, only whitespace separates words.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - each character/word is processed
    // a constant number of times. The optimized version avoids extra allocations
    // but big-O time is the same.
    // Space Complexity: O(n) for the output in both cases (a new string must be
    // produced), but the optimized approach avoids the intermediate List<String>
    // and multiple string splits that the brute force approach creates.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does reversing the whole array first, then reversing each word, correctly reorder words?
    // - How would you do this truly in-place if the language allowed mutable strings (like a char[] with a known final length)?
    // - What if there could be other whitespace types (tabs, newlines) mixed in?
    // - How would you reverse only the characters of each word without reversing the order of the words?
    // - What if the input was extremely large and streamed word by word instead of being fully in memory?
    // - Could you solve this with a Deque instead of split+reverse+join? What are the trade-offs?
    // - How would you extend this to reverse words but keep punctuation attached correctly?

    // Brute force: split into words, reverse the list, join with single spaces. O(n) time, O(n) space.
    public static String reverseWordsBruteForce(String s) {
        if (s == null) {
            return "";
        }
        // Splitting on one-or-more whitespace after trimming removes empty tokens.
        String[] tokens = s.trim().split("\\s+");
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }
        Collections.reverse(words);
        return String.join(" ", words);
    }

    // Optimized: reverse whole char array, then reverse each word, then clean up spacing.
    public static String reverseWordsOptimized(String s) {
        if (s == null) {
            return "";
        }
        char[] chars = s.toCharArray();
        int n = chars.length;

        // Step 1: reverse the entire array.
        reverse(chars, 0, n - 1);

        // Step 2: reverse each word back to correct spelling, and step 3: build clean output.
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < n) {
            // Skip spaces between words.
            if (chars[i] == ' ') {
                i++;
                continue;
            }
            int wordStart = i;
            while (i < n && chars[i] != ' ') {
                i++;
            }
            // Reverse this word's characters back to correct order.
            reverse(chars, wordStart, i - 1);
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(chars, wordStart, i - wordStart);
        }
        return result.toString();
    }

    private static void reverse(char[] chars, int start, int end) {
        while (start < end) {
            char temp = chars[start];
            chars[start] = chars[end];
            chars[end] = temp;
            start++;
            end--;
        }
    }

    public static void main(String[] args) {
        // Expected: "blue is sky the"
        System.out.println("Input: \"  the sky   is blue  \"");
        System.out.println("Brute force output: \"" + reverseWordsBruteForce("  the sky   is blue  ") + "\"");
        System.out.println("Optimized output: \"" + reverseWordsOptimized("  the sky   is blue  ") + "\"");

        // Expected: "word" (single word)
        System.out.println("\nInput: \"word\" (single word)");
        System.out.println("Optimized output: \"" + reverseWordsOptimized("word") + "\"");

        // Expected: "" (all spaces, edge case)
        System.out.println("\nInput: \"    \" (all spaces)");
        System.out.println("Optimized output: \"" + reverseWordsOptimized("    ") + "\"");
    }
}
