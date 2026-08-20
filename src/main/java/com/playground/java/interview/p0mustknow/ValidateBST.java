package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Binary Search Tree / Recursion with Bounds
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Determine whether a given binary tree is a valid Binary
 * Search Tree (BST).
 */
public class ValidateBST {

    // ================= PROBLEM =================
    // A valid BST is defined as: for every node, ALL values in its left subtree are
    // strictly less than the node's value, and ALL values in its right subtree are
    // strictly greater than the node's value -- and this must hold recursively for
    // every node's subtrees too, not just its immediate children.
    //
    // Example tree (looks locally fine at every parent-child pair, but is NOT valid):
    //       10
    //       / \
    //      5   15
    //         /  \
    //        6    20
    //
    // Every immediate parent-child pair is individually fine: 5 < 10, 15 > 10,
    // 6 < 15, 20 > 15. But node 6 sits in the RIGHT subtree of the root (10), so it
    // is required to be greater than 10 -- yet 6 < 10. This makes the tree an
    // INVALID BST, even though no single parent-child pair looks wrong in isolation.
    // Expected output: false

    // Example of a genuinely VALID BST for contrast:
    //        5
    //       / \
    //      3   8
    //     / \   \
    //    1   4   9
    // Expected output: true

    // ================= SIMPLE APPROACH =================
    // *** THE COMMON WRONG APPROACH (a trap many candidates fall into) ***
    // A tempting but INCORRECT shortcut is to only check the immediate parent-child
    // relationship at each node:
    //     boolean wrongIsValid(node):
    //         if node == null: return true
    //         if node.left != null && node.left.val >= node.val: return false
    //         if node.right != null && node.right.val <= node.val: return false
    //         return wrongIsValid(node.left) && wrongIsValid(node.right)
    // This only checks ONE level down and completely ignores constraints from
    // ancestors further up the tree.

    // ================= WHY IT'S NOT ENOUGH =================
    // Using the same counterexample from the PROBLEM section:
    //       10
    //       / \
    //      5   15
    //         /  \
    //        6    20
    // Running the naive local-only check: 5 < 10 (ok), 15 > 10 (ok), 6 < 15 (ok),
    // 20 > 15 (ok) -- every immediate parent-child comparison passes, so the naive
    // check incorrectly returns TRUE. It never compares node 6 against the root (10),
    // even though node 6 lives in the root's RIGHT subtree and is therefore required
    // to be greater than 10. This is exactly why local-only checks are insufficient:
    // a violation introduced by an ancestor two or more levels up is completely
    // invisible to a check that only ever compares a node against its direct parent.
    // The fix must propagate constraints from EVERY ancestor down the recursion, not
    // just the immediate parent -- which is exactly what the (min, max) range
    // approach below does.

    // ================= OPTIMIZED APPROACH =================
    // Approach A -- Valid range (min, max) passed down recursively:
    //   Each recursive call carries a valid (low, high) exclusive range that the
    //   current node's value must fall within, based on ALL of its ancestors so far
    //   (not just its immediate parent). When recursing left, the upper bound
    //   tightens to the current node's value (everything in the left subtree must be
    //   less than it); when recursing right, the lower bound tightens to the current
    //   node's value. This correctly propagates constraints from every ancestor, not
    //   just the immediate parent.
    //
    // Approach B -- Inorder traversal must be strictly increasing:
    //   A key BST property is that an inorder traversal (Left, Node, Right) of a
    //   valid BST visits nodes in strictly ascending sorted order. So: perform an
    //   inorder traversal and check that each visited value is strictly greater than
    //   the previously visited value. If not, the tree is not a valid BST. This can
    //   be done recursively or iteratively (with an explicit stack) while tracking a
    //   `previousValue` variable, and short-circuits as soon as a violation is found.

    // ================= WHY THIS DATA STRUCTURE =================
    // Approach A needs no special data structure -- it relies on passing two extra
    // long parameters (min/max bounds) down through recursion, letting the call stack
    // implicitly carry the "accumulated constraints from every ancestor on the path
    // from the root," which is exactly the information the naive local check was
    // missing. Approach B relies on the recursive call stack (or an explicit Deque
    // acting as a stack for the iterative inorder version, same mechanics as
    // BinaryTreeTraversals.inorderIterative) purely to enumerate nodes in sorted
    // order; the actual validity check itself is a single O(1) comparison against a
    // running `previousValue`, so the "data structure" doing the real work here is
    // just that one long variable tracking traversal history, combined with whichever
    // stack mechanism enumerates nodes in order.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> vacuously a valid BST, return true.
    // - Single node -> always valid regardless of its value.
    // - Skewed tree (all left or all right) -> still must satisfy strict ordering
    //   across the whole chain; range approach tightens bounds at every step.
    // - Duplicate values -> classic BST definition (as used by LeetCode #98 and most
    //   interviews) requires STRICT inequality, so a node equal to an ancestor's
    //   value makes the tree INVALID; use strict < / > comparisons everywhere, never
    //   <= / >=, and be explicit with the interviewer about which convention is used.
    // - Integer boundary values (e.g., node value == Integer.MIN_VALUE or MAX_VALUE)
    //   -> use `long` for the min/max bounds (initialized to Long.MIN_VALUE /
    //   Long.MAX_VALUE) to avoid integer overflow/edge issues when comparing against
    //   Integer.MIN_VALUE or Integer.MAX_VALUE node values.
    // - Unbalanced tree -> both approaches handle this correctly; only affects the
    //   recursion depth / stack size, not correctness.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches -- every node is visited exactly once,
    // doing O(1) comparison work per node.
    // Space Complexity: O(h) for both approaches due to recursion call-stack depth,
    // where h is tree height (O(log n) balanced, O(n) worst-case skewed). The
    // iterative inorder variant of Approach B uses an explicit Deque of size O(h)
    // instead of the call stack, but the asymptotic space is the same.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the naive "check only direct children" approach fail, and can you
    //   produce a counterexample on the spot? (See WHY IT'S NOT ENOUGH above.)
    // - Should the BST validity check use strict or non-strict inequalities, and
    //   why does that matter for duplicate values?
    // - How would you validate a BST if node values could be any Comparable type, not
    //   just int (avoiding the Integer.MIN/MAX_VALUE overflow trap entirely by using
    //   nullable bounds instead of sentinel long values)?
    // - Can you convert a valid BST into a sorted doubly linked list in-place using
    //   the same inorder traversal idea?
    // - How would you find the closest value to a target in a BST, and why is that
    //   O(h) instead of O(n)? (Leverages BST ordering to prune one side at each step.)
    // - What is the kth smallest element in a BST, and how does inorder traversal
    //   make that straightforward (stop after the kth visited node)?
    // - How would recovering a BST with exactly two swapped nodes work, using the
    //   same "inorder must be increasing" idea to spot the two out-of-order nodes?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- WRONG approach kept for illustration only (do not use) ----------
    public static boolean wrongIsValidBST(TreeNode node) {
        if (node == null) {
            return true;
        }
        // BUG: only compares node against its IMMEDIATE children, ignoring
        // constraints inherited from grandparents/ancestors further up the tree.
        if (node.left != null && node.left.val >= node.val) {
            return false;
        }
        if (node.right != null && node.right.val <= node.val) {
            return false;
        }
        return wrongIsValidBST(node.left) && wrongIsValidBST(node.right);
    }

    // ---------- Correct Approach A: valid (min, max) range passed down ----------
    public static boolean isValidBSTRange(TreeNode root) {
        return validateRange(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private static boolean validateRange(TreeNode node, long low, long high) {
        if (node == null) {
            return true; // base case: an empty subtree trivially satisfies any range
        }
        // step: current node's value must respect ALL accumulated ancestor bounds
        if (node.val <= low || node.val >= high) {
            return false;
        }
        // step: left subtree's upper bound tightens to node.val
        // step: right subtree's lower bound tightens to node.val
        return validateRange(node.left, low, node.val)
                && validateRange(node.right, node.val, high);
    }

    // ---------- Correct Approach B: inorder traversal must be strictly increasing ----------
    public static boolean isValidBSTInorder(TreeNode root) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode current = root;
        Long previousValue = null; // boxed so we can represent "no previous value yet"
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current); // step: dive left, same mechanics as inorder traversal
                current = current.left;
            }
            current = stack.pop(); // step: visit next node in sorted order
            if (previousValue != null && current.val <= previousValue) {
                return false; // step: violation of strictly-increasing inorder property
            }
            previousValue = (long) current.val;
            current = current.right; // step: continue into right subtree
        }
        return true;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildValidBST() {
        //        5
        //       / \
        //      3   8
        //     / \   \
        //    1   4   9
        TreeNode root = new TreeNode(5);
        root.left = new TreeNode(3);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(1);
        root.left.right = new TreeNode(4);
        root.right.right = new TreeNode(9);
        return root;
    }

    private static TreeNode buildSubtlyInvalidBST() {
        // Passes a naive local check but is NOT a valid BST:
        //       10
        //       / \
        //      5   15
        //         /  \
        //        6    20
        // 6 is in the right subtree of root(10), so it must be > 10, but 6 < 10.
        TreeNode root = new TreeNode(10);
        root.left = new TreeNode(5);
        root.right = new TreeNode(15);
        root.right.left = new TreeNode(6);
        root.right.right = new TreeNode(20);
        return root;
    }

    private static TreeNode buildObviouslyInvalidBST() {
        //   5
        //  / \
        // 1   4
        //    / \
        //   3   6
        TreeNode root = new TreeNode(5);
        root.left = new TreeNode(1);
        root.right = new TreeNode(4);
        root.right.left = new TreeNode(3);
        root.right.right = new TreeNode(6);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Valid BST [5,3,8,1,4,null,9]");
        TreeNode valid = buildValidBST();
        System.out.println("Range approach:   " + isValidBSTRange(valid)); // Expected: true
        System.out.println("Inorder approach: " + isValidBSTInorder(valid)); // Expected: true
        System.out.println("(Wrong naive check also happens to say): " + wrongIsValidBST(valid));

        System.out.println();
        System.out.println("Test 2: Subtly invalid BST [10,5,15,null,null,6,20]"
                + " -- fools the naive local check!");
        TreeNode subtlyInvalid = buildSubtlyInvalidBST();
        System.out.println("WRONG naive check (incorrectly passes): "
                + wrongIsValidBST(subtlyInvalid)); // Prints: true (BUG!)
        System.out.println("Range approach (correct):   "
                + isValidBSTRange(subtlyInvalid)); // Expected: false
        System.out.println("Inorder approach (correct): "
                + isValidBSTInorder(subtlyInvalid)); // Expected: false

        System.out.println();
        System.out.println("Test 3: Obviously invalid BST [5,1,4,null,null,3,6]");
        TreeNode obviouslyInvalid = buildObviouslyInvalidBST();
        System.out.println("Range approach:   " + isValidBSTRange(obviouslyInvalid)); // Expected: false
        System.out.println("Inorder approach: " + isValidBSTInorder(obviouslyInvalid)); // Expected: false

        System.out.println();
        System.out.println("Test 4: Empty tree (null root) and single node");
        System.out.println("Range approach (null root):   " + isValidBSTRange(null)); // Expected: true
        TreeNode single = new TreeNode(42);
        System.out.println("Range approach (single node): " + isValidBSTRange(single)); // Expected: true

        System.out.println();
        System.out.println("Test 5: Duplicate values (BST with equal node, must be false)");
        TreeNode dupRoot = new TreeNode(5);
        dupRoot.left = new TreeNode(5); // equal value in left subtree -> invalid (strict <)
        System.out.println("Range approach:   " + isValidBSTRange(dupRoot)); // Expected: false
        System.out.println("Inorder approach: " + isValidBSTInorder(dupRoot)); // Expected: false
    }
}
