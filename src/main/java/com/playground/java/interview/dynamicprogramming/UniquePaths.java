package com.playground.java.interview.dynamicprogramming;

/**
 * PATTERN: Dynamic Programming / 2D Grid Path Counting
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given an m x n grid, count the number of unique paths from the top-left
 * corner to the bottom-right corner, moving only right or down at each step.
 */
public class UniquePaths {

    // ================= PROBLEM =================
    // A robot is located at the top-left corner of an m x n grid. It can only move either down
    // or right at any point in time, trying to reach the bottom-right corner. Count how many
    // unique paths exist.
    // Example: m = 3, n = 7 -> output = 28
    // Example: m = 3, n = 2 -> output = 3
    //          (paths: Right->Down->Down, Down->Right->Down, Down->Down->Right)
    //
    // ================= SIMPLE APPROACH =================
    // Recursively try both moves (right and down) from the current cell, and sum the number of
    // paths found from each, until reaching the bottom-right corner (base case: 1 valid path) or
    // going out of bounds (base case: 0 paths).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The same "number of paths from cell (i, j) to the destination" subproblem is recomputed
    // many times through different routes that pass through the same cell, leading to
    // exponential O(2^(m+n)) time without memoization.
    //
    // ================= OPTIMIZED APPROACH =================
    // Bottom-up DP: let dp[i][j] = number of unique paths from the top-left corner to cell
    // (i, j).
    // - dp[0][j] = 1 for all j (only one way to reach any cell in the top row: keep moving right).
    // - dp[i][0] = 1 for all i (only one way to reach any cell in the left column: keep moving
    //   down).
    // - For i, j >= 1: dp[i][j] = dp[i-1][j] + dp[i][j-1] (arrived either from above or from the
    //   left).
    // The answer is dp[m-1][n-1].
    // Math alternative: this is equivalent to choosing which (m-1) of the total (m-1)+(n-1) moves
    // are "down" moves (the rest are "right" moves), so the answer can also be computed directly
    // as the binomial coefficient C(m+n-2, m-1) = (m+n-2)! / ((m-1)! * (n-1)!) - useful as an
    // O(m+n) (or O(min(m,n))) alternative if you only need the final count and not the DP table
    // itself.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A 2D array mirrors the grid itself directly, and each cell's value depends only on the
    // cell above and the cell to the left, so filling it row by row (or even collapsing to a
    // single rolling 1D row, since each row only needs the row above it) is both efficient and
    // easy to reason about, without needing any graph/queue structure since movement is
    // restricted to only two directions.
    //
    // ================= EDGE CASES =================
    // - m = 1 or n = 1 (a single row or single column): only 1 unique path exists (straight line).
    // - m = 1 and n = 1 (a 1x1 grid, start equals destination): exactly 1 path (staying put).
    // - Large grid (e.g. m = n = 100): answer can be a very large number - int may overflow for
    //   big enough grids, so long or BigInteger should be considered for very large inputs.
    // - Square grid vs. very rectangular grid: the DP handles both uniformly, no special casing
    //   needed.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(2^(m+n)) for brute force; O(m * n) for the DP version, one pass filling
    // the grid, O(1) work per cell.
    // Space Complexity: O(m * n) for the full DP table (can be optimized to O(n) using a single
    // rolling 1D row, since each row only depends on the row directly above it).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you optimize the O(m*n) space DP down to O(min(m,n)) using a rolling 1D array?
    // - How would you solve "Unique Paths II", where some grid cells contain obstacles that block movement?
    // - Why is the combinatorics formula C(m+n-2, m-1) mathematically equivalent to the DP recurrence?
    // - What precision/overflow issues arise from using the combinatorics formula directly with factorials for large m, n, and how would you avoid them?
    // - How would you extend this DP if diagonal moves were also allowed?
    // - How would you count unique paths with a maximum allowed number of "right" moves, rather than an unbounded grid?

    // Brute force: plain recursion trying right/down from each cell, O(2^(m+n)).
    public static int uniquePathsBruteForce(int m, int n) {
        return countFrom(m, n, 0, 0);
    }

    private static int countFrom(int m, int n, int row, int col) {
        if (row == m - 1 && col == n - 1) {
            return 1; // reached the bottom-right corner - one valid path
        }
        if (row >= m || col >= n) {
            return 0; // went out of bounds - not a valid path
        }

        // Step: try moving down, and try moving right, sum both counts.
        int moveDown = countFrom(m, n, row + 1, col);
        int moveRight = countFrom(m, n, row, col + 1);

        return moveDown + moveRight;
    }

    // Optimized: bottom-up 2D DP, dp[i][j] = paths from start to cell (i, j).
    public static int uniquePathsOptimized(int m, int n) {
        int[][] dp = new int[m][n];

        // Step: top row and left column each have exactly one path (a straight line).
        for (int j = 0; j < n; j++) {
            dp[0][j] = 1;
        }
        for (int i = 0; i < m; i++) {
            dp[i][0] = 1;
        }

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                // Step: paths to this cell = paths from above + paths from the left.
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }

        return dp[m - 1][n - 1];
    }

    public static void main(String[] args) {
        int m1 = 3;
        int n1 = 7;
        // Expected: 28
        System.out.println("Input: m=3, n=7");
        System.out.println("Output: " + uniquePathsOptimized(m1, n1));

        int m2 = 3;
        int n2 = 2;
        // Expected: 3
        System.out.println("\nInput: m=3, n=2");
        System.out.println("Output: " + uniquePathsOptimized(m2, n2));

        int m3 = 1;
        int n3 = 1;
        // Expected: 1 (start equals destination, a 1x1 grid)
        System.out.println("\nInput: m=1, n=1");
        System.out.println("Output: " + uniquePathsOptimized(m3, n3));
    }
}
