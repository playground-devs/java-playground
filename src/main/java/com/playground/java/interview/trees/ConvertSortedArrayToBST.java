package com.playground.java.interview.trees;

/**
 * PATTERN: Binary Search Tree / Divide and Conquer Recursion
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Given a sorted (ascending) integer array, build a
 * height-balanced binary search tree from it.
 */
public class ConvertSortedArrayToBST {

    // ================= PROBLEM =================
    // Given an integer array sorted in ascending order, construct a binary search tree
    // (BST) from it that is height-balanced: for every node, the depths of its two
    // subtrees differ by at most 1.
    //
    // Example array: [-10, -3, 0, 5, 9]
    //
    // One valid height-balanced BST (picking the lower-middle index as root each time):
    //           0
    //         /   \
    //       -10    5
    //         \      \
    //         -3      9
    //
    // Any answer is acceptable as long as it is a valid BST (in-order gives back the
    // sorted array) AND it is height-balanced -- there can be multiple correct answers
    // for the same input array.
    // Expected output (one valid answer): in-order traversal reproduces [-10, -3, 0, 5, 9],
    // and the tree's height is the minimum possible for 5 elements (height 3).

    // ================= SIMPLE APPROACH =================
    // Insert the array's elements one at a time into an initially empty BST, using the
    // standard BST insertion algorithm (compare against the current node, go left if
    // smaller, right if larger or equal, repeat until an empty spot is found), taking
    // the elements in their original sorted (ascending) order: first insert -10, then
    // insert -3, then 0, then 5, then 9.

    // ================= WHY IT'S NOT ENOUGH =================
    // Because the input array is already SORTED, inserting elements in that same
    // ascending order into a BST one at a time always produces a completely
    // right-skewed, degenerate tree -- every new element is larger than everything
    // already inserted, so it always becomes the right child of the previous node,
    // ending in what is effectively a linked list disguised as a "tree" (height = n,
    // instead of the minimum possible height of about log2(n)). This is NOT
    // height-balanced at all, which directly violates the problem's requirement, and
    // it also makes future lookups on that BST degrade to O(n) instead of O(log n).

    // ================= OPTIMIZED APPROACH =================
    // Use divide-and-conquer recursion directly on the array's index RANGE, without
    // ever inserting one element at a time:
    //   1. Given a range [lo, hi] (inclusive) of the array still to be placed, if
    //      lo > hi, there are no elements left -- return null (empty subtree).
    //   2. Otherwise, pick mid = the middle index of [lo, hi] as this subtree's root
    //      value, so that roughly half of the remaining elements fall to each side.
    //   3. Recursively build the LEFT subtree from the range [lo, mid - 1] (everything
    //      smaller than the chosen root) and the RIGHT subtree from [mid + 1, hi]
    //      (everything larger).
    //   4. Attach the two recursively-built subtrees to the root node and return it.
    // Because the array is already sorted, simply always choosing the middle element
    // as the local root guarantees the BST ordering property automatically (everything
    // to the left in the array range IS smaller, everything to the right IS larger --
    // no comparisons are ever needed), and always splitting the remaining range roughly
    // in half at every recursive step is exactly what keeps the resulting tree balanced.
    //     build(lo, hi) = null                                            if lo > hi
    //     build(lo, hi) = new Node(arr[mid]) with children build(lo, mid-1), build(mid+1, hi)  otherwise,
    //                     where mid = (lo + hi) / 2  (or (lo + hi + 1) / 2 for the upper-middle variant)

    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure (queue, stack, map) is needed at all here -- this is
    // pure divide-and-conquer recursion directly over array INDICES, with the JVM's own
    // call stack implicitly tracking "which range am I currently responsible for
    // building." This is the right tool because the problem has the classic
    // divide-and-conquer shape: split the input in half, solve each half completely
    // independently and recursively, then combine the two solved halves (here, simply
    // by attaching them as left/right children of the current root) -- there is no
    // need to track any cross-cutting state between the two halves, so nothing beyond
    // the call stack's natural LIFO bookkeeping of "range currently being processed" is required.
    //
    // A key design decision embedded in the recursion is HOW to pick mid when the
    // range has an even number of elements (no single exact middle exists): mid =
    // (lo + hi) / 2 in Java integer division always rounds DOWN, consistently picking
    // the LOWER-middle index. This is a deliberate, consistent choice (not a bug) --
    // picking the lower-middle every time (rather than sometimes rounding up and
    // sometimes down inconsistently) guarantees the resulting tree is height-balanced
    // and deterministic; picking the UPPER-middle instead (mid = (lo + hi + 1) / 2)
    // is an equally valid, equally balanced alternative that simply produces a
    // different (but still correct) tree shape -- both are acceptable answers since
    // the problem does not require a unique output.

    // ================= EDGE CASES =================
    // - Empty array (length 0) -> return a null tree (no nodes at all).
    // - Single-element array -> a single-node tree (that element as the root, no children).
    // - Two-element array -> lo/hi middle choice matters here: with mid = (lo+hi)/2
    //   (lower-middle), the smaller element becomes the root with the larger as its
    //   right child; with the upper-middle variant, the larger becomes the root with
    //   the smaller as its left child -- both are valid, height-balanced 2-node trees.
    // - Array with negative numbers / mixed signs -> irrelevant to the algorithm,
    //   since it never compares values, only splits by index.
    // - Duplicate values in the input array -> the algorithm still works purely by
    //   index-splitting; duplicates simply end up at whatever position their sorted
    //   index places them, without needing special-case handling (result is still a
    //   valid, if not strictly-unique-valued, BST since equal values are consistently
    //   treated as "not smaller," matching standard BST convention).
    // - Very large array -> recursion depth is O(log n), not O(n), specifically
    //   because we always split by INDEX RANGE in half, unlike naive one-at-a-time
    //   BST insertion which risks O(n) recursion/iteration depth on sorted input.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) -- every one of the n array elements becomes exactly one
    // tree node via exactly one recursive call that creates it; there is no repeated
    // work across calls since each call handles a disjoint index range.
    // Space Complexity: O(log n) recursion call-stack space for a balanced split at
    // every level (height-balanced tree has height O(log n) by construction here),
    // PLUS O(n) for the output tree's n nodes themselves (which is unavoidable since
    // the answer itself has n nodes); the O(log n) figure refers only to the
    // additional call-stack depth used WHILE building, not the final tree's node count.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does inserting sorted elements one-by-one into a BST produce a degenerate,
    //   unbalanced tree? (Every new element is larger than all previous ones, so it
    //   always attaches as a right child, forming a linked-list-shaped tree.)
    // - Is the resulting tree unique? (No -- picking lower-middle vs upper-middle at
    //   ties with even-length ranges produces different, equally valid, height-balanced trees.)
    // - How would you verify a produced tree is both a valid BST and height-balanced?
    //   (In-order traversal must reproduce the original sorted array for BST validity;
    //   a separate height-check recursion, like in "Balanced Binary Tree," confirms
    //   the balance property.)
    // - How would this change if the input were a sorted LINKED LIST instead of an
    //   array? (Random access to the "middle" is no longer O(1) -- either convert to
    //   an array first, or use the slow/fast pointer technique to find the middle node
    //   in O(n) per level, giving O(n log n) total instead of O(n).)
    // - Could you build the tree in a bottom-up, iterative way instead of top-down
    //   recursion? (Yes, but it's less natural here since determining balanced
    //   structure bottom-up from a flat sorted array requires essentially simulating
    //   the same recursive split.)
    // - What if the array contains duplicate values -- does the BST property still
    //   hold cleanly? (Yes, as long as equal values are consistently treated as
    //   "belongs to the right of equal or smaller," matching how index-splitting
    //   naturally handles ties without explicit comparisons.)
    // - How does this problem relate to binary search itself? (The recursive
    //   "find the middle, recurse into each half" structure is exactly the divide step
    //   of binary search, just building a tree as a side effect instead of returning
    //   an index/boolean.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Approach: divide and conquer over the array's index range (lower-middle variant) ----------
    public static TreeNode sortedArrayToBST(int[] nums) {
        if (nums == null || nums.length == 0) {
            return null; // edge case: empty (or absent) input array
        }
        return buildBalanced(nums, 0, nums.length - 1);
    }

    private static TreeNode buildBalanced(int[] nums, int lo, int hi) {
        if (lo > hi) {
            return null; // base case: no elements left in this range
        }
        int mid = lo + (hi - lo) / 2; // step: lower-middle index (rounds down), avoids overflow vs (lo+hi)/2
        TreeNode node = new TreeNode(nums[mid]);
        node.left = buildBalanced(nums, lo, mid - 1);   // step: everything smaller goes left
        node.right = buildBalanced(nums, mid + 1, hi);  // step: everything larger goes right
        return node;
    }

    // ---------- Demo helpers ----------
    // In-order traversal, used to verify the built tree reproduces the sorted input.
    private static void inOrderCollect(TreeNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        inOrderCollect(node.left, sb);
        sb.append(node.val).append(" ");
        inOrderCollect(node.right, sb);
    }

    // Height check, used to verify the built tree is height-balanced.
    private static int heightIfBalanced(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int leftHeight = heightIfBalanced(node.left);
        if (leftHeight == -1) {
            return -1;
        }
        int rightHeight = heightIfBalanced(node.right);
        if (rightHeight == -1) {
            return -1;
        }
        if (Math.abs(leftHeight - rightHeight) > 1) {
            return -1; // signal: unbalanced at this node
        }
        return 1 + Math.max(leftHeight, rightHeight);
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sorted array [-10, -3, 0, 5, 9]");
        int[] arr1 = {-10, -3, 0, 5, 9};
        TreeNode t1 = sortedArrayToBST(arr1);
        StringBuilder inOrder1 = new StringBuilder();
        inOrderCollect(t1, inOrder1);
        System.out.println("In-order: " + inOrder1.toString().trim()); // Expected: -10 -3 0 5 9
        System.out.println("Root value: " + t1.val); // Expected: 0
        System.out.println("Is height-balanced: " + (heightIfBalanced(t1) != -1)); // Expected: true

        System.out.println();
        System.out.println("Test 2: Single-element array [7]");
        int[] arr2 = {7};
        TreeNode t2 = sortedArrayToBST(arr2);
        System.out.println("Root value: " + t2.val + ", left: " + t2.left + ", right: " + t2.right); // Expected: 7, null, null

        System.out.println();
        System.out.println("Test 3: Empty array []");
        TreeNode t3 = sortedArrayToBST(new int[0]);
        System.out.println("Result is null: " + (t3 == null)); // Expected: true

        System.out.println();
        System.out.println("Test 4: Even-length sorted array [1, 2, 3, 4]");
        int[] arr4 = {1, 2, 3, 4};
        TreeNode t4 = sortedArrayToBST(arr4);
        StringBuilder inOrder4 = new StringBuilder();
        inOrderCollect(t4, inOrder4);
        System.out.println("In-order: " + inOrder4.toString().trim()); // Expected: 1 2 3 4
        System.out.println("Root value (lower-middle of [0,3] -> index 1): " + t4.val); // Expected: 2
        System.out.println("Is height-balanced: " + (heightIfBalanced(t4) != -1)); // Expected: true
    }
}
