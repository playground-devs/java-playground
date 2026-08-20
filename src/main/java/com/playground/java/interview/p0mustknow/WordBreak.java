package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PATTERN: Dynamic Programming
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given a string and a dictionary of words, determine if the string can be
 * segmented into a space-separated sequence of one or more dictionary words.
 */
public class WordBreak {

    // ================= PROBLEM =================
    // You are given a string s and a dictionary of words (wordDict).
    // Determine whether s can be split into a sequence of dictionary words (words can be
    // reused any number of times).
    // Example: s = "leetcode", wordDict = ["leet", "code"]
    // Output -> true   (because "leetcode" = "leet" + "code")
    // Example: s = "catsandog", wordDict = ["cats", "dog", "sand", "and", "cat"]
    // Output -> false  (no valid segmentation covers the whole string)
    //
    // ================= SIMPLE APPROACH =================
    // Plain recursive solution: try every possible prefix of the remaining string.
    // canBreak(s, startIndex):
    //   - If startIndex == s.length(), we've successfully consumed the whole string - true.
    //   - Otherwise, for every endIndex from startIndex+1 to s.length():
    //       - If s.substring(startIndex, endIndex) is a word in the dictionary,
    //         recursively check canBreak(s, endIndex) - if that succeeds, return true.
    //   - If no prefix leads to a full valid segmentation, return false.
    // This tries every way of cutting the string into dictionary-word pieces.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The same startIndex can be reached through many different paths (different prefix
    // cuts that all land on the same startIndex), causing canBreak(s, startIndex) to be
    // recomputed repeatedly - classic overlapping subproblems.
    // Example: s = "aaaaaaaaaaaaaaaaab", wordDict = ["a", "aa", "aaa", ...] (no "b" alone).
    // canBreak(0) tries cutting off "a" -> canBreak(1), or "aa" -> canBreak(2), or
    // "aaa" -> canBreak(3), etc. Each of these branches independently continues trying
    // to consume more "a"s in every possible grouping, meaning canBreak(5), canBreak(6),
    // etc. all get recomputed from many different starting branches. This produces an
    // exponential number of recursive calls (up to O(2^n) in the worst case) even though
    // there are only n+1 distinct subproblems (startIndex = 0 through n).
    //
    // ================= OPTIMIZED APPROACH =================
    // Recurrence relation (in words): the string s[0..i) can be segmented if there exists
    // some earlier valid cut point j (where s[0..j) is already known to be segmentable)
    // such that the remaining piece s[j..i) is itself a dictionary word.
    // Formula: dp[i] = true if there exists some j < i where dp[j] == true AND
    //                  s.substring(j, i) is in the dictionary.
    // Base case: dp[0] = true (the empty prefix is trivially "segmentable" - zero words used).
    // Final answer: dp[s.length()].
    //
    // Top-down (memoization): same recursive structure as brute force, but cache the
    // boolean result for each startIndex in a HashMap (or boolean[]/Boolean[] array) so
    // each distinct startIndex is only computed once.
    //
    // Bottom-up (tabulation): build dp[] of size s.length()+1 from dp[0] = true upward.
    // For each i from 1 to n, check every earlier j from 0 to i-1: if dp[j] is true and
    // s.substring(j, i) is a dictionary word, set dp[i] = true.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // We use a boolean array (or HashMap<Integer, Boolean>) for dp because the
    // subproblem key (startIndex) is a dense, small integer range (0 .. s.length()) -
    // array indexing gives O(1) access. A HashSet<String> is used for the dictionary
    // itself (not the dp cache) because checking "is this substring a valid word" needs
    // to happen many times (for every possible cut), and a HashSet gives O(1) average
    // lookup by content instead of O(k) linear scanning through a List of k words.
    //
    // ================= EDGE CASES =================
    // - Empty string s: trivially true (zero words needed) - matches dp[0] = true base case.
    // - Empty dictionary with non-empty s: always false (no words available to match anything).
    // - Word in dictionary equals the whole string: true immediately.
    // - Repeated word usage needed (e.g. s = "aaa", wordDict = ["a"]): must allow reusing
    //   the same dictionary word multiple times - the recurrence naturally supports this
    //   since dp[j] doesn't track which words were used, just whether it's reachable.
    // - Dictionary contains words not usable in any valid segmentation: should be ignored
    //   naturally by the algorithm without special handling.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force is exponential, up to O(2^n), due to overlapping
    // startIndex subproblems recomputed across many different recursive paths. Memoized/
    // tabulated DP is O(n^2) (or O(n^2 * average word length) if substring extraction and
    // hashing costs are counted) - polynomial - because there are n+1 distinct
    // subproblems, and each does O(n) work checking every possible earlier cut point j.
    // Space Complexity: O(n) for the dp[] array/memo cache, plus O(n) recursion stack for
    // the top-down version, plus O(sum of word lengths) for the HashSet dictionary. Unlike
    // Climbing Stairs, this cannot generally be reduced below O(n) because dp[i] can
    // depend on ANY earlier dp[j] (not just the previous one or two states).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you also return the actual segmentation (the sequence of words),
    //   not just true/false (this is "Word Break II")?
    // - How would you optimize substring lookups if the dictionary contains very long words?
    // - Could a Trie improve the dictionary lookup step compared to a HashSet?
    // - Why can't we always keep dp as O(1) space here, unlike Fibonacci-style DP?
    // - How would you handle case-insensitivity or extra whitespace in the input string?
    // - What's the worst-case input that maximizes brute-force recursive calls?
    // - How would you extend this to allow a MAXIMUM number of words used in the segmentation?

    // Brute force: plain recursion trying every possible prefix cut, recomputes overlapping states.
    public static boolean wordBreakBruteForceRecursive(String s, List<String> wordDict, int startIndex) {
        if (startIndex == s.length()) {
            return true; // base case: consumed the entire string successfully
        }
        Set<String> dictionary = new HashSet<>(wordDict);
        for (int endIndex = startIndex + 1; endIndex <= s.length(); endIndex++) {
            String prefix = s.substring(startIndex, endIndex);
            // Step: if this prefix is a valid word, try to break the rest of the string.
            if (dictionary.contains(prefix) && wordBreakBruteForceRecursive(s, wordDict, endIndex)) {
                return true;
            }
        }
        return false;
    }

    // Top-down memoization: cache the boolean result for each startIndex.
    public static boolean wordBreakMemoized(String s, List<String> wordDict) {
        Set<String> dictionary = new HashSet<>(wordDict);
        Map<Integer, Boolean> memo = new HashMap<>();
        return wordBreakMemoizedHelper(s, dictionary, 0, memo);
    }

    private static boolean wordBreakMemoizedHelper(String s, Set<String> dictionary,
                                                    int startIndex, Map<Integer, Boolean> memo) {
        if (startIndex == s.length()) {
            return true;
        }
        // Step: return the cached answer if we already solved this startIndex before.
        if (memo.containsKey(startIndex)) {
            return memo.get(startIndex);
        }
        for (int endIndex = startIndex + 1; endIndex <= s.length(); endIndex++) {
            String prefix = s.substring(startIndex, endIndex);
            if (dictionary.contains(prefix) && wordBreakMemoizedHelper(s, dictionary, endIndex, memo)) {
                memo.put(startIndex, true);
                return true;
            }
        }
        memo.put(startIndex, false);
        return false;
    }

    // Bottom-up tabulation: dp[i] = true if s[0..i) can be segmented into dictionary words.
    public static boolean wordBreakTabulation(String s, List<String> wordDict) {
        Set<String> dictionary = new HashSet<>(wordDict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true; // base case: empty prefix needs zero words

        // Step: for every position i, check every earlier valid cut point j.
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && dictionary.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break; // found one valid way to reach i, no need to check other j's
                }
            }
        }
        return dp[n];
    }

    public static void main(String[] args) {
        String s1 = "leetcode";
        List<String> wordDict1 = java.util.Arrays.asList("leet", "code");
        // Expected: true  ("leet" + "code")
        System.out.println("Input: s=\"leetcode\", wordDict=[\"leet\",\"code\"]");
        System.out.println("Brute force: " + wordBreakBruteForceRecursive(s1, wordDict1, 0));
        System.out.println("Memoized:    " + wordBreakMemoized(s1, wordDict1));
        System.out.println("Tabulation:  " + wordBreakTabulation(s1, wordDict1));

        String s2 = "catsandog";
        List<String> wordDict2 = java.util.Arrays.asList("cats", "dog", "sand", "and", "cat");
        // Expected: false  (no valid full segmentation)
        System.out.println("\nInput: s=\"catsandog\", wordDict=[\"cats\",\"dog\",\"sand\",\"and\",\"cat\"]");
        System.out.println("Tabulation:  " + wordBreakTabulation(s2, wordDict2));

        String s3 = "";
        List<String> wordDict3 = java.util.Arrays.asList("a", "b");
        // Expected: true  (empty string edge case - trivially segmentable)
        System.out.println("\nInput: s=\"\" (empty string edge case)");
        System.out.println("Tabulation:  " + wordBreakTabulation(s3, wordDict3));
    }
}
