package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Modified Binary Search
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Find a target's index in O(log n) inside a sorted array that has been rotated at an unknown pivot, with no duplicate values.
 */
// ================= PROBLEM =================
// A sorted array (ascending order, no duplicates) has been "rotated" at some unknown
// pivot point. For example [0,1,2,4,5,6,7] rotated at index 3 becomes [4,5,6,7,0,1,2].
// Given this rotated array and a target value, find the index of the target. If it's
// not present, return -1.
//
// Example: nums = [4,5,6,7,0,1,2], target = 0 -> Output: 4
// Example: nums = [4,5,6,7,0,1,2], target = 3 -> Output: -1 (not present)
//
// ================= SIMPLE APPROACH =================
// Just scan the array left to right, comparing each element to the target, and return
// the index if found, else -1. This is a plain linear search that ignores the sorted
// structure entirely.
//
// ================= WHY IT'S NOT ENOUGH =================
// This takes O(n) time and completely wastes the fact that the array is "sorted, just
// rotated." We know there's a smarter approach because half of the array (either the
// left portion or the right portion relative to any midpoint) is always still properly
// sorted, which is exactly the kind of structure binary search exploits.
//
// ================= OPTIMIZED APPROACH =================
// Use a modified binary search. At each step, look at nums[low], nums[mid], nums[high]:
//   1. Compute mid = low + (high - low) / 2.
//   2. If nums[mid] == target, return mid.
//   3. Determine which half is "properly sorted" (not rotated):
//        - If nums[low] <= nums[mid], the LEFT half (low..mid) is sorted normally.
//        - Otherwise, the RIGHT half (mid..high) is sorted normally.
//   4. Once we know which half is sorted, check if the target falls within that
//      sorted half's value range:
//        - If left half is sorted and nums[low] <= target < nums[mid], search left
//          half (high = mid - 1). Otherwise search right half (low = mid + 1).
//        - If right half is sorted and nums[mid] < target <= nums[high], search right
//          half (low = mid + 1). Otherwise search left half (high = mid - 1).
//   5. Repeat until found or low > high (return -1).
// The key insight: even though the whole array isn't sorted, at least one of the two
// halves around any midpoint always is, and we can use that half's range to decide
// where the target could possibly be.
//
// ================= WHY THIS DATA STRUCTURE =================
// Just like classic binary search, we only need two or three index variables (low,
// mid, high) -- no extra data structure. The rotation doesn't break binary search, it
// just requires an extra comparison step to figure out which half is sorted before
// deciding which way to narrow the search. This preserves O(log n) time, which a
// linear scan or any approach involving first "finding the pivot" via linear scan
// would not achieve as directly (though finding the pivot with binary search first,
// then doing a normal binary search, is a valid two-pass alternative with the same
// overall complexity).
//
// ================= EDGE CASES =================
// - Array not rotated at all (pivot at index 0): should behave like plain binary search.
// - Single-element array: matches or doesn't, straightforward base case.
// - Empty array: return -1 immediately.
// - Target is the pivot element itself (the minimum of the array): must be found correctly.
// - Target equal to nums[low] or nums[high] boundary values: off-by-one care needed in range checks.
// - Two-element array, either order: smallest rotation case, must not break the sorted-half logic.
//
// ================= COMPLEXITY =================
// Time Complexity: O(log n) because, just like standard binary search, every step
//                   eliminates half of the remaining search space, regardless of rotation.
// Space Complexity: O(1) extra space for the iterative version (only a few index variables).
//
// ================= INTERVIEW FOLLOW-UPS =================
// - How does your solution change if the array can contain duplicate values (this makes it impossible in the worst case to always tell which half is sorted in O(log n))?
// - Can you first find the rotation pivot index with binary search, then do a second binary search on the correct half? Compare that two-pass approach to the single-pass one implemented here.
// - How would you find the minimum element in a rotated sorted array using a similar technique?
// - What happens to this algorithm if the array is rotated by the full length (i.e., not actually rotated)?
// - Can this approach be generalized to a "rotated" 2D matrix or a circular buffer search?
// - Walk through why checking nums[low] <= nums[mid] correctly determines whether the left half is sorted.
// - How would you handle this if the array is extremely large and stored across a distributed system (i.e., no random access in O(1))?

public class SearchInRotatedSortedArray {

    // Modified binary search: O(log n)
    public static int search(int[] nums, int target) {
        int low = 0;
        int high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (nums[mid] == target) {
                return mid; // found it
            }

            // figure out which half is properly sorted
            if (nums[low] <= nums[mid]) {
                // left half (low..mid) is sorted normally
                if (nums[low] <= target && target < nums[mid]) {
                    // target lies within the sorted left half's range
                    high = mid - 1;
                } else {
                    // target must be in the right half
                    low = mid + 1;
                }
            } else {
                // right half (mid..high) is sorted normally
                if (nums[mid] < target && target <= nums[high]) {
                    // target lies within the sorted right half's range
                    low = mid + 1;
                } else {
                    // target must be in the left half
                    high = mid - 1;
                }
            }
        }
        return -1; // not found
    }

    public static void main(String[] args) {
        int[] nums1 = {4, 5, 6, 7, 0, 1, 2};

        // Expected: 4 (value 0 is at index 4)
        System.out.println("nums = " + java.util.Arrays.toString(nums1) + ", target = 0");
        System.out.println("Result: " + search(nums1, 0));

        // Expected: -1 (3 is not present)
        System.out.println("\nnums = " + java.util.Arrays.toString(nums1) + ", target = 3");
        System.out.println("Result: " + search(nums1, 3));

        // Expected: 0 (array not rotated, target is first element)
        int[] nums2 = {1};
        System.out.println("\nnums = " + java.util.Arrays.toString(nums2) + ", target = 1");
        System.out.println("Result: " + search(nums2, 1));

        // Edge case: empty array
        int[] nums3 = {};
        // Expected: -1
        System.out.println("\nnums = " + java.util.Arrays.toString(nums3) + ", target = 5");
        System.out.println("Result: " + search(nums3, 5));
    }
}
