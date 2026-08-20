package com.playground.java.interview.trees;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Binary Tree / Preorder DFS + String Encoding
 * PRIORITY: P1 - Frequently Asked
 * ONE-LINE PROBLEM STATEMENT: Convert a binary tree into a single string
 * (serialize) and reconstruct an identical tree from that string (deserialize).
 */
public class SerializeDeserializeBinaryTree {

    // ================= PROBLEM =================
    // Design an algorithm to serialize a binary tree into a single String, and to
    // deserialize that String back into a tree that has the exact same structure and
    // values as the original. serialize(deserialize(s)) == s in effect, and
    // deserialize(serialize(root)) must reproduce the same shape as root.
    //
    // Example tree:
    //        1
    //       / \
    //      2   3
    //         / \
    //        4   5
    //
    // Serialized (preorder, "null" marker for missing children, comma-delimited):
    //   "1,2,null,null,3,4,null,null,5,null,null"
    // Deserializing that string must rebuild the exact tree above.

    // ================= SIMPLE APPROACH =================
    // A tempting shortcut is to serialize only the non-null values using a level-order
    // (BFS) scan without any null markers, e.g. just "1,2,3,4,5". This is compact, but
    // it throws away structural information: is 4 the left child of 2 or of 3? Is 5 a
    // left or right child? Without explicit markers for missing children (or without
    // storing the tree's shape some other way, like a full array representation with
    // fixed 2*i/2*i+1 indexing), a plain list of present values is fundamentally
    // ambiguous -- many different tree shapes produce the same list of non-null values.

    // ================= WHY IT'S NOT ENOUGH =================
    // Omitting "null" markers means deserialization cannot tell where each node's
    // children should attach. For example, both of these distinct trees:
    //     1                1
    //      \              /
    //       2     and    2
    //      /              \
    //     3                3
    // would serialize to the same value list "1,2,3" if null markers are dropped,
    // so deserialize() has no way to recover which tree was the original. The fix is
    // to explicitly record every missing child as a placeholder token (e.g. the
    // string "null") in the output, so the traversal order alone is enough to
    // unambiguously reconstruct the shape on the way back in.

    // ================= OPTIMIZED APPROACH =================
    // Serialize: perform a preorder traversal (root, then left, then right). At every
    // node, append its value to a delimiter-joined string; when a child is missing,
    // append the literal marker "null" instead of recursing into it. This guarantees
    // every node (real or missing) contributes exactly one token, in a fixed,
    // recoverable order.
    //     serialize(node) = "null,"                                   if node == null
    //     serialize(node) = node.val + "," + serialize(node.left) + serialize(node.right)  otherwise
    //
    // Deserialize: split the string on the delimiter to get a flat sequence of tokens,
    // then consume them in the SAME preorder sequence they were written in, using
    // either a Queue<String> (poll from the front) or a single index pointer that only
    // ever advances forward:
    //   1. Read (consume) the next token.
    //   2. If it's "null", this subtree is empty -- return null immediately, and do
    //      NOT consume any further tokens for this call (there are none for a null node).
    //   3. Otherwise, create a new TreeNode with that value, then RECURSIVELY consume
    //      the next tokens for node.left first, then for node.right -- in that exact
    //      order, mirroring how they were written during serialization.
    // Because both directions walk the tree in the same fixed preorder sequence, the
    // token stream alone (no extra bookkeeping) is enough to perfectly rebuild the tree.

    // ================= WHY THIS DATA STRUCTURE =================
    // A Queue<String> (here, ArrayDeque used via poll() from the front) is the natural
    // fit for deserialization because it models "consume tokens strictly in the order
    // they arrive, one at a time, never revisiting an already-consumed token" -- which
    // is exactly what reconstructing a preorder sequence requires. An alternative that
    // works equally well is a single mutable index/pointer into an array of tokens
    // (avoiding the overhead of a Queue's internal structure), but it requires the
    // pointer to be passed by reference (e.g. via a single-element array, an
    // AtomicInteger, or an instance field) since Java has no native "int&" -- a Queue
    // sidesteps that awkwardness entirely because polling naturally mutates shared
    // state without needing an explicit reference wrapper. A Stack would be the wrong
    // choice here since it would consume tokens in reverse order, breaking the fixed
    // root-left-right sequence that the recursive rebuild relies on.

    // ================= EDGE CASES =================
    // - Empty tree / null root -> serializes to just "null" (a single marker token,
    //   no trailing content), and deserializing "null" must yield a null root, not an
    //   exception.
    // - Single node -> serializes to "value,null,null" (value plus two missing-child markers).
    // - Skewed tree (all left or all right children) -> every "missing" side
    //   contributes a "null" token at every level, so the string length grows
    //   proportionally to tree height times the branching being skipped -- still
    //   correct, just less compact than a balanced tree's encoding.
    // - Duplicate values across different nodes -> irrelevant to correctness, since
    //   reconstruction relies purely on token POSITION/ORDER, never on values being unique.
    // - Node values that could themselves contain the delimiter character (e.g. a
    //   comma) if this were adapted to store non-integer data -- would require escaping
    //   or a length-prefixed encoding instead of a naive delimiter split.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both serialize and deserialize -- every node (and
    // every null-child marker, of which there are also O(n)) is visited/emitted or
    // consumed exactly once.
    // Space Complexity: O(n) for the output string itself and for the token
    // queue/array during deserialization (proportional to 2n+1 tokens: n real nodes
    // plus up to n+1 null markers), PLUS O(h) recursion call-stack space for both the
    // serializing and deserializing DFS, where h is tree height (O(log n) balanced,
    // O(n) worst case for a skewed tree).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must missing children be explicitly marked, rather than just omitted?
    //   (Without markers, the traversal order alone is ambiguous -- multiple distinct
    //   tree shapes can produce the same sequence of present values.)
    // - Could you use level-order (BFS) serialization instead of preorder? (Yes -- just
    //   as valid, as long as serialize and deserialize agree on the same traversal
    //   order and both consistently mark missing children.)
    // - How would you serialize a Binary SEARCH Tree more compactly, exploiting the
    //   sorted-order property? (You can skip "null" markers entirely and reconstruct
    //   purely from value order plus a min/max bound per recursive call, since a BST's
    //   structure is fully determined by value ordering.)
    // - What if the delimiter character could also appear inside a value (e.g. if
    //   values were arbitrary strings, not integers)? (Need escaping or a
    //   length-prefixed / count-prefixed token format instead of naive comma-splitting.)
    // - How would you avoid recursion for very deep/skewed trees during deserialization?
    //   (Use an explicit stack-based iterative rebuild instead of recursive calls.)
    // - How does this compare to serializing via a level-order BFS with an explicit
    //   queue for BOTH directions, instead of preorder recursion? (Same asymptotic
    //   complexity; BFS keeps a "frontier" of not-yet-expanded nodes instead of
    //   relying on the call stack, trading stack space for queue space.)
    // - Is the produced serialization format the SHORTEST possible encoding? (No --
    //   it's not bit-packed or Huffman-coded; it optimizes for correctness and
    //   simplicity, not size.)

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    private static final String NULL_MARKER = "null";
    private static final String DELIMITER = ",";

    // ---------- Approach: preorder DFS serialize, token-consuming DFS deserialize ----------
    public static String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

    private static void serializeHelper(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append(NULL_MARKER).append(DELIMITER); // step: explicit marker for a missing child
            return;
        }
        sb.append(node.val).append(DELIMITER); // step: visit root first (preorder)
        serializeHelper(node.left, sb);        // step: then left subtree
        serializeHelper(node.right, sb);       // step: then right subtree
    }

    public static TreeNode deserialize(String data) {
        Deque<String> tokens = new ArrayDeque<>();
        for (String token : data.split(DELIMITER)) {
            tokens.add(token); // step: load the flat preorder token sequence into a queue
        }
        return deserializeHelper(tokens);
    }

    private static TreeNode deserializeHelper(Deque<String> tokens) {
        String token = tokens.poll(); // step: consume exactly one token, in the same order it was written
        if (token == null || token.equals(NULL_MARKER)) {
            return null; // base case: this subtree was recorded as missing
        }
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left = deserializeHelper(tokens);  // step: rebuild left subtree next (matches serialize order)
        node.right = deserializeHelper(tokens); // step: then right subtree
        return node;
    }

    // ---------- Demo helpers ----------
    private static TreeNode buildSampleTree() {
        //        1
        //       / \
        //      2   3
        //         / \
        //        4   5
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.right.left = new TreeNode(4);
        root.right.right = new TreeNode(5);
        return root;
    }

    // Helper for verifying round-trip correctness by comparing tree shape/values.
    private static boolean treesEqual(TreeNode a, TreeNode b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.val == b.val && treesEqual(a.left, b.left) && treesEqual(a.right, b.right);
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Sample tree [1,2,3,null,null,4,5]");
        TreeNode sample = buildSampleTree();
        String serialized = serialize(sample);
        System.out.println("Serialized: " + serialized); // Expected: "1,2,null,null,3,4,null,null,5,null,null,"
        TreeNode restored = deserialize(serialized);
        System.out.println("Round-trip matches original: " + treesEqual(sample, restored)); // Expected: true

        System.out.println();
        System.out.println("Test 2: Single node [42]");
        TreeNode single = new TreeNode(42);
        String serializedSingle = serialize(single);
        System.out.println("Serialized: " + serializedSingle); // Expected: "42,null,null,"
        System.out.println("Round-trip matches original: " + treesEqual(single, deserialize(serializedSingle))); // Expected: true

        System.out.println();
        System.out.println("Test 3: Empty tree (null root)");
        String serializedEmpty = serialize(null);
        System.out.println("Serialized: " + serializedEmpty); // Expected: "null,"
        TreeNode restoredEmpty = deserialize(serializedEmpty);
        System.out.println("Round-trip matches original (both null): " + (restoredEmpty == null)); // Expected: true
    }
}
