package com.playground.java.interview.mathpuzzles;

import java.util.Arrays;

/**
 * PATTERN: Math / Array Manipulation
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Rearrange an array of numbers into the next lexicographically
 * greater permutation, in-place. If no such permutation exists (already the largest),
 * rearrange it into the smallest possible order (sorted ascending).
 */
public class NextPermutation {

    // ================= PROBLEM =================
    // You get an array of numbers representing a permutation.
    // You need to rearrange it in-place into the next permutation in lexicographic
    // (dictionary) order - the smallest arrangement that is strictly greater than
    // the current one. If the current arrangement is already the largest possible,
    // wrap around to the smallest (fully sorted ascending).
    // Example: nums = [1, 2, 3] -> output = [1, 3, 2]
    // Example: nums = [3, 2, 1] -> output = [1, 2, 3] (wraps around, was the largest)
    // Example: nums = [1, 1, 5] -> output = [1, 5, 1]
    //
    // ================= SIMPLE APPROACH =================
    // Generate all permutations of the array, sort them lexicographically, find the
    // current permutation in that sorted list, and return the one right after it
    // (or the first one if the current is the last).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Generating all permutations takes O(n!) time and space, which is completely
    // impractical for anything beyond very small arrays (n! grows explosively).
    //
    // ================= OPTIMIZED APPROACH =================
    // Use the classic "next permutation" algorithm:
    // 1. Scan from the right, find the first index i where nums[i] < nums[i+1]
    //    (the first place where the sequence stops being non-increasing from the right).
    //    This is the "pivot". If no such index exists, the whole array is the
    //    largest permutation - just reverse it entirely to get the smallest.
    // 2. If a pivot is found, scan from the right again to find the first index j
    //    (j > i) where nums[j] > nums[i] (the smallest number on the right side
    //    that is still bigger than the pivot).
    // 3. Swap nums[i] and nums[j].
    // 4. Reverse the subarray to the right of index i (from i+1 to the end) - this
    //    puts that suffix back into ascending order, which is the smallest possible
    //    arrangement for that suffix, guaranteeing we get the *next* permutation,
    //    not some much larger one.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - the algorithm rearranges the array
    // in-place using a pivot search, a swap, and a reversal, all directly on the
    // input array with O(1) extra space, avoiding the combinatorial explosion of
    // generating every permutation.
    //
    // ================= EDGE CASES =================
    // - Already the largest permutation (strictly descending, e.g., [3,2,1]): wraps to the smallest (ascending).
    // - Already the smallest permutation (strictly ascending, e.g., [1,2,3]): produces the very next larger one.
    // - Array with duplicate values (e.g., [1,1,5]): must still find the correct next permutation.
    // - Single element array: no change possible, "next" is itself.
    // - Two element array: simple swap ([1,2] -> [2,1], [2,1] -> [1,2]).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized algorithm - a constant number of
    // linear scans (find pivot, find swap candidate, reverse suffix). Brute force
    // permutation generation is O(n!).
    // Space Complexity: O(1) for the optimized approach - purely in-place.
    // Brute force needs O(n! * n) space to store all permutations.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does reversing the suffix after the swap guarantee the *next* permutation and not a larger jump?
    // - How would you find the *previous* permutation instead of the next one?
    // - How would you find the Kth permutation of n elements directly, without iterating one-by-one?
    // - What if the array contains duplicate values - does the algorithm still correctly avoid skipping or repeating permutations?
    // - How would you verify a solution is correct by testing it against all permutations for small n?
    // - Can you explain the relationship between this algorithm and how permutations are ordered lexicographically?
    // - What's the total number of "next permutation" calls needed to cycle through all n! permutations back to the start?

    // Optimized: classic in-place next-permutation algorithm. O(n) time, O(1) space.
    public static void nextPermutationOptimized(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        int n = nums.length;

        // Step 1: find the first index i from the right where nums[i] < nums[i+1].
        int i = n - 2;
        while (i >= 0 && nums[i] >= nums[i + 1]) {
            i--;
        }

        if (i >= 0) {
            // Step 2: find the smallest value on the right of i that is still greater than nums[i].
            int j = n - 1;
            while (nums[j] <= nums[i]) {
                j--;
            }
            // Step 3: swap the pivot with that value.
            swap(nums, i, j);
        }
        // Step 4: reverse everything after index i to get the smallest suffix arrangement.
        // If no pivot was found (i == -1), this reverses the entire array, wrapping to the smallest permutation.
        reverse(nums, i + 1, n - 1);
    }

    private static void swap(int[] nums, int a, int b) {
        int temp = nums[a];
        nums[a] = nums[b];
        nums[b] = temp;
    }

    private static void reverse(int[] nums, int start, int end) {
        while (start < end) {
            swap(nums, start, end);
            start++;
            end--;
        }
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3};
        // Expected: [1, 3, 2]
        System.out.println("Input: [1,2,3]");
        nextPermutationOptimized(nums1);
        System.out.println("Optimized output: " + Arrays.toString(nums1));

        int[] nums2 = {3, 2, 1};
        // Expected: [1, 2, 3] (wraps around, was the largest permutation)
        System.out.println("\nInput: [3,2,1] (already largest, wraps to smallest)");
        nextPermutationOptimized(nums2);
        System.out.println("Optimized output: " + Arrays.toString(nums2));

        int[] nums3 = {1, 1, 5};
        // Expected: [1, 5, 1] (duplicate values)
        System.out.println("\nInput: [1,1,5] (duplicate values)");
        nextPermutationOptimized(nums3);
        System.out.println("Optimized output: " + Arrays.toString(nums3));
    }
}
