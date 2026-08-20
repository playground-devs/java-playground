package com.playground.java.interview.linkedlist;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Linked List / Hashing / Interleaving Trick
 * PRIORITY: P2
 * PROBLEM STATEMENT: Deep copy a linked list where each node has both a next pointer and a random pointer to any node (or null).
 */
public class CopyListWithRandomPointer {

    // ================= PROBLEM =================
    // Given a linked list where each node has a "next" pointer and an extra "random" pointer
    // that can point to ANY node in the list (or null), create a deep copy of the list - all
    // new nodes, with next and random pointers correctly pointing into the NEW list (not the original).
    // Example: original A -> B -> C, A.random = C, B.random = null, C.random = A
    // Output: a completely new list A' -> B' -> C' with A'.random = C', B'.random = null, C'.random = A'.
    //
    // ================= SIMPLE APPROACH =================
    // Two passes using a HashMap<Node, Node> mapping each original node to its clone.
    // Pass 1: walk the original list once, creating a clone for every node (with next/random
    //          left unset for now) and storing orig -> clone in the map.
    // Pass 2: walk the original list again; for each original node, use the map to set
    //          clone.next = map.get(orig.next) and clone.random = map.get(orig.random)
    //          (map.get on a null key/value naturally returns null, which is exactly what we want).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This is a perfectly good O(n) time solution, but it needs O(n) extra space for the
    // HashMap on top of the O(n) space the cloned list itself requires. There's a clever
    // O(1)-extra-space technique (excluding the unavoidable output list) using list
    // interleaving that interviewers often want to see as a follow-up.
    //
    // ================= OPTIMIZED APPROACH =================
    // The interleaving trick avoids a HashMap entirely:
    // Step 1: for each original node "orig", create its clone and splice it in immediately
    //          after orig, so the list becomes orig1 -> clone1 -> orig2 -> clone2 -> ... .
    // Step 2: walk the interleaved list again; for each original node, set
    //          clone.random = (orig.random != null) ? orig.random.next : null.
    //          This works because orig.random's clone is, by construction, sitting right
    //          after orig.random in the interleaved list.
    // Step 3: unweave the interleaved list back into two separate lists - restore the
    //          original list's next pointers, and extract the clone list's next pointers,
    //          being careful to save "next original" before overwriting any pointers.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Interleaving each clone directly after its original turns "orig.random.next" into an
    // implicit O(1) lookup that plays the exact same role as "map.get(orig.random)" would in
    // the HashMap approach - the physical adjacency of orig and its clone in memory (i.e. in
    // the list's pointer structure) IS the mapping, so no separate hash table is needed. This
    // is why the optimized approach only needs O(1) extra pointer variables instead of O(n)
    // map entries, while still achieving the same O(n) time complexity.
    //
    // ================= EDGE CASES =================
    // - Empty list (head == null): return null immediately.
    // - Single node whose random points to itself: after interleaving, orig.random.next
    //   correctly resolves to the node's own clone.
    // - Random pointer is null for some or all nodes: must propagate null correctly, not
    //   throw a NullPointerException.
    // - Random pointers pointing backward (to an earlier node) vs forward (to a later node):
    //   both must work identically since the interleaving trick doesn't depend on direction.
    // - Every node's random points to the same single node: that node's clone must be shared
    //   correctly across all clones' random pointers, not duplicated.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - each does a small constant number of full
    // passes over the list (HashMap approach: 2 passes; interleaving approach: 3 passes),
    // and every pass is linear in the number of nodes.
    // Space Complexity: HashMap approach O(n) for the map storing one entry per node.
    // Interleaving approach O(1) extra space (not counting the new list itself, which is an
    // unavoidable O(n) output) - only a few pointer variables are used throughout.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Walk through exactly why "orig.random.next" gives you the clone of orig.random after interleaving.
    // - What could go wrong if you forgot to save "orig.next" before overwriting it during the unweave step?
    // - How would you adapt this if nodes had a THIRD arbitrary pointer field as well?
    // - Why is the HashMap approach still perfectly acceptable in most interviews despite using more space?
    // - How would you verify (write a quick test) that the random pointers in the clone point into the CLONE list, not the original?
    // - How would this problem change for a doubly linked list with random pointers?
    // - Could you do this recursively with memoization instead of iteratively? What's the trade-off?

    public static class Node {
        int val;
        Node next;
        Node random;

        Node(int val) {
            this.val = val;
        }
    }

    // Brute force: HashMap<Node, Node> mapping original nodes to their clones, two passes.
    public static Node copyRandomListBruteForce(Node head) {
        if (head == null) {
            return null;
        }

        Map<Node, Node> origToClone = new HashMap<>();

        // Pass 1: create a clone for every original node (next/random unset for now).
        for (Node curr = head; curr != null; curr = curr.next) {
            origToClone.put(curr, new Node(curr.val));
        }

        // Pass 2: wire up next and random pointers using the map.
        for (Node curr = head; curr != null; curr = curr.next) {
            Node clone = origToClone.get(curr);
            clone.next = origToClone.get(curr.next);     // null if curr.next is null
            clone.random = origToClone.get(curr.random); // null if curr.random is null
        }

        return origToClone.get(head);
    }

    // Optimized: interleave clones into the original list to avoid a HashMap, O(1) extra space.
    public static Node copyRandomListOptimized(Node head) {
        if (head == null) {
            return null;
        }

        // Step 1: interleave - orig1 -> clone1 -> orig2 -> clone2 -> ...
        for (Node curr = head; curr != null; curr = curr.next.next) {
            Node clone = new Node(curr.val);
            clone.next = curr.next;
            curr.next = clone;
        }

        // Step 2: set random pointers on the clones using the interleaved structure.
        for (Node curr = head; curr != null; curr = curr.next.next) {
            Node clone = curr.next;
            clone.random = (curr.random != null) ? curr.random.next : null;
        }

        // Step 3: unweave into two separate lists - restore original, extract clone list.
        Node cloneHead = head.next;
        Node curr = head;
        while (curr != null) {
            Node clone = curr.next;
            curr.next = clone.next;               // restore original's next (skip the clone)
            clone.next = (clone.next != null) ? clone.next.next : null; // clone's real next
            curr = curr.next;
        }

        return cloneHead;
    }

    private static Node buildList(int[] values, int[] randomIndices) {
        if (values.length == 0) {
            return null;
        }
        Node[] nodes = new Node[values.length];
        for (int i = 0; i < values.length; i++) {
            nodes[i] = new Node(values[i]);
        }
        for (int i = 0; i < values.length - 1; i++) {
            nodes[i].next = nodes[i + 1];
        }
        for (int i = 0; i < values.length; i++) {
            nodes[i].random = (randomIndices[i] == -1) ? null : nodes[randomIndices[i]];
        }
        return nodes[0];
    }

    private static String describe(Node head) {
        StringBuilder sb = new StringBuilder();
        Node curr = head;
        // Use a map from node identity to a printable index for random pointer labeling.
        Map<Node, Integer> indexOf = new HashMap<>();
        int idx = 0;
        for (Node n = head; n != null; n = n.next) {
            indexOf.put(n, idx++);
        }
        for (Node n = head; n != null; n = n.next) {
            String randomLabel = (n.random == null) ? "null" : String.valueOf(indexOf.get(n.random));
            sb.append("[val=").append(n.val).append(", random->").append(randomLabel).append("] ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Node original1 = buildList(new int[]{7, 13, 11, 10, 1}, new int[]{-1, 0, 4, 2, 0});
        System.out.println("Input: [7,13,11,10,1] with random indices [-1,0,4,2,0]");
        // Expected: same val/random structure as input, but on entirely new node objects
        System.out.println("Original: " + describe(original1));
        Node copy1 = copyRandomListOptimized(original1);
        System.out.println("Optimized copy: " + describe(copy1));
        System.out.println("Original list still intact: " + describe(original1));

        Node original2 = buildList(new int[]{7, 13, 11, 10, 1}, new int[]{-1, 0, 4, 2, 0});
        Node copy2 = copyRandomListBruteForce(original2);
        System.out.println("\nBrute force copy: " + describe(copy2));

        Node single = buildList(new int[]{1}, new int[]{0});
        System.out.println("\nInput: single node whose random points to itself");
        // Expected: [val=1, random->0] where 0 refers to the clone's own index
        System.out.println("Optimized copy: " + describe(copyRandomListOptimized(single)));

        System.out.println("\nInput: empty list");
        // Expected: null
        System.out.println("Optimized copy: " + copyRandomListOptimized(null));
    }
}
