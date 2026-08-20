package com.playground.java.interview.binarysearch;

import java.util.Arrays;

/**
 * PATTERN: Binary Search (on partition point)
 * PRIORITY: P2
 * ONE-LINE PROBLEM STATEMENT: Find the median of two sorted arrays in O(log(min(m,n))) time.
 */
public class MedianOfTwoSortedArrays {

    // ================= PROBLEM =================
    // Given two sorted arrays nums1 (size m) and nums2 (size n), find the median of
    // the combined sorted array, WITHOUT necessarily merging them, ideally in
    // O(log(min(m,n))) time.
    // Example: nums1 = [1,3], nums2 = [2] -> combined sorted = [1,2,3] -> median = 2
    // Example: nums1 = [1,2], nums2 = [3,4] -> combined sorted = [1,2,3,4] -> median = (2+3)/2 = 2.5
    //
    // ================= SIMPLE APPROACH =================
    // Merge both sorted arrays into one sorted array of size m + n (like the merge
    // step of merge sort), then read off the middle element (odd total length) or
    // the average of the two middle elements (even total length).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Merging touches every single element from both arrays, costing O(m + n) time
    // and O(m + n) extra space, even though the median only depends on a handful of
    // elements right around the middle. Since both arrays are already sorted, there
    // should be a way to jump straight to "the middle" without visiting everything.
    //
    // ================= OPTIMIZED APPROACH =================
    // Binary search on the PARTITION POINT in the SMALLER array (always binary search
    // on the smaller of the two arrays so the search space is O(log(min(m,n)))).
    //
    // Concept: imagine cutting the combined sorted array into a LEFT half and a RIGHT
    // half, where the left half has exactly half (or, for odd total length, one more
    // than half) of all the elements. If we can find a cut in nums1 at index i and a
    // matching cut in nums2 at index j such that:
    //   - i + j = (m + n + 1) / 2   (left half has the correct total count)
    //   - nums1[i-1] <= nums2[j]    (everything in nums1's left part is <= nums2's right part)
    //   - nums2[j-1] <= nums1[i]    (everything in nums2's left part is <= nums1's right part)
    // then we have found the correct partition, and the median can be read directly
    // from the four boundary values (treating out-of-bounds indices as -infinity/+infinity).
    //
    // Worked example: nums1 = [1,3], nums2 = [2] (m=2, n=1, total=3, left half size = 2)
    //   Try i = 1 (cut nums1 after 1 element: left={1}, right={3}), then
    //   j = (2+1+1)/2 - i = 2 - 1 = 1 (cut nums2 after 1 element: left={2}, right={})
    //   Check: nums1[i-1]=1 <= nums2[j]=+inf (ok), nums2[j-1]=2 <= nums1[i]=3 (ok) -> valid!
    //   Total length is odd (3), so median = max(left parts) = max(1, 2) = 2. Matches expected.
    //
    // Worked example: nums1 = [1,2], nums2 = [3,4] (m=2, n=2, total=4, left half size = 2)
    //   Try i = 0 (nums1 left={}, right={1,2}), j = (2+2+1)/2 - 0 = 2 (nums2 left={3,4}, right={})
    //   Check: nums1[i-1]=-inf <= nums2[j]=+inf (ok), nums2[j-1]=4 <= nums1[i]=1 (FAILS, 4 > 1)
    //   -> partition invalid, i is too small, need to move i to the right.
    //   Try i = 1 (nums1 left={1}, right={2}), j = 2 - 1 = 1 (nums2 left={3}, right={4})
    //   Check: nums1[i-1]=1 <= nums2[j]=4 (ok), nums2[j-1]=3 <= nums1[i]=2 (FAILS, 3 > 2)
    //   -> still too small on the nums2 side, keep moving i right.
    //   Try i = 2 (nums1 left={1,2}, right={}), j = 2 - 2 = 0 (nums2 left={}, right={3,4})
    //   Check: nums1[i-1]=2 <= nums2[j]=3 (ok), nums2[j-1]=-inf <= nums1[i]=+inf (ok) -> valid!
    //   Total length is even (4), so median = (max(left) + min(right)) / 2 = (max(2,-inf) + min(+inf,3)) / 2 = (2+3)/2 = 2.5. Matches expected.
    //
    // Binary search drives the choice of i: start with lo=0, hi=m (size of the SMALLER
    // array). If nums2[j-1] > nums1[i] (nums2's left part has something too big), i is
    // too small, so move lo = i + 1. If nums1[i-1] > nums2[j] (nums1's left part has
    // something too big), i is too large, so move hi = i - 1. Repeat until valid.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure is used - the key insight is that "i" (the cut
    // point in the smaller array) is a monotonic search variable: as i increases,
    // nums1's left part grows and nums2's left part shrinks, so the two validity
    // checks move in predictable, opposite directions. That monotonicity is exactly
    // what makes binary search applicable here, even though we're searching over
    // "partition points" rather than over array values directly. Always binary
    // searching on the SMALLER array bounds the search space to O(log(min(m,n))),
    // which is the whole point of this approach over merging.
    //
    // ================= EDGE CASES =================
    // - One array is empty: median is simply the median of the other array.
    // - Arrays of very different sizes, e.g. nums1 has 1 element, nums2 has 1000.
    // - Combined length is odd vs even (changes the median formula).
    // - Duplicate values across the two arrays, e.g. nums1 = [2,2], nums2 = [2,2].
    // - All elements of one array are smaller than all elements of the other.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force merge O(m + n) - every element from both arrays
    // is visited once during the merge.
    // Optimized O(log(min(m,n))) - binary search only over the smaller array's index
    // range, with O(1) work per iteration.
    // Space Complexity: Brute force O(m + n) for the merged array.
    // Optimized O(1) - only a handful of index and boundary variables are used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must the binary search always run on the SMALLER array? What happens (correctness and complexity) if you binary search on the larger one instead?
    // - Walk through why i + j = (m + n + 1) / 2 gives the correct left-half size for both odd and even total lengths.
    // - How do you handle the -infinity / +infinity boundary conditions in code cleanly?
    // - How would you generalize this to find the k-th smallest element across two sorted arrays (median is just the special case k = middle)?
    // - How would this extend to finding the median across MORE than two sorted arrays?
    // - What if the arrays could contain duplicates spanning across both arrays - does the algorithm still work unchanged?
    // - Can you prove that a valid partition (i, j) always exists for any two sorted arrays?

    // Brute force: merge both arrays, then read off the middle, O(m + n).
    public static double findMedianBruteForce(int[] nums1, int[] nums2) {
        int m = nums1.length;
        int n = nums2.length;
        int[] merged = new int[m + n];

        // classic merge step, like merge sort's merge
        int i = 0, j = 0, k = 0;
        while (i < m && j < n) {
            merged[k++] = (nums1[i] <= nums2[j]) ? nums1[i++] : nums2[j++];
        }
        while (i < m) {
            merged[k++] = nums1[i++];
        }
        while (j < n) {
            merged[k++] = nums2[j++];
        }

        int total = merged.length;
        if (total % 2 == 1) {
            return merged[total / 2]; // odd length, exact middle
        } else {
            return (merged[total / 2 - 1] + merged[total / 2]) / 2.0; // even, average of two middles
        }
    }

    // Optimized: binary search on the partition point in the smaller array, O(log(min(m,n))).
    public static double findMedianOptimized(int[] nums1, int[] nums2) {
        // always binary search on the smaller array to keep it O(log(min(m,n)))
        if (nums1.length > nums2.length) {
            return findMedianOptimized(nums2, nums1);
        }

        int m = nums1.length;
        int n = nums2.length;
        int lo = 0, hi = m;
        int halfLen = (m + n + 1) / 2; // size of the "left half" across both arrays

        while (lo <= hi) {
            int i = lo + (hi - lo) / 2; // cut point in nums1
            int j = halfLen - i;        // matching cut point in nums2

            // boundary values, treating out-of-range as -infinity / +infinity
            int nums1LeftMax = (i == 0) ? Integer.MIN_VALUE : nums1[i - 1];
            int nums1RightMin = (i == m) ? Integer.MAX_VALUE : nums1[i];
            int nums2LeftMax = (j == 0) ? Integer.MIN_VALUE : nums2[j - 1];
            int nums2RightMin = (j == n) ? Integer.MAX_VALUE : nums2[j];

            if (nums1LeftMax <= nums2RightMin && nums2LeftMax <= nums1RightMin) {
                // valid partition found
                if ((m + n) % 2 == 1) {
                    return Math.max(nums1LeftMax, nums2LeftMax); // odd total, middle element
                } else {
                    return (Math.max(nums1LeftMax, nums2LeftMax) + Math.min(nums1RightMin, nums2RightMin)) / 2.0;
                }
            } else if (nums1LeftMax > nums2RightMin) {
                // i is too large, move it left
                hi = i - 1;
            } else {
                // i is too small, move it right
                lo = i + 1;
            }
        }

        throw new IllegalArgumentException("Input arrays are not sorted correctly");
    }

    public static void main(String[] args) {
        int[] a1 = {1, 3};
        int[] b1 = {2};
        // Expected: 2.0
        System.out.println("nums1 = " + Arrays.toString(a1) + ", nums2 = " + Arrays.toString(b1));
        System.out.println("Brute force: " + findMedianBruteForce(a1, b1));
        System.out.println("Optimized:   " + findMedianOptimized(a1, b1));

        int[] a2 = {1, 2};
        int[] b2 = {3, 4};
        // Expected: 2.5
        System.out.println("\nnums1 = " + Arrays.toString(a2) + ", nums2 = " + Arrays.toString(b2));
        System.out.println("Brute force: " + findMedianBruteForce(a2, b2));
        System.out.println("Optimized:   " + findMedianOptimized(a2, b2));

        // Edge case: one array is empty
        int[] a3 = {};
        int[] b3 = {1, 2, 3, 4, 5};
        // Expected: 3.0 (median of [1,2,3,4,5])
        System.out.println("\nnums1 = [], nums2 = " + Arrays.toString(b3));
        System.out.println("Brute force: " + findMedianBruteForce(a3, b3));
        System.out.println("Optimized:   " + findMedianOptimized(a3, b3));
    }
}
