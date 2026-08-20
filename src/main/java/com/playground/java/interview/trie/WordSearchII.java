package com.playground.java.interview.trie;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Trie + Backtracking (DFS) on a 2D Grid
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a 2D board of letters and a list of words, find all words from the
 * list that can be formed by a sequence of adjacent cells on the board (each cell used at most
 * once per word).
 */
public class WordSearchII {

    // ================= PROBLEM =================
    // You have an m x n board of single characters, and a list of candidate words. A word can
    // be "found" on the board if it can be built by moving from cell to cell horizontally or
    // vertically (not diagonally), never reusing the same cell twice within one word. Return
    // all words from the list that can be found on the board.
    // Example: board = [['o','a','a','n'],
    //                    ['e','t','a','e'],
    //                    ['i','h','k','r'],
    //                    ['i','f','l','v']]
    //          words = ["oath","pea","eat","rain"]
    //          -> output = ["oath", "eat"]
    //
    // ================= SIMPLE APPROACH =================
    // For each word in the list independently, run a classic Word-Search style DFS/backtracking
    // over the entire board, starting from every cell, to check if that specific word can be
    // formed.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Running a full independent DFS search for every single word is O(words * m * n * 4^L)
    // in the worst case (L = word length) - words that share common prefixes (like "eat" and
    // "eats") redundantly re-explore the exact same starting paths on the board over and over,
    // once per word.
    //
    // ================= OPTIMIZED APPROACH =================
    // Build a single Trie containing ALL the candidate words first. Then do just ONE combined
    // DFS sweep over the board (starting from every cell), following paths in the Trie instead
    // of searching for one word at a time:
    // 1) At each board cell, check if the current letter has a corresponding child in the
    //    current TrieNode. If not, this path can never lead to any candidate word - stop
    //    immediately (this is the key pruning power of the Trie).
    // 2) If it does have a child, move into that child TrieNode and continue the DFS to
    //    neighboring cells (up/down/left/right), temporarily marking the current cell as
    //    visited (e.g. replace it with a sentinel character) so it isn't reused within the same
    //    word path, then restore it on backtrack.
    // 3) Whenever a TrieNode's isEndOfWord flag is true, we've found a complete word - add it
    //    to the results (and mark that TrieNode's word slot as already used, to avoid adding
    //    duplicates from multiple paths that spell the same word).
    // This way, words sharing a common prefix share the same DFS exploration up to where their
    // paths diverge, instead of re-searching from scratch for each one.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A Trie lets the DFS prune impossible paths in O(1) per step (just check "does a child
    // exist for this letter"), and it naturally batches all candidate words into one shared
    // search structure so common prefixes are explored only once, rather than once per word -
    // this is precisely what turns "words * board search" into "one board search guided by the
    // combined structure of all words".
    //
    // ================= EDGE CASES =================
    // - A word not present anywhere on the board: simply never gets added.
    // - Two words that are prefixes of each other (e.g. "a" and "ab"): both can be found
    //   independently if the board supports it; the Trie's isEndOfWord flag on each relevant
    //   node handles this correctly.
    // - Duplicate words in the input list: should not produce duplicate entries in the output.
    // - Board smaller than the shortest candidate word length: those words simply won't be found; no crash.
    // - Word that revisits the same cell twice (not allowed): the temporary "mark visited" sentinel during DFS prevents this correctly.
    // - Empty word list or empty board: return an empty result list.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m*n*4^L) in the worst case (L = length of the longest word) for the
    // combined DFS sweep, but with Trie pruning this only explores paths that are actual
    // prefixes of some candidate word, which is far less in practice than the brute force's
    // O(words * m*n*4^L). Building the Trie itself costs O(total characters across all words).
    // Space Complexity: O(total characters across all words) for the Trie, plus O(L) for the
    // DFS recursion stack depth.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How do you avoid adding the same found word to the result list multiple times (multiple paths spelling the same word)?
    // - How would you optimize further by removing a word (or pruning a Trie branch) once it's been found, so future DFS calls skip it (Trie node pruning optimization)?
    // - How does using a Trie here compare to just using a HashSet of words plus a HashSet of prefixes for pruning?
    // - How would you extend this to allow diagonal moves as well?
    // - How would you adapt this if words could be reused across different starting cells but never within the same word (already handled) versus allowing letter reuse entirely?
    // - Why is marking the visited cell with a sentinel character (like '#') and restoring it afterward more space-efficient than a separate boolean visited grid?
    // - What's the worst-case blow-up scenario for this algorithm (e.g. all same letters), and how does Trie pruning still help or not help there?

    static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        String word = null; // non-null exactly when a full candidate word ends at this node
    }

    // Optimized: build one Trie for all words, then one combined DFS sweep over the board.
    public static List<String> findWords(char[][] board, String[] words) {
        List<String> result = new ArrayList<>();
        if (board == null || board.length == 0 || words == null || words.length == 0) {
            return result;
        }

        // Step 1: build the Trie containing all candidate words.
        TrieNode root = new TrieNode();
        for (String word : words) {
            TrieNode current = root;
            for (char c : word.toCharArray()) {
                int index = c - 'a';
                if (current.children[index] == null) {
                    current.children[index] = new TrieNode();
                }
                current = current.children[index];
            }
            current.word = word;
        }

        // Step 2: one combined DFS sweep over the board, starting from every cell.
        int rows = board.length;
        int cols = board[0].length;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                dfs(board, row, col, root, result);
            }
        }

        return result;
    }

    private static void dfs(char[][] board, int row, int col, TrieNode node, List<String> result) {
        int rows = board.length;
        int cols = board[0].length;
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return;
        }
        char c = board[row][col];
        if (c == '#') {
            return; // already used in the current path
        }
        TrieNode next = node.children[c - 'a'];
        if (next == null) {
            return; // Trie pruning: no candidate word continues with this letter
        }

        if (next.word != null) {
            // Step: found a complete candidate word - record it and avoid re-adding duplicates.
            result.add(next.word);
            next.word = null;
        }

        // Step: mark this cell as visited (sentinel), explore neighbors, then restore it.
        board[row][col] = '#';
        dfs(board, row + 1, col, next, result);
        dfs(board, row - 1, col, next, result);
        dfs(board, row, col + 1, next, result);
        dfs(board, row, col - 1, next, result);
        board[row][col] = c;
    }

    public static void main(String[] args) {
        char[][] board1 = {
                {'o', 'a', 'a', 'n'},
                {'e', 't', 'a', 'e'},
                {'i', 'h', 'k', 'r'},
                {'i', 'f', 'l', 'v'}
        };
        String[] words1 = {"oath", "pea", "eat", "rain"};
        // Expected: [oath, eat] (order may vary)
        System.out.println("Input: 4x4 board, words=[oath,pea,eat,rain]");
        System.out.println("Output: " + findWords(board1, words1));

        char[][] board2 = {{'a', 'b'}, {'c', 'd'}};
        String[] words2 = {"abcb"};
        // Expected: [] (would require reusing cell 'b', not allowed)
        System.out.println("\nInput: 2x2 board, words=[abcb] (requires reuse)");
        System.out.println("Output: " + findWords(board2, words2));

        char[][] board3 = {{'a'}};
        String[] words3 = {};
        // Expected: [] (empty word list)
        System.out.println("\nInput: 1x1 board, words=[] (empty)");
        System.out.println("Output: " + findWords(board3, words3));
    }
}
