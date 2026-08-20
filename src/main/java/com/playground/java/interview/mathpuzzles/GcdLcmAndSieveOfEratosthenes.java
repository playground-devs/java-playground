package com.playground.java.interview.mathpuzzles;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Math / Number Theory Basics
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Implement three classic "basic math" warm-up algorithms commonly
 * asked together: GCD (Euclidean algorithm), LCM (using GCD), and the Sieve of
 * Eratosthenes to find all prime numbers up to n.
 */
public class GcdLcmAndSieveOfEratosthenes {

    // ================= PROBLEM =================
    // Three related, commonly-asked "warm-up" math problems:
    // 1. GCD: find the Greatest Common Divisor of two numbers.
    //    Example: gcd(12, 18) -> output = 6
    // 2. LCM: find the Least Common Multiple of two numbers.
    //    Example: lcm(4, 6) -> output = 12
    // 3. Sieve of Eratosthenes: find all prime numbers up to (and including) n.
    //    Example: primesUpTo(10) -> output = [2, 3, 5, 7]
    //
    // ================= SIMPLE APPROACH =================
    // GCD (brute force): try every number from min(a,b) down to 1, and return the
    // first one that divides both a and b evenly.
    // LCM (brute force): keep incrementing a multiple of the larger number until it's
    // also divisible by the smaller number.
    // Primes (brute force): for every number from 2 to n, check if it's prime by
    // testing divisibility against every number from 2 up to itself minus one.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Brute force GCD is O(min(a,b)) time - slow for very large numbers.
    // Brute force LCM depends on brute force GCD or repeated incrementing, which can
    // be slow, especially if the LCM itself is large.
    // Brute force primality testing for each number up to n is roughly O(n * sqrt(n))
    // or worse, which becomes slow when n is large (e.g., checking primes up to a million).
    //
    // ================= OPTIMIZED APPROACH =================
    // GCD: use the Euclidean algorithm - repeatedly replace (a, b) with (b, a % b)
    // until b becomes 0; at that point a is the GCD. This shrinks the numbers
    // exponentially fast (related to the Fibonacci sequence in the worst case).
    // LCM: use the identity lcm(a, b) = (a * b) / gcd(a, b). Since we can compute
    // GCD fast, LCM becomes fast too. Divide before multiplying (or use long) to
    // avoid overflow when a*b could be large.
    // Sieve of Eratosthenes: create a boolean array marking numbers as prime,
    // starting all as true (except 0 and 1). Starting from 2, for every number that
    // is still marked prime, mark all of its multiples as not prime. This way, every
    // composite number gets "crossed off" using its smallest prime factor, and the
    // total work across the whole sieve is much less than checking each number individually.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // GCD/LCM need no extra data structure - just simple arithmetic with a loop
    // (Euclidean algorithm) or recursion. The Sieve of Eratosthenes uses a boolean
    // array indexed by number value, which gives O(1) access to "is this number
    // prime" and lets us efficiently cross off multiples in bulk rather than
    // testing each number's primality independently from scratch.
    //
    // ================= EDGE CASES =================
    // - gcd(0, n): should return n (gcd with zero is the other number).
    // - gcd/lcm with negative numbers: typically defined using absolute values.
    // - lcm calculation risking integer overflow for large inputs - use long arithmetic.
    // - Sieve with n less than 2: no primes exist, return an empty list.
    // - Sieve with n = 2: the only prime is 2 itself.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: GCD via Euclidean algorithm is O(log(min(a,b))). LCM is
    // O(log(min(a,b))) since it relies on GCD. Sieve of Eratosthenes is
    // O(n log log n), much faster than checking each number individually (O(n*sqrt(n))).
    // Space Complexity: GCD and LCM use O(1) extra space (or O(log(min(a,b))) stack
    // space if implemented recursively). Sieve of Eratosthenes uses O(n) space for
    // the boolean array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you prove why the Euclidean algorithm terminates and why gcd(a,b) == gcd(b, a % b)?
    // - Why do we divide before multiplying in lcm(a,b) = a / gcd(a,b) * b (to avoid overflow)?
    // - Why does the Sieve of Eratosthenes only need to start crossing off multiples of a prime p starting from p*p (not 2*p)?
    // - How would you find the GCD/LCM of an entire array of numbers, not just two?
    // - How would you generate a segmented sieve if n was too large to fit a full boolean array in memory?
    // - What's the extended Euclidean algorithm used for, and how does it relate to modular inverses?
    // - How would you check if a single large number is prime efficiently without building a full sieve (trial division up to sqrt(n))?

    // ---------- GCD ----------

    // Brute force GCD: try every divisor from min(a,b) down to 1. O(min(a,b)).
    public static int gcdBruteForce(int a, int b) {
        int smaller = Math.min(a, b);
        for (int candidate = smaller; candidate >= 1; candidate--) {
            if (a % candidate == 0 && b % candidate == 0) {
                return candidate;
            }
        }
        return 1;
    }

    // Optimized GCD: Euclidean algorithm. O(log(min(a,b))).
    public static int gcdEuclidean(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // ---------- LCM ----------

    // Optimized LCM: uses gcd, divides before multiplying to reduce overflow risk. O(log(min(a,b))).
    public static long lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int gcdValue = gcdEuclidean(a, b);
        // Divide first, then multiply, to keep intermediate values smaller.
        return ((long) (a / gcdValue)) * b;
    }

    // ---------- Sieve of Eratosthenes ----------

    // Brute force primality check for every number up to n. O(n * sqrt(n)).
    public static List<Integer> primesUpToBruteForce(int n) {
        List<Integer> primes = new ArrayList<>();
        for (int candidate = 2; candidate <= n; candidate++) {
            if (isPrimeBruteForce(candidate)) {
                primes.add(candidate);
            }
        }
        return primes;
    }

    private static boolean isPrimeBruteForce(int number) {
        for (int divisor = 2; divisor * divisor <= number; divisor++) {
            if (number % divisor == 0) {
                return false;
            }
        }
        return number > 1;
    }

    // Optimized: Sieve of Eratosthenes. O(n log log n) time, O(n) space.
    public static List<Integer> primesUpToSieve(int n) {
        List<Integer> primes = new ArrayList<>();
        if (n < 2) {
            return primes;
        }
        boolean[] isComposite = new boolean[n + 1];

        for (int p = 2; (long) p * p <= n; p++) {
            if (!isComposite[p]) {
                // Cross off all multiples of p, starting at p*p (smaller multiples
                // were already crossed off by smaller primes).
                for (int multiple = p * p; multiple <= n; multiple += p) {
                    isComposite[multiple] = true;
                }
            }
        }

        for (int number = 2; number <= n; number++) {
            if (!isComposite[number]) {
                primes.add(number);
            }
        }
        return primes;
    }

    public static void main(String[] args) {
        // Expected: 6
        System.out.println("Input: gcd(12, 18)");
        System.out.println("Brute force output: " + gcdBruteForce(12, 18));
        System.out.println("Euclidean output: " + gcdEuclidean(12, 18));

        // Expected: 12
        System.out.println("\nInput: lcm(4, 6)");
        System.out.println("Optimized output: " + lcm(4, 6));

        // Expected: [2, 3, 5, 7]
        System.out.println("\nInput: primesUpTo(10)");
        System.out.println("Brute force output: " + primesUpToBruteForce(10));
        System.out.println("Sieve output: " + primesUpToSieve(10));

        // Expected: gcd(0, 5) = 5; primesUpTo(1) = [] (edge cases)
        System.out.println("\nInput: gcd(0, 5) and primesUpTo(1) (edge cases)");
        System.out.println("gcd(0, 5) output: " + gcdEuclidean(0, 5));
        System.out.println("primesUpTo(1) output: " + primesUpToSieve(1));
    }
}
