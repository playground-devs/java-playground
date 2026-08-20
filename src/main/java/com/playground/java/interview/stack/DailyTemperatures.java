package com.playground.java.interview.stack;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Monotonic Stack
 * PRIORITY: P1
 * PROBLEM STATEMENT: For each day, find how many days you must wait until a warmer temperature occurs.
 */
public class DailyTemperatures {

    // ================= PROBLEM =================
    // Given an array of daily temperatures, return an array where each element is the number
    // of days you would have to wait after that day to get a warmer temperature. If there is
    // no future day with a warmer temperature, put 0 for that day.
    // Example: temperatures = [73,74,75,71,69,72,76,73]
    // -> Output: [1,1,4,2,1,1,0,0]
    //
    // ================= SIMPLE APPROACH =================
    // For each day i, scan forward day by day (i+1, i+2, ...) until finding a day with a
    // strictly warmer temperature, and record the distance. If no warmer day is found before
    // the end of the array, record 0.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // In the worst case (e.g. strictly decreasing temperatures, or all days needing to look
    // all the way to a warm day near the end), each day's forward scan can take O(n) time,
    // giving O(n^2) total time. Days near the beginning end up being rescanned worth of work
    // even though the answer for later days doesn't depend on already-known information from
    // earlier scans.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a monotonic decreasing stack of indices (temperatures at those indices decrease as
    // you go from the bottom to the top of the stack).
    // Step 1: Walk through the temperatures left to right. For each day i:
    // Step 2: While the stack is non-empty AND temperatures[i] is greater than the
    //          temperature at the index on top of the stack, pop that index - it means day i
    //          is the first warmer day for that popped day. Set result[poppedIndex] =
    //          i - poppedIndex (the number of days waited).
    // Step 3: Push i onto the stack (its warmer day hasn't been found yet).
    // Step 4: Any indices still left on the stack after the full scan never found a warmer
    //          day, so their result stays 0 (the array's default value).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A monotonic decreasing stack lets each day be resolved in O(1) amortized time: instead
    // of every day individually scanning forward, each day is popped exactly once, at the
    // moment its answer becomes known (the first time a warmer day appears). This works
    // because the stack only ever holds indices whose "warmer day" question is still
    // unanswered, and a new warmer temperature immediately resolves every unresolved day it
    // is warmer than, in the correct order (most recent unresolved day resolved first, which
    // is naturally LIFO).
    //
    // ================= EDGE CASES =================
    // - Strictly decreasing temperatures, e.g. [75,74,73]: no day ever finds a warmer day, entire result is 0.
    // - Strictly increasing temperatures, e.g. [70,71,72]: every day's very next day is warmer, result is all 1s except the last day (0).
    // - All same temperature, e.g. [70,70,70]: no day is STRICTLY warmer than another, entire result is 0.
    // - Single day: result is [0], no future day exists.
    // - Empty array: result is an empty array.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n^2) because each day's forward scan can take up to
    // O(n) time. Optimized O(n) because each index is pushed onto and popped from the stack
    // at most once.
    // Space Complexity: Brute force O(n) just for the result array (O(1) extra beyond that).
    // Optimized O(n) worst case for the stack (e.g. strictly decreasing temperatures push every index and none get popped).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve this problem walking RIGHT TO LEFT instead of left to right, using a different stack strategy?
    // - How does this problem relate to "Next Greater Element" - are they the same underlying pattern?
    // - What if you needed to find, for each day, the number of days until a temperature that is warmer by at least some threshold amount?
    // - How would you extend this to answer "how many days until warmer" queries for arbitrary historical date ranges, not just from each day forward?
    // - Why does a monotonic stack guarantee each index is pushed and popped at most once (proving the O(n) bound)?
    // - What is the difference in stack direction (increasing vs decreasing) between this problem and "Largest Rectangle in Histogram"?
    // - How would you adapt this if temperatures could update dynamically (streaming), and you needed running answers?

    // Brute force: for each day, scan forward for a warmer day. O(n^2).
    public static int[] dailyTemperaturesBruteForce(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (temperatures[j] > temperatures[i]) {
                    result[i] = j - i;
                    break; // found the first warmer day, stop scanning
                }
            }
            // if no warmer day found, result[i] stays 0 (default)
        }

        return result;
    }

    // Optimized: monotonic decreasing stack of indices. O(n).
    public static int[] dailyTemperaturesOptimized(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>(); // indices with decreasing temperatures, top to bottom

        for (int i = 0; i < n; i++) {
            // Resolve every unresolved day that this day is warmer than.
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int poppedIndex = stack.pop();
                result[poppedIndex] = i - poppedIndex;
            }
            stack.push(i);
        }
        // Any indices remaining on the stack never found a warmer day; result stays 0.

        return result;
    }

    public static void main(String[] args) {
        int[] temps1 = {73, 74, 75, 71, 69, 72, 76, 73};
        // Expected: [1,1,4,2,1,1,0,0]
        System.out.println("Input: " + java.util.Arrays.toString(temps1));
        System.out.println("Brute force: " + java.util.Arrays.toString(dailyTemperaturesBruteForce(temps1)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(dailyTemperaturesOptimized(temps1)));

        int[] temps2 = {75, 74, 73};
        // Expected: [0,0,0] (strictly decreasing, no warmer day ever)
        System.out.println("\nInput: " + java.util.Arrays.toString(temps2));
        System.out.println("Brute force: " + java.util.Arrays.toString(dailyTemperaturesBruteForce(temps2)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(dailyTemperaturesOptimized(temps2)));

        int[] temps3 = {};
        // Expected: [] (empty input)
        System.out.println("\nInput: " + java.util.Arrays.toString(temps3));
        System.out.println("Brute force: " + java.util.Arrays.toString(dailyTemperaturesBruteForce(temps3)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(dailyTemperaturesOptimized(temps3)));
    }
}
