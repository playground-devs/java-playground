package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 2D String DP (Levenshtein Distance)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given two strings, find the minimum number of insert/delete/replace
 * operations required to convert the first string into the second.
 */
public class EditDistance {

    // ================= PROBLEM =================
    // Given two words, word1 and word2, find the minimum number of operations required to
    // convert word1 into word2. You may insert a character, delete a character, or replace a
    // character (each operation counts as one step).
    // Example: word1 = "horse", word2 = "ros" -> output = 3
    //          ("horse" -> "rorse" [replace 'h' with 'r'] -> "rose" [delete 'r'] ->
    //           "ros" [delete 'e'])
    // Example: word1 = "intention", word2 = "execution" -> output = 5
    //
    // ================= SIMPLE APPROACH =================
    // Recursively compare characters from the end (or start) of both words. If the current
    // characters match, move both pointers inward for free. If they don't match, try all three
    // operations (insert, delete, replace) and recurse into the resulting smaller subproblem for
    // each, taking the minimum plus 1 for the operation performed.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The same "convert the remaining suffix/prefix of word1 into the remaining suffix/prefix of
    // word2" subproblem is recomputed many times across different branches of the recursion,
    // leading to exponential time in the worst case without memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // Bottom-up 2D DP: let dp[i][j] = minimum operations to convert the first i characters of
    // word1 into the first j characters of word2.
    // - dp[0][j] = j (convert empty word1 into first j chars of word2: j insertions).
    // - dp[i][0] = i (convert first i chars of word1 into empty word2: i deletions).
    // - For i, j >= 1:
    //   - If word1[i-1] == word2[j-1]: dp[i][j] = dp[i-1][j-1] (characters already match, no
    //     operation needed, carry over the diagonal value).
    //   - Else: dp[i][j] = 1 + min(dp[i-1][j],   // delete word1[i-1]
    //                              dp[i][j-1],   // insert word2[j-1]
    //                              dp[i-1][j-1]) // replace word1[i-1] with word2[j-1]
    // The final answer is dp[word1.length()][word2.length()].
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 2D table indexed by (prefix length of word1, prefix length of word2) is the natural fit
    // because each subproblem is fully described by exactly those two lengths, and the three
    // possible operations map directly to the three neighboring cells (top, left, diagonal) -
    // no more complex structure like a graph or heap is needed to capture these dependencies.
    //
    // ================= EDGE CASES =================
    // - One or both strings empty: answer is simply the length of the non-empty string (all
    //   insertions or all deletions).
    // - Identical strings: answer is 0, no operations needed.
    // - Completely disjoint characters (no character in common): answer is at least
    //   max(len1, len2) - min(len1, len2) plus replacements for the overlapping length.
    // - One string is a prefix of the other (e.g. "abc" and "ab"): answer equals the length
    //   difference (pure insertions or deletions, no replacements needed).
    // - Strings differing by a single character in the middle: answer is 1 (a single replace).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m * n) where m, n are the lengths of word1 and word2 - one DP table
    // cell computed in O(1) each, versus exponential time for the naive recursion.
    // Space Complexity: O(m * n) for the 2D DP table (can be optimized to O(min(m, n)) using a
    // rolling 1D array, since each row only depends on the row above it).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you optimize the O(m*n) space DP down to O(min(m,n)) using a rolling 1D array?
    // - How would you reconstruct the actual sequence of edit operations, not just the count?
    // - How does Edit Distance relate to the Longest Common Subsequence problem?
    // - How would you modify this if insert/delete/replace had different costs instead of all costing 1?
    // - How would you extend this to allow a fourth operation, "swap two adjacent characters" (Damerau-Levenshtein distance)?
    // - What's the time/space complexity impact if one of the strings is extremely long compared to the other?

    // Brute force: plain recursion trying insert/delete/replace, exponential time.
    public static int minDistanceBruteForce(String word1, String word2) {
        return solve(word1, word2, word1.length(), word2.length());
    }

    private static int solve(String word1, String word2, int i, int j) {
        if (i == 0) {
            return j; // word1 exhausted, insert all remaining chars of word2
        }
        if (j == 0) {
            return i; // word2 exhausted, delete all remaining chars of word1
        }

        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            // Step: characters match, no operation needed here, move both pointers inward.
            return solve(word1, word2, i - 1, j - 1);
        }

        // Step: characters differ, try delete / insert / replace, take the cheapest + 1.
        int delete = solve(word1, word2, i - 1, j);
        int insert = solve(word1, word2, i, j - 1);
        int replace = solve(word1, word2, i - 1, j - 1);

        return 1 + Math.min(delete, Math.min(insert, replace));
    }

    // Optimized: bottom-up 2D DP table.
    public static int minDistanceOptimized(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        // Step: base cases - converting to/from an empty string.
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    // Step: matching characters carry over the diagonal value for free.
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Step: take the cheapest of delete, insert, replace, plus this operation.
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        String word1a = "horse";
        String word2a = "ros";
        // Expected: 3
        System.out.println("Input: word1=\"horse\", word2=\"ros\"");
        System.out.println("Output: " + minDistanceOptimized(word1a, word2a));

        String word1b = "intention";
        String word2b = "execution";
        // Expected: 5
        System.out.println("\nInput: word1=\"intention\", word2=\"execution\"");
        System.out.println("Output: " + minDistanceOptimized(word1b, word2b));

        String word1c = "";
        String word2c = "abc";
        // Expected: 3 (insert all three characters)
        System.out.println("\nInput: word1=\"\" (empty), word2=\"abc\"");
        System.out.println("Output: " + minDistanceOptimized(word1c, word2c));
    }
}
