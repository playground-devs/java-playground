package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * PATTERN: Binary Tree Views
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Print the nodes of a binary tree that are visible when looking at the
 * tree from the left side - the FIRST node encountered at each depth level.
 */
public class LeftViewOfBinaryTree {

    // ================= PROBLEM =================
    // Imagine standing to the LEFT of the tree and looking straight across each row.
    // For every level (depth), you can only see the FIRST node in that row - i.e. the
    // leftmost node at that depth.
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
    // Level 0: 1            -> left view sees 1
    // Level 1: 2, 3         -> left view sees 2 (leftmost)
    // Level 2: 4, 5         -> left view sees 4 (leftmost)
    // Level 3: 6            -> left view sees 6
    // Expected output: [1, 2, 4, 6]
    //
    // ================= SIMPLE APPROACH =================
    // Do a level-order traversal (BFS). For each level, look at every node in that level
    // but only remember/print the first one you dequeue.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This "simple" BFS approach IS actually a fine and commonly used solution - the main
    // pitfall is implementation detail: without carefully tracking level boundaries (using
    // the classic "queue size snapshot per level" trick), it's easy to either scan all nodes
    // and pick wrong ones, or run in O(n) time but with fragile, hard-to-read level-tracking
    // logic. Also, if someone reaches for BFS without realizing a DFS approach exists, they
    // may miss a chance to solve it in true O(h) auxiliary space for skewed/balanced trees.
    //
    // ================= OPTIMIZED APPROACH =================
    // Two equally good O(n) options, shown here:
    // 1) BFS: for each level, dequeue all nodes currently in the queue (queue.size() snapshot
    //    at the start of the loop), take the value of the FIRST one dequeued as that level's
    //    left-view node, then enqueue their children left-to-right.
    // 2) DFS (root -> left -> right), tracking current depth: maintain a List sized by max
    //    depth seen so far; the FIRST time we reach a given depth, record that node's value.
    //    Since we recurse left before right, the first node reaching a new depth is always
    //    the leftmost one, giving the correct left view with a simple pre-order DFS.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Queue (ArrayDeque) for BFS: lets us process the tree level by level in FIFO order,
    // so within a level, nodes come out in left-to-right order and the first dequeued node
    // per level is exactly the left-view node.
    // A simple List<Integer> (indexed by depth) for DFS: since we always visit left before
    // right and check "have I already recorded this depth", the list naturally fills with the
    // leftmost node per depth without needing a map - depth values are small, sequential
    // integers (0, 1, 2, ...) which is a perfect fit for a List instead of a Map.
    //
    // ================= EDGE CASES =================
    // - Empty tree (root == null): return an empty list.
    // - Single node: left view is just that node.
    // - Right-skewed tree (only right children): left view still shows every node, since each
    //   node is alone at its depth (it is technically both leftmost and rightmost).
    // - Complete/perfect binary tree: left view is simply the leftmost node of every level.
    // - Unbalanced tree where left subtree is shorter than right subtree: DFS approach must
    //   still visit deeper levels via the right subtree and record first-seen node there too.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both BFS and DFS - every node is visited exactly once.
    // Space Complexity: O(n) worst case - BFS queue can hold an entire level (up to n/2 nodes
    // for a wide tree); DFS uses O(h) recursion stack space (h = tree height) plus O(h) for
    // the result list, which is O(n) only in the worst-case skewed tree, O(log n) if balanced.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does left view differ from right view, and can you get both in a single traversal?
    // - Why does the DFS approach work even though it doesn't explicitly track "leftmost per level"?
    // - What if the tree is very deep (height ~ n) - which approach uses less memory, BFS or DFS?
    // - How would you modify DFS to compute the RIGHT view instead? (Visit right before left.)
    // - Can left view be derived from vertical order traversal? Why or why not (hint: no, they
    //   are different views based on level vs horizontal distance).
    // - What if the tree has millions of nodes and you need view boundaries per level for a UI -
    //   would you precompute and cache all views together?
    // - How would you handle an n-ary tree instead of a strict binary tree?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Approach 1: BFS level order - first node dequeued per level is the left-view node.
    public static List<Integer> leftViewBFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // Step: snapshot how many nodes are in this level.
            for (int i = 0; i < levelSize; i++) {
                TreeNode current = queue.poll();
                if (i == 0) {
                    // Step: the first node popped in this level is the leftmost one.
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

    // Approach 2: DFS (root -> left -> right) - first node to reach a new depth wins.
    public static List<Integer> leftViewDFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        dfs(root, 0, result);
        return result;
    }

    private static void dfs(TreeNode node, int depth, List<Integer> result) {
        if (node == null) {
            return;
        }
        // Step: if this is the first time we reach this depth, record it (leftmost wins
        // because we always recurse left before right).
        if (depth == result.size()) {
            result.add(node.val);
        }
        dfs(node.left, depth + 1, result);
        dfs(node.right, depth + 1, result);
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
        // Expected: [1, 2, 4, 6]
        System.out.println("Sample tree left view (BFS): " + leftViewBFS(sampleTree));
        System.out.println("Sample tree left view (DFS): " + leftViewDFS(sampleTree));

        TreeNode singleNode = new TreeNode(99);
        // Expected: [99]
        System.out.println("Single node left view: " + leftViewBFS(singleNode));

        TreeNode empty = null;
        // Expected: []
        System.out.println("Empty tree left view: " + leftViewBFS(empty));

        // Right-skewed tree: 1 -> 2 -> 3 (all right children)
        TreeNode rightSkewed = new TreeNode(1);
        rightSkewed.right = new TreeNode(2);
        rightSkewed.right.right = new TreeNode(3);
        // Expected: [1, 2, 3] (each node is alone at its depth)
        System.out.println("Right-skewed tree left view: " + leftViewBFS(rightSkewed));
    }
}
