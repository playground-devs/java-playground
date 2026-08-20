package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Arrays / Dynamic Programming
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Find the contiguous subarray with the largest sum (Kadane's Algorithm).
 */
public class MaxSubArrayKadane {

    // ================= PROBLEM =================
    // You get a list of numbers (can include negative numbers).
    // You need to find a stretch of consecutive numbers (a "subarray") whose sum is
    // the largest possible.
    // Example: nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4] -> output = 6
    // because the subarray [4, -1, 2, 1] has the largest sum, which is 6.
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible subarray: pick every starting point, then every ending point,
    // add up the numbers in between, and keep track of the largest sum seen.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Trying every start and every end point is O(n^2) combinations, and adding up
    // each subarray from scratch can make it O(n^3) if not careful (or O(n^2) if we
    // keep a running sum). Either way, this is too slow for large arrays.
    //
    // ================= OPTIMIZED APPROACH =================
    // Walk through the array once, keeping a "running sum so far".
    // At each number, decide: should I keep extending the previous subarray by adding
    // the current number, or should I start a brand new subarray from this number?
    // The answer: if the running sum so far is negative, it's dragging down any future
    // sum, so it's better to drop it and start fresh from the current number.
    // Otherwise, keep extending.
    // Keep a separate variable to remember the best (maximum) sum seen at any point.
    // This is called Kadane's Algorithm.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just two variables (current sum and max sum).
    // This is the key insight of Kadane's Algorithm: we don't need to store all subarrays
    // or recompute sums; we only need to know the best subarray ending at each position,
    // which can be derived from the best subarray ending at the previous position.
    // This turns an O(n^2) or worse problem into a simple O(n) single pass.
    //
    // ================= EDGE CASES =================
    // - Empty array: no subarray exists, decide how to handle (we throw an exception here).
    // - All numbers are negative: the answer is the largest single number (least negative).
    // - Single element array: the answer is that element itself.
    // - All numbers are positive: the answer is the sum of the entire array.
    // - Array with a mix of very large positive and negative numbers.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - we scan the array exactly once for Kadane's algorithm.
    // Brute force is O(n^2) (with running sum) or O(n^3) (recomputing sums each time).
    // Space Complexity: O(1) for Kadane's algorithm - only a couple of variables are used.
    // Brute force also uses O(1) extra space but is much slower in time.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you also return the actual subarray (start and end indices), not just the sum?
    // - What if the array is empty or all elements are negative - how do you handle it cleanly?
    // - How would you solve this for a circular array (the subarray can wrap around the end)?
    // - What if you needed the maximum sum of a subarray with at least K elements?
    // - How would you adapt Kadane's algorithm to find the maximum product subarray instead of sum?
    // - How would this scale if the array were a live data stream (numbers keep arriving)?
    // - Can you explain why greedily dropping a negative running sum is always optimal (never loses a better answer)?

    // Brute force: try every subarray, keep a running sum as we extend the end pointer.
    public static int maxSubArrayBruteForce(int[] nums) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        int maxSum = Integer.MIN_VALUE;
        for (int start = 0; start < nums.length; start++) {
            int runningSum = 0;
            for (int end = start; end < nums.length; end++) {
                runningSum += nums[end];
                maxSum = Math.max(maxSum, runningSum);
            }
        }
        return maxSum;
    }

    // Optimized: Kadane's algorithm, one pass.
    public static int maxSubArrayKadane(int[] nums) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        int currentSum = nums[0];
        int maxSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            // Either extend the previous subarray or start fresh at nums[i].
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            // Track the best sum seen so far.
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }

    public static void main(String[] args) {
        int[] nums1 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        // Expected: 6 (subarray [4, -1, 2, 1])
        System.out.println("Input: [-2, 1, -3, 4, -1, 2, 1, -5, 4]");
        System.out.println("Brute force output: " + maxSubArrayBruteForce(nums1));
        System.out.println("Kadane output: " + maxSubArrayKadane(nums1));

        int[] nums2 = {-3, -1, -2, -5};
        // Expected: -1 (least negative single element)
        System.out.println("\nInput: [-3, -1, -2, -5] (all negative)");
        System.out.println("Kadane output: " + maxSubArrayKadane(nums2));

        int[] nums3 = {5};
        // Expected: 5 (single element)
        System.out.println("\nInput: [5] (single element)");
        System.out.println("Kadane output: " + maxSubArrayKadane(nums3));
    }
}
