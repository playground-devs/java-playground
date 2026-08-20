package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Dynamic Programming
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given n stairs where you can climb 1 or 2 steps at a time, count the
 * number of distinct ways to reach the top.
 */
public class ClimbingStairs {

    // ================= PROBLEM =================
    // You are climbing a staircase with n steps. At each move you can climb either
    // 1 step or 2 steps. Count how many distinct ways you can reach the top.
    // Example: n = 4
    // Ways -> 1+1+1+1, 1+1+2, 1+2+1, 2+1+1, 2+2  => output = 5
    //
    // IMPORTANT INSIGHT: this is literally the Fibonacci sequence in disguise.
    // ways(n) = ways(n-1) + ways(n-2), because the very last move to reach step n was
    // either a 1-step move from step n-1, or a 2-step move from step n-2.
    // With ways(1)=1 and ways(2)=2, this generates 1,2,3,5,8,13,... - shifted Fibonacci.
    //
    // ================= SIMPLE APPROACH =================
    // Plain recursive solution: climbStairs(n) = climbStairs(n-1) + climbStairs(n-2),
    // with base cases climbStairs(1) = 1 and climbStairs(2) = 2 (or climbStairs(0) = 1
    // representing "one way to stand still", depending on how you set up the base case).
    // This mirrors the naive recursive Fibonacci implementation exactly.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The plain recursion recomputes the same subproblems many times - classic
    // overlapping subproblems. Consider climbStairs(5):
    //   climbStairs(5)
    //     -> climbStairs(4) + climbStairs(3)
    //          climbStairs(4) -> climbStairs(3) + climbStairs(2)
    //          climbStairs(3) -> climbStairs(2) + climbStairs(1)
    // Notice climbStairs(3) is computed twice (once inside climbStairs(4)'s subtree, and
    // once directly), and climbStairs(2) is computed three times total. As n grows, the
    // number of redundant calls doubles at every level, giving an exponential O(2^n)
    // recursion tree - exactly like naive fib(n) - even though there are only n distinct
    // subproblems (climbStairs(1) through climbStairs(n)).
    //
    // ================= OPTIMIZED APPROACH =================
    // Recurrence relation (in words): the number of ways to reach step n equals the
    // number of ways to reach step n-1 (then take one final 1-step) PLUS the number of
    // ways to reach step n-2 (then take one final 2-step).
    // Formula: dp[n] = dp[n-1] + dp[n-2], with dp[1] = 1, dp[2] = 2.
    //
    // Top-down (memoization): same recursive structure as the brute force, but we cache
    // each computed dp[n] in a HashMap/array so repeated calls return instantly instead
    // of recomputing.
    //
    // Bottom-up (tabulation): build dp[] iteratively from dp[1] and dp[2] up to dp[n],
    // so every subproblem is computed exactly once, in order, with no recursion overhead.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // We use an array (or HashMap) for memoization because each subproblem climbStairs(k)
    // has a fixed, predictable integer key (k ranges from 0 to n) - an array gives O(1)
    // direct-index access/storage, which is simpler and faster than a HashMap's O(1)
    // average (with hashing overhead) for this dense, small integer key range. A HashMap
    // would still work correctly and is useful when keys are sparse or non-integer, but
    // an array is the natural, more efficient choice here.
    //
    // ================= EDGE CASES =================
    // - n = 0: by convention, 1 way (stand still, do nothing) - define base case carefully.
    // - n = 1: exactly 1 way (a single 1-step).
    // - n = 2: exactly 2 ways (1+1, or 2).
    // - Negative n: invalid input, should not be climbed.
    // - Large n (e.g. n = 45+): values grow past int range quickly - consider long or
    //   overflow-safe arithmetic for very large n.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force is O(2^n) - exponential, because of the repeated
    // recursive calls shown above. Memoized/tabulated DP is O(n) - polynomial (in fact
    // linear) - because each of the n distinct subproblems is computed exactly once.
    // Space Complexity: Memoized version uses O(n) for the cache plus O(n) recursion
    // stack. Tabulation uses O(n) for the dp[] array. This can be optimized to O(1) space
    // because dp[n] only ever depends on the previous two values (dp[n-1] and dp[n-2]) -
    // so we only need to keep two rolling variables instead of the full array, shown in
    // climbStairsSpaceOptimized().
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is this problem "Fibonacci in disguise" - can you prove the recurrence is identical?
    // - How would this change if you could climb 1, 2, or 3 steps at a time?
    // - How would you compute climbStairs(n) for very large n (e.g. n = 10^9) efficiently
    //   (matrix exponentiation for Fibonacci in O(log n))?
    // - Why does the space-optimized version only need two variables instead of a full array?
    // - What's the difference between top-down memoization and bottom-up tabulation in
    //   terms of recursion overhead and stack overflow risk for large n?
    // - How would you modify this if some steps were "broken" (forbidden) stairs?
    // - Could you solve this with matrix exponentiation or a closed-form (Binet's) formula?

    // Brute force: plain recursion, recomputes overlapping subproblems (exponential).
    public static long climbStairsBruteForceRecursive(int n) {
        if (n <= 2) {
            return n; // base cases: 1 way for n=1, 2 ways for n=2 (n=0 handled by caller)
        }
        // Step: last move was either a 1-step (from n-1) or a 2-step (from n-2).
        return climbStairsBruteForceRecursive(n - 1) + climbStairsBruteForceRecursive(n - 2);
    }

    // Top-down memoization: cache results in a HashMap to avoid recomputation.
    public static long climbStairsMemoized(int n) {
        Map<Integer, Long> memo = new HashMap<>();
        return climbStairsMemoizedHelper(n, memo);
    }

    private static long climbStairsMemoizedHelper(int n, Map<Integer, Long> memo) {
        if (n <= 2) {
            return n;
        }
        // Step: return cached answer if we already solved this subproblem before.
        if (memo.containsKey(n)) {
            return memo.get(n);
        }
        long ways = climbStairsMemoizedHelper(n - 1, memo) + climbStairsMemoizedHelper(n - 2, memo);
        memo.put(n, ways); // cache before returning
        return ways;
    }

    // Bottom-up tabulation: build up dp[] from the smallest subproblems.
    public static long climbStairsTabulation(int n) {
        if (n <= 2) {
            return n;
        }
        long[] dp = new long[n + 1];
        dp[1] = 1;
        dp[2] = 2;
        // Step: each dp[i] depends only on the previous two computed values.
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    // Space-optimized: only keep the last two values instead of the whole dp[] array.
    public static long climbStairsSpaceOptimized(int n) {
        if (n <= 2) {
            return n;
        }
        long prevPrev = 1; // represents dp[1]
        long prev = 2;      // represents dp[2]
        long current = 0;
        for (int i = 3; i <= n; i++) {
            current = prev + prevPrev; // step: dp[i] = dp[i-1] + dp[i-2]
            prevPrev = prev;
            prev = current;
        }
        return current;
    }

    public static void main(String[] args) {
        int n1 = 4;
        // Expected: 5
        System.out.println("Input: n=4");
        System.out.println("Brute force:      " + climbStairsBruteForceRecursive(n1));
        System.out.println("Memoized:         " + climbStairsMemoized(n1));
        System.out.println("Tabulation:       " + climbStairsTabulation(n1));
        System.out.println("Space optimized:  " + climbStairsSpaceOptimized(n1));

        int n2 = 1;
        // Expected: 1
        System.out.println("\nInput: n=1 (edge case)");
        System.out.println("Space optimized:  " + climbStairsSpaceOptimized(n2));

        int n3 = 10;
        // Expected: 89
        System.out.println("\nInput: n=10");
        System.out.println("Tabulation:       " + climbStairsTabulation(n3));
        System.out.println("Space optimized:  " + climbStairsSpaceOptimized(n3));
    }
}
