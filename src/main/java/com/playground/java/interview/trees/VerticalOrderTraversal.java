package com.playground.java.interview.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * PATTERN: Binary Tree Views / BFS + TreeMap grouping
 * PRIORITY: P2 - Good to Know
 * ONE-LINE PROBLEM STATEMENT: Group every node's value into vertical "columns" based on
 * its horizontal distance from the root, and return the columns ordered left to right.
 */
public class VerticalOrderTraversal {

    // ================= PROBLEM =================
    // Given the root of a binary tree, group ALL node values (not just one per column,
    // unlike Top/Bottom View) by horizontal distance (HD) from the root, where:
    //   root HD = 0, left child HD = parent HD - 1, right child HD = parent HD + 1.
    // Return the groups as a list of lists, ordered by HD ascending (left column first).
    //
    // Example tree:
    //              20
    //            /    \
    //          8        22
    //        /   \         \
    //      5      3         25
    //            /  \
    //          10    14
    //
    // HDs:        20(0)
    //           8(-1)   22(1)
    //        5(-2) 3(0)    25(2)
    //             10(-1) 14(1)
    //
    // Columns (grouped by HD, ascending):
    //   HD -2: [5]
    //   HD -1: [8, 10]   (8 at level 1, 10 at level 3 -- both land on HD -1)
    //   HD  0: [20, 3]
    //   HD  1: [22, 14]  (22 at level 1, 14 at level 3 -- both land on HD 1)
    //   HD  2: [25]
    // Expected output: [[5], [8, 10], [20, 3], [22, 14], [25]]

    // ================= SIMPLE APPROACH =================
    // Do a DFS from the root, tracking horizontal distance as you go, and append every
    // node's value into a HashMap<Integer, List<Integer>> keyed by HD as you visit it.
    // Once the DFS finishes, extract all the keys from the HashMap, sort them, and
    // build the final list of lists in that sorted key order.

    // ================= WHY IT'S NOT ENOUGH =================
    // Two separate problems arise from this naive combination. First, a plain HashMap
    // does not maintain any key ordering, so an extra O(k log k) sort of the distinct
    // HD keys is required afterward (k = number of distinct horizontal distances) --
    // easy to forget, and an unnecessary extra pass when a self-sorting map exists.
    // Second, and more subtly, DFS does NOT guarantee that nodes sharing the same HD
    // are appended in top-to-bottom, left-to-right order: DFS dives fully down one
    // child's subtree before backtracking, so it can easily append a deeper node into
    // a column before a shallower node at the same HD that appears later in DFS order
    // (e.g. visiting all of 8's descendants, including one that lands on HD 0, before
    // ever reaching 20's own HD-0 entry down a different branch would be fine here
    // since 20 is visited first regardless, but in general DFS order and "reading order
    // top-to-bottom" are not the same thing, and getting within-column ordering exactly
    // right with DFS requires also tracking and sorting by depth/level, adding more
    // bookkeeping than necessary).

    // ================= OPTIMIZED APPROACH =================
    // Do a standard level-order traversal (BFS) using a Queue, but each queue entry
    // carries both the node AND its horizontal distance. Use a TreeMap<Integer,
    // List<Integer>> keyed by HD to collect values as they're dequeued:
    //   1. Enqueue (root, hd=0).
    //   2. While the queue is non-empty, dequeue (node, hd); append node.val to
    //      treeMap.get(hd) (creating the list on first use); enqueue
    //      (node.left, hd-1) and (node.right, hd+1) if they exist.
    //   3. After the BFS completes, iterate the TreeMap in its natural (ascending key)
    //      order to produce the final list of columns, left to right, with zero extra sorting.
    // Because BFS visits nodes strictly level by level, and within a level strictly
    // left to right (since children are enqueued in left-then-right order at every
    // node), every column's internal list is automatically built in the correct
    // top-to-bottom, left-to-right order with no extra bookkeeping at all.

    // ================= WHY THIS DATA STRUCTURE =================
    // Queue (ArrayDeque) for BFS: guarantees nodes are processed level by level and,
    // within a level, in left-to-right order (since every node enqueues its left child
    // before its right child). This gives us TWO tie-breaking guarantees for free:
    //   - Nodes at the same HD but DIFFERENT levels are naturally appended in
    //     top-to-bottom order, because shallower levels are always fully drained from
    //     the queue before any deeper level begins.
    //   - Nodes at the same HD AND the same level (e.g. the left child of one node and
    //     the right child of a different node, both landing on the same HD) are
    //     appended in left-to-right order, because BFS processes the queue strictly in
    //     the order nodes were enqueued, and within one level that order IS the
    //     left-to-right reading order. The List's stable insertion order (simply
    //     calling add(...) as each node is dequeued) captures this automatically --
    //     no secondary sort key (like tracking depth or column index) is ever needed
    //     for this tie-break.
    // TreeMap<Integer, List<Integer>> keyed by HD: TreeMap keeps its keys sorted at all
    // times, so reading it via values() or entrySet() after the BFS directly yields
    // columns in ascending-HD (left-to-right) order, with no separate sort step. A
    // plain HashMap would require collecting and sorting the keys manually afterward.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> return an empty list of columns.
    // - Single node -> one column containing just that node's value.
    // - Skewed tree (all left or all right children) -> every node lands on a distinct
    //   HD, so every column has exactly one value.
    // - Two nodes with the same HD at DIFFERENT levels -> both appear in the same
    //   column, shallower one first (see BFS ordering guarantee above).
    // - Two nodes with the same HD at the SAME level (a genuine "collision," e.g. the
    //   right-right grandchild of the root and the left-left grandchild of the root's
    //   other subtree, both landing on HD 0) -> both appear in that column, in
    //   left-to-right traversal order.
    // - Duplicate values across different nodes -> stored independently by position;
    //   duplicates never merge or get deduplicated.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) -- each of the n nodes is enqueued/dequeued exactly
    // once (O(n) BFS work), but each TreeMap get-or-create/append operation costs
    // O(log k) where k is the number of distinct horizontal distances (k <= n), giving
    // O(n log k) = O(n log n) worst case overall.
    // Space Complexity: O(n) -- the queue holds at most one full level's worth of
    // nodes (O(w) where w is max tree width, itself O(n) worst case), and the TreeMap
    // stores every node's value exactly once across all its column lists, so total
    // stored values are O(n); no recursion call-stack space is used at all since this
    // is a purely iterative BFS.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How is this different from Top View or Bottom View of a binary tree? (Those
    //   keep only ONE node per HD -- first-seen or last-seen respectively -- while
    //   Vertical Order keeps ALL nodes per HD.)
    // - What if two nodes at the same HD and the same level need a specific
    //   tie-break, e.g. by value instead of by traversal order? (Would need to sort
    //   each column's list by value afterward, or use a TreeMap<Integer,
    //   TreeMap<Integer, List<Integer>>> keyed by (HD, then value) if that's the spec.)
    // - How would you also track and return each value's depth (row), not just its
    //   column? (Store (value, depth) pairs in each column list instead of raw values.)
    // - Could you solve this with DFS instead, and what extra bookkeeping would that
    //   require to get ordering exactly right? (Track depth alongside HD, then sort
    //   each column's collected (depth, value) pairs by depth afterward.)
    // - What's the range of horizontal distances relative to tree shape? (Roughly
    //   -height to +height for a tree leaning maximally in one direction.)
    // - How would this generalize to an N-ary tree where "left/right" isn't well
    //   defined? (Would need an explicit horizontal-distance-per-child convention,
    //   e.g. evenly spacing children, since there's no single fixed left/right offset.)
    // - How would you adapt this to print the tree diagram-style, row by row and
    //   column by column, instead of returning flat lists? (Track (HD, depth) per
    //   node and build a 2D grid indexed by both.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // Pairs a tree node with its horizontal distance, for use inside the BFS queue.
    static class HDNode {
        TreeNode node;
        int hd;
        HDNode(TreeNode node, int hd) { this.node = node; this.hd = hd; }
    }

    // ---------- Approach: BFS + TreeMap<horizontal distance, column values> ----------
    public static List<List<Integer>> verticalOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        // TreeMap keeps horizontal distances sorted automatically, ascending.
        Map<Integer, List<Integer>> hdToColumn = new TreeMap<>();
        Queue<HDNode> queue = new ArrayDeque<>();
        queue.offer(new HDNode(root, 0));

        while (!queue.isEmpty()) {
            HDNode current = queue.poll();
            // step: append (never overwrite) -- unlike bottom view, every node per HD is kept.
            hdToColumn.computeIfAbsent(current.hd, k -> new ArrayList<>()).add(current.node.val);

            if (current.node.left != null) {
                queue.offer(new HDNode(current.node.left, current.hd - 1)); // step: left child before right
            }
            if (current.node.right != null) {
                queue.offer(new HDNode(current.node.right, current.hd + 1));
            }
        }

        // step: TreeMap iteration order is ascending key order = columns left-to-right.
        result.addAll(hdToColumn.values());
        return result;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        TreeNode root = new TreeNode(20);
        root.left = new TreeNode(8);
        root.right = new TreeNode(22);
        root.left.left = new TreeNode(5);
        root.left.right = new TreeNode(3);
        root.left.right.left = new TreeNode(10);
        root.left.right.right = new TreeNode(14);
        root.right.right = new TreeNode(25);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample tree");
        TreeNode sample = buildSampleTree();
        // Expected: [[5], [8, 10], [20, 3], [22, 14], [25]]
        System.out.println(verticalOrder(sample));

        System.out.println();
        System.out.println("Test 2: Single node [42]");
        TreeNode single = new TreeNode(42);
        System.out.println(verticalOrder(single)); // Expected: [[42]]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        System.out.println(verticalOrder(null)); // Expected: []

        System.out.println();
        System.out.println("Test 4: Left-skewed tree [1,2,null,3,null,4]");
        TreeNode skewed = new TreeNode(1);
        skewed.left = new TreeNode(2);
        skewed.left.left = new TreeNode(3);
        skewed.left.left.left = new TreeNode(4);
        // Expected: [[4], [3], [2], [1]] (HDs are -3, -2, -1, 0)
        System.out.println(verticalOrder(skewed));
    }
}
