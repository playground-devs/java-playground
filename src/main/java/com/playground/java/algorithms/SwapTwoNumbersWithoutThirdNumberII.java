package com.example.javaeight.algorithms;

public class SwapTwoNumbersWithoutThirdNumberII {
    public static int[] swap(int a,int b){
        return new int[]{b,a};
    }
    public static void main(String [] args){
        int a =10;
        int b = 20;
        System.out.println("before swapping the numbers::a= "+a+", b = "+b);
        int [] swapped = swap(a, b);
        a = swapped[0];
        b= swapped[1];
        System.out.println("After swapping the numbers::a = "+a+", b = "+b);
    }
}
