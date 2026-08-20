package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 2D DP on a Grid
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a binary matrix (containing only 0s and 1s), find the largest
 * square containing only 1s and return its area.
 */
public class MaximalSquare {

    // ================= PROBLEM =================
    // You are given an m x n matrix filled with '0' and '1' characters. Find the largest square
    // submatrix that contains only '1's, and return its area (side length squared).
    // Example: matrix = [[1,0,1,0,0],
    //                     [1,0,1,1,1],
    //                     [1,1,1,1,1],
    //                     [1,0,0,1,0]]
    //          -> output = 4 (a 2x2 square of 1s exists, area = 4)
    //
    // ================= SIMPLE APPROACH =================
    // For every cell as a potential top-left (or bottom-right) corner, and for every possible
    // square size starting from 1 upward, check whether the entire square of that size is filled
    // with 1s. Keep expanding the size until a 0 is found or the boundary is exceeded, tracking
    // the largest valid square size seen.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // For each of the O(m*n) starting cells, checking a square of size k takes O(k^2) work, and
    // we may try up to O(min(m,n)) sizes - this brute force is O(m*n*min(m,n)^2) or worse in the
    // worst case, which is far too slow for large grids.
    //
    // ================= OPTIMIZED APPROACH =================
    // Dynamic programming: let dp[i][j] = the side length of the largest square of 1s whose
    // BOTTOM-RIGHT corner is exactly at cell (i, j).
    // - If matrix[i][j] == '0', dp[i][j] = 0 (no square of 1s can end here).
    // - If matrix[i][j] == '1':
    //   - If i == 0 or j == 0 (first row or first column), dp[i][j] = 1 (a square can only be
    //     size 1 here, since it can't extend further up/left).
    //   - Otherwise, dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]). The intuition:
    //     a square of size k ending at (i,j) requires the cell directly above, directly left,
    //     AND diagonally up-left to ALL support at least a square of size k-1 - the smallest of
    //     those three determines how far the square can be safely extended, hence take the min
    //     and add 1 for the current cell itself.
    // Track the maximum dp[i][j] seen across the whole grid; the answer is that maximum, squared
    // (to convert side length into area).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 2D DP table where each cell's value depends only on its top, left, and top-left
    // neighbors captures exactly the constraint that a bigger square requires ALL three
    // "smaller square" possibilities around it to already support that size - this turns an
    // O(size^2) per-cell verification into an O(1) per-cell computation once the DP values for
    // earlier cells are known.
    //
    // ================= EDGE CASES =================
    // - Matrix with no 1s at all: answer is 0.
    // - Matrix that is entirely 1s: the largest square is min(rows, cols) in side length.
    // - Single row or single column: the largest possible square is always 1x1 (side length
    //   capped at 1) if any 1 exists, since dp[i][j]=1 whenever i==0 or j==0.
    // - 1x1 matrix: answer is 1 if it's '1', else 0.
    // - Matrix given as characters ('0'/'1') vs integers (0/1): must convert correctly when reading input.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m*n) - each cell's DP value is computed once in O(1) time from its
    // three neighbors, versus O(m*n*min(m,n)^2) for the brute force expanding-square check.
    // Space Complexity: O(m*n) for the DP table (can be optimized to O(n) using a rolling
    // single-row array, since each row's DP values only depend on the row directly above).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you reduce the space complexity from O(m*n) to O(n) using a rolling 1D array?
    // - How would you also return the actual location (top-left corner) of the largest square, not just its area?
    // - How would you solve the related "maximal RECTANGLE of 1s" problem (harder, typically uses a histogram + stack approach per row)?
    // - Why does the recurrence use min() of the three neighbors rather than, say, their average or max?
    // - How would you modify this DP if the matrix could be updated dynamically (cells flipping between 0 and 1) and you needed the answer after each update?
    // - Can you solve this problem in-place by reusing the input matrix itself as the DP table (careful with '0'/'1' vs int distinctions)?

    // Optimized: 2D DP where dp[i][j] = side length of the largest square ending at (i, j).
    public static int maximalSquare(char[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return 0;
        }

        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] dp = new int[rows][cols];
        int maxSide = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == '1') {
                    if (i == 0 || j == 0) {
                        // Step: first row/column - a square here can only have side length 1.
                        dp[i][j] = 1;
                    } else {
                        // Step: extend the smallest of the three neighboring squares by 1.
                        dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                    }
                    maxSide = Math.max(maxSide, dp[i][j]);
                }
                // else dp[i][j] stays 0 (default), since matrix[i][j] == '0'.
            }
        }

        return maxSide * maxSide;
    }

    public static void main(String[] args) {
        char[][] matrix1 = {
                {'1', '0', '1', '0', '0'},
                {'1', '0', '1', '1', '1'},
                {'1', '1', '1', '1', '1'},
                {'1', '0', '0', '1', '0'}
        };
        // Expected: 4 (a 2x2 square of 1s)
        System.out.println("Input: 4x5 binary matrix");
        System.out.println("Output: " + maximalSquare(matrix1));

        char[][] matrix2 = {{'0', '1'}, {'1', '0'}};
        // Expected: 1 (no 2x2 square, but single 1s exist)
        System.out.println("\nInput: [[0,1],[1,0]]");
        System.out.println("Output: " + maximalSquare(matrix2));

        char[][] matrix3 = {{'0'}};
        // Expected: 0 (no 1s at all)
        System.out.println("\nInput: [[0]] (single cell, no 1s)");
        System.out.println("Output: " + maximalSquare(matrix3));
    }
}
