package com.playground.java.interview.trie;

/**
 * PATTERN: Trie (Prefix Tree)
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Implement a Trie with insert(word), search(word), and
 * startsWith(prefix) operations.
 */
public class ImplementTrie {

    // ================= PROBLEM =================
    // Design a data structure that can efficiently store a set of words and support:
    // - insert(word): add a word to the set.
    // - search(word): check if the exact word exists in the set.
    // - startsWith(prefix): check if any word in the set starts with the given prefix.
    // Example: insert("apple"); search("apple") -> true; search("app") -> false;
    //          startsWith("app") -> true.
    //
    // ================= SIMPLE APPROACH =================
    // Keep all inserted words in a List<String> (or a HashSet<String> for search). To search
    // for an exact word, check set membership. To check startsWith(prefix), scan every stored
    // word and check if it starts with the prefix.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // A HashSet gives O(L) exact search (L = word length) using hashing, but startsWith(prefix)
    // requires scanning ALL N stored words and checking each one, giving O(N*L) time - very
    // wasteful when there are many words and many prefix queries, which is the operation a Trie
    // is specifically designed to make fast.
    //
    // ================= OPTIMIZED APPROACH =================
    // Build a Trie (prefix tree): a tree where each node represents one character, and a path
    // from the root spells out a prefix (or full word). Each node has up to 26 children (one
    // per lowercase letter) and a boolean flag marking "a word ends here".
    // - insert(word): starting at the root, for each character, move to (or create) the child
    //   node for that character. After processing the last character, mark that node's
    //   isEndOfWord flag as true.
    // - search(word): walk the Trie following each character of the word. If any character has
    //   no matching child, the word doesn't exist - return false. If we successfully walk the
    //   whole word, return true only if the final node's isEndOfWord flag is true (a prefix that
    //   isn't itself a complete inserted word should return false).
    // - startsWith(prefix): same walk as search, but return true as soon as we successfully walk
    //   the whole prefix, regardless of isEndOfWord (we just need SOME word to start this way).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A Trie shares common prefixes across words as shared tree paths, so both search and
    // startsWith only need to walk exactly L characters (the length of the word/prefix being
    // queried) - completely independent of how many words N are stored. This turns startsWith
    // from O(N*L) into O(L), which is the entire point of using this structure over a flat list
    // or hash set.
    //
    // ================= EDGE CASES =================
    // - Searching for a word that is a strict prefix of an inserted word but was never itself
    //   inserted (e.g. inserted "apple", search("app")): must return false (isEndOfWord check).
    // - Empty string: insert("") should mark the root itself as isEndOfWord; startsWith("") is
    //   trivially true if the Trie is non-empty (every word starts with the empty prefix).
    // - Inserting the same word twice: should be idempotent, no duplicate effect.
    // - Case sensitivity: this implementation assumes lowercase a-z only; would need
    //   adjustment (e.g. a HashMap<Character, TrieNode> instead of a fixed size-26 array) for
    //   mixed case or Unicode.
    // - Searching in an empty Trie (nothing inserted yet): always returns false.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(L) for insert, search, and startsWith, where L is the length of the
    // word/prefix - each operation walks the Trie exactly L levels deep, regardless of how many
    // words N are stored (versus O(N*L) for a naive list scan of startsWith).
    // Space Complexity: O(total characters across all inserted words) in the worst case (no
    // shared prefixes); shared prefixes reduce this since overlapping paths are stored once.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you extend this Trie to support wildcard search (e.g. ".ad" matches "bad", "cad", "dad")?
    // - How would you support deleting a word from the Trie (careful: don't remove nodes still shared by other words)?
    // - How would you modify the Trie to also return the COUNT of words with a given prefix, not just whether one exists?
    // - Why use a fixed-size array of 26 children instead of a HashMap<Character, TrieNode> - what's the tradeoff (space vs flexibility for Unicode/mixed case)?
    // - How is a Trie used in autocomplete systems or IP routing (longest prefix match)?
    // - How would you serialize/deserialize a Trie to persist it to disk?
    // - How does WordSearchII use a Trie to speed up searching for multiple words on a board simultaneously?

    // Nested static TrieNode: each node has up to 26 children (a-z) and an end-of-word flag.
    static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isEndOfWord = false;
    }

    private final TrieNode root;

    public ImplementTrie() {
        root = new TrieNode();
    }

    // Insert a word into the Trie, creating nodes along the way as needed.
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (current.children[index] == null) {
                // Step: no existing path for this character - create a new node.
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        current.isEndOfWord = true;
    }

    // Check if the exact word was inserted (must end exactly at a marked node).
    public boolean search(String word) {
        TrieNode node = walk(word);
        return node != null && node.isEndOfWord;
    }

    // Check if any inserted word starts with the given prefix.
    public boolean startsWith(String prefix) {
        return walk(prefix) != null;
    }

    // Shared helper: walk the Trie along the given string, returning the final node or null.
    private TrieNode walk(String s) {
        TrieNode current = root;
        for (char c : s.toCharArray()) {
            int index = c - 'a';
            if (current.children[index] == null) {
                return null;
            }
            current = current.children[index];
        }
        return current;
    }

    public static void main(String[] args) {
        ImplementTrie trie = new ImplementTrie();
        trie.insert("apple");

        // Expected: search("apple") = true, search("app") = false, startsWith("app") = true
        System.out.println("Insert: apple");
        System.out.println("search(\"apple\"): " + trie.search("apple"));
        System.out.println("search(\"app\"): " + trie.search("app"));
        System.out.println("startsWith(\"app\"): " + trie.startsWith("app"));

        trie.insert("app");
        // Expected: search("app") = true now that it's explicitly inserted
        System.out.println("\nInsert: app");
        System.out.println("search(\"app\"): " + trie.search("app"));

        // Expected: search/startsWith on a totally different prefix returns false
        System.out.println("\nsearch(\"banana\"): " + trie.search("banana"));
        System.out.println("startsWith(\"ban\"): " + trie.startsWith("ban"));
    }
}
