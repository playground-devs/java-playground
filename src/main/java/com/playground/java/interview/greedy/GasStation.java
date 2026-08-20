package com.playground.java.interview.greedy;

import java.util.Arrays;

/**
 * PATTERN: Greedy
 * PRIORITY: P2
 * ONE-LINE PROBLEM STATEMENT: Find the starting gas station that allows completing a circular route, or determine it's impossible.
 */
public class GasStation {

    // ================= PROBLEM =================
    // There are n gas stations arranged in a circle. gas[i] is the gas available at station
    // i, and cost[i] is the gas needed to travel from station i to station i+1. Starting
    // with an empty tank at some station, determine an index to start from such that you
    // can travel the entire circuit exactly once. Return -1 if no such start exists
    // (assume the answer is unique if it exists).
    // Example: gas = [1,2,3,4,5], cost = [3,4,5,1,2] -> start at index 3 (tank never goes negative from there)
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible starting station. For each candidate start, simulate the full
    // circuit: track the tank as you visit each station, and if the tank ever goes
    // negative, that start fails - move on to the next candidate.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // In the worst case (e.g. only the last station works), this simulates nearly n full
    // circuits, each up to n stations long, giving O(n^2) time. Most of that repeated work
    // is unnecessary once you notice a key structural fact about when a start can/can't work.
    //
    // ================= OPTIMIZED APPROACH =================
    // Single greedy pass, O(n):
    // Step 1: Compute totalTank = sum(gas[i] - cost[i]) over all stations. If totalTank < 0,
    //          it's impossible overall (not enough gas in the whole system) - return -1.
    // Step 2: Otherwise, walk through stations tracking a running tank (starting at 0) and
    //          a candidate start (starting at 0). At each station i: tank += gas[i] - cost[i].
    //          If tank < 0, none of the stations from the current candidate start through i
    //          could have been a valid starting point (explained below) - so reset
    //          candidateStart = i + 1 and tank = 0, then continue.
    // Step 3: Because totalTank >= 0 is guaranteed, the candidateStart left standing after
    //          the full pass is guaranteed to be a valid answer - no need to double-check
    //          by simulating again.
    //
    // Proof sketch (why the greedy reset is safe):
    // If starting at station S and driving to station i causes the tank to go negative,
    // then for ANY station M between S and i (inclusive), starting at M instead would also
    // fail by the time you reach i - because the tank accumulated from S through M was
    // never negative before the point of failure (otherwise we'd have reset earlier), so
    // starting at M can only have LESS or EQUAL gas margin by the time you reach i, not more.
    // So it's safe to jump the candidate start all the way to i + 1 and discard every
    // station in between as impossible.
    // Also, since totalTank >= 0, whatever deficit accumulated before the final reset must
    // be exactly offset by surplus accumulated after it (by the time we wrap back around) -
    // guaranteeing the last candidateStart works, with no separate wraparound simulation needed.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No special data structure is needed - just two running totals (tank and totalTank)
    // and a candidate index, because the greedy insight above lets a single left-to-right
    // pass safely skip over entire ranges of guaranteed-invalid starts, without ever needing
    // to revisit them.
    //
    // ================= EDGE CASES =================
    // - totalGas exactly equals totalCost: still solvable (tight case, tank ends exactly at 0).
    // - Single station (n = 1): trivially station 0 if gas[0] >= cost[0], else -1.
    // - All gas[i] == cost[i]: any station works; this algorithm returns index 0 (tank never
    //   goes negative since every step nets to 0).
    // - Impossible case, totalGas < totalCost: return -1 immediately, no need to scan further.
    // - The valid answer being near the end of the array: the algorithm naturally finds it in
    //   one forward pass without literally wrapping around the array, because the proof
    //   sketch above guarantees whatever candidate remains at the end already accounts for the full circuit.

    // Brute force: try every starting station, simulate the full circuit. O(n^2)
    public static int canCompleteCircuitBruteForce(int[] gas, int[] cost) {
        int n = gas.length;
        for (int start = 0; start < n; start++) {
            int tank = 0;
            boolean completedCircuit = true;
            for (int step = 0; step < n; step++) {
                int i = (start + step) % n; // wrap around the circle
                tank += gas[i] - cost[i];
                if (tank < 0) {
                    completedCircuit = false;
                    break;
                }
            }
            if (completedCircuit) {
                return start;
            }
        }
        return -1;
    }

    // Optimized: single greedy pass. O(n)
    public static int canCompleteCircuitGreedy(int[] gas, int[] cost) {
        int n = gas.length;
        int totalTank = 0;
        int tank = 0;
        int candidateStart = 0;

        for (int i = 0; i < n; i++) {
            int net = gas[i] - cost[i];
            totalTank += net;
            tank += net;

            // This stretch from candidateStart to i failed - no station in it can work.
            if (tank < 0) {
                candidateStart = i + 1;
                tank = 0;
            }
        }

        return totalTank >= 0 ? candidateStart : -1;
    }

    public static void main(String[] args) {
        int[] gas1 = {1, 2, 3, 4, 5};
        int[] cost1 = {3, 4, 5, 1, 2};
        System.out.println("gas = " + Arrays.toString(gas1) + ", cost = " + Arrays.toString(cost1));
        // Expected: 3
        System.out.println("Brute force: " + canCompleteCircuitBruteForce(gas1, cost1));
        System.out.println("Greedy:      " + canCompleteCircuitGreedy(gas1, cost1));

        int[] gas2 = {2, 3, 4};
        int[] cost2 = {3, 4, 3};
        // Expected: -1 (totalGas=9, totalCost=10, impossible overall)
        System.out.println("\ngas = " + Arrays.toString(gas2) + ", cost = " + Arrays.toString(cost2));
        System.out.println("Brute force: " + canCompleteCircuitBruteForce(gas2, cost2));
        System.out.println("Greedy:      " + canCompleteCircuitGreedy(gas2, cost2));

        int[] gas3 = {5, 5, 5};
        int[] cost3 = {5, 5, 5};
        // Expected: 0 (all net to 0, tight case, any station works, algorithm returns 0)
        System.out.println("\ngas = " + Arrays.toString(gas3) + ", cost = " + Arrays.toString(cost3) + " (all gas[i] == cost[i])");
        System.out.println("Brute force: " + canCompleteCircuitBruteForce(gas3, cost3));
        System.out.println("Greedy:      " + canCompleteCircuitGreedy(gas3, cost3));
    }
}
