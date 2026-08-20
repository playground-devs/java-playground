package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * PATTERN: Arrays / Sorting / Intervals
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given a list of intervals, merge all overlapping intervals into one.
 */
public class MergeIntervals {

    // ================= PROBLEM =================
    // You get a list of time ranges (intervals), each with a start and an end.
    // Some of these ranges overlap with each other. You need to merge the overlapping
    // ones into a single bigger range, and return the final list of non-overlapping ranges.
    // Example: intervals = [[1,3], [2,6], [8,10], [15,18]] -> output = [[1,6], [8,10], [15,18]]
    // because [1,3] and [2,6] overlap and merge into [1,6].
    //
    // ================= SIMPLE APPROACH =================
    // Compare every interval with every other interval to check if they overlap.
    // If two intervals overlap, merge them into one and repeat the comparison process
    // again on the updated list, until no more merges are possible.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Comparing every interval with every other interval is O(n^2), and repeating this
    // process until nothing changes can make it even slower in the worst case.
    // It's also messy to implement correctly (updating a list while iterating over it).
    //
    // ================= OPTIMIZED APPROACH =================
    // First, sort all intervals by their start value. Once sorted, any intervals that
    // overlap must be next to each other in the list.
    // Then walk through the sorted intervals one by one, keeping track of a "current merged
    // interval". If the next interval's start is less than or equal to the current merged
    // interval's end, they overlap - extend the current merged interval's end if needed.
    // If the next interval's start is greater than the current merged interval's end,
    // there's no overlap - save the current merged interval to the result and start a new one.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting (an array/list operation) is the key trick here: it turns an all-pairs
    // comparison problem into a simple single linear scan, because after sorting by start time,
    // overlaps can only happen between neighboring intervals.
    // A simple ArrayList is enough to build the result, no map or tree needed.
    //
    // ================= EDGE CASES =================
    // - Empty list of intervals: return an empty list.
    // - Single interval: return it as is.
    // - Intervals that only touch at the boundary, e.g. [1,3] and [3,5]: decide if these
    //   count as overlapping (commonly yes, they get merged into [1,5]).
    // - All intervals overlap into one big interval.
    // - No intervals overlap at all: output equals input (just sorted).
    // - Intervals given out of order.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) - dominated by the sort; the merging scan afterward is O(n).
    // Brute force is O(n^2) or worse due to repeated comparisons and merges.
    // Space Complexity: O(n) for the output list (and O(log n) to O(n) for the sort itself,
    // depending on the JDK's sort implementation).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you insert a new interval into an already-merged, sorted list efficiently?
    // - What if touching intervals like [1,3] and [3,5] should NOT be merged - how does the condition change?
    // - How would you find the minimum number of meeting rooms needed, given the same kind of interval list?
    // - What if intervals arrive one at a time as a stream, instead of all at once?
    // - How would you extend this to intervals on a 2D plane or multi-dimensional ranges?
    // - Can you do this without sorting, if you know the intervals are already close to sorted?
    // - How would you remove a given interval range from a set of merged intervals?

    // Optimized: sort by start, then merge overlapping neighbors in one pass.
    public static int[][] mergeIntervalsOptimized(int[][] intervals) {
        if (intervals == null || intervals.length == 0) {
            return new int[0][];
        }

        // Step 1: sort intervals by their start value.
        int[][] sorted = intervals.clone();
        Arrays.sort(sorted, Comparator.comparingInt(interval -> interval[0]));

        List<int[]> merged = new ArrayList<>();
        int[] current = sorted[0];
        merged.add(current);

        for (int i = 1; i < sorted.length; i++) {
            int[] next = sorted[i];
            if (next[0] <= current[1]) {
                // Overlaps (or touches) the current merged interval - extend its end.
                current[1] = Math.max(current[1], next[1]);
            } else {
                // No overlap - this becomes the new "current" interval.
                current = next;
                merged.add(current);
            }
        }

        return merged.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        int[][] intervals1 = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        // Expected: [[1, 6], [8, 10], [15, 18]]
        System.out.println("Input: [[1,3],[2,6],[8,10],[15,18]]");
        System.out.println("Output: " + Arrays.deepToString(mergeIntervalsOptimized(intervals1)));

        int[][] intervals2 = {{1, 4}, {4, 5}};
        // Expected: [[1, 5]] (touching intervals get merged)
        System.out.println("\nInput: [[1,4],[4,5]] (touching boundary)");
        System.out.println("Output: " + Arrays.deepToString(mergeIntervalsOptimized(intervals2)));

        int[][] intervals3 = {};
        // Expected: [] (empty input)
        System.out.println("\nInput: [] (empty)");
        System.out.println("Output: " + Arrays.deepToString(mergeIntervalsOptimized(intervals3)));
    }
}
