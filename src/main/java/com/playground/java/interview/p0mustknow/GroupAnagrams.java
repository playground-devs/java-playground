package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PATTERN: Strings / HashMap
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Group a list of strings so that all anagrams of each other end up
 * in the same group.
 */
public class GroupAnagrams {

    // ================= PROBLEM =================
    // You get a list of words. Some words are "anagrams" of each other, meaning they
    // contain exactly the same letters, just in a different order (e.g. "eat" and "tea").
    // You need to group all such words together.
    // Example: strs = ["eat", "tea", "tan", "ate", "nat", "bat"]
    // -> output = [["eat","tea","ate"], ["tan","nat"], ["bat"]]
    // (order of groups and order within groups can vary)
    //
    // ================= SIMPLE APPROACH =================
    // For each word, compare it against every other word to check if they are anagrams
    // (e.g. by sorting both words and checking if the sorted versions are equal).
    // Build groups manually by checking each word against already-formed groups.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Comparing every word against every other word (or every existing group) is O(n^2)
    // comparisons, and each comparison might involve sorting a word, adding more cost.
    // This does not scale well when there are many words.
    //
    // ================= OPTIMIZED APPROACH =================
    // The key idea: anagrams share the same "signature" - if you sort the letters of an
    // anagram, you always get the same result. For example, "eat", "tea", "ate" all become "aet"
    // when sorted.
    // So, for each word, compute this sorted-letters signature, and use it as a key in a map.
    // All words that produce the same signature belong to the same group - so we just append
    // each word to the list stored under its signature key.
    // At the end, the map's values are exactly the groups we need.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap (signature -> list of words) lets us decide which group a word belongs to
    // in O(1) average time (just look up its signature), instead of comparing it against
    // every existing group one by one.
    // This turns an O(n^2) grouping problem into roughly O(n * k log k), where k is average word length.
    //
    // ================= EDGE CASES =================
    // - Empty list of words: return an empty list of groups.
    // - Empty string "" in the list: it is its own group (signature is also "").
    // - All words are identical: they all belong to the same single group.
    // - No two words are anagrams: every word is its own group.
    // - Words with different letter cases (e.g. "Eat" vs "eat"): decide if grouping is case-sensitive
    //   (typically yes, treated as different unless stated otherwise).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * k log k) - for n words, each of average length k, we sort each
    // word's characters (O(k log k)) to build its signature. Brute force is O(n^2 * k log k) or worse.
    // Space Complexity: O(n * k) - we store every word (and its signature) in the map.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you compute the anagram signature without sorting, to avoid the log k factor (e.g. character counts)?
    // - How would you handle Unicode characters instead of just lowercase English letters?
    // - What if the word list is huge and streamed - can you group anagrams incrementally?
    // - How would you make the grouping case-insensitive or ignore punctuation/spaces?
    // - What if you need to preserve the original input order of the groups (first-seen order)?
    // - How would you scale this across multiple machines (distributed grouping) for a massive dataset?
    // - Can you use a character-count array (size 26) as the key instead of a sorted string - what are the trade-offs?

    // Optimized: HashMap keyed by sorted-character signature of each word.
    public static List<List<String>> groupAnagramsOptimized(String[] strs) {
        Map<String, List<String>> signatureToGroup = new HashMap<>();

        for (String word : strs) {
            // Build the signature by sorting the word's characters.
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            String signature = new String(chars);

            // Add this word to the group matching its signature (create the group if new).
            signatureToGroup.computeIfAbsent(signature, key -> new ArrayList<>()).add(word);
        }

        return new ArrayList<>(signatureToGroup.values());
    }

    public static void main(String[] args) {
        String[] strs1 = {"eat", "tea", "tan", "ate", "nat", "bat"};
        // Expected (groups, any order): [["eat","tea","ate"], ["tan","nat"], ["bat"]]
        System.out.println("Input: [\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]");
        System.out.println("Output: " + groupAnagramsOptimized(strs1));

        String[] strs2 = {""};
        // Expected: [[""]]
        System.out.println("\nInput: [\"\"] (single empty string)");
        System.out.println("Output: " + groupAnagramsOptimized(strs2));

        String[] strs3 = {"a"};
        // Expected: [["a"]]
        System.out.println("\nInput: [\"a\"] (single character, no anagrams)");
        System.out.println("Output: " + groupAnagramsOptimized(strs3));
    }
}
