package com.playground.java.interview.matrix;

import java.util.Arrays;

/**
 * PATTERN: Matrix / In-Place Transformation
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an n x n matrix, rotate it 90 degrees clockwise in place.
 */
public class RotateImage {

    // ================= PROBLEM =================
    // You are given a square (n x n) matrix representing an image. Rotate it 90 degrees
    // clockwise, and you must do this in place (without allocating another n x n matrix).
    // Example: matrix = [[1,2,3],
    //                     [4,5,6],
    //                     [7,8,9]]
    //          -> output = [[7,4,1],
    //                        [8,5,2],
    //                        [9,6,3]]
    //
    // ================= SIMPLE APPROACH =================
    // Create a brand new n x n matrix. For every cell (row, col) in the original matrix, copy
    // its value into the new matrix at position (col, n-1-row), which is where it belongs after
    // a 90 degree clockwise rotation. Then copy the new matrix back over the original.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This uses O(n^2) extra space for the new matrix, but the problem explicitly asks for an
    // in-place rotation using only O(1) extra space.
    //
    // ================= OPTIMIZED APPROACH =================
    // Do it in two simple in-place steps:
    // 1) Transpose the matrix: swap matrix[i][j] with matrix[j][i] for all i < j. This flips
    //    the matrix across its main diagonal (rows become columns).
    // 2) Reverse each row: for every row, swap the elements from left and right ends moving
    //    inward. This flips the matrix horizontally.
    // Doing a transpose followed by a horizontal row reversal is mathematically equivalent to a
    // 90 degree clockwise rotation.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure is needed at all - the matrix is rotated purely through
    // in-place swaps (transpose swaps, then row-reversal swaps), which is exactly what achieves
    // O(1) extra space. Geometrically: transposing reflects across the main diagonal, and then
    // reversing each row reflects left-right; composing a diagonal reflection with a horizontal
    // reflection produces a 90 degree clockwise rotation.
    //
    // ================= EDGE CASES =================
    // - 1x1 matrix: rotating does nothing, matrix stays the same.
    // - 2x2 matrix: smallest case where rotation is visible.
    // - Matrix with all identical values: rotation is a no-op visually, but the algorithm still runs correctly.
    // - Non-square matrix: this transpose-then-reverse trick only works for square (n x n) matrices; a rectangular matrix would need a different (out-of-place) approach.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2) - every cell is visited a constant number of times across the
    // transpose and row-reversal steps.
    // Space Complexity: O(1) - all swaps happen in place on the original matrix, no extra
    // matrix is allocated.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you rotate the matrix 90 degrees counter-clockwise instead (hint: reverse columns then transpose, or transpose then reverse each column)?
    // - How would you rotate by 180 degrees in place?
    // - Why does transpose + reverse-rows specifically produce a CLOCKWISE rotation and not counter-clockwise?
    // - How would this approach change for a non-square (m x n) matrix, where in-place rotation isn't straightforward?
    // - Can you rotate the matrix by processing it layer by layer (four-way swap of elements in concentric square rings) instead of transpose+reverse?
    // - How would you verify your in-place rotation is correct without using extra space for validation?

    // Optimized: transpose the matrix, then reverse each row, both in place.
    public static void rotate(int[][] matrix) {
        int n = matrix.length;

        // Step 1: transpose - swap matrix[i][j] with matrix[j][i] for all i < j.
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }

        // Step 2: reverse each row - swap left and right ends moving inward.
        for (int i = 0; i < n; i++) {
            int left = 0;
            int right = n - 1;
            while (left < right) {
                int temp = matrix[i][left];
                matrix[i][left] = matrix[i][right];
                matrix[i][right] = temp;
                left++;
                right--;
            }
        }
    }

    public static void main(String[] args) {
        int[][] matrix1 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        // Expected: [[7, 4, 1], [8, 5, 2], [9, 6, 3]]
        System.out.println("Input: [[1,2,3],[4,5,6],[7,8,9]]");
        rotate(matrix1);
        System.out.println("Output: " + Arrays.deepToString(matrix1));

        int[][] matrix2 = {{5, 1}, {2, 8}};
        // Expected: [[2, 5], [8, 1]]
        System.out.println("\nInput: [[5,1],[2,8]]");
        rotate(matrix2);
        System.out.println("Output: " + Arrays.deepToString(matrix2));

        int[][] matrix3 = {{7}};
        // Expected: [[7]] (1x1 matrix, no visible change)
        System.out.println("\nInput: [[7]] (1x1)");
        rotate(matrix3);
        System.out.println("Output: " + Arrays.deepToString(matrix3));
    }
}
