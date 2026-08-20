package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * PATTERN: Binary Tree / DFS (Depth-First Traversals)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Implement Inorder, Preorder, and Postorder traversal of a
 * binary tree, both recursively and iteratively (six methods total).
 */
public class BinaryTreeTraversals {

    // ================= PROBLEM =================
    // Visit every node of a binary tree in one of three classic orders and collect the
    // values into a List<Integer>:
    //   - Inorder:   Left, Node, Right
    //   - Preorder:  Node, Left, Right
    //   - Postorder: Left, Right, Node
    //
    // Example tree:
    //        1
    //       / \
    //      2   3
    //     / \
    //    4   5
    //
    // Expected outputs for this tree:
    //   Inorder:   [4, 2, 5, 1, 3]
    //   Preorder:  [1, 2, 4, 5, 3]
    //   Postorder: [4, 5, 2, 3, 1]

    // ================= SIMPLE APPROACH =================
    // The recursive definitions are literally the problem statement:
    //   inorder(node)   = inorder(node.left)  + [node.val] + inorder(node.right)
    //   preorder(node)  = [node.val] + preorder(node.left)  + preorder(node.right)
    //   postorder(node) = postorder(node.left) + postorder(node.right) + [node.val]
    // Each is a direct 4-5 line recursive method. This is the "obvious" solution and,
    // for interview purposes, is already a fully correct and expected answer.

    // ================= WHY IT'S NOT ENOUGH =================
    // Recursion is correct and simple, but interviewers routinely follow up with
    // "now do it without recursion" -- because recursion silently uses the call stack,
    // which (a) can blow up with a StackOverflowError on very deep/skewed trees, and
    // (b) hides the mechanics that you're expected to understand and reproduce
    // manually with an explicit Stack. The iterative versions are the "optimized /
    // production-safe" versions in the sense that they give you explicit control over
    // memory and avoid recursion-depth limits, at the cost of a bit more bookkeeping
    // code.

    // ================= OPTIMIZED APPROACH =================
    // Iterative traversals using an explicit Deque<TreeNode> as a Stack:
    //
    // PREORDER (Node, Left, Right) -- easiest iterative one:
    //   Push root. While stack not empty: pop node, visit it, push right THEN left
    //   (so left is popped and processed first, preserving Node-Left-Right order).
    //
    // INORDER (Left, Node, Right):
    //   Maintain a "current" pointer starting at root. Push nodes and move current to
    //   current.left until current is null (going as far left as possible, pushing
    //   every node along the way). Then pop a node, visit it, and move current to
    //   node.right to repeat the process on the right subtree.
    //
    // POSTORDER (Left, Right, Node) -- the trickiest, explained below in detail:
    //   Two clean strategies are shown:
    //     (a) Two-stack trick: run a "modified preorder" that visits Node-Right-Left
    //         (swap the push order of preorder) and push each visited value onto a
    //         second stack instead of a result list; popping everything off the
    //         second stack at the end reverses it to Left-Right-Node. Simple to code,
    //         uses O(n) extra space for the second stack.
    //     (b) Single-stack with a "last visited node" marker: peek (don't pop) the
    //         top of the stack; only pop and visit it once we've confirmed both its
    //         left and right subtrees are already fully visited -- tracked via a
    //         `lastVisited` pointer compared against the peeked node's right child.

    // ================= WHY THIS DATA STRUCTURE =================
    // Recursion's call stack IS an implicit stack: every recursive call pushes a new
    // stack frame (holding the current node reference and where you are in the
    // in/pre/post logic), and returning from a call pops that frame -- which is
    // exactly LIFO behavior. The iterative versions simply make that implicit
    // mechanism explicit by using a java.util.Deque as a manual Stack: we push nodes
    // we still need to "come back to" and pop them in reverse order of discovery,
    // mirroring what the JVM would otherwise do for us automatically. A Queue (FIFO)
    // would be wrong here -- that gives BFS/level-order, not depth-first order.
    // Postorder needs the extra "last visited" state (or a second stack) precisely
    // because a stack alone can't distinguish "I'm seeing this node for the first
    // time (go left)" from "I'm back here after finishing the right subtree (now
    // visit it)" -- inorder and preorder never need to revisit/re-peek the same node,
    // but postorder must effectively visit each node's stack frame up to a third time.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> return an empty list for every traversal.
    // - Single node -> all three traversals produce the same single-element list.
    // - Skewed tree (all left children only) -> inorder becomes ascending-by-insertion
    //   order for a left-skewed BST-like chain; stack depth equals tree height in the
    //   iterative version, same as recursion depth in the recursive version.
    // - Duplicate values -> traversal order depends only on structure/position, not
    //   value equality, so duplicates are handled with no special-casing.
    // - Unbalanced tree -> correctness is unaffected; only the stack's peak size
    //   varies with the shape (bounded by height h in the worst case).

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for all six methods -- every node is visited (and pushed/
    // popped) a constant number of times.
    // Space Complexity: O(h) auxiliary space for all six methods, where h is the tree
    // height (O(log n) balanced, O(n) worst-case skewed) -- this is the recursion call
    // stack depth for the recursive versions, and the explicit Deque's peak size for
    // the iterative versions. The two-stack postorder variant is still O(h) for each
    // stack (O(n) combined in the worst case), not asymptotically worse. Output lists
    // add O(n) additional space in all cases, separate from the traversal bookkeeping.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is iterative postorder considered the hardest of the three? (Explained
    //   above: it needs to distinguish "first visit" from "return from right child.")
    // - Can you implement Morris Traversal for O(1) extra space inorder traversal
    //   using temporary threaded links (Morris threading)? Be ready to at least
    //   describe the idea even if not asked to code it.
    // - How would you do a level-order (BFS) traversal instead? (Different data
    //   structure entirely -- a Queue, see LevelOrderTraversal.java.)
    // - How do you detect if two trees produce the same traversal but are actually
    //   structurally different? (Preorder or postorder ALONE is not enough to rebuild
    //   a tree uniquely without null markers; inorder + preorder together are.)
    // - How would you serialize/deserialize a tree using preorder traversal?
    // - What's the difference in behavior/output if the tree has duplicate values and
    //   you needed to reconstruct it from just one traversal?
    // - How does traversal order change if you needed "right, node, left" (reverse
    //   inorder) -- e.g., to get a BST's values in descending order?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ================= RECURSIVE =================

    public static List<Integer> inorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        inorderHelper(root, result);
        return result;
    }

    private static void inorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }
        inorderHelper(node.left, result);
        result.add(node.val);
        inorderHelper(node.right, result);
    }

    public static List<Integer> preorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }

    private static void preorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }
        result.add(node.val);
        preorderHelper(node.left, result);
        preorderHelper(node.right, result);
    }

    public static List<Integer> postorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        postorderHelper(root, result);
        return result;
    }

    private static void postorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }
        postorderHelper(node.left, result);
        postorderHelper(node.right, result);
        result.add(node.val);
    }

    // ================= ITERATIVE =================

    // Inorder: go as far left as possible pushing along the way, then pop-visit-go right.
    public static List<Integer> inorderIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode current = root;
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current); // step: dive left, remembering the path
                current = current.left;
            }
            current = stack.pop();       // step: back at a node whose left subtree is done
            result.add(current.val);     // step: visit it
            current = current.right;     // step: now explore its right subtree
        }
        return result;
    }

    // Preorder: push root; pop, visit, push right then left (so left pops first).
    public static List<Integer> preorderIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            result.add(node.val);              // step: visit Node first
            if (node.right != null) {
                stack.push(node.right);         // step: push right before left...
            }
            if (node.left != null) {
                stack.push(node.left);          // ...so left is processed first (LIFO)
            }
        }
        return result;
    }

    // Postorder - Strategy A: two stacks (modified preorder Node-Right-Left, then reverse).
    public static List<Integer> postorderIterativeTwoStacks(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<TreeNode> stack1 = new ArrayDeque<>();
        Deque<Integer> stack2 = new ArrayDeque<>(); // will hold values in reverse postorder
        stack1.push(root);
        while (!stack1.isEmpty()) {
            TreeNode node = stack1.pop();
            stack2.push(node.val);                 // step: collect in Node-Right-Left order
            if (node.left != null) {
                stack1.push(node.left);             // step: push left before right...
            }
            if (node.right != null) {
                stack1.push(node.right);            // ...so right is processed first
            }
        }
        while (!stack2.isEmpty()) {
            result.add(stack2.pop());               // step: reversing gives Left-Right-Node
        }
        return result;
    }

    // Postorder - Strategy B: single stack with a "last visited node" marker.
    public static List<Integer> postorderIterativeOneStack(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode current = root;
        TreeNode lastVisited = null;
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);       // step: dive left, remembering the path
                current = current.left;
            }
            TreeNode peekNode = stack.peek(); // step: look at top without popping yet
            // If right child exists and hasn't been processed yet, go there first.
            if (peekNode.right != null && lastVisited != peekNode.right) {
                current = peekNode.right;
            } else {
                // Both left and right subtrees are done (or right doesn't exist) -> visit.
                result.add(peekNode.val);
                lastVisited = stack.pop();
            }
        }
        return result;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        //        1
        //       / \
        //      2   3
        //     / \
        //    4   5
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);
        return root;
    }

    private static TreeNode buildLeftSkewedTree() {
        //      3
        //     /
        //    2
        //   /
        //  1
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(1);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample tree [1,2,3,4,5]");
        TreeNode sample = buildSampleTree();
        System.out.println("Inorder   recursive: " + inorderRecursive(sample));
        System.out.println("Inorder   iterative: " + inorderIterative(sample));
        // Expected: [4, 2, 5, 1, 3]
        System.out.println("Preorder  recursive: " + preorderRecursive(sample));
        System.out.println("Preorder  iterative: " + preorderIterative(sample));
        // Expected: [1, 2, 4, 5, 3]
        System.out.println("Postorder recursive:      " + postorderRecursive(sample));
        System.out.println("Postorder iter (2-stack): " + postorderIterativeTwoStacks(sample));
        System.out.println("Postorder iter (1-stack): " + postorderIterativeOneStack(sample));
        // Expected: [4, 5, 2, 3, 1]

        System.out.println();
        System.out.println("Test 2: Left-skewed tree [3,2,null,1]");
        TreeNode skewed = buildLeftSkewedTree();
        System.out.println("Inorder:   " + inorderIterative(skewed));   // Expected: [1, 2, 3]
        System.out.println("Preorder:  " + preorderIterative(skewed));  // Expected: [3, 2, 1]
        System.out.println("Postorder: " + postorderIterativeOneStack(skewed)); // Expected: [1, 2, 3]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root) and single node");
        System.out.println("Empty inorder:  " + inorderIterative(null)); // Expected: []
        TreeNode single = new TreeNode(42);
        System.out.println("Single node all three: "
                + inorderIterative(single) + " / "
                + preorderIterative(single) + " / "
                + postorderIterativeOneStack(single));
        // Expected: [42] / [42] / [42]

        // Sanity check: all postorder strategies must agree.
        List<Integer> a = postorderRecursive(sample);
        List<Integer> b = postorderIterativeTwoStacks(sample);
        List<Integer> c = postorderIterativeOneStack(sample);
        boolean allMatch = a.equals(b) && b.equals(c);
        System.out.println("All postorder strategies agree: " + allMatch); // Expected: true
    }
}
