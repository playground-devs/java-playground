package com.playground.java.interview.prefixsum;

/**
 * PATTERN: Prefix Sum
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an immutable array, answer multiple range-sum queries
 * sumRange(i, j) efficiently.
 */
public class RangeSumQueryImmutable {

    // ================= PROBLEM =================
    // You get an array of numbers that never changes (immutable), and you will be
    // asked many times to compute the sum of elements between index i and index j
    // (inclusive). You need to answer each query as fast as possible.
    // Example: nums = [-2, 0, 3, -5, 2, -1]
    // sumRange(0, 2) -> output = 1   (-2 + 0 + 3 = 1)
    // sumRange(2, 5) -> output = -1  (3 + -5 + 2 + -1 = -1)
    //
    // ================= SIMPLE APPROACH =================
    // For every sumRange(i, j) call, loop from index i to index j and add up the
    // elements directly, returning the total.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Each query takes O(n) time in the worst case (summing nearly the whole array).
    // If there are Q queries, total time becomes O(Q * n), which is too slow when
    // there are many queries on a large array (the whole point of "immutable" is that
    // we should preprocess once and answer queries much faster).
    //
    // ================= OPTIMIZED APPROACH =================
    // Precompute a prefix sum array once, where prefix[k] = sum of all elements from
    // index 0 to index k-1. Building this takes O(n) time, done only once in the
    // constructor.
    // Then sumRange(i, j) can be answered instantly using prefix[j+1] - prefix[i],
    // because prefix[j+1] is the total up to j, and subtracting prefix[i] removes
    // everything before index i, leaving exactly the sum from i to j.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A prefix sum array turns "sum of a range" into a single subtraction of two
    // precomputed values, giving O(1) time per query after a one-time O(n)
    // preprocessing step - ideal when the underlying array never changes and many
    // queries are expected.
    //
    // ================= EDGE CASES =================
    // - Query where i == j: sum is just that single element.
    // - Query covering the entire array (i=0, j=n-1).
    // - Array with negative numbers: prefix sums can decrease, subtraction still works correctly.
    // - Empty array: no valid queries possible.
    // - Repeated queries on the same range: each still answered in O(1) since preprocessing is already done.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) one-time preprocessing to build the prefix sum array,
    // then O(1) per query for the optimized approach. Brute force is O(1) "preprocessing"
    // (none needed) but O(n) per query, so O(Q*n) for Q queries overall.
    // Space Complexity: O(n) for the optimized approach to store the prefix sum array.
    // Brute force uses O(1) extra space but is far slower across many queries.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we build a prefix array of size n+1 instead of size n?
    // - What would you do differently if the array were mutable (values could be updated) - would you use a Fenwick Tree / Segment Tree instead?
    // - How would you extend this to 2D range sum queries (sum of a submatrix)?
    // - How would you handle range "update" queries efficiently if updates became common (discuss Binary Indexed Tree)?
    // - What if queries could also ask for range minimum or maximum instead of sum - would prefix sums still work?
    // - How would you handle overflow if the array contained very large numbers and many elements?
    // - Can you explain why this preprocessing trade-off (O(n) space, O(1) query) is worth it here?

    private final int[] prefixSums;

    // Optimized: build a prefix sum array once. O(n) time, O(n) space.
    public RangeSumQueryImmutable(int[] nums) {
        this.prefixSums = new int[nums.length + 1];
        // prefixSums[0] = 0 by default, meaning "sum of zero elements".
        for (int i = 0; i < nums.length; i++) {
            prefixSums[i + 1] = prefixSums[i] + nums[i];
        }
    }

    // Optimized query: O(1) using the precomputed prefix sums.
    public int sumRangeOptimized(int i, int j) {
        // Sum of elements [i..j] = total up to j+1 minus total up to i.
        return prefixSums[j + 1] - prefixSums[i];
    }

    // Brute force query (kept for comparison): recomputes the sum every time. O(n) per query.
    public static int sumRangeBruteForce(int[] nums, int i, int j) {
        int sum = 0;
        for (int index = i; index <= j; index++) {
            sum += nums[index];
        }
        return sum;
    }

    public static void main(String[] args) {
        int[] nums = {-2, 0, 3, -5, 2, -1};
        RangeSumQueryImmutable rangeSumQuery = new RangeSumQueryImmutable(nums);

        // Expected: 1 (-2 + 0 + 3)
        System.out.println("Input: nums=[-2,0,3,-5,2,-1], sumRange(0, 2)");
        System.out.println("Brute force output: " + sumRangeBruteForce(nums, 0, 2));
        System.out.println("Optimized output: " + rangeSumQuery.sumRangeOptimized(0, 2));

        // Expected: -1 (3 + -5 + 2 + -1)
        System.out.println("\nInput: sumRange(2, 5)");
        System.out.println("Optimized output: " + rangeSumQuery.sumRangeOptimized(2, 5));

        // Expected: -2 (single element at index 0)
        System.out.println("\nInput: sumRange(0, 0) (single element, edge case)");
        System.out.println("Optimized output: " + rangeSumQuery.sumRangeOptimized(0, 0));
    }
}
