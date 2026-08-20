package com.playground.java.interview.binarysearch;

import java.util.Arrays;

/**
 * PATTERN: Binary Search (on the slope)
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Find any peak element (strictly greater than its neighbors) in O(log n).
 */
public class FindPeakElement {

    // ================= PROBLEM =================
    // A peak element is one that is strictly greater than its neighbors. Elements
    // outside the array bounds are considered to be negative infinity, so the first
    // or last element can be a peak just by being greater than its single neighbor.
    // Assumption: nums[i] != nums[i+1] for all valid i (no plateaus), which is what
    // guarantees a peak always exists and that binary search on the slope is safe.
    // You may return the index of ANY peak, not necessarily the largest.
    // Example: nums = [1,2,3,1] -> Output: 2 (nums[2] = 3 is greater than both neighbors)
    // Example: nums = [1,2,1,3,5,6,4] -> Output: 1 or 5 (both are valid peaks)
    //
    // ================= SIMPLE APPROACH =================
    // Scan left to right and check each element against both neighbors (treating
    // out-of-bounds neighbors as negative infinity). Return the first index where
    // the element is greater than both neighbors.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This is O(n) and inspects nearly every element, but the problem promises we
    // only need to find "a" peak, not "the" peak - the array's local slope gives us
    // enough information to eliminate half the search space at every step instead of
    // checking one element at a time.
    //
    // ================= OPTIMIZED APPROACH =================
    // Binary search on the "slope" between mid and mid + 1:
    // Step 1: compute mid = lo + (hi - lo) / 2.
    // Step 2: compare nums[mid] and nums[mid + 1].
    //   - If nums[mid] < nums[mid + 1], the array is going "uphill" at mid, so a peak
    //     MUST exist somewhere to the right (either mid+1 itself or further along,
    //     since the sequence has to eventually stop climbing or hit the end, which
    //     also counts as a peak). Move lo = mid + 1.
    //   - Otherwise (nums[mid] >= nums[mid + 1], going "downhill" or flat-then-down),
    //     a peak must exist at mid or to its left, so move hi = mid.
    // Step 3: when lo == hi, that index is guaranteed to be a peak.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure is needed - the trick is recognizing that the
    // "uphill vs downhill" comparison at mid behaves like a monotonic predicate:
    // once you're heading downhill, everything to the right of a peak can be safely
    // discarded, and vice versa. This lets binary search discard half the array each
    // step even though the array as a whole is NOT sorted.
    //
    // ================= EDGE CASES =================
    // - Single element array: trivially a peak (no neighbors to compare against).
    // - Strictly increasing array, e.g. [1,2,3,4]: peak is the last index.
    // - Strictly decreasing array, e.g. [4,3,2,1]: peak is the first index.
    // - Peak at the very start or very end of the array.
    // - Multiple peaks exist, e.g. [1,3,2,4,1]: any correct peak index is acceptable.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n) - inspects each element once in the worst case.
    // Optimized O(log n) - each comparison halves the remaining search space.
    // Space Complexity: O(1) for both approaches - only a few index variables are used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the "uphill implies peak to the right" argument hold even though the array isn't globally sorted?
    // - What breaks if the array is allowed to have equal adjacent elements (plateaus)? How would you adapt?
    // - How would you find ALL peaks instead of just one?
    // - How would you find the global maximum (largest peak) instead of any peak?
    // - What if the array is circular (first and last elements are neighbors)?
    // - How would this generalize to a 2D grid (find a peak that is greater than all 4 neighbors)?
    // - Could you solve this recursively? What does the recursion tree look like?

    // Brute force: linear scan comparing each element to both neighbors, O(n).
    public static int findPeakBruteForce(int[] nums) {
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            // treat out-of-bounds neighbors as negative infinity
            int left = (i == 0) ? Integer.MIN_VALUE : nums[i - 1];
            int right = (i == n - 1) ? Integer.MIN_VALUE : nums[i + 1];
            if (nums[i] > left && nums[i] > right) {
                return i;
            }
        }
        return -1; // should not happen given problem guarantees
    }

    // Optimized: binary search on the slope, O(log n).
    public static int findPeakOptimized(int[] nums) {
        int lo = 0;
        int hi = nums.length - 1;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < nums[mid + 1]) {
                // going uphill, a peak must be to the right
                lo = mid + 1;
            } else {
                // going downhill (or at a peak), peak is at mid or to the left
                hi = mid;
            }
        }
        // lo == hi, guaranteed to be a peak index
        return lo;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3, 1};
        // Expected: 2 (nums[2] = 3 is greater than both neighbors)
        System.out.println("nums = " + Arrays.toString(nums1));
        System.out.println("Brute force: index " + findPeakBruteForce(nums1));
        System.out.println("Optimized:   index " + findPeakOptimized(nums1));

        int[] nums2 = {1, 2, 1, 3, 5, 6, 4};
        // Expected: index 1 or index 5, both are valid peaks
        System.out.println("\nnums = " + Arrays.toString(nums2));
        System.out.println("Brute force: index " + findPeakBruteForce(nums2));
        System.out.println("Optimized:   index " + findPeakOptimized(nums2));

        // Edge case: single element array, trivially a peak
        int[] nums3 = {42};
        // Expected: 0
        System.out.println("\nnums = " + Arrays.toString(nums3));
        System.out.println("Optimized: index " + findPeakOptimized(nums3));

        // Edge case: strictly decreasing array, peak is the first index
        int[] nums4 = {4, 3, 2, 1};
        // Expected: 0
        System.out.println("\nnums = " + Arrays.toString(nums4));
        System.out.println("Optimized: index " + findPeakOptimized(nums4));
    }
}
