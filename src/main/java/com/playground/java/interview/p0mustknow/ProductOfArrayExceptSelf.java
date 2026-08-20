package com.playground.java.interview.p0mustknow;

import java.util.Arrays;

/**
 * PATTERN: Arrays / Prefix-Suffix Products
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Return an array where each element is the product of all other
 * elements except itself, without using division.
 */
public class ProductOfArrayExceptSelf {

    // ================= PROBLEM =================
    // You get a list of numbers. For each position, you need to compute the product
    // of all the other numbers in the list (but not the number at that position itself).
    // You are NOT allowed to use division.
    // Example: nums = [1, 2, 3, 4] -> output = [24, 12, 8, 6]
    // because: 24 = 2*3*4, 12 = 1*3*4, 8 = 1*2*4, 6 = 1*2*3.
    //
    // ================= SIMPLE APPROACH =================
    // For each position i, loop through the entire array again and multiply all
    // numbers except the one at position i.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // For each of the n positions, we do another loop of n steps, giving O(n^2) time.
    // This is too slow for large arrays.
    // The obvious trick of "multiply everything, then divide by nums[i]" is not allowed here
    // (division is banned), and it also breaks when the array contains a zero.
    //
    // ================= OPTIMIZED APPROACH =================
    // Instead of recomputing the product every time, build it up using two passes.
    // First pass (left to right): for each position, compute the product of all numbers
    // to its LEFT, and store it in the result array.
    // Second pass (right to left): keep a running product of all numbers to the RIGHT
    // of the current position, and multiply it into the result array as we go.
    // After both passes, result[i] = (product of everything left of i) * (product of everything right of i),
    // which is exactly what we want, and no division was used.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // We just need a plain array (the output array itself) and a couple of running variables.
    // No hash map or extra lookup structure is needed because the "prefix product" and
    // "suffix product" can be built incrementally in a single forward and single backward pass.
    // This keeps extra space at O(1) (excluding the output array, which the problem requires anyway).
    //
    // ================= EDGE CASES =================
    // - Empty array or single element array: result depends on problem definition (often undefined
    //   or a single 1 for one element, since there's nothing else to multiply).
    // - Array contains exactly one zero: every other position's product becomes 0,
    //   but the position where the zero is should get the product of all non-zero numbers.
    // - Array contains two or more zeros: every position's product becomes 0.
    // - Negative numbers: sign should be handled naturally by normal multiplication.
    // - Large arrays: watch out for integer overflow if numbers are large.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - two linear passes over the array (left-to-right, then right-to-left).
    // Brute force is O(n^2) since it recomputes products for each position.
    // Space Complexity: O(1) extra space (not counting the output array), since we only keep
    // a running suffix product variable. Brute force is technically O(1) extra space too, but far slower.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would your solution change if division WAS allowed? What edge case (zeros) would it break on?
    // - What if the array contains one or more zeros - does your two-pass approach still work correctly?
    // - Can you do this truly in O(1) extra space, not counting the output array? (Yes - this solution does.)
    // - How would you handle potential integer overflow for large inputs?
    // - What if the array is streamed and you cannot do two full passes over it?
    // - How would you parallelize the prefix and suffix product computations?
    // - Can you generalize this to any associative operation, not just multiplication (e.g. XOR)?

    // Brute force: for each index, multiply everything except that index (O(n^2)).
    public static int[] productExceptSelfBruteForce(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            int product = 1;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    product *= nums[j];
                }
            }
            result[i] = product;
        }
        return result;
    }

    // Optimized: prefix product pass + suffix product pass, O(n) time, O(1) extra space.
    public static int[] productExceptSelfOptimized(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];

        // Step 1: fill result[i] with product of all elements to the LEFT of i.
        result[0] = 1; // nothing to the left of the first element.
        for (int i = 1; i < n; i++) {
            result[i] = result[i - 1] * nums[i - 1];
        }

        // Step 2: multiply in the product of all elements to the RIGHT of i,
        // using a running suffix product variable (no extra array needed).
        int suffixProduct = 1;
        for (int i = n - 1; i >= 0; i--) {
            result[i] = result[i] * suffixProduct;
            suffixProduct *= nums[i];
        }

        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3, 4};
        // Expected: [24, 12, 8, 6]
        System.out.println("Input: [1, 2, 3, 4]");
        System.out.println("Brute force output: " + Arrays.toString(productExceptSelfBruteForce(nums1)));
        System.out.println("Optimized output: " + Arrays.toString(productExceptSelfOptimized(nums1)));

        int[] nums2 = {-1, 1, 0, -3, 3};
        // Expected: [0, 0, 9, 0, 0]
        System.out.println("\nInput: [-1, 1, 0, -3, 3] (contains a zero)");
        System.out.println("Optimized output: " + Arrays.toString(productExceptSelfOptimized(nums2)));

        int[] nums3 = {0, 0};
        // Expected: [0, 0] (two zeros -> every product is 0)
        System.out.println("\nInput: [0, 0] (two zeros)");
        System.out.println("Optimized output: " + Arrays.toString(productExceptSelfOptimized(nums3)));
    }
}
