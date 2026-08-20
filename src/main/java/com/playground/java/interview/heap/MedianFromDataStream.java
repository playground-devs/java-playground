package com.playground.java.interview.heap;

import java.util.Collections;
import java.util.PriorityQueue;

/**
 * PATTERN: Heap (Two Heaps)
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Support adding numbers from a stream and finding the running median efficiently.
 */
public class MedianFromDataStream {

    // ================= PROBLEM =================
    // Design a data structure with two operations:
    //   addNum(int num)  - add a number from an ongoing data stream.
    //   findMedian()     - return the median of all numbers added so far.
    // Both operations can be called many times, interleaved in any order.
    // Example: addNum(1), addNum(2) -> findMedian() = 1.5
    //          addNum(3)             -> findMedian() = 2
    //
    // ================= SIMPLE APPROACH =================
    // Keep all numbers in a plain growable list. Every time findMedian() is called, sort
    // the whole list and pick the middle element (or average the two middle elements).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting the entire list costs O(n log n) - and we pay that cost EVERY SINGLE TIME
    // findMedian() is called, even if only one number was added since the last call. In a
    // stream with frequent median queries interleaved with adds, this is very wasteful.
    //
    // ================= OPTIMIZED APPROACH =================
    // Maintain two heaps that split the numbers into a "lower half" and an "upper half":
    //   lowerHalf - a MAX-heap holding the smaller half of all numbers seen so far.
    //   upperHalf - a MIN-heap holding the larger half of all numbers seen so far.
    // Kept balanced so their sizes differ by at most 1.
    // addNum(num):
    //   Step 1: Always offer num to lowerHalf (max-heap) first.
    //   Step 2: Move lowerHalf's max over to upperHalf (keeps the two halves correctly
    //            ordered relative to each other - lowerHalf's biggest might actually
    //            belong in the upper half).
    //   Step 3: If upperHalf now has more elements than lowerHalf, move its min back to
    //            lowerHalf (rebalance so lowerHalf is never smaller, and at most 1 bigger).
    // findMedian():
    //   If both heaps are the same size, median = average of lowerHalf.peek() and upperHalf.peek().
    //   If lowerHalf has one extra element, median = lowerHalf.peek().
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // The median only ever depends on the one or two numbers sitting right at the boundary
    // between the "lower half" and "upper half" of the data - specifically the max of the
    // lower half and the min of the upper half. A max-heap gives O(1) access to the lower
    // half's maximum, and a min-heap gives O(1) access to the upper half's minimum, so
    // findMedian() is just peeking at one or two heap tops - O(1). Insertions stay cheap at
    // O(log n) because heaps support O(log n) insert. This is a massive improvement over
    // brute force, which pays O(n log n) per findMedian() call regardless of how many adds
    // happened in between.
    //
    // ================= EDGE CASES =================
    // - findMedian() called before any addNum(): no data yet - guard against this (here we throw).
    // - Single element added: median is just that element (lowerHalf has 1, upperHalf has 0).
    // - Even count of elements: median = average of the two middle values.
    // - Odd count of elements: median = the single true middle value (from lowerHalf).
    // - All identical values added: heaps still balance correctly, median = that value.
    // - Numbers added in already-sorted order vs. random order: complexity is identical either way
    //   since heap insert cost doesn't depend on input order.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force: O(n log n) per findMedian() call (full sort each time).
    // Optimized: addNum() is O(log n) (heap insert + rebalance), findMedian() is O(1) (peek only).
    // Space Complexity: O(n) for both approaches - all n numbers must be stored somewhere,
    // either in the flat list (brute force) or split across the two heaps (optimized).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must the two heaps differ in size by at most 1, and why always insert into lowerHalf first?
    // - How would you handle a very large stream where numbers are bounded to a small range (e.g. 0-100)? (Bucket/counting approach can beat heaps.)
    // - How would you extend this to find the median of the last k elements only (sliding window median)?
    // - What if you needed the k-th percentile instead of just the median?
    // - How would you make addNum()/findMedian() thread-safe for concurrent producers/consumers?
    // - Can you do this with a single balanced BST or order-statistics tree instead of two heaps? What are the trade-offs?
    // - What happens to correctness if you accidentally use a min-heap for both halves instead of a max-heap for the lower half?

    // Brute force: store everything in a list, sort on every findMedian() call.
    public static class BruteForceMedianFinder {
        private final java.util.List<Integer> numbers = new java.util.ArrayList<>();

        public void addNum(int num) {
            numbers.add(num);
        }

        public double findMedian() {
            if (numbers.isEmpty()) {
                throw new IllegalStateException("No numbers added yet");
            }
            java.util.List<Integer> sorted = new java.util.ArrayList<>(numbers);
            Collections.sort(sorted); // O(n log n) every call
            int n = sorted.size();
            int mid = n / 2;
            if (n % 2 == 0) {
                return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
            }
            return sorted.get(mid);
        }
    }

    // Optimized: two heaps, kept balanced after every insert.
    public static class MedianFinder {
        private final PriorityQueue<Integer> lowerHalf = new PriorityQueue<>(Collections.reverseOrder()); // max-heap
        private final PriorityQueue<Integer> upperHalf = new PriorityQueue<>(); // min-heap

        public void addNum(int num) {
            // Step 1: always add to lowerHalf first.
            lowerHalf.offer(num);
            // Step 2: move lowerHalf's max into upperHalf to keep the halves properly ordered.
            upperHalf.offer(lowerHalf.poll());
            // Step 3: rebalance - lowerHalf should never be smaller than upperHalf.
            if (upperHalf.size() > lowerHalf.size()) {
                lowerHalf.offer(upperHalf.poll());
            }
        }

        public double findMedian() {
            if (lowerHalf.isEmpty()) {
                throw new IllegalStateException("No numbers added yet");
            }
            if (lowerHalf.size() > upperHalf.size()) {
                return lowerHalf.peek();
            }
            return (lowerHalf.peek() + upperHalf.peek()) / 2.0;
        }
    }

    public static void main(String[] args) {
        System.out.println("Optimized MedianFinder (two heaps):");
        MedianFinder finder = new MedianFinder();
        finder.addNum(1);
        finder.addNum(2);
        // Expected: 1.5
        System.out.println("addNum(1), addNum(2) -> findMedian() = " + finder.findMedian() + " (expected 1.5)");
        finder.addNum(3);
        // Expected: 2
        System.out.println("addNum(3) -> findMedian() = " + finder.findMedian() + " (expected 2.0)");
        finder.addNum(0);
        // Expected: 1.5 (sorted: 0,1,2,3)
        System.out.println("addNum(0) -> findMedian() = " + finder.findMedian() + " (expected 1.5)");

        System.out.println("\nEdge case: single element");
        MedianFinder single = new MedianFinder();
        single.addNum(42);
        // Expected: 42.0
        System.out.println("addNum(42) -> findMedian() = " + single.findMedian() + " (expected 42.0)");

        System.out.println("\nBrute force comparison:");
        BruteForceMedianFinder bruteForce = new BruteForceMedianFinder();
        bruteForce.addNum(5);
        bruteForce.addNum(1);
        bruteForce.addNum(9);
        // Expected: 5.0 (sorted: 1,5,9)
        System.out.println("addNum(5,1,9) -> findMedian() = " + bruteForce.findMedian() + " (expected 5.0)");
    }
}
