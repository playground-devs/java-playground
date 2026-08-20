package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * PATTERN: Binary Tree / BFS + Horizontal Distance
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Return the set of nodes visible when a binary tree is
 * viewed from directly above, one node per unique horizontal distance from the root.
 */
public class TopViewOfBinaryTree {

    // ================= PROBLEM =================
    // Imagine looking straight down at a binary tree from above: for every vertical
    // "column" (horizontal distance, or HD, from the root), only the TOPMOST node in
    // that column is visible; anything directly below it is hidden from view. The
    // task is to return those visible node values, ordered from leftmost column to
    // rightmost column.
    //
    // Horizontal distance (HD) convention: root has HD = 0; going to a left child
    // decreases HD by 1; going to a right child increases HD by 1.
    //
    // Example tree:
    //           1
    //         /   \
    //        2     3
    //         \
    //          4
    //           \
    //            5
    //             \
    //              6
    //
    // HDs: 1 -> 0, 2 -> -1, 3 -> +1, 4 -> 0, 5 -> +1, 6 -> +2
    // Column -1: {2}                  -> topmost = 2
    // Column  0: {1 (depth 0), 4 (depth 2)} -> topmost = 1 (1 is shallower, seen first)
    // Column +1: {3 (depth 1), 5 (depth 3)} -> topmost = 3 (3 is shallower, seen first)
    // Column +2: {6}                  -> topmost = 6
    // Expected output (left to right by HD): [2, 1, 3, 6]
    // Note: node 4 and node 5 are HIDDEN from the top view because node 1 and node 3
    // (respectively) sit directly above them in the same column and are shallower.

    // ================= SIMPLE APPROACH =================
    // A naive attempt might do a plain DFS (preorder), computing each node's
    // horizontal distance, and store the FIRST node value it encounters for each HD
    // into a map. This seems reasonable but is subtly broken: DFS explores one whole
    // branch deeply before backtracking, so the ORDER in which nodes of a given HD
    // are first encountered depends on the shape/traversal order of the tree, NOT on
    // which node is actually shallowest (topmost). DFS could visit a deep node at a
    // given HD before it visits a shallower node at that same HD, depending on
    // whether the shallower node happens to live in a part of the tree that DFS
    // reaches later.

    // ================= WHY IT'S NOT ENOUGH =================
    // Concrete counterexample for naive "first-seen via preorder DFS" using the tree
    // above: a preorder DFS visits 1(HD0), 2(HD-1), 4(HD0), 5(HD+1), 6(HD+2), 3(HD+1).
    // If we naively record "first value seen per HD" in this DFS order, HD=+1 would
    // be recorded as 5 (visited before backtracking to 3), which is WRONG -- node 3
    // (depth 1) is shallower than node 5 (depth 3) and should be the visible one.
    // DFS can absolutely visit a deeper node in a column before a shallower one in
    // that same column, because DFS order is driven by tree structure/recursion
    // order, not by depth. A DFS-based solution CAN be made correct, but only if you
    // additionally track depth per HD and only overwrite the map entry when you find
    // a strictly shallower node -- at which point you're re-deriving exactly the
    // guarantee that BFS gives you for free.

    // ================= OPTIMIZED APPROACH =================
    // BFS (level-order traversal) while tracking horizontal distance:
    //   1. Use a Queue of (node, horizontalDistance) pairs, starting with (root, 0).
    //   2. Process the queue in standard BFS order (level by level, though we don't
    //      even need to chunk by level explicitly here).
    //   3. For each dequeued (node, hd): if hd is NOT already a key in our
    //      TreeMap<Integer, Integer>, record hd -> node.val. If hd IS already
    //      present, skip it -- we've already recorded the topmost node for that
    //      column (an earlier, shallower node already claimed it).
    //   4. Enqueue (node.left, hd - 1) and (node.right, hd + 1) for continued
    //      traversal.
    //   5. Finally, read the TreeMap's values in key order (TreeMap keeps keys
    //      sorted automatically) to get the top view from leftmost to rightmost
    //      column.

    // ================= WHY THIS DATA STRUCTURE =================
    // Two data structures work together here, each for a specific reason:
    //   - Queue (BFS): BFS processes nodes in strictly non-decreasing depth order --
    //     every node at depth d is dequeued before any node at depth d+1. This gives
    //     us an ironclad guarantee: the FIRST time we ever see a given horizontal
    //     distance during BFS, it is guaranteed to be from the SHALLOWEST (topmost)
    //     node at that HD, because no deeper node at that same HD could possibly have
    //     been dequeued earlier. This is exactly the guarantee DFS lacks (see WHY
    //     IT'S NOT ENOUGH) -- DFS order is tied to structure, BFS order is tied to
    //     depth, and "topmost" is fundamentally a depth-ordering question, which is
    //     why BFS is the natural fit and DFS requires extra bookkeeping to fix.
    //   - TreeMap<Integer, Integer> keyed by horizontal distance: a TreeMap keeps its
    //     keys in sorted order automatically (backed by a red-black tree), so once
    //     we're done inserting, iterating map.values() directly gives us columns
    //     ordered from leftmost (most negative HD) to rightmost (most positive HD)
    //     with zero extra sorting step. A plain HashMap would require collecting and
    //     manually sorting the keys afterward; TreeMap gives us that ordering as an
    //     inherent property of the data structure, at the cost of O(log n) per
    //     insertion instead of HashMap's O(1) average.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> return an empty list, nothing to view.
    // - Single node -> top view is just that one node's value.
    // - Skewed tree (all left children) -> every node has a unique HD, so the top
    //   view includes every single node, ordered top-to-bottom-becomes-left-to-right.
    // - Duplicate values -> handled correctly since we key by HD/depth via BFS order,
    //   not by value; equal values at different columns are independent entries.
    // - Unbalanced tree with overlapping columns (as in the example above, where
    //   node 4 and node 5 are hidden behind node 1 and node 3) -> this is the core
    //   case the "first-seen-wins" BFS rule is specifically designed to handle
    //   correctly.
    // - Two nodes at the SAME horizontal distance AND same depth (possible when one
    //   is reached via a left-then-right path and another via a right-then-left path
    //   converging on the same HD at the same level) -> whichever is dequeued first
    //   in BFS order wins; the conventional tie-break (and what most interview specs
    //   expect) is that the node encountered first in left-to-right BFS order at that
    //   level is kept, since we enqueue left before right at every node.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) -- O(n) to visit every node once via BFS (O(1)
    // queue operations with ArrayDeque per node), plus O(log n) per TreeMap
    // insertion in the worst case (only performed for genuinely new HDs, but
    // TreeMap's containsKey/put are both O(log n) regardless), and O(n) to iterate
    // the resulting map's values at the end. If a HashMap were used instead to get
    // plain O(1) average insertion, you would need an additional O(n log n) sort of
    // the HD keys afterward to produce left-to-right order anyway, so overall
    // complexity is O(n log n) either way for this problem.
    // Space Complexity: O(n) -- the queue holds up to O(w) nodes at once (w = max
    // tree width), and the TreeMap holds at most one entry per unique horizontal
    // distance, which is bounded by n in the worst case (e.g., a skewed tree where
    // every node has a distinct HD).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does Bottom View differ, and how would you modify this algorithm for it?
    //   (Instead of skipping when hd already exists, ALWAYS overwrite hd -> node.val
    //   on every visit, so the LAST node seen per column -- the deepest, since BFS
    //   processes deeper nodes later -- wins instead of the first.)
    // - How would you implement Vertical Order Traversal (ALL nodes per column, not
    //   just the topmost)? (Same HD-keyed BFS, but map to a List<Integer> per HD
    //   instead of a single Integer, appending instead of skip/overwrite; often also
    //   needs a secondary sort by depth-then-value within each column for ties.)
    // - Why can't you reliably solve Top View using DFS without extra depth
    //   bookkeeping? (Covered in WHY IT'S NOT ENOUGH -- DFS order isn't depth order.)
    // - How would you handle the tie-break rule differently if the problem wanted
    //   the RIGHT-most node at a given HD/depth to win instead of the left-most?
    //   (Enqueue right before left, or explicitly compare and prefer on tie.)
    // - Could you solve this without a TreeMap, e.g., using two arrays/offsets
    //   indexed by shifted horizontal distance, if you knew the HD range up front?
    //   (Yes -- one full DFS/BFS pass to find min/max HD, then an array of size
    //   (max-min+1) as O(1)-access storage instead of TreeMap's O(log n).)
    // - How does horizontal distance relate to Diagonal Traversal of a binary tree?
    //   (Diagonal traversal groups nodes differently: right children stay on the
    //   same diagonal, only left children start a new diagonal line.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Small helper pair class to carry (node, horizontal distance) through the queue.
    private static class NodeHd {
        TreeNode node;
        int hd;
        NodeHd(TreeNode node, int hd) {
            this.node = node;
            this.hd = hd;
        }
    }

    // ---------- Optimized approach: BFS + TreeMap keyed by horizontal distance ----------
    public static List<Integer> topView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result; // edge case: empty tree
        }
        // TreeMap keeps HD keys sorted, giving left-to-right column order for free.
        TreeMap<Integer, Integer> hdToValue = new TreeMap<>();
        Queue<NodeHd> queue = new ArrayDeque<>();
        queue.offer(new NodeHd(root, 0));

        while (!queue.isEmpty()) {
            NodeHd current = queue.poll();
            // step: BFS guarantees the FIRST node seen at a given hd is the topmost
            if (!hdToValue.containsKey(current.hd)) {
                hdToValue.put(current.hd, current.node.val);
            }
            if (current.node.left != null) {
                queue.offer(new NodeHd(current.node.left, current.hd - 1)); // step: left -> hd-1
            }
            if (current.node.right != null) {
                queue.offer(new NodeHd(current.node.right, current.hd + 1)); // step: right -> hd+1
            }
        }

        for (Map.Entry<Integer, Integer> entry : hdToValue.entrySet()) {
            result.add(entry.getValue()); // step: iterate in sorted-by-hd order
        }
        return result;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildOverlappingColumnsTree() {
        //           1
        //         /   \
        //        2     3
        //         \
        //          4
        //           \
        //            5
        //             \
        //              6
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.right = new TreeNode(4);
        root.left.right.right = new TreeNode(5);
        root.left.right.right.right = new TreeNode(6);
        return root;
    }

    private static TreeNode buildBalancedTree() {
        //        1
        //       / \
        //      2   3
        //     / \  / \
        //    4  5 6   7
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);
        root.right.left = new TreeNode(6);
        root.right.right = new TreeNode(7);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Tree with hidden nodes (4 and 5 hidden behind 1 and 3)");
        TreeNode overlapping = buildOverlappingColumnsTree();
        System.out.println("Top view: " + topView(overlapping));
        // Expected: [2, 1, 3, 6]

        System.out.println();
        System.out.println("Test 2: Balanced complete tree [1,2,3,4,5,6,7]");
        TreeNode balanced = buildBalancedTree();
        System.out.println("Top view: " + topView(balanced));
        // HDs: 1->0, 2->-1, 3->+1, 4->-2, 5->0(hidden by 1), 6->0(hidden by 1), 7->+2
        // Expected: [4, 2, 1, 3, 7]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        System.out.println("Top view: " + topView(null));
        // Expected: []

        System.out.println();
        System.out.println("Test 4: Single node tree [99]");
        TreeNode single = new TreeNode(99);
        System.out.println("Top view: " + topView(single));
        // Expected: [99]
    }
}
