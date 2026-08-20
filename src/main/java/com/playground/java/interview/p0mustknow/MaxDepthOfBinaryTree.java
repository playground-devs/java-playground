package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * PATTERN: Binary Tree / DFS & BFS
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Find the maximum depth (height) of a binary tree, i.e., the
 * number of nodes on the longest path from the root down to the farthest leaf.
 */
public class MaxDepthOfBinaryTree {

    // ================= PROBLEM =================
    // Given the root of a binary tree, return its maximum depth: the number of nodes
    // along the longest path from the root node down to the farthest leaf node.
    //
    // Example tree:
    //        3
    //       / \
    //      9  20
    //         / \
    //        15  7
    //
    // Longest path: 3 -> 20 -> 15 (or 3 -> 20 -> 7), which has 3 nodes.
    // Expected output: 3

    // ================= SIMPLE APPROACH =================
    // This is one of the rare problems where the natural recursive solution IS the
    // optimized solution -- there is no meaningfully worse "brute force" to contrast
    // it with (unlike, say, a naive O(n^2) approach for a different problem), so we
    // fold "why it's not enough" into this section instead of a separate one.
    //
    // The recursive insight: the depth of a tree rooted at `node` is 1 (for the node
    // itself) plus the larger of its two subtrees' depths. The base case is a null
    // node, which has depth 0. This translates directly into a 3-line recursive
    // method:
    //     maxDepth(node) = 0                                            if node == null
    //     maxDepth(node) = 1 + max(maxDepth(node.left), maxDepth(node.right))  otherwise
    // This recursive approach is already optimal in time (O(n)); the only thing an
    // interviewer might push on is the O(h) call-stack space it uses, which motivates
    // the iterative BFS alternative below.

    // ================= WHY IT'S NOT ENOUGH =================
    // (Merged into Simple Approach above: recursion is already optimal here. The one
    // legitimate concern is stack-overflow risk on a very deep, skewed tree with,
    // say, 100,000+ nodes in a single chain -- each recursive call consumes a stack
    // frame, and unlike balanced trees where h = O(log n), a skewed tree has h = O(n),
    // which can exceed the JVM's default thread stack size. That risk is what
    // motivates showing an iterative version.)

    // ================= OPTIMIZED APPROACH =================
    // Iterative BFS level-counting: perform a standard level-order traversal with a
    // Queue, and simply count how many full levels we process before the queue is
    // empty. Each pass through the outer while-loop corresponds to exactly one level
    // of depth, so incrementing a counter once per outer iteration gives the answer
    // directly, without needing to explicitly store level contents.

    // ================= WHY THIS DATA STRUCTURE =================
    // A Queue (FIFO), via ArrayDeque, is used for the BFS approach because it
    // naturally processes nodes level by level: all nodes at depth d are dequeued and
    // have their children enqueued before any node at depth d+1 is dequeued. By
    // capturing queue.size() before draining a level (the same trick used in
    // LevelOrderTraversal), we know exactly how many nodes belong to the current
    // depth, letting us increment our depth counter exactly once per level rather
    // than once per node. For the recursive approach, no explicit data structure is
    // needed at all -- the JVM's own call stack implicitly tracks "how deep are we
    // right now," which is precisely what maxDepth is asking us to measure; each
    // return unwinds one level and the max() combines both subtrees' reported depths.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> depth is 0 (no nodes at all).
    // - Single node -> depth is 1.
    // - Skewed tree (all left or all right children) -> depth equals the number of
    //   nodes, n; this is also the worst case for recursion call-stack depth.
    // - Duplicate values -> irrelevant to depth, which depends purely on structure.
    // - Unbalanced tree (one subtree much deeper than the other) -> max() correctly
    //   picks the deeper side; a common bug is accidentally using min() or summing
    //   both sides instead of taking the max of the two subtree depths.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches -- every node is visited exactly once
    // (once via recursive call, once via enqueue/dequeue in BFS).
    // Space Complexity:
    //   Recursive: O(h) call-stack space, where h is tree height -- O(log n) for a
    //   balanced tree, O(n) worst case for a fully skewed tree. This recursion stack
    //   space is the main trade-off versus the iterative version.
    //   Iterative BFS: O(w) where w is the maximum width of the tree (widest level),
    //   which is at most ceil(n/2) for a complete binary tree's last level; no
    //   recursion call-stack usage at all, trading stack space for queue/heap space.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you find the MINIMUM depth (shortest root-to-leaf path)? (Careful:
    //   must not stop at a node with only one null child -- that's not a leaf.)
    // - How would you check if a tree is height-balanced (Balanced Binary Tree
    //   problem) using this same recursive depth idea? (Compute depth bottom-up and
    //   short-circuit/return -1 as soon as any subtree is found unbalanced.)
    // - How would you compute the diameter of a binary tree (longest path between any
    //   two nodes, not necessarily through the root)? (Track max(left+right) at every
    //   node while computing depth, not just the depth itself.)
    // - Why might BFS be preferred over recursion in a production system handling
    //   extremely large or adversarially deep trees? (Avoids StackOverflowError; BFS
    //   memory grows with tree width, not depth.)
    // - Could you compute max depth using DFS with an explicit stack instead of
    //   recursion, storing (node, depth) pairs to avoid recursion entirely while
    //   still doing DFS? (Yes -- push (root, 1); pop and track max, push children with
    //   depth+1.)
    // - How does this problem relate to counting the total number of nodes, or to
    //   finding all root-to-leaf paths?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Approach 1: simple recursion (DFS via call stack) ----------
    public static int maxDepthRecursive(TreeNode root) {
        if (root == null) {
            return 0; // base case: an empty subtree contributes 0 depth
        }
        int leftDepth = maxDepthRecursive(root.left);   // step: depth of left subtree
        int rightDepth = maxDepthRecursive(root.right); // step: depth of right subtree
        return 1 + Math.max(leftDepth, rightDepth);      // step: +1 for the current node
    }

    // ---------- Approach 2: iterative BFS level counting ----------
    public static int maxDepthIterativeBFS(TreeNode root) {
        if (root == null) {
            return 0; // edge case: empty tree
        }
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        int depth = 0;
        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // step: snapshot current level's node count
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
            }
            depth++; // step: one full level fully processed = one unit of depth
        }
        return depth;
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
        // 1 -> 2 -> 3 -> 4 (all left children)
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(3);
        root.left.left.left = new TreeNode(4);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Balanced tree [3,9,20,null,null,15,7]");
        TreeNode balanced = buildBalancedTree();
        System.out.println("Recursive: " + maxDepthRecursive(balanced)); // Expected: 3
        System.out.println("Iterative: " + maxDepthIterativeBFS(balanced)); // Expected: 3

        System.out.println();
        System.out.println("Test 2: Left-skewed tree of 4 nodes [1,2,null,3,null,4]");
        TreeNode skewed = buildSkewedTree();
        System.out.println("Recursive: " + maxDepthRecursive(skewed)); // Expected: 4
        System.out.println("Iterative: " + maxDepthIterativeBFS(skewed)); // Expected: 4

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        System.out.println("Recursive: " + maxDepthRecursive(null)); // Expected: 0
        System.out.println("Iterative: " + maxDepthIterativeBFS(null)); // Expected: 0

        System.out.println();
        System.out.println("Test 4: Single node tree [42]");
        TreeNode single = new TreeNode(42);
        System.out.println("Recursive: " + maxDepthRecursive(single)); // Expected: 1
        System.out.println("Iterative: " + maxDepthIterativeBFS(single)); // Expected: 1
    }
}
