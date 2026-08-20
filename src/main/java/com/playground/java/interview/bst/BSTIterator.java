package com.playground.java.interview.bst;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Binary Search Tree / Controlled Inorder Traversal (Iterator Design)
 * PRIORITY: P2 - Good to Know
 * ONE-LINE PROBLEM STATEMENT: Design an iterator over a BST that returns values in
 * ascending order one at a time, with hasNext() in O(1) and next() in O(1) amortized time.
 */
public class BSTIterator {

    // ================= PROBLEM =================
    // Implement a class BSTIterator that, given the root of a binary search tree,
    // supports:
    //   - boolean hasNext(): returns true if there are still values left to iterate.
    //   - int next(): returns the next smallest value in the BST that hasn't been
    //     returned yet.
    // Calling next() repeatedly should produce values in strictly ascending order.
    //
    // Example tree:
    //        7
    //       / \
    //      3   15
    //         /  \
    //        9   20
    //
    // Iterator usage:
    //   next() -> 3, next() -> 7, next() -> 9, next() -> 15, next() -> 20
    //   hasNext() -> false (after the last next() call above)

    // ================= SIMPLE APPROACH =================
    // In the constructor, do one full inorder traversal of the entire tree up front and
    // store every value into a List (or array/queue) in ascending order. Then
    // hasNext() just checks whether there is a next index left in that list, and
    // next() returns list.get(pointer++).

    // ================= WHY IT'S NOT ENOUGH =================
    // This precomputes and stores ALL n values in memory immediately, which is O(n)
    // space -- even if the caller only ever calls next() once or twice before losing
    // interest, or even if n is huge (e.g. a BST backing a large in-memory index). The
    // whole point of an "iterator" abstraction is usually to produce values lazily,
    // on demand, without holding the entire dataset in memory at once. An O(n)-space
    // solution defeats that purpose, even though it technically satisfies the
    // hasNext()/next() interface correctly.

    // ================= OPTIMIZED APPROACH =================
    // Maintain an explicit Deque<TreeNode> as a stack that always holds exactly the
    // nodes still "pending visit" along the current path, lazily expanding only as
    // far as needed:
    //   - Constructor: push the left spine of the root onto the stack (root,
    //     root.left, root.left.left, ...) until we hit null. The stack's top is now
    //     the smallest not-yet-returned value.
    //   - hasNext(): simply return !stack.isEmpty(). No traversal work needed, O(1).
    //   - next(): pop the top of the stack -- that node's value is the next smallest
    //     value to return. If the popped node has a right child, push that right
    //     child's entire left spine onto the stack (so the new top becomes the next
    //     smallest value after the one we just returned). Return the popped value.
    // This produces values one at a time, lazily, only doing work proportional to
    // what's actually needed to find the next value.

    // ================= WHY THIS DATA STRUCTURE =================
    // An explicit Deque<TreeNode> used as a stack (LIFO) exactly mirrors what a
    // recursive inorder traversal's call stack would look like at any paused point in
    // time, but as a plain object we can hold onto BETWEEN calls to next() -- something
    // a normal recursive function cannot do (a recursive call either runs to
    // completion or has to be a coroutine/generator, which Java methods are not). This
    // lets the iterator "pause" traversal after producing one value and "resume"
    // exactly where it left off on the next() call. A Queue (FIFO) would not preserve
    // the correct next-smallest-first ordering, since inorder traversal fundamentally
    // needs to dive all the way left (last pushed) before backing out (first popped).

    // ================= EDGE CASES =================
    // - Null/empty root -> hasNext() should immediately return false, and next() should
    //   never be called (or should throw if misused).
    // - Single-node tree -> hasNext() true once, one next() call returns that value,
    //   then hasNext() becomes false.
    // - Skewed tree (all left or all right children) -> the initial left-spine push (or
    //   the per-next() right-spine push) can be as long as O(h) = O(n) in the worst
    //   case for that one call.
    // - Calling next() when hasNext() is false -> undefined/should throw
    //   (e.g. from stack.pop() on an empty deque raising an exception) -- caller
    //   contract is to always check hasNext() first.
    // - Fully iterating to the end and confirming values come out in strictly
    //   ascending order matching a plain inorder traversal.

    // ================= COMPLEXITY =================
    // Time Complexity: hasNext() is O(1) always (just checks stack emptiness).
    // next() is O(1) AMORTIZED, not strictly O(1) on every individual call -- a single
    // call to next() can do up to O(h) work if it needs to push a long left spine after
    // popping a node with a right child. However, across the ENTIRE lifetime of the
    // iterator, every node in the tree is pushed onto the stack exactly once and popped
    // exactly once, so the total push/pop work over all next() calls combined is O(n).
    // Dividing that total O(n) work across n calls to next() gives O(1) amortized time
    // per call, even though individual calls vary.
    // Space Complexity: O(h) where h is the height of the tree, because the stack only
    // ever holds nodes along the current path from some ancestor down to the current
    // position -- never more than one path's worth of nodes at a time. This is a
    // significant improvement over the naive O(n)-space precomputed-list approach.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you implement hasPrevious()/previous() to also support going
    //   backwards? (mirror with a second stack, or track direction with more state)
    // - How would you support BSTIterator over a tree that can be mutated (insert/
    //   delete) while iteration is in progress? (typically undefined/needs a
    //   fail-fast modification counter, similar to ConcurrentModificationException)
    // - Why is next() described as O(1) amortized rather than strictly O(1)? (explain
    //   total work over n calls divided by n calls, worst case single call is O(h))
    // - How does this compare to Morris traversal in terms of space? (Morris achieves
    //   O(1) space by temporarily threading right pointers, but mutates tree structure
    //   during traversal, which is undesirable for a read-only iterator over shared
    //   data)
    // - How would you generalize this pattern to any tree traversal that needs to be
    //   "pausable," not just inorder on a BST? (same lazy explicit-stack technique
    //   works for preorder too; postorder is trickier)
    // - What's the tradeoff of precomputing the full sorted list (simple approach) vs
    //   this lazy approach if the caller is expected to call next() for every single
    //   node anyway? (asymptotically same total time, but lazy approach still wins on
    //   space and on "first value available" latency)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // ---------- Optimized: explicit stack holding only the current path's pending nodes ----------
    private final Deque<TreeNode> stack = new ArrayDeque<>();

    public BSTIterator(TreeNode root) {
        pushLeftSpine(root); // step: prime the stack so the top is the smallest value
    }

    private void pushLeftSpine(TreeNode node) {
        while (node != null) {
            stack.push(node); // step: defer visiting node until we've gone as far left as possible
            node = node.left;
        }
    }

    public boolean hasNext() {
        return !stack.isEmpty(); // step: O(1) -- just check if any pending nodes remain
    }

    public int next() {
        TreeNode node = stack.pop();          // step: this is the next smallest unvisited value
        pushLeftSpine(node.right);             // step: expose the next-smallest value after this one
        return node.val;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        //        7
        //       / \
        //      3   15
        //         /  \
        //        9   20
        TreeNode root = new TreeNode(7);
        root.left = new TreeNode(3);
        root.right = new TreeNode(15);
        root.right.left = new TreeNode(9);
        root.right.right = new TreeNode(20);
        return root;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample BST, iterate fully");
        BSTIterator it1 = new BSTIterator(buildSampleTree());
        StringBuilder sb1 = new StringBuilder();
        while (it1.hasNext()) {
            sb1.append(it1.next()).append(" ");
        }
        System.out.println("Output: " + sb1.toString().trim()); // Expected: 3 7 9 15 20

        System.out.println("Test 2: Single node tree");
        BSTIterator it2 = new BSTIterator(new TreeNode(42));
        System.out.println("hasNext(): " + it2.hasNext()); // Expected: true
        System.out.println("next(): " + it2.next());       // Expected: 42
        System.out.println("hasNext(): " + it2.hasNext()); // Expected: false

        System.out.println("Test 3: Edge case - empty tree (null root)");
        BSTIterator it3 = new BSTIterator(null);
        System.out.println("hasNext(): " + it3.hasNext()); // Expected: false

        System.out.println("Test 4: Edge case - left-skewed tree");
        TreeNode skewed = new TreeNode(3);
        skewed.left = new TreeNode(2);
        skewed.left.left = new TreeNode(1);
        BSTIterator it4 = new BSTIterator(skewed);
        StringBuilder sb4 = new StringBuilder();
        while (it4.hasNext()) {
            sb4.append(it4.next()).append(" ");
        }
        System.out.println("Output: " + sb4.toString().trim()); // Expected: 1 2 3
    }
}
