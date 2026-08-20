package com.playground.java.interview.trees;

/**
 * PATTERN: Binary Tree / Post-Order DFS
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Find the length (in number of edges) of the longest path
 * between any two nodes in a binary tree; the path may or may not pass through the root.
 */
public class DiameterOfBinaryTree {

    // ================= PROBLEM =================
    // Given the root of a binary tree, return the diameter: the length of the longest
    // path between any two nodes, measured in the number of EDGES on that path (not
    // the number of nodes). The path does NOT need to pass through the root.
    //
    // Example tree:
    //      1
    //       \
    //        2
    //       /  \
    //      3    4
    //     / \    \
    //    5   6    7
    //
    // The longest path is 5 -> 3 -> 2 -> 4 -> 7 (or 6 -> 3 -> 2 -> 4 -> 7), which has
    // 4 edges. Notice this path never touches the root (1) at all -- node 2 (not the
    // root) is the "peak" of the longest path, since it is the highest point where the
    // path bends from the left side (3's subtree) to the right side (4's subtree). A
    // path that is forced through the root, such as 5 -> 3 -> 2 -> 1, only reaches
    // 3 edges, which is worse.
    // Expected output: 4

    // ================= SIMPLE APPROACH =================
    // For every single node in the tree, compute the "diameter through this node" as
    // height(leftSubtree) + height(rightSubtree) -- i.e., treat this node as the
    // highest point ("peak") of a path that dips into its left subtree and its right
    // subtree. Then take the max of this value over ALL nodes. A naive way to do this
    // is: for each node n (visited via any traversal), independently call a separate
    // height(n) helper on n.left and n.right, compute leftHeight + rightHeight, and
    // track the running maximum.

    // ================= WHY IT'S NOT ENOUGH =================
    // Calling a standalone height() helper from scratch for every node means the
    // height of the same subtree gets recomputed many times. For example, the height
    // of the subtree rooted at a leaf's grandparent is recomputed once when we visit
    // the grandparent, and its constituent smaller subtrees' heights are recomputed
    // again independently as we separately visit each of those smaller nodes too.
    // This is exactly the "repeated subtree work" pattern: computing height(node) for
    // every node while ALSO recursing into every node to check "is this node a
    // candidate peak" gives O(n) work per node for height, times n nodes to check,
    // for O(n^2) time overall in the worst case (e.g. a skewed/linear tree).
    //
    // A very common interview bug that looks similar but is actually a *correctness*
    // bug, not just a performance one: only checking leftHeight(root) + rightHeight(root)
    // once at the top and calling that "the diameter." That misses paths like 4 -> 2 -> 5
    // in the example above whenever the true longest path's peak is NOT the root but
    // some node buried deeper in the tree. The diameter must be the max of
    // leftHeight + rightHeight taken over EVERY node, not just the root.

    // ================= OPTIMIZED APPROACH =================
    // Combine the height computation and the diameter check into a SINGLE post-order
    // DFS pass. Write one recursive helper, height(node), that:
    //   1. Recursively computes leftHeight = height(node.left) and
    //      rightHeight = height(node.right) (post-order: children before parent).
    //   2. As a SIDE EFFECT, before returning, updates a running "best diameter seen
    //      so far" value by comparing it against leftHeight + rightHeight -- i.e., it
    //      treats the CURRENT node as a candidate peak of the longest path.
    //   3. Returns 1 + max(leftHeight, rightHeight) as this node's own height, so the
    //      caller (the parent) can use it in step 2 for itself.
    // Because every node's height is computed exactly once (not recomputed from
    // scratch by callers above it), and every node is also checked exactly once as a
    // potential peak, this collapses the two responsibilities into one O(n) pass.
    //     height(node) = 0                                          if node == null
    //     height(node) = 1 + max(height(node.left), height(node.right))  otherwise
    //     bestDiameter = max(bestDiameter, height(node.left) + height(node.right))  (checked at every node)

    // ================= WHY THIS DATA STRUCTURE =================
    // No explicit auxiliary data structure (queue, stack, map) is needed here -- the
    // JVM's own recursion call stack IS the data structure doing the work. Post-order
    // DFS via recursion naturally guarantees that by the time we are ready to check a
    // node as a candidate "peak," both of its subtrees' heights have already been
    // fully computed and are sitting in local variables (leftHeight, rightHeight) on
    // that stack frame. A single mutable field (an instance field or an int[1]/AtomicInteger
    // if the method must stay static and side-effect-free-looking) is used to carry the
    // "best diameter so far" across all the recursive calls, since the answer is a
    // property of the WHOLE tree, not of any single subtree's return value -- returning
    // it directly would conflict with also needing to return height() to the parent.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> diameter is 0 (no edges exist at all).
    // - Single node -> diameter is 0 (no pair of distinct nodes to connect).
    // - Two nodes (root + one child) -> diameter is 1 (a single edge).
    // - Skewed tree (all left or all right children) -> diameter equals n - 1 (one
    //   long straight path), and the peak is effectively the topmost node.
    // - Diameter's peak is NOT the root -> must be handled correctly by checking every
    //   node, not just the root (this is the classic bug called out above).
    // - Duplicate values -> irrelevant; diameter depends purely on structure/shape.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized single-pass approach -- each node's
    // height is computed exactly once via post-order recursion, and the diameter
    // check at each node is O(1) work. (The naive approach that recomputes height()
    // from scratch for every node is O(n^2) worst case on a skewed tree.)
    // Space Complexity: O(h) recursion call-stack space, where h is the tree height --
    // O(log n) for a balanced tree, O(n) worst case for a fully skewed tree, since
    // each recursive call to height() adds one stack frame until a null child is hit.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What's the classic bug interviewers look for here? (Only checking
    //   left.height + right.height at the root, instead of at every node in the tree.)
    // - Why is diameter measured in edges, not nodes, and how would the answer change
    //   if it were nodes instead? (Just add 1 to the edge-count answer.)
    // - How would you also return the actual path (the sequence of node values), not
    //   just its length? (Track the best peak node and re-walk down its deepest
    //   left/right children once the peak is known.)
    // - How does this problem relate to Maximum Depth of Binary Tree? (Diameter reuses
    //   the exact same height() recursion, just adds a side-effect check per node.)
    // - Could this be solved iteratively (no recursion)? (Yes, with an explicit stack
    //   doing a manual post-order traversal and a HashMap<TreeNode,Integer> caching
    //   each node's already-computed height.)
    // - What if the tree is a very deep, skewed tree with 10^6 nodes -- what breaks?
    //   (StackOverflowError from O(n) recursion depth; would need the iterative
    //   post-order variant.)
    // - How would this generalize to an N-ary tree (each node has a list of children,
    //   not just left/right)? (Diameter = sum of the two LARGEST child-subtree
    //   heights at each node, not just two fixed slots.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Running "best diameter seen so far" across the whole recursive traversal. Reset
    // at the start of every top-level call to diameterOfBinaryTree(...).
    private static int bestDiameter;

    // ---------- Approach: single post-order DFS pass (height computation + diameter side-effect) ----------
    public static int diameterOfBinaryTree(TreeNode root) {
        bestDiameter = 0; // step: reset shared state for this call
        height(root);
        return bestDiameter;
    }

    // Returns the height of the subtree rooted at `node`, while also updating
    // bestDiameter as a side effect by treating `node` as a candidate path "peak".
    private static int height(TreeNode node) {
        if (node == null) {
            return 0; // base case: an empty subtree has height 0
        }
        int leftHeight = height(node.left);   // step: height of left subtree (post-order: children first)
        int rightHeight = height(node.right); // step: height of right subtree

        // step: THIS is the key line an interviewer probes -- check EVERY node as a
        // potential peak of the longest path, not just the root.
        bestDiameter = Math.max(bestDiameter, leftHeight + rightHeight);

        return 1 + Math.max(leftHeight, rightHeight); // step: +1 edge for the current node's own level
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildTreeWithOffRootPeak() {
        //      1
        //       \
        //        2
        //       /  \
        //      3    4
        //     / \    \
        //    5   6    7
        TreeNode root = new TreeNode(1);
        root.right = new TreeNode(2);
        root.right.left = new TreeNode(3);
        root.right.right = new TreeNode(4);
        root.right.left.left = new TreeNode(5);
        root.right.left.right = new TreeNode(6);
        root.right.right.right = new TreeNode(7);
        return root;
    }

    private static TreeNode buildSkewedTree() {
        // 1 -> 2 -> 3 -> 4 (all left children), diameter should be 3 edges
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(3);
        root.left.left.left = new TreeNode(4);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Tree [1,null,2,3,4,5,null,null,null,null,7] with peak away from the root");
        TreeNode t1 = buildTreeWithOffRootPeak();
        System.out.println("Diameter: " + diameterOfBinaryTree(t1)); // Expected: 4 (path 5-3-2-4-7 or 6-3-2-4-7)

        System.out.println();
        System.out.println("Test 2: Left-skewed tree of 4 nodes [1,2,null,3,null,4]");
        TreeNode t2 = buildSkewedTree();
        System.out.println("Diameter: " + diameterOfBinaryTree(t2)); // Expected: 3

        System.out.println();
        System.out.println("Test 3: Single node tree [42]");
        TreeNode single = new TreeNode(42);
        System.out.println("Diameter: " + diameterOfBinaryTree(single)); // Expected: 0

        System.out.println();
        System.out.println("Test 4: Empty tree (null root)");
        System.out.println("Diameter: " + diameterOfBinaryTree(null)); // Expected: 0
    }
}
