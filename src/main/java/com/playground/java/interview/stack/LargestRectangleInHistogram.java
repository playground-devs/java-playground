package com.playground.java.interview.stack;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Monotonic Stack
 * PRIORITY: P1
 * PROBLEM STATEMENT: Given bar heights of a histogram, find the area of the largest rectangle that fits within the histogram.
 */
public class LargestRectangleInHistogram {

    // ================= PROBLEM =================
    // Given an array of integers representing the heights of histogram bars (each of width 1,
    // placed side by side), find the area of the largest rectangle that can be formed using
    // one or more contiguous bars.
    // Example: heights = [2,1,5,6,2,3] -> the largest rectangle uses bars [5,6] (indices 2-3),
    // height 5, width 2 -> Output: 10.
    //
    // ================= SIMPLE APPROACH =================
    // For every bar i, treat it as the shortest bar of a candidate rectangle: expand left and
    // right from i as far as possible while every bar in that range is >= heights[i], then the
    // candidate area is heights[i] * (width of that range). Track the maximum area seen.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // For each of the n bars, expanding left and right can take up to O(n) time in the worst
    // case (e.g. all bars the same height), giving O(n^2) total time. This repeats a lot of
    // work: many bars end up re-scanning overlapping ranges instead of reusing what was
    // already discovered about nearby bars.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a monotonic increasing stack of indices (heights at those indices increase as you
    // go from the bottom to the top of the stack).
    // Step 1: Walk through bars left to right. For each bar at index i:
    // Step 2: While the stack is non-empty AND heights[i] is less than the height at the
    //          index on top of the stack, pop that index. The popped bar can't extend any
    //          further right than i (since a shorter bar blocks it), so finalize its
    //          rectangle now: height = heights[popped], width = i - (new stack top index) - 1,
    //          or i if the stack is now empty (meaning popped was the shortest bar so far,
    //          so it extends all the way back to index 0).
    // Step 3: Push i onto the stack.
    // Step 4: After processing all bars, pop any remaining indices the same way, but use
    //          n (the array length) as the right boundary since nothing to their right was
    //          ever shorter.
    // Track the maximum area computed at every pop.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A monotonic increasing stack efficiently finds, for every bar, the nearest bar to the
    // left and right that is strictly shorter - and it does this in O(1) amortized time per
    // bar instead of the O(n) per bar that naive left/right expansion requires. This works
    // because the moment a shorter bar appears, every taller bar still on the stack has just
    // found its right boundary (the current shorter bar), and whatever remains below it on
    // the stack is automatically its left boundary (the nearest bar shorter than it that
    // hasn't been popped yet). Each index is pushed exactly once and popped at most once,
    // so the total work across the whole array is O(n).
    //
    // ================= EDGE CASES =================
    // - Empty array: no bars, answer is 0.
    // - Single bar: answer is that bar's height.
    // - All bars the same height: answer is height * n (one giant rectangle).
    // - Strictly increasing heights, e.g. [1,2,3,4,5]: nothing gets popped until the final sweep, best rectangle uses fewer, taller bars near the end.
    // - Strictly decreasing heights, e.g. [5,4,3,2,1]: every new bar immediately pops the previous one, best rectangle is likely the full width at the smallest height, or a taller partial width.
    // - Bars with height 0 mixed in: they should never contribute to a positive-area rectangle covering them.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n^2) because each bar can require an O(n) expansion.
    // Optimized O(n) because each index is pushed onto and popped from the stack at most once.
    // Space Complexity: Brute force O(1) extra space (just a running max).
    // Optimized O(n) worst case for the stack, e.g. strictly increasing heights push every index before any pops occur.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you adapt this to find the largest RECTANGLE in a 2D binary matrix (treat each row as a histogram)?
    // - Why does using a sentinel value of 0 (or -1) appended to the heights array simplify the final cleanup pass?
    // - Can you explain why each index is pushed and popped at most once, proving the O(n) bound?
    // - What would change if bars could have negative heights?
    // - How would you also return WHICH bars form the largest rectangle, not just the area?
    // - What's the difference between this problem and "Trapping Rain Water" - why does one need a monotonic increasing stack and the other a decreasing one (or two pointers)?
    // - How would you solve this using divide and conquer instead (find min in range, recurse left/right)? What's its complexity?

    // Brute force: for each bar, expand left/right while neighbors are tall enough. O(n^2).
    public static int largestRectangleBruteForce(int[] heights) {
        int n = heights.length;
        int maxArea = 0;

        for (int i = 0; i < n; i++) {
            int height = heights[i];
            int left = i;
            int right = i;

            // Expand left while the bar is tall enough.
            while (left > 0 && heights[left - 1] >= height) {
                left--;
            }
            // Expand right while the bar is tall enough.
            while (right < n - 1 && heights[right + 1] >= height) {
                right++;
            }

            int width = right - left + 1;
            maxArea = Math.max(maxArea, height * width);
        }

        return maxArea;
    }

    // Optimized: monotonic increasing stack of indices. O(n).
    public static int largestRectangleOptimized(int[] heights) {
        int n = heights.length;
        Deque<Integer> stack = new ArrayDeque<>(); // holds indices, heights increasing bottom to top
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            // Use height 0 as a virtual bar past the end to flush out the remaining stack.
            int currentHeight = (i == n) ? 0 : heights[i];

            // Pop and finalize rectangles while the current bar is shorter than the stack top.
            while (!stack.isEmpty() && currentHeight < heights[stack.peek()]) {
                int poppedIndex = stack.pop();
                int height = heights[poppedIndex];
                // Width extends from just after the new stack top to just before i.
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, height * width);
            }
            stack.push(i);
        }

        return maxArea;
    }

    public static void main(String[] args) {
        int[] heights1 = {2, 1, 5, 6, 2, 3};
        // Expected: 10 (bars [5,6], width 2)
        System.out.println("Input: " + java.util.Arrays.toString(heights1));
        System.out.println("Brute force: " + largestRectangleBruteForce(heights1));
        System.out.println("Optimized:   " + largestRectangleOptimized(heights1));

        int[] heights2 = {2, 4};
        // Expected: 4 (single bar of height 4, width 1; or two bars of height 2, width 2 -> both give 4)
        System.out.println("\nInput: " + java.util.Arrays.toString(heights2));
        System.out.println("Brute force: " + largestRectangleBruteForce(heights2));
        System.out.println("Optimized:   " + largestRectangleOptimized(heights2));

        int[] heights3 = {};
        // Expected: 0 (empty histogram)
        System.out.println("\nInput: " + java.util.Arrays.toString(heights3));
        System.out.println("Brute force: " + largestRectangleBruteForce(heights3));
        System.out.println("Optimized:   " + largestRectangleOptimized(heights3));
    }
}
