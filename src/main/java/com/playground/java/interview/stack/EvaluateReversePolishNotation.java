package com.playground.java.interview.stack;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Stack
 * PRIORITY: P1
 * PROBLEM STATEMENT: Evaluate an arithmetic expression given in Reverse Polish (postfix) Notation.
 */
public class EvaluateReversePolishNotation {

    // ================= PROBLEM =================
    // Given an array of tokens representing an arithmetic expression in Reverse Polish
    // Notation (postfix), evaluate it and return the integer result. Valid operators are
    // +, -, *, /. Each operand may be an integer (possibly negative).
    // Example: ["2","1","+","3","*"] -> (2+1)*3 = 9
    // Example: ["4","13","5","/","+"] -> 4 + (13/5) = 4 + 2 = 6
    //
    // ================= SIMPLE APPROACH =================
    // Without using an explicit stack, you could repeatedly scan the token list looking for
    // the first "num num op" pattern, compute it, and splice the result back into the list
    // in place of those three tokens, then rescan from the beginning. Repeat until only one
    // token remains.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Every "find pattern, splice, rescan" cycle costs O(n) just to find the pattern plus
    // O(n) to rebuild the list, and this happens roughly n/2 times (once per operator), so
    // the total cost is O(n^2). It's also fiddly to implement correctly (list mutation while
    // scanning) compared to the natural stack-based approach, since postfix notation is
    // literally designed to be evaluated with a stack in a single left-to-right pass.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a stack (Deque<Integer>) and walk the tokens left to right.
    // Step 1: If the token is a number, parse it and push it onto the stack.
    // Step 2: If the token is an operator (+, -, *, /), pop the top two values off the stack.
    //          The value popped FIRST is the right-hand operand (b), and the value popped
    //          SECOND is the left-hand operand (a), because the stack is LIFO and "a b op"
    //          means a came before b in the original expression.
    // Step 3: Apply the operator as (a op b) and push the result back onto the stack.
    // Step 4: After processing all tokens, exactly one value remains on the stack - that is
    //          the final answer.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A stack is the natural fit because postfix notation defers each operator until both of
    // its operands have already been seen, and the two most recently seen values not yet
    // consumed are always the correct operands for the next operator - which is exactly
    // LIFO (last-in-first-out) behavior. No other structure (queue, list with random access)
    // models "most recently produced value gets used next" as directly or efficiently.
    //
    // ================= EDGE CASES =================
    // - Division truncates toward zero (Java's integer division already does this, e.g. -7/2 = -3, matching typical problem specs).
    // - Negative operands, e.g. "-3", "4", "+" -> 1. Must parse the "-" as part of the number, not as an operator token.
    // - Single-number expression with no operators, e.g. ["5"] -> 5.
    // - Operator order matters for non-commutative ops (subtraction, division): must pop in the correct a, b order.
    // - Division by zero: not typically expected in valid input, but worth mentioning to an interviewer.
    // - Large expressions: intermediate results could overflow int in extreme cases (worth mentioning long as an alternative).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) where n is the number of tokens - each token is pushed and/or
    // popped from the stack at most once.
    // Space Complexity: O(n) worst case for the stack, e.g. an expression that is almost all
    // numbers before a single trailing operator would push many values before any pops happen.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you evaluate an expression given in standard infix notation instead (e.g. using Shunting Yard to convert to postfix first)?
    // - How would you support additional operators like exponentiation (^) or modulo (%)?
    // - What if operands could be floating point numbers instead of integers?
    // - How would you validate that the input is a well-formed RPN expression before evaluating it?
    // - Why does postfix notation avoid the need for parentheses or operator precedence rules entirely?
    // - How would you convert this postfix expression back into an infix expression with correct parenthesization?
    // - What happens if the stack has more than one value left at the end - what does that indicate about the input?

    public static int evalRPN(String[] tokens) {
        Deque<Integer> stack = new ArrayDeque<>();

        for (String token : tokens) {
            if (isOperator(token)) {
                // Order matters: b was pushed most recently, so it's popped first (right operand).
                int b = stack.pop();
                int a = stack.pop();
                stack.push(applyOperator(a, b, token));
            } else {
                // Token is a number (may include a leading '-' sign for negatives).
                stack.push(Integer.parseInt(token));
            }
        }

        return stack.pop();
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private static int applyOperator(int a, int b, String operator) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                return a / b; // Java truncates toward zero, matching typical RPN problem specs
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    public static void main(String[] args) {
        String[] tokens1 = {"2", "1", "+", "3", "*"};
        // Expected: 9 ((2+1)*3)
        System.out.println("Input: " + java.util.Arrays.toString(tokens1));
        System.out.println("Result: " + evalRPN(tokens1));

        String[] tokens2 = {"4", "13", "5", "/", "+"};
        // Expected: 6 (4 + (13/5) = 4 + 2)
        System.out.println("\nInput: " + java.util.Arrays.toString(tokens2));
        System.out.println("Result: " + evalRPN(tokens2));

        String[] tokens3 = {"-3", "4", "+"};
        // Expected: 1 (-3 + 4), tests negative operand parsing
        System.out.println("\nInput: " + java.util.Arrays.toString(tokens3));
        System.out.println("Result: " + evalRPN(tokens3));

        String[] tokens4 = {"5"};
        // Expected: 5 (single number, no operators)
        System.out.println("\nInput: " + java.util.Arrays.toString(tokens4));
        System.out.println("Result: " + evalRPN(tokens4));
    }
}
