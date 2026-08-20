package com.playground.java.interview.hashmap;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: HashMap / Prefix Sum
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Count the number of contiguous subarrays whose elements sum to
 * exactly a target value k.
 */
public class SubarraySumEqualsK {

    // ================= PROBLEM =================
    // You get an array of integers (can include negative numbers) and a target k.
    // You need to count how many contiguous subarrays have a sum exactly equal to k.
    // Example: nums = [1, 1, 1], k = 2 -> output = 2
    // because subarrays [1,1] (indices 0-1) and [1,1] (indices 1-2) both sum to 2.
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible subarray using two nested loops: pick a starting index,
    // then extend the ending index, keeping a running sum. Every time the running
    // sum equals k, count it.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // There are O(n^2) possible subarrays (start, end pairs), and even with a
    // running sum trick this is O(n^2) time overall, which is too slow for large arrays.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use prefix sums with a HashMap. Keep a running "prefix sum" as you walk through
    // the array (sum of all elements from index 0 up to the current index).
    // Key insight: subarray (i+1 .. j) sums to k exactly when prefixSum[j] - prefixSum[i] == k,
    // which means prefixSum[i] == prefixSum[j] - k.
    // So at each step, check how many earlier prefix sums equal (currentPrefixSum - k) -
    // that count tells you how many subarrays ending here sum to k.
    // Store each prefix sum's frequency in a HashMap as you go, starting with
    // prefixSum 0 occurring once (to handle subarrays starting from index 0).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap<Integer, Integer> mapping prefix-sum-value to how many times it has
    // occurred gives O(1) average lookup to answer "how many earlier prefixes equal
    // (currentSum - k)", turning what would be a nested-loop search into a single pass.
    //
    // ================= EDGE CASES =================
    // - Array contains negative numbers: prefix sums can repeat or decrease, the
    //   HashMap approach still works correctly because it just tracks sums, not order.
    // - k is 0: subarrays that sum to zero (e.g., due to cancelling positive/negative values).
    // - Empty array: answer is 0.
    // - Single element equal to k: counts as one valid subarray.
    // - Multiple subarrays with the same sum overlapping at different positions.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized prefix-sum + HashMap approach - one
    // pass through the array with O(1) average map operations. Brute force is O(n^2).
    // Space Complexity: O(n) for the optimized approach - the HashMap can store up
    // to n distinct prefix sums. Brute force is O(1) extra space.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we initialize the HashMap with prefixSum 0 mapped to count 1 before starting?
    // - How does this approach handle negative numbers differently from a sliding window approach (which would not work here)?
    // - What if you needed to count subarrays with a sum divisible by k instead of exactly equal to k?
    // - What if you needed to return the actual subarrays (indices) instead of just a count?
    // - How would you find the longest (not count of) subarray summing to k?
    // - Could a sliding window solve this problem directly - why or why not (hint: negative numbers)?
    // - How would this scale for a streaming array where new elements keep arriving?

    // Brute force: check every subarray with a running sum. O(n^2).
    public static int subarraySumBruteForce(int[] nums, int k) {
        int count = 0;
        for (int start = 0; start < nums.length; start++) {
            int runningSum = 0;
            for (int end = start; end < nums.length; end++) {
                runningSum += nums[end];
                if (runningSum == k) {
                    count++;
                }
            }
        }
        return count;
    }

    // Optimized: prefix sum + HashMap of prefixSum -> frequency. O(n).
    public static int subarraySumOptimized(int[] nums, int k) {
        Map<Integer, Integer> prefixSumCounts = new HashMap<>();
        // A prefix sum of 0 has occurred once before we start (represents "no elements yet").
        prefixSumCounts.put(0, 1);

        int count = 0;
        int currentSum = 0;
        for (int num : nums) {
            currentSum += num;
            // If (currentSum - k) has been seen before, those earlier points mark
            // the start of a subarray ending here that sums to exactly k.
            count += prefixSumCounts.getOrDefault(currentSum - k, 0);
            // Record this prefix sum for future subarrays to reference.
            prefixSumCounts.put(currentSum, prefixSumCounts.getOrDefault(currentSum, 0) + 1);
        }
        return count;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 1, 1};
        // Expected: 2
        System.out.println("Input: nums=[1,1,1], k=2");
        System.out.println("Brute force output: " + subarraySumBruteForce(nums1, 2));
        System.out.println("Optimized output: " + subarraySumOptimized(nums1, 2));

        int[] nums2 = {1, 2, 3, -3, 4};
        // Expected: 3 (subarrays [1,2], [3], and [1,2,3,-3] all sum to 3)
        System.out.println("\nInput: nums=[1,2,3,-3,4], k=3 (negative numbers)");
        System.out.println("Optimized output: " + subarraySumOptimized(nums2, 3));

        int[] nums3 = {};
        // Expected: 0 (empty array)
        System.out.println("\nInput: nums=[] (empty array), k=0");
        System.out.println("Optimized output: " + subarraySumOptimized(nums3, 0));
    }
}
