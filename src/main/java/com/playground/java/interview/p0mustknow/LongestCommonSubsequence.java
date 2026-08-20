package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Dynamic Programming
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given two strings, find the length of their longest common subsequence
 * (characters in the same relative order, not necessarily contiguous).
 */
public class LongestCommonSubsequence {

    // ================= PROBLEM =================
    // You are given two strings. A "subsequence" is a sequence derived by deleting zero
    // or more characters without changing the relative order of the remaining characters.
    // Find the length of the longest subsequence that is common to both strings.
    // Example: text1 = "abcde", text2 = "ace"
    // Output -> 3   (the common subsequence is "ace")
    //
    // ================= SIMPLE APPROACH =================
    // Plain recursive solution: compare characters from the end (or start) of both
    // strings using two indices i (into text1) and j (into text2).
    //   - If text1[i] == text2[j]: this character is part of the LCS, so add 1 and
    //     recurse on the remaining substrings (i+1, j+1).
    //   - If text1[i] != text2[j]: this character can't be matched together, so try
    //     skipping one character from either string and take the best result:
    //     max( lcs(i+1, j), lcs(i, j+1) ).
    // Base case: if either index reaches the end of its string, the LCS of that pair is 0.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The plain recursion branches into two recursive calls whenever characters don't
    // match, and the SAME (i, j) pair can be reached through multiple different paths
    // of skips - classic overlapping subproblems.
    // Example with text1="abc", text2="ac": lcs(0,0) compares 'a' vs 'a' (match) -> lcs(1,1).
    // lcs(1,1) compares 'b' vs 'c' (no match) -> max(lcs(2,1), lcs(1,2)).
    // lcs(2,1) compares 'c' vs 'c' (match) -> lcs(3,2).
    // lcs(1,2) is out of bounds on text2 -> 0.
    // Even in this tiny example, larger strings quickly cause the same (i, j) pair to be
    // recomputed from many different skip paths, giving exponential O(2^(m+n)) time in
    // the worst case, even though there are only m*n distinct (i, j) subproblems total.
    //
    // ================= OPTIMIZED APPROACH =================
    // Recurrence relation (in words): if the current characters of both strings match,
    // the LCS length is 1 plus the LCS of the remaining substrings after both characters.
    // If they don't match, the LCS length is the better of "skip a character from text1"
    // or "skip a character from text2".
    // Formula: dp[i][j] = dp[i-1][j-1] + 1                  if text1[i-1] == text2[j-1]
    //          dp[i][j] = max(dp[i-1][j], dp[i][j-1])       otherwise
    // (using 1-based dp indices where dp[i][j] = LCS length of text1[0..i) and text2[0..j))
    // Base case: dp[0][j] = 0 and dp[i][0] = 0 (an empty string has LCS length 0 with anything).
    //
    // Bottom-up (tabulation): fill a 2D dp table of size (m+1) x (n+1) row by row, using
    // the recurrence above - each cell depends only on cells above, to the left, or
    // diagonally above-left, all of which are already computed.
    // (A top-down memoized version using a HashMap keyed by "i,j" pair or a 2D array cache
    // works identically to the tabulation but is filled lazily via recursion.)
    //
    // BONUS - reconstructing the actual subsequence string: after filling the dp table,
    // walk backward from dp[m][n] to dp[0][0]:
    //   - If text1[i-1] == text2[j-1], that character is part of the LCS - prepend it,
    //     then move diagonally to (i-1, j-1).
    //   - Else, move toward whichever neighbor (dp[i-1][j] or dp[i][j-1]) has the larger
    //     value (that's the direction the optimal solution came from).
    // This traces back the exact choices the recurrence made when it built dp[m][n].
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 2D array is used for the dp table because the subproblems are naturally indexed
    // by two dense, small integer ranges (i from 0..m, j from 0..n) - direct 2D array
    // indexing gives O(1) access, simpler and faster than a HashMap keyed by pairs.
    // (A HashMap<String, Integer> or HashMap<Long, Integer> keyed by an encoded (i,j) pair
    // would work for a top-down memoized version, but is unnecessary overhead here since
    // the key space is small and dense - array indexing is strictly better.)
    //
    // ================= EDGE CASES =================
    // - One or both strings empty: LCS length is 0.
    // - No common characters at all: LCS length is 0.
    // - One string is fully contained as a subsequence of the other: LCS length equals
    //   the length of the shorter string.
    // - Strings with repeated characters (e.g. "aabba" and "ababa"): must be handled
    //   correctly by the standard recurrence - no special casing needed.
    // - Case sensitivity: "ABC" vs "abc" have LCS length 0 unless explicitly normalized.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force is O(2^(m+n)) - exponential - due to overlapping
    // subproblems and branching on mismatches. Tabulated/memoized DP is O(m*n) -
    // polynomial - because there are exactly (m+1)*(n+1) distinct subproblems, each
    // computed in O(1) time using previously computed values.
    // Space Complexity: O(m*n) for the full 2D dp table (needed if we want to reconstruct
    // the actual subsequence string). If we only need the LENGTH (not the subsequence
    // itself), this can be optimized to O(min(m, n)) space, because each row of the dp
    // table only depends on the row directly above it - so we only need to keep two
    // rolling rows (or one row updated carefully) instead of the full table.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you reconstruct the actual LCS string, not just its length?
    // - How would you reduce space to O(min(m,n)) if you only need the length?
    // - How does this relate to the Edit Distance (Levenshtein) problem?
    // - What changes for "Longest Common Substring" (must be contiguous, not just ordered)?
    // - How would you find ALL longest common subsequences, not just one/the length?
    // - Why do we use 1-based indices in the dp table instead of 0-based?
    // - Can this be parallelized or does the diagonal dependency prevent it?

    // Brute force: plain recursion comparing characters, recomputes overlapping (i, j) states.
    public static int lcsBruteForceRecursive(String text1, String text2, int i, int j) {
        if (i == text1.length() || j == text2.length()) {
            return 0; // base case: ran out of characters in either string
        }
        if (text1.charAt(i) == text2.charAt(j)) {
            // Step: characters match - use this character, move both pointers forward.
            return 1 + lcsBruteForceRecursive(text1, text2, i + 1, j + 1);
        }
        // Step: characters don't match - try skipping one character from either string.
        return Math.max(
                lcsBruteForceRecursive(text1, text2, i + 1, j),
                lcsBruteForceRecursive(text1, text2, i, j + 1)
        );
    }

    // Top-down memoization: cache results keyed by an encoded (i, j) pair.
    public static int lcsMemoized(String text1, String text2) {
        Map<Long, Integer> memo = new HashMap<>();
        return lcsMemoizedHelper(text1, text2, 0, 0, memo);
    }

    private static int lcsMemoizedHelper(String text1, String text2, int i, int j, Map<Long, Integer> memo) {
        if (i == text1.length() || j == text2.length()) {
            return 0;
        }
        long key = ((long) i << 32) | j; // encode the (i, j) pair into a single long key
        if (memo.containsKey(key)) {
            return memo.get(key);
        }
        int result;
        if (text1.charAt(i) == text2.charAt(j)) {
            result = 1 + lcsMemoizedHelper(text1, text2, i + 1, j + 1, memo);
        } else {
            result = Math.max(
                    lcsMemoizedHelper(text1, text2, i + 1, j, memo),
                    lcsMemoizedHelper(text1, text2, i, j + 1, memo)
            );
        }
        memo.put(key, result);
        return result;
    }

    // Bottom-up tabulation: build the full 2D dp table.
    public static int lcsTabulation(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1]; // dp[i][j] = LCS length of text1[0..i), text2[0..j)

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1; // characters match - extend the diagonal LCS
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]); // take the better skip
                }
            }
        }
        return dp[m][n];
    }

    // Bonus: reconstruct the actual LCS string by walking back through the dp table.
    public static String reconstructLcs(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        StringBuilder lcs = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                lcs.append(text1.charAt(i - 1)); // this character is part of the LCS
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--; // the answer came from skipping a character in text1
            } else {
                j--; // the answer came from skipping a character in text2
            }
        }
        return lcs.reverse().toString(); // we built it backward, so reverse it
    }

    public static void main(String[] args) {
        String text1a = "abcde";
        String text2a = "ace";
        // Expected: 3  (LCS = "ace")
        System.out.println("Input: text1=\"abcde\", text2=\"ace\"");
        System.out.println("Brute force: " + lcsBruteForceRecursive(text1a, text2a, 0, 0));
        System.out.println("Memoized:    " + lcsMemoized(text1a, text2a));
        System.out.println("Tabulation:  " + lcsTabulation(text1a, text2a));
        System.out.println("Reconstructed subsequence: " + reconstructLcs(text1a, text2a));

        String text1b = "abc";
        String text2b = "def";
        // Expected: 0  (no common characters)
        System.out.println("\nInput: text1=\"abc\", text2=\"def\" (no common chars edge case)");
        System.out.println("Tabulation:  " + lcsTabulation(text1b, text2b));

        String text1c = "";
        String text2c = "xyz";
        // Expected: 0  (empty string edge case)
        System.out.println("\nInput: text1=\"\", text2=\"xyz\" (empty string edge case)");
        System.out.println("Tabulation:  " + lcsTabulation(text1c, text2c));
    }
}
