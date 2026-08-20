package com.playground.java.interview.graphs;

import java.util.LinkedList;
import java.util.Queue;

/**
 * PATTERN: Graph / Multi-Source BFS (Grid)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given a grid of fresh, rotten, and empty cells, find the minimum number of
 * minutes until no cell has a fresh orange left, or -1 if that's impossible.
 */
public class RottingOranges {

    // ================= PROBLEM =================
    // You are given an m x n grid where each cell is one of: 0 (empty), 1 (fresh orange), or 2
    // (rotten orange). Every minute, any fresh orange that is 4-directionally adjacent to a
    // rotten orange also becomes rotten. Return the minimum number of minutes that must elapse
    // until no cell has a fresh orange left. If this is impossible (some fresh orange can never
    // be reached), return -1.
    // Example: grid = [[2,1,1],[1,1,0],[0,1,1]] -> output = 4
    // Example: grid = [[2,1,1],[0,1,1],[1,0,1]] -> output = -1 (the bottom-left fresh orange is
    //          isolated by the empty cell and can never rot)
    //
    // ================= SIMPLE APPROACH =================
    // Repeatedly scan the entire grid minute by minute: on each pass, find every fresh orange
    // adjacent to a rotten one and mark it "to become rotten," then apply all those changes at
    // once, and repeat until a full pass produces no new rotten oranges.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Re-scanning the ENTIRE grid on every single minute (even the parts far from any rot
    // frontier) wastes a lot of repeated work - each minute costs O(m*n) just to find the
    // current frontier, and this could take many minutes, when a single BFS pass could compute
    // the same result while only ever visiting each cell once.
    //
    // ================= OPTIMIZED APPROACH =================
    // Multi-source BFS: instead of starting BFS from a single source, push ALL initially rotten
    // oranges into the queue at once, each tagged with distance/minute 0. Then run standard BFS:
    // - Pop a cell, look at its 4 neighbors.
    // - If a neighbor is a fresh orange (1), rot it (mark as 2), record its minute as
    //   (current cell's minute + 1), and push it onto the queue.
    // - Track the maximum minute seen across the whole BFS - that's the answer, since BFS
    //   naturally processes cells in increasing order of "minutes to rot."
    // - Also track how many fresh oranges remain; if any are still fresh after BFS completes,
    //   return -1 (they were unreachable from any rotten source).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A queue is exactly what's needed for BFS's level-by-level (minute-by-minute) expansion
    // guarantee - since ALL initially rotten oranges are enqueued together as "minute 0"
    // sources, the BFS naturally spreads outward in synchronized "waves" that correspond exactly
    // to real minutes elapsing, which is what makes multi-source BFS the right model for
    // "simultaneous spread from many starting points" problems like this one.
    //
    // ================= EDGE CASES =================
    // - No fresh oranges at all: answer is 0 minutes immediately (nothing needs to rot).
    // - No rotten oranges but some fresh ones exist: those fresh oranges can never rot - answer
    //   is -1.
    // - A fresh orange is isolated by empty cells (0s) from every rotten orange: answer is -1,
    //   since BFS will never reach it.
    // - All oranges already rotten: answer is 0 minutes.
    // - Grid with only one cell: trivially 0 minutes if rotten or empty, -1 if fresh (alone,
    //   nothing to rot it).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m * n) - every cell is enqueued and processed at most once in the BFS,
    // versus O(m*n) work potentially repeated many times per minute in the naive repeated-scan
    // approach.
    // Space Complexity: O(m * n) for the BFS queue in the worst case (e.g. if the entire grid
    // starts rotten).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does pushing ALL initially rotten oranges into the queue at once (rather than one at a time) correctly model "simultaneous" spread across multiple sources?
    // - How would you modify this to return the actual grid state at each minute, not just the final minute count?
    // - How does multi-source BFS here compare to running single-source BFS from each rotten orange separately and taking the minimum?
    // - How would you adapt this if oranges could also rot diagonally, not just up/down/left/right?
    // - How would you detect and handle a grid with no oranges at all (all cells are 0)?
    // - What's the relationship between this problem and other multi-source BFS problems like "01 Matrix" (distance to nearest 0)?

    public static int orangesRotting(int[][] grid) {
        if (grid.length == 0 || grid[0].length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        Queue<int[]> queue = new LinkedList<>(); // each entry: {row, col}
        int freshCount = 0;

        // Step: seed the queue with every initially rotten orange, and count fresh ones.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] == 2) {
                    queue.offer(new int[]{row, col});
                } else if (grid[row][col] == 1) {
                    freshCount++;
                }
            }
        }

        if (freshCount == 0) {
            return 0; // nothing fresh to begin with
        }

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int minutesElapsed = 0;

        // Step: BFS wave by wave, one wave = one minute.
        while (!queue.isEmpty()) {
            int waveSize = queue.size();
            boolean rottedAnyThisWave = false;

            for (int i = 0; i < waveSize; i++) {
                int[] cell = queue.poll();
                for (int[] direction : directions) {
                    int newRow = cell[0] + direction[0];
                    int newCol = cell[1] + direction[1];
                    if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                        continue;
                    }
                    if (grid[newRow][newCol] == 1) {
                        // Step: rot this fresh orange and enqueue it for the next wave.
                        grid[newRow][newCol] = 2;
                        freshCount--;
                        rottedAnyThisWave = true;
                        queue.offer(new int[]{newRow, newCol});
                    }
                }
            }

            if (rottedAnyThisWave) {
                minutesElapsed++;
            }
        }

        return freshCount == 0 ? minutesElapsed : -1;
    }

    public static void main(String[] args) {
        int[][] grid1 = {{2, 1, 1}, {1, 1, 0}, {0, 1, 1}};
        // Expected: 4
        System.out.println("Input: [[2,1,1],[1,1,0],[0,1,1]]");
        System.out.println("Output: " + orangesRotting(grid1));

        int[][] grid2 = {{2, 1, 1}, {0, 1, 1}, {1, 0, 1}};
        // Expected: -1 (bottom-left fresh orange is isolated)
        System.out.println("\nInput: [[2,1,1],[0,1,1],[1,0,1]] (isolated fresh orange)");
        System.out.println("Output: " + orangesRotting(grid2));

        int[][] grid3 = {{0, 2}};
        // Expected: 0 (no fresh oranges at all)
        System.out.println("\nInput: [[0,2]] (no fresh oranges)");
        System.out.println("Output: " + orangesRotting(grid3));
    }
}
