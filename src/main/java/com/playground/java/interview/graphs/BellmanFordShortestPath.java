package com.playground.java.interview.graphs;

import java.util.Arrays;

/**
 * PATTERN: Graph / Single-Source Shortest Path with Negative Weights (Bellman-Ford)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a weighted directed graph (which may contain negative edge weights),
 * find the shortest distance from a source vertex to every other vertex, and detect negative-
 * weight cycles.
 */
public class BellmanFordShortestPath {

    // ================= PROBLEM =================
    // You are given a directed, weighted graph where edge weights CAN be negative, and a source
    // vertex. Find the shortest distance from the source to every other vertex. If the graph
    // contains a cycle whose total weight is negative and is reachable from the source, shortest
    // paths through it are undefined (you could loop forever, decreasing the "distance"
    // indefinitely) - this must be detected and reported.
    // Example: vertices = 5, edges = [[0,1,-1],[0,2,4],[1,2,3],[1,3,2],[1,4,2],[3,2,5],[3,1,1],[4,3,-3]]
    //          source = 0 -> distances = [0, -1, 2, -2, 1] (no negative cycle here)
    //
    // ================= SIMPLE APPROACH =================
    // (Dijkstra's algorithm would normally be the "simple/standard" approach for single-source
    // shortest paths, but it fundamentally BREAKS in the presence of negative edge weights,
    // since its greedy "never revisit a finalized node" assumption relies on all weights being
    // non-negative. So it isn't a valid brute force here - this problem specifically requires
    // an algorithm designed for negative weights from the start.)
    //
    // ================= OPTIMIZED APPROACH =================
    // Bellman-Ford algorithm:
    // 1) Initialize distance[source] = 0, and distance[all other vertices] = infinity.
    // 2) Relax every edge (u, v, weight) up to V-1 times (V = number of vertices): for each
    //    edge, if distance[u] + weight < distance[v], update distance[v] = distance[u] + weight.
    //    Repeating this V-1 times guarantees convergence, because the shortest path between any
    //    two vertices in a graph with V vertices uses at most V-1 edges (assuming no negative
    //    cycle) - each full pass over all edges can "extend" the correctly-computed shortest
    //    path by one more edge in the worst case.
    // 3) Do ONE more pass (the V-th pass) over all edges: if ANY edge can still be relaxed
    //    (distance[u] + weight < distance[v]), that means a negative-weight cycle exists that's
    //    reachable from the source - report this instead of returning distances.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No priority queue or heap is needed (unlike Dijkstra) - Bellman-Ford relies on brute-force
    // repeated relaxation of ALL edges, which is exactly what makes it correct even with
    // negative weights (Dijkstra's greedy "finalize the minimum unvisited node" trick is invalid
    // once negative weights can retroactively improve an already-finalized distance). A plain
    // distance array and an edge list are all that's required.
    //
    // ================= EDGE CASES =================
    // - No negative weights at all: Bellman-Ford still works correctly, just does more work than
    //   Dijkstra would for the same result (Dijkstra would be preferred in that case for
    //   performance).
    // - Negative edge weight, but no negative CYCLE: shortest distances are still well-defined
    //   and computed correctly.
    // - Negative cycle reachable from the source: must be explicitly detected on the V-th pass
    //   and reported (distances are otherwise meaningless for vertices affected by the cycle).
    // - Negative cycle NOT reachable from the source: does not affect the shortest paths from
    //   the source at all, and should not be falsely reported (only cycles that can actually be
    //   reached should trigger detection - the V-th pass check only flags edges that DO relax
    //   using already-finite distances, so unreachable-cycle edges will have distance =
    //   infinity and never trigger a false relax).
    // - Disconnected vertices (unreachable from source): distance stays infinity for them.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(V * E) - V-1 relaxation passes (plus one more to check for negative
    // cycles), each pass examining every edge once.
    // Space Complexity: O(V) for the distance array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - When would you choose Bellman-Ford over Dijkstra, and vice versa (negative weights vs performance)?
    // - Why does Dijkstra's algorithm fail specifically in the presence of negative edge weights - can you construct a concrete counterexample?
    // - How would you identify and print the ACTUAL vertices involved in a detected negative cycle, not just report that one exists?
    // - How does Bellman-Ford relate to the Floyd-Warshall algorithm (all-pairs shortest paths, also handles negative weights but not negative cycles)?
    // - How would you optimize Bellman-Ford with early termination if no edge was relaxed during an entire pass (the distances have already converged)?
    // - How does SPFA (Shortest Path Faster Algorithm, a queue-based optimization of Bellman-Ford) improve on this in typical (non-adversarial) cases?

    // Result holder: either valid distances, or a flag indicating a negative cycle was found.
    public static class BellmanFordResult {
        public final int[] distances;
        public final boolean hasNegativeCycle;

        BellmanFordResult(int[] distances, boolean hasNegativeCycle) {
            this.distances = distances;
            this.hasNegativeCycle = hasNegativeCycle;
        }
    }

    // Optimized: relax all edges V-1 times, then one more pass to detect negative cycles.
    // Each edge is represented as {u, v, weight}.
    public static BellmanFordResult shortestPath(int vertices, int[][] edges, int source) {
        int[] distances = new int[vertices];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[source] = 0;

        // Step 1: relax every edge V-1 times.
        for (int i = 0; i < vertices - 1; i++) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int weight = edge[2];
                if (distances[u] != Integer.MAX_VALUE && distances[u] + weight < distances[v]) {
                    distances[v] = distances[u] + weight;
                }
            }
        }

        // Step 2: one more pass - if any edge can still be relaxed, a negative cycle exists.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int weight = edge[2];
            if (distances[u] != Integer.MAX_VALUE && distances[u] + weight < distances[v]) {
                return new BellmanFordResult(distances, true);
            }
        }

        return new BellmanFordResult(distances, false);
    }

    public static void main(String[] args) {
        int vertices1 = 5;
        int[][] edges1 = {
                {0, 1, -1}, {0, 2, 4}, {1, 2, 3}, {1, 3, 2}, {1, 4, 2}, {3, 2, 5}, {3, 1, 1}, {4, 3, -3}
        };
        // Expected: distances = [0, -1, 2, -2, 1], no negative cycle
        System.out.println("Input: 5 vertices, edges with a negative weight but no negative cycle, source=0");
        BellmanFordResult result1 = shortestPath(vertices1, edges1, 0);
        System.out.println("Negative cycle: " + result1.hasNegativeCycle);
        System.out.println("Distances: " + Arrays.toString(result1.distances));

        int vertices2 = 3;
        int[][] edges2 = {{0, 1, 1}, {1, 2, -1}, {2, 0, -1}};
        // Expected: negative cycle detected (0->1->2->0 sums to -1)
        System.out.println("\nInput: 3 vertices, edges=[[0,1,1],[1,2,-1],[2,0,-1]] (negative cycle), source=0");
        BellmanFordResult result2 = shortestPath(vertices2, edges2, 0);
        System.out.println("Negative cycle: " + result2.hasNegativeCycle);

        int vertices3 = 4;
        int[][] edges3 = {{0, 1, 5}};
        // Expected: vertex 2 and 3 unreachable (Integer.MAX_VALUE), no negative cycle
        System.out.println("\nInput: 4 vertices, edges=[[0,1,5]] (disconnected), source=0");
        BellmanFordResult result3 = shortestPath(vertices3, edges3, 0);
        System.out.println("Negative cycle: " + result3.hasNegativeCycle);
        System.out.println("Distances: " + Arrays.toString(result3.distances));
    }
}
