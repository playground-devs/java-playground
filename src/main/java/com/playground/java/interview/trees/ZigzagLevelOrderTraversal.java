package com.playground.java.interview.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * PATTERN: Binary Tree / BFS Level Order
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Traverse a binary tree level by level, alternating the
 * reading direction of each level between left-to-right and right-to-left.
 */
public class ZigzagLevelOrderTraversal {

    // ================= PROBLEM =================
    // Given the root of a binary tree, return its level-order traversal, but with
    // alternating direction: level 0 read left-to-right, level 1 read right-to-left,
    // level 2 left-to-right again, and so on ("zigzag").
    //
    // Example tree:
    //        3
    //       / \
    //      9  20
    //         / \
    //        15  7
    //
    // Levels: [3], [9, 20], [15, 7]
    // Zigzag: level 0 (3) -> left-to-right -> [3]
    //         level 1 (9, 20) -> right-to-left -> [20, 9]
    //         level 2 (15, 7) -> left-to-right -> [15, 7]
    // Expected output: [[3], [20, 9], [15, 7]]

    // ================= SIMPLE APPROACH =================
    // Perform an ordinary level-order BFS, collecting each level's nodes into a
    // List<Integer> in the normal left-to-right order every single time (exactly like
    // a standard level-order traversal). Then, in a second pass, check the level's
    // index: if it's odd, call Collections.reverse(...) (or build a new reversed
    // list) on that level's list before adding it to the final result.

    // ================= WHY IT'S NOT ENOUGH =================
    // This works correctly, but it does unnecessary extra work: every "reversed"
    // level is built once in the wrong order and then reversed afterward, an O(levelSize)
    // reversal pass on top of the O(levelSize) BFS collection that already happened --
    // effectively touching every node on odd levels twice instead of once. It's not a
    // complexity-CLASS problem (still O(n) overall, since reversing one level is
    // O(levelSize) and levels partition all n nodes), but it's avoidable extra work and
    // an extra step that's easy to get wrong (e.g. reversing the wrong levels if the
    // off-by-one on level parity is mixed up, or mutating a list that's aliased elsewhere).

    // ================= OPTIMIZED APPROACH =================
    // Do the same level-order BFS, but build each level's list directly in its final
    // desired order using a Deque<Integer> instead of a plain List, and a boolean flag
    // (leftToRight) that toggles once per completed level:
    //   1. Snapshot queue.size() as levelSize (the standard BFS level-boundary trick).
    //   2. For each of the levelSize nodes dequeued this level:
    //        - if leftToRight is true, addLast(node.val)  (append normally)
    //        - if leftToRight is false, addFirst(node.val) (prepend instead)
    //      Either way, still enqueue node.left then node.right for the NEXT level,
    //      regardless of this level's direction -- the BFS traversal itself always
    //      walks left-to-right internally; only how we WRITE each value into the
    //      level's output list changes.
    //   3. After the level is fully drained, flip leftToRight = !leftToRight, and add
    //      the completed Deque (as a List) to the result.
    // Because addFirst() during a right-to-left level places each newly dequeued value
    // at the front, the level's list ends up in reverse order directly, with no
    // separate reversal step ever required.

    // ================= WHY THIS DATA STRUCTURE =================
    // A Deque<Integer> (via ArrayDeque or LinkedList) is used for each level's output
    // because it is the only common Java collection offering O(1) insertion at BOTH
    // ends (addFirst and addLast). A plain ArrayList only offers efficient O(1)
    // amortized insertion at the END; inserting at the FRONT (add(0, value)) is O(k)
    // per call since every existing element must shift right, which would make
    // building a right-to-left level via repeated front-inserts O(levelSize^2) instead
    // of O(levelSize). The Deque lets us pick addLast() or addFirst() per node based on
    // the current level's direction and get O(1) per insertion either way, producing
    // the correctly-ordered level directly instead of building-then-reversing.
    // Separately, the outer traversal still uses a Queue (also backed by ArrayDeque)
    // for the BFS itself, exactly as in a standard level-order traversal -- that FIFO
    // queue is what guarantees nodes are discovered level by level in the first place;
    // it is a completely separate structure from the per-level output Deque.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> return an empty list of levels.
    // - Single node -> one level, direction is left-to-right (level 0), trivially just
    //   that one value.
    // - Skewed tree (all left or all right children) -> every level has exactly one
    //   node, so "direction" has no visible effect (a one-element list looks the same
    //   forwards or backwards), but the toggle logic must still not error out.
    // - Tree with exactly 2 levels -> verifies the toggle flips exactly once.
    // - Tree with an odd vs even total number of levels -> confirms the last level's
    //   direction is computed correctly regardless of tree depth parity.
    // - Duplicate values across nodes -> irrelevant; ordering is purely structural.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) -- every node is dequeued and enqueued exactly once during
    // BFS, and each node contributes exactly one O(1) addFirst/addLast call to its
    // level's Deque, regardless of that level's direction.
    // Space Complexity: O(n) -- the result stores every node's value exactly once
    // across all levels, and the BFS queue holds at most one full level's worth of
    // nodes at a time (O(w), where w is the widest level, itself O(n) worst case for a
    // very wide/complete tree); no recursion call-stack space is used since this is a
    // purely iterative BFS.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is a Deque preferred here over building each level normally and reversing
    //   odd levels afterward? (Avoids a second O(levelSize) pass per odd level --
    //   Deque produces the right order in one pass via addFirst/addLast.)
    // - Could you solve this recursively (DFS) instead of iteratively (BFS)? (Yes --
    //   track depth as a parameter, maintain one list per depth, and insert into the
    //   front or back of the depth's list based on depth parity; less natural than BFS
    //   since level boundaries aren't as explicit.)
    // - How would plain level-order traversal (no zigzag) differ in code? (Identical
    //   BFS skeleton, but always addLast/append, no direction flag needed at all.)
    // - What if you needed to zigzag every OTHER level starting from level 1 instead of
    //   level 0 (i.e. invert which levels reverse)? (Just flip the initial value of the
    //   leftToRight boolean before the loop starts.)
    // - How would you adapt this for an N-ary tree (each node has a list of children)?
    //   (Same BFS skeleton; enqueue all children in their normal left-to-right order
    //   regardless of the current level's zigzag direction, exactly as with binary trees.)
    // - Is the BFS traversal order itself ever actually reversed, or only the OUTPUT
    //   list per level? (Only the output list -- internally, children are always
    //   discovered/enqueued in the same left-to-right order every level, which is what
    //   keeps the "next level's" left-to-right discovery order correct regardless of
    //   how the previous level's values were written out.)
    // - How would you verify this is correct with a unit test on a tree with 4+ levels?
    //   (Confirm alternating direction is visible on levels with 2+ elements, since
    //   single-element levels can't reveal an ordering bug.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Approach: BFS with a Deque per level, direction flag toggled per level ----------
    public static List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) {
            return result; // edge case: empty tree
        }

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        boolean leftToRight = true; // step: level 0 always reads left-to-right

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // step: snapshot current level's node count
            Deque<Integer> levelValues = new ArrayDeque<>(); // step: supports O(1) insert at both ends

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();

                if (leftToRight) {
                    levelValues.addLast(node.val); // step: normal append for left-to-right levels
                } else {
                    levelValues.addFirst(node.val); // step: prepend to reverse the level directly
                }

                // step: children are always enqueued left-then-right, regardless of
                // this level's zigzag direction -- direction only affects the OUTPUT list.
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
            }

            result.add(new ArrayList<>(levelValues)); // step: materialize this level's final order
            leftToRight = !leftToRight; // step: flip direction for the next level
        }

        return result;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
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

    public static void main(String[] args) {
        System.out.println("Test 1: Sample tree [3,9,20,null,null,15,7]");
        TreeNode sample = buildSampleTree();
        System.out.println(zigzagLevelOrder(sample)); // Expected: [[3], [20, 9], [15, 7]]

        System.out.println();
        System.out.println("Test 2: Single node [42]");
        TreeNode single = new TreeNode(42);
        System.out.println(zigzagLevelOrder(single)); // Expected: [[42]]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        System.out.println(zigzagLevelOrder(null)); // Expected: []

        System.out.println();
        System.out.println("Test 4: 4-level tree [1,2,3,4,5,6,7,8]");
        TreeNode fourLevels = new TreeNode(1);
        fourLevels.left = new TreeNode(2);
        fourLevels.right = new TreeNode(3);
        fourLevels.left.left = new TreeNode(4);
        fourLevels.left.right = new TreeNode(5);
        fourLevels.right.left = new TreeNode(6);
        fourLevels.right.right = new TreeNode(7);
        fourLevels.left.left.left = new TreeNode(8);
        // Expected: [[1], [3, 2], [4, 5, 6, 7], [8]]
        System.out.println(zigzagLevelOrder(fourLevels));
    }
}
