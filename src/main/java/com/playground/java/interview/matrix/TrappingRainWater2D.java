package com.playground.java.interview.matrix;

import java.util.PriorityQueue;

/**
 * PATTERN: Matrix / Priority Queue (Min-Heap) BFS from Boundary Inward
 * PRIORITY: P3 - Advanced / Less Common
 * PROBLEM STATEMENT: Given a 2D height map, compute the total amount of rain water it can trap
 * (the 2D generalization of the classic Trapping Rain Water problem).
 */
public class TrappingRainWater2D {

    // ================= PROBLEM =================
    // You are given an m x n matrix of non-negative integers representing the height of each
    // cell in a terrain. After it rains, compute how much water the terrain can trap overall.
    // Water trapped at a cell depends on the lowest "wall" surrounding it in every direction,
    // just like the 1D version, but here water can escape in any of 4 directions (up, down,
    // left, right), and cells on the outer border can never trap water (water just flows off).
    // Example: heightMap = [[1,4,3,1,3,2],
    //                        [3,2,1,3,2,4],
    //                        [2,3,3,2,3,1]]
    //          -> output = 4 (total trapped water units)
    //
    // ================= SIMPLE APPROACH =================
    // (No simple brute force generalizes cleanly here the way two-pointer/prefix-max does for
    // 1D - in 1D, water only needs a wall to its left and right, but in 2D water can escape in
    // 4 directions, so "the lowest boundary" is not just two neighbors but the entire connected
    // boundary of the region, which requires a graph/BFS-style approach from the start.)
    //
    // ================= OPTIMIZED APPROACH =================
    // Think of it like water receding from the outside in: start with the ENTIRE outer border
    // of the grid as your initial "boundary wall" (since none of those cells can trap water -
    // any water there just flows off the edge of the map). Push every border cell into a
    // min-heap, keyed by height.
    // 1) Repeatedly pop the LOWEST-height cell currently on the boundary - this is the weakest
    //    point of the wall right now, so it determines how high water can rise for whatever is
    //    just inside it.
    // 2) For each of its unvisited neighbors (up/down/left/right), the trapped water at that
    //    neighbor is max(0, currentBoundaryHeight - neighborHeight) - it can only hold water up
    //    to the height of the boundary cell we just popped, if it's shorter.
    // 3) The neighbor's "effective wall height" going forward becomes max(currentBoundaryHeight,
    //    neighborHeight) (either it fills up to the boundary's level, or it's already taller) -
    //    push the neighbor into the heap with this effective height, and mark it visited.
    // 4) Keep going until the heap is empty; sum up all the trapped water computed in step 2.
    // This mimics water level slowly receding from the outer boundary toward the interior,
    // always constrained by the weakest (lowest) point of the wall encountered so far.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A min-heap (PriorityQueue) of (height, row, col) is essential because we must always
    // process the CURRENT globally-lowest boundary cell next - this greedy choice guarantees
    // that when we visit any interior cell for the first time, we've already found the true
    // minimum possible surrounding wall height for it (any other path to it would go through an
    // equal-or-higher boundary first). This greedy-by-lowest-height approach is what makes a
    // single pass correct, similar in spirit to Dijkstra's algorithm using a min-heap.
    //
    // ================= EDGE CASES =================
    // - Grid smaller than 3x3 (fewer than 2 rows or 2 columns): no interior cells exist, so no
    //   water can ever be trapped - return 0 immediately.
    // - All cells the same height: no water trapped (every interior cell already matches its boundary).
    // - A single very deep interior cell surrounded by tall walls: traps a lot of water, height computed correctly by the algorithm's chain of "effective wall heights".
    // - Completely flat border with a bowl-shaped interior: correctly computes water level based on border height.
    // - Already-visited cells must never be reprocessed (each cell is pushed into the heap and finalized exactly once).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m*n*log(m*n)) - every cell is pushed and popped from the heap exactly
    // once, and each heap operation costs O(log(m*n)) for a grid with m*n cells.
    // Space Complexity: O(m*n) for the visited array and the heap, which can hold up to O(m*n)
    // cells in the worst case.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must you start the heap with the ENTIRE border, not just the corners or a single cell?
    // - How does this algorithm relate to Dijkstra's shortest path algorithm (both use a greedy min-heap expansion)?
    // - How would you reconstruct the actual water level at every single cell (not just the total volume)?
    // - What happens if the height map has negative values or non-integer heights - does the approach still work?
    // - How would you parallelize or optimize this for a very large grid where the whole grid can't fit in memory at once?
    // - How does this 2D version generalize the classic 1D Trapping Rain Water two-pointer approach - why doesn't a simple two-pointer work here?
    // - Could this be solved with a BFS using multiple priority levels (bucket queue) instead of a general-purpose heap, for better constant factors?

    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Optimized: min-heap BFS starting from the boundary, water level receding inward.
    public static int trapRainWater(int[][] heightMap) {
        int rows = heightMap.length;
        if (rows < 3) {
            return 0;
        }
        int cols = heightMap[0].length;
        if (cols < 3) {
            return 0;
        }

        boolean[][] visited = new boolean[rows][cols];
        // Min-heap of {height, row, col}, ordered by height ascending.
        PriorityQueue<int[]> boundary = new PriorityQueue<>((a, b) -> a[0] - b[0]);

        // Step 1: seed the heap with the entire outer border - none of it can trap water.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (row == 0 || row == rows - 1 || col == 0 || col == cols - 1) {
                    boundary.offer(new int[]{heightMap[row][col], row, col});
                    visited[row][col] = true;
                }
            }
        }

        int totalWater = 0;

        // Step 2: always expand from the lowest current boundary cell inward.
        while (!boundary.isEmpty()) {
            int[] cell = boundary.poll();
            int currentHeight = cell[0];
            int row = cell[1];
            int col = cell[2];

            for (int[] dir : DIRECTIONS) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols || visited[newRow][newCol]) {
                    continue;
                }
                visited[newRow][newCol] = true;

                int neighborHeight = heightMap[newRow][newCol];
                // Step: water trapped is bounded by the lowest wall encountered so far.
                totalWater += Math.max(0, currentHeight - neighborHeight);

                // Step: the effective wall height going forward is the taller of the two.
                int effectiveHeight = Math.max(currentHeight, neighborHeight);
                boundary.offer(new int[]{effectiveHeight, newRow, newCol});
            }
        }

        return totalWater;
    }

    public static void main(String[] args) {
        int[][] heightMap1 = {
                {1, 4, 3, 1, 3, 2},
                {3, 2, 1, 3, 2, 4},
                {2, 3, 3, 2, 3, 1}
        };
        // Expected: 4
        System.out.println("Input: [[1,4,3,1,3,2],[3,2,1,3,2,4],[2,3,3,2,3,1]]");
        System.out.println("Output: " + trapRainWater(heightMap1));

        int[][] heightMap2 = {
                {3, 3, 3, 3},
                {3, 1, 1, 3},
                {3, 1, 1, 3},
                {3, 3, 3, 3}
        };
        // Expected: 8 (bowl of depth 2 in the middle 2x2 area)
        System.out.println("\nInput: 4x4 bowl surrounded by height-3 walls");
        System.out.println("Output: " + trapRainWater(heightMap2));

        int[][] heightMap3 = {{1, 2}, {3, 4}};
        // Expected: 0 (grid too small, no interior cells)
        System.out.println("\nInput: [[1,2],[3,4]] (2x2, too small)");
        System.out.println("Output: " + trapRainWater(heightMap3));
    }
}
