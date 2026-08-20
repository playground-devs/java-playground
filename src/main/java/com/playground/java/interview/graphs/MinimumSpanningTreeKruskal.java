package com.playground.java.interview.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Graph / Minimum Spanning Tree (Kruskal's Algorithm, Union-Find)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a weighted, undirected, connected graph, find the Minimum Spanning
 * Tree (MST) - the subset of edges connecting all vertices with the minimum total edge weight
 * and no cycles.
 */
public class MinimumSpanningTreeKruskal {

    // ================= PROBLEM =================
    // You are given a connected, undirected graph with weighted edges. Find a Minimum Spanning
    // Tree: a subset of the edges that connects all vertices together, contains no cycles, and
    // has the smallest possible total edge weight among all such spanning trees.
    // Example: vertices = 4, edges = [[0,1,10],[0,2,6],[0,3,5],[1,3,15],[2,3,4]]
    //          -> MST edges = [2,3,4],[0,3,5],[0,1,10] with total weight = 19
    //
    // ================= SIMPLE APPROACH =================
    // Consider all possible subsets of edges that form a spanning tree (exactly V-1 edges,
    // connecting all vertices with no cycle), and pick the one with the smallest total weight.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // The number of possible spanning trees grows combinatorially with the number of edges,
    // making brute-force enumeration infeasible for anything but tiny graphs.
    //
    // ================= OPTIMIZED APPROACH =================
    // Kruskal's Algorithm - a greedy approach using Union-Find to avoid cycles:
    // 1) Sort ALL edges in the graph by weight, ascending.
    // 2) Initialize Union-Find with each vertex in its own separate set.
    // 3) Walk through the sorted edges from smallest weight to largest. For each edge (u, v,
    //    weight): if u and v are already in the same Union-Find set, adding this edge would
    //    create a cycle - skip it. Otherwise, union u and v's sets together, and add this edge
    //    to the MST result (accumulate its weight into the total).
    // 4) Stop early once V-1 edges have been added to the MST (a spanning tree on V vertices
    //    always has exactly V-1 edges) - no need to examine remaining edges.
    // The greedy choice of "always take the next cheapest edge that doesn't create a cycle" is
    // provably optimal for MST construction (the "cut property" of MSTs guarantees this).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Union-Find is the perfect fit here because Kruskal's algorithm's core repeated operation
    // is "are these two vertices already connected in the MST being built so far" - Union-Find
    // answers this in near O(1) amortized time (with path compression and union by rank),
    // letting the greedy edge-by-edge selection process run efficiently across all edges after
    // one initial sort.
    //
    // ================= EDGE CASES =================
    // - Disconnected graph: no spanning tree exists that connects everything - Kruskal's
    //   algorithm will simply add fewer than V-1 edges and stop (running out of edges to
    //   consider); this should be detected and reported rather than silently returning a
    //   partial forest as if it were a full MST.
    // - Graph with duplicate edge weights: Kruskal's algorithm still works correctly (ties are
    //   broken arbitrarily by sort order, but the total MST weight remains optimal regardless of
    //   which tied edge is chosen).
    // - Single vertex, no edges: trivially, the MST is empty (0 edges, 0 total weight).
    // - Graph that is already a tree (exactly V-1 edges, no cycles): Kruskal's algorithm simply
    //   includes every edge, since none of them would ever create a cycle.
    // - Self-loop edges (u == v): would always be skipped since u and v are already the same
    //   vertex (already unioned with itself) - correctly never included in the MST.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(E log E) - dominated by sorting all edges; the subsequent Union-Find
    // operations across all edges take O(E * alpha(V)) which is nearly linear (alpha = inverse
    // Ackermann function, practically constant).
    // Space Complexity: O(V) for the Union-Find parent/rank arrays, plus O(E) if edges are
    // copied/sorted into a new structure.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does Kruskal's algorithm compare to Prim's algorithm (which grows the MST from a single starting vertex using a min-heap) - when would you prefer one over the other?
    // - Why is Kruskal's greedy edge-by-edge selection provably optimal (what is the "cut property" of MSTs)?
    // - How would you detect and report that the input graph is disconnected (no valid spanning tree exists)?
    // - How would you find the Maximum Spanning Tree instead (hint: sort descending, or negate weights)?
    // - How would you adapt Kruskal's algorithm to run incrementally as new edges are added to a live/streaming graph?
    // - What's the relationship between Kruskal's MST algorithm and the RedundantConnectionUnionFind cycle-detection technique?

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

        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) {
                return false; // already connected - would create a cycle
            }
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

    // Optimized: Kruskal's algorithm - sort edges by weight, add via Union-Find, skip cycles.
    // Each edge is represented as {u, v, weight}. Returns the MST edges, or fewer than V-1
    // edges if the graph is disconnected.
    public static List<int[]> minimumSpanningTree(int vertices, int[][] edges) {
        // Step 1: sort all edges by weight, ascending.
        int[][] sortedEdges = edges.clone();
        Arrays.sort(sortedEdges, (a, b) -> Integer.compare(a[2], b[2]));

        UnionFind unionFind = new UnionFind(vertices);
        List<int[]> mstEdges = new ArrayList<>();
        int edgesUsed = 0;

        // Step 2: greedily take the next cheapest edge that doesn't create a cycle.
        for (int[] edge : sortedEdges) {
            if (edgesUsed == vertices - 1) {
                break; // spanning tree is complete
            }
            int u = edge[0];
            int v = edge[1];
            if (unionFind.union(u, v)) {
                mstEdges.add(edge);
                edgesUsed++;
            }
        }

        return mstEdges;
    }

    public static void main(String[] args) {
        int vertices1 = 4;
        int[][] edges1 = {{0, 1, 10}, {0, 2, 6}, {0, 3, 5}, {1, 3, 15}, {2, 3, 4}};
        // Expected MST edges: [2,3,4], [0,3,5], [0,1,10] -> total weight 19
        System.out.println("Input: 4 vertices, edges=[[0,1,10],[0,2,6],[0,3,5],[1,3,15],[2,3,4]]");
        List<int[]> mst1 = minimumSpanningTree(vertices1, edges1);
        int totalWeight1 = 0;
        for (int[] edge : mst1) {
            totalWeight1 += edge[2];
        }
        System.out.println("MST edges: " + mst1.stream().map(Arrays::toString).reduce("", (a, b) -> a + " " + b));
        System.out.println("Total weight: " + totalWeight1);

        int vertices2 = 3;
        int[][] edges2 = {{0, 1, 1}, {1, 2, 2}, {0, 2, 2}};
        // Expected MST edges: [0,1,1], [1,2,2] -> total weight 3
        System.out.println("\nInput: 3 vertices, edges=[[0,1,1],[1,2,2],[0,2,2]]");
        List<int[]> mst2 = minimumSpanningTree(vertices2, edges2);
        int totalWeight2 = 0;
        for (int[] edge : mst2) {
            totalWeight2 += edge[2];
        }
        System.out.println("MST edges: " + mst2.stream().map(Arrays::toString).reduce("", (a, b) -> a + " " + b));
        System.out.println("Total weight: " + totalWeight2);

        int vertices3 = 4;
        int[][] edges3 = {{0, 1, 1}, {2, 3, 1}};
        // Expected: only 2 edges found (disconnected graph, no full spanning tree possible)
        System.out.println("\nInput: 4 vertices, edges=[[0,1,1],[2,3,1]] (disconnected)");
        List<int[]> mst3 = minimumSpanningTree(vertices3, edges3);
        System.out.println("Edges used: " + mst3.size() + " (expected V-1=3 for a full MST - disconnected here)");
    }
}
