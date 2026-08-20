package com.playground.java.interview.binarysearch;

import java.util.Arrays;

/**
 * PATTERN: Binary Search on the Answer
 * PRIORITY: P2
 * ONE-LINE PROBLEM STATEMENT: Find the minimum eating speed that lets Koko finish all banana piles within h hours.
 */
public class KokoEatingBananas {

    // ================= PROBLEM =================
    // Koko has piles of bananas (piles[i] = number of bananas in pile i) and h hours
    // total. Each hour she picks exactly one pile and eats up to "speed" bananas from
    // it. If the pile has fewer than "speed" bananas, she finishes that pile and the
    // rest of that hour is wasted (she does NOT move on to another pile that hour).
    // Find the minimum integer eating speed k such that she can eat all piles within h hours.
    // Example: piles = [3,6,7,11], h = 8 -> Output: 4
    //   (at speed 4: hours = ceil(3/4)+ceil(6/4)+ceil(7/4)+ceil(11/4) = 1+2+2+3 = 8, exactly fits)
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible speed k starting from 1 up to max(piles). For each speed,
    // compute the total hours needed (sum of ceil(pile / k) for every pile). Return
    // the first k for which total hours <= h.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This checks speeds one at a time in increasing order, costing O(max(piles) * n)
    // in the worst case (n piles, checked for up to max(piles) different speeds).
    // If pile sizes are large (e.g. up to 10^9), this brute force is far too slow.
    //
    // ================= OPTIMIZED APPROACH =================
    // Binary search on the ANSWER (the eating speed k) instead of on the piles array.
    // Step 1: set lo = 1 (slowest possible useful speed) and hi = max(piles) (eating
    //          the single largest pile in one hour is always enough).
    // Step 2: pick mid = lo + (hi - lo) / 2 as a candidate speed.
    // Step 3: compute hoursNeeded(mid) = sum over all piles of ceil(pile / mid).
    // Step 4: if hoursNeeded(mid) <= h, speed mid WORKS, so try to find an even
    //          smaller speed: hi = mid.
    //          Otherwise mid is too slow, so we need a bigger speed: lo = mid + 1.
    // Step 5: when lo == hi, that value is the minimum working speed.
    //
    // This works because canFinish(speed) is MONOTONIC: if speed S lets Koko finish
    // in time, then any speed greater than S also lets her finish (faster eating
    // never increases hours needed). That monotonic "yes/no" boundary is exactly what
    // binary search needs, even though we're searching over a range of possible
    // ANSWERS rather than over the piles array itself.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure is required - the important idea is the general
    // "binary search on the answer" META-PATTERN: whenever you can write a fast
    // feasibility check isFeasible(candidateAnswer) that is monotonic (all answers
    // on one side of some threshold are "yes", all on the other side are "no"), you
    // can binary search directly over the space of possible answers instead of over
    // an array of values. This pattern generalizes far beyond this problem, e.g.:
    //   - Split Array Largest Sum: binary search on "the largest subarray sum allowed".
    //   - Capacity To Ship Packages Within D Days: binary search on "ship capacity".
    //   - Allocate Minimum Number of Pages / Books: binary search on "max pages per student".
    // In all of these, the "array" being searched isn't the input at all - it's the
    // conceptual range of possible numeric answers, bounded by an easy lower and
    // upper bound, with a monotonic feasibility check driving the binary search.
    //
    // ================= EDGE CASES =================
    // - h exactly equal to the number of piles: forces the minimum speed to be at
    //   least max(piles), since each pile can only be visited once per hour budget.
    // - Single pile: minimum speed is ceil(pile / h).
    // - Piles containing the value 1 mixed with large piles: small piles barely cost any hours.
    // - h very large (much bigger than number of piles): speed 1 may already be enough.
    // - All piles the same size.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(maxPile * n) - up to maxPile candidate speeds
    // tried, each costing O(n) to compute hours needed.
    // Optimized O(n log(maxPile)) - binary search does O(log(maxPile)) iterations,
    // each costing O(n) to compute hours needed for that candidate speed.
    // Space Complexity: O(1) extra space for both approaches - only a few counters
    // and index variables are used.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is canFinish(speed) monotonic, and why does binary search require that property?
    // - How would you compute ceil(pile / speed) without floating point division?
    // - How does this generalize to "Capacity To Ship Packages Within D Days" or "Split Array Largest Sum"? What's the feasibility check in each?
    // - What's the correct lower bound for lo - why not 0?
    // - What if speeds had to be from a restricted set of allowed values instead of any integer?
    // - How would the algorithm change if Koko could eat from multiple piles in a single hour?
    // - Could you solve this with a different search strategy (e.g. exponential search) if you didn't know a tight upper bound in advance?

    private static long hoursNeeded(int[] piles, int speed) {
        long hours = 0;
        for (int pile : piles) {
            // ceil(pile / speed) without floating point
            hours += (pile + speed - 1) / speed;
        }
        return hours;
    }

    // Brute force: try every speed starting from 1, O(maxPile * n).
    public static int minEatingSpeedBruteForce(int[] piles, int h) {
        int maxPile = Arrays.stream(piles).max().getAsInt();
        for (int speed = 1; speed <= maxPile; speed++) {
            if (hoursNeeded(piles, speed) <= h) {
                return speed; // first (smallest) speed that works
            }
        }
        return maxPile; // fallback, should always be found by maxPile
    }

    // Optimized: binary search on the answer (the eating speed), O(n log(maxPile)).
    public static int minEatingSpeedOptimized(int[] piles, int h) {
        int lo = 1;
        int hi = Arrays.stream(piles).max().getAsInt();

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2; // candidate eating speed
            if (hoursNeeded(piles, mid) <= h) {
                hi = mid; // this speed works, try a smaller one
            } else {
                lo = mid + 1; // too slow, need a bigger speed
            }
        }
        return lo; // minimum working speed
    }

    public static void main(String[] args) {
        int[] piles1 = {3, 6, 7, 11};
        int h1 = 8;
        // Expected: 4
        System.out.println("piles = " + Arrays.toString(piles1) + ", h = " + h1);
        System.out.println("Brute force: " + minEatingSpeedBruteForce(piles1, h1));
        System.out.println("Optimized:   " + minEatingSpeedOptimized(piles1, h1));

        int[] piles2 = {30, 11, 23, 4, 20};
        int h2 = 5;
        // Expected: 30 (h equals number of piles, forces speed = max pile)
        System.out.println("\npiles = " + Arrays.toString(piles2) + ", h = " + h2);
        System.out.println("Brute force: " + minEatingSpeedBruteForce(piles2, h2));
        System.out.println("Optimized:   " + minEatingSpeedOptimized(piles2, h2));

        // Edge case: h very large, speed 1 is already enough
        int[] piles3 = {1, 2, 3};
        int h3 = 100;
        // Expected: 1
        System.out.println("\npiles = " + Arrays.toString(piles3) + ", h = " + h3);
        System.out.println("Optimized: " + minEatingSpeedOptimized(piles3, h3));
    }
}
