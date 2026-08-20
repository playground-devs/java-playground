package com.playground.java.interview.matrix;

import java.util.Arrays;

/**
 * PATTERN: Matrix / In-Place Marking
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an m x n matrix, if an element is 0, set its entire row and column
 * to 0, and do it in place using O(1) extra space.
 */
public class SetMatrixZeroes {

    // ================= PROBLEM =================
    // You are given a matrix of integers. Whenever you find a cell with value 0, every other
    // cell in that same row AND that same column must also become 0. Do this in place.
    // Example: matrix = [[1,1,1],
    //                     [1,0,1],
    //                     [1,1,1]]
    //          -> output = [[1,0,1],
    //                        [0,0,0],
    //                        [1,0,1]]
    // because the 0 at (1,1) zeroes out row 1 and column 1 entirely.
    //
    // ================= SIMPLE APPROACH =================
    // Make a full copy of the original matrix. Scan the COPY for zeroes, and for every zero
    // found at (row, col), set the entire row and column to 0 in the ORIGINAL matrix. Using a
    // copy avoids the problem of newly-created zeroes being mistaken for original zeroes.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This uses O(m*n) extra space for the full copy, which does not meet the O(1) extra space
    // constraint the problem typically asks for.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use the matrix's own first row and first column as marker arrays instead of allocating a
    // new one:
    // 1) First, check separately whether the first row itself contains any zero, and whether
    //    the first column itself contains any zero - remember these two facts in two booleans,
    //    since we're about to reuse row 0 and column 0 as scratch space.
    // 2) Scan the rest of the matrix (from row 1, col 1 onward). Whenever matrix[i][j] == 0,
    //    mark matrix[i][0] = 0 (flag "row i has a zero") and matrix[0][j] = 0 (flag "column j
    //    has a zero").
    // 3) Scan the rest of the matrix again (from row 1, col 1 onward). For each cell, if its
    //    row marker (matrix[i][0]) or column marker (matrix[0][j]) is 0, zero out that cell.
    // 4) Finally, using the two booleans from step 1, zero out the first row and/or first
    //    column if they originally contained a zero.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary array is needed - the first row and first column of the matrix itself double
    // as the "marker" bookkeeping we would otherwise need a separate boolean array for. This is
    // what brings the extra space down from O(m+n) (two marker arrays) to O(1) (two booleans).
    //
    // ================= EDGE CASES =================
    // - Matrix with no zeroes at all: nothing changes.
    // - Zero in the first row or first column: must be handled carefully since those cells
    //   double as markers - hence the two separate "firstRowHasZero"/"firstColHasZero" booleans
    //   checked BEFORE the markers get overwritten.
    // - Entire matrix is zeroes already: stays all zeroes.
    // - 1x1 matrix: if it's 0, it stays 0; if not, it stays unchanged.
    // - Multiple zeroes in the same row or column: markers just get set to 0 multiple times, no issue.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m*n) - a constant number of full passes over the matrix (marking,
    // then zeroing based on markers).
    // Space Complexity: O(1) extra space - only two boolean flags are used; the matrix itself
    // stores the marker information.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is it necessary to check "does the first row/column originally have a zero" BEFORE using them as markers, and not after?
    // - How would you solve this if you could NOT modify the input matrix at all (must return a new matrix)?
    // - Could you solve this using a single integer/bitmask flag instead of two booleans? Why not (rows and columns are independent conditions)?
    // - How would you adapt the approach if the matrix contained sentinel values that must not be treated as "zero"?
    // - Walk through why processing "mark first" then "zero out based on markers" in two separate passes is necessary (why can't they be one pass)?

    // Optimized: use the first row and first column as in-place marker arrays.
    public static void setZeroes(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Step 1: remember (before overwriting) whether row 0 / column 0 originally had a zero.
        boolean firstRowHasZero = false;
        boolean firstColHasZero = false;
        for (int col = 0; col < cols; col++) {
            if (matrix[0][col] == 0) {
                firstRowHasZero = true;
                break;
            }
        }
        for (int row = 0; row < rows; row++) {
            if (matrix[row][0] == 0) {
                firstColHasZero = true;
                break;
            }
        }

        // Step 2: use first row/column as markers for zeroes found in the rest of the matrix.
        for (int row = 1; row < rows; row++) {
            for (int col = 1; col < cols; col++) {
                if (matrix[row][col] == 0) {
                    matrix[row][0] = 0;
                    matrix[0][col] = 0;
                }
            }
        }

        // Step 3: zero out cells whose row or column marker indicates a zero.
        for (int row = 1; row < rows; row++) {
            for (int col = 1; col < cols; col++) {
                if (matrix[row][0] == 0 || matrix[0][col] == 0) {
                    matrix[row][col] = 0;
                }
            }
        }

        // Step 4: finally handle the first row and first column themselves.
        if (firstRowHasZero) {
            Arrays.fill(matrix[0], 0);
        }
        if (firstColHasZero) {
            for (int row = 0; row < rows; row++) {
                matrix[row][0] = 0;
            }
        }
    }

    public static void main(String[] args) {
        int[][] matrix1 = {{1, 1, 1}, {1, 0, 1}, {1, 1, 1}};
        // Expected: [[1, 0, 1], [0, 0, 0], [1, 0, 1]]
        System.out.println("Input: [[1,1,1],[1,0,1],[1,1,1]]");
        setZeroes(matrix1);
        System.out.println("Output: " + Arrays.deepToString(matrix1));

        int[][] matrix2 = {{0, 1, 2, 0}, {3, 4, 5, 2}, {1, 3, 1, 5}};
        // Expected: [[0, 0, 0, 0], [0, 4, 5, 0], [0, 3, 1, 0]]
        System.out.println("\nInput: [[0,1,2,0],[3,4,5,2],[1,3,1,5]]");
        setZeroes(matrix2);
        System.out.println("Output: " + Arrays.deepToString(matrix2));

        int[][] matrix3 = {{1}};
        // Expected: [[1]] (no zero present)
        System.out.println("\nInput: [[1]] (no zero, 1x1)");
        setZeroes(matrix3);
        System.out.println("Output: " + Arrays.deepToString(matrix3));
    }
}
