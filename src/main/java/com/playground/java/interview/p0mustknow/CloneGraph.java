package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * PATTERN: Graph / BFS-DFS (Deep Copy with Cycle Handling)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given a reference to a node in an undirected, possibly cyclic graph,
 * return a deep copy (clone) of the entire connected graph.
 */
public class CloneGraph {

    // ================= PROBLEM =================
    // Each node has a value and a list of neighbor references. Neighbors point to each
    // other, and cycles are common (undirected edges mean A -> B implies B -> A).
    // You must produce a completely separate copy of the graph: same structure and values,
    // but made of brand-new node objects, so mutating the clone never affects the original.
    //
    // Example graph (undirected, node numbers are values, edges shown both ways):
    //   1 -- 2
    //   |    |
    //   4 -- 3
    // Adjacency: 1: [2,4], 2: [1,3], 3: [2,4], 4: [1,3]
    // This graph has a cycle: 1 -> 2 -> 3 -> 4 -> 1.
    // Expected output: a new graph with the same adjacency (1':[2',4'], 2':[1',3'],
    // 3':[2',4'], 4':[1',3']) where every node is a distinct new object from the original.
    //
    // ================= SIMPLE APPROACH =================
    // Start at the given node, recursively visit every neighbor, and for each node create
    // a clone. Copy the value and then recursively clone each neighbor too.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Because the graph is cyclic (and even a simple back-and-forth edge like 1<->2 counts
    // as a cycle from a traversal point of view), naively recursing into every neighbor
    // without remembering "have I already cloned this node" causes infinite recursion -
    // clone(1) clones neighbor 2, which tries to clone neighbor 1, which tries to clone
    // neighbor 2 again, forever. You need a way to detect "already cloned" and reuse that
    // clone instead of creating a new one and recursing again.
    //
    // ================= OPTIMIZED APPROACH =================
    // Keep a Map<OriginalNode, ClonedNode> as you traverse. Two ways to traverse, both shown:
    // 1) BFS: start a queue with the original start node. Clone it immediately and record it
    //    in the map. Pop nodes, and for each neighbor: if not yet cloned, clone it, record it
    //    in the map, and enqueue it; either way, add the (already or newly) cloned neighbor to
    //    the current clone's neighbor list.
    // 2) DFS (recursive): if the node is already in the map, return its clone immediately
    //    (this is what breaks the infinite cycle). Otherwise, create the clone, put it in the
    //    map BEFORE recursing into neighbors (critical ordering), then recursively clone each
    //    neighbor and add it to the clone's neighbor list.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // HashMap<OriginalNode, ClonedNode>: gives O(1) average lookup for "have I already cloned
    // this original node", which is exactly what's needed to (a) avoid infinite loops on
    // cycles - we stop recursing/enqueueing once a node is already mapped - and (b) avoid
    // creating duplicate clones for the same original node when it's reachable via multiple
    // paths (e.g. node 1 is a neighbor of both 2 and 4).
    // Queue (ArrayDeque) for BFS: standard graph traversal frontier, ensures every original
    // node is dequeued and processed exactly once.
    // The recursion call stack for DFS: implicitly tracks the current path; combined with the
    // map check, this is what prevents the infinite recursion described above.
    //
    // ================= EDGE CASES =================
    // - Null input node: return null immediately (nothing to clone).
    // - Single isolated node with no neighbors: clone just that one node with an empty list.
    // - Self-loop (a node that is its own neighbor): map check still prevents infinite recursion.
    // - Disconnected graph: cloneGraph only clones the connected component reachable from the
    //   given start node - other components are not visited at all (by definition of the problem).
    // - Graph with a cycle (as in the example above): must not infinitely recurse or re-clone nodes.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(V + E) - every node (vertex) is cloned exactly once, and every edge
    // is processed exactly once (or twice for an undirected edge represented both ways) to
    // wire up neighbor lists.
    // Space Complexity: O(V) for the HashMap holding one entry per original node, plus O(V)
    // for the BFS queue (worst case) or O(V) DFS recursion stack depth in the worst case
    // (e.g. a long path-like graph).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must the clone be placed in the map BEFORE recursing into its neighbors (DFS)?
    //   (Otherwise a cycle back to this node would trigger infinite recursion before the map entry exists.)
    // - How would you clone a DIRECTED graph instead - does the algorithm change?
    // - What if the graph is disconnected and you need to clone ALL components, not just one?
    // - How would you detect if the clone is a truly independent deep copy (no shared references)?
    // - What if node values are not unique - does that break the map-based approach? (No, we
    //   key by object reference/identity, not by value.)
    // - How would you handle a graph so large it doesn't fit in memory for a full clone at once?
    // - Could you use Union-Find or an iterative stack-based DFS instead of recursion? Why might you want to?

    // Graph node definition: value plus list of neighbor references (undirected edges).
    static class Node {
        int val;
        List<Node> neighbors;
        Node(int val) {
            this.val = val;
            this.neighbors = new ArrayList<>();
        }
    }

    // Approach 1: BFS with a HashMap<original, clone>.
    public static Node cloneGraphBFS(Node startNode) {
        if (startNode == null) {
            return null;
        }

        Map<Node, Node> originalToClone = new HashMap<>();
        Node startClone = new Node(startNode.val);
        originalToClone.put(startNode, startClone);

        Queue<Node> queue = new ArrayDeque<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node currentOriginal = queue.poll();
            for (Node neighbor : currentOriginal.neighbors) {
                if (!originalToClone.containsKey(neighbor)) {
                    // Step: first time seeing this neighbor - clone it and enqueue for its own processing.
                    originalToClone.put(neighbor, new Node(neighbor.val));
                    queue.add(neighbor);
                }
                // Step: wire up the current clone's neighbor list using the (existing or new) clone.
                originalToClone.get(currentOriginal).neighbors.add(originalToClone.get(neighbor));
            }
        }
        return startClone;
    }

    // Approach 2: DFS (recursive) with a HashMap<original, clone>.
    public static Node cloneGraphDFS(Node startNode) {
        return dfsClone(startNode, new HashMap<>());
    }

    private static Node dfsClone(Node original, Map<Node, Node> originalToClone) {
        if (original == null) {
            return null;
        }
        // Step: if already cloned, return the existing clone immediately - this is what
        // breaks infinite recursion on cycles.
        if (originalToClone.containsKey(original)) {
            return originalToClone.get(original);
        }

        // Step: create the clone and register it BEFORE recursing into neighbors,
        // so a cycle back to this node finds it already mapped.
        Node clone = new Node(original.val);
        originalToClone.put(original, clone);

        for (Node neighbor : original.neighbors) {
            clone.neighbors.add(dfsClone(neighbor, originalToClone));
        }
        return clone;
    }

    // Helper: build the 4-node cyclic sample graph drawn in the PROBLEM section above.
    // 1 -- 2
    // |    |
    // 4 -- 3
    private static Node buildSampleGraph() {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        n1.neighbors.add(n2);
        n1.neighbors.add(n4);
        n2.neighbors.add(n1);
        n2.neighbors.add(n3);
        n3.neighbors.add(n2);
        n3.neighbors.add(n4);
        n4.neighbors.add(n1);
        n4.neighbors.add(n3);
        return n1;
    }

    // Helper: print a graph's adjacency starting from a given node (BFS print, visited-guarded).
    private static String describeGraph(Node start) {
        if (start == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        Map<Node, Boolean> visited = new HashMap<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(start);
        visited.put(start, true);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            sb.append(node.val).append(":[");
            for (Node neighbor : node.neighbors) {
                sb.append(neighbor.val).append(",");
            }
            sb.append("] ");
            for (Node neighbor : node.neighbors) {
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, true);
                    queue.add(neighbor);
                }
            }
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        Node sampleGraph = buildSampleGraph();
        // Expected: 1:[2,4] 2:[1,3] 3:[2,4] 4:[1,3]  (structure identical, but new objects)
        Node clonedBFS = cloneGraphBFS(sampleGraph);
        System.out.println("Original graph: " + describeGraph(sampleGraph));
        System.out.println("Cloned graph (BFS): " + describeGraph(clonedBFS));
        System.out.println("Clone is a different object than original start node: " + (clonedBFS != sampleGraph));

        Node clonedDFS = cloneGraphDFS(sampleGraph);
        System.out.println("Cloned graph (DFS): " + describeGraph(clonedDFS));

        // Single isolated node, no neighbors.
        Node isolated = new Node(42);
        // Expected: 42:[]
        System.out.println("Isolated node clone: " + describeGraph(cloneGraphBFS(isolated)));

        // Self-loop node: neighbor list contains itself.
        Node selfLoop = new Node(7);
        selfLoop.neighbors.add(selfLoop);
        // Expected: 7:[7] (clone also loops to itself, not back to the original)
        Node clonedSelfLoop = cloneGraphDFS(selfLoop);
        System.out.println("Self-loop clone: " + describeGraph(clonedSelfLoop));
        System.out.println("Self-loop clone points to itself, not original: "
                + (clonedSelfLoop.neighbors.get(0) == clonedSelfLoop)
                + " / not original: " + (clonedSelfLoop.neighbors.get(0) != selfLoop));

        // Null input.
        // Expected: null
        System.out.println("Null input clone: " + cloneGraphBFS(null));
    }
}
