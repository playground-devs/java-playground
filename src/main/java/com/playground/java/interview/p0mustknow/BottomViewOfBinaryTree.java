package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * PATTERN: Binary Tree Views
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Print the nodes of a binary tree that are visible when looking at the
 * tree from directly below - one node per horizontal distance from the root.
 */
public class BottomViewOfBinaryTree {

    // ================= PROBLEM =================
    // Imagine standing below the tree and looking straight up. For every vertical
    // "column" (horizontal distance from the root), you can only see the LOWEST node
    // in that column - the last one you'd hit going bottom to top.
    // Horizontal distance (HD): root = 0, left child = parent HD - 1, right child = parent HD + 1.
    //
    // Example tree:
    //              20
    //            /    \
    //          8        22
    //        /   \         \
    //      5      3         25
    //            /  \
    //          10    14
    //
    // HDs:        20(0)
    //           8(-1)   22(1)
    //        5(-2) 3(0)    25(2)
    //             10(-1) 14(0)
    //
    // Bottom view (one node per HD, sorted left-to-right i.e. by HD ascending):
    // HD -2: 5
    // HD -1: 8 first, but 10 also has HD -1 and appears later (deeper) -> 10 wins
    // HD  0: 20 first, then 3, then 14 -> 14 wins (last one seen)
    // HD  1: 22
    // HD  2: 25
    // Expected output: 5 10 3->14(14 wins) 22 25  => [5, 10, 14, 22, 25]
    //
    // ================= SIMPLE APPROACH =================
    // Do a DFS from the root, tracking horizontal distance and depth.
    // For each HD, keep the node with the greatest depth (deepest wins).
    // If depths tie, whichever is encountered by a fixed traversal order can differ from
    // level-order results, so DFS needs extra depth bookkeeping to match true "bottom" view.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // DFS depth-based tie-breaking is more bookkeeping than necessary and it's easy to
    // get the tie-break rule wrong (should later-level nodes overwrite earlier ones at the
    // same HD, or should it be based on strict depth comparison?). It also processes nodes
    // in an order that does not naturally match "last node seen scanning level by level",
    // which is the simplest mental model for this problem.
    //
    // ================= OPTIMIZED APPROACH =================
    // Do a standard level-order traversal (BFS) using a queue, tracking each node's HD.
    // For every node visited, simply OVERWRITE map[HD] = node.val.
    // Because BFS visits level 0, then level 1, then level 2, ... in order, the LAST time
    // we overwrite a given HD is guaranteed to be from the deepest node at that HD -
    // exactly the node visible from the bottom. Then read the map in ascending HD order.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Queue (ArrayDeque) for BFS: guarantees we process nodes level by level, so
    // "last write wins" naturally produces the deepest node per horizontal distance
    // without any manual depth comparison.
    // TreeMap<Integer, Integer> keyed by horizontal distance: TreeMap keeps keys sorted,
    // so after the BFS we can just iterate the map in natural (ascending) key order to get
    // the columns left-to-right, with no separate sorting step. A plain HashMap would require
    // extracting and sorting the keys afterward.
    //
    // ================= EDGE CASES =================
    // - Empty tree (root == null): return an empty list.
    // - Single node: bottom view is just that node.
    // - Skewed tree (all left or all right children): every HD has exactly one node.
    // - Two nodes with the same HD at different levels: deeper one (visited later in BFS) wins.
    // - Two nodes with the same HD at the SAME level (e.g. left child of one node and right
    //   child of another meeting at the same column): BFS visits left-to-right within a level,
    //   so the rightmost one in traversal order overwrites and is shown - this matches the
    //   standard definition of bottom view.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each node is enqueued and dequeued exactly once, and each
    // TreeMap put/overwrite is O(log n), giving O(n log n) overall; the log factor comes
    // purely from the TreeMap's sorted structure (a hash map would give O(n) but need
    // an O(n log n) sort afterward anyway, so total order is the same either way).
    // Space Complexity: O(n) - the queue can hold up to one full level (worst case O(n) for a
    // very wide tree) and the map stores one entry per distinct horizontal distance (at most n).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How is bottom view different from top view? (Top view keeps the FIRST node seen per HD.)
    // - How would you solve this with DFS instead, and what depth tie-breaking rule would you use?
    // - What if two nodes land on the same HD at the same depth - which one should win, and why?
    // - Can you do this without a TreeMap, e.g. using a HashMap plus tracking min/max HD?
    // - How would vertical order traversal (grouping ALL nodes per HD, not just the bottom one)
    //   differ, and how would ties within a column be resolved (by insertion order, by value)?
    // - What's the horizontal distance range and how does it relate to tree width?
    // - How would you adapt this for an n-ary tree?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Small helper node pairing an actual tree node with its horizontal distance, used in BFS.
    static class HDNode {
        TreeNode node;
        int hd;
        HDNode(TreeNode node, int hd) { this.node = node; this.hd = hd; }
    }

    // Optimized approach: BFS + TreeMap<horizontal distance, last value seen>.
    public static List<Integer> bottomView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        // TreeMap keeps horizontal distances sorted automatically.
        Map<Integer, Integer> hdToValue = new TreeMap<>();
        Queue<HDNode> queue = new ArrayDeque<>();
        queue.add(new HDNode(root, 0));

        while (!queue.isEmpty()) {
            HDNode current = queue.poll();
            // Step: overwrite whatever was at this HD before - later (deeper) nodes win.
            hdToValue.put(current.hd, current.node.val);

            if (current.node.left != null) {
                queue.add(new HDNode(current.node.left, current.hd - 1));
            }
            if (current.node.right != null) {
                queue.add(new HDNode(current.node.right, current.hd + 1));
            }
        }

        // Step: TreeMap iteration order is ascending key order = left-to-right columns.
        result.addAll(hdToValue.values());
        return result;
    }

    // Helper: build the sample tree drawn in the PROBLEM section above.
    private static TreeNode buildSampleTree() {
        TreeNode root = new TreeNode(20);
        root.left = new TreeNode(8);
        root.right = new TreeNode(22);
        root.left.left = new TreeNode(5);
        root.left.right = new TreeNode(3);
        root.left.right.left = new TreeNode(10);
        root.left.right.right = new TreeNode(14);
        root.right.right = new TreeNode(25);
        return root;
    }

    public static void main(String[] args) {
        TreeNode sampleTree = buildSampleTree();
        // Expected: [5, 10, 14, 22, 25]
        System.out.println("Sample tree bottom view: " + bottomView(sampleTree));

        TreeNode singleNode = new TreeNode(42);
        // Expected: [42]
        System.out.println("Single node bottom view: " + bottomView(singleNode));

        TreeNode empty = null;
        // Expected: []
        System.out.println("Empty tree bottom view: " + bottomView(empty));

        // Left-skewed tree: 1 -> 2 -> 3 -> 4 (all left children)
        TreeNode skewed = new TreeNode(1);
        skewed.left = new TreeNode(2);
        skewed.left.left = new TreeNode(3);
        skewed.left.left.left = new TreeNode(4);
        // Expected: [4, 3, 2, 1]  (HDs are 0, -1, -2, -3)
        System.out.println("Left-skewed tree bottom view: " + bottomView(skewed));
    }
}
