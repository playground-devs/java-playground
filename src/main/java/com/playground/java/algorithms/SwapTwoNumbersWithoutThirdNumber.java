package com.example.javaeight.algorithms;

public class SwapTwoNumbersWithoutThirdNumber {
    public static void main(String[] args) {
        int a = 10;
        int b = 20;

        System.out.println("Before swapping: a = " + a + ", b = " + b);

        // Swapping without using a third variable
        a = a + b; // Now a is 30
        b = a - b; // Now b is 10 (original value of a)
        a = a - b; // Now a is 20 (original value of b)

        System.out.println("After swapping: a = " + a + ", b = " + b);
    }
}
