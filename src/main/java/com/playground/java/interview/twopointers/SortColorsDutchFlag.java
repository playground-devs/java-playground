package com.playground.java.interview.twopointers;

import java.util.Arrays;

/**
 * PATTERN: Two Pointers / Dutch National Flag
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Sort an array containing only the values 0, 1, and 2 in a single
 * pass, in-place, without using a counting sort or a general sort function.
 */
public class SortColorsDutchFlag {

    // ================= PROBLEM =================
    // You get an array containing only three distinct values: 0, 1, and 2
    // (representing colors, like red, white, and blue).
    // You need to sort the array in-place so all 0s come first, then all 1s, then all 2s.
    // Example: nums = [2, 0, 2, 1, 1, 0] -> output = [0, 0, 1, 1, 2, 2]
    //
    // ================= SIMPLE APPROACH =================
    // Count how many 0s, 1s, and 2s are in the array in one pass.
    // Then overwrite the array in a second pass: write that many 0s, then that many 1s,
    // then that many 2s.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This "counting sort" approach works and is technically O(n) time, but it
    // requires two full passes over the array (one to count, one to overwrite),
    // and the problem is often specifically posed as a challenge to solve it in a
    // single pass using constant extra space with pointer manipulation (the Dutch
    // National Flag algorithm), which is what interviewers are testing for.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use three pointers: low, mid, and high.
    // - low marks the boundary before which everything is 0.
    // - high marks the boundary after which everything is 2.
    // - mid is the current element being examined.
    // Walk through the array with mid:
    // - If nums[mid] is 0, swap it with nums[low], then move both low and mid forward.
    // - If nums[mid] is 1, it's already in the right general area, just move mid forward.
    // - If nums[mid] is 2, swap it with nums[high], then move high backward (but do NOT
    //   move mid forward yet, because the swapped-in value at mid still needs to be checked).
    // Continue until mid crosses high. This sorts the array in exactly one pass.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - three integer pointers achieve an in-place
    // three-way partition of the array in a single traversal. This is the same idea
    // as the two-way partition step in quicksort, extended to three buckets, avoiding
    // any second pass or auxiliary counting array.
    //
    // ================= EDGE CASES =================
    // - Array already sorted: algorithm still works, just does fewer effective swaps.
    // - Array with only one distinct value (e.g., all 1s): trivially already "sorted".
    // - Empty array or single-element array.
    // - Array sorted in reverse order (e.g., [2,2,1,1,0,0]): tests the high-pointer swaps heavily.
    // - Large runs of the same value in a row.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized Dutch National Flag approach - a single
    // pass where mid moves forward every iteration except on a "swap with high" case,
    // and high only moves a bounded number of times. Counting sort is also O(n) but
    // needs two passes (still same big-O, but double the constant work).
    // Space Complexity: O(1) for the optimized approach - only three pointers, no
    // extra array. Counting sort brute force is also O(1) extra space (just three counters).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we not advance mid after swapping with high, but we do advance mid after swapping with low?
    // - Can you generalize this three-way partitioning idea to sort an array of k distinct small values?
    // - How does this relate to the three-way partitioning used in an optimized quicksort (Dutch flag quicksort)?
    // - What if the values weren't guaranteed to be exactly 0, 1, 2 but were three arbitrary known constants?
    // - Could you solve this stably (preserving relative order of equal elements) - does this algorithm do that?
    // - How would you adapt this if the array contained four distinct values instead of three?
    // - What's the worst-case number of swaps this algorithm performs?

    // Brute force / alternative: counting sort with two passes. O(n) time, O(1) space, but two passes.
    public static void sortColorsCountingSort(int[] nums) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int countZero = 0;
        int countOne = 0;
        int countTwo = 0;

        // First pass: count occurrences of each color.
        for (int num : nums) {
            if (num == 0) {
                countZero++;
            } else if (num == 1) {
                countOne++;
            } else {
                countTwo++;
            }
        }

        // Second pass: overwrite the array in sorted order.
        int index = 0;
        while (countZero-- > 0) {
            nums[index++] = 0;
        }
        while (countOne-- > 0) {
            nums[index++] = 1;
        }
        while (countTwo-- > 0) {
            nums[index++] = 2;
        }
    }

    // Optimized: Dutch National Flag, single pass with three pointers. O(n) time, O(1) space.
    public static void sortColorsDutchFlag(int[] nums) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int low = 0;
        int mid = 0;
        int high = nums.length - 1;

        while (mid <= high) {
            if (nums[mid] == 0) {
                // Swap the 0 into the low region, both regions grow.
                swap(nums, low, mid);
                low++;
                mid++;
            } else if (nums[mid] == 1) {
                // Already in the correct middle region, just move on.
                mid++;
            } else {
                // nums[mid] == 2: swap into the high region.
                // Do not increment mid, since the swapped-in value still needs checking.
                swap(nums, mid, high);
                high--;
            }
        }
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static void main(String[] args) {
        int[] nums1 = {2, 0, 2, 1, 1, 0};
        // Expected: [0, 0, 1, 1, 2, 2]
        System.out.println("Input: [2,0,2,1,1,0]");
        sortColorsDutchFlag(nums1);
        System.out.println("Dutch flag output: " + Arrays.toString(nums1));

        int[] nums2 = {2, 2, 1, 1, 0, 0};
        // Expected: [0, 0, 1, 1, 2, 2] (reverse-sorted input)
        System.out.println("\nInput: [2,2,1,1,0,0] (reverse sorted)");
        sortColorsCountingSort(nums2);
        System.out.println("Counting sort output: " + Arrays.toString(nums2));

        int[] nums3 = {1};
        // Expected: [1] (single element)
        System.out.println("\nInput: [1] (single element)");
        sortColorsDutchFlag(nums3);
        System.out.println("Dutch flag output: " + Arrays.toString(nums3));
    }
}
