package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 1D DP (Non-Adjacent Sum)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given an array of non-negative integers representing money in each house,
 * find the maximum amount you can rob without robbing two directly adjacent houses.
 */
public class HouseRobber {

    // ================= PROBLEM =================
    // You are a robber planning to rob houses along a street, where each house has a certain
    // amount of money, given as an array. You cannot rob two adjacent houses (the security
    // systems are linked and will alert the police). Find the maximum amount of money you can
    // rob without triggering an alarm.
    // Example: nums = [1, 2, 3, 1] -> output = 4 (rob house 0 for 1, and house 2 for 3 -> 1+3=4)
    // Example: nums = [2, 7, 9, 3, 1] -> output = 12 (rob houses 0, 2, 4 -> 2+9+1=12)
    //
    // ================= SIMPLE APPROACH =================
    // At each house, you have two choices: rob it (and skip the next one) or skip it (and move
    // to the next one normally). Recursively try both choices at every house and take the
    // maximum result over all valid paths.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The same "starting from house i" subproblem gets recomputed many times through different
    // combinations of rob/skip decisions taken earlier, leading to exponential O(2^n) time
    // without memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // Bottom-up DP: let dp[i] = maximum money that can be robbed from the first i houses.
    // - dp[0] = 0 (no houses, no money).
    // - dp[1] = nums[0] (only one house, rob it).
    // - For i from 2 to n: dp[i] = max(dp[i-1], dp[i-2] + nums[i-1]).
    //   Either skip house i (keep dp[i-1]), or rob house i and add it to dp[i-2] (the best
    //   result excluding the immediately preceding house).
    // Since dp[i] only ever depends on the previous two values, we don't need a full array -
    // just track two rolling variables (prevOne, prevTwo) and update them as we scan.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No array, heap, or graph structure is required at all - this recurrence only ever looks
    // back two steps, so two rolling integer variables are sufficient, giving O(1) space instead
    // of O(n). This is the same "compress a 1D DP array into two variables" trick used in
    // Fibonacci/Climbing Stairs style problems.
    //
    // ================= EDGE CASES =================
    // - Empty array: no houses to rob, answer is 0.
    // - Single house: rob it, answer is nums[0].
    // - Two houses: rob the larger one, since they are adjacent and both cannot be robbed.
    // - All houses have the same value: still only every other house (roughly) should be robbed.
    // - Alternating small/large values (e.g. [5, 1, 5, 1, 5]): confirms the DP correctly skips
    //   the smaller adjacent houses to maximize the total.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - one pass through the array, O(1) work per house, versus O(2^n) for
    // the naive recursion.
    // Space Complexity: O(1) - only two rolling variables are kept (the optimized version avoids
    // the O(n) DP array entirely).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve the "House Robber II" variant where the houses are arranged in a circle (first and last are adjacent)?
    // - How would you modify this DP if you needed to also return WHICH houses were robbed, not just the max total?
    // - Why does dp[i] = max(dp[i-1], dp[i-2] + nums[i-1]) correctly avoid ever counting two adjacent houses?
    // - How does this problem relate structurally to the Climbing Stairs / Fibonacci recurrence?
    // - Could this be solved with a greedy approach instead of DP? Why or why not?
    // - How would you extend this to a "House Robber III" variant where houses form a binary tree instead of a line?

    // Brute force: plain recursion trying rob/skip at each house, O(2^n).
    public static int robBruteForce(int[] nums) {
        return robFrom(nums, 0);
    }

    private static int robFrom(int[] nums, int index) {
        if (index >= nums.length) {
            return 0; // no more houses left to consider
        }

        // Step: option 1 - skip this house, move to the next one.
        int skip = robFrom(nums, index + 1);

        // Step: option 2 - rob this house, skip the next one (adjacent), move two ahead.
        int rob = nums[index] + robFrom(nums, index + 2);

        return Math.max(skip, rob);
    }

    // Optimized: O(n) time, O(1) space using two rolling variables.
    public static int robOptimized(int[] nums) {
        int prevTwo = 0; // best result excluding the last two houses (dp[i-2])
        int prevOne = 0; // best result excluding the last house (dp[i-1])

        for (int num : nums) {
            // Step: either skip this house (keep prevOne) or rob it (prevTwo + num).
            int current = Math.max(prevOne, prevTwo + num);
            prevTwo = prevOne;
            prevOne = current;
        }

        return prevOne;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3, 1};
        // Expected: 4 (rob houses 0 and 2 -> 1+3=4)
        System.out.println("Input: [1, 2, 3, 1]");
        System.out.println("Output: " + robOptimized(nums1));

        int[] nums2 = {2, 7, 9, 3, 1};
        // Expected: 12 (rob houses 0, 2, 4 -> 2+9+1=12)
        System.out.println("\nInput: [2, 7, 9, 3, 1]");
        System.out.println("Output: " + robOptimized(nums2));

        int[] nums3 = {};
        // Expected: 0 (no houses)
        System.out.println("\nInput: [] (empty array)");
        System.out.println("Output: " + robOptimized(nums3));
    }
}
