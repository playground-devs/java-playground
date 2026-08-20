package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Stack (Design)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Design a stack that supports push, pop, top, and retrieving the minimum element, all in O(1) time.
 */
public class MinStack {

    // ================= PROBLEM =================
    // Implement a stack with the usual push/pop/top operations, plus a getMin() operation
    // that returns the current minimum element in the stack - and ALL operations must be O(1).
    // Example:
    //   push(5) -> stack: [5],        min = 5
    //   push(2) -> stack: [5,2],      min = 2
    //   push(7) -> stack: [5,2,7],    min = 2
    //   getMin() -> 2
    //   pop()    -> removes 7,        stack: [5,2], min = 2
    //   pop()    -> removes 2,        stack: [5],   min = 5
    //   getMin() -> 5
    //
    // ================= SIMPLE APPROACH =================
    // A regular stack only tracks the top element - it has no idea what the minimum is
    // without scanning everything. The simplest fix: store PAIRS (value, currentMinAtThisPoint)
    // instead of just values. Every push computes and stores "min so far including this value".
    // getMin() then just peeks at the top pair's second element.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works and is technically O(1) for all operations, but it roughly DOUBLES the memory
    // used per element (storing two ints instead of one for every single push), even though
    // most of the time the minimum does not change between pushes. It's wasteful when pushes
    // of non-minimum values happen far more often than new minimums.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use two stacks:
    //   1) mainStack - stores every pushed value, exactly like a normal stack.
    //   2) minStack  - only stores a new value when it is LESS THAN OR EQUAL TO the current
    //      minimum (the top of minStack). It mirrors "checkpoints" of what the minimum was
    //      at each point, but only grows when a new minimum (or a tie) actually occurs.
    // On push(x): push x onto mainStack. If minStack is empty or x <= minStack.peek(), also
    // push x onto minStack (a new minimum "checkpoint").
    // On pop(): pop mainStack. If the popped value equals minStack.peek(), also pop minStack
    // (that minimum checkpoint no longer applies since its value is leaving the stack).
    // On getMin(): simply peek minStack - its top is always the current overall minimum.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Two stacks work because a stack (LIFO) naturally mirrors the "undo" behavior we need:
    // whatever was the minimum before the current top existed is still sitting right
    // underneath it in minStack, ready to be exposed the instant we pop back past it.
    // mainStack alone can give O(1) push/pop/top for VALUES, but has zero info about
    // "what was the min before". minStack alone (without mainStack) has no way to know the
    // FULL sequence of values (since it skips non-minimum pushes), so we still need mainStack
    // for correct pop()/top() behavior. Together: mainStack guarantees O(1) push/pop/top on
    // the real values, and minStack guarantees O(1) getMin() by keeping only the minimum
    // "history" instead of the full history, added/removed in perfect lockstep with mainStack's
    // pops - this synchronization is exactly what makes both operations O(1) without recomputation.
    // (Using <= rather than < when pushing to minStack correctly handles duplicate minimum
    // values, e.g. pushing 2, 2 - both need a checkpoint so the min stays correct after one pops.)
    //
    // ================= EDGE CASES =================
    // - getMin()/pop()/top() called on an empty stack: should throw or be guarded against.
    // - Pushing the same minimum value multiple times, e.g. push(1), push(1): minStack must
    //   push for BOTH so that popping one 1 still leaves min = 1 correctly (hence using <=, not <).
    // - Pushing values in strictly increasing order: minStack should only ever contain the
    //   very first (smallest) value, since nothing later is <= it.
    // - Pushing values in strictly decreasing order: minStack grows in lockstep with mainStack.
    // - Single element pushed then popped: stack and minStack both empty afterward.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(1) for push, pop, top, and getMin - each performs a constant number
    // of stack operations (peek/push/pop), regardless of stack size.
    // Space Complexity: O(n) worst case for both approaches - the pair approach always uses
    // 2n space; the two-stack approach uses n (mainStack) + up to n (minStack in the worst
    // case of strictly decreasing pushes), so still O(n) but typically much less in practice.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must minStack use <= instead of < when deciding to push a new checkpoint?
    // - Can you do this with a single stack by encoding both value and min together (e.g.
    //   storing an encoded value using math tricks) to save space? What are the risks (overflow)?
    // - How would you extend this to also track the current MAXIMUM in O(1)?
    // - What if you needed getMin() over just the last k elements, not the whole stack?
    // - How would you make this thread-safe for concurrent push/pop from multiple threads?
    // - Why is ArrayDeque preferred over java.util.Stack in modern Java code?
    // - How does this pattern generalize to a "min queue" using two stacks or a deque?

    // Simple approach: store (value, minSoFar) pairs, doubling memory per element.
    public static class SimpleMinStack {
        private final Deque<int[]> stack = new ArrayDeque<>(); // each entry: [value, minSoFar]

        public void push(int val) {
            int currentMin = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
            stack.push(new int[]{val, currentMin});
        }

        public void pop() {
            stack.pop();
        }

        public int top() {
            return stack.peek()[0];
        }

        public int getMin() {
            return stack.peek()[1];
        }
    }

    // Optimized approach: two stacks, minStack only grows on new minimums (or ties).
    private final Deque<Integer> mainStack = new ArrayDeque<>();
    private final Deque<Integer> minStack = new ArrayDeque<>();

    public void push(int val) {
        mainStack.push(val);
        // Push to minStack if it's empty or val ties/beats the current minimum.
        if (minStack.isEmpty() || val <= minStack.peek()) {
            minStack.push(val);
        }
    }

    public void pop() {
        int popped = mainStack.pop();
        // If the popped value was the current minimum, retire that checkpoint too.
        if (!minStack.isEmpty() && popped == minStack.peek()) {
            minStack.pop();
        }
    }

    public int top() {
        return mainStack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }

    public static void main(String[] args) {
        System.out.println("Optimized MinStack (two-stack approach):");
        MinStack minStack1 = new MinStack();
        minStack1.push(5);
        minStack1.push(2);
        minStack1.push(7);
        // Expected: 2
        System.out.println("getMin() after push(5,2,7) = " + minStack1.getMin() + " (expected 2)");
        minStack1.pop(); // removes 7
        // Expected: 2
        System.out.println("getMin() after pop() = " + minStack1.getMin() + " (expected 2)");
        // Expected: 2
        System.out.println("top() = " + minStack1.top() + " (expected 2)");
        minStack1.pop(); // removes 2
        // Expected: 5
        System.out.println("getMin() after pop() = " + minStack1.getMin() + " (expected 5)");

        System.out.println("\nEdge case: duplicate minimums, push(1), push(1)");
        MinStack minStack2 = new MinStack();
        minStack2.push(1);
        minStack2.push(1);
        minStack2.pop();
        // Expected: 1 (one 1 still remains)
        System.out.println("getMin() after popping one 1 = " + minStack2.getMin() + " (expected 1)");

        System.out.println("\nSimpleMinStack (pair-based approach) for comparison:");
        SimpleMinStack simple = new SimpleMinStack();
        simple.push(5);
        simple.push(2);
        simple.push(7);
        // Expected: 2
        System.out.println("getMin() after push(5,2,7) = " + simple.getMin() + " (expected 2)");
    }
}
