package com.playground.java.interview.arrays;

import java.util.Arrays;

/**
 * PATTERN: Arrays / Three Pointers (Merge from the back)
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Merge two sorted arrays into the first one in-place, where the
 * first array has extra trailing space to hold the merged result.
 */
public class MergeSortedArrayInPlace {

    // ================= PROBLEM =================
    // You get two sorted arrays, nums1 and nums2.
    // nums1 has extra empty slots at the end (filled with placeholder zeroes) to hold
    // all the elements once merged. m is the number of real elements in nums1, n is
    // the number of elements in nums2.
    // You need to merge nums2 into nums1 in-place so nums1 becomes one sorted array.
    // Example: nums1 = [1, 2, 3, 0, 0, 0], m = 3, nums2 = [2, 5, 6], n = 3
    // -> output nums1 = [1, 2, 2, 3, 5, 6]
    //
    // ================= SIMPLE APPROACH =================
    // Copy the first m elements of nums1 and all n elements of nums2 into a new
    // temporary array, then sort that temporary array, then copy it back into nums1.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting from scratch costs O((m+n) log(m+n)) time and ignores the fact that
    // both arrays are already sorted individually. It also uses O(m+n) extra space
    // for the temporary array, which the in-place requirement is meant to avoid.
    //
    // ================= OPTIMIZED APPROACH =================
    // Merge from the back using three pointers: one at the last real element of nums1
    // (index m-1), one at the last element of nums2 (index n-1), and one at the very
    // last slot of nums1 (index m+n-1, the write position).
    // Compare the two "current largest" candidates from nums1 and nums2, place the
    // bigger one at the write position, and move that pointer backward.
    // Merging from the back avoids overwriting elements in nums1 that we still need
    // to compare, because we always write into slots that are already "consumed" or empty.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just three integer pointers.
    // Because nums1 already has the exact amount of trailing free space needed,
    // we can fill it from the back without shifting elements or allocating memory,
    // turning what looks like an O(n) space merge into an O(1) space merge.
    //
    // ================= EDGE CASES =================
    // - nums2 is empty (n = 0): nums1 is already correct, nothing to do.
    // - nums1's real elements are empty (m = 0): just copy all of nums2 into nums1.
    // - All elements of nums2 are smaller than all elements of nums1.
    // - All elements of nums2 are larger than all elements of nums1.
    // - Duplicate values across both arrays.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m + n) for the optimized merge - each element from both
    // arrays is visited and placed exactly once. Brute force sort is O((m+n) log(m+n)).
    // Space Complexity: O(1) for the optimized approach - merges directly into nums1.
    // Brute force uses O(m+n) extra space for the temporary array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must we merge from the back instead of the front for this specific setup?
    // - What would go wrong if you tried to merge from the front in-place here?
    // - What if nums2 finishes first but nums1 still has remaining elements - do you need to do anything?
    // - What if nums1 finishes first but nums2 still has remaining elements - why must you copy them?
    // - How would this change if neither array had extra space and you could not modify either in-place?
    // - Can you generalize this three-pointer merge idea to merge k sorted arrays?
    // - How would you handle this if the arrays were extremely large and lived on disk (external merge)?

    // Brute force: copy both into a temp array, sort, copy back. O((m+n) log(m+n)) time, O(m+n) space.
    public static void mergeBruteForce(int[] nums1, int m, int[] nums2, int n) {
        int[] merged = new int[m + n];
        // Copy the real elements of nums1.
        System.arraycopy(nums1, 0, merged, 0, m);
        // Copy all elements of nums2.
        System.arraycopy(nums2, 0, merged, m, n);
        // Sort the combined array from scratch.
        Arrays.sort(merged);
        // Copy the sorted result back into nums1.
        System.arraycopy(merged, 0, nums1, 0, m + n);
    }

    // Optimized: merge from the back using three pointers. O(m+n) time, O(1) space.
    public static void mergeOptimized(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;              // last real element in nums1
        int j = n - 1;              // last element in nums2
        int writePos = m + n - 1;   // last write slot in nums1

        // Place the larger of the two candidates at the end, moving backward.
        while (i >= 0 && j >= 0) {
            if (nums1[i] > nums2[j]) {
                nums1[writePos--] = nums1[i--];
            } else {
                nums1[writePos--] = nums2[j--];
            }
        }
        // If nums2 still has leftover elements, copy them in (they are the smallest remaining).
        // If nums1 still has leftover elements, they are already in their correct place.
        while (j >= 0) {
            nums1[writePos--] = nums2[j--];
        }
    }

    public static void main(String[] args) {
        int[] nums1a = {1, 2, 3, 0, 0, 0};
        int[] nums2a = {2, 5, 6};
        // Expected: [1, 2, 2, 3, 5, 6]
        System.out.println("Input: nums1=[1,2,3,0,0,0] (m=3), nums2=[2,5,6] (n=3)");
        mergeOptimized(nums1a, 3, nums2a, 3);
        System.out.println("Optimized output: " + Arrays.toString(nums1a));

        int[] nums1b = {0};
        int[] nums2b = {1};
        // Expected: [1] (nums1 has no real elements, m=0)
        System.out.println("\nInput: nums1=[0] (m=0), nums2=[1] (n=1)");
        mergeBruteForce(nums1b, 0, nums2b, 1);
        System.out.println("Brute force output: " + Arrays.toString(nums1b));

        int[] nums1c = {1};
        int[] nums2c = {};
        // Expected: [1] (nums2 is empty, nums1 unchanged)
        System.out.println("\nInput: nums1=[1] (m=1), nums2=[] (n=0)");
        mergeOptimized(nums1c, 1, nums2c, 0);
        System.out.println("Optimized output: " + Arrays.toString(nums1c));
    }
}
