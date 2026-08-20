package com.playground.java.interview.prefixsum;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Prefix Sum / HashMap (Remainder Counting)
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Count the number of contiguous subarrays whose sum is divisible
 * by a given integer K.
 */
public class SubarraySumDivisibleByK {

    // ================= PROBLEM =================
    // You get an array of integers and an integer K.
    // You need to count how many contiguous subarrays have a sum that is evenly
    // divisible by K (sum % K == 0).
    // Example: nums = [4, 5, 0, -2, -3, 1], K = 5 -> output = 7
    // (there are 7 subarrays whose sum is a multiple of 5)
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible subarray using two nested loops, keeping a running sum.
    // Every time the running sum is divisible by K, count it.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This is O(n^2) time because of the nested loops over all (start, end) pairs,
    // which is too slow for large arrays.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use prefix sums combined with the remainder (modulo) of each prefix sum by K.
    // Key insight: subarray (i+1 .. j) is divisible by K exactly when
    // prefixSum[j] % K == prefixSum[i] % K (their remainders match), because
    // (prefixSum[j] - prefixSum[i]) % K == 0 when the remainders are equal.
    // So, walk through the array keeping a running prefix sum, compute its remainder
    // mod K at each step, and count how many times each remainder has appeared before
    // using a HashMap<remainder, count>. Add that count to the answer at each step,
    // since each earlier occurrence of the same remainder marks a valid subarray end.
    // IMPORTANT: Java's % operator can return negative results for negative numbers,
    // so normalize the remainder with ((sum % K) + K) % K to keep it in [0, K-1].
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap<Integer, Integer> mapping remainder-value to how many times it has
    // occurred gives O(1) average lookups to find "how many earlier prefixes share
    // this remainder", replacing an O(n) inner scan with a constant-time map lookup,
    // and bringing the total time down to O(n).
    //
    // ================= EDGE CASES =================
    // - Negative numbers in the array: prefix sums and remainders can be negative,
    //   must normalize with ((rem % k) + k) % k to avoid incorrect negative remainders.
    // - K equals 1: every subarray sum is divisible by 1, so the answer is the total
    //   number of subarrays, n*(n+1)/2.
    // - Array contains zeroes: a zero on its own is divisible by any K.
    // - Empty array: answer is 0.
    // - Prefix sum of 0 must be pre-seeded in the map with count 1 (an empty prefix,
    //   remainder 0, exists before we've read any elements).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized prefix-sum + HashMap approach - a
    // single pass with O(1) average map operations. Brute force is O(n^2).
    // Space Complexity: O(min(n, K)) for the optimized approach - the HashMap can
    // hold at most K distinct remainders. Brute force is O(1) extra space.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must we normalize the remainder using ((sum % k) + k) % k in Java?
    // - Why do we pre-seed the HashMap with remainder 0 mapped to count 1 before starting?
    // - How is this problem structurally identical to "Subarray Sum Equals K" but using remainders instead of raw differences?
    // - What if K could be 0 - why would that break the modulo operation, and how would you handle it?
    // - How would you find the longest (not count of) subarray divisible by K?
    // - What if you needed subarrays whose sum leaves a specific remainder R (not just 0) when divided by K?
    // - How would this scale to a streaming array where you need a running count of divisible subarrays so far?

    // Brute force: check every subarray with a running sum. O(n^2).
    public static int subarraysDivByKBruteForce(int[] nums, int k) {
        int count = 0;
        for (int start = 0; start < nums.length; start++) {
            int runningSum = 0;
            for (int end = start; end < nums.length; end++) {
                runningSum += nums[end];
                if (runningSum % k == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    // Optimized: prefix sum remainders + HashMap of remainder -> frequency. O(n).
    public static int subarraysDivByKOptimized(int[] nums, int k) {
        Map<Integer, Integer> remainderCounts = new HashMap<>();
        // An empty prefix (remainder 0) has occurred once before we start.
        remainderCounts.put(0, 1);

        int count = 0;
        int prefixSum = 0;
        for (int num : nums) {
            prefixSum += num;
            // Normalize remainder to always be in [0, k-1], even for negative sums.
            int remainder = ((prefixSum % k) + k) % k;

            // Every earlier prefix with the same remainder marks a valid subarray ending here.
            count += remainderCounts.getOrDefault(remainder, 0);
            remainderCounts.put(remainder, remainderCounts.getOrDefault(remainder, 0) + 1);
        }
        return count;
    }

    public static void main(String[] args) {
        int[] nums1 = {4, 5, 0, -2, -3, 1};
        int k1 = 5;
        // Expected: 7
        System.out.println("Input: nums=[4,5,0,-2,-3,1], k=5");
        System.out.println("Brute force output: " + subarraysDivByKBruteForce(nums1, k1));
        System.out.println("Optimized output: " + subarraysDivByKOptimized(nums1, k1));

        int[] nums2 = {5};
        int k2 = 9;
        // Expected: 0 (single element not divisible by 9)
        System.out.println("\nInput: nums=[5], k=9");
        System.out.println("Optimized output: " + subarraysDivByKOptimized(nums2, k2));

        int[] nums3 = {-1, 2, 9};
        int k3 = 2;
        // Expected: 2 (subarrays: [2] and [-1,2,9] both sum to values divisible by 2)
        System.out.println("\nInput: nums=[-1,2,9], k=2 (negative numbers, tests remainder normalization)");
        System.out.println("Optimized output: " + subarraysDivByKOptimized(nums3, k3));
    }
}
