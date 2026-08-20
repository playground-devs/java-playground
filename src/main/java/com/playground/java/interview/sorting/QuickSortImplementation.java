package com.playground.java.interview.sorting;

import java.util.Arrays;
import java.util.Random;

/**
 * PATTERN: Divide and Conquer / Sorting
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Sort an array of integers in ascending order using quicksort from scratch with a randomized pivot.
 */
public class QuickSortImplementation {

    // ================= PROBLEM =================
    // Given an unsorted array of integers, sort it in ascending order, in place.
    // Example: nums = [5, 2, 4, 6, 1, 3] -> Output: [1, 2, 3, 4, 5, 6]
    //
    // ================= SIMPLE APPROACH =================
    // A naive approach is a simple O(n^2) sort like insertion sort or bubble sort:
    // repeatedly compare and swap adjacent elements, or insert each element into its
    // correct spot among the already-sorted prefix.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // O(n^2) sorts don't scale for large inputs - a million elements means roughly a
    // trillion comparisons in the worst case. We want an in-place algorithm that is
    // close to O(n log n) on average without needing O(n) extra space like merge sort.
    //
    // ================= OPTIMIZED APPROACH =================
    // Quicksort uses divide and conquer via partitioning:
    // Step 1: If the sub-array has 0 or 1 elements, it's already sorted - base case.
    // Step 2: Pick a pivot element (here: a RANDOM index, swapped to the end).
    // Step 3: Partition the sub-array (Lomuto scheme) so everything <= pivot ends up to
    //          its left, and everything > pivot ends up to its right. The pivot lands in
    //          its final sorted position.
    // Step 4: Recursively quicksort the left part and the right part independently.
    // On average, each partition splits the array roughly in half, giving O(n log n).
    //
    // Why worst case O(n^2) happens: if the pivot is always the smallest or largest
    // remaining element (e.g. always picking the first or last element as pivot on an
    // ALREADY SORTED array), every partition splits the array into "0 elements" and
    // "n-1 elements" instead of two roughly equal halves. That gives n levels of
    // recursion instead of log n, each doing O(n) work -> O(n^2) total.
    //
    // How randomizing the pivot mitigates it: by picking a uniformly random index as the
    // pivot each time, we make it extremely unlikely (regardless of the input's original
    // order) that we repeatedly pick the worst possible pivot. This turns the worst case
    // into a rare, low-probability event rather than something triggered deterministically
    // by common inputs like already-sorted or reverse-sorted arrays.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Quicksort operates directly on the array using in-place swaps and random access by
    // index - something arrays support in O(1) but linked lists do not, which is why
    // quicksort is preferred for arrays and merge sort is preferred for linked lists.
    // No auxiliary array is needed (unlike merge sort's temporary arrays), so quicksort
    // is typically faster in practice for arrays due to better cache locality and lower
    // constant factors, at the cost of losing merge sort's stability and worst-case
    // guarantee.
    //
    // ================= EDGE CASES =================
    // - Empty array or single element: base case, nothing to do.
    // - Array already sorted or reverse sorted: handled well thanks to random pivot
    //   (without randomization, this is exactly the worst-case trigger).
    // - Array with all identical elements: partitioning still terminates correctly,
    //   though Lomuto scheme can degrade toward O(n^2) on all-equal arrays unless a
    //   3-way partition is used (mentioned in follow-ups).
    // - Very small arrays (0-1 elements after recursive splits): recursion base case.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Average case O(n log n) - random pivot makes balanced splits
    // likely. Worst case O(n^2) - only if pivots are consistently the min/max of the
    // remaining range (astronomically unlikely with true randomization).
    // Space Complexity: O(log n) average for the recursion stack (balanced splits);
    // O(n) worst case recursion stack if splits are maximally unbalanced. Sorting is
    // done in place, so no auxiliary array is needed (unlike merge sort).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does randomizing the pivot help avoid worst-case behavior?
    // - What is 3-way partitioning (Dutch National Flag) and how does it help with many duplicate values?
    // - Compare Lomuto vs Hoare partition schemes - which does fewer swaps on average?
    // - Is quicksort stable? Why or why not, and does it matter here?
    // - How would you make quicksort iterative instead of recursive to avoid stack overflow on adversarial inputs?
    // - Why is quicksort often faster in practice than merge sort despite the same average time complexity?
    // - How does introsort (used by many standard library sorts) combine quicksort with heapsort to guarantee O(n log n) worst case?

    private static final Random RANDOM = new Random();

    // Optimized: quicksort with randomized pivot, Lomuto partition scheme.
    public static int[] quickSort(int[] arr) {
        int[] result = arr.clone();
        quickSortHelper(result, 0, result.length - 1);
        return result;
    }

    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low >= high) {
            return; // base case: 0 or 1 elements
        }
        int pivotIndex = partition(arr, low, high);
        // Step: recursively sort the parts left and right of the pivot's final position.
        quickSortHelper(arr, low, pivotIndex - 1);
        quickSortHelper(arr, pivotIndex + 1, high);
    }

    private static int partition(int[] arr, int low, int high) {
        // Step 1: pick a random index in [low, high] and swap it to the end as the pivot.
        int randomIndex = low + RANDOM.nextInt(high - low + 1);
        swap(arr, randomIndex, high);
        int pivot = arr[high];

        // Step 2: Lomuto partition - i tracks the boundary of elements <= pivot.
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        // Step 3: place the pivot right after the last element <= pivot.
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void swap(int[] arr, int a, int b) {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    public static void main(String[] args) {
        int[] nums1 = {5, 2, 4, 6, 1, 3};
        System.out.println("Input: " + Arrays.toString(nums1));
        // Expected: [1, 2, 3, 4, 5, 6]
        System.out.println("Quicksort: " + Arrays.toString(quickSort(nums1)));

        int[] nums2 = {1, 2, 3, 4, 5}; // already sorted - worst case without random pivot
        System.out.println("\nInput (already sorted): " + Arrays.toString(nums2));
        // Expected: [1, 2, 3, 4, 5]
        System.out.println("Quicksort: " + Arrays.toString(quickSort(nums2)));

        int[] nums3 = {};
        System.out.println("\nInput: " + Arrays.toString(nums3));
        // Expected: []
        System.out.println("Quicksort: " + Arrays.toString(quickSort(nums3)));

        int[] nums4 = {7, 7, 7, 7};
        System.out.println("\nInput (all duplicates): " + Arrays.toString(nums4));
        // Expected: [7, 7, 7, 7]
        System.out.println("Quicksort: " + Arrays.toString(quickSort(nums4)));
    }
}
