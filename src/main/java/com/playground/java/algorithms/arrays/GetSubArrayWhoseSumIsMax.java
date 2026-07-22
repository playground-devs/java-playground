package com.playground.java.algorithms.arrays;
import java.util.*;


public class GetSubArrayWhoseSumIsMax {
    public static void main(String[] args) {
        Integer[] arr = {2, 3, -8, 7, -1, 2, 3};

        int maxSoFar = arr[0];
        int maxEndingHere = arr[0];

        int start = 0, end = 0, tempStart = 0;

        for (int i = 1; i < arr.length; i++) {
            if (maxEndingHere + arr[i] < arr[i]) {
                maxEndingHere = arr[i];
                tempStart = i;
            } else {
                maxEndingHere += arr[i];
            }

            if (maxEndingHere > maxSoFar) {
                maxSoFar = maxEndingHere;
                start = tempStart;
                end = i;
            }
        }

        // Print the maximum subarray
        System.out.print("Maximum Subarray: [");
        for (int i = start; i <= end; i++) {
            System.out.print(arr[i] + (i < end ? ", " : ""));
        }
        System.out.println("]");

        System.out.println("Maximum Sum: " + maxSoFar);
    }

}
