package com.playground.java.interview.intervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Arrays / Sorting / Intervals
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given a sorted, non-overlapping list of intervals and a new interval,
 * insert the new interval into the list, merging with any overlapping intervals as needed.
 */
public class InsertInterval {

    // ================= PROBLEM =================
    // You have a list of intervals that is already sorted by start time and has no overlaps
    // between any two intervals. You are given one new interval to insert into this list.
    // After inserting, if the new interval overlaps with any existing intervals, they must be
    // merged together so the final list is still sorted and non-overlapping.
    // Example: intervals = [[1,3],[6,9]], newInterval = [2,5]
    //          -> output = [[1,5],[6,9]]  (new interval overlaps [1,3], merges into [1,5])
    //
    // ================= SIMPLE APPROACH =================
    // Add the new interval to the existing list, then run a general "merge intervals" routine
    // (sort everything by start, then sweep and merge overlapping neighbors) on the whole list.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Re-sorting the entire list is O(n log n), but the existing list is ALREADY sorted -
    // we are throwing away that information. We only need a single linear scan, giving O(n).
    //
    // ================= OPTIMIZED APPROACH =================
    // Walk through the existing sorted intervals once, in three phases:
    // 1) Add every interval that ends strictly before the new interval starts (no overlap,
    //    comes entirely before it) directly to the result.
    // 2) While the current interval overlaps the (possibly already-growing) new interval
    //    (i.e. its start <= newInterval's end), merge it into the new interval by taking
    //    min(starts) and max(ends). Keep expanding until no more intervals overlap it.
    //    Then add this final merged interval to the result.
    // 3) Add all remaining intervals (they start after the merged interval ends) as is.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A plain ArrayList used as the result buffer is enough - since the input is already
    // sorted, a single left-to-right pass with three clearly separated phases avoids any need
    // to re-sort or use a heap/tree; we only ever look at the next interval in order.
    //
    // ================= EDGE CASES =================
    // - Empty existing interval list: result is just the new interval.
    // - New interval overlaps nothing: it gets inserted at the correct position, list unchanged otherwise.
    // - New interval overlaps ALL existing intervals: everything collapses into one merged interval.
    // - New interval's range is a subset of an existing interval (fully contained): merged interval equals the existing one.
    // - New interval touches an interval exactly at the boundary (e.g. [1,3] and newInterval [3,5]): decide if touching counts as overlap (commonly yes).
    // - New interval goes before the very first interval or after the very last interval.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - a single linear pass over the existing intervals, no sorting needed
    // because the input is already sorted.
    // Space Complexity: O(n) for the output list (input is not modified in place).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would this change if the input list were NOT already sorted?
    // - How would you delete an interval range from a sorted, merged interval list?
    // - Can you do this with binary search to find the insertion point faster than a linear scan?
    // - How would you handle inserting many new intervals one at a time efficiently (streaming)?
    // - What if touching intervals like [1,3] and [3,5] should NOT be merged - how does the overlap condition change?
    // - How is this related to the general "merge intervals" problem, and how do the two solutions differ?
    // - How would you extend this to support removing overlaps instead of merging them?

    // Optimized: single linear pass in three phases (before, merge, after).
    public static int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int i = 0;
        int n = intervals.length;

        // Step 1: add all intervals that end before the new interval starts.
        while (i < n && intervals[i][1] < newInterval[0]) {
            result.add(intervals[i]);
            i++;
        }

        // Step 2: merge all intervals that overlap the new interval.
        int mergedStart = newInterval[0];
        int mergedEnd = newInterval[1];
        while (i < n && intervals[i][0] <= mergedEnd) {
            mergedStart = Math.min(mergedStart, intervals[i][0]);
            mergedEnd = Math.max(mergedEnd, intervals[i][1]);
            i++;
        }
        result.add(new int[]{mergedStart, mergedEnd});

        // Step 3: add all remaining intervals (they start after the merged interval ends).
        while (i < n) {
            result.add(intervals[i]);
            i++;
        }

        return result.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        int[][] intervals1 = {{1, 3}, {6, 9}};
        int[] newInterval1 = {2, 5};
        // Expected: [[1, 5], [6, 9]]
        System.out.println("Input: [[1,3],[6,9]], new=[2,5]");
        System.out.println("Output: " + Arrays.deepToString(insert(intervals1, newInterval1)));

        int[][] intervals2 = {{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}};
        int[] newInterval2 = {4, 8};
        // Expected: [[1, 2], [3, 10], [12, 16]]
        System.out.println("\nInput: [[1,2],[3,5],[6,7],[8,10],[12,16]], new=[4,8]");
        System.out.println("Output: " + Arrays.deepToString(insert(intervals2, newInterval2)));

        int[][] intervals3 = {};
        int[] newInterval3 = {5, 7};
        // Expected: [[5, 7]] (empty existing list)
        System.out.println("\nInput: [] (empty), new=[5,7]");
        System.out.println("Output: " + Arrays.deepToString(insert(intervals3, newInterval3)));
    }
}
