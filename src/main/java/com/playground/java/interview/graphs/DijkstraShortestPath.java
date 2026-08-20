package com.playground.java.interview.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * PATTERN: Graph / Single-Source Shortest Path with Non-Negative Weights (Dijkstra)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given a weighted directed graph with non-negative edge weights, find the
 * shortest distance from a source node to every other node.
 */
public class DijkstraShortestPath {

    // ================= PROBLEM =================
    // Given a directed, weighted graph where every edge weight is non-negative, and a source
    // node, compute the shortest distance from the source to every other node.
    // Example: 4 nodes, edges 0->1(4), 0->2(1), 2->1(2), 1->3(1), 2->3(5), source = 0
    //          -> distances = [0, 3, 1, 4]
    //          (shortest path to node 1 is 0->2->1 costing 1+2=3, not the direct 0->1 costing 4)
    //
    // ================= SIMPLE APPROACH =================
    // Repeatedly scan every edge in the graph and "relax" it (if dist[u] + weight < dist[v],
    // update dist[v]), looping over the full edge list again and again until a complete pass
    // produces no more updates.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Blindly re-relaxing every edge over and over (up to O(V) full passes) wastes a lot of
    // work once we know all weights are non-negative - that guarantee lets us always safely
    // "finalize" the closest unvisited node first, instead of repeatedly re-checking edges that
    // could never actually improve an already-optimal distance.
    //
    // ================= OPTIMIZED APPROACH =================
    // Dijkstra's algorithm: initialize dist[source] = 0 and dist[all others] = infinity. Use a
    // min-heap (PriorityQueue<int[]>) of {distance, node} pairs ordered by distance ascending,
    // seeded with {0, source}.
    // - Pop the entry with the smallest distance. Because all weights are non-negative, the
    //   moment a node is popped, its recorded distance is guaranteed to be its true shortest
    //   distance - nothing still in the heap could ever produce something smaller.
    // - If this node was already finalized (visited), skip it (it's a stale, outdated entry).
    // - Otherwise, finalize it, then relax every outgoing edge: for a neighbor with weight w, if
    //   dist[current] + w < dist[neighbor], update dist[neighbor] and push {newDist, neighbor}.
    // - Repeat until the heap is empty.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A min-heap gives O(log n) extraction of the current smallest-distance node, which is what
    // "always process the closest unvisited node next" requires - far better than an O(V) linear
    // scan every iteration (the classic array-based O(V^2) Dijkstra). An adjacency list gives
    // O(1) access to a node's outgoing edges. Together they achieve O((V+E) log V), which scales
    // much better than O(V^2) on sparse graphs.
    //
    // ================= EDGE CASES =================
    // - Source has no outgoing edges: dist[source] = 0, everything else stays infinity.
    // - Disconnected graph: unreachable nodes keep dist = infinity (e.g. Integer.MAX_VALUE).
    // - Self-loops: harmless, relaxing them can never improve dist[node] since weight >= 0.
    // - Multiple parallel edges between the same pair of nodes: relaxation naturally settles on
    //   the smaller effective distance through repeated pops/pushes.
    // - A negative edge weight is present: Dijkstra's greedy "finalize and never revisit" step
    //   breaks - it may silently produce a WRONG (too large) answer with no error or warning.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O((V + E) log V) - each node/edge causes at most one heap push, and each
    // push/pop costs O(log V).
    // Space Complexity: O(V + E) - O(V) for dist[]/visited[], O(E) for the adjacency list, and up
    // to O(E) for the heap in the worst case (stale duplicate entries).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does Dijkstra fail with negative edge weights - can you construct a concrete counterexample where finalizing a node too early gives a wrong answer?
    // - What algorithm correctly handles negative weights, and how does it fix this (Bellman-Ford, already implemented separately in this repo)?
    // - How would you reconstruct the actual shortest PATH, not just the distance, using a parent[] array?
    // - How would you detect a negative cycle if you suspected one - why can't Dijkstra do this on its own?
    // - When might the array-based O(V^2) Dijkstra actually outperform the heap-based O((V+E) log V) version?
    // - How would you adapt this for the "cheapest flights within K stops" variant, where the state also needs to track stops used?

    public static int[] dijkstra(List<List<int[]>> adjacencyList, int source, int numNodes) {
        int[] dist = new int[numNodes];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        boolean[] visited = new boolean[numNodes];
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> a[0] - b[0]); // {distance, node}
        minHeap.offer(new int[]{0, source});

        while (!minHeap.isEmpty()) {
            int[] top = minHeap.poll();
            int currentDist = top[0];
            int currentNode = top[1];

            if (visited[currentNode]) {
                // Step: stale entry - this node was already finalized with a better distance.
                continue;
            }
            visited[currentNode] = true; // Step: finalize - safe since weights are non-negative.

            for (int[] edge : adjacencyList.get(currentNode)) {
                int neighbor = edge[0];
                int weight = edge[1];
                if (!visited[neighbor] && currentDist + weight < dist[neighbor]) {
                    // Step: found a shorter path to neighbor - relax the edge.
                    dist[neighbor] = currentDist + weight;
                    minHeap.offer(new int[]{dist[neighbor], neighbor});
                }
            }
        }

        return dist;
    }

    private static List<List<int[]>> buildAdjacencyList(int numNodes, int[][] edges) {
        List<List<int[]>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            adjacencyList.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            adjacencyList.get(edge[0]).add(new int[]{edge[1], edge[2]}); // {neighbor, weight}
        }
        return adjacencyList;
    }

    public static void main(String[] args) {
        int numNodes1 = 4;
        int[][] edges1 = {{0, 1, 4}, {0, 2, 1}, {2, 1, 2}, {1, 3, 1}, {2, 3, 5}};
        List<List<int[]>> adjacencyList1 = buildAdjacencyList(numNodes1, edges1);
        // Expected: [0, 3, 1, 4]
        System.out.println("Input: 4 nodes, edges 0->1(4),0->2(1),2->1(2),1->3(1),2->3(5), source=0");
        System.out.println("Output: " + Arrays.toString(dijkstra(adjacencyList1, 0, numNodes1)));

        int numNodes2 = 3;
        int[][] edges2 = {{0, 1, 5}};
        List<List<int[]>> adjacencyList2 = buildAdjacencyList(numNodes2, edges2);
        // Expected: [0, 5, MAX_VALUE] (node 2 unreachable)
        System.out.println("\nInput: 3 nodes, edges 0->1(5) (disconnected), source=0");
        System.out.println("Output: " + Arrays.toString(dijkstra(adjacencyList2, 0, numNodes2)));

        int numNodes3 = 1;
        int[][] edges3 = {};
        List<List<int[]>> adjacencyList3 = buildAdjacencyList(numNodes3, edges3);
        // Expected: [0] (single node, no edges)
        System.out.println("\nInput: 1 node, no edges, source=0");
        System.out.println("Output: " + Arrays.toString(dijkstra(adjacencyList3, 0, numNodes3)));
    }
}
