package com.playground.java.interview.bst;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * PATTERN: Binary Tree / Level-Order Linking (Perfect Binary Tree)
 * PRIORITY: P2 - Good to Know
 * ONE-LINE PROBLEM STATEMENT: Given a PERFECT binary tree, populate each node's `next`
 * pointer to point to its next right neighbor on the same level, or null if it is the rightmost node on that level.
 */
public class PopulateNextRightPointersInEachNode {

    // ================= PROBLEM =================
    // Given the root of a PERFECT binary tree (every level is completely filled --
    // every parent has exactly two children, all the way down to the leaves; this
    // guarantee matters a lot for the optimized approach below), each node has an
    // extra pointer field `next`. Populate every node's `next` pointer so that it
    // points to its immediate right neighbor on the same level, or null if it is the
    // rightmost node on that level.
    //
    // Example (before, next pointers all null):
    //          1
    //        /   \
    //       2     3
    //      / \   / \
    //     4   5 6   7
    //
    // Example (after, next pointers drawn as -->):
    //          1  --> null
    //        /   \
    //       2  --> 3  --> null
    //      / \    / \
    //     4-->5-->6-->7 --> null

    // ================= SIMPLE APPROACH =================
    // Do a standard BFS level-order traversal using a Queue. For each level, first
    // record how many nodes are currently in the queue (that level's size), then
    // dequeue exactly that many nodes one at a time, linking node[i].next = node[i+1]
    // for each consecutive pair dequeued within that level, and leaving the very last
    // node dequeued in that level pointing to null. While dequeuing each node, enqueue
    // its left and right children (if any) so the next level's nodes are ready to
    // process the same way.

    // ================= WHY IT'S NOT ENOUGH =================
    // This is O(n) time, which is optimal, but it uses O(w) extra queue space, where w
    // is the maximum width of any level. For a perfect binary tree, the last (deepest)
    // level alone holds up to roughly n/2 nodes -- so the queue can grow to hold a
    // huge fraction of the entire tree at once. We want to eliminate this auxiliary
    // queue entirely and do the linking in O(1) extra space (not counting the
    // recursion-free, pointer-only bookkeeping), since the tree being PERFECT gives us
    // enough structure to avoid needing a queue at all.

    // ================= OPTIMIZED APPROACH =================
    // Because the tree is PERFECT, once an entire level's `next` pointers are fully
    // connected, we can walk across that level using ONLY the `next` pointers we just
    // built (no queue needed) to connect the NEXT level down. Maintain two pointers:
    //   - `leftmost`: the leftmost node of the level we are currently linking FROM.
    //   - `curr`: walks left-to-right across the current level via curr.next.
    // Algorithm:
    //   1. Start with leftmost = root.
    //   2. While leftmost has a left child (i.e. we haven't reached the bottom level):
    //        a. Set curr = leftmost.
    //        b. While curr != null:
    //             - Connect curr.left.next = curr.right (both children of the SAME
    //               parent, always adjacent on the next level).
    //             - If curr.next != null, connect curr.right.next = curr.next.left
    //               (bridging across two DIFFERENT parents' subtrees -- curr's right
    //               child and curr's next-sibling's left child are adjacent on the
    //               next level).
    //             - Advance curr = curr.next (move across the current level).
    //        c. Move down a level: leftmost = leftmost.left.
    //   3. Stop when leftmost.left is null (we've reached the leaf level, nothing
    //      more to connect below it).
    // Every level's linking is done using only the pointers already built on the
    // level above it, so no queue is ever needed.

    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure (no Queue, no Stack, no List) is needed at all --
    // just a small constant number of node-reference variables (leftmost, curr). This
    // works ONLY because the tree is guaranteed PERFECT: every node at a given depth
    // has exactly two children, so once we know a level is fully linked via `next`,
    // we can deterministically predict exactly how the next level's nodes relate to
    // each other (same-parent children are adjacent; different-parent children are
    // bridged via curr.next.left) without ever needing to "remember" a whole level's
    // worth of pending nodes in a queue. This is the key insight that lets us replace
    // O(w) queue space with O(1) extra space.

    // ================= EDGE CASES =================
    // - Null/empty root -> nothing to do, return immediately.
    // - Single-node tree (just the root, no children) -> root.next stays null, no
    //   linking needed since there is no left child to descend into.
    // - Two-level tree (root + two children) -> one round of linking: root.left.next
    //   = root.right, and root.right.next stays null since root.next is null.
    // - A tree that is NOT perfect (e.g. missing a node partway down) -> this
    //   optimized algorithm's correctness relies entirely on the "perfect binary
    //   tree" guarantee; if a node is missing, curr.left or curr.right could be null
    //   unexpectedly and the pointer-walking logic would break or NPE. (A different,
    //   more defensive algorithm is needed for arbitrary/"not necessarily complete"
    //   binary trees -- often solved by falling back to a dummy-head-per-level trick
    //   or the BFS approach.)
    // - Already-linked `next` pointers from a previous run -> the algorithm
    //   overwrites them correctly since it only ever reads left/right child pointers
    //   and the already-built current level's `next` pointers, not stale lower-level
    //   `next` values.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) because every node is visited exactly once across all
    // levels combined -- each node participates in exactly one "curr" step where its
    // next pointer (or its children's) is set.
    // Space Complexity: O(1) extra space for the optimized approach -- only a fixed
    // number of pointer variables (leftmost, curr) are used regardless of tree size;
    // no recursion and no queue. (The brute-force BFS approach uses O(w) queue space,
    // where w is the widest level, up to ~n/2 for a perfect tree's last level.)

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What changes if the tree is a general binary tree, NOT guaranteed perfect
    //   (some nodes missing children)? (the direct pointer-bridging trick breaks;
    //   need a dummy/sentinel node per level to track the "last connected node so
    //   far" while still walking via next pointers, or fall back to BFS with a queue)
    // - How would you verify, given a populated tree, that the `next` pointers are
    //   all correct? (BFS level by level, confirming each level's chain ends in null
    //   and matches the expected left-to-right node values)
    // - Could you solve this recursively instead of iteratively? (yes -- recursively
    //   connect two children of the same node, then recursively connect the "outer"
    //   pair across siblings, similar to the classic "connect two nodes" helper
    //   pattern; but that adds O(log n) recursion stack space, technically not O(1))
    // - Why does curr.right.next = curr.next.left specifically bridge across
    //   different parents correctly? (because curr.next is the very next sibling on
    //   the SAME level, and its leftmost child is exactly the next node after
    //   curr.right on the level below, due to the perfect-tree guarantee)
    // - How would this generalize to an n-ary tree instead of a binary tree?
    //   (same core idea: use the already-linked parent level to derive the next
    //   level's children linkage, just iterating over more than 2 children per node)
    // - What's the tradeoff of the O(1)-space pointer-walking approach vs the
    //   simpler BFS-with-queue approach in an interview setting? (BFS is easier to
    //   get right quickly and works on ANY tree shape; the optimized approach is
    //   faster to state as "O(1) space" but relies critically on the perfect-tree
    //   guarantee and is easier to get subtly wrong under time pressure)

    static class Node {
        int val;
        Node left, right, next;
        Node(int val) { this.val = val; }
    }

    // ---------- Approach 1: brute force - BFS level order traversal using a Queue ----------
    public static Node connectBruteForce(Node root) {
        if (root == null) {
            return null; // step: nothing to do for an empty tree
        }
        Queue<Node> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // step: snapshot how many nodes belong to this level
            Node prevNode = null;
            for (int i = 0; i < levelSize; i++) {
                Node curr = queue.poll(); // step: dequeue next node in this level
                if (prevNode != null) {
                    prevNode.next = curr; // step: link previous node in this level to curr
                }
                prevNode = curr;
                if (curr.left != null) {
                    queue.offer(curr.left); // step: enqueue children for the next level
                }
                if (curr.right != null) {
                    queue.offer(curr.right);
                }
            }
            // step: prevNode is now the rightmost node of this level; its next stays null
        }
        return root;
    }

    // ---------- Approach 2: optimized - O(1) extra space using existing next pointers ----------
    public static Node connectOptimized(Node root) {
        if (root == null) {
            return null; // step: nothing to do for an empty tree
        }
        Node leftmost = root; // step: leftmost node of the level currently being used as the source

        while (leftmost.left != null) { // step: stop once we've reached the leaf level
            Node curr = leftmost; // step: walk across the current level via next pointers
            while (curr != null) {
                curr.left.next = curr.right; // step: same-parent children are always adjacent

                if (curr.next != null) {
                    // step: bridge across two different parents' subtrees
                    curr.right.next = curr.next.left;
                }

                curr = curr.next; // step: advance across the current level
            }
            leftmost = leftmost.left; // step: move down to the next level to link
        }
        return root;
    }

    // ---------- Demo helpers ----------
    private static Node buildPerfectTree() {
        //          1
        //        /   \
        //       2     3
        //      / \   / \
        //     4   5 6   7
        Node root = new Node(1);
        root.left = new Node(2);
        root.right = new Node(3);
        root.left.left = new Node(4);
        root.left.right = new Node(5);
        root.right.left = new Node(6);
        root.right.right = new Node(7);
        return root;
    }

    private static String levelOrderWithNext(Node root) {
        StringBuilder sb = new StringBuilder();
        Node levelStart = root;
        while (levelStart != null) {
            Node curr = levelStart;
            while (curr != null) {
                sb.append(curr.val);
                sb.append(curr.next != null ? "->" : "->null ");
                curr = curr.next;
            }
            sb.append("| ");
            levelStart = levelStart.left;
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Perfect binary tree, brute force BFS approach");
        Node tree1 = buildPerfectTree();
        connectBruteForce(tree1);
        System.out.println(levelOrderWithNext(tree1));
        // Expected: 1->null | 2->3->null | 4->5->6->7->null |

        System.out.println("Test 2: Perfect binary tree, optimized O(1)-space approach");
        Node tree2 = buildPerfectTree();
        connectOptimized(tree2);
        System.out.println(levelOrderWithNext(tree2));
        // Expected: 1->null | 2->3->null | 4->5->6->7->null |

        System.out.println("Test 3: Edge case - single node tree");
        Node single = new Node(42);
        connectOptimized(single);
        System.out.println("next: " + single.next); // Expected: null

        System.out.println("Test 4: Edge case - empty tree (null root)");
        Node empty = connectOptimized(null);
        System.out.println("Result: " + empty); // Expected: null
    }
}
