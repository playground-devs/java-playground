package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Binary Search
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Search a sorted array for a target value in O(log n), and find the insertion position when the value is absent.
 */
// ================= PROBLEM =================
// Given a sorted array of distinct integers and a target value, find the index of the
// target if it exists. If it does not exist, find the index where it would be inserted
// to keep the array sorted (this variant is often called "search insert position" or
// relates to "lower bound").
//
// Example: nums = [1,3,5,6], target = 5 -> Output: 2 (index of 5)
// Example: nums = [1,3,5,6], target = 2 -> Output: 1 (2 would be inserted at index 1)
//
// ================= SIMPLE APPROACH =================
// Scan the array from left to right, comparing each element to the target. If found,
// return its index. If we pass the point where the target would fit (current element
// becomes greater than target), return that index as the insertion point.
//
// ================= WHY IT'S NOT ENOUGH =================
// Linear scan takes O(n) time. Since the array is already sorted, we are wasting the
// sorted property -- we can eliminate half of the remaining search space with every
// comparison instead of only ruling out one element at a time.
//
// ================= OPTIMIZED APPROACH =================
// Use binary search. Keep a low pointer and a high pointer marking the current search
// range. Repeatedly:
//   1. Compute mid = low + (high - low) / 2 (avoids integer overflow vs (low+high)/2).
//   2. If nums[mid] == target, we found it, return mid.
//   3. If nums[mid] < target, the target must be in the right half, so move low = mid + 1.
//   4. If nums[mid] > target, the target must be in the left half, so move high = mid - 1.
// Repeat until low > high. At that point, "low" is exactly the correct insertion index
// if the target was never found (this is the classic "lower bound" trick).
// The same logic can be written iteratively (using a while loop) or recursively
// (function calls itself on a smaller range) -- both included below.
//
// ================= WHY THIS DATA STRUCTURE =================
// No auxiliary data structure is needed at all, just two index variables (low, high).
// Binary search works here specifically because the array is sorted, which guarantees
// that comparing against the middle element tells us definitively which half the
// target (or its insertion point) must be in. This is what allows us to safely discard
// half the search space on every comparison, giving logarithmic time instead of linear.
//
// ================= EDGE CASES =================
// - Empty array: target cannot be found, insertion index is 0.
// - Target smaller than all elements: insertion index is 0.
// - Target larger than all elements: insertion index is array length.
// - Target equal to the first or last element: must be handled correctly by boundary conditions.
// - Single-element array: must work whether the single element matches, is smaller, or is larger than target.
// - Duplicate values (not in this classic version, but a common follow-up): need to decide leftmost vs rightmost match.
//
// ================= COMPLEXITY =================
// Time Complexity: O(log n) because the search range is halved on every iteration/recursive call.
// Space Complexity: Iterative version O(1) extra space (just a few variables).
//                    Recursive version O(log n) extra space due to the call stack.
//
// ================= INTERVIEW FOLLOW-UPS =================
// - How would you modify binary search to find the leftmost (first) occurrence of a duplicate target?
// - How would you modify it to find the rightmost (last) occurrence?
// - Why do we compute mid as low + (high - low) / 2 instead of (low + high) / 2?
// - How would you adapt binary search to work on a rotated sorted array?
// - What is the relationship between "search insert position" and "lower_bound" in C++ / "bisect_left" in Python?
// - Can binary search be applied to a non-array data structure, like a linked list? Why or why not (hint: no O(1) random access)?
// - How would you binary search on an "answer" rather than an array index (e.g. finding minimum capacity to ship packages within D days)?
// - What happens to recursive binary search with very large arrays in terms of stack depth, and how would you mitigate it?

public class BinarySearch {

    // Iterative binary search: returns index of target, or -1 if not found
    public static int searchIterative(int[] nums, int target) {
        int low = 0;
        int high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2; // avoid overflow
            if (nums[mid] == target) {
                return mid; // found it
            } else if (nums[mid] < target) {
                low = mid + 1; // target is in the right half
            } else {
                high = mid - 1; // target is in the left half
            }
        }
        return -1; // not found
    }

    // Recursive binary search: returns index of target, or -1 if not found
    public static int searchRecursive(int[] nums, int target) {
        return searchRecursiveHelper(nums, target, 0, nums.length - 1);
    }

    private static int searchRecursiveHelper(int[] nums, int target, int low, int high) {
        if (low > high) {
            return -1; // search space exhausted, not found
        }
        int mid = low + (high - low) / 2;
        if (nums[mid] == target) {
            return mid;
        } else if (nums[mid] < target) {
            return searchRecursiveHelper(nums, target, mid + 1, high); // search right half
        } else {
            return searchRecursiveHelper(nums, target, low, mid - 1); // search left half
        }
    }

    // Search insert position (lower bound style): returns index of target if found,
    // otherwise the index where target should be inserted to keep array sorted.
    public static int searchInsertPosition(int[] nums, int target) {
        int low = 0;
        int high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (nums[mid] == target) {
                return mid; // exact match found
            } else if (nums[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // when the loop ends, low is the correct insertion index
        return low;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 3, 5, 6};

        // Expected: 2 (index of 5)
        System.out.println("nums = " + java.util.Arrays.toString(nums1) + ", target = 5");
        System.out.println("Iterative: " + searchIterative(nums1, 5));
        System.out.println("Recursive: " + searchRecursive(nums1, 5));
        System.out.println("Insert position: " + searchInsertPosition(nums1, 5));

        // Expected: 1 (2 would be inserted between 1 and 3)
        System.out.println("\nnums = " + java.util.Arrays.toString(nums1) + ", target = 2");
        System.out.println("Iterative (not found): " + searchIterative(nums1, 2));
        System.out.println("Insert position: " + searchInsertPosition(nums1, 2));

        // Expected: 4 (7 would be inserted at the end)
        System.out.println("\nnums = " + java.util.Arrays.toString(nums1) + ", target = 7");
        System.out.println("Insert position: " + searchInsertPosition(nums1, 7));

        // Edge case: empty array
        int[] nums2 = {};
        // Expected: -1 (not found) and insert position 0
        System.out.println("\nnums = " + java.util.Arrays.toString(nums2) + ", target = 5");
        System.out.println("Iterative: " + searchIterative(nums2, 5));
        System.out.println("Insert position: " + searchInsertPosition(nums2, 5));
    }
}
