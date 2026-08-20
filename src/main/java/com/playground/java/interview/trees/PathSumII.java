package com.playground.java.interview.trees;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Binary Tree / Backtracking DFS
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Find every root-to-leaf path whose node values sum to a
 * given target, and return each such path as a list of node values.
 */
public class PathSumII {

    // ================= PROBLEM =================
    // Given the root of a binary tree and an integer targetSum, return ALL root-to-leaf
    // paths where the sum of the values along the path equals targetSum. Each path must
    // be returned as its own list of node values, in top-to-bottom order.
    //
    // Example tree, targetSum = 22:
    //            5
    //          /   \
    //         4     8
    //        /     /  \
    //       11    13   4
    //      /  \        / \
    //     7    2      5   1
    //
    // Root-to-leaf paths and their sums:
    //   5-4-11-7  = 27  (no)
    //   5-4-11-2  = 22  (yes)
    //   5-8-13    = 26  (no)
    //   5-8-4-5   = 22  (yes)
    //   5-8-4-1   = 18  (no)
    // Expected output: [[5,4,11,2], [5,8,4,5]]

    // ================= SIMPLE APPROACH =================
    // Enumerate every root-to-leaf path in the tree first (e.g. via DFS, storing a
    // full copy of the path-so-far at every leaf into a big list of "all paths").
    // Once every path has been collected, make a second pass over that full list and
    // sum each path's values, keeping only the ones whose sum equals targetSum.

    // ================= WHY IT'S NOT ENOUGH =================
    // This does strictly more work than necessary: it materializes and stores EVERY
    // root-to-leaf path in the tree (there can be up to n/2 leaves, each producing a
    // path of up to O(h) values), even though most of them will be thrown away in the
    // second pass once their sum doesn't match. It also requires a second full scan
    // over all collected paths just to sum them, instead of tracking the running sum
    // incrementally while walking down the tree. There is no need to keep the paths
    // that don't match around in memory at all -- checking the remaining sum can be
    // done cheaply on the way down and abandoned (backtracked) as soon as it's clear a
    // branch cannot possibly work, without ever fully materializing it.

    // ================= OPTIMIZED APPROACH =================
    // Do a single DFS with backtracking, maintaining ONE mutable "current path" list
    // and a running "remaining sum" as parameters/state, rather than pre-computing all
    // paths and filtering afterward:
    //   1. On entering a node, add node.val to the current path list, and subtract
    //      node.val from the remaining sum (or equivalently pass remainingSum - node.val).
    //   2. If node is a leaf (both children null) AND remainingSum == 0, the current
    //      path is a valid answer -- add a COPY of the current path list to the results
    //      (a copy is essential; see WHY THIS DATA STRUCTURE below).
    //   3. Otherwise, recurse into node.left and node.right with the updated remaining sum.
    //   4. Before returning from this node (i.e. right before the call unwinds), REMOVE
    //      node.val from the end of the current path list -- this is the "backtrack" step
    //      that undoes step 1 so that when control returns to the parent and explores the
    //      sibling subtree, the path list correctly no longer contains this node.

    // ================= WHY THIS DATA STRUCTURE =================
    // A single mutable List (used as a stack via add(...) at the end / remove(last)) --
    // typically an ArrayList<Integer> or a LinkedList used as a Deque -- is the natural
    // fit because a root-to-leaf path in the middle of a DFS traversal behaves exactly
    // like a stack: nodes are pushed as we descend and popped as we backtrack back up,
    // mirroring the DFS call stack itself. Reusing ONE list across the whole traversal
    // (instead of allocating a brand-new list per recursive call) avoids O(n) path
    // copying at every single node; the ONLY place a copy is needed is at the moment a
    // valid leaf path is found, since that snapshot must survive future mutations of
    // the shared list. This is precisely why "new ArrayList<>(currentPath)" (a copy
    // constructor) is used when adding to results, rather than adding currentPath itself
    // -- adding the same list reference would mean every stored "answer" is actually
    // the same mutable object, which would end up empty (or wrong) once backtracking
    // continues to remove elements from it after the answer was supposedly recorded.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> no paths at all, return an empty list.
    // - Single node whose value equals targetSum -> that single node is itself a valid
    //   root-to-leaf path (a root can also be a leaf).
    // - Single node whose value does NOT equal targetSum -> result is empty.
    // - Negative node values -> the running sum can go negative and later come back to
    //   0 further down, so do not prune early just because remainingSum < 0 unless you
    //   also confirm all remaining values are non-negative (safest to not prune on sign
    //   at all unless the problem guarantees non-negative values).
    // - Multiple valid paths sharing a common prefix -> each must be captured as an
    //   independent copy; forgetting to backtrack (step 4) corrupts every subsequent path.
    // - targetSum of 0 with node values that can sum to 0 via positives and negatives.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2) worst case -- every node is visited once (O(n) DFS calls),
    // but each time a valid leaf path is found, copying the current path list costs
    // O(h) (up to O(n) on a skewed tree), and in the worst case (e.g. every path
    // matches) this copying happens for O(n) leaves, giving O(n * h) = O(n^2) worst case.
    // For a balanced tree this is closer to O(n log n) since h = O(log n).
    // Space Complexity: O(h) recursion call-stack space for the DFS itself (h = tree
    // height), PLUS O(h) for the shared currentPath list at any point in time, PLUS
    // O(n * h) in the worst case for the stored result lists (all matching paths).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must you add a COPY of the current path to the results, not the list
    //   itself? (The list is mutated by backtracking after the leaf check; without a
    //   copy, all stored "answers" would alias the same object and end up wrong/empty.)
    // - How would you solve the simpler "Path Sum I" (does ANY root-to-leaf path sum to
    //   target, return true/false) more efficiently? (No need to track the actual path
    //   or collect results -- just short-circuit return true on the first match.)
    // - How would you extend this to allow paths that don't have to start at the root
    //   or end at a leaf (Path Sum III style)? (Requires a prefix-sum / HashMap approach
    //   or checking every node as a potential path start.)
    // - What if node values could be negative -- does that break any pruning you might
    //   add for performance? (Yes -- can't prune purely on "remaining sum already
    //   negative" since a later negative value could still bring it back to zero.)
    // - How would you adapt this to return the path with the MAXIMUM sum instead of an
    //   exact target sum? (Track a running best sum/path instead of an equality check.)
    // - Could this be done iteratively with an explicit stack instead of recursion, and
    //   what would you need to store on the stack besides the node? (Node plus the
    //   remaining sum and/or path snapshot at that point, since you lose the implicit
    //   call-stack bookkeeping.)
    // - How does memory usage change if there are many overlapping valid paths sharing
    //   long common prefixes? (Each stored result is copied independently, so no
    //   sharing -- total space can be O(n * h) even though the tree itself is O(n).)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Approach: single DFS pass with backtracking ----------
    public static List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        List<List<Integer>> results = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>(); // step: shared, reused "current path" stack
        dfs(root, targetSum, currentPath, results);
        return results;
    }

    private static void dfs(TreeNode node, int remainingSum, List<Integer> currentPath, List<List<Integer>> results) {
        if (node == null) {
            return; // base case: nothing to add for an empty subtree
        }

        currentPath.add(node.val);          // step: descend -- push this node onto the path
        remainingSum -= node.val;           // step: track how much more we still need to reach 0

        boolean isLeaf = node.left == null && node.right == null;
        if (isLeaf && remainingSum == 0) {
            results.add(new ArrayList<>(currentPath)); // step: COPY -- currentPath keeps mutating after this
        } else {
            dfs(node.left, remainingSum, currentPath, results);
            dfs(node.right, remainingSum, currentPath, results);
        }

        currentPath.remove(currentPath.size() - 1); // step: backtrack -- pop this node before returning to parent
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        //            5
        //          /   \
        //         4     8
        //        /     /  \
        //       11    13   4
        //      /  \        / \
        //     7    2      5   1
        TreeNode root = new TreeNode(5);
        root.left = new TreeNode(4);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(11);
        root.left.left.left = new TreeNode(7);
        root.left.left.right = new TreeNode(2);
        root.right.left = new TreeNode(13);
        root.right.right = new TreeNode(4);
        root.right.right.left = new TreeNode(5);
        root.right.right.right = new TreeNode(1);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample tree, targetSum = 22");
        TreeNode sample = buildSampleTree();
        System.out.println(pathSum(sample, 22)); // Expected: [[5, 4, 11, 2], [5, 8, 4, 5]]

        System.out.println();
        System.out.println("Test 2: Single node [1], targetSum = 1");
        TreeNode single = new TreeNode(1);
        System.out.println(pathSum(single, 1)); // Expected: [[1]]

        System.out.println();
        System.out.println("Test 3: Empty tree (null root), targetSum = 0");
        System.out.println(pathSum(null, 0)); // Expected: []

        System.out.println();
        System.out.println("Test 4: Sample tree, targetSum = 100 (no matching path)");
        System.out.println(pathSum(sample, 100)); // Expected: []
    }
}
