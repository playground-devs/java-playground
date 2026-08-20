package com.playground.java.interview.graphs;

/**
 * PATTERN: Graph / Union-Find (Disjoint Set Union)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given an adjacency matrix of cities, count the number of provinces
 * (groups of directly or indirectly connected cities).
 */
public class NumberOfProvincesUnionFind {

    // ================= PROBLEM =================
    // You are given an n x n matrix isConnected, where isConnected[i][j] = 1 if city i and city
    // j are directly connected, and 0 otherwise (isConnected[i][i] is always 1). A province is a
    // group of cities that are directly or indirectly connected. Return the total number of
    // provinces.
    // Example: isConnected = [[1,1,0],[1,1,0],[0,0,1]] -> output = 2
    //          (cities 0 and 1 form one province, city 2 forms its own province alone)
    //
    // ================= SIMPLE APPROACH =================
    // Run DFS or BFS from each unvisited city, following the matrix row to visit every directly
    // or indirectly connected city, marking them all visited and counting this as one province.
    // Repeat from the next unvisited city until all cities are visited. (This is a valid,
    // equally common O(n^2) approach - this file focuses on Union-Find as the primary solution.)
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // DFS/BFS is not incorrect here - both are O(n^2), same as Union-Find, for this one-shot
    // matrix. The reason to prefer Union-Find in an interview is that it generalizes far better
    // to STREAMING/INCREMENTAL connectivity ("process these connections one at a time and report
    // the province count after each"), where DFS/BFS would require re-scanning from scratch on
    // every update, while Union-Find updates incrementally in near-O(1) time per union.
    //
    // ================= OPTIMIZED APPROACH =================
    // Union-Find (Disjoint Set Union): maintain a parent[] array where parent[i] initially points
    // to itself (every city starts as its own province), plus a rank[] array for balancing.
    // - For every pair (i, j) with i < j where isConnected[i][j] == 1, call union(i, j).
    // - find(x) walks up to the root representative of x's province, using PATH COMPRESSION
    //   (pointing every visited node directly at the root on the way back) so future find()
    //   calls are much faster.
    // - union(a, b) finds both roots; if they differ, attaches one under the other using UNION
    //   BY RANK (attach the shorter tree under the taller tree's root) and decrements a running
    //   province counter.
    // - The counter (starting at n) after processing all connections is the final province count.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Union-Find with path compression + union by rank gives amortized O(alpha(n)) (essentially
    // constant) time per find()/union() call, so the whole algorithm runs in effectively O(n^2)
    // time overall - dominated purely by scanning the matrix, not by the union-find operations
    // themselves. It needs only two small arrays (parent[], rank[]) and no explicit
    // recursion/queue machinery, and it directly answers "are these two cities already
    // connected?" incrementally, without redoing any traversal - exactly the property this whole
    // category of connectivity problems needs.
    //
    // ================= EDGE CASES =================
    // - n = 1 (single city): exactly 1 province.
    // - No connections at all (isConnected is the identity matrix): n provinces, one per city.
    // - Every city connected to every other city: exactly 1 province.
    // - isConnected is symmetric by problem guarantee, but the code iterates only i < j pairs
    //   defensively assuming symmetry, since the matrix is guaranteed symmetric.
    // - Re-processing an already-unioned pair: union() is a safe no-op (roots already equal).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2 * alpha(n)), effectively O(n^2) - every entry of the n x n matrix
    // is inspected once, and each union()/find() call costs amortized O(alpha(n)) thanks to path
    // compression + union by rank.
    // Space Complexity: O(n) for the parent[] and rank[] arrays.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you answer "are city A and city B in the same province?" in O(1)-ish time after building the structure?
    // - How would this change if connections were given as an edge list instead of an adjacency matrix (becomes O(E) instead of O(n^2))?
    // - Why does path compression alone still give good amortized performance, and why do we usually combine it with union by rank anyway?
    // - How would you support "disconnecting" two cities - why can't Union-Find do this efficiently?
    // - What's the difference between "union by rank" and "union by size"?
    // - How would you count provinces using DFS instead, and what's the trade-off versus Union-Find?

    static class UnionFind {
        private final int[] parent;
        private final int[] rank;
        private int provinceCount; // number of distinct provinces (components) currently

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            provinceCount = n; // Step: initially every city is its own province.
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                // Step: path compression - point x directly at the root on the way back up.
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        void union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) {
                return; // already in the same province - no-op
            }

            // Step: union by rank - attach the shorter tree under the taller tree's root.
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }
            provinceCount--; // two provinces just merged into one
        }

        int getProvinceCount() {
            return provinceCount;
        }
    }

    public static int findCircleNum(int[][] isConnected) {
        int n = isConnected.length;
        UnionFind unionFind = new UnionFind(n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (isConnected[i][j] == 1) {
                    unionFind.union(i, j);
                }
            }
        }

        return unionFind.getProvinceCount();
    }

    public static void main(String[] args) {
        int[][] matrix1 = {{1, 1, 0}, {1, 1, 0}, {0, 0, 1}};
        // Expected: 2
        System.out.println("Input: [[1,1,0],[1,1,0],[0,0,1]]");
        System.out.println("Output: " + findCircleNum(matrix1));

        int[][] matrix2 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        // Expected: 3 (no connections at all)
        System.out.println("\nInput: [[1,0,0],[0,1,0],[0,0,1]] (no connections)");
        System.out.println("Output: " + findCircleNum(matrix2));

        int[][] matrix3 = {{1}};
        // Expected: 1 (single city)
        System.out.println("\nInput: [[1]] (single city)");
        System.out.println("Output: " + findCircleNum(matrix3));
    }
}
