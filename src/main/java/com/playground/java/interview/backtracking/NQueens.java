package com.playground.java.interview.backtracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PATTERN: Backtracking / Constraint Satisfaction (N-Queens)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Place N queens on an N x N chessboard such that no two queens attack each
 * other, and return all distinct valid board configurations.
 */
public class NQueens {

    // ================= PROBLEM =================
    // Place N queens on an N x N chessboard so that no two queens attack each other (no two
    // queens share the same row, column, or diagonal). Return all distinct solutions, each
    // represented as a list of strings where 'Q' marks a queen and '.' marks an empty cell.
    // Example: n = 4 -> output = [
    //            [".Q..", "...Q", "Q...", "..Q."],
    //            ["..Q.", "Q...", "...Q", ".Q.."]
    //          ]  (2 solutions total)
    //
    // ================= SIMPLE APPROACH =================
    // Try placing a queen in every one of the N*N cells combination-wise (choose N cells out of
    // N*N total), then check afterward whether the chosen set of N cells forms a valid,
    // non-attacking placement.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Checking all C(N*N, N) combinations of cells for validity after the fact is enormously
    // wasteful - the vast majority of placements are invalid, and most of that invalidity could
    // have been detected (and pruned) much earlier, e.g. as soon as two queens are placed in the
    // same column, instead of only after all N queens have already been placed.
    //
    // ================= OPTIMIZED APPROACH =================
    // Backtrack row by row, placing exactly one queen per row (this alone eliminates the
    // "same row" conflict automatically, since we never consider two queens in the same row).
    // For each row, try every column:
    // - Track which columns, "positive diagonals" (row + col, constant along a diagonal), and
    //   "negative diagonals" (row - col, constant along the other diagonal) are already
    //   occupied, using boolean arrays or HashSets.
    // - If placing a queen at (row, col) would conflict with any already-used column or
    //   diagonal, skip that column.
    // - Otherwise, place the queen, mark column/diagonals as used, recurse into the next row,
    //   then backtrack (remove the queen, unmark column/diagonals) before trying the next
    //   column.
    // - Base case: once row == n, a full valid placement has been found - record the board.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Boolean arrays (or HashSets) for columns, "row+col" diagonals, and "row-col" diagonals give
    // O(1) conflict checks and O(1) mark/unmark operations, turning what would otherwise be an
    // O(n) scan per placement (checking every previously placed queen) into O(1) - this is what
    // makes the backtracking search practical, since conflict-checking happens at every single
    // node of the search tree.
    //
    // ================= EDGE CASES =================
    // - n = 1: exactly 1 solution (a single queen on the only cell).
    // - n = 2 or n = 3: 0 solutions exist (too small a board to place non-attacking queens).
    // - n = 4: exactly 2 distinct solutions (a well-known base case for verifying correctness).
    // - Larger n (e.g. n = 8, the classic "8 queens" problem): 92 solutions - useful for
    //   verifying performance and correctness at a larger scale.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n!) in the worst case for the backtracking search (roughly - each row
    // has fewer valid column choices as more queens are placed), versus much worse for the
    // brute-force "check all combinations" approach; conflict checks are O(1) each thanks to the
    // tracking sets.
    // Space Complexity: O(n) for the recursion depth and the column/diagonal tracking
    // structures, plus O(n^2) per solution stored in the output.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does placing exactly one queen per row automatically eliminate the "same row" conflict without any extra bookkeeping?
    // - Why do "row + col" and "row - col" correctly and uniquely identify each of the two diagonal directions?
    // - How would you modify this to just COUNT the number of solutions (N-Queens II) without building the actual boards, saving memory?
    // - How would bitmasking (using integers as bitsets for columns/diagonals) speed this up further compared to boolean arrays?
    // - How would you extend this to place queens with one or more cells pre-blocked (obstacles) on the board?
    // - What symmetry-based pruning (e.g. only searching half the first row) could cut the search space roughly in half?

    // Optimized: backtrack row by row, tracking used columns and both diagonals for O(1) checks.
    public static List<List<String>> solveNQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        Set<Integer> usedColumns = new HashSet<>();
        Set<Integer> usedPositiveDiagonals = new HashSet<>(); // row + col
        Set<Integer> usedNegativeDiagonals = new HashSet<>(); // row - col
        int[] queenColumnPerRow = new int[n]; // queenColumnPerRow[row] = column of the queen in that row

        backtrack(0, n, queenColumnPerRow, usedColumns, usedPositiveDiagonals, usedNegativeDiagonals, result);
        return result;
    }

    private static void backtrack(int row, int n, int[] queenColumnPerRow, Set<Integer> usedColumns,
                                   Set<Integer> usedPositiveDiagonals, Set<Integer> usedNegativeDiagonals,
                                   List<List<String>> result) {
        if (row == n) {
            // Step: all n rows have a queen placed without conflicts - record this board.
            result.add(buildBoard(queenColumnPerRow, n));
            return;
        }

        for (int col = 0; col < n; col++) {
            int positiveDiagonal = row + col;
            int negativeDiagonal = row - col;

            if (usedColumns.contains(col) || usedPositiveDiagonals.contains(positiveDiagonal)
                    || usedNegativeDiagonals.contains(negativeDiagonal)) {
                // Step: this column or diagonal is already attacked - skip this column.
                continue;
            }

            // Step: place the queen and mark its column/diagonals as used.
            queenColumnPerRow[row] = col;
            usedColumns.add(col);
            usedPositiveDiagonals.add(positiveDiagonal);
            usedNegativeDiagonals.add(negativeDiagonal);

            backtrack(row + 1, n, queenColumnPerRow, usedColumns, usedPositiveDiagonals, usedNegativeDiagonals, result);

            // Step: backtrack - remove the queen and unmark its column/diagonals.
            usedColumns.remove(col);
            usedPositiveDiagonals.remove(positiveDiagonal);
            usedNegativeDiagonals.remove(negativeDiagonal);
        }
    }

    private static List<String> buildBoard(int[] queenColumnPerRow, int n) {
        List<String> board = new ArrayList<>();
        for (int row = 0; row < n; row++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int col = 0; col < n; col++) {
                rowBuilder.append(col == queenColumnPerRow[row] ? 'Q' : '.');
            }
            board.add(rowBuilder.toString());
        }
        return board;
    }

    public static void main(String[] args) {
        int n1 = 4;
        // Expected: 2 solutions
        System.out.println("Input: n=4");
        List<List<String>> result1 = solveNQueens(n1);
        System.out.println("Number of solutions: " + result1.size());
        for (List<String> board : result1) {
            System.out.println(board);
        }

        int n2 = 1;
        // Expected: 1 solution (single queen on the only cell)
        System.out.println("\nInput: n=1");
        List<List<String>> result2 = solveNQueens(n2);
        System.out.println("Number of solutions: " + result2.size());
        System.out.println(result2);

        int n3 = 2;
        // Expected: 0 solutions (board too small)
        System.out.println("\nInput: n=2 (no valid placement possible)");
        List<List<String>> result3 = solveNQueens(n3);
        System.out.println("Number of solutions: " + result3.size());
    }
}
