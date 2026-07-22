package com.playground.java.algorithms.strings;

import java.util.Stack;

public class ParenthesesValidator {
    public static void main(String[] args){
        String s1 = "()()";
        String s2 = "()(";
        Stack<Character> stack = new Stack<>();
        isBalanced(s1, stack);

    }

    private static boolean isBalanced(String s1, Stack<Character> stack) {
        for(Character c: s1.toCharArray()){
            if(c == '('){
                stack.push(c);
            } else if (c == ')') {
                if(stack.isEmpty()){
                    return false;
                }
                stack.pop();
            }
        }
        return stack.isEmpty();
    }
}
