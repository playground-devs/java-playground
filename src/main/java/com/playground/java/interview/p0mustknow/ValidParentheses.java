package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Strings / Stack
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given a string of brackets, check whether every opening bracket has a
 * matching closing bracket in the correct order.
 */
public class ValidParentheses {

    // ================= PROBLEM =================
    // You get a string made only of the characters ( ) { } [ ].
    // You need to check if the brackets are "balanced" - every opening bracket must be
    // closed by the same TYPE of bracket, and they must close in the right order.
    // Example: "()[]{}" -> output = true
    // Example: "(]" -> output = false (wrong closing type)
    // Example: "([)]" -> output = false (wrong order, even though types are used correctly overall)
    //
    // ================= SIMPLE APPROACH =================
    // Repeatedly scan the string for a pattern like "()" or "[]" or "{}" (an opening bracket
    // immediately followed by its matching closing bracket with nothing in between) and remove it.
    // Keep repeating until nothing more can be removed. If the string becomes empty, it's valid.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Repeatedly scanning and shrinking the string is slow - each removal can take O(n) time,
    // and you might need to do this O(n) times, giving O(n^2) overall.
    // It's also awkward to implement (string mutation, re-scanning from the start each time).
    //
    // ================= OPTIMIZED APPROACH =================
    // Walk through the string one character at a time.
    // Whenever you see an OPENING bracket, push it onto a stack (remember it for later).
    // Whenever you see a CLOSING bracket, check the top of the stack:
    //   - If the stack is empty, there's nothing to match this closing bracket with -> invalid.
    //   - If the top of the stack is the matching opening bracket, pop it off (it's satisfied).
    //   - If the top of the stack is a different type of opening bracket, it's a mismatch -> invalid.
    // At the end, if the stack is empty, every opening bracket was matched -> valid.
    // If the stack still has leftover opening brackets, they were never closed -> invalid.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A Stack (Last-In-First-Out) is a perfect fit because brackets must close in the REVERSE
    // order they were opened - the most recently opened bracket must be the next one closed.
    // Pushing and popping are both O(1), so checking the whole string only takes one pass.
    //
    // ================= EDGE CASES =================
    // - Empty string: considered valid (no brackets to mismatch).
    // - String with only opening brackets, e.g. "(((": invalid, stack never empties.
    // - String with only closing brackets, e.g. ")))": invalid, stack is empty when we try to pop.
    // - Odd length string: can never be balanced (quick early check, though not required).
    // - Nested and mixed brackets, e.g. "{[()()]}": valid.
    // - Wrong order, e.g. "([)]": invalid even though counts of each bracket type match.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - we scan the string once, and each push/pop is O(1).
    // Brute force (repeated pattern removal) is O(n^2).
    // Space Complexity: O(n) in the worst case - e.g. a string of all opening brackets
    // pushes every character onto the stack.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you extend this to also handle other paired symbols, like <angle brackets> or quotes?
    // - What if the string also contains other characters (letters, numbers) mixed with brackets?
    // - Can you solve this without using an explicit Stack class (e.g. using recursion or a counter)?
    // - Why does a simple counter (count opens minus closes) NOT work here, unlike single-bracket-type problems?
    // - How would you report WHERE the first invalid bracket is, not just true/false?
    // - What if the string is huge and streamed character by character - can you still validate it online?
    // - How would you generate all valid combinations of n pairs of parentheses (a related but different problem)?

    // Optimized: single pass using a Stack (Deque) to track open brackets.
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }

        // Map each closing bracket to its matching opening bracket.
        Map<Character, Character> closingToOpening = new HashMap<>();
        closingToOpening.put(')', '(');
        closingToOpening.put(']', '[');
        closingToOpening.put('}', '{');

        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                // Opening bracket - remember it.
                stack.push(c);
            } else if (closingToOpening.containsKey(c)) {
                // Closing bracket - must match the most recent opening bracket.
                if (stack.isEmpty() || stack.pop() != closingToOpening.get(c)) {
                    return false;
                }
            }
            // Any other character is ignored for this problem (assume input is only brackets).
        }

        // Valid only if every opening bracket was matched (nothing left on the stack).
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        // Expected: true
        System.out.println("Input: \"()[]{}\"");
        System.out.println("Output: " + isValid("()[]{}"));

        // Expected: false (wrong order)
        System.out.println("\nInput: \"([)]\"");
        System.out.println("Output: " + isValid("([)]"));

        // Expected: true (empty string is trivially valid)
        System.out.println("\nInput: \"\" (empty string)");
        System.out.println("Output: " + isValid(""));

        // Expected: false (unmatched closing bracket)
        System.out.println("\nInput: \"]\"");
        System.out.println("Output: " + isValid("]"));
    }
}
