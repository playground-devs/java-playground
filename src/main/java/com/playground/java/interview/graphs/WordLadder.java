package com.playground.java.interview.graphs;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * PATTERN: Graph / BFS Shortest Path on an Implicit Graph (Word Ladder)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Find the length of the shortest transformation sequence from beginWord to
 * endWord, changing exactly one letter at a time through words in a given wordList.
 */
public class WordLadder {

    // ================= PROBLEM =================
    // Given beginWord, endWord, and a dictionary wordList, find the length of the shortest
    // transformation sequence from beginWord to endWord such that only one letter changes at a
    // time, and every intermediate word (including endWord) must exist in wordList. Return the
    // number of words in the sequence, or 0 if no such sequence exists.
    // Example: beginWord = "hit", endWord = "cog", wordList = ["hot","dot","dog","lot","log","cog"]
    //          -> output = 5 (hit -> hot -> dot -> dog -> cog)
    //
    // ================= SIMPLE APPROACH =================
    // From the current word, compare it against every other remaining word in wordList to find
    // ones that differ by exactly one letter, treat those as graph neighbors, and explore them
    // via BFS to guarantee the shortest chain length.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Comparing a word against every other word in wordList to find its neighbors costs
    // O(wordList.length * wordLength) per word. Since BFS may visit up to wordList.length words,
    // and each visit repeats this full-list scan, total time becomes roughly
    // O(wordList.length^2 * wordLength) - quadratic in dictionary size, far too slow for large
    // dictionaries.
    //
    // ================= OPTIMIZED APPROACH =================
    // Generate neighbors WITHOUT scanning wordList at all: for the current word, try replacing
    // each character position with each of the 26 lowercase letters (skipping the letter already
    // there), producing a candidate word, and check in O(1) whether that candidate exists in a
    // HashSet built from wordList. This turns "find my neighbors" from an O(wordList.length)
    // linear scan into an O(26 * wordLength) operation independent of dictionary size. Then run
    // standard level-by-level BFS: start from beginWord at length 1, and the moment endWord is
    // generated, return the current level (word count so far). Remove each word from the HashSet
    // once used, so it acts as a visited marker and is never enqueued twice.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashSet<String> gives O(1) average membership checks - essential since we generate up to
    // 26 * wordLength candidate strings per word and need to instantly know if each is a valid,
    // unvisited dictionary word (removing consumed words also makes the set double as the
    // "visited" tracker, with no extra structure needed). A Queue drives the BFS itself,
    // guaranteeing the first time endWord is reached is via the shortest possible chain - the
    // same level-by-level guarantee used in multi-source BFS problems like Rotting Oranges.
    //
    // ================= EDGE CASES =================
    // - endWord is not in wordList at all: return 0 immediately, since every word in the
    //   sequence (including the final one) must be a valid dictionary word.
    // - beginWord equals endWord: trivially a sequence of length 1 (no transformation needed);
    //   state this assumption explicitly since some variants may disallow it.
    // - No path exists between beginWord and endWord through the dictionary: BFS exhausts the
    //   queue without reaching endWord, return 0.
    // - wordList is empty: return 0 (unless beginWord equals endWord).
    // - beginWord happens to also be present in wordList: harmless, it's simply removed from the
    //   set once enqueued so it's never revisited.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(wordList.length * wordLength^2) - for every dequeued word (up to
    // wordList.length of them), we try wordLength positions times 26 letters, and each candidate
    // costs O(wordLength) to build and hash/compare.
    // Space Complexity: O(wordList.length * wordLength) for the HashSet of dictionary words, plus
    // O(wordList.length) for the BFS queue in the worst case.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you reconstruct the actual shortest transformation sequence, not just its length, using parent pointers?
    // - How could bidirectional BFS (searching from both beginWord and endWord simultaneously) speed this up on large dictionaries?
    // - What if wordList contains words of different lengths than beginWord - how would you filter those out up front?
    // - How would you find ALL shortest transformation sequences (Word Ladder II), not just the length of one?
    // - Why is generating 26*wordLength candidates per word better than scanning the whole wordList as the dictionary grows large?
    // - What's a fast early check you can do if beginWord and endWord differ in length?

    public static int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> dictionary = new HashSet<>(wordList);
        if (!dictionary.contains(endWord)) {
            return 0; // endWord must be a valid dictionary word
        }

        Queue<String> queue = new ArrayDeque<>();
        queue.offer(beginWord);
        dictionary.remove(beginWord); // Step: mark beginWord as used so it's never revisited.

        int length = 1; // beginWord itself counts as the first word in the sequence

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // Step: snapshot this BFS level (one letter-change away).

            for (int i = 0; i < levelSize; i++) {
                String word = queue.poll();
                if (word.equals(endWord)) {
                    return length; // reached the target at this BFS level
                }

                char[] chars = word.toCharArray();
                for (int pos = 0; pos < chars.length; pos++) {
                    char original = chars[pos];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == original) {
                            continue; // skip the letter that's already there
                        }
                        chars[pos] = c;
                        String candidate = new String(chars);
                        if (dictionary.contains(candidate)) {
                            // Step: valid unvisited neighbor - mark visited and enqueue it.
                            dictionary.remove(candidate);
                            queue.offer(candidate);
                        }
                    }
                    chars[pos] = original; // Step: restore before trying the next position.
                }
            }

            length++; // moving to the next BFS level = one more word in the chain
        }

        return 0; // queue exhausted without reaching endWord - no valid sequence
    }

    public static void main(String[] args) {
        List<String> wordList1 = List.of("hot", "dot", "dog", "lot", "log", "cog");
        // Expected: 5 (hit -> hot -> dot -> dog -> cog)
        System.out.println("Input: beginWord=\"hit\", endWord=\"cog\", wordList=" + wordList1);
        System.out.println("Output: " + ladderLength("hit", "cog", wordList1));

        List<String> wordList2 = List.of("hot", "dot", "dog", "lot", "log");
        // Expected: 0 ("cog" is not in wordList)
        System.out.println("\nInput: beginWord=\"hit\", endWord=\"cog\", wordList=" + wordList2 + " (missing endWord)");
        System.out.println("Output: " + ladderLength("hit", "cog", wordList2));

        List<String> wordListEmpty = List.of();
        // Expected: 0 (empty dictionary, endWord unreachable)
        System.out.println("\nInput: beginWord=\"hit\", endWord=\"cog\", wordList=[] (empty dictionary)");
        System.out.println("Output: " + ladderLength("hit", "cog", wordListEmpty));
    }
}
