package com.playground.java.interview.graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * PATTERN: Graph / Topological Sort (Kahn's Algorithm)
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a list of words sorted lexicographically according to an unknown
 * alien language's alphabet, derive a valid ordering of the alien alphabet's letters.
 */
public class AlienDictionary {

    // ================= PROBLEM =================
    // You are given a list of words that are supposedly sorted according to the rules of some
    // alien language's alphabet (which uses the same lowercase English letters, but in a
    // different order). Determine one valid ordering of the alphabet consistent with this
    // sorted list, or report that no valid ordering exists.
    // Example: words = ["wrt","wrf","er","ett","rftt"] -> output = "wertf"
    // (comparing "wrt"->"wrf" tells us t comes before f; "wrf"->"er" tells us w comes before e;
    //  "er"->"ett" tells us r comes before t; "ett"->"rftt" tells us e comes before r.)
    //
    // ================= SIMPLE APPROACH =================
    // (This is inherently a graph + topological sort problem - there isn't a simpler approach
    // below building a precedence graph from adjacent word comparisons and then ordering it.)
    //
    // ================= OPTIMIZED APPROACH =================
    // 1) Build a graph of character precedence: compare each pair of ADJACENT words in the list.
    //    Find the first position where their characters differ - that tells us
    //    "word1's character comes before word2's character" in the alien alphabet (an edge
    //    earlierChar -> laterChar). Only the FIRST differing character between each adjacent
    //    pair gives useful information; stop comparing that pair right there.
    // 2) Special invalid case: if word1 is longer than word2 AND word2 is a strict prefix of
    //    word1 (e.g. "abc" appears before "ab"), this is impossible in any valid lexicographic
    //    order - report no valid ordering exists.
    // 3) Include every unique character seen across all words as a node in the graph, even ones
    //    with no edges (they can appear anywhere consistent with the rest of the order).
    // 4) Run topological sort (Kahn's algorithm: build in-degree counts, seed a queue with all
    //    in-degree-zero characters, repeatedly dequeue and decrement neighbors' in-degrees).
    // 5) If the resulting order includes every character, that's a valid alphabet ordering.
    //    If not all characters got included, there's a cycle in the precedence graph, meaning
    //    the given word list is contradictory - no valid ordering exists.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // An adjacency structure (Map<Character, Set<Character>>) plus an in-degree map captures
    // exactly the "must come before" relationships extracted from adjacent word comparisons,
    // and Kahn's BFS-based topological sort is the natural way to turn those pairwise
    // precedence constraints into one consistent total (or partial) ordering, while
    // simultaneously detecting contradictions (cycles) if the input is invalid.
    //
    // ================= EDGE CASES =================
    // - A longer word appears before its own prefix (e.g. ["abc", "ab"]): invalid input, no
    //   ordering possible - must be explicitly detected, since it wouldn't naturally show up as
    //   a graph cycle otherwise.
    // - Words list has only one word: no ordering information at all, but the character set can
    //   still be returned in any order.
    // - Duplicate adjacent words: no useful information, no edges generated from a pair with no
    //   differing characters (as long as neither is a strict-prefix violation of the other).
    // - A genuine cycle in the derived graph (e.g. edges a->b->c->a from contradictory
    //   comparisons): correctly detected by topological sort producing fewer nodes than exist.
    // - Characters that never appear in any comparison (isolated nodes): still included in the
    //   final order since we seed the graph with every character from every word up front.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(C) where C is the total number of characters across all words (for
    // building the graph by comparing adjacent words), plus O(V + E) for the topological sort
    // where V = 26 (at most) and E = number of derived precedence edges - overall close to
    // linear in the total input size.
    // Space Complexity: O(1) for the alphabet-sized graph structures (bounded by 26 letters),
    // plus O(C) transiently while scanning words.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you detect and clearly report the SPECIFIC invalid case where a word is followed by its own prefix?
    // - How would you return ALL valid alien alphabet orderings, not just one (if multiple are consistent with the input)?
    // - Why do we only need to look at the FIRST differing character between adjacent words, and not compare every character pair?
    // - How would you verify a given candidate alphabet ordering against the original word list, to double check correctness?
    // - What happens if the word list is very large - how would you avoid redundant edges being added to the graph (e.g. using a Set to dedupe edges)?
    // - How does this generalize to alphabets larger than 26 lowercase letters (e.g. Unicode characters)?

    // Optimized: build precedence graph from adjacent word pairs, then Kahn's topological sort.
    public static String alienOrder(String[] words) {
        // Step 1: seed the graph with every unique character seen, and prepare in-degree map.
        Map<Character, Set<Character>> graph = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }

        // Step 2: compare each pair of adjacent words to derive precedence edges.
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            int minLength = Math.min(word1.length(), word2.length());
            boolean foundDifference = false;

            for (int j = 0; j < minLength; j++) {
                char c1 = word1.charAt(j);
                char c2 = word2.charAt(j);
                if (c1 != c2) {
                    // Step: c1 must come before c2 - add the edge if it's new.
                    if (!graph.get(c1).contains(c2)) {
                        graph.get(c1).add(c2);
                        inDegree.merge(c2, 1, Integer::sum);
                    }
                    foundDifference = true;
                    break;
                }
            }

            // Step: invalid case - a longer word appears before its own prefix.
            if (!foundDifference && word1.length() > word2.length()) {
                return "";
            }
        }

        // Step 3: Kahn's algorithm - seed queue with all in-degree-zero characters.
        Queue<Character> queue = new ArrayDeque<>();
        for (Map.Entry<Character, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        StringBuilder order = new StringBuilder();
        while (!queue.isEmpty()) {
            char current = queue.poll();
            order.append(current);
            for (char neighbor : graph.get(current)) {
                inDegree.merge(neighbor, -1, Integer::sum);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Step 4: if not all characters were ordered, a cycle exists - invalid input.
        if (order.length() != inDegree.size()) {
            return "";
        }
        return order.toString();
    }

    public static void main(String[] args) {
        String[] words1 = {"wrt", "wrf", "er", "ett", "rftt"};
        // Expected: "wertf" (one valid ordering)
        System.out.println("Input: [\"wrt\",\"wrf\",\"er\",\"ett\",\"rftt\"]");
        System.out.println("Output: " + alienOrder(words1));

        String[] words2 = {"z", "x", "z"};
        // Expected: "" (contradiction: z<x from first pair, x<z from second pair - cycle)
        System.out.println("\nInput: [\"z\",\"x\",\"z\"] (contradictory)");
        System.out.println("Output: \"" + alienOrder(words2) + "\"");

        String[] words3 = {"abc", "ab"};
        // Expected: "" (invalid: longer word appears before its own prefix)
        System.out.println("\nInput: [\"abc\",\"ab\"] (longer word before its own prefix)");
        System.out.println("Output: \"" + alienOrder(words3) + "\"");
    }
}
