package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * PATTERN: Graph / BFS-DFS (Matrix Traversal, Connected Components)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given a 2D grid of '1' (land) and '0' (water), count the number of
 * islands, where an island is a group of '1's connected 4-directionally (up/down/left/right).
 */
public class NumberOfIslands {

    // ================= PROBLEM =================
    // You get a grid of characters, each cell is '1' (land) or '0' (water).
    // Count how many separate islands exist. Land cells connected horizontally or
    // vertically (NOT diagonally) belong to the same island.
    //
    // Example grid:
    //   1 1 0 0 0
    //   1 1 0 0 0
    //   0 0 1 0 0
    //   0 0 0 1 1
    //
    // Island #1: the 2x2 block of 1's in the top-left corner.
    // Island #2: the single 1 in the middle.
    // Island #3: the two connected 1's in the bottom-right corner.
    // Expected output: 3
    //
    // ================= SIMPLE APPROACH =================
    // Scan every cell. Whenever you find an unvisited '1', that's a brand-new island -
    // increment the count. Then you must somehow "use up" every land cell that belongs to
    // this same island so you don't count it again later.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Without a systematic way to explore and mark ALL cells connected to the one you just
    // found, you'd either re-count the same island multiple times, or need an expensive
    // re-scan of the whole grid per island to find connected cells. You need a traversal
    // technique (flood fill) that visits every connected cell exactly once per discovery.
    //
    // ================= OPTIMIZED APPROACH =================
    // Scan every cell once. When an unvisited '1' is found: increment island count, then
    // run a "flood fill" from that cell to visit and mark every connected land cell so the
    // main scan skips them later. Two ways to flood fill, both shown below:
    // 1) DFS (recursive): recursively visit up/down/left/right neighbors that are still '1'.
    // 2) BFS (iterative, queue-based): push the start cell, then repeatedly pop a cell,
    //    mark it visited, and push its unvisited land neighbors.
    // Both mark visited cells by flipping grid[r][c] from '1' to '0' (mutating input) so we
    // never revisit them; a separate boolean[][] visited array is used instead if the input
    // grid must not be mutated (e.g. read-only input, or need original grid preserved after).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // For DFS: the call stack itself acts as the "frontier to explore" - no explicit data
    // structure needed, but risks StackOverflowError on a huge, fully-connected grid because
    // recursion depth can reach grid.length * grid[0].length in the worst case (one giant island).
    // For BFS: a Queue (ArrayDeque) holds the "frontier" of cells discovered but not yet
    // expanded, processed in FIFO order. This avoids deep recursion entirely (safe for huge
    // grids), trading recursion-stack space for explicit heap-allocated queue space.
    // Mutating grid in place (or a boolean[][] visited array): gives O(1) "have I visited
    // this cell" checks - critical so we never re-explore the same land cell twice, which
    // would otherwise cause infinite loops or repeated counting within a single island.
    //
    // ================= EDGE CASES =================
    // - Empty grid (null or 0 rows): 0 islands.
    // - Grid with no land at all (all '0'): 0 islands.
    // - Grid that is entirely land: 1 island.
    // - Single cell grid ('1' or '0'): 1 or 0 islands respectively.
    // - Diagonal-only connections (e.g. '1' at (0,0) and (1,1) but not (0,1)/(1,0)): these
    //   are considered SEPARATE islands since only 4-directional connectivity counts.
    // - Irregularly shaped/very large islands: recursion depth matters for DFS (risk of
    //   stack overflow); BFS avoids this.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(rows * cols) for both DFS and BFS - every cell is visited and
    // marked at most once across the entire algorithm (the outer scan plus all flood fills
    // together touch each cell a constant number of times).
    // Space Complexity: O(rows * cols) worst case for both - DFS uses recursion call stack
    // depth up to the size of the largest island (all cells could be one connected island);
    // BFS uses queue space up to the size of the largest "wavefront" of an island, which is
    // also bounded by rows * cols in the worst case.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve this if you could not mutate the input grid? (Use a boolean[][] visited array.)
    // - How would the solution change for 8-directional (including diagonal) connectivity?
    // - How would you find the SIZE of the largest island instead of just counting islands?
    // - What if the grid is a live stream and cells can be added/updated incrementally - how
    //   would Union-Find (Disjoint Set Union) handle this more efficiently than re-scanning?
    // - How do you avoid a StackOverflowError with DFS on a huge, fully-connected grid?
    // - How would you count islands in a 3D grid (adding a "depth" dimension)?
    // - Could you solve this using Union-Find instead of BFS/DFS? What would the tradeoffs be?

    // Approach 1: DFS (recursive flood fill), mutating the grid in place.
    public static int numIslandsDFS(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            return 0;
        }
        int rows = grid.length;
        int cols = grid[0].length;
        int islandCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '1') {
                    // Step: found a new, unvisited piece of land -> new island.
                    islandCount++;
                    // Step: sink the entire connected island so we never count it again.
                    dfsFloodFill(grid, r, c);
                }
            }
        }
        return islandCount;
    }

    private static void dfsFloodFill(char[][] grid, int r, int c) {
        // Step: bounds check and "is this water or already-visited" check.
        if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length || grid[r][c] != '1') {
            return;
        }
        // Step: mark visited by flipping to water so we don't revisit.
        grid[r][c] = '0';
        // Step: explore all 4 neighbors.
        dfsFloodFill(grid, r + 1, c);
        dfsFloodFill(grid, r - 1, c);
        dfsFloodFill(grid, r, c + 1);
        dfsFloodFill(grid, r, c - 1);
    }

    // Approach 2: BFS (queue-based flood fill), mutating the grid in place.
    public static int numIslandsBFS(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            return 0;
        }
        int rows = grid.length;
        int cols = grid[0].length;
        int islandCount = 0;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '1') {
                    islandCount++;
                    // Step: mark starting cell visited immediately before enqueueing.
                    grid[r][c] = '0';
                    Queue<int[]> queue = new ArrayDeque<>();
                    queue.add(new int[]{r, c});

                    while (!queue.isEmpty()) {
                        int[] cell = queue.poll();
                        for (int[] dir : directions) {
                            int nr = cell[0] + dir[0];
                            int nc = cell[1] + dir[1];
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == '1') {
                                // Step: mark visited the moment we discover it (not when popped)
                                // to avoid enqueueing the same cell multiple times.
                                grid[nr][nc] = '0';
                                queue.add(new int[]{nr, nc});
                            }
                        }
                    }
                }
            }
        }
        return islandCount;
    }

    // Helper: deep-copy a grid so we can run both DFS and BFS on identical, unmutated input.
    private static char[][] copyGrid(char[][] grid) {
        char[][] copy = new char[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = grid[i].clone();
        }
        return copy;
    }

    public static void main(String[] args) {
        char[][] grid1 = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        // Expected: 3
        System.out.println("Grid 1 islands (DFS): " + numIslandsDFS(copyGrid(grid1)));
        System.out.println("Grid 1 islands (BFS): " + numIslandsBFS(copyGrid(grid1)));

        char[][] grid2 = {}; // empty grid
        // Expected: 0
        System.out.println("Empty grid islands (DFS): " + numIslandsDFS(copyGrid(grid2)));

        char[][] grid3 = {
            {'1', '0', '1'},
            {'0', '1', '0'},
            {'1', '0', '1'}
        };
        // Expected: 5 (all diagonal-only connections, each cell is its own island)
        System.out.println("Diagonal-only grid islands (BFS): " + numIslandsBFS(copyGrid(grid3)));

        char[][] grid4 = {
            {'1', '1', '1'},
            {'1', '1', '1'},
            {'1', '1', '1'}
        };
        // Expected: 1 (entire grid is one connected island)
        System.out.println("All-land grid islands (DFS): " + numIslandsDFS(copyGrid(grid4)));
    }
}
