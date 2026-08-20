package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Binary Tree / Recursion (post-order style combination)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Find the Lowest Common Ancestor (LCA) of two given nodes
 * in a general binary tree (not necessarily a BST).
 */
public class LowestCommonAncestor {

    // ================= PROBLEM =================
    // Given the root of a binary tree and two nodes p and q that exist in the tree,
    // find their lowest common ancestor: the deepest node that has both p and q as
    // descendants (a node is allowed to be a descendant of itself).
    //
    // Example tree:
    //        3
    //       / \
    //      5   1
    //     / \ / \
    //    6  2 0  8
    //      / \
    //     7   4
    //
    // LCA(5, 1) -> 3 (they're in different subtrees of the root)
    // LCA(5, 4) -> 5 (5 is an ancestor of 4, and a node can be its own ancestor)
    // LCA(6, 4) -> 5 (both 6 and 4 are in 5's subtree; 5 is the deepest common one)
    // Expected outputs: 3, 5, 5 respectively for the three queries above.

    // ================= SIMPLE APPROACH =================
    // A naive approach would be: find the full root-to-p path and the full
    // root-to-q path as two lists (via DFS, recording the path), then walk both
    // paths simultaneously from the start and find the last node where they still
    // agree -- that's the LCA. This works but requires O(n) extra space to store
    // both paths explicitly, plus the bookkeeping of comparing them index by index.

    // ================= WHY IT'S NOT ENOUGH =================
    // The two-paths approach isn't wrong, just heavier than necessary: it does two
    // full path-recording traversals (or one shared traversal with early exits) and
    // then a linear scan over both stored paths, for O(n) extra space beyond the
    // recursion itself. The recursive "return the node itself" trick below achieves
    // the same O(n) time bound but with a much smaller constant factor and no
    // explicit path storage -- it lets the call stack encode the necessary path
    // information implicitly and combines results on the way back up in a single
    // pass, which is both simpler to code and what interviewers expect for this
    // specific problem.

    // ================= OPTIMIZED APPROACH =================
    // Single-pass recursion (post-order style: process children first, then combine):
    //   lca(node, p, q):
    //     1. Base case: if node is null, or node == p, or node == q -> return node.
    //        (If we've found one of the targets, we return it immediately -- it is a
    //        candidate ancestor of itself, and we let the callers above decide what
    //        to do with that information.)
    //     2. Recurse into left subtree: left = lca(node.left, p, q)
    //     3. Recurse into right subtree: right = lca(node.right, p, q)
    //     4. Combine:
    //        - If BOTH left and right are non-null, it means p was found on one side
    //          and q was found on the other side -- so the CURRENT node is the LCA;
    //          return node.
    //        - If only one side is non-null, propagate that result upward (the LCA,
    //          or one of the targets, must be further up or exactly at that side).
    //        - If both are null, neither p nor q exists in this subtree; return null.

    // ================= WHY THIS DATA STRUCTURE =================
    // No explicit data structure (queue/stack/map) is needed here -- the recursive
    // call stack itself does all the work. Each stack frame, on its way back up
    // ("unwinding"), reports whether it found p, q, both, or neither in its subtree.
    // This is fundamentally a post-order traversal pattern: we must fully explore
    // both children (left and right) BEFORE we can decide what the current node
    // should report, because the decision ("am I the LCA, or should I just pass
    // along what one child found?") depends on combining both children's results.
    // This is exactly why post-order (children before parent) is the natural shape
    // for "combine information from subtrees" problems, as opposed to pre-order
    // (which would process the parent before knowing anything about the children).
    //
    // Contrast with the BST version: if this were guaranteed to be a valid Binary
    // SEARCH Tree, we would NOT need to explore both subtrees at all. Because a BST
    // is ordered, at each node we can compare p.val and q.val against node.val: if
    // both are smaller, the LCA must be in the left subtree; if both are larger, it
    // must be in the right subtree; and the moment they "split" (one smaller, one
    // larger, or one equals the current node), the current node IS the LCA. This
    // lets us follow a SINGLE path down the tree in O(h) = O(log n) time for a
    // balanced BST, instead of the O(n) full-tree exploration required for a
    // general (unordered) binary tree, because BST ordering lets us discard one
    // entire subtree at every step without ever visiting it.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> undefined/no LCA to find; guard against calling
    //   with a null root (in production code, you'd typically require the caller to
    //   guarantee both p and q exist in the tree).
    // - Single node -> if that node is p or q (and the other equals it, or the
    //   problem allows p == q), the node itself is the LCA.
    // - One node is an ancestor of the other (e.g., LCA(5, 4) in the example above)
    //   -> the shallower node itself is correctly returned as the LCA, because the
    //   base case returns node the moment it matches p or q, without needing to
    //   keep searching further down.
    // - Skewed tree (all left or all right children) -> still O(n) time, O(n) worst
    //   case recursion depth since h = n for a fully skewed tree.
    // - Duplicate values -> this algorithm compares by NODE REFERENCE/IDENTITY, not
    //   by value, so duplicate values are handled correctly as long as p and q refer
    //   to specific TreeNode objects rather than being looked up by value.
    // - p or q not present in the tree at all -> this classic recursive solution
    //   will not detect that; it will silently return whatever it finds, which could
    //   be an incorrect answer. A more defensive version separately verifies both p
    //   and q actually exist in the tree before trusting the result.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) -- in the worst case (e.g., p and q both deep, or one
    // missing), we must visit every node once to be sure we've found both.
    // Space Complexity: O(h) recursion call-stack space, where h is tree height --
    // O(log n) for a balanced tree, O(n) worst case for a skewed tree. No other
    // auxiliary data structures are used.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does the algorithm change if the tree IS a valid BST? (See "WHY THIS
    //   DATA STRUCTURE" above -- becomes O(h) = O(log n) using ordering, no need to
    //   explore both subtrees.)
    // - How would you find the LCA if nodes also had a `parent` pointer? (Walk both
    //   p and q up to the root collecting ancestor sets/depths, similar to finding
    //   the intersection point of two linked lists -- align depths then move up
    //   together, or use a HashSet of one path and walk the other.)
    // - How would you handle the case where p or q might NOT exist in the tree?
    //   (Do a separate existence check, or have the recursive function return a
    //   richer result object indicating whether each target was actually found.)
    // - How would you find the LCA of more than two nodes (k nodes)? (Generalize the
    //   combine step to count how many targets were found across children.)
    // - What if the tree is actually a general graph/DAG (nodes can have multiple
    //   parents)? (This single-pass tree recursion no longer applies directly;
    //   you'd need a different algorithm, e.g., based on ancestor sets or binary
    //   lifting for DAGs.)
    // - Can you find the distance between two nodes using LCA? (distance(p, q) =
    //   depth(p) + depth(q) - 2 * depth(LCA(p, q)).)
    // - Why is it safe for the base case to return `node` as soon as node == p or
    //   node == q, without checking whether the OTHER target is also below it?

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- General binary tree LCA (works for any binary tree, not just BST) ----------
    public static TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        // Base case: null subtree, or we've reached one of the two target nodes.
        if (root == null || root == p || root == q) {
            return root;
        }
        // step: search for p and q independently in left and right subtrees
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        // step: combine results from both subtrees
        if (left != null && right != null) {
            // p and q were found in DIFFERENT subtrees -> current node is the LCA
            return root;
        }
        // step: only one side found something (or neither) -> propagate it upward
        return (left != null) ? left : right;
    }

    // ---------- BST-only alternative: O(h) using ordering, kept here for comparison ----------
    // NOTE: only correct if the tree is guaranteed to be a valid Binary Search Tree.
    public static TreeNode lowestCommonAncestorBST(TreeNode root, TreeNode p, TreeNode q) {
        TreeNode current = root;
        while (current != null) {
            if (p.val < current.val && q.val < current.val) {
                current = current.left; // step: both targets smaller -> go left, O(h)
            } else if (p.val > current.val && q.val > current.val) {
                current = current.right; // step: both targets larger -> go right, O(h)
            } else {
                return current; // step: split point (or exact match) found -> this is the LCA
            }
        }
        return null; // p or q not found (shouldn't happen if both are guaranteed present)
    }

    // ---------- Demo helpers ----------
    static TreeNode n7, n4, n6, n2, n0, n8, n5, n1;

    private static TreeNode buildSampleTree() {
        //        3
        //       / \
        //      5   1
        //     / \ / \
        //    6  2 0  8
        //      / \
        //     7   4
        TreeNode root = new TreeNode(3);
        n5 = new TreeNode(5);
        n1 = new TreeNode(1);
        n6 = new TreeNode(6);
        n2 = new TreeNode(2);
        n0 = new TreeNode(0);
        n8 = new TreeNode(8);
        n7 = new TreeNode(7);
        n4 = new TreeNode(4);

        root.left = n5;
        root.right = n1;
        n5.left = n6;
        n5.right = n2;
        n1.left = n0;
        n1.right = n8;
        n2.left = n7;
        n2.right = n4;
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample general binary tree [3,5,1,6,2,0,8,null,null,7,4]");
        TreeNode root = buildSampleTree();

        TreeNode lca1 = lowestCommonAncestor(root, n5, n1);
        System.out.println("LCA(5, 1) = " + (lca1 != null ? lca1.val : "null")); // Expected: 3

        TreeNode lca2 = lowestCommonAncestor(root, n5, n4);
        System.out.println("LCA(5, 4) = " + (lca2 != null ? lca2.val : "null")); // Expected: 5

        TreeNode lca3 = lowestCommonAncestor(root, n6, n4);
        System.out.println("LCA(6, 4) = " + (lca3 != null ? lca3.val : "null")); // Expected: 5

        System.out.println();
        System.out.println("Test 2: BST-only fast path for comparison");
        //        6
        //      /   \
        //     2     8
        //    / \   / \
        //   0   4 7   9
        //      / \
        //     3   5
        TreeNode bstRoot = new TreeNode(6);
        TreeNode bst2 = new TreeNode(2);
        TreeNode bst8 = new TreeNode(8);
        TreeNode bst0 = new TreeNode(0);
        TreeNode bst4 = new TreeNode(4);
        TreeNode bst7 = new TreeNode(7);
        TreeNode bst9 = new TreeNode(9);
        TreeNode bst3 = new TreeNode(3);
        TreeNode bst5 = new TreeNode(5);
        bstRoot.left = bst2;
        bstRoot.right = bst8;
        bst2.left = bst0;
        bst2.right = bst4;
        bst8.left = bst7;
        bst8.right = bst9;
        bst4.left = bst3;
        bst4.right = bst5;

        TreeNode bstLca1 = lowestCommonAncestorBST(bstRoot, bst2, bst8);
        System.out.println("BST LCA(2, 8) = " + bstLca1.val); // Expected: 6
        TreeNode bstLca2 = lowestCommonAncestorBST(bstRoot, bst2, bst4);
        System.out.println("BST LCA(2, 4) = " + bstLca2.val); // Expected: 2
        TreeNode bstLca3 = lowestCommonAncestorBST(bstRoot, bst3, bst5);
        System.out.println("BST LCA(3, 5) = " + bstLca3.val); // Expected: 4

        System.out.println();
        System.out.println("Test 3: Edge case - single node tree, LCA of node with itself");
        TreeNode single = new TreeNode(42);
        TreeNode lcaSingle = lowestCommonAncestor(single, single, single);
        System.out.println("LCA(42, 42) = " + (lcaSingle != null ? lcaSingle.val : "null"));
        // Expected: 42

        System.out.println();
        System.out.println("Test 4: Edge case - null root");
        TreeNode lcaNull = lowestCommonAncestor(null, n5, n1);
        System.out.println("LCA on null root = " + lcaNull); // Expected: null
    }
}
