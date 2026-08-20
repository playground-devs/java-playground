package com.playground.java.interview.sorting;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * PATTERN: Heap (Priority Queue) / Binary Search on Answer
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Find the k-th smallest element in an n x n matrix where every row and column is sorted ascending.
 */
public class KthSmallestInSortedMatrix {

    // ================= PROBLEM =================
    // Given an n x n matrix where each row is sorted ascending left-to-right and each
    // column is sorted ascending top-to-bottom, find the k-th smallest element overall.
    // Example: matrix = [[1,5,9],[10,11,13],[12,13,15]], k = 8 -> Output: 13
    // (all values sorted: 1,5,9,10,11,12,13,13,15 - the 8th smallest is 13)
    //
    // ================= SIMPLE APPROACH =================
    // Flatten every element of the matrix into a single array, sort that array, then
    // return the element at index k-1.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This completely ignores that rows and columns are already sorted. Sorting all
    // n*n elements costs O(n^2 log n), which is more work than necessary - we don't
    // need the WHOLE matrix sorted, just enough information to identify the k-th
    // smallest value.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a max-heap of size k (mirrors the "k-th largest via min-heap" pattern, flipped):
    // Step 1: Walk through every element in the matrix.
    // Step 2: Push each element onto a max-heap.
    // Step 3: Whenever the heap size exceeds k, pop the largest element - this keeps only
    //          the k SMALLEST elements seen so far in the heap.
    // Step 4: After processing the whole matrix, the top of the max-heap (the largest of
    //          the k smallest values kept) is exactly the k-th smallest element overall.
    //
    // (Interview follow-up note: a further optimization uses BINARY SEARCH ON THE VALUE
    // RANGE instead of a heap: binary search between matrix[0][0] (min) and
    // matrix[n-1][n-1] (max). For a candidate mid value, count how many elements in the
    // matrix are <= mid using a staircase walk starting at the bottom-left corner
    // (move right if the current element <= mid, otherwise move up) - this count takes
    // O(n) since the staircase visits at most 2n cells. Binary search narrows the value
    // range until it converges on the k-th smallest value, giving O(n log(max-min))
    // overall, better than the heap approach when n is large. Not implemented in full
    // here, but worth mentioning as the "next level" answer in interviews.)
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A max-heap bounded to size k is the mirror image of the classic "k-th largest via
    // min-heap of size k" pattern: here we want the k SMALLEST elements, so we evict the
    // current largest whenever the heap overflows past k, keeping only "candidates for
    // smallest k". The top of that heap (largest of the kept k) is by definition the k-th
    // smallest overall. This avoids fully sorting all n^2 elements - heap operations are
    // bounded by size k instead of by the full element count.
    //
    // ================= EDGE CASES =================
    // - k = 1: answer is simply the minimum element, matrix[0][0].
    // - k = n*n: answer is the maximum element, matrix[n-1][n-1].
    // - 1x1 matrix: trivially returns that single element regardless of k (k must be 1).
    // - Matrix with duplicate values: k-th smallest counts duplicates by position, not
    //   distinct values (e.g. [[1,1],[1,1]], k=3 -> 1).
    // - Non-square considerations: this problem assumes n x n; a rectangular matrix would
    //   need the row/column counts handled separately.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n^2 log n) because we sort all n^2 elements.
    // Optimized max-heap of size k: O(n^2 log k) because we process all n^2 elements,
    // each heap insertion/removal costing O(log k).
    // (Follow-up) Binary search on value range: O(n log(max-min)) because each binary
    // search step does an O(n) staircase count, and there are O(log(max-min)) steps.
    // Space Complexity: Brute force O(n^2) to hold the flattened, sorted array.
    // Optimized max-heap: O(k) extra space for the heap.
    // Binary search on value range: O(1) extra space (just a few counters/pointers).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Walk through the binary-search-on-value-range approach and the staircase counting trick in detail.
    // - Why does the staircase walk from the bottom-left corner correctly count elements <= mid in O(n)?
    // - How would you find the k-th LARGEST element instead? What changes?
    // - What if the matrix were rectangular (m x n) instead of square?
    // - How would this change if you needed the k-th smallest across multiple separate sorted matrices?
    // - Is there a way to solve this without any heap or binary search, purely using the sorted structure directly (e.g. merging sorted rows like "merge k sorted lists")? What would its complexity be?
    // - How would you adapt this to support duplicate-value matrices where you need the k-th DISTINCT smallest value?

    // Brute force: flatten, sort, index. O(n^2 log n).
    public static int kthSmallestBruteForce(int[][] matrix, int k) {
        int n = matrix.length;
        int[] flattened = new int[n * n];
        int idx = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                flattened[idx++] = val;
            }
        }
        Arrays.sort(flattened);
        return flattened[k - 1];
    }

    // Optimized: max-heap of size k. O(n^2 log k).
    public static int kthSmallestMaxHeap(int[][] matrix, int k) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        for (int[] row : matrix) {
            for (int val : row) {
                maxHeap.offer(val); // add current element
                if (maxHeap.size() > k) {
                    maxHeap.poll(); // evict current largest, keep only k smallest so far
                }
            }
        }
        // top of max-heap is the largest among the k smallest kept -> k-th smallest overall
        return maxHeap.peek();
    }

    public static void main(String[] args) {
        int[][] matrix1 = {{1, 5, 9}, {10, 11, 13}, {12, 13, 15}};
        int k1 = 8;
        System.out.println("Input: matrix=[[1,5,9],[10,11,13],[12,13,15]], k=" + k1);
        // Expected: 13 (sorted: 1,5,9,10,11,12,13,13,15 -> 8th smallest is 13)
        System.out.println("Brute force: " + kthSmallestBruteForce(matrix1, k1));
        System.out.println("Max-heap:    " + kthSmallestMaxHeap(matrix1, k1));

        int[][] matrix2 = {{-5}};
        int k2 = 1;
        System.out.println("\nInput: matrix=[[-5]], k=" + k2);
        // Expected: -5 (1x1 matrix)
        System.out.println("Brute force: " + kthSmallestBruteForce(matrix2, k2));
        System.out.println("Max-heap:    " + kthSmallestMaxHeap(matrix2, k2));

        int[][] matrix3 = {{1, 1}, {1, 1}};
        int k3 = 3;
        System.out.println("\nInput: matrix=[[1,1],[1,1]], k=" + k3);
        // Expected: 1 (all duplicates, k-th position still 1)
        System.out.println("Brute force: " + kthSmallestBruteForce(matrix3, k3));
        System.out.println("Max-heap:    " + kthSmallestMaxHeap(matrix3, k3));
    }
}
