package com.playground.java.interview.bst;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * PATTERN: Binary Search Tree / Inorder Traversal
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Given the root of a BST and an integer k, find the k-th
 * smallest value in the tree (1-indexed).
 */
public class KthSmallestInBST {

    // ================= PROBLEM =================
    // Given the root of a binary search tree and an integer k, return the k-th smallest
    // value stored in the tree (1-indexed, so k=1 means the smallest value overall).
    //
    // Example tree:
    //        5
    //       / \
    //      3   6
    //     / \
    //    2   4
    //   /
    //  1
    //
    // Inorder traversal of a BST always yields values in ascending sorted order:
    // [1, 2, 3, 4, 5, 6]
    // For k = 3, the answer is 3 (the 3rd element in that sorted sequence).

    // ================= SIMPLE APPROACH =================
    // Do a full inorder traversal (left, node, right) of the tree and collect every
    // single value into a List, in ascending order (this is a well-known property of
    // BST inorder traversal). Once the list is built, simply return list.get(k - 1).

    // ================= WHY IT'S NOT ENOUGH =================
    // This visits and stores ALL n nodes even when k is tiny. For example, if k = 1 on
    // a BST with a million nodes, we still walk the entire tree and allocate a list of
    // a million values just to read the very first element. That is O(n) time and O(n)
    // extra space when we really only needed to look at, at most, the first k nodes in
    // sorted order.

    // ================= OPTIMIZED APPROACH =================
    // Do an ITERATIVE inorder traversal using an explicit stack, and stop as soon as we
    // have popped the k-th node -- no need to build the full sorted list.
    //   1. Push the leftmost spine: starting at root, keep pushing node and moving to
    //      node.left until node becomes null. Now the top of the stack is the smallest
    //      unvisited value.
    //   2. Pop the top of the stack. That is the next smallest value. Decrement a
    //      counter k. If k reaches 0, this popped node's value IS the answer -- return
    //      immediately (short-circuit, do not keep traversing the rest of the tree).
    //   3. Otherwise, move to the popped node's right child, and push that node's
    //      entire left spine onto the stack (same idea as step 1), then repeat from
    //      step 2.
    // This visits only as many nodes as needed to reach the k-th smallest, instead of
    // the whole tree.

    // ================= WHY THIS DATA STRUCTURE =================
    // An explicit Deque<TreeNode> used as a stack (LIFO) mirrors exactly what recursion
    // would do on the call stack for an inorder traversal, but lets us pause after each
    // "visit" and check whether we have reached the k-th node -- something plain
    // recursion cannot easily short-circuit out of without extra plumbing (e.g. a
    // counter field or an exception to unwind early). A Queue (FIFO) would not work
    // here because inorder traversal order depends on always going as deep left as
    // possible before visiting a node, which is inherently a "last pushed, first
    // popped" access pattern, not first-in-first-out.

    // ================= EDGE CASES =================
    // - Null/empty root -> k-th smallest is undefined; this implementation would run
    //   off an empty stack, so callers must guarantee 1 <= k <= number of nodes.
    // - k = 1 -> the smallest value in the tree (leftmost leaf on the left spine).
    // - k = n (n = total node count) -> the largest value in the tree.
    // - k out of range (k <= 0 or k > n) -> invalid input; should be validated/guarded.
    // - Single-node tree -> the only valid k is 1, returning that node's value.
    // - Skewed tree (all left children, or all right children) -> height h = n, so the
    //   optimized approach degrades to O(n) time/space in the worst case, same as the
    //   naive approach for that particular shape.
    // - Duplicate values (not valid in a strict BST, but if allowed) -> the algorithm
    //   still returns a value at the correct sorted position; "k-th smallest" is
    //   well-defined by position, duplicates just occupy consecutive positions.

    // ================= COMPLEXITY =================
    // Time Complexity: O(h + k) for the optimized approach -- O(h) to push the initial
    // left spine, then each of the k pops does at most O(h) additional pushes for the
    // next left spine, so total work is bounded by O(h + k). Worst case (skewed tree,
    // k = n) this is O(n). Brute force is always O(n) since it visits every node.
    // Space Complexity: Optimized -- O(h) for the explicit stack, where h is tree
    // height (at most n nodes are ever on the stack at once, but only along a single
    // path from root to the current node). Brute force -- O(n) because it stores every
    // value in a list regardless of k.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you handle k-th LARGEST instead of k-th smallest? (mirror the
    //   traversal: push right spine first, i.e. reverse inorder)
    // - What if there are many repeated "find k-th smallest" queries on the same tree
    //   that can also be mutated (insert/delete)? (augment each node with a subtree
    //   size/rank field to answer in O(h) per query without traversal)
    // - What if k can be arbitrarily large or the tree is huge and read from disk/DB?
    //   (streaming inorder generator/iterator pattern, see BSTIterator)
    // - Can you do this without any extra space (O(1) auxiliary), even if it means
    //   modifying tree pointers temporarily? (Morris inorder traversal using threading)
    // - How would recursion with an early-return/counter field compare to the explicit
    //   stack approach in terms of clarity vs stack-overflow risk on very deep trees?
    // - What changes if the tree is NOT guaranteed to be a valid BST? (inorder no
    //   longer yields sorted order, so this approach breaks; would need a full sort)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Approach 1: brute force - full inorder traversal into a list ----------
    public static int kthSmallestBruteForce(TreeNode root, int k) {
        List<Integer> sortedValues = new ArrayList<>();
        inorderCollect(root, sortedValues); // step: fills sortedValues in ascending order
        return sortedValues.get(k - 1);     // step: k is 1-indexed
    }

    private static void inorderCollect(TreeNode node, List<Integer> out) {
        if (node == null) {
            return; // base case: nothing to visit
        }
        inorderCollect(node.left, out);  // step: visit left subtree first (smaller values)
        out.add(node.val);               // step: visit current node
        inorderCollect(node.right, out); // step: visit right subtree last (larger values)
    }

    // ---------- Approach 2: optimized - iterative inorder with explicit stack, short-circuit ----------
    public static int kthSmallestOptimized(TreeNode root, int k) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode curr = root;
        int remaining = k;

        // step: push the leftmost spine starting from root
        while (curr != null) {
            stack.push(curr);
            curr = curr.left;
        }

        while (!stack.isEmpty()) {
            TreeNode node = stack.pop(); // step: this is the next smallest unvisited value
            remaining--;
            if (remaining == 0) {
                return node.val; // step: found the k-th smallest, short-circuit immediately
            }
            // step: move to right child and push its entire left spine
            curr = node.right;
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
        }

        throw new IllegalArgumentException("k is out of range for the given tree");
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        //        5
        //       / \
        //      3   6
        //     / \
        //    2   4
        //   /
        //  1
        TreeNode root = new TreeNode(5);
        root.left = new TreeNode(3);
        root.right = new TreeNode(6);
        root.left.left = new TreeNode(2);
        root.left.right = new TreeNode(4);
        root.left.left.left = new TreeNode(1);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample BST, k = 3");
        TreeNode tree1 = buildSampleTree();
        System.out.println("Brute force: " + kthSmallestBruteForce(tree1, 3)); // Expected: 3
        System.out.println("Optimized:   " + kthSmallestOptimized(tree1, 3));  // Expected: 3

        System.out.println("Test 2: Sample BST, k = 1 (smallest value overall)");
        TreeNode tree2 = buildSampleTree();
        System.out.println("Brute force: " + kthSmallestBruteForce(tree2, 1)); // Expected: 1
        System.out.println("Optimized:   " + kthSmallestOptimized(tree2, 1));  // Expected: 1

        System.out.println("Test 3: Edge case - single node tree, k = 1");
        TreeNode single = new TreeNode(42);
        System.out.println("Brute force: " + kthSmallestBruteForce(single, 1)); // Expected: 42
        System.out.println("Optimized:   " + kthSmallestOptimized(single, 1));  // Expected: 42

        System.out.println("Test 4: Edge case - right-skewed tree, k = last (largest)");
        TreeNode skewed = new TreeNode(1);
        skewed.right = new TreeNode(2);
        skewed.right.right = new TreeNode(3);
        System.out.println("Brute force: " + kthSmallestBruteForce(skewed, 3)); // Expected: 3
        System.out.println("Optimized:   " + kthSmallestOptimized(skewed, 3));  // Expected: 3
    }
}
