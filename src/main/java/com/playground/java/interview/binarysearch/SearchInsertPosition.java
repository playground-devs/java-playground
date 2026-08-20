package com.playground.java.interview.binarysearch;

import java.util.Arrays;

/**
 * PATTERN: Binary Search
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Find the index of a target in a sorted array, or the index where it would be inserted to keep the array sorted.
 */
public class SearchInsertPosition {

    // ================= PROBLEM =================
    // Given a sorted array of distinct integers and a target value, return the index
    // if the target is found. If not, return the index where it would be inserted to
    // keep the array sorted.
    // Example: nums = [1,3,5,6], target = 5 -> Output: 2 (found at index 2)
    // Example: nums = [1,3,5,6], target = 2 -> Output: 1 (would be inserted between 1 and 3)
    //
    // ================= SIMPLE APPROACH =================
    // Scan the array left to right and return the index of the first element that is
    // greater than or equal to the target. If no such element exists, the target
    // belongs at the very end (index = nums.length).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The array is sorted, so we don't need to look at every element one by one -
    // a linear scan is O(n) but the sortedness lets us eliminate half the remaining
    // candidates on every comparison, which is exactly what binary search exploits.
    //
    // ================= OPTIMIZED APPROACH =================
    // Standard binary search with lo = 0, hi = nums.length - 1.
    // Step 1: compute mid = lo + (hi - lo) / 2.
    // Step 2: if nums[mid] == target, we found it, return mid.
    // Step 3: if nums[mid] < target, the target (or its insert spot) is to the right,
    //          so move lo = mid + 1.
    // Step 4: if nums[mid] > target, the target (or its insert spot) is to the left
    //          (possibly at mid itself), so move hi = mid - 1.
    // Step 5: when lo > hi, the loop ends without finding the target. At this point,
    //          lo is exactly the correct insertion index - every element before lo
    //          is smaller than target and every element from lo onward (that we didn't
    //          rule out) is bigger, so inserting at lo keeps the array sorted.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - the key insight is that binary search's
    // exit condition (lo > hi) naturally lands lo on the correct insertion point.
    // This works because binary search always converges lo to the first index whose
    // value is >= target, whether or not that exact value exists in the array.
    //
    // ================= EDGE CASES =================
    // - Target smaller than every element: should insert at index 0.
    // - Target larger than every element: should insert at index nums.length.
    // - Target exactly equal to an existing element: return that element's index.
    // - Empty array: should immediately return 0 (only possible insertion point).
    // - Single-element array, target less than / equal to / greater than that element.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n) - linear scan touches every element in the
    // worst case (target larger than all elements).
    // Optimized O(log n) - each step halves the search space.
    // Space Complexity: O(1) for both approaches - only a few index variables are used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would this change if the array contains duplicates and you need the
    //   leftmost insertion point (bisect_left) vs rightmost (bisect_right)?
    // - Can you write this using recursion instead of iteration? What's the trade-off?
    // - How does this relate to Java's Arrays.binarySearch and its "-(insertion point) - 1" return convention?
    // - What if the array were sorted in descending order instead?
    // - How would you adapt this to search in a 2D row/column-sorted matrix?
    // - Why do we use lo + (hi - lo) / 2 instead of (lo + hi) / 2?
    // - How would you find the insertion point in a very large, disk-backed sorted file?

    // Brute force: linear scan, O(n).
    public static int searchInsertBruteForce(int[] nums, int target) {
        // walk left to right, return first index whose value is >= target
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] >= target) {
                return i;
            }
        }
        // target is bigger than everything, insert at the end
        return nums.length;
    }

    // Optimized: binary search, O(log n).
    public static int searchInsertOptimized(int[] nums, int target) {
        int lo = 0;
        int hi = nums.length - 1;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; // avoid overflow
            if (nums[mid] == target) {
                return mid; // exact match
            } else if (nums[mid] < target) {
                lo = mid + 1; // target is further right
            } else {
                hi = mid - 1; // target is further left (or at mid)
            }
        }
        // lo now sits exactly at the correct insertion index
        return lo;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 3, 5, 6};
        // Expected: 2 (found exactly at index 2)
        System.out.println("nums = " + Arrays.toString(nums1) + ", target = 5");
        System.out.println("Brute force: " + searchInsertBruteForce(nums1, 5));
        System.out.println("Optimized:   " + searchInsertOptimized(nums1, 5));

        // Expected: 1 (2 would go between 1 and 3)
        System.out.println("\nnums = " + Arrays.toString(nums1) + ", target = 2");
        System.out.println("Brute force: " + searchInsertBruteForce(nums1, 2));
        System.out.println("Optimized:   " + searchInsertOptimized(nums1, 2));

        // Edge case: target bigger than all elements -> insert at end
        // Expected: 4
        System.out.println("\nnums = " + Arrays.toString(nums1) + ", target = 7");
        System.out.println("Brute force: " + searchInsertBruteForce(nums1, 7));
        System.out.println("Optimized:   " + searchInsertOptimized(nums1, 7));

        // Edge case: empty array -> insert at index 0
        int[] empty = {};
        // Expected: 0
        System.out.println("\nnums = [], target = 5");
        System.out.println("Optimized:   " + searchInsertOptimized(empty, 5));
    }
}
