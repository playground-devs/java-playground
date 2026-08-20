package com.playground.java.interview.bitmanipulation;

/**
 * PATTERN: Bit Manipulation / Brian Kernighan's Algorithm
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an integer, count the number of set bits (1-bits) it has, also
 * known as its Hamming weight.
 */
public class NumberOf1Bits {

    // ================= PROBLEM =================
    // You are given a 32-bit integer. Count how many bits in its binary representation are 1.
    // Example: n = 11 (binary 00000000000000000000000000001011) -> output = 3 (three 1-bits).
    //
    // ================= SIMPLE APPROACH =================
    // Check each of the 32 bits one at a time. For each bit position i (0 to 31), test whether
    // bit i is set using (n >> i) & 1, and count how many times this is 1.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This always does exactly 32 iterations regardless of how many bits are actually set,
    // which is wasteful when the number has very few 1-bits (e.g. a power of two has only one).
    //
    // ================= OPTIMIZED APPROACH =================
    // Brian Kernighan's algorithm: repeatedly apply n = n & (n - 1), which clears the LOWEST set
    // bit of n in a single operation, and count how many times this can be done before n becomes
    // 0. This loop runs exactly as many times as there are set bits, not 32 times.
    // Why n & (n-1) clears the lowest set bit: subtracting 1 flips all bits from the lowest set
    // bit down to bit 0 (the lowest set bit becomes 0, and all the 0s below it become 1s).
    // ANDing with the original n then clears exactly that lowest set bit and leaves everything
    // above it unchanged.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No data structure is needed - this is pure bit arithmetic. The key insight is that
    // n & (n-1) is a self-contained O(1) operation that removes exactly one set bit per call,
    // so looping until n == 0 counts the set bits in time proportional to the POPULATION COUNT
    // of n, not its bit width.
    //
    // ================= EDGE CASES =================
    // - n = 0: has zero set bits, loop body never executes.
    // - n = 1: has exactly one set bit.
    // - n = -1 (all 32 bits set, as a two's complement representation): Brian Kernighan's loop
    //   still correctly runs 32 times, one per set bit; using an unsigned-shift mindset (or
    //   treating n as a bit pattern rather than a signed value) matters for negative inputs.
    // - Power of two (n = 2^k, exactly one set bit): loop runs exactly once.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(k) where k is the number of set bits (at most 32 for a 32-bit int) -
    // strictly better than the brute force's fixed 32 iterations whenever k < 32.
    // Space Complexity: O(1) - only a counter and the number itself are used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does n & (n-1) work mathematically - can you explain it with a binary example?
    // - How would you count set bits across a huge range of numbers (0 to n) efficiently (see CountingBits)?
    // - How would Java's built-in Integer.bitCount(n) likely be implemented internally (SWAR / parallel bit-counting tricks)?
    // - How would you find the position of the lowest set bit, not just count all set bits?
    // - How would this differ for a 64-bit long instead of a 32-bit int?
    // - Why is Brian Kernighan's approach generally faster in practice than checking all 32 bits, even though both are technically O(32) = O(1) for a fixed-width integer?

    // Brute force: check all 32 bit positions.
    public static int hammingWeightBruteForce(int n) {
        int count = 0;
        for (int i = 0; i < 32; i++) {
            if (((n >> i) & 1) == 1) {
                count++;
            }
        }
        return count;
    }

    // Optimized: Brian Kernighan's algorithm - n & (n-1) clears the lowest set bit each time.
    public static int hammingWeightOptimized(int n) {
        int count = 0;
        while (n != 0) {
            // Step: clear the lowest set bit and count it.
            n = n & (n - 1);
            count++;
        }
        return count;
    }

    public static void main(String[] args) {
        int n1 = 11; // binary: 1011
        // Expected: 3
        System.out.println("Input: 11 (binary 1011)");
        System.out.println("Output: " + hammingWeightOptimized(n1));

        int n2 = 128; // binary: 10000000 (power of two)
        // Expected: 1
        System.out.println("\nInput: 128 (binary 10000000)");
        System.out.println("Output: " + hammingWeightOptimized(n2));

        int n3 = 0;
        // Expected: 0 (no set bits)
        System.out.println("\nInput: 0");
        System.out.println("Output: " + hammingWeightOptimized(n3));

        int n4 = -1; // all 32 bits set
        // Expected: 32
        System.out.println("\nInput: -1 (all 32 bits set)");
        System.out.println("Output: " + hammingWeightOptimized(n4));
    }
}
