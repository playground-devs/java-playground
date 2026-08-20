package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * PATTERN: Binary Tree Views
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Print the nodes of a binary tree that are visible when looking at the
 * tree from the right side - the LAST node encountered at each depth level.
 */
public class RightViewOfBinaryTree {

    // ================= PROBLEM =================
    // Imagine standing to the RIGHT of the tree and looking straight across each row.
    // For every level (depth), you can only see the LAST node in that row - i.e. the
    // rightmost node at that depth.
    //
    // Example tree:
    //              1
    //            /   \
    //           2      3
    //            \       \
    //             4        5
    //            /
    //           6
    //
    // Level 0: 1            -> right view sees 1
    // Level 1: 2, 3         -> right view sees 3 (rightmost)
    // Level 2: 4, 5         -> right view sees 5 (rightmost)
    // Level 3: 6            -> right view sees 6
    // Expected output: [1, 3, 5, 6]
    //
    // ================= SIMPLE APPROACH =================
    // Do a level-order traversal (BFS). For each level, look at every node in that level
    // but only remember/print the LAST one you dequeue.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Same caveat as left view: this simple BFS approach is actually valid and commonly used
    // in interviews. The risk is purely in implementation - forgetting to snapshot the level
    // size before the inner loop (queue.size() changes as you add children mid-loop), which
    // causes level boundaries to blur and the wrong node to be picked as "last in level".
    //
    // ================= OPTIMIZED APPROACH =================
    // Two equally good O(n) options, shown here:
    // 1) BFS: for each level, dequeue all nodes currently in the queue (snapshot size first),
    //    take the value of the LAST one dequeued as that level's right-view node.
    // 2) DFS (root -> RIGHT -> left), tracking current depth: the FIRST time we reach a given
    //    depth wins, but because we recurse right before left this time, the first node to
    //    reach a new depth is always the rightmost one - giving the right view directly.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Queue (ArrayDeque) for BFS: FIFO order guarantees nodes come out left-to-right within
    // a level, so the last node dequeued in that level is exactly the rightmost, visible node.
    // A simple List<Integer> (indexed by depth) for DFS: since we flip the recursion order to
    // right-before-left, "first node to reach a new depth" now means "rightmost node at that
    // depth" - same trick as left view, just mirrored. No map needed since depths are small
    // sequential integers.
    //
    // ================= EDGE CASES =================
    // - Empty tree (root == null): return an empty list.
    // - Single node: right view is just that node.
    // - Left-skewed tree (only left children): right view still shows every node, since each
    //   node is alone at its depth.
    // - Tree where the deepest node is in the left subtree while the right subtree is shallow:
    //   right view must still descend into the left subtree to find nodes at deeper levels
    //   that have no counterpart on the right - this is the classic right-view "gotcha".
    // - Complete/perfect binary tree: right view is simply the rightmost node of every level.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both BFS and DFS - every node is visited exactly once.
    // Space Complexity: O(n) worst case - BFS queue can hold up to n/2 nodes for a wide tree;
    // DFS uses O(h) recursion stack (h = tree height), O(n) worst case for a skewed tree,
    // O(log n) if the tree is balanced.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - What's the classic gotcha where a naive right-view solution fails? (A left subtree
    //   deeper than the right subtree contributes nodes with no right-side sibling at that depth.)
    // - How would you get left view AND right view from a single traversal pass?
    // - Why does swapping recursion order (right-before-left) correctly flip left view into right view?
    // - Compare BFS vs DFS memory usage for a very wide vs. very deep tree.
    // - How is right view related to boundary traversal of a binary tree?
    // - Could you compute the right view iteratively without recursion or an explicit queue,
    //   e.g. using Morris traversal? What would be the tradeoffs?
    // - How would you extend this to return, per level, both the leftmost and rightmost values?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Approach 1: BFS level order - last node dequeued per level is the right-view node.
    public static List<Integer> rightViewBFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // Step: snapshot level size before mutating the queue.
            for (int i = 0; i < levelSize; i++) {
                TreeNode current = queue.poll();
                if (i == levelSize - 1) {
                    // Step: the last node popped in this level is the rightmost one.
                    result.add(current.val);
                }
                if (current.left != null) {
                    queue.add(current.left);
                }
                if (current.right != null) {
                    queue.add(current.right);
                }
            }
        }
        return result;
    }

    // Approach 2: DFS (root -> right -> left) - first node to reach a new depth wins (rightmost).
    public static List<Integer> rightViewDFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        dfs(root, 0, result);
        return result;
    }

    private static void dfs(TreeNode node, int depth, List<Integer> result) {
        if (node == null) {
            return;
        }
        // Step: first time reaching this depth wins - since we go right before left,
        // that first node is the rightmost one.
        if (depth == result.size()) {
            result.add(node.val);
        }
        dfs(node.right, depth + 1, result);
        dfs(node.left, depth + 1, result);
    }

    // Helper: build the sample tree drawn in the PROBLEM section above.
    private static TreeNode buildSampleTree() {
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.right = new TreeNode(4);
        root.right.right = new TreeNode(5);
        root.left.right.left = new TreeNode(6);
        return root;
    }

    public static void main(String[] args) {
        TreeNode sampleTree = buildSampleTree();
        // Expected: [1, 3, 5, 6]
        System.out.println("Sample tree right view (BFS): " + rightViewBFS(sampleTree));
        System.out.println("Sample tree right view (DFS): " + rightViewDFS(sampleTree));

        TreeNode singleNode = new TreeNode(99);
        // Expected: [99]
        System.out.println("Single node right view: " + rightViewBFS(singleNode));

        TreeNode empty = null;
        // Expected: []
        System.out.println("Empty tree right view: " + rightViewBFS(empty));

        // The classic gotcha: left subtree deeper than right subtree.
        //          1
        //         /
        //        2
        //       /
        //      3
        TreeNode leftDeeper = new TreeNode(1);
        leftDeeper.left = new TreeNode(2);
        leftDeeper.left.left = new TreeNode(3);
        // Expected: [1, 2, 3] (right view must still surface deep left-only nodes)
        System.out.println("Left-deeper tree right view: " + rightViewDFS(leftDeeper));
    }
}
