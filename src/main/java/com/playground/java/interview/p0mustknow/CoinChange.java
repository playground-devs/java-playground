package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Dynamic Programming
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given coin denominations and a target amount, find the minimum number
 * of coins needed to make that amount (or -1 if it cannot be made).
 */
public class CoinChange {

    // ================= PROBLEM =================
    // You are given an array of coin denominations and a target amount.
    // You have an unlimited supply of each coin. Find the minimum number of coins needed
    // to make up exactly the target amount. If it's impossible, return -1.
    // Example: coins = [1, 2, 5], amount = 11
    // Output -> 3   (because 11 = 5 + 5 + 1, using 3 coins)
    // Example: coins = [2], amount = 3 -> Output -> -1 (impossible, 3 is odd)
    //
    // ================= SIMPLE APPROACH =================
    // Plain recursive solution: to make "amount", try using each coin denomination as the
    // "last coin used". For each coin c (where c <= amount), recursively solve for
    // amount - c, then add 1 (for the coin we just used). Take the minimum result over
    // all coin choices.
    // minCoins(amount) = 1 + min( minCoins(amount - c) ) for every coin c <= amount
    // Base case: minCoins(0) = 0 (no coins needed to make amount 0).
    // If amount < 0 or no coin fits, that branch is invalid (treat as infinity).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Just like Fibonacci, this recursion recomputes the same "amount" subproblem many
    // times through different coin combinations. For coins=[1,2,5] and amount=6:
    //   minCoins(6)
    //     -> uses coin 1 -> minCoins(5)
    //     -> uses coin 2 -> minCoins(4)
    //     -> uses coin 5 -> minCoins(1)
    //   minCoins(5) itself calls minCoins(4), minCoins(3), minCoins(0)
    //   minCoins(4) is now being computed BOTH directly from minCoins(6)'s branch,
    //   AND indirectly inside minCoins(5)'s branch - a repeated subproblem.
    // As amount grows, the number of overlapping calls explodes, giving roughly
    // O(coins.length ^ amount) exponential time in the worst case, even though there
    // are only "amount + 1" distinct subproblems (minCoins(0) through minCoins(amount)).
    //
    // ================= OPTIMIZED APPROACH =================
    // Recurrence relation (in words): the minimum coins to make "amount" is 1 (for the
    // last coin used) plus the minimum coins needed for the remaining amount after using
    // that coin, minimized over every possible coin denomination that fits.
    // Formula: dp[amount] = min( dp[amount - c] + 1 ) for every coin c where c <= amount,
    // and dp[0] = 0.
    //
    // Top-down (memoization): same recursive structure, but cache dp[amount] in a HashMap
    // so each distinct amount subproblem is solved only once.
    //
    // Bottom-up (tabulation): build a dp[] array of size (amount + 1), initialize every
    // entry to a sentinel value "amount + 1" (acting as "infinity", since the true answer
    // can never exceed amount coins of denomination 1, so amount+1 is safely larger than
    // any real answer, and importantly won't overflow like Integer.MAX_VALUE would if we
    // add 1 to it). Set dp[0] = 0, then for each amount from 1 upward, try every coin and
    // take the minimum. At the end, if dp[amount] is still the sentinel, no combination works.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // We use an array for dp[] because the subproblem keys (0 .. amount) are dense,
    // small, non-negative integers - array indexing gives O(1) direct access, which is
    // simpler and faster than a HashMap here. (The memoized top-down version uses a
    // HashMap for clarity/flexibility, but a plain int[] initialized to a sentinel like
    // -1 "not yet computed" works just as well and is often preferred in production code.)
    // The sentinel value "amount + 1" is used instead of Integer.MAX_VALUE to safely allow
    // "dp[amount - c] + 1" without integer overflow while still being unambiguously larger
    // than any real, valid answer.
    //
    // ================= EDGE CASES =================
    // - amount = 0: always 0 coins needed, regardless of coins array.
    // - No coins can make the amount (e.g. coins=[2], amount=3): return -1.
    // - coins array is empty: return -1 for any amount > 0, 0 for amount = 0.
    // - A coin equals the amount exactly: answer is 1.
    // - Duplicate denominations in the coins array: should not affect correctness.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force is exponential, roughly O(coins.length ^ amount) in
    // the worst case, due to overlapping subproblems shown above. Memoized/tabulated DP
    // is O(amount * coins.length) - polynomial - because there are "amount + 1" distinct
    // subproblems, and each does O(coins.length) work trying every coin.
    // Space Complexity: O(amount) for the dp[] array or memo map, plus O(amount) recursion
    // stack for the top-down version. This dp[] array generally cannot be reduced below
    // O(amount) (unlike Fibonacci-style problems) because dp[amount] can depend on any
    // earlier dp[amount - c] for an arbitrary coin c, not just the last one or two values.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why can't we always reduce the dp array to O(1) space, unlike Climbing Stairs?
    // - How would you also return WHICH coins were used, not just the count?
    // - How does this change for the "Coin Change 2" variant (count the number of ways,
    //   not the minimum coins)?
    // - Why do we initialize dp[] to "amount + 1" instead of Integer.MAX_VALUE?
    // - What's the time complexity if coins are not sorted - does it matter here?
    // - How would greedy (always pick the largest coin first) fail on certain coin sets
    //   (e.g. coins=[1,3,4], amount=6)?
    // - How would you adapt this to a bounded supply of each coin (e.g. only 2 of each)?

    // Brute force: plain recursion, tries every coin, recomputes overlapping subproblems.
    public static int coinChangeBruteForceRecursive(int[] coins, int amount) {
        if (amount == 0) {
            return 0; // base case: 0 coins needed for amount 0
        }
        if (amount < 0) {
            return -1; // invalid path: overshot the target
        }
        int minCoins = Integer.MAX_VALUE;
        for (int coin : coins) {
            int subResult = coinChangeBruteForceRecursive(coins, amount - coin);
            if (subResult >= 0) {
                minCoins = Math.min(minCoins, subResult + 1); // +1 for using this coin
            }
        }
        return (minCoins == Integer.MAX_VALUE) ? -1 : minCoins;
    }

    // Top-down memoization: cache each amount's answer in a HashMap.
    public static int coinChangeMemoized(int[] coins, int amount) {
        Map<Integer, Integer> memo = new HashMap<>();
        return coinChangeMemoizedHelper(coins, amount, memo);
    }

    private static int coinChangeMemoizedHelper(int[] coins, int amount, Map<Integer, Integer> memo) {
        if (amount == 0) {
            return 0;
        }
        if (amount < 0) {
            return -1;
        }
        // Step: return the cached answer if we already solved this amount before.
        if (memo.containsKey(amount)) {
            return memo.get(amount);
        }
        int minCoins = Integer.MAX_VALUE;
        for (int coin : coins) {
            int subResult = coinChangeMemoizedHelper(coins, amount - coin, memo);
            if (subResult >= 0) {
                minCoins = Math.min(minCoins, subResult + 1);
            }
        }
        int result = (minCoins == Integer.MAX_VALUE) ? -1 : minCoins;
        memo.put(amount, result); // cache before returning
        return result;
    }

    // Bottom-up tabulation: dp[i] = min coins to make amount i, using a sentinel value.
    public static int coinChangeTabulation(int[] coins, int amount) {
        int sentinel = amount + 1; // acts as "infinity" - larger than any real answer, no overflow risk
        int[] dp = new int[amount + 1];
        java.util.Arrays.fill(dp, sentinel);
        dp[0] = 0; // base case: 0 coins needed to make amount 0

        // Step: for every amount from 1 up to the target, try every coin.
        for (int currentAmount = 1; currentAmount <= amount; currentAmount++) {
            for (int coin : coins) {
                if (coin <= currentAmount) {
                    dp[currentAmount] = Math.min(dp[currentAmount], dp[currentAmount - coin] + 1);
                }
            }
        }
        return (dp[amount] == sentinel) ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        int[] coins1 = {1, 2, 5};
        int amount1 = 11;
        // Expected: 3  (5 + 5 + 1)
        System.out.println("Input: coins=[1,2,5], amount=11");
        System.out.println("Brute force: " + coinChangeBruteForceRecursive(coins1, amount1));
        System.out.println("Memoized:    " + coinChangeMemoized(coins1, amount1));
        System.out.println("Tabulation:  " + coinChangeTabulation(coins1, amount1));

        int[] coins2 = {2};
        int amount2 = 3;
        // Expected: -1  (impossible - 3 is odd, only even amounts possible with coin 2)
        System.out.println("\nInput: coins=[2], amount=3 (impossible edge case)");
        System.out.println("Tabulation:  " + coinChangeTabulation(coins2, amount2));

        int[] coins3 = {1, 3, 4};
        int amount3 = 0;
        // Expected: 0
        System.out.println("\nInput: coins=[1,3,4], amount=0 (edge case)");
        System.out.println("Tabulation:  " + coinChangeTabulation(coins3, amount3));
    }
}
