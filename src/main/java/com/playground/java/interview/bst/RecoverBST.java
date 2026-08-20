package com.playground.java.interview.bst;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Binary Search Tree / Inorder Traversal + Two-Pointer Violation Tracking
 * PRIORITY: P2 - Good to Know
 * ONE-LINE PROBLEM STATEMENT: Exactly two nodes of a BST had their values swapped by
 * mistake; recover the tree in-place (without changing its structure) so it is a valid BST again.
 */
public class RecoverBST {

    // ================= PROBLEM =================
    // The values of exactly two nodes of a binary search tree were accidentally
    // swapped. The tree's SHAPE/STRUCTURE is still a valid BST shape -- only the two
    // values are in the wrong spots. Recover the tree by swapping those two values
    // back, without changing the structure (no rotations, no rebuilding).
    //
    // Example (adjacent swap in inorder order):
    //        3
    //       / \
    //      1   4
    //         /
    //        2
    // Inorder gives: 1, 3, 2, 4 -- notice 3 > 2, a single violation. The nodes holding
    // 3 and 2 were swapped. After recovery:
    //        2
    //       / \
    //      1   4
    //         /
    //        3
    // Inorder becomes: 1, 2, 3, 4 (correct ascending order).
    //
    // Example (non-adjacent swap in inorder order):
    //        3
    //       / \
    //      2   4
    //     /
    //    1  <- imagine 3 and 1's positions were swapped conceptually; two separate
    //          violations show up in the inorder sequence, one at each swapped node.

    // ================= SIMPLE APPROACH =================
    // Do a full inorder traversal of the tree, collecting every (node reference, value)
    // pair in a list. Since a correct BST's inorder traversal is strictly ascending,
    // scan that list to find which two positions are "out of order" relative to a
    // fully sorted copy of the same values (or simply find the two positions whose
    // values, if swapped back, would make the whole sequence strictly ascending).
    // Then swap those two nodes' values.

    // ================= WHY IT'S NOT ENOUGH =================
    // Collecting every node into a list is O(n) extra space, and scanning/sorting to
    // find the two misplaced positions adds unnecessary overhead when we can detect
    // the exact two violating nodes DURING a single traversal pass, without ever
    // materializing the full list -- we only need a couple of pointers, not the whole
    // sequence of values.

    // ================= OPTIMIZED APPROACH =================
    // Do a single inorder traversal (recursive or iterative with an explicit stack)
    // while keeping a running `prev` pointer to the previously visited node. In a
    // valid BST, inorder values are strictly increasing, so prev.val must always be
    // less than current.val. Track two node references, `first` and `second`
    // (both initially null):
    //   - Whenever prev.val > current.val (a violation is found):
    //       - If `first` is still null, this is the FIRST violation encountered.
    //         Set first = prev (the earlier, too-large node) and, tentatively,
    //         second = current (assuming the swapped nodes are adjacent in inorder
    //         order -- i.e. this might be the only violation).
    //       - If `first` is already set, this is the SECOND violation, meaning the
    //         two swapped nodes are NOT adjacent in inorder order. Update
    //         second = current only; leave `first` as it was set at the first
    //         violation (it is already correct).
    //   - After the traversal is complete, swap first.val and second.val.
    // Two cases in practice:
    //   Case 1 - swapped nodes ARE adjacent in inorder sequence: exactly ONE violation
    //   is found; first/second are set together at that single point.
    //   Case 2 - swapped nodes are NOT adjacent: exactly TWO violations are found;
    //   `first` is fixed at the first violation's prev node, and `second` gets
    //   overwritten at the second violation's current node.

    // ================= WHY THIS DATA STRUCTURE =================
    // Recursion's call stack (or an explicit Deque<TreeNode> used as a stack for the
    // iterative version) is the right tool because inorder traversal inherently needs
    // to remember "go all the way left, then come back up, visit, then go right" --
    // exactly what a stack (implicit via recursion, or explicit via a Deque) provides.
    // We do not need any additional structure (like a list of all values) because the
    // BST-inorder-is-sorted invariant lets us detect violations with just ONE extra
    // "previously visited node" pointer compared against the current node, checked
    // on the fly as we go, rather than needing random access to all values at once.

    // ================= EDGE CASES =================
    // - Null/empty tree -> nothing to recover, no-op.
    // - Single-node tree -> trivially already a valid BST, no swap possible or needed.
    // - The two swapped nodes are adjacent in inorder order (e.g. parent-child in
    //   inorder sequence, not necessarily in tree structure) -> exactly one violation
    //   detected, first and second set together.
    // - The two swapped nodes are far apart in inorder order (e.g. one near the
    //   leftmost leaf, one near the rightmost leaf) -> exactly two violations
    //   detected, first set at violation #1, second updated at violation #2.
    // - The root itself is one of the two swapped nodes -> must still be handled
    //   correctly since `prev` starts as null and the root is the first node visited.
    // - Skewed tree -> O(n) traversal depth; recursion stack could be as deep as n.
    // - Duplicate values -- not expected in a strict BST, but if the input allows
    //   "prev.val >= current.val" style boundary equality, the strict "<" check must
    //   be chosen carefully to avoid false-positive violations.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) because a single inorder traversal visits every node
    // exactly once, and detecting/fixing the swap is O(1) extra work per node checked.
    // Space Complexity: O(h) for recursion call-stack space (or explicit stack space
    // in the iterative version), where h is tree height -- we never store all n
    // values, only a constant number of node references (prev, first, second) plus
    // whatever the traversal mechanism itself needs (recursion frames or explicit
    // stack entries along the current path).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve this with O(1) extra space (not counting recursion)?
    //   (Morris inorder traversal, using temporary right-pointer threading instead of
    //   a stack or recursion)
    // - How would you verify, after recovery, that the tree is now a valid BST?
    //   (run a validate-BST check: another inorder traversal confirming strictly
    //   ascending order, or min/max bound propagation)
    // - What if THREE (not two) node values were swapped? (this specific two-violation
    //   detection trick breaks down; would need a more general approach, e.g. collect
    //   all violations and match against a fully sorted list)
    // - Could you detect and fix this in a single pass without waiting to fully
    //   finish the traversal? (yes -- once first and second have been set from two
    //   violations, you could break early, similar to the short-circuit idea in
    //   KthSmallestInBST)
    // - How does the iterative stack-based version's behavior differ from the
    //   recursive version in terms of when `prev` gets updated? (same logical order,
    //   just explicit push/pop instead of call/return)
    // - Why must we compare prev.val > current.val rather than prev.val >= current.val
    //   for a strict BST? (equal values are not a "violation" by the strict-BST
    //   invariant convention used here; would only matter for BSTs that allow
    //   duplicates)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    private static TreeNode prev;
    private static TreeNode first;
    private static TreeNode second;

    // ---------- Approach: single inorder traversal detecting up to two violations ----------
    public static void recoverTree(TreeNode root) {
        // step: reset static tracking state before each independent run
        prev = null;
        first = null;
        second = null;

        inorderDetect(root); // step: single traversal finds first/second violating nodes

        if (first != null && second != null) {
            int temp = first.val; // step: swap the two misplaced values back
            first.val = second.val;
            second.val = temp;
        }
    }

    private static void inorderDetect(TreeNode node) {
        if (node == null) {
            return; // base case: nothing to visit
        }
        inorderDetect(node.left); // step: visit left subtree first

        // step: compare current node against the previously visited (smaller-expected) node
        if (prev != null && prev.val > node.val) {
            if (first == null) {
                first = prev;   // step: first violation - remember the earlier, too-large node
                second = node;  // step: tentatively assume adjacent swap
            } else {
                second = node;  // step: second violation - update second, first stays fixed
            }
        }
        prev = node; // step: advance prev to the current node for the next comparison

        inorderDetect(node.right); // step: visit right subtree last
    }

    // ---------- Alternative: iterative inorder using an explicit stack (same logic) ----------
    public static void recoverTreeIterative(TreeNode root) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode curr = root;
        TreeNode prevNode = null;
        TreeNode firstNode = null;
        TreeNode secondNode = null;

        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr); // step: dive left, deferring visits along the way
                curr = curr.left;
            }
            curr = stack.pop(); // step: visit next node in ascending inorder order

            if (prevNode != null && prevNode.val > curr.val) {
                if (firstNode == null) {
                    firstNode = prevNode; // step: first violation found
                    secondNode = curr;
                } else {
                    secondNode = curr; // step: second violation found, first stays fixed
                }
            }
            prevNode = curr;

            curr = curr.right; // step: move on to the right subtree
        }

        if (firstNode != null && secondNode != null) {
            int temp = firstNode.val; // step: swap the two misplaced values back
            firstNode.val = secondNode.val;
            secondNode.val = temp;
        }
    }

    // ---------- Demo helpers ----------
    private static void printInorder(TreeNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        printInorder(node.left, sb);
        sb.append(node.val).append(" ");
        printInorder(node.right, sb);
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Adjacent swap - one violation");
        // Correct BST would be: 1,2,3,4 inorder. Here 3 and 2 are swapped in value.
        //        3
        //       / \
        //      1   4
        //         /
        //        2
        TreeNode tree1 = new TreeNode(3);
        tree1.left = new TreeNode(1);
        tree1.right = new TreeNode(4);
        tree1.right.left = new TreeNode(2);
        recoverTree(tree1);
        StringBuilder sb1 = new StringBuilder();
        printInorder(tree1, sb1);
        System.out.println("Recovered inorder: " + sb1.toString().trim()); // Expected: 1 2 3 4

        System.out.println("Test 2: Non-adjacent swap - two violations");
        // Correct BST (inorder 1,2,3,4,5) would be:
        //        3
        //       / \
        //      2   5
        //     /   /
        //    1   4
        // Here the leftmost leaf (correct value 1) and the root's right child
        // (correct value 5) were swapped, so the two violating positions are far
        // apart in inorder order (not adjacent):
        //        3
        //       / \
        //      2   1   <- should be 5 (value swapped)
        //     /   /
        //    5   4      <- should be 1 (value swapped)
        // Corrupted inorder: 5, 2, 3, 4, 1 -- two violations: (5 > 2) and (4 > 1).
        TreeNode tree2 = new TreeNode(3);
        tree2.left = new TreeNode(2);
        tree2.right = new TreeNode(1); // should be 5 after recovery
        tree2.left.left = new TreeNode(5); // should be 1 after recovery
        tree2.right.left = new TreeNode(4);
        recoverTree(tree2);
        StringBuilder sb2 = new StringBuilder();
        printInorder(tree2, sb2);
        System.out.println("Recovered inorder: " + sb2.toString().trim()); // Expected: 1 2 3 4 5

        System.out.println("Test 3: Edge case - single node tree (nothing to recover)");
        TreeNode single = new TreeNode(42);
        recoverTree(single);
        StringBuilder sb3 = new StringBuilder();
        printInorder(single, sb3);
        System.out.println("Recovered inorder: " + sb3.toString().trim()); // Expected: 42

        System.out.println("Test 4: Iterative approach on the same adjacent-swap case as Test 1");
        TreeNode tree4 = new TreeNode(3);
        tree4.left = new TreeNode(1);
        tree4.right = new TreeNode(4);
        tree4.right.left = new TreeNode(2);
        recoverTreeIterative(tree4);
        StringBuilder sb4 = new StringBuilder();
        printInorder(tree4, sb4);
        System.out.println("Recovered inorder: " + sb4.toString().trim()); // Expected: 1 2 3 4
    }
}
