package com.playground.java.interview.arrays;

import java.util.Arrays;

/**
 * PATTERN: Arrays / In-place Reversal
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Rotate an array to the right by k steps, in-place.
 */
public class RotateArray {

    // ================= PROBLEM =================
    // You get an array of numbers and a number k.
    // You need to rotate the array to the right by k positions, in-place.
    // Example: nums = [1, 2, 3, 4, 5, 6, 7], k = 3 -> output = [5, 6, 7, 1, 2, 3, 4]
    // because each element moves 3 positions to the right, wrapping around at the end.
    //
    // ================= SIMPLE APPROACH =================
    // Rotate the array one step at a time, k times.
    // Each single-step rotation: save the last element, shift every other element
    // one position to the right, then put the saved last element at the front.
    // Repeat this k times (after normalizing k with k % n).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Each single-step rotation takes O(n) time because we shift every element.
    // Doing this k times gives O(n * k) time, which is very slow when k is close to n
    // (for example, rotating a million-element array by half a million steps).
    //
    // ================= OPTIMIZED APPROACH =================
    // Use the "reverse trick":
    // 1. Reverse the entire array.
    // 2. Reverse the first k elements.
    // 3. Reverse the remaining n-k elements.
    // This places every element in its final rotated position using only reversals,
    // no shifting one-by-one and no extra array.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just the array itself and a reverse helper.
    // Three reversals, each O(n), still add up to O(n) total (not O(n^2)), and the
    // whole rotation happens in-place with O(1) extra space, unlike an approach
    // that copies elements into a brand-new array.
    //
    // ================= EDGE CASES =================
    // - k is 0: array stays the same.
    // - k is a multiple of array length (e.g., n=4, k=8): array stays the same, must normalize k % n.
    // - k is larger than array length: normalize with k % n first.
    // - Array has only one element: any k leaves it unchanged.
    // - Empty array: nothing to rotate.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized reverse trick - three passes over the array,
    // each O(n), which is still O(n) overall. Brute force is O(n * k).
    // Space Complexity: O(1) for the optimized approach - rotation happens in-place.
    // Brute force is also O(1) extra space but far slower in time.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we normalize k with k % n before rotating?
    // - Can you rotate left instead of right using the same reverse trick?
    // - How would you do this without any extra space, not even a temp swap variable?
    // - What if the array is a linked list instead - how would rotation differ?
    // - Could you rotate using a temporary array of size k instead? What's the trade-off?
    // - How would you handle a very large k (larger than array length by orders of magnitude) safely?
    // - What if the rotation amount could be negative (meaning rotate left)?

    // Brute force: rotate one step at a time, k times. O(n*k).
    public static void rotateBruteForce(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int n = nums.length;
        k = k % n;
        for (int step = 0; step < k; step++) {
            int last = nums[n - 1];
            // Shift every element one position to the right.
            for (int i = n - 1; i > 0; i--) {
                nums[i] = nums[i - 1];
            }
            // Wrap the last element around to the front.
            nums[0] = last;
        }
    }

    // Optimized: reverse whole array, then reverse first k, then reverse the rest.
    public static void rotateOptimized(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            return;
        }
        int n = nums.length;
        k = k % n;
        // Step 1: reverse the entire array.
        reverse(nums, 0, n - 1);
        // Step 2: reverse the first k elements (now in correct rotated order).
        reverse(nums, 0, k - 1);
        // Step 3: reverse the remaining n-k elements.
        reverse(nums, k, n - 1);
    }

    // Helper: reverse nums[start..end] in-place.
    private static void reverse(int[] nums, int start, int end) {
        while (start < end) {
            int temp = nums[start];
            nums[start] = nums[end];
            nums[end] = temp;
            start++;
            end--;
        }
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3, 4, 5, 6, 7};
        // Expected: [5, 6, 7, 1, 2, 3, 4]
        System.out.println("Input: [1,2,3,4,5,6,7], k=3");
        rotateOptimized(nums1, 3);
        System.out.println("Optimized output: " + Arrays.toString(nums1));

        int[] nums2 = {-1, -100, 3, 99};
        // Expected: [3, 99, -1, -100]
        System.out.println("\nInput: [-1,-100,3,99], k=2");
        rotateBruteForce(nums2, 2);
        System.out.println("Brute force output: " + Arrays.toString(nums2));

        int[] nums3 = {1, 2, 3};
        // Expected: [1, 2, 3] (k is a multiple of length, no change)
        System.out.println("\nInput: [1,2,3], k=9 (k > n, normalizes to k=0)");
        rotateOptimized(nums3, 9);
        System.out.println("Optimized output: " + Arrays.toString(nums3));
    }
}
