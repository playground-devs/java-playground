package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Backtracking
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given n pairs of parentheses, generate all combinations of well-formed
 * (valid) parentheses strings.
 */
public class GenerateParentheses {

    // ================= PROBLEM =================
    // You are given a number n representing pairs of parentheses.
    // You must generate every string of length 2n made of '(' and ')' that is "well-formed"
    // - meaning every '(' has a matching ')' and at no point do we have more ')' than '('
    // read so far.
    // Example: n = 3
    // Output -> "((()))", "(()())", "(())()", "()(())", "()()()"
    //
    // ================= SIMPLE APPROACH =================
    // The brute-force full enumeration idea: generate ALL possible strings of length 2n
    // using only '(' and ')' (that is 2^(2n) total strings, since each of the 2n positions
    // has 2 choices), then, for each generated string, run a separate validity check
    // (e.g. using a counter that must never go negative and must end at zero) and keep
    // only the ones that pass. This wastes enormous effort building strings that could
    // have been discarded early (e.g. ")))(((" is invalid from the very first character,
    // but brute force still builds the whole thing before checking).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Not applicable in the DP "overlapping subproblems" sense - this is a backtracking
    // problem. The issue with the brute-force approach is wasted work: 2^(2n) candidate
    // strings are generated when the number of valid strings is only the n-th Catalan
    // number (much smaller, e.g. for n=3 there are 2^6=64 candidate strings but only 5
    // are valid). Most branches become invalid very early (as soon as close-count
    // exceeds open-count) but brute force doesn't notice until the whole string is built.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use backtracking with pruning: track openCount and closeCount used so far in the
    // current path, and only take a step if it can still lead to a valid string:
    //   1. If openCount < n: we are allowed to add '(' (we haven't used all opens yet).
    //   2. If closeCount < openCount: we are allowed to add ')' (there is an unmatched
    //      '(' to close - adding ')' here can never make the string invalid).
    //   3. If openCount == n && closeCount == n: we have a complete valid string of
    //      length 2n - record it.
    // This prunes invalid branches immediately instead of discovering them at the end,
    // because we never even attempt the closing bracket count exceeding the opening bracket count.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Recursion + a mutable "current path" (implemented here as a StringBuilder, the
    // string-building equivalent of a list) models "append one character, recurse to
    // decide the next character, then remove that character to try the other option"
    // - the standard choose/explore/un-choose backtracking template.
    // We use a mutable, reusable StringBuilder rather than creating a new String at every
    // recursive call because String concatenation creates a brand-new object every time
    // (O(length) copy), while StringBuilder's append() and deleteCharAt() (the "backtrack"
    // step) are O(1) amortized and reuse the same buffer across the entire recursion tree.
    //
    // ================= EDGE CASES =================
    // - n = 0: exactly one valid result - the empty string "".
    // - n = 1: exactly one valid result - "()".
    // - Larger n: the count of valid strings grows as the Catalan number C(n),
    //   which grows quickly but far slower than 2^(2n).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(4^n / sqrt(n)) which is the n-th Catalan number (the exact count
    // of valid strings), since backtracking with pruning only explores paths that can
    // still become valid - each valid string also takes O(n) to build/copy, but the
    // Catalan number growth dominates the asymptotic bound typically quoted for this problem.
    // Space Complexity: O(n) for the recursion depth and the StringBuilder buffer
    // (not counting the space needed to store all output strings).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you validate an already-given parentheses string in O(n) time (a
    //   related but different problem)?
    // - How would you extend this to multiple bracket types - (), {}, []?
    // - Why is checking "closeCount < openCount" sufficient to guarantee validity,
    //   rather than checking the whole string so far?
    // - What is a Catalan number, and why does it describe the count of valid outputs here?
    // - How would you generate just the k-th valid combination without generating all of them?
    // - Could you solve this with dynamic programming instead of backtracking? How?
    // - Why do we use StringBuilder with deleteCharAt() instead of String concatenation?

    // Backtracking with pruning: track openCount and closeCount to only build valid paths.
    public static List<String> generateParenthesis(int n) {
        List<String> results = new ArrayList<>();
        StringBuilder currentPath = new StringBuilder();
        backtrack(currentPath, 0, 0, n, results);
        return results;
    }

    private static void backtrack(StringBuilder currentPath, int openCount, int closeCount,
                                   int n, List<String> results) {
        // Step: a complete, valid string has used exactly n opens and n closes.
        if (currentPath.length() == 2 * n) {
            results.add(currentPath.toString());
            return;
        }

        // Choose '(' only if we still have opens left to use.
        if (openCount < n) {
            currentPath.append('(');
            backtrack(currentPath, openCount + 1, closeCount, n, results);
            // Backtrack: remove the '(' we just tried, to try the other branch.
            currentPath.deleteCharAt(currentPath.length() - 1);
        }

        // Choose ')' only if it would still match an unmatched '(' (pruning invalid paths).
        if (closeCount < openCount) {
            currentPath.append(')');
            backtrack(currentPath, openCount, closeCount + 1, n, results);
            // Backtrack: remove the ')' we just tried, to let the caller try other options.
            currentPath.deleteCharAt(currentPath.length() - 1);
        }
    }

    // Brute force (for comparison only): generate all 2^(2n) strings, filter valid ones.
    public static List<String> generateParenthesisBruteForce(int n) {
        List<String> results = new ArrayList<>();
        char[] candidate = new char[2 * n];
        buildAllStrings(candidate, 0, results);
        return results;
    }

    private static void buildAllStrings(char[] candidate, int index, List<String> results) {
        if (index == candidate.length) {
            if (isValid(candidate)) {
                results.add(new String(candidate));
            }
            return;
        }
        candidate[index] = '(';
        buildAllStrings(candidate, index + 1, results);
        candidate[index] = ')';
        buildAllStrings(candidate, index + 1, results);
    }

    private static boolean isValid(char[] candidate) {
        int balance = 0;
        for (char c : candidate) {
            balance += (c == '(') ? 1 : -1;
            if (balance < 0) {
                return false; // more closes than opens so far
            }
        }
        return balance == 0;
    }

    public static void main(String[] args) {
        int n1 = 3;
        // Expected: ["((()))","(()())","(())()","()(())","()()()"]
        System.out.println("Input: n=3");
        System.out.println("Backtracking (pruned): " + generateParenthesis(n1));
        System.out.println("Brute force (filtered): " + generateParenthesisBruteForce(n1));

        int n2 = 1;
        // Expected: ["()"]
        System.out.println("\nInput: n=1");
        System.out.println("Backtracking (pruned): " + generateParenthesis(n2));

        int n3 = 0;
        // Expected: [""]
        System.out.println("\nInput: n=0 (edge case)");
        System.out.println("Backtracking (pruned): " + generateParenthesis(n3));
    }
}
