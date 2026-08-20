package com.playground.java.interview.hashmap;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: HashMap / Bijective Mapping
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Determine whether two strings are isomorphic, meaning characters
 * in the first string can be consistently and uniquely replaced to get the second string.
 */
public class IsomorphicStrings {

    // ================= PROBLEM =================
    // You get two strings, s and t, of the same length.
    // You need to check if they are isomorphic: every character in s must map to
    // exactly one character in t, and that mapping must be consistent everywhere,
    // AND no two different characters in s can map to the same character in t.
    // Example: s = "egg", t = "add" -> output = true (e->a, g->d, consistent both ways)
    // Example: s = "foo", t = "bar" -> output = false (o maps to both 'a' and 'r')
    //
    // ================= SIMPLE APPROACH =================
    // There isn't really a meaningfully different "slower" brute force here since
    // the natural single-pass mapping check is already the right approach; a naive
    // alternative would be to try building the mapping and checking consistency
    // using nested loops for every pair of positions with the same character, but
    // that adds no value over one clean linear pass. Instead, the "simple approach"
    // shown below uses only one HashMap (s -> t direction), which looks correct at
    // first glance but has a subtle bug.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Using only a single HashMap<Character, Character> from s's characters to t's
    // characters correctly enforces "each s character maps to only one t character",
    // but it does NOT catch the case where two different s characters map to the
    // SAME t character (e.g., s = "ab", t = "aa" would incorrectly look valid with
    // a one-directional map, but it's not truly isomorphic since both 'a' and 'b'
    // map to 'a', which is not a one-to-one/bijective mapping).
    //
    // ================= OPTIMIZED APPROACH =================
    // Use two HashMaps: one mapping characters from s to t, and another mapping
    // characters from t to s. Walk through both strings together at each index i.
    // Check: if s[i] is already mapped, it must map to t[i] (otherwise inconsistent).
    // Check: if t[i] is already mapped (in the reverse map), it must map back to s[i]
    // (otherwise two different s characters are mapping to the same t character).
    // If both checks pass, record the mapping in both directions and continue.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Two HashMaps give O(1) average lookups in both directions, letting us verify
    // a true bijective (one-to-one and onto) relationship between the character sets
    // of the two strings in a single linear pass, which a single one-directional map
    // cannot guarantee on its own.
    //
    // ================= EDGE CASES =================
    // - Strings of different lengths: cannot be isomorphic, return false immediately.
    // - Empty strings: trivially isomorphic (true), no characters to conflict.
    // - Same character mapping to itself throughout (e.g., "aa" and "aa"): valid.
    // - A character mapping to itself in position but colliding elsewhere (e.g., "ab", "aa"): invalid.
    // - Strings where one character in s always maps consistently but breaks the reverse mapping.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - a single pass through both strings (length n) with
    // O(1) average HashMap operations at each step.
    // Space Complexity: O(k) where k is the number of distinct characters (bounded
    // by the character set size, e.g., at most 256 for extended ASCII), effectively O(1)
    // for a fixed alphabet, or O(n) in the worst case for a very large/varied alphabet.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is a single one-directional HashMap not sufficient to prove isomorphism?
    // - Can you solve this without HashMaps, using two integer arrays if the character set is small and known (like ASCII)?
    // - How would you check if a string follows a given "pattern" (a classic isomorphism variant, e.g. LeetCode 290)?
    // - What if the strings were extremely long - would you consider streaming character by character instead of loading both fully?
    // - How does isomorphism differ from just checking if the strings have the same character frequency pattern?
    // - What if the mapping needed to allow a character to map to itself only under certain rules?
    // - Can you generalize this to check isomorphism between two arrays of arbitrary objects, not just characters?

    // Optimized: two HashMaps enforce a true bijective mapping in one pass. O(n) time, O(k) space.
    public static boolean isIsomorphicOptimized(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }

        Map<Character, Character> sToT = new HashMap<>();
        Map<Character, Character> tToS = new HashMap<>();

        for (int i = 0; i < s.length(); i++) {
            char sChar = s.charAt(i);
            char tChar = t.charAt(i);

            // Check the forward mapping s -> t is consistent.
            if (sToT.containsKey(sChar) && sToT.get(sChar) != tChar) {
                return false;
            }
            // Check the reverse mapping t -> s is consistent (catches many-to-one collisions).
            if (tToS.containsKey(tChar) && tToS.get(tChar) != sChar) {
                return false;
            }

            sToT.put(sChar, tChar);
            tToS.put(tChar, sChar);
        }
        return true;
    }

    // Buggy single-direction approach kept here for comparison/teaching purposes only.
    // This INCORRECTLY reports true for cases like s="ab", t="aa".
    public static boolean isIsomorphicSingleDirectionBuggy(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        Map<Character, Character> sToT = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            char sChar = s.charAt(i);
            char tChar = t.charAt(i);
            if (sToT.containsKey(sChar) && sToT.get(sChar) != tChar) {
                return false;
            }
            sToT.put(sChar, tChar);
        }
        return true;
    }

    public static void main(String[] args) {
        // Expected: true (e->a, g->d)
        System.out.println("Input: s=\"egg\", t=\"add\"");
        System.out.println("Optimized output: " + isIsomorphicOptimized("egg", "add"));

        // Expected: false (o maps to both a and r)
        System.out.println("\nInput: s=\"foo\", t=\"bar\"");
        System.out.println("Optimized output: " + isIsomorphicOptimized("foo", "bar"));

        // Expected: false (b and a both map to a) - demonstrates why single-direction map fails
        System.out.println("\nInput: s=\"ab\", t=\"aa\" (many-to-one collision)");
        System.out.println("Optimized (correct) output: " + isIsomorphicOptimized("ab", "aa"));
        System.out.println("Single-direction (buggy) output: " + isIsomorphicSingleDirectionBuggy("ab", "aa"));
    }
}
