package com.playground.java.interview.backtracking;

import java.util.Arrays;

/**
 * PATTERN: Backtracking / Constraint Satisfaction
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Solve a 9x9 Sudoku board in place by filling empty cells so every row,
 * column, and 3x3 sub-box contains digits 1-9 exactly once.
 */
public class SudokuSolver {

    // ================= PROBLEM =================
    // You are given a partially filled 9x9 Sudoku board, where empty cells are marked with '.'.
    // Fill in the empty cells so that: every row contains digits 1-9 exactly once, every column
    // contains digits 1-9 exactly once, and every one of the nine 3x3 sub-boxes contains digits
    // 1-9 exactly once. Solve it in place.
    // Example (partial):
    //   Input row 0:  "53..7...." (mostly empty)
    //   ... (full 9x9 board with a valid unique solution)
    //   Output: the same board with every '.' replaced by the single correct digit.
    //
    // ================= SIMPLE APPROACH =================
    // (Sudoku solving is inherently a constraint-satisfaction/backtracking problem - there
    // isn't a simpler non-backtracking approach that reliably works for arbitrary valid
    // puzzles; the "brute force" here IS backtracking with straightforward validity checks, as
    // opposed to smarter constraint-propagation techniques used by advanced solvers.)
    //
    // ================= OPTIMIZED APPROACH =================
    // Backtracking with constraint checking:
    // 1) Scan the board for the next empty cell ('.').
    // 2) Try placing each digit 1-9 in that cell, one at a time.
    // 3) For each candidate digit, check if it's valid to place there: it must not already
    //    appear in the same row, the same column, or the same 3x3 sub-box.
    // 4) If valid, place the digit, and recursively try to solve the rest of the board.
    // 5) If the recursive call succeeds (returns true, meaning the whole board got solved),
    //    propagate that success back up immediately.
    // 6) If it fails (no digit works from some later empty cell), backtrack: undo the placement
    //    (reset the cell back to '.') and try the next candidate digit.
    // 7) If no empty cells remain at all, the board is completely and validly filled - success.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure beyond the board itself (2D char array) and simple O(9) row/
    // column/box scans is strictly necessary for correctness - the board doubles as its own
    // state representation, and undoing a placement is just resetting a cell back to '.'.
    // (A more advanced solver could maintain bitmask sets per row/column/box for O(1) validity
    // checks instead of O(9) scans, but a straightforward board-scan check is simpler to reason
    // about and sufficiently fast for standard 9x9 puzzles.)
    //
    // ================= EDGE CASES =================
    // - Board that is already completely filled and valid: no empty cells found, solver
    //   immediately reports success without changing anything.
    // - Board with multiple valid solutions: this backtracking approach returns the FIRST valid
    //   solution it finds (via always trying digits 1-9 in increasing order); a well-formed
    //   Sudoku puzzle is expected to have a unique solution.
    // - Board with no valid solution at all: the backtracking exhausts all possibilities for the
    //   first empty cell and correctly returns false (should not happen for a valid, solvable
    //   puzzle, but the algorithm handles it gracefully rather than crashing).
    // - Nearly-empty board (few given digits): more backtracking needed, but still correct,
    //   just slower.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: worst case O(9^(number of empty cells)) since backtracking may need to
    // try up to 9 digits at each empty cell - in practice, constraint checks prune the vast
    // majority of branches quickly for well-formed puzzles, making it fast for real Sudoku boards.
    // Space Complexity: O(1) extra space beyond the board itself (aside from the recursion
    // stack, which is at most O(number of empty cells) deep).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you speed up validity checks using bitmasks (one per row, column, and box) instead of O(9) scans each time?
    // - How would you pick the "most constrained" empty cell first (fewest valid candidate digits) instead of always scanning left-to-right, top-to-bottom, to prune faster (a classic CSP heuristic)?
    // - How would you validate whether a given (possibly complete) Sudoku board is itself valid, without solving it?
    // - How would you modify this to detect and report if a puzzle has multiple solutions, rather than just returning the first one found?
    // - How does this backtracking structure generalize to solving other constraint satisfaction puzzles (e.g. N-Queens, Kakuro)?
    // - What's the theoretical worst-case complexity, and why is it rarely reached in practice for real Sudoku puzzles?

    private static final int SIZE = 9;
    private static final int BOX_SIZE = 3;

    // Optimized: backtracking with row/column/box validity checks. Solves the board in place.
    public static boolean solveSudoku(char[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == '.') {
                    // Step: found an empty cell - try every digit 1-9 here.
                    for (char digit = '1'; digit <= '9'; digit++) {
                        if (isValidPlacement(board, row, col, digit)) {
                            board[row][col] = digit;
                            if (solveSudoku(board)) {
                                return true; // propagate success up
                            }
                            board[row][col] = '.'; // backtrack: undo and try next digit
                        }
                    }
                    return false; // no digit worked here - this branch is a dead end
                }
            }
        }
        return true; // no empty cells left - board is completely and validly filled
    }

    private static boolean isValidPlacement(char[][] board, int row, int col, char digit) {
        int boxRowStart = (row / BOX_SIZE) * BOX_SIZE;
        int boxColStart = (col / BOX_SIZE) * BOX_SIZE;

        for (int i = 0; i < SIZE; i++) {
            // Check the same row and the same column.
            if (board[row][i] == digit || board[i][col] == digit) {
                return false;
            }
            // Check the same 3x3 sub-box.
            int boxRow = boxRowStart + i / BOX_SIZE;
            int boxCol = boxColStart + i % BOX_SIZE;
            if (board[boxRow][boxCol] == digit) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        char[][] board1 = {
                {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
                {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
                {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
                {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
                {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
                {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
                {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
                {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
                {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };
        // Expected: a fully solved, valid 9x9 Sudoku board (solvable = true)
        System.out.println("Input: classic partially-filled 9x9 Sudoku puzzle");
        boolean solved1 = solveSudoku(board1);
        System.out.println("Solvable: " + solved1);
        for (char[] row : board1) {
            System.out.println(Arrays.toString(row));
        }

        char[][] board2 = {
                {'1', '2', '3', '4', '5', '6', '7', '8', '9'},
                {'4', '5', '6', '7', '8', '9', '1', '2', '3'},
                {'7', '8', '9', '1', '2', '3', '4', '5', '6'},
                {'2', '1', '4', '3', '6', '5', '8', '9', '7'},
                {'3', '6', '5', '8', '9', '7', '2', '1', '4'},
                {'8', '9', '7', '2', '1', '4', '3', '6', '5'},
                {'5', '3', '1', '6', '4', '2', '9', '7', '8'},
                {'6', '4', '2', '9', '7', '8', '5', '3', '1'},
                {'9', '7', '8', '5', '3', '1', '6', '4', '2'}
        };
        // Expected: already complete and valid - solveSudoku returns true, board unchanged
        System.out.println("\nInput: already completely filled, valid board");
        System.out.println("Solvable (already solved): " + solveSudoku(board2));
    }
}
