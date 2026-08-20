package com.playground.java.interview.graphs;

import java.util.Arrays;

/**
 * PATTERN: Graph / Union-Find (Disjoint Set Union)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a graph that started as a tree with n nodes but has one extra edge
 * added (making it have exactly one cycle), find that redundant edge.
 */
public class RedundantConnectionUnionFind {

    // ================= PROBLEM =================
    // You are given a list of edges for a graph that originally was a tree with n nodes (n
    // edges would make it not a tree - a tree with n nodes has exactly n-1 edges), but one
    // extra edge was added, creating exactly one cycle. Find the edge that can be removed so
    // that the result is a tree again. If multiple such edges could work, return the one that
    // appears LAST in the input list.
    // Example: edges = [[1,2],[1,3],[2,3]] -> output = [2,3]
    // (1-2 and 1-3 form a valid tree; adding 2-3 creates a cycle 1-2-3-1, and [2,3] is the last
    //  edge in the list that completes this cycle.)
    //
    // ================= SIMPLE APPROACH =================
    // For each edge in the list, temporarily remove it and check (via DFS/BFS) whether the
    // remaining graph is still connected as a valid tree (n-1 edges, all nodes reachable, no
    // cycle). Return the first such edge found, scanning from the end of the list backward.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Doing a full DFS/BFS connectivity check for each candidate edge is O(n) per check, and
    // checking every edge gives O(n^2) overall - much slower than necessary given that
    // Union-Find can detect the exact redundant edge in a single linear pass.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use Union-Find (Disjoint Set Union) and process edges in the ORDER they're given:
    // 1) Initialize each node as its own separate set (parent[i] = i).
    // 2) For each edge (u, v) in order, find the "root" representative of u's set and v's set.
    // 3) If u and v are ALREADY in the same set (same root), then this edge connects two nodes
    //    that were already connected through earlier edges - adding it creates a cycle. THIS is
    //    the redundant edge - return it immediately (since we process edges in order, the first
    //    one we find this way is guaranteed to be the LAST such edge overall, matching the
    //    problem's tie-breaking rule, because a tree only needs n-1 edges and any additional
    //    edge connecting an already-unified pair is by definition the "extra" one).
    // 4) Otherwise, union the two sets together (merge them under one root) and continue.
    // Use union by rank/size and path compression for near-constant-time operations.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Union-Find is purpose-built for exactly this "detect when two elements are already
    // connected" query, answering it in near O(1) amortized time per operation (with path
    // compression and union by rank), which is precisely the check needed to detect a cycle-
    // creating edge as soon as it's encountered, in a single linear scan through the edges.
    //
    // ================= EDGE CASES =================
    // - Multiple possible redundant edges structurally, but only one true answer per problem
    //   constraints (exactly one cycle exists): Union-Find naturally finds exactly the one edge
    //   that closes the cycle, processing edges in given order.
    // - Edge list where the redundant edge appears very early: still detected correctly, since
    //   Union-Find only cares about connectivity, not edge position, until union fails.
    // - Self-loop edge (u == v): would immediately fail the "find(u) == find(v)" check since a
    //   node is always its own root initially - correctly flagged as redundant (though atypical
    //   for this problem's usual constraints).
    // - A tree with exactly n-1 edges and no redundant edge at all: not expected per problem
    //   constraints (a redundant edge is guaranteed to exist), but if it did, no edge would ever
    //   trigger the cycle condition.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * alpha(n)) where alpha is the inverse Ackermann function (practically
    // constant) - each of the n edges triggers one or two near-O(1) Union-Find operations with
    // path compression and union by rank, versus O(n^2) for the brute force DFS/BFS-per-edge approach.
    // Space Complexity: O(n) for the parent and rank arrays.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve the DIRECTED graph version of this problem (Redundant Connection II) - what additional cases arise (two candidate edges, one creating a cycle, one causing a node to have two parents)?
    // - Why does processing edges IN ORDER and returning the first cycle-creating edge automatically satisfy the "return the last valid answer" tie-breaking rule?
    // - How does path compression and union by rank each individually contribute to the near-O(1) amortized time complexity?
    // - How would you find ALL edges that, if removed, would restore the graph to a valid tree (not just one)?
    // - How would you adapt this to detect a cycle in a general graph (not guaranteed to be tree+1 edge) and return ALL edges involved in cycles?
    // - What's the difference between this Union-Find-based cycle detection and DFS-based cycle detection with parent tracking (see GraphCycleDetection)?

    static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // path compression
            }
            return parent[x];
        }

        // Returns false if x and y are already in the same set (union would create a cycle).
        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) {
                return false; // already connected - this edge is redundant
            }
            // Union by rank: attach the smaller tree under the larger tree's root.
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            return true;
        }
    }

    // Optimized: Union-Find - the first edge that fails to union (already connected) is redundant.
    public static int[] findRedundantConnection(int[][] edges) {
        int n = edges.length;
        UnionFind unionFind = new UnionFind(n + 1); // nodes are 1-indexed

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            if (!unionFind.union(u, v)) {
                // Step: u and v were already connected - this edge closes the cycle.
                return edge;
            }
        }

        throw new IllegalArgumentException("No redundant edge found");
    }

    public static void main(String[] args) {
        int[][] edges1 = {{1, 2}, {1, 3}, {2, 3}};
        // Expected: [2, 3]
        System.out.println("Input: [[1,2],[1,3],[2,3]]");
        System.out.println("Output: " + Arrays.toString(findRedundantConnection(edges1)));

        int[][] edges2 = {{1, 2}, {2, 3}, {3, 4}, {1, 4}, {1, 5}};
        // Expected: [1, 4]
        System.out.println("\nInput: [[1,2],[2,3],[3,4],[1,4],[1,5]]");
        System.out.println("Output: " + Arrays.toString(findRedundantConnection(edges2)));

        int[][] edges3 = {{1, 2}, {2, 3}};
        // Expected: no redundant edge (well-formed 3-node tree, not expected per real input, shown for completeness)
        System.out.println("\nInput: [[1,2],[2,3]] (a valid tree, no cycle - edge case)");
        try {
            System.out.println("Output: " + Arrays.toString(findRedundantConnection(edges3)));
        } catch (IllegalArgumentException e) {
            System.out.println("Output: " + e.getMessage());
        }
    }
}
