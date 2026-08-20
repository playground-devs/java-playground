package com.playground.java.interview.sorting;

import java.util.Arrays;

/**
 * PATTERN: Divide and Conquer / Sorting
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Sort an array of integers in ascending order using merge sort from scratch.
 */
public class MergeSortImplementation {

    // ================= PROBLEM =================
    // Given an unsorted array of integers, sort it in ascending order.
    // Example: nums = [5, 2, 4, 6, 1, 3] -> Output: [1, 2, 3, 4, 5, 6]
    //
    // ================= SIMPLE APPROACH =================
    // A naive approach is to use a simple O(n^2) sort like bubble sort or insertion sort:
    // repeatedly scan the array and swap adjacent out-of-order elements until nothing
    // changes, or repeatedly insert each element into its correct position among the
    // already-sorted prefix.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // O(n^2) sorts compare/swap roughly n*n times in the worst case. For small arrays
    // this is fine, but for large inputs (say n = 1,000,000) that is a trillion
    // operations - completely impractical. We need an approach that scales better than
    // quadratic time.
    //
    // ================= OPTIMIZED APPROACH =================
    // Merge sort uses divide and conquer:
    // Step 1: If the array has 0 or 1 elements, it's already sorted - base case, return.
    // Step 2: Split the array into two halves at the midpoint.
    // Step 3: Recursively merge sort the left half and the right half.
    // Step 4: Merge the two now-sorted halves back together into one sorted array by
    //          repeatedly comparing the front of each half and picking the smaller one.
    // Each merge step is O(n), and we do O(log n) levels of splitting, giving O(n log n)
    // overall.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Merge sort works on plain arrays (or linked lists) and only ever needs sequential
    // access - it never needs random access/swaps like quicksort's partitioning does.
    // This makes merge sort naturally suited to linked lists (no O(n) random access
    // penalty) and to external sorting (sorting data too big to fit in memory, e.g. on
    // disk or across machines), because it processes data in sequential chunks that can
    // be merged from separate files/streams. Merge sort is also STABLE - equal elements
    // keep their original relative order, because during the merge step we always take
    // from the left half when values are equal (never skip past a tie), which matters
    // when sorting objects by one field while preserving order by another.
    //
    // ================= EDGE CASES =================
    // - Empty array: should return immediately (nothing to sort).
    // - Single element array: already sorted, base case.
    // - Array with all identical elements: merge sort still works correctly and stays stable.
    // - Array already sorted: still O(n log n), no early-exit optimization by default.
    // - Array sorted in reverse order: still O(n log n), unlike quicksort which can degrade.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) in all cases (best, average, worst) - the array is
    // always split into log n levels, and each level does O(n) work to merge.
    // Space Complexity: O(n) extra space for the temporary arrays used during merging
    // (plus O(log n) recursion stack space).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is merge sort considered stable, and why does that matter in practice?
    // - How would you sort a linked list using merge sort in O(1) extra space (no array copies)?
    // - What is external sorting, and why is merge sort the algorithm of choice for it?
    // - Can you make merge sort in-place? What's the trade-off (complexity vs space)?
    // - How does merge sort compare to quicksort in terms of worst-case guarantees?
    // - How would you count the number of inversions in an array using a modified merge sort?
    // - Could you parallelize merge sort? Which part naturally splits across threads?

    // Naive baseline: insertion sort, O(n^2) time, O(1) extra space.
    public static int[] insertionSortBaseline(int[] arr) {
        int[] result = arr.clone();
        // Step 1: for each element, shift it left into its correct position.
        for (int i = 1; i < result.length; i++) {
            int key = result[i];
            int j = i - 1;
            while (j >= 0 && result[j] > key) {
                result[j + 1] = result[j];
                j--;
            }
            result[j + 1] = key;
        }
        return result;
    }

    // Optimized: merge sort, O(n log n) time, O(n) space.
    public static int[] mergeSort(int[] arr) {
        int[] result = arr.clone();
        if (result.length <= 1) {
            return result; // base case: already sorted
        }
        mergeSortHelper(result, 0, result.length - 1);
        return result;
    }

    private static void mergeSortHelper(int[] arr, int left, int right) {
        if (left >= right) {
            return; // base case: 0 or 1 elements
        }
        int mid = left + (right - left) / 2;
        // Step 1: recursively sort left half.
        mergeSortHelper(arr, left, mid);
        // Step 2: recursively sort right half.
        mergeSortHelper(arr, mid + 1, right);
        // Step 3: merge the two sorted halves.
        merge(arr, left, mid, right);
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int[] leftHalf = Arrays.copyOfRange(arr, left, mid + 1);
        int[] rightHalf = Arrays.copyOfRange(arr, mid + 1, right + 1);

        int i = 0; // pointer into leftHalf
        int j = 0; // pointer into rightHalf
        int k = left; // pointer into arr (destination)

        // Step 4: pick the smaller front element from each half.
        while (i < leftHalf.length && j < rightHalf.length) {
            if (leftHalf[i] <= rightHalf[j]) { // <= keeps sort stable (left wins ties)
                arr[k++] = leftHalf[i++];
            } else {
                arr[k++] = rightHalf[j++];
            }
        }
        // Step 5: copy any leftover elements (already sorted).
        while (i < leftHalf.length) {
            arr[k++] = leftHalf[i++];
        }
        while (j < rightHalf.length) {
            arr[k++] = rightHalf[j++];
        }
    }

    public static void main(String[] args) {
        int[] nums1 = {5, 2, 4, 6, 1, 3};
        System.out.println("Input: " + Arrays.toString(nums1));
        // Expected: [1, 2, 3, 4, 5, 6]
        System.out.println("Merge sort: " + Arrays.toString(mergeSort(nums1)));
        System.out.println("Insertion sort baseline: " + Arrays.toString(insertionSortBaseline(nums1)));

        int[] nums2 = {};
        System.out.println("\nInput: " + Arrays.toString(nums2));
        // Expected: []
        System.out.println("Merge sort: " + Arrays.toString(mergeSort(nums2)));

        int[] nums3 = {9, 9, 3, 3, 1};
        System.out.println("\nInput: " + Arrays.toString(nums3));
        // Expected: [1, 3, 3, 9, 9]
        System.out.println("Merge sort: " + Arrays.toString(mergeSort(nums3)));
    }
}
