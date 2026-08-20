package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 0-1 Knapsack (Subset Sum)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given an array of positive integers, determine if it can be partitioned
 * into two subsets such that the sum of elements in both subsets is equal.
 */
public class PartitionEqualSubsetSum {

    // ================= PROBLEM =================
    // You are given an array of positive integers. Determine whether it's possible to split it
    // into two subsets so that both subsets have the exact same sum.
    // Example: nums = [1, 5, 11, 5] -> output = true, because [1, 5, 5] and [11] both sum to 11.
    // Example: nums = [1, 2, 3, 5] -> output = false (total sum 11 is odd, can never split evenly).
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible way of splitting the array into two groups (equivalent to trying every
    // subset - include or exclude each element), and check if any split results in equal sums.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Trying every subset is O(2^n), which is exponential and infeasible for anything beyond a
    // small array.
    //
    // ================= OPTIMIZED APPROACH =================
    // First, key insight: if the total sum of the array is odd, it's immediately impossible to
    // split into two equal halves - return false right away. Otherwise, the problem reduces to:
    // "does some subset of the array sum to exactly totalSum / 2?" (if one subset sums to
    // half, the rest automatically sums to the other half).
    // This is the classic 0/1 knapsack "subset sum" problem. Use a boolean DP array
    // dp[sum] = true if some subset of the elements processed so far sums to exactly `sum`.
    // - dp[0] = true always (the empty subset sums to 0).
    // - For each number in the array, update dp from HIGH sum down to LOW sum (this is a 0/1
    //   knapsack style in-place update: iterating downward ensures each number is only used
    //   once per subset, since we don't want to re-use a number's own update within the same pass):
    //   dp[sum] = dp[sum] || dp[sum - number] for sum from target down to number.
    // - At the end, dp[target] tells us if a subset summing to target (= totalSum/2) exists.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 1D boolean array (rather than a full 2D dp[i][sum] table) is enough because each row of
    // the classic 2D 0/1 knapsack DP only depends on the row above it - by iterating the sum
    // dimension in DECREASING order for each new number, we can safely reuse a single array in
    // place, cutting space from O(n * target) down to O(target).
    //
    // ================= EDGE CASES =================
    // - Total sum is odd: immediately impossible, return false without running the DP at all.
    // - Array with a single element: impossible unless that element itself is 0 (not applicable
    //   here since elements are positive) - generally false for a single positive element.
    // - Array where all elements are equal and there's an even count: always partitionable.
    // - Very large numbers making target = totalSum/2 huge: DP array size and time both scale
    //   with target, so this can become slow/memory-heavy for large sums (pseudo-polynomial).
    // - Empty array: sum is 0, which trivially "partitions" into two empty-sum subsets (edge case worth discussing, typically returns true for target=0 trivially satisfied by dp[0]=true, but no real elements means this may be a degenerate case to clarify with the interviewer).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * target) where target = totalSum / 2 - for each of n numbers, we
    // update up to `target` DP entries. This is pseudo-polynomial (depends on the VALUE of the
    // sum, not just array length), much better than O(2^n) brute force for reasonable sums.
    // Space Complexity: O(target) for the 1D boolean DP array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you extend this to split into K equal-sum subsets instead of just 2 (harder, generally uses backtracking with pruning)?
    // - How would you reconstruct WHICH elements belong in each subset, not just whether a split exists?
    // - Why must the sum dimension be iterated in decreasing order during the update - what breaks if you iterate increasing?
    // - How does this relate to the general 0/1 knapsack problem (maximize value under a weight constraint)?
    // - What if the array could contain negative numbers - how would the approach need to change?
    // - How would you handle extremely large sums where even O(target) space is too much (would need a different, likely approximate, approach)?

    // Brute force: try every subset via recursion (include/exclude each element).
    public static boolean canPartitionBruteForce(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        if (totalSum % 2 != 0) {
            return false;
        }
        return canReachSum(nums, 0, totalSum / 2);
    }

    private static boolean canReachSum(int[] nums, int index, int remainingTarget) {
        if (remainingTarget == 0) {
            return true;
        }
        if (index >= nums.length || remainingTarget < 0) {
            return false;
        }
        // Step: try including nums[index], or excluding it.
        return canReachSum(nums, index + 1, remainingTarget - nums[index])
                || canReachSum(nums, index + 1, remainingTarget);
    }

    // Optimized: reduce to subset-sum == totalSum/2 using a 1D boolean DP array.
    public static boolean canPartitionOptimized(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        // Step: an odd total sum can never be split into two equal integer halves.
        if (totalSum % 2 != 0) {
            return false;
        }

        int target = totalSum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true; // the empty subset always sums to 0

        for (int num : nums) {
            // Step: update sums from high to low so each number is used at most once per subset.
            for (int sum = target; sum >= num; sum--) {
                if (dp[sum - num]) {
                    dp[sum] = true;
                }
            }
        }

        return dp[target];
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 5, 11, 5};
        // Expected: true ([1,5,5] and [11] both sum to 11)
        System.out.println("Input: [1,5,11,5]");
        System.out.println("Output: " + canPartitionOptimized(nums1));

        int[] nums2 = {1, 2, 3, 5};
        // Expected: false (total sum 11 is odd)
        System.out.println("\nInput: [1,2,3,5]");
        System.out.println("Output: " + canPartitionOptimized(nums2));

        int[] nums3 = {2};
        // Expected: false (single element, cannot split evenly)
        System.out.println("\nInput: [2] (single element)");
        System.out.println("Output: " + canPartitionOptimized(nums3));
    }
}
