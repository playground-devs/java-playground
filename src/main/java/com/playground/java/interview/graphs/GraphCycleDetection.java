package com.playground.java.interview.graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Graph / Cycle Detection (Union-Find for Undirected, DFS Recursion-Stack for Directed)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Detect whether a cycle exists in an undirected graph, and separately,
 * whether a cycle exists in a directed graph - using the two different techniques each case
 * calls for.
 */
public class GraphCycleDetection {

    // ================= PROBLEM =================
    // Given a graph's edges, determine if it contains a cycle. This is commonly asked in TWO
    // distinct flavors that require DIFFERENT techniques:
    // - Undirected graph: edges have no direction (a-b implies b-a). Example: edges
    //   [[0,1],[1,2],[2,0]] on 3 nodes -> cycle exists (0-1-2-0).
    // - Directed graph: edges have direction (a->b does NOT imply b->a). Example: edges
    //   [[0,1],[1,2],[2,0]] -> cycle exists (0->1->2->0). But edges [[0,1],[0,2],[1,2]] has NO
    //   cycle, even though node 2 is reachable two ways - there's no directed cycle.
    //
    // ================= SIMPLE APPROACH =================
    // For undirected graphs: a plain DFS marking visited nodes, and flagging a cycle if you ever
    // reach an already-visited node that ISN'T the immediate parent you came from.
    // For directed graphs: a plain DFS marking visited nodes, flagging a cycle if you ever reach
    // an already-visited node at all (this is WRONG for directed graphs, explained below).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // For undirected graphs, forgetting to exclude the immediate parent node causes false
    // positives, since every undirected edge is inherently a 2-node "cycle" back to where you
    // came from unless explicitly excluded.
    // For directed graphs, a simple "visited" flag alone is NOT enough and gives WRONG answers:
    // reaching an already-visited node that is NOT currently on the active recursion path (i.e.
    // it was fully explored and returned from earlier, via a completely different path) is
    // perfectly fine in a DAG and does NOT indicate a cycle - only reaching a node that's
    // CURRENTLY being explored (on the current DFS recursion stack) indicates a real cycle.
    //
    // ================= OPTIMIZED APPROACH =================
    // Undirected graph -> Union-Find (or DFS with parent tracking):
    // - Union-Find: process each edge (u, v). If u and v are already in the same set, this edge
    //   closes a cycle - return true immediately. Otherwise union them and continue.
    // - (Alternative: DFS tracking each node's immediate parent; a cycle exists if DFS reaches
    //   an already-visited neighbor that isn't the parent it just came from.)
    //
    // Directed graph -> DFS with a 3-state coloring / recursion-stack tracking:
    // - WHITE (unvisited): never explored.
    // - GRAY (on the current recursion stack, i.e. "in progress"): currently being explored as
    //   part of the current DFS path from the root.
    // - BLACK (fully explored): DFS from this node completely finished, and it led to no cycle.
    // - During DFS from a node, mark it GRAY. For each neighbor: if GRAY, a "back edge" was
    //   found - meaning we've looped back to a node still on the current path -> cycle found.
    //   If WHITE, recurse into it. If BLACK, it's a safe, already-fully-explored node -> skip.
    // - After exploring all neighbors, mark the current node BLACK (done, safe).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Union-Find works for UNDIRECTED graphs because "connected already" is a symmetric,
    // transitive relationship - exactly what disjoint sets model.
    // The 3-color/recursion-stack approach is required for DIRECTED graphs because cycle
    // detection there depends on the notion of a "back edge to an ancestor currently being
    // explored", which is fundamentally about the CURRENT PATH from the root, not just overall
    // reachability - a single "visited" boolean cannot distinguish "already fully explored via
    // another branch" (safe) from "still actively being explored on this branch" (a cycle).
    //
    // ================= EDGE CASES =================
    // - Self-loop edge (node points to itself): a cycle in both undirected and directed graphs.
    // - Disconnected graph (multiple components): must check cycle detection starting from
    //   EVERY unvisited node, not just one starting point, since a cycle could exist in any
    //   component.
    // - Directed graph with a "diamond" shape (0->1, 0->2, 1->3, 2->3): no cycle, but naive
    //   "visited-only" checking would incorrectly flag one, since node 3 is reached twice.
    // - Empty graph (no edges): no cycle by definition.
    // - Undirected graph edge given as [u, v] with u == v (self-loop): should be treated as an
    //   immediate cycle, needs explicit handling if parent-tracking DFS is used (since a self-
    //   loop's "parent" check could otherwise be ambiguous).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(V + E) for both approaches - DFS visits every vertex and edge once;
    // Union-Find processes each edge in near O(1) amortized time with path compression.
    // Space Complexity: O(V) for the visited/color arrays (or Union-Find parent/rank arrays),
    // plus O(V) for the DFS recursion stack in the worst case.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why doesn't Union-Find work directly for detecting cycles in DIRECTED graphs?
    // - Why is a simple "visited" boolean array insufficient for directed cycle detection, but sufficient (with parent tracking) for undirected?
    // - How would you find and print the ACTUAL cycle (the sequence of nodes), not just whether one exists?
    // - How does directed cycle detection relate to topological sort (a DAG has no cycle, and Kahn's algorithm inherently detects cycles too - see CourseScheduleTopoSort)?
    // - How would you detect a cycle in a directed graph using Kahn's BFS-based approach instead of DFS coloring?
    // - What changes if the graph is very large and DFS recursion could cause a stack overflow - how would you convert to an iterative approach?

    // ---- Undirected graph cycle detection: Union-Find ----
    static class UnionFind {
        private final int[] parent;

        UnionFind(int size) {
            parent = new int[size];
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
                return false; // already connected - this edge closes a cycle
            }
            parent[rootX] = rootY;
            return true;
        }
    }

    public static boolean hasCycleUndirected(int numNodes, int[][] edges) {
        UnionFind unionFind = new UnionFind(numNodes);
        for (int[] edge : edges) {
            if (!unionFind.union(edge[0], edge[1])) {
                return true; // union failed - u and v were already connected
            }
        }
        return false;
    }

    // ---- Directed graph cycle detection: DFS with 3-color / recursion-stack tracking ----
    private static final int WHITE = 0; // unvisited
    private static final int GRAY = 1;  // currently on the recursion stack
    private static final int BLACK = 2; // fully explored, safe

    public static boolean hasCycleDirected(int numNodes, int[][] edges) {
        List<List<Integer>> adjacency = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            adjacency.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            adjacency.get(edge[0]).add(edge[1]);
        }

        int[] color = new int[numNodes];
        for (int node = 0; node < numNodes; node++) {
            if (color[node] == WHITE) {
                if (dfsDirected(node, adjacency, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean dfsDirected(int node, List<List<Integer>> adjacency, int[] color) {
        color[node] = GRAY; // mark as "in progress" on the current path
        for (int neighbor : adjacency.get(node)) {
            if (color[neighbor] == GRAY) {
                return true; // back edge to a node on the current path - cycle found
            }
            if (color[neighbor] == WHITE && dfsDirected(neighbor, adjacency, color)) {
                return true;
            }
        }
        color[node] = BLACK; // fully explored, safe
        return false;
    }

    public static void main(String[] args) {
        int[][] undirectedEdges1 = {{0, 1}, {1, 2}, {2, 0}};
        // Expected: true (0-1-2-0 forms a cycle)
        System.out.println("Undirected, 3 nodes, edges=[[0,1],[1,2],[2,0]]");
        System.out.println("hasCycleUndirected: " + hasCycleUndirected(3, undirectedEdges1));

        int[][] undirectedEdges2 = {{0, 1}, {1, 2}};
        // Expected: false (simple path, no cycle)
        System.out.println("\nUndirected, 3 nodes, edges=[[0,1],[1,2]]");
        System.out.println("hasCycleUndirected: " + hasCycleUndirected(3, undirectedEdges2));

        int[][] directedEdges1 = {{0, 1}, {1, 2}, {2, 0}};
        // Expected: true (0->1->2->0 forms a directed cycle)
        System.out.println("\nDirected, 3 nodes, edges=[[0,1],[1,2],[2,0]]");
        System.out.println("hasCycleDirected: " + hasCycleDirected(3, directedEdges1));

        int[][] directedEdges2 = {{0, 1}, {0, 2}, {1, 2}};
        // Expected: false (diamond shape, node 2 reached twice but no directed cycle)
        System.out.println("\nDirected, 3 nodes, edges=[[0,1],[0,2],[1,2]] (diamond, no cycle)");
        System.out.println("hasCycleDirected: " + hasCycleDirected(3, directedEdges2));
    }
}
