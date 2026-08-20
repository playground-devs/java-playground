package com.playground.java.interview.dynamicprogramming;

import java.util.Arrays;

/**
 * PATTERN: Dynamic Programming / Longest Increasing Subsequence (DP + Binary Search)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given an array of integers, find the length of the longest strictly
 * increasing subsequence (elements need not be contiguous, but must keep their relative order).
 */
public class LongestIncreasingSubsequence {

    // ================= PROBLEM =================
    // Given an integer array, find the length of the longest strictly increasing subsequence
    // (LIS). A subsequence keeps the original relative order of elements but does not need to be
    // contiguous.
    // Example: nums = [10, 9, 2, 5, 3, 7, 101, 18] -> output = 4 (the LIS is [2, 3, 7, 101])
    // Example: nums = [0, 1, 0, 3, 2, 3] -> output = 4 (the LIS is [0, 1, 2, 3])
    //
    // ================= SIMPLE APPROACH =================
    // At each index, recursively decide whether to include it in the subsequence (only if it's
    // strictly greater than the last included element) or skip it, and take the maximum length
    // found across all such choices, starting from every possible index.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This plain recursion re-explores the same "starting new subsequence from index i, given a
    // previous element" combinations repeatedly, leading to exponential O(2^n) time without
    // memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // Two levels of optimization are shown here:
    // 1) O(n^2) DP: let dp[i] = length of the longest increasing subsequence that ENDS exactly
    //    at index i. Initialize every dp[i] = 1 (each element alone is a subsequence of length
    //    1). For each i, look back at every j < i: if nums[j] < nums[i], the subsequence ending
    //    at j can be extended by nums[i], so dp[i] = max(dp[i], dp[j] + 1). The answer is the
    //    max value across the whole dp array.
    // 2) O(n log n) patience-sorting / binary search: maintain a "tails" array, where tails[k] is
    //    the smallest possible tail value of any increasing subsequence of length k+1 found so
    //    far. For each new number x: binary search for the leftmost position in tails where
    //    tails[pos] >= x, and replace tails[pos] with x (or append x if it's bigger than every
    //    tail so far, extending the longest subsequence found). The final length of tails is the
    //    LIS length. Note: tails does not necessarily hold an actual valid subsequence, only the
    //    best possible tail values per length - but its length always equals the true LIS length.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // The O(n^2) DP needs only a simple 1D array since dp[i] depends on all previous dp[j]
    // values, not just the last one or two. The O(n log n) approach uses a dynamically growing
    // array (tails) specifically because it supports binary search (via Arrays.binarySearch or a
    // manual lower-bound search) to find the insertion point in O(log n), which is what
    // collapses the O(n) inner scan of the DP version down to O(log n) per element.
    //
    // ================= EDGE CASES =================
    // - Empty array: LIS length is 0.
    // - Single element: LIS length is 1.
    // - Strictly decreasing array (e.g. [5, 4, 3, 2, 1]): LIS length is 1 (every element starts
    //   its own subsequence of length 1, none can extend another).
    // - Strictly increasing array (e.g. [1, 2, 3, 4]): LIS length equals the array length.
    // - Duplicate values (e.g. [2, 2]): since the subsequence must be STRICTLY increasing,
    //   duplicates cannot extend each other, so LIS length stays 1 for that pair.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(2^n) for brute force; O(n^2) for the DP version (n elements, each
    // scanning up to n previous elements); O(n log n) for the binary-search version (n elements,
    // each doing an O(log n) binary search).
    // Space Complexity: O(n) for the DP array; O(n) for the tails array in the binary-search
    // version (worst case tails grows to the full array length).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the "tails" array in the O(n log n) approach not represent an actual valid subsequence, yet its length still equals the correct LIS length?
    // - How would you reconstruct the actual LIS elements (not just the length) from either approach?
    // - How would you modify this for a "longest non-decreasing subsequence" (allowing equal values) instead of strictly increasing?
    // - What is patience sorting, and how does this binary-search technique relate to it?
    // - How would you adapt this to find the Longest Common Subsequence between two arrays instead?
    // - Why can we use Arrays.binarySearch with a small trick (or a custom lower-bound search) even though tails changes dynamically?

    // Brute force: plain recursion, O(2^n).
    public static int lengthOfLISBruteForce(int[] nums) {
        return bestFrom(nums, -1, 0);
    }

    private static int bestFrom(int[] nums, int prevIndex, int currentIndex) {
        if (currentIndex == nums.length) {
            return 0; // reached the end, no more elements to add
        }

        // Step: option 1 - skip this element entirely.
        int skip = bestFrom(nums, prevIndex, currentIndex + 1);

        // Step: option 2 - include this element, only if it strictly extends the previous one.
        int take = 0;
        if (prevIndex == -1 || nums[currentIndex] > nums[prevIndex]) {
            take = 1 + bestFrom(nums, currentIndex, currentIndex + 1);
        }

        return Math.max(skip, take);
    }

    // Optimized (DP): dp[i] = length of LIS ending exactly at index i, O(n^2).
    public static int lengthOfLISDp(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }

        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1); // every single element is a subsequence of length 1 by itself
        int maxLength = 1;

        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                // Step: if nums[j] can be extended by nums[i], try improving dp[i].
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLength = Math.max(maxLength, dp[i]);
        }

        return maxLength;
    }

    // Optimized (binary search / patience sorting): O(n log n).
    public static int lengthOfLISBinarySearch(int[] nums) {
        int[] tails = new int[nums.length]; // tails[k] = smallest tail value for LIS of length k+1
        int size = 0; // current number of valid entries in tails

        for (int num : nums) {
            // Step: binary search for the leftmost index where tails[index] >= num.
            int lo = 0;
            int hi = size;
            while (lo < hi) {
                int mid = lo + (hi - lo) / 2;
                if (tails[mid] < num) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }

            // Step: replace the tail at that position, or extend tails if num is a new max.
            tails[lo] = num;
            if (lo == size) {
                size++;
            }
        }

        return size;
    }

    public static void main(String[] args) {
        int[] nums1 = {10, 9, 2, 5, 3, 7, 101, 18};
        // Expected: 4 (LIS = [2, 3, 7, 101])
        System.out.println("Input: [10, 9, 2, 5, 3, 7, 101, 18]");
        System.out.println("Output (DP): " + lengthOfLISDp(nums1));
        System.out.println("Output (binary search): " + lengthOfLISBinarySearch(nums1));

        int[] nums2 = {0, 1, 0, 3, 2, 3};
        // Expected: 4 (LIS = [0, 1, 2, 3])
        System.out.println("\nInput: [0, 1, 0, 3, 2, 3]");
        System.out.println("Output (DP): " + lengthOfLISDp(nums2));
        System.out.println("Output (binary search): " + lengthOfLISBinarySearch(nums2));

        int[] nums3 = {};
        // Expected: 0 (empty array)
        System.out.println("\nInput: [] (empty array)");
        System.out.println("Output (DP): " + lengthOfLISDp(nums3));
        System.out.println("Output (binary search): " + lengthOfLISBinarySearch(nums3));
    }
}
