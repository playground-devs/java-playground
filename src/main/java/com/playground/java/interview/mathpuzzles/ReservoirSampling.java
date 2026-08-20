package com.playground.java.interview.mathpuzzles;

import java.util.Random;

/**
 * PATTERN: Math / Reservoir Sampling
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Pick one element uniformly at random from a stream of unknown or
 * very large length, using only O(1) extra space.
 */
public class ReservoirSampling {

    // ================= PROBLEM =================
    // You get a stream of numbers, one at a time, and you don't know in advance how
    // many numbers there will be in total (it could even be unbounded, like a live
    // feed of events). You need to pick one element such that every element that
    // has appeared so far has an equal probability of being the one chosen, at any
    // point you're asked "give me your current pick".
    // Example: stream = [5, 3, 9, 1, 7] one at a time -> after seeing all of them,
    // each of the 5 numbers should have exactly a 1/5 chance of being the final pick.
    //
    // ================= SIMPLE APPROACH =================
    // Load the entire stream into a list/array first. Once you know the total
    // count n, generate a random index between 0 and n-1, and return the element
    // at that index.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This requires storing the entire stream in memory (O(n) space), and it
    // requires knowing the total length n in advance. For a truly large or
    // infinite/unbounded stream (e.g., log lines from a live system, or a stream
    // too big to fit in memory), this approach is simply not feasible - you might
    // run out of memory, or you may never even know when the stream "ends".
    //
    // ================= OPTIMIZED APPROACH =================
    // Use Reservoir Sampling (specifically Algorithm R for a reservoir of size 1).
    // Keep a single "chosen" variable. When the first element arrives, it becomes
    // the chosen element automatically. For every subsequent element (the k-th
    // element, 1-indexed), replace the chosen element with this new one with
    // probability 1/k. This can be done by generating a random integer between 0
    // and k-1 (inclusive) and replacing the chosen element only if that random
    // number is exactly 0.
    // This guarantees that after processing all n elements seen so far, every
    // element has exactly a 1/n chance of being the current chosen one - provable
    // by induction on k.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No data structure beyond a couple of variables (the chosen value and a
    // running count of elements seen) is needed. This is exactly the point of
    // reservoir sampling: it turns a problem that seems to require storing
    // everything into one that only requires O(1) memory, by cleverly adjusting
    // the replacement probability as more elements arrive.
    //
    // ================= EDGE CASES =================
    // - Stream with exactly one element: that element is always the chosen one (100% probability, trivially).
    // - Empty stream (no elements ever arrive): there is no valid chosen element - handle by throwing or returning a sentinel.
    // - Stream where every element is identical: the "choice" doesn't matter but the algorithm still runs correctly.
    // - Very large stream where element count exceeds Integer.MAX_VALUE - would need a long counter instead of int.
    // - Being asked for the "current pick" partway through the stream, not just at the end - the algorithm supports this naturally since it maintains a valid uniform pick at every step.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(1) per incoming element for reservoir sampling with
    // reservoir size 1 - a single random number generation and comparison per element.
    // Loading everything first is O(n) time overall but O(1) per element too; the real
    // difference is space.
    // Space Complexity: O(1) for reservoir sampling - just the chosen value and a
    // counter, regardless of how many elements have streamed through.
    // O(n) for the "load everything first" approach, since it must store the whole stream.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you prove by induction that after processing k elements, each has exactly a 1/k chance of being chosen?
    // - How would you extend this to pick K elements uniformly at random instead of just 1 (reservoir of size K)?
    // - How would you handle picking a weighted random element, where some elements should have higher probability?
    // - What if you needed to support "undo" (removing the most recent element from consideration)?
    // - How would this work in a distributed system processing a stream across multiple machines?
    // - What source of randomness would you use in production, and does its quality matter here?
    // - How would you test that your reservoir sampling implementation is actually producing a uniform distribution?

    private final Random random;
    private Object chosen;
    private long count;

    public ReservoirSampling() {
        this.random = new Random();
        this.chosen = null;
        this.count = 0;
    }

    // Process one new element arriving from the stream. O(1) time, O(1) space.
    public void addElementOptimized(Object element) {
        count++;
        if (count == 1) {
            // First element always becomes the initial chosen pick.
            chosen = element;
            return;
        }
        // Replace the chosen element with probability 1/count.
        // nextInt((int) count) returns a value in [0, count-1]; replace only if it's exactly 0.
        int randomIndex = random.nextInt((int) count);
        if (randomIndex == 0) {
            chosen = element;
        }
    }

    // Returns the current uniformly-random pick from everything seen so far.
    public Object getCurrentPick() {
        if (count == 0) {
            throw new IllegalStateException("No elements have been added yet");
        }
        return chosen;
    }

    // Brute force / naive alternative: load the whole stream first, then pick a random index. O(n) space.
    public static Object pickRandomBruteForce(Object[] fullStream) {
        if (fullStream == null || fullStream.length == 0) {
            throw new IllegalArgumentException("Stream must not be empty");
        }
        Random random = new Random();
        int randomIndex = random.nextInt(fullStream.length);
        return fullStream[randomIndex];
    }

    public static void main(String[] args) {
        // Expected: some value from the stream (non-deterministic due to randomness),
        // but statistically each of the 5 elements should show up roughly equally
        // often if this were run many times.
        Integer[] stream1 = {5, 3, 9, 1, 7};
        System.out.println("Input: stream = [5, 3, 9, 1, 7], processed one at a time");
        ReservoirSampling sampler = new ReservoirSampling();
        for (Integer value : stream1) {
            sampler.addElementOptimized(value);
        }
        System.out.println("Reservoir sampling pick (random, any of the 5 values is valid): " + sampler.getCurrentPick());
        System.out.println("Brute force pick (loads whole array first): " + pickRandomBruteForce(stream1));

        // Expected: 42 (only one element ever arrives, must be the pick)
        ReservoirSampling singleElementSampler = new ReservoirSampling();
        singleElementSampler.addElementOptimized(42);
        System.out.println("\nInput: stream = [42] (single element)");
        System.out.println("Reservoir sampling pick: " + singleElementSampler.getCurrentPick());

        // Expected: IllegalStateException (edge case, no elements added yet)
        System.out.println("\nInput: empty stream, calling getCurrentPick() before any element arrives");
        try {
            new ReservoirSampling().getCurrentPick();
        } catch (IllegalStateException e) {
            System.out.println("Correctly threw: " + e.getMessage());
        }
    }
}
