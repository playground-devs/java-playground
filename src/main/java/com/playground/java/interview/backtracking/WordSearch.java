package com.playground.java.interview.backtracking;

/**
 * PATTERN: Backtracking / DFS on a 2D Grid (Word Search)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given a 2D board of letters and a word, determine if the word can be
 * constructed by moving to horizontally or vertically adjacent cells, without reusing a cell.
 */
public class WordSearch {

    // ================= PROBLEM =================
    // Given an m x n grid of characters (board) and a string word, determine if the word exists
    // in the grid. The word must be constructed from letters of sequentially adjacent cells,
    // where adjacent cells are horizontally or vertically neighboring. The same cell may not be
    // used more than once within a single word path.
    // Example: board = [['A','B','C','E'],['S','F','C','S'],['A','D','E','E']], word = "ABCCED"
    //          -> output = true
    // Example: same board, word = "SEE" -> output = true
    // Example: same board, word = "ABCB" -> output = false (the second 'B' would require reusing
    //          the first 'B' cell)
    //
    // ================= SIMPLE APPROACH =================
    // From every cell in the grid, try to match the word letter by letter, exploring all 4
    // directions (up/down/left/right) at each step, keeping a separate visited-tracking
    // structure (e.g. a boolean[][] or a HashSet of coordinates) to avoid reusing cells within
    // the current path.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Using a separate boolean[][] visited array (or a HashSet) alongside the board works
    // correctly, but allocates and threads through extra memory on every single DFS call; the
    // same correctness can be achieved with zero extra memory by temporarily mutating the board
    // itself as a marker, which is the standard optimization interviewers look for.
    //
    // ================= OPTIMIZED APPROACH =================
    // DFS + backtracking starting from every cell that matches the word's first letter:
    // - At each step, if the current cell's letter doesn't match the expected character in the
    //   word, or the position is out of bounds, or the cell was already used in this path,
    //   fail this path (return false).
    // - If we've matched all characters of the word, succeed (return true).
    // - Otherwise: temporarily mark the current cell as visited by overwriting it with a sentinel
    //   character (e.g. '#', which can never match a real letter), recursively try all 4
    //   directions from here for the next character, then RESTORE the original letter
    //   ("un-mark") before returning - this restore step is what makes the mutation safe, since
    //   the board is left exactly as it was for the caller (and for other starting cells tried
    //   later).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Mutating the board directly (rather than allocating a parallel boolean[][] visited array)
    // achieves the "mark visited, then restore" behavior with zero extra memory - it works
    // because the sentinel character used for marking ('#') can never appear as a real board
    // letter or match any character being searched for, and every mutation is always undone
    // (backtracked) immediately after the recursive exploration from that cell returns.
    //
    // ================= EDGE CASES =================
    // - Word longer than the total number of cells in the board: can never be found - fails
    //   immediately (or naturally, since we'd run out of unused cells).
    // - Word is a single character: succeeds if that character exists anywhere on the board.
    // - Word requires revisiting the same cell twice (e.g. "ABCB" needing the same 'B' cell
    //   twice): correctly fails, since a cell can only be used once per path.
    // - Board has only one row or one column: DFS still works correctly, simply with fewer
    //   valid directions available at the edges.
    // - Multiple valid starting positions on the board for the word's first letter: all are
    //   tried; the search succeeds if ANY of them leads to a full match.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m * n * 4^L) in the worst case, where m, n are the board dimensions and
    // L is the word length - we may attempt the search from every cell, and at each of the L
    // steps we explore up to 4 directions.
    // Space Complexity: O(L) for the recursion depth (bounded by the word length); no extra
    // grid-sized memory is used since the board itself is mutated in place as the visited
    // marker.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is it safe to mutate the board in place as a visited marker, as long as it's always restored before the function returns?
    // - What sentinel character would be unsafe to use for marking, and why (e.g. what if the word could contain '#')?
    // - How would you extend this to "Word Search II", finding ALL words from a dictionary that exist on the board (hint: a Trie built from the dictionary)?
    // - How would you solve this if diagonal moves were also allowed?
    // - What's the impact on time complexity if the word contains many repeated letters versus all distinct letters?
    // - How would you adapt this to return the actual PATH of coordinates for the found word, not just true/false?

    // Optimized: DFS + backtracking, marking visited cells by temporarily mutating the board.
    public static boolean exist(char[][] board, String word) {
        if (board.length == 0 || board[0].length == 0 || word.isEmpty()) {
            return false;
        }

        int rows = board.length;
        int cols = board[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Step: try starting the word search from every cell.
                if (dfs(board, word, row, col, 0)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean dfs(char[][] board, String word, int row, int col, int wordIndex) {
        if (wordIndex == word.length()) {
            return true; // matched every character of the word
        }
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return false; // out of bounds
        }
        if (board[row][col] != word.charAt(wordIndex)) {
            return false; // letter doesn't match (also fails if cell was already visited, since
            // a visited cell holds the sentinel '#', which matches no real letter)
        }

        // Step: temporarily mark this cell as visited by overwriting its letter.
        char originalLetter = board[row][col];
        board[row][col] = '#';

        // Step: explore all 4 directions for the next character.
        boolean found = dfs(board, word, row + 1, col, wordIndex + 1)
                || dfs(board, word, row - 1, col, wordIndex + 1)
                || dfs(board, word, row, col + 1, wordIndex + 1)
                || dfs(board, word, row, col - 1, wordIndex + 1);

        // Step: backtrack - restore the original letter so other paths can still use this cell.
        board[row][col] = originalLetter;

        return found;
    }

    public static void main(String[] args) {
        char[][] board1 = {
                {'A', 'B', 'C', 'E'},
                {'S', 'F', 'C', 'S'},
                {'A', 'D', 'E', 'E'}
        };
        // Expected: true
        System.out.println("Input: board=[[A,B,C,E],[S,F,C,S],[A,D,E,E]], word=\"ABCCED\"");
        System.out.println("Output: " + exist(board1, "ABCCED"));

        // Expected: true
        System.out.println("\nInput: same board, word=\"SEE\"");
        System.out.println("Output: " + exist(board1, "SEE"));

        // Expected: false (would require reusing the same 'B' cell twice)
        System.out.println("\nInput: same board, word=\"ABCB\" (would reuse a cell)");
        System.out.println("Output: " + exist(board1, "ABCB"));
    }
}
