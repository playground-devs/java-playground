package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 0-1 Knapsack (2D Table + Rolling Array)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given item weights, values, and a knapsack capacity, find the maximum
 * total value that can be carried without exceeding capacity, using each item at most once.
 */
public class ZeroOneKnapsack {

    // ================= PROBLEM =================
    // You have n items, each with a weight and a value, and a knapsack that can hold at most
    // `capacity` total weight. Choose a subset of items (each item used at most once - hence
    // "0-1") to maximize the total value without the total weight exceeding capacity.
    // Example: weights = [1, 3, 4, 5], values = [1, 4, 5, 7], capacity = 7
    //          -> output = 9 (take items with weight 3 and 4, values 4+5=9)
    //
    // ================= SIMPLE APPROACH =================
    // At each item, recursively decide whether to include it (if it fits in the remaining
    // capacity) or exclude it, and take the maximum value over both choices, moving to the next
    // item each time.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The same "remaining capacity at item i" subproblem is recomputed repeatedly across
    // different combinations of earlier include/exclude decisions, leading to exponential
    // O(2^n) time without memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // 2D DP table: let dp[i][c] = maximum value achievable using the first i items with capacity
    // c. For each item i (1-indexed) and each capacity c:
    // - If weights[i-1] > c, the item doesn't fit: dp[i][c] = dp[i-1][c] (carry over, can't use
    //   it).
    // - Otherwise: dp[i][c] = max(dp[i-1][c], dp[i-1][c - weights[i-1]] + values[i-1]) - either
    //   skip the item (carry over the value without it) or take it (add its value to the best
    //   result using the remaining capacity from the previous row).
    // The answer is dp[n][capacity].
    // Space-optimized 1D rolling array: since dp[i][*] only ever depends on dp[i-1][*], we can
    // collapse the table into a single 1D array dp[c], BUT we must iterate capacity from HIGH to
    // LOW when updating for each item - this ensures dp[c - weight] on the right-hand side still
    // refers to the previous item's row (not one already updated for the current item), which
    // would incorrectly allow reusing the same item twice.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 2D table makes the recurrence clearest for learning (each row is a fresh "snapshot" for
    // one more item considered), while the 1D rolling array is the practical choice in interviews
    // once you can show you understand WHY the reverse iteration order is required to avoid item
    // reuse - a plain forward iteration would silently turn this into an unbounded/coin-change
    // style knapsack instead.
    //
    // ================= EDGE CASES =================
    // - capacity = 0: no items can be taken, max value is 0.
    // - An item's weight exceeds capacity entirely: it can never be included at any point in the
    //   DP.
    // - All items fit within capacity: take everything, answer is sum of all values.
    // - Empty items list: max value is 0 regardless of capacity.
    // - Multiple items with the same weight/value: handled correctly since each is still a
    //   distinct index used at most once.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(2^n) for brute force; O(n * capacity) for both the 2D DP and the 1D
    // rolling array version (same number of states/transitions, just less memory in the latter).
    // Space Complexity: O(n * capacity) for the 2D table; O(capacity) for the 1D rolling array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must the 1D rolling array iterate capacity from high to low, and what breaks if you iterate low to high instead?
    // - How would you modify this for the "unbounded knapsack" variant where each item can be used unlimited times (this is where low-to-high iteration would actually apply)?
    // - How would you reconstruct WHICH items were chosen, not just the max value, from the 2D table?
    // - How does 0-1 Knapsack relate to the Subset Sum and Partition Equal Subset Sum problems?
    // - What happens to the time/space complexity if capacity is extremely large (e.g. 10^9) - would this DP approach still be feasible?
    // - How would you extend this to a "fractional knapsack" variant, and why does that require a completely different (greedy) approach?

    // Brute force: plain recursion trying include/exclude at each item, O(2^n).
    public static int knapsackBruteForce(int[] weights, int[] values, int capacity) {
        return solve(weights, values, capacity, 0);
    }

    private static int solve(int[] weights, int[] values, int remainingCapacity, int index) {
        if (index == weights.length || remainingCapacity == 0) {
            return 0; // no more items or no more room
        }

        // Step: option 1 - skip this item.
        int skip = solve(weights, values, remainingCapacity, index + 1);

        // Step: option 2 - take this item, only if it fits.
        int take = 0;
        if (weights[index] <= remainingCapacity) {
            take = values[index] + solve(weights, values, remainingCapacity - weights[index], index + 1);
        }

        return Math.max(skip, take);
    }

    // Optimized: 2D DP table, dp[i][c] = max value using first i items with capacity c.
    public static int knapsack2D(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            int weight = weights[i - 1];
            int value = values[i - 1];
            for (int c = 0; c <= capacity; c++) {
                // Step: carry over the result without this item by default.
                dp[i][c] = dp[i - 1][c];
                // Step: if it fits, see if taking it beats skipping it.
                if (weight <= c) {
                    dp[i][c] = Math.max(dp[i][c], dp[i - 1][c - weight] + value);
                }
            }
        }

        return dp[n][capacity];
    }

    // Optimized (space): 1D rolling array, iterating capacity high-to-low per item.
    public static int knapsack1D(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];

        for (int i = 0; i < weights.length; i++) {
            int weight = weights[i];
            int value = values[i];
            // Step: iterate capacity from high to low so dp[c - weight] still reflects the
            // PREVIOUS item's state, preventing this item from being counted more than once.
            for (int c = capacity; c >= weight; c--) {
                dp[c] = Math.max(dp[c], dp[c - weight] + value);
            }
        }

        return dp[capacity];
    }

    public static void main(String[] args) {
        int[] weights1 = {1, 3, 4, 5};
        int[] values1 = {1, 4, 5, 7};
        int capacity1 = 7;
        // Expected: 9 (take weight 3 [value 4] and weight 4 [value 5] -> 4+5=9)
        System.out.println("Input: weights=[1,3,4,5], values=[1,4,5,7], capacity=7");
        System.out.println("Output (2D): " + knapsack2D(weights1, values1, capacity1));
        System.out.println("Output (1D): " + knapsack1D(weights1, values1, capacity1));

        int[] weights2 = {2, 3, 4, 5};
        int[] values2 = {3, 4, 5, 6};
        int capacity2 = 5;
        // Expected: 7 (take weight 2 [value 3] and weight 3 [value 4] -> 3+4=7)
        System.out.println("\nInput: weights=[2,3,4,5], values=[3,4,5,6], capacity=5");
        System.out.println("Output (2D): " + knapsack2D(weights2, values2, capacity2));
        System.out.println("Output (1D): " + knapsack1D(weights2, values2, capacity2));

        int[] weights3 = {5, 6, 7};
        int[] values3 = {10, 20, 30};
        int capacity3 = 0;
        // Expected: 0 (no capacity at all)
        System.out.println("\nInput: weights=[5,6,7], values=[10,20,30], capacity=0");
        System.out.println("Output (2D): " + knapsack2D(weights3, values3, capacity3));
        System.out.println("Output (1D): " + knapsack1D(weights3, values3, capacity3));
    }
}
