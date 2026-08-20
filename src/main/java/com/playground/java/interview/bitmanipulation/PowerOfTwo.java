package com.playground.java.interview.bitmanipulation;

/**
 * PATTERN: Bit Manipulation
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an integer, determine if it is a power of two.
 */
public class PowerOfTwo {

    // ================= PROBLEM =================
    // You are given an integer n. Determine whether it is a power of two (1, 2, 4, 8, 16, ...).
    // Example: n = 16 -> output = true (16 = 2^4). n = 18 -> output = false.
    //
    // ================= SIMPLE APPROACH =================
    // Repeatedly divide n by 2 as long as it is even and greater than 1. If you ever reach
    // exactly 1, it was a power of two; if you hit an odd number greater than 1 first, it isn't.
    // (Alternative brute force: keep multiplying 1 by 2 until it reaches or exceeds n, then
    // check for equality.)
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This takes O(log n) time with a loop and repeated division/multiplication, when the same
    // answer can be determined in O(1) time using a single bitwise property of powers of two.
    //
    // ================= OPTIMIZED APPROACH =================
    // A power of two, in binary, always has EXACTLY ONE set bit (e.g. 8 = 1000, 16 = 10000).
    // Using the same trick as Brian Kernighan's algorithm, n & (n-1) clears the lowest set bit.
    // If n has only one set bit, clearing it results in 0. So: n is a power of two if and only
    // if n > 0 AND (n & (n-1)) == 0. The n > 0 check is required because 0 itself would
    // incorrectly pass (n & (n-1)) == 0 (0 & -1 == 0) despite having no set bits at all, and
    // negative numbers should never count as powers of two.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No data structure needed - this is a single O(1) bitwise arithmetic check. It relies on
    // the structural fact that powers of two are the only positive integers with exactly one
    // set bit in binary.
    //
    // ================= EDGE CASES =================
    // - n = 0: not a power of two (has zero set bits) - explicitly excluded by the n > 0 check.
    // - n = 1: IS a power of two (2^0 = 1), and 1 & 0 == 0, correctly returns true.
    // - Negative numbers: never powers of two - excluded by the n > 0 check (a negative number's
    //   two's complement representation would otherwise have many set bits, but the sign check
    //   catches it first anyway).
    // - Integer.MIN_VALUE: negative, correctly excluded by n > 0 before any bitwise logic runs.
    // - Very large powers of two near Integer.MAX_VALUE (e.g. 2^30): works correctly since the
    //   check only depends on bit patterns, not magnitude comparisons.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(1) - a single bitwise AND and comparison, versus O(log n) for
    // repeated division/multiplication.
    // Space Complexity: O(1) - no extra memory used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you check if a number is a power of THREE or power of FOUR instead (this exact bit trick doesn't directly apply)?
    // - Why does the n > 0 check matter - what specific values would break the check without it?
    // - How is this bit trick related to Brian Kernighan's "count set bits" algorithm?
    // - How would you find the next power of two greater than or equal to a given n?
    // - Can you check "power of two" using n & (-n) == n instead (isolating the lowest set bit)? Why does that also work?
    // - How would this differ for checking powers of two in an unsigned/64-bit context?

    // Optimized: a power of two has exactly one set bit, so n & (n-1) clears it to zero.
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    public static void main(String[] args) {
        int n1 = 16;
        // Expected: true (16 = 2^4)
        System.out.println("Input: 16");
        System.out.println("Output: " + isPowerOfTwo(n1));

        int n2 = 18;
        // Expected: false (18 = 10010 in binary, two set bits)
        System.out.println("\nInput: 18");
        System.out.println("Output: " + isPowerOfTwo(n2));

        int n3 = 0;
        // Expected: false (zero is not a power of two)
        System.out.println("\nInput: 0");
        System.out.println("Output: " + isPowerOfTwo(n3));

        int n4 = -8;
        // Expected: false (negative numbers are never powers of two)
        System.out.println("\nInput: -8 (negative)");
        System.out.println("Output: " + isPowerOfTwo(n4));
    }
}
