package com.playground.java.interview.arrays;

import java.util.Arrays;

/**
 * PATTERN: Arrays / Two Pointers
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Move all zeroes in an array to the end, keeping non-zero elements
 * in their original relative order, in-place.
 */
public class MoveZeroes {

    // ================= PROBLEM =================
    // You get an array of numbers, some of which may be zero.
    // You need to move all the zeroes to the end of the array, while keeping the
    // relative order of the non-zero numbers the same, and do it in-place.
    // Example: nums = [0, 1, 0, 3, 12] -> output = [1, 3, 12, 0, 0]
    //
    // ================= SIMPLE APPROACH =================
    // Create a brand new array of the same size.
    // Walk through the original array and copy every non-zero number into the new
    // array in order. Once done, fill the rest of the new array with zeroes.
    // Finally copy the new array back into the original array.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works correctly but uses O(n) extra space for the new array.
    // The problem is usually asked specifically to test whether you can do it
    // in-place with only O(1) extra space, so this approach misses that requirement.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use two pointers. Keep a pointer "insertPos" that marks where the next
    // non-zero number should go, starting at 0.
    // Walk through the array with a pointer i. Whenever nums[i] is not zero,
    // swap nums[i] with nums[insertPos], then move insertPos forward by one.
    // By the end, all non-zero numbers have been pushed to the front in order,
    // and all zeroes have naturally been pushed to the back.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just two integer pointers.
    // The two-pointer swap technique rearranges the array in a single pass without
    // needing a second array to hold intermediate results, which is exactly the
    // "partition" idea used in quicksort's partition step.
    //
    // ================= EDGE CASES =================
    // - Array with all zeroes: everything stays zero, nothing to move.
    // - Array with no zeroes at all: array stays unchanged.
    // - Empty array or single-element array.
    // - Zeroes scattered non-contiguously among non-zero numbers.
    // - Zeroes already at the end.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - each visits every element once.
    // The real difference is space, not time.
    // Space Complexity: O(1) for the optimized two-pointer swap (in-place).
    // O(n) for the brute force approach because of the temporary array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we swap instead of just overwriting nums[insertPos] = nums[i]?
    // - Can you reduce the number of swaps by skipping the swap when i == insertPos?
    // - How would you change this to move all zeroes to the front instead of the end?
    // - Could you solve it with a single overwrite pass plus a final zero-fill pass? What's the trade-off?
    // - How does this two-pointer technique relate to the Dutch National Flag partitioning problem?
    // - What if the array contained other "falsy" values (like nulls) instead of just zero?
    // - How would this approach change if the data was a stream you could only read once?

    // Brute force: build a new array of non-zero elements then zeroes, copy back. O(n) space.
    public static void moveZeroesBruteForce(int[] nums) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int[] result = new int[nums.length];
        int index = 0;
        // Copy all non-zero elements first, preserving order.
        for (int num : nums) {
            if (num != 0) {
                result[index++] = num;
            }
        }
        // The rest of result[] is already 0 by default, fill remaining positions explicitly for clarity.
        while (index < result.length) {
            result[index++] = 0;
        }
        // Copy back into the original array.
        System.arraycopy(result, 0, nums, 0, nums.length);
    }

    // Optimized: two-pointer in-place swap. O(1) extra space.
    public static void moveZeroesOptimized(int[] nums) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int insertPos = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                // Swap current non-zero element into the next available front slot.
                int temp = nums[insertPos];
                nums[insertPos] = nums[i];
                nums[i] = temp;
                insertPos++;
            }
        }
    }

    public static void main(String[] args) {
        int[] nums1 = {0, 1, 0, 3, 12};
        // Expected: [1, 3, 12, 0, 0]
        System.out.println("Input: [0,1,0,3,12]");
        moveZeroesOptimized(nums1);
        System.out.println("Optimized output: " + Arrays.toString(nums1));

        int[] nums2 = {0, 0, 0};
        // Expected: [0, 0, 0] (all zeroes)
        System.out.println("\nInput: [0,0,0] (all zeroes)");
        moveZeroesBruteForce(nums2);
        System.out.println("Brute force output: " + Arrays.toString(nums2));

        int[] nums3 = {1, 2, 3};
        // Expected: [1, 2, 3] (no zeroes, unchanged)
        System.out.println("\nInput: [1,2,3] (no zeroes)");
        moveZeroesOptimized(nums3);
        System.out.println("Optimized output: " + Arrays.toString(nums3));
    }
}
