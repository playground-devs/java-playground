package com.playground.java.interview.bitmanipulation;

import java.util.Arrays;

/**
 * PATTERN: Bit Manipulation / Dynamic Programming
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an integer n, return an array where each index i (0 to n) holds
 * the number of set bits (1-bits) in the binary representation of i.
 */
public class CountingBits {

    // ================= PROBLEM =================
    // You are given a non-negative integer n. For every number from 0 to n (inclusive), count
    // how many 1-bits it has in binary, and return all these counts as an array.
    // Example: n = 5 -> output = [0,1,1,2,1,2]
    //   0=000->0, 1=001->1, 2=010->1, 3=011->2, 4=100->1, 5=101->2
    //
    // ================= SIMPLE APPROACH =================
    // For each number i from 0 to n, independently count its set bits using a bit-counting
    // routine (e.g. Brian Kernighan's n & (n-1) trick, or checking all bits), and store the
    // result in bits[i].
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Even with Brian Kernighan's O(popcount) trick per number, computing every number's bit
    // count from scratch independently is O(n log n) overall in the worst case, and it ignores
    // the fact that each number's bit count is closely related to a smaller number's bit count
    // that was already computed earlier in the same array.
    //
    // ================= OPTIMIZED APPROACH =================
    // Build the answer with dynamic programming in O(n) total time, using the relation:
    //   bits[i] = bits[i >> 1] + (i & 1)
    // Reasoning: shifting i right by 1 (i >> 1) drops the lowest bit, which is exactly i / 2 -
    // a number whose bit count we've ALREADY computed earlier in the loop (since i >> 1 < i).
    // The dropped lowest bit is either 0 or 1, given by (i & 1). So the total set bits in i is
    // just the set bits in i>>1, plus 1 if i's own lowest bit was set.
    // bits[0] = 0 is the base case; then fill bits[1..n] left to right using the relation.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A simple int array used as a DP table is exactly what's needed: each entry only depends
    // on one earlier, already-computed entry (bits[i >> 1]), so filling the array once, left to
    // right, computes every answer in O(1) additional work per index - no recomputation, no
    // extra bit-counting routine needed at all.
    //
    // ================= EDGE CASES =================
    // - n = 0: output is just [0] (only counting bits of 0 itself).
    // - n = 1: output is [0, 1].
    // - Powers of two (e.g. i = 8 = 1000): bits[8] = bits[4] + 0 = bits[2] + 0 = bits[1] + 0 = 1, correctly cascades down to 1.
    // - Large n: still O(n) time and space, no repeated bit-counting overhead per element.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each of the n+1 entries is computed in O(1) using a previously
    // computed entry, versus O(n log n) for counting each number's bits independently.
    // Space Complexity: O(n) for the output array itself (required by the problem; no extra
    // auxiliary space beyond that).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you derive an alternative DP relation using i & (i-1) instead of i >> 1 (bits[i] = bits[i & (i-1)] + 1)? How does it differ conceptually?
    // - Why is bits[i >> 1] guaranteed to already be computed when processing i, in a simple left-to-right loop?
    // - How would you extend this to count bits for numbers in an arbitrary range [low, high], not just [0, n]?
    // - How does this relate to Brian Kernighan's algorithm used in NumberOf1Bits?
    // - Could you compute this in O(n) time using SWAR (SIMD Within A Register) parallel bit-counting instead of DP? What's the tradeoff?
    // - How would the DP table change if you needed bit counts for 64-bit longs instead of ints?

    // Optimized: O(n) DP using bits[i] = bits[i >> 1] + (i & 1).
    public static int[] countBits(int n) {
        int[] bits = new int[n + 1];
        // bits[0] = 0 is the base case (already correct via array default initialization).
        for (int i = 1; i <= n; i++) {
            // Step: reuse the already-computed answer for i >> 1, plus i's own lowest bit.
            bits[i] = bits[i >> 1] + (i & 1);
        }
        return bits;
    }

    public static void main(String[] args) {
        int n1 = 5;
        // Expected: [0, 1, 1, 2, 1, 2]
        System.out.println("Input: n=5");
        System.out.println("Output: " + Arrays.toString(countBits(n1)));

        int n2 = 8;
        // Expected: [0, 1, 1, 2, 1, 2, 2, 3, 1]
        System.out.println("\nInput: n=8");
        System.out.println("Output: " + Arrays.toString(countBits(n2)));

        int n3 = 0;
        // Expected: [0] (only counting bits of 0)
        System.out.println("\nInput: n=0");
        System.out.println("Output: " + Arrays.toString(countBits(n3)));
    }
}
