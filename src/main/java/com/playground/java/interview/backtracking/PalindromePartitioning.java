package com.playground.java.interview.backtracking;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Backtracking / String Partitioning
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a string, partition it into all possible sequences of substrings
 * where every substring in the sequence is a palindrome.
 */
public class PalindromePartitioning {

    // ================= PROBLEM =================
    // You are given a string. Split it into one or more substrings such that EVERY substring in
    // the split is itself a palindrome. Return all possible such partitions.
    // Example: s = "aab" -> output = [["a","a","b"], ["aa","b"]]
    // (both partitions consist entirely of palindromic pieces: "a","a","b" are each palindromes,
    // and "aa","b" are each palindromes).
    //
    // ================= SIMPLE APPROACH =================
    // (This problem is inherently a "generate all valid combinations" problem - backtracking IS
    // the natural approach; there isn't a meaningfully simpler brute force below it other than
    // generating every possible way to place partition boundaries (2^(n-1) ways) and checking
    // if each results in an all-palindrome partition after the fact.)
    //
    // ================= OPTIMIZED APPROACH =================
    // Backtracking, trying every prefix that is itself a palindrome:
    // 1) Starting at some index `start` in the string, try every possible end position for the
    //    "next piece": for each candidate end position, check if s[start..end] is a palindrome.
    // 2) If it is, add it to the current partition path, and recursively continue partitioning
    //    the REST of the string (from `end` onward).
    // 3) If the recursion reaches the end of the string, the current path is a complete valid
    //    partition - add a copy of it to the results.
    // 4) Backtrack: remove the last piece added before trying the next candidate end position,
    //    so the same path list can be reused for exploring other partition options.
    // This naturally prunes: as soon as a candidate piece is NOT a palindrome, that entire
    // branch is skipped immediately (never explored further), avoiding wasted work on partitions
    // that could never be valid.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Backtracking with a shared mutable "current path" list (add before recursing, remove
    // after) is the standard technique for "generate all valid combinatorial sequences" - it
    // avoids the overhead of copying the path at every recursive step, only copying it once,
    // when a complete valid partition is found. A simple isPalindrome() helper (two-pointer
    // check) is enough to test each candidate substring in O(length) time.
    //
    // ================= EDGE CASES =================
    // - Single character string: it's always its own palindrome partition (one way: itself).
    // - Empty string: conventionally treated as having one valid (empty) partition.
    // - String that is entirely one repeated character (e.g. "aaaa"): produces MANY valid
    //   partitions (every possible way of grouping consecutive same characters is a palindrome).
    // - String with no smaller palindromic pieces than itself (e.g. "abc" with no repeats):
    //   only one valid partition exists - each character on its own.
    // - String that is itself a full palindrome (e.g. "aba"): the whole string as one big piece
    //   is itself one of the valid partitions, in addition to smaller splits.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * 2^n) in the worst case - there can be up to 2^(n-1) different ways
    // to partition a string of length n, and each palindrome check costs up to O(n); worst case
    // is dominated by strings like all-same-character strings that produce exponentially many
    // valid partitions.
    // Space Complexity: O(n) for the recursion depth and current path (excluding the space
    // needed to store all output partitions, which itself can be exponential in the worst case).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you optimize repeated palindrome checks using a precomputed DP table (isPalindrome[i][j]) instead of checking each substring fresh every time?
    // - How would you find just the MINIMUM number of cuts needed to partition the string into palindromes (a different, DP-based problem: Palindrome Partitioning II)?
    // - How would memoization help here, if at all, given that the "current path" makes each state somewhat unique?
    // - How would you modify this to return only the partition with the fewest pieces, or the most pieces?
    // - What's the relationship between this problem and general string segmentation problems like Word Break?
    // - How would you adapt this for very long strings where the exponential blow-up in output size becomes impractical (e.g. only count partitions rather than enumerate them)?

    // Optimized: backtracking, trying every palindromic prefix at each step.
    public static List<List<String>> partition(String s) {
        List<List<String>> result = new ArrayList<>();
        backtrack(s, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(String s, int start, List<String> currentPath, List<List<String>> result) {
        if (start == s.length()) {
            // Step: reached the end - the current path is a complete valid partition.
            result.add(new ArrayList<>(currentPath));
            return;
        }

        for (int end = start + 1; end <= s.length(); end++) {
            String piece = s.substring(start, end);
            if (!isPalindrome(piece)) {
                continue; // prune: this piece can never be part of a valid partition
            }
            // Step: choose this palindromic piece and recurse on the rest of the string.
            currentPath.add(piece);
            backtrack(s, end, currentPath, result);
            // Step: backtrack - remove the piece before trying the next candidate.
            currentPath.remove(currentPath.size() - 1);
        }
    }

    private static boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        String s1 = "aab";
        // Expected: [["a", "a", "b"], ["aa", "b"]]
        System.out.println("Input: \"aab\"");
        System.out.println("Output: " + partition(s1));

        String s2 = "a";
        // Expected: [["a"]]
        System.out.println("\nInput: \"a\" (single character)");
        System.out.println("Output: " + partition(s2));

        String s3 = "aaaa";
        // Expected: 8 total partitions (all groupings of the repeated 'a's are palindromes)
        System.out.println("\nInput: \"aaaa\" (all repeated characters)");
        List<List<String>> result3 = partition(s3);
        System.out.println("Output count: " + result3.size());
        System.out.println("Output: " + result3);
    }
}
