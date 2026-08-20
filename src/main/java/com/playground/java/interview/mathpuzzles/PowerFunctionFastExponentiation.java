package com.playground.java.interview.mathpuzzles;

/**
 * PATTERN: Math / Exponentiation by Squaring
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Implement pow(x, n) that calculates x raised to the power n,
 * handling both positive and negative exponents efficiently.
 */
public class PowerFunctionFastExponentiation {

    // ================= PROBLEM =================
    // You get a base number x (a double) and an integer exponent n (can be negative).
    // You need to compute x raised to the power n.
    // Example: pow(2.0, 10) -> output = 1024.0
    // Example: pow(2.0, -2) -> output = 0.25   (2^-2 = 1 / 2^2 = 1/4)
    //
    // ================= SIMPLE APPROACH =================
    // Multiply x by itself n times in a loop (for positive n).
    // For negative n, compute the positive-power result first, then take its
    // reciprocal (1 / result).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This takes O(n) time because it does one multiplication per unit of the
    // exponent. For very large exponents (e.g., n = 1,000,000,000), this is far
    // too slow.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use fast exponentiation (exponentiation by squaring). The key idea:
    // x^n can be broken down using the fact that x^n = (x^(n/2))^2 when n is even,
    // and x^n = x * (x^(n/2))^2 when n is odd (integer division).
    // This halves the exponent at every step instead of subtracting 1, so the
    // number of multiplications grows logarithmically with n instead of linearly.
    // For negative n, first convert to the positive case by computing x^(-n) and
    // then taking the reciprocal, being careful with the special case n = Integer.MIN_VALUE
    // (whose absolute value doesn't fit in an int) by using a long for the exponent.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just a loop or recursion that repeatedly
    // squares the base and halves the exponent. This "divide the exponent in half
    // each time" trick is what turns O(n) multiplications into O(log n) multiplications.
    //
    // ================= EDGE CASES =================
    // - n = 0: any x^0 is 1 (even for x = 0, by convention in this problem).
    // - n is negative: result is the reciprocal of x^(-n).
    // - n = Integer.MIN_VALUE: -n overflows a 32-bit int, must widen to long before negating.
    // - x = 0 with positive n: result is 0.
    // - x = 1 or x = -1: result cycles predictably (useful for a quick sanity check).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(log n) for the optimized fast exponentiation - the exponent
    // is halved at every step. Brute force is O(n) - one multiplication per unit of exponent.
    // Space Complexity: O(1) for the iterative optimized version. O(log n) for a
    // recursive version due to call stack depth. Brute force is O(1) extra space.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does squaring the base and halving the exponent still give the correct result?
    // - How do you correctly handle Integer.MIN_VALUE as the exponent without overflow?
    // - Can you implement this both iteratively and recursively - what are the trade-offs (stack space vs. clarity)?
    // - How would you compute this using modular exponentiation (x^n mod m), common in cryptography?
    // - What if x and n were both very large (BigInteger/BigDecimal territory) - how would your approach change?
    // - How would floating-point precision errors affect repeated squaring for non-integer bases?
    // - Can you extend this to compute matrix exponentiation (used for fast Fibonacci computation)?

    // Brute force: multiply x by itself n times. O(n) time.
    public static double powBruteForce(double x, int n) {
        if (n == 0) {
            return 1.0;
        }
        long absN = Math.abs((long) n);
        double result = 1.0;
        for (long i = 0; i < absN; i++) {
            result *= x;
        }
        return n < 0 ? 1.0 / result : result;
    }

    // Optimized: fast exponentiation by squaring. O(log n) time.
    public static double powOptimized(double x, int n) {
        // Use long to safely negate n even when n == Integer.MIN_VALUE.
        long exponent = n;
        if (exponent < 0) {
            x = 1 / x;
            exponent = -exponent;
        }
        return fastPow(x, exponent);
    }

    private static double fastPow(double x, long n) {
        if (n == 0) {
            return 1.0;
        }
        // Recursively compute the result for half the exponent.
        double half = fastPow(x, n / 2);
        if (n % 2 == 0) {
            // Even exponent: x^n = (x^(n/2))^2
            return half * half;
        } else {
            // Odd exponent: x^n = x * (x^(n/2))^2
            return half * half * x;
        }
    }

    public static void main(String[] args) {
        // Expected: 1024.0
        System.out.println("Input: pow(2.0, 10)");
        System.out.println("Brute force output: " + powBruteForce(2.0, 10));
        System.out.println("Optimized output: " + powOptimized(2.0, 10));

        // Expected: 0.25
        System.out.println("\nInput: pow(2.0, -2) (negative exponent)");
        System.out.println("Optimized output: " + powOptimized(2.0, -2));

        // Expected: 1.0 (n = 0 edge case)
        System.out.println("\nInput: pow(5.0, 0) (zero exponent)");
        System.out.println("Optimized output: " + powOptimized(5.0, 0));
    }
}
