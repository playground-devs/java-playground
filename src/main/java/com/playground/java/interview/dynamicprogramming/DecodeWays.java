package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 1D DP (Fibonacci-style)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a string of digits, where 'A'=1 ... 'Z'=26, count the number of
 * distinct ways it can be decoded into letters.
 */
public class DecodeWays {

    // ================= PROBLEM =================
    // You have a string of digits. Each digit or pair of digits maps to a letter: '1'->'A' ...
    // '26'->'Z'. Count how many different ways the string can be decoded into a sequence of
    // letters.
    // Example: s = "226" -> output = 3, because it can decode as "2 2 6" -> "BBF",
    //          "22 6" -> "VF", or "2 26" -> "BZ".
    // Example: s = "06" -> output = 0, because a leading zero digit has no valid single-digit
    //          mapping and "06" is not a valid two-digit code either (must be 10-26).
    //
    // ================= SIMPLE APPROACH =================
    // Use recursion: at each position, try decoding just the next 1 digit (if it's not '0'),
    // and try decoding the next 2 digits (if that two-digit number is between 10 and 26), and
    // recursively count the ways to decode the rest of the string from there. Sum both branches.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This plain recursion re-solves the same subproblems (decoding "the rest of the string
    // starting at index i") many times through different paths, leading to exponential O(2^n)
    // time in the worst case without memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // Bottom-up DP: let dp[i] = number of ways to decode the first i characters of the string.
    // - dp[0] = 1 (empty prefix has exactly one way to decode: do nothing).
    // - dp[1] = 1 if s[0] != '0', else 0 (a single leading character must be a valid digit 1-9).
    // - For i from 2 to n:
    //   - If s[i-1] (the current single digit) is not '0', it can stand alone as a valid
    //     letter, so add dp[i-1] (all the ways to decode everything before it).
    //   - If the two-digit number formed by s[i-2..i-1] is between 10 and 26 (inclusive), that
    //     pair can be treated as one letter, so add dp[i-2] (all the ways to decode everything
    //     before that pair).
    //   - dp[i] = (contribution from 1-digit case) + (contribution from 2-digit case).
    // The final answer is dp[n].
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A simple 1D array (or even just two rolling variables, since dp[i] only depends on dp[i-1]
    // and dp[i-2]) is enough - this is structurally similar to the Fibonacci/Climbing Stairs
    // recurrence, where each position's answer is built from at most the previous two answers,
    // no need for any more complex structure like a graph or heap.
    //
    // ================= EDGE CASES =================
    // - String starts with '0' (e.g. "06"): dp[1] = 0 immediately, since '0' has no valid
    //   single-digit letter mapping, and this correctly cascades to 0 total ways.
    // - A '0' appears in the middle NOT preceded by a valid '1' or '2' (e.g. "100"): must be
    //   captured as 0 ways from that point on, since '0' alone is invalid and no valid 2-digit
    //   pairing rescues it either.
    // - Two-digit numbers above 26 (e.g. "27"): only the 1-digit interpretation is valid, the
    //   2-digit contribution is skipped for that pair.
    // - Empty string: conventionally treated as 1 way (the empty decoding) - matches dp[0] = 1.
    // - String of all the same repeating valid pair digit (e.g. "111111"): grows like a
    //   Fibonacci sequence in ways count, testing the DP recurrence's compounding correctly.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - one pass building the DP array left to right, each entry computed
    // in O(1) from the previous two entries; versus O(2^n) for the naive recursion.
    // Space Complexity: O(n) for the DP array (can be optimized to O(1) using two rolling
    // variables instead of a full array, since only the last two values are ever needed).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you optimize the O(n) space DP down to O(1) using two rolling variables instead of a full array?
    // - How would you extend this if '*' can represent "any digit 1-9" (a harder LeetCode variant, Decode Ways II)?
    // - How would you modify the DP to also return one example valid decoding, not just the count?
    // - Why is checking "is s[i-1] != '0'" not enough by itself - why do we also need the 2-digit check in the 10-26 range specifically?
    // - How does this recurrence relate structurally to Climbing Stairs or Fibonacci?
    // - What's the earliest point at which you can short-circuit and return 0 if the string is clearly invalid?

    // Brute force: plain recursion trying 1-digit and 2-digit decodes at each step.
    public static int numDecodingsBruteForce(String s) {
        return decodeFrom(s, 0);
    }

    private static int decodeFrom(String s, int index) {
        if (index == s.length()) {
            return 1; // reached the end cleanly - one valid decoding path
        }
        if (s.charAt(index) == '0') {
            return 0; // '0' cannot stand alone
        }

        // Step: try decoding just the next 1 digit.
        int ways = decodeFrom(s, index + 1);

        // Step: try decoding the next 2 digits, if valid (10-26) and in range.
        if (index + 1 < s.length()) {
            int twoDigit = Integer.parseInt(s.substring(index, index + 2));
            if (twoDigit >= 10 && twoDigit <= 26) {
                ways += decodeFrom(s, index + 2);
            }
        }

        return ways;
    }

    // Optimized: bottom-up 1D DP, dp[i] built from dp[i-1] and dp[i-2].
    public static int numDecodingsOptimized(String s) {
        int n = s.length();
        if (n == 0) {
            return 1;
        }

        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = s.charAt(0) != '0' ? 1 : 0;

        for (int i = 2; i <= n; i++) {
            // Step: single-digit decode of s[i-1] contributes dp[i-1] if it's a valid digit (1-9).
            int oneDigit = s.charAt(i - 1) - '0';
            if (oneDigit >= 1 && oneDigit <= 9) {
                dp[i] += dp[i - 1];
            }

            // Step: two-digit decode of s[i-2..i-1] contributes dp[i-2] if it's in range 10-26.
            int twoDigit = Integer.parseInt(s.substring(i - 2, i));
            if (twoDigit >= 10 && twoDigit <= 26) {
                dp[i] += dp[i - 2];
            }
        }

        return dp[n];
    }

    public static void main(String[] args) {
        String s1 = "226";
        // Expected: 3 ("2 2 6", "22 6", "2 26")
        System.out.println("Input: \"226\"");
        System.out.println("Output: " + numDecodingsOptimized(s1));

        String s2 = "06";
        // Expected: 0 (leading zero, invalid)
        System.out.println("\nInput: \"06\"");
        System.out.println("Output: " + numDecodingsOptimized(s2));

        String s3 = "10";
        // Expected: 1 (only "10" -> "J")
        System.out.println("\nInput: \"10\"");
        System.out.println("Output: " + numDecodingsOptimized(s3));
    }
}
