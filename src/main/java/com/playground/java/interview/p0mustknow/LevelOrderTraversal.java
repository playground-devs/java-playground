package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * PATTERN: Binary Tree / BFS
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Given a binary tree, return the level-order traversal of its
 * node values, i.e., from left to right, level by level, as a List of Lists.
 */
public class LevelOrderTraversal {

    // ================= PROBLEM =================
    // Given the root of a binary tree, return a List<List<Integer>> where each inner
    // list contains the values of the nodes at that depth, left to right.
    //
    // Example tree:
    //        3
    //       / \
    //      9  20
    //         / \
    //        15  7
    //
    // Expected output: [[3], [9, 20], [15, 7]]
    // Level 0 -> [3]
    // Level 1 -> [9, 20]
    // Level 2 -> [15, 7]

    // ================= SIMPLE APPROACH =================
    // The natural way to "walk level by level" is Breadth-First Search (BFS) using a
    // Queue. Push the root, then repeatedly: capture the current queue size (this is
    // exactly how many nodes are on the current level), drain that many nodes, collect
    // their values into a level list, and push their children for the next round.
    // There isn't a meaningfully different "brute force" here worth calling out
    // separately -- the queue-based BFS *is* the standard and correct approach, so we
    // fold the "why not something simpler" discussion into this section:
    // a naive recursive DFS *can* also produce level order (see optimizedApproachDFS
    // below, kept only to illustrate an alternative), but it requires you to pass the
    // current depth down and index into (or size-check) an output list -- more fiddly
    // and less intuitive than BFS, which mirrors the level structure directly.

    // ================= WHY IT'S NOT ENOUGH =================
    // (Not applicable in the classic "brute force vs optimized" sense -- BFS with a
    // queue already IS the optimal approach for this problem. There is no better
    // asymptotic solution; the only design choice is BFS with a queue (natural, O(n))
    // vs DFS with explicit depth tracking (also O(n) but less natural for "level"
    // semantics). Both are shown below for completeness.)

    // ================= OPTIMIZED APPROACH =================
    // BFS with a Queue, processing one full level per outer-loop iteration:
    // 1. Enqueue root.
    // 2. While queue is not empty:
    //      a. levelSize = queue.size()  <-- snapshot BEFORE draining, this is the key
    //         trick that separates "levels" without needing sentinel markers.
    //      b. Loop levelSize times: poll a node, add its value to the current level
    //         list, enqueue its non-null children.
    //      c. Add the completed level list to the result.
    // This is O(n) time, visiting every node exactly once.

    // ================= WHY THIS DATA STRUCTURE =================
    // A Queue (FIFO) is exactly what gives us "level order": nodes are processed in
    // the same order they were discovered, so all nodes of level k are fully enqueued
    // (as children of level k-1) before any node of level k+1 is enqueued. Snapshotting
    // queue.size() at the top of each while-iteration is what lets us "chunk" the FIFO
    // stream back into discrete levels without extra sentinel/null markers or storing
    // depth alongside each node. A Stack (LIFO) would give DFS ordering instead, which
    // does not naturally group nodes by depth. We use ArrayDeque as the Queue
    // implementation because it's an unbounded, resizable ring buffer that is faster
    // than LinkedList for this purpose (no per-node allocation and better cache
    // locality) -- always prefer ArrayDeque over LinkedList as a Queue/Deque in modern
    // Java code.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> return an empty list (no levels at all).
    // - Single node -> one level containing just that node's value.
    // - Skewed tree (all left or all right children only) -> each level has exactly
    //   one node, so the result looks like a list of n singleton lists; still O(n).
    // - Duplicate values -> level order only cares about structure/position, values
    //   being equal doesn't change the algorithm at all.
    // - Unbalanced tree (mix of deep and shallow subtrees) -> still handled correctly
    //   because each node individually enqueues its own children regardless of depth
    //   elsewhere in the tree.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) -- every node is enqueued exactly once and dequeued
    // exactly once, and we do O(1) work per node (aside from the amortized O(1) cost
    // of ArrayDeque operations).
    // Space Complexity: O(n) -- the queue can hold up to the width of the widest
    // level, which in the worst case (a complete binary tree's last level) is
    // ceil(n/2). The output itself also stores all n values across all levels.
    // No recursion is used in the BFS solution, so there's no call-stack space beyond
    // O(1) local variables; the DFS-with-depth alternative would instead use O(h)
    // recursion call-stack space where h is the tree height.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you do a zig-zag (spiral) level order traversal? (Reverse every
    //   other level, or use a Deque and alternate insert direction per level.)
    // - How would you return only the rightmost node of each level (right side view)?
    //   (Same BFS, but take the last element processed in each level's inner loop.)
    // - How would you compute the level with the maximum sum? (Same BFS, sum instead
    //   of collect, track max.)
    // - Can you do level order without a queue, using recursion + depth tracking?
    //   Yes -- DFS passing depth, appending to result.get(depth), creating a new list
    //   when depth == result.size(). Shown as an alternative here.
    // - What if the tree were an n-ary tree instead of binary? (Same BFS pattern,
    //   just enqueue a variable number of children per node instead of exactly two.)
    // - How does this generalize to graph BFS (shortest path in unweighted graphs)?
    //   Same level-by-level queue mechanics, but you must track a visited set to
    //   avoid revisiting nodes since graphs can have cycles, unlike trees.
    // - What's the space complexity difference between BFS (queue) and DFS
    //   (recursion) for a very wide but shallow tree vs a very deep but narrow tree?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Optimized BFS approach ----------
    public static List<List<Integer>> levelOrderBFS(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) {
            return result; // edge case: empty tree
        }
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // snapshot: exactly how many nodes on this level
            List<Integer> currentLevel = new ArrayList<>(levelSize);
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
            }
            result.add(currentLevel);
        }
        return result;
    }

    // ---------- Alternative: DFS with explicit depth tracking ----------
    public static List<List<Integer>> levelOrderDFS(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        dfsHelper(root, 0, result);
        return result;
    }

    private static void dfsHelper(TreeNode node, int depth, List<List<Integer>> result) {
        if (node == null) {
            return;
        }
        if (depth == result.size()) {
            result.add(new ArrayList<>()); // first time we reach this depth
        }
        result.get(depth).add(node.val);
        dfsHelper(node.left, depth + 1, result);
        dfsHelper(node.right, depth + 1, result);
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildBalancedTree() {
        //        3
        //       / \
        //      9  20
        //         / \
        //        15  7
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(9);
        root.right = new TreeNode(20);
        root.right.left = new TreeNode(15);
        root.right.right = new TreeNode(7);
        return root;
    }

    private static TreeNode buildSkewedTree() {
        //  1
        //   \
        //    2
        //     \
        //      3
        TreeNode root = new TreeNode(1);
        root.right = new TreeNode(2);
        root.right.right = new TreeNode(3);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Balanced tree [3,9,20,null,null,15,7]");
        TreeNode balanced = buildBalancedTree();
        System.out.println("BFS result:  " + levelOrderBFS(balanced));
        System.out.println("DFS result:  " + levelOrderDFS(balanced));
        // Expected: [[3], [9, 20], [15, 7]]

        System.out.println();
        System.out.println("Test 2: Right-skewed tree [1,null,2,null,3]");
        TreeNode skewed = buildSkewedTree();
        System.out.println("BFS result:  " + levelOrderBFS(skewed));
        // Expected: [[1], [2], [3]]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        System.out.println("BFS result:  " + levelOrderBFS(null));
        // Expected: []

        System.out.println();
        System.out.println("Test 4: Single node tree [42]");
        TreeNode single = new TreeNode(42);
        System.out.println("BFS result:  " + levelOrderBFS(single));
        // Expected: [[42]]
    }
}
