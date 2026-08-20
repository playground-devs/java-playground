package com.playground.java.interview.intervals;

import java.util.Arrays;

/**
 * PATTERN: Arrays / Sorting / Intervals (Two-Pointer Sweep)
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given meeting time intervals, find the minimum number of conference
 * rooms required so that no two overlapping meetings share the same room.
 */
public class MeetingRoomsII {

    // ================= PROBLEM =================
    // You are given a list of meeting intervals, each with a start and end time. Meetings that
    // overlap in time cannot use the same room. Find the minimum number of rooms needed so that
    // every meeting can be scheduled without a room conflict.
    // Example: intervals = [[0,30],[5,10],[15,20]]
    //   - [0,30] and [5,10] overlap -> need 2 rooms at that point.
    //   - [5,10] ends before [15,20] starts, so that room can be reused.
    // Expected output: 2
    //
    // ================= SIMPLE APPROACH =================
    // Compare every pair of meetings to see which ones overlap, then figure out, at any given
    // moment, the maximum number of meetings happening at the same time (this is the answer).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Checking every pair of meetings is O(n^2). For each meeting we would also need to figure
    // out how many other meetings are concurrently active, which is awkward to compute
    // correctly with a purely pairwise approach and does not scale for large inputs.
    //
    // ================= OPTIMIZED APPROACH =================
    // Two-pointer sweep over separately sorted start and end times:
    // 1) Extract all start times into one array and all end times into another array, and sort
    //    each array independently.
    // 2) Use two pointers, one walking through sorted starts and one through sorted ends.
    //    Walk through the starts in order; whenever the current start time is before the
    //    current earliest end time, a new room is needed (rooms++). Otherwise, a meeting has
    //    already ended by the time this one starts, so that room can be reused (advance the end
    //    pointer, no new room needed).
    // 3) Track the running count of rooms in use, and keep the maximum seen - that maximum is
    //    the minimum number of rooms required.
    // (Alternative: push each meeting's end time onto a min-heap as we scan meetings sorted by
    // start time; if the heap's smallest end time is <= the current meeting's start, pop it
    // and reuse that room, otherwise add a new room. The heap size at the end is the answer.)
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting start times and end times separately turns "how many meetings overlap at once"
    // into a simple counting sweep: every time we cross a start we increment concurrent
    // meetings, every time we cross an end (that happened before or at this start) we
    // decrement. Two simple sorted int arrays are enough - no need for a heap in this variant,
    // which keeps the implementation O(n log n) time and O(n) space with very little overhead.
    //
    // ================= EDGE CASES =================
    // - Empty meeting list: 0 rooms needed.
    // - Single meeting: 1 room needed.
    // - All meetings overlap with each other: rooms needed equals the number of meetings.
    // - No meetings overlap at all: only 1 room needed.
    // - Meetings that touch exactly at the boundary (one ends exactly when another starts):
    //   commonly treated as NOT overlapping (the room can be reused), so the condition uses
    //   "start < end" rather than "start <= end" to decide if a new room is needed.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) - dominated by sorting the start and end time arrays; the
    // sweep itself afterward is O(n).
    // Space Complexity: O(n) for the two extracted and sorted start/end arrays.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you also report the actual room assignment for each meeting, not just the count?
    // - Compare the two-pointer sweep approach vs the min-heap approach - what are the tradeoffs?
    // - How would you handle meetings that touch at the boundary if that SHOULD count as an overlap?
    // - How would you extend this to track room usage over time as meetings are added/removed dynamically?
    // - What if meetings can be rescheduled slightly to reduce the number of rooms needed - how would you approach that (different, harder problem)?
    // - How does this problem relate to "merge intervals" and "maximum overlapping intervals" in general?
    // - How would you solve this if intervals arrive as a live stream instead of all at once?

    // Optimized: two-pointer sweep over separately sorted start and end times.
    public static int minMeetingRooms(int[][] intervals) {
        int n = intervals.length;
        if (n == 0) {
            return 0;
        }

        // Step 1: extract and sort start times and end times independently.
        int[] starts = new int[n];
        int[] ends = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i] = intervals[i][1];
        }
        Arrays.sort(starts);
        Arrays.sort(ends);

        // Step 2: sweep - a new room is needed whenever the next meeting starts before the
        // earliest currently-running meeting ends; otherwise that room is freed up and reused.
        int rooms = 0;
        int maxRooms = 0;
        int startPointer = 0;
        int endPointer = 0;

        while (startPointer < n) {
            if (starts[startPointer] < ends[endPointer]) {
                // This meeting starts before the earliest ongoing meeting ends - needs a new room.
                rooms++;
                startPointer++;
            } else {
                // A room has freed up (a meeting ended by the time this one starts) - reuse it.
                rooms--;
                endPointer++;
            }
            maxRooms = Math.max(maxRooms, rooms);
        }

        return maxRooms;
    }

    public static void main(String[] args) {
        int[][] intervals1 = {{0, 30}, {5, 10}, {15, 20}};
        // Expected: 2
        System.out.println("Input: [[0,30],[5,10],[15,20]]");
        System.out.println("Output: " + minMeetingRooms(intervals1));

        int[][] intervals2 = {{7, 10}, {2, 4}};
        // Expected: 1 (no overlap)
        System.out.println("\nInput: [[7,10],[2,4]]");
        System.out.println("Output: " + minMeetingRooms(intervals2));

        int[][] intervals3 = {};
        // Expected: 0 (empty input)
        System.out.println("\nInput: [] (empty)");
        System.out.println("Output: " + minMeetingRooms(intervals3));
    }
}
