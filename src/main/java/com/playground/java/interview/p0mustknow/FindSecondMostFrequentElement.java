package com.playground.java.interview.p0mustknow;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * PATTERN: HashMap / Java 8 Streams (groupingBy + counting)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Find the element in an array that has the second highest frequency
 * of occurrence.
 */
public class FindSecondMostFrequentElement {

    // ================= PROBLEM =================
    // You get a list of numbers. Some numbers repeat more than others.
    // You need to find the number that has the SECOND highest frequency (count of occurrences),
    // not the most frequent one, but the next one down.
    // Example: nums = [1, 2, 2, 3, 3, 3] -> output = 2
    // because 3 appears 3 times (most frequent), and 2 appears 2 times (second most frequent).
    //
    // This problem is not really about algorithmic complexity (a HashMap already makes counting
    // O(n)). It's commonly asked to check Java 8 fluency - can you solve it with a plain loop,
    // AND can you express the same logic cleanly using Streams (groupingBy + counting)?
    // So instead of "Brute Force vs Optimized", this file shows "Loop Approach" vs
    // "Java 8 Streams Approach" - both are O(n log n)-ish and both are perfectly acceptable;
    // the interviewer is checking your comfort with functional-style Java.
    //
    // ================= LOOP APPROACH =================
    // Step 1: Build a frequency map (number -> count) using a simple for-each loop and a HashMap.
    // Step 2: Find the highest frequency value present in the map.
    // Step 3: Find the highest frequency value that is STRICTLY LESS than the max frequency
    // (this is the "second highest" distinct frequency).
    // Step 4: Find any number in the map whose frequency equals that second highest frequency.
    //
    // ================= JAVA 8 STREAMS APPROACH =================
    // Step 1: Use Arrays.stream(...).boxed() to turn the int[] into a Stream<Integer>.
    // Step 2: Use Collectors.groupingBy(Function.identity(), Collectors.counting()) to build
    // the same frequency map (number -> count) in a single declarative statement.
    // Step 3: Stream the map's entries, sort them by frequency descending, and get the distinct
    // frequency values in order using map + distinct.
    // Step 4: Skip the first (highest) distinct frequency, take the next one, and find an entry
    // matching it.
    // This expresses the exact same logic as the loop version, just declaratively.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap (or the Streams groupingBy equivalent) is the natural choice for frequency
    // counting because it gives O(1) average time to increment a count per element, avoiding
    // the need to re-scan the array for every number.
    //
    // ================= EDGE CASES =================
    // - Array with fewer than 2 distinct frequency levels (e.g. all elements are unique,
    //   so every frequency is 1): there is no "second highest" frequency - must handle this
    //   (we throw an exception here).
    // - Array with only one element: no second most frequent element exists.
    // - Empty array: no elements at all, must handle explicitly.
    // - Ties: multiple numbers share the same second-highest frequency - we return the first
    //   one found (behavior should be clearly documented, as the "correct" answer may vary).
    // - All elements identical, e.g. [5, 5, 5, 5]: only one frequency level exists (no second).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) to build the frequency map, plus O(m log m) to sort/scan the
    // distinct frequency values, where m is the number of distinct elements. Overall O(n + m log m),
    // which is effectively O(n log n) in the worst case (m close to n).
    // Space Complexity: O(m) for the frequency map, where m is the number of distinct elements.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you generalize this to find the Kth most frequent element?
    // - What's the performance and readability trade-off between the loop version and the Streams version?
    // - Does Collectors.groupingBy(Function.identity(), Collectors.counting()) have any overhead vs a plain HashMap loop?
    // - How would you handle a tie for the second highest frequency - return one, or all of them?
    // - Can you rewrite the Streams version using Collectors.groupingBy with a downstream summing collector instead of counting?
    // - How would you make the Streams version run in parallel (parallelStream) - is it safe here?
    // - What happens if the array is empty or null - how does each approach fail, and how should it fail?

    // ---------- Loop Approach ----------
    public static int secondMostFrequentLoop(int[] nums) {
        if (nums == null || nums.length < 2) {
            throw new NoSuchElementException("Not enough elements to have a second most frequent value");
        }

        // Step 1: count frequency of each number.
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int num : nums) {
            frequencyMap.merge(num, 1, Integer::sum);
        }

        // Step 2: find the maximum frequency.
        int maxFrequency = Integer.MIN_VALUE;
        for (int freq : frequencyMap.values()) {
            maxFrequency = Math.max(maxFrequency, freq);
        }

        // Step 3: find the highest frequency that is strictly less than maxFrequency.
        int secondFrequency = Integer.MIN_VALUE;
        for (int freq : frequencyMap.values()) {
            if (freq < maxFrequency && freq > secondFrequency) {
                secondFrequency = freq;
            }
        }

        if (secondFrequency == Integer.MIN_VALUE) {
            throw new NoSuchElementException("No second distinct frequency level exists");
        }

        // Step 4: return the first number found with that second-highest frequency.
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() == secondFrequency) {
                return entry.getKey();
            }
        }

        // Should never reach here given the logic above.
        throw new NoSuchElementException("No second most frequent element found");
    }

    // ---------- Java 8 Streams Approach ----------
    public static int secondMostFrequentStreams(int[] nums) {
        if (nums == null || nums.length < 2) {
            throw new NoSuchElementException("Not enough elements to have a second most frequent value");
        }

        // Step 1 + 2: build number -> count map using groupingBy + counting.
        Map<Integer, Long> frequencyMap = java.util.Arrays.stream(nums)
                .boxed()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        // Step 3: get distinct frequency values, sorted highest to lowest.
        List<Long> distinctFrequenciesDesc = frequencyMap.values().stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (distinctFrequenciesDesc.size() < 2) {
            throw new NoSuchElementException("No second distinct frequency level exists");
        }

        // Step 4: the second entry in the sorted distinct list is the second-highest frequency.
        long secondFrequency = distinctFrequenciesDesc.get(1);

        // Find any number whose frequency matches the second-highest frequency.
        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == secondFrequency)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No second most frequent element found"));
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 2, 3, 3, 3};
        // Expected: 2 (3 appears 3 times = most frequent, 2 appears 2 times = second most frequent)
        System.out.println("Input: [1, 2, 2, 3, 3, 3]");
        System.out.println("Loop Approach output: " + secondMostFrequentLoop(nums1));
        System.out.println("Java 8 Streams Approach output: " + secondMostFrequentStreams(nums1));

        int[] nums2 = {5, 5, 5, 5};
        // Expected: exception - only one frequency level exists (all elements identical)
        System.out.println("\nInput: [5, 5, 5, 5] (only one frequency level)");
        try {
            secondMostFrequentLoop(nums2);
        } catch (NoSuchElementException e) {
            System.out.println("Loop Approach threw as expected: " + e.getMessage());
        }

        int[] nums3 = {4, 4, 6, 6, 1, 2, 2, 2};
        // Expected: 4 or 6 (both tied at frequency 2, which is second highest after 2 -> freq 3)
        System.out.println("\nInput: [4, 4, 6, 6, 1, 2, 2, 2] (tie for second highest frequency)");
        System.out.println("Loop Approach output: " + secondMostFrequentLoop(nums3));
        System.out.println("Java 8 Streams Approach output: " + secondMostFrequentStreams(nums3));
    }
}
