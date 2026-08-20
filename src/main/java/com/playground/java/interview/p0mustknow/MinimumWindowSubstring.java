package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Sliding Window + HashMap
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Find the smallest substring of s that contains every character of t (including duplicates).
 */
// ================= PROBLEM =================
// Given two strings s and t, find the smallest window (contiguous substring) in s
// that contains all the characters of t, including matching how many times each
// character appears in t. If no such window exists, return an empty string.
//
// Example: s = "ADOBECODEBANC", t = "ABC"
// The smallest substring of s containing 'A', 'B', and 'C' is "BANC" -> Output: "BANC"
//
// ================= SIMPLE APPROACH =================
// Try every possible substring of s (every start index, every end index), and for
// each substring check whether it contains all characters of t with the right counts
// (by building a frequency map of the substring and comparing it against t's frequency
// map). Keep track of the smallest valid substring found.
//
// ================= WHY IT'S NOT ENOUGH =================
// There are O(n^2) possible substrings, and checking each one against t's requirement
// takes O(n) time (to build/compare frequency maps), giving O(n^3) total in the worst
// case (or O(n^2) with some optimization). For large strings this is far too slow.
// We are also redoing a lot of duplicate counting work as the window boundaries shift
// by just one character at a time.
//
// ================= OPTIMIZED APPROACH =================
// Use the sliding window technique with two pointers, left and right, both starting at 0.
// Step 1: Build a frequency map ("need") of every character in t, and count how many
//          distinct characters we need to fully satisfy (call this "required").
// Step 2: Expand the window by moving right forward, adding s.charAt(right) into a
//          "window" frequency map.
// Step 3: Whenever adding a character causes the window's count for that character to
//          exactly match the required count in "need", increment a "formed" counter
//          (formed = number of distinct characters currently fully satisfied).
// Step 4: While formed == required (the window is currently valid, contains everything
//          needed), try to shrink from the left to find a smaller valid window:
//            - record the window if it's the smallest seen so far
//            - remove s.charAt(left) from the window map, and if that character's count
//              drops below what's needed, decrement "formed"
//            - move left forward
// Step 5: Continue moving right until it reaches the end of s. Return the smallest
//          window recorded, or empty string if none was found.
// This way each character is added to the window once and removed at most once, so the
// total work is linear.
//
// ================= WHY THIS DATA STRUCTURE =================
// A HashMap (or a fixed-size array if we know the character set, e.g. ASCII) is ideal
// for tracking character counts because it gives O(1) average time to increment,
// decrement, and look up counts. The "have/need" counters (formed vs required) let us
// check window validity in O(1) instead of comparing two full frequency maps on every
// step, which would be much slower. Sliding window (two pointers) is the right
// technique because the problem only ever needs to consider contiguous substrings, and
// once we know a window is invalid, we never need to shrink past the point where it
// becomes invalid, so we can move pointers strictly forward, giving linear time overall.
//
// ================= EDGE CASES =================
// - t longer than s: no valid window can exist, return empty string.
// - t or s is empty or null: no valid window (empty string), guard for this explicitly.
// - t has repeated characters (e.g. "AABC"): the window must contain at least that many
//   occurrences of each character, not just presence.
// - No valid window exists at all: return empty string.
// - The entire string s itself is the smallest valid window: should still work correctly.
// - Characters in s that don't appear in t at all: they can appear inside the window,
//   they just don't help satisfy requirements.
//
// ================= COMPLEXITY =================
// Time Complexity: Brute force O(n^3) (or O(n^2) with a smarter check) because we
//                   examine every substring and validate it.
//                   Optimized sliding window O(n + m) where n = length of s, m = length
//                   of t, because the right pointer and left pointer each traverse s at
//                   most once, and building the need map takes O(m).
// Space Complexity: Brute force O(m) for frequency maps built per substring check
//                    (not counting the substrings themselves).
//                    Optimized O(m + k) where k is the number of distinct characters in
//                    the current window, bounded by the character set size.
//
// ================= INTERVIEW FOLLOW-UPS =================
// - Why do we track "formed" and "required" counts instead of comparing two full maps every time?
// - How would the solution change if the input could contain Unicode characters instead of just ASCII/uppercase letters?
// - Can you extend this to find the minimum window containing ALL characters of t in ANY multiset combination from multiple target strings?
// - How would you modify this to return all minimum-length windows, not just one?
// - What's the difference between this "variable size" sliding window and a "fixed size" sliding window problem? Give an example of each.
// - How would you optimize further if this function were called repeatedly with the same s but different t values?
// - Walk through why moving the left pointer forward can never cause us to miss a smaller valid window.

import java.util.HashMap;
import java.util.Map;

public class MinimumWindowSubstring {

    // Brute force: check every substring, O(n^3) worst case
    public static String minWindowBruteForce(String s, String t) {
        if (s == null || t == null || s.length() < t.length() || t.isEmpty()) {
            return "";
        }

        Map<Character, Integer> need = buildFrequencyMap(t);
        String best = "";

        // try every possible start and end index
        for (int start = 0; start < s.length(); start++) {
            for (int end = start + 1; end <= s.length(); end++) {
                String candidate = s.substring(start, end);
                if (candidate.length() < t.length()) {
                    continue; // can't possibly contain all of t
                }
                if (containsAll(candidate, need)) {
                    // update best if this candidate is smaller
                    if (best.isEmpty() || candidate.length() < best.length()) {
                        best = candidate;
                    }
                    break; // no need to grow this start further, we want the smallest
                }
            }
        }
        return best;
    }

    // helper: build frequency map of characters in a string
    private static Map<Character, Integer> buildFrequencyMap(String str) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : str.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    // helper: check whether candidate contains all characters (with counts) required by need
    private static boolean containsAll(String candidate, Map<Character, Integer> need) {
        Map<Character, Integer> have = buildFrequencyMap(candidate);
        for (Map.Entry<Character, Integer> entry : need.entrySet()) {
            if (have.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    // Optimized: sliding window with have/need counters, O(n + m)
    public static String minWindowOptimized(String s, String t) {
        if (s == null || t == null || s.length() < t.length() || t.isEmpty()) {
            return "";
        }

        // step 1: build the "need" map -- how many of each character t requires
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) {
            need.merge(c, 1, Integer::sum);
        }
        int required = need.size(); // number of distinct characters we must fully satisfy

        Map<Character, Integer> window = new HashMap<>();
        int formed = 0; // number of distinct characters currently fully satisfied

        int left = 0;
        int bestLen = Integer.MAX_VALUE;
        int bestStart = 0;

        // step 2: expand window with right pointer
        for (int right = 0; right < s.length(); right++) {
            char rightChar = s.charAt(right);
            window.merge(rightChar, 1, Integer::sum);

            // if this character's count now exactly matches what's needed, one more requirement is satisfied
            if (need.containsKey(rightChar) && window.get(rightChar).intValue() == need.get(rightChar).intValue()) {
                formed++;
            }

            // step 3: window is valid, try to shrink from the left
            while (formed == required) {
                // record smaller window if found
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                }

                char leftChar = s.charAt(left);
                window.put(leftChar, window.get(leftChar) - 1);
                // if removing this character breaks the requirement, decrement formed
                if (need.containsKey(leftChar) && window.get(leftChar).intValue() < need.get(leftChar).intValue()) {
                    formed--;
                }
                left++;
            }
        }

        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    public static void main(String[] args) {
        String s1 = "ADOBECODEBANC";
        String t1 = "ABC";
        // Expected: "BANC"
        System.out.println("s = " + s1 + ", t = " + t1);
        System.out.println("Brute force: " + minWindowBruteForce(s1, t1));
        System.out.println("Optimized:   " + minWindowOptimized(s1, t1));

        String s2 = "a";
        String t2 = "aa";
        // Expected: "" (s doesn't have enough 'a's to satisfy t)
        System.out.println("\ns = " + s2 + ", t = " + t2);
        System.out.println("Brute force: \"" + minWindowBruteForce(s2, t2) + "\"");
        System.out.println("Optimized:   \"" + minWindowOptimized(s2, t2) + "\"");

        String s3 = "a";
        String t3 = "a";
        // Expected: "a"
        System.out.println("\ns = " + s3 + ", t = " + t3);
        System.out.println("Brute force: " + minWindowBruteForce(s3, t3));
        System.out.println("Optimized:   " + minWindowOptimized(s3, t3));
    }
}
