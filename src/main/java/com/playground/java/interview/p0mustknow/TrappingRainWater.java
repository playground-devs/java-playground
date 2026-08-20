package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Two Pointers / Precomputed Arrays
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Given bar heights forming an elevation map, compute how much rain water gets trapped between the bars.
 */
// ================= PROBLEM =================
// We are given an array of non-negative integers representing the height of bars,
// each of width 1, standing side by side. After it rains, water gets trapped in the
// dips between bars. We need to compute the total amount of trapped water.
//
// Example: height = [0,1,0,2,1,0,1,3,2,1,2,1]
// Water trapped at each index depends on the tallest bar to its left and the tallest
// bar to its right. Total trapped water -> Output: 6
//
// ================= SIMPLE APPROACH =================
// For every index i, water trapped on top of bar i = min(max height to the left of i,
// max height to the right of i) - height[i], but only if that value is positive.
// The brute force way: for each index, scan left to find the max, scan right to find
// the max, then compute the trapped amount. Sum all of these up.
//
// ================= WHY IT'S NOT ENOUGH =================
// For every index we do a full left scan and a full right scan, so this is O(n) work
// per index, O(n^2) overall. For large inputs this is too slow.
// A quick improvement is to precompute leftMax[] and rightMax[] arrays in O(n) time
// each, so each index lookup becomes O(1). This gets total time down to O(n), but it
// uses O(n) extra space for the two arrays.
//
// ================= OPTIMIZED APPROACH =================
// We can do it in O(n) time and O(1) extra space using two pointers.
// Keep a left pointer starting at index 0 and a right pointer at the last index.
// Track leftMax (max height seen so far from the left) and rightMax (max height seen
// so far from the right).
// At each step, compare height[left] and height[right]:
//   - If height[left] < height[right], we know the water level at "left" is bounded by
//     leftMax (because there's a taller bar somewhere to the right, specifically rightMax
//     or beyond, guaranteed since height[right] >= height[left] and rightMax >= height[right]).
//     So update leftMax, add (leftMax - height[left]) to the total, and move left forward.
//   - Otherwise, do the symmetric thing on the right side: update rightMax, add
//     (rightMax - height[right]) to total, and move right backward.
// This works because whichever side has the smaller current height is the side whose
// trapped water we can safely finalize -- the taller side guarantees a wall high enough.
//
// ================= WHY THIS DATA STRUCTURE =================
// No hash maps or stacks are needed. Two integer pointers plus two running max
// variables are enough because water trapped at any position only depends on the
// running maximum from each side, and we can decide which side is "safe" to finalize
// by comparing current heights. This avoids the O(n) space of storing full leftMax[]
// and rightMax[] arrays, which the "in-between" precomputed-array approach requires.
// A monotonic stack is an alternative approach (processing bar by bar, popping when a
// taller bar is found) but two pointers is simpler and uses less space for this problem.
//
// ================= EDGE CASES =================
// - Empty array or array with fewer than 3 bars: no water can be trapped, answer is 0.
// - All bars the same height: no dips, answer is 0.
// - Strictly increasing or strictly decreasing heights: no water trapped, answer is 0.
// - Bars with height 0 mixed in: still handled correctly, they just trap water up to
//   the surrounding max.
// - Very tall single spike in the middle: most water trapped near it, formula still holds.
//
// ================= COMPLEXITY =================
// Time Complexity: Brute force with left/right max arrays: O(n) to build both arrays,
//                   O(n) to sum, so O(n) overall (naive per-index scanning version is O(n^2)).
//                   Optimized two-pointer: O(n), single pass, each pointer moves at most n times total.
// Space Complexity: Brute force with arrays: O(n) extra space for leftMax[] and rightMax[].
//                    Optimized two-pointer: O(1) extra space, only a few variables.
//
// ================= INTERVIEW FOLLOW-UPS =================
// - Why is it safe to finalize the water on the side with the smaller current height?
// - How would you solve this using a monotonic decreasing stack instead? Walk through the logic.
// - How does this problem relate to "Container With Most Water"? Why can't we use the exact same two-pointer movement rule for both?
// - Can you extend this to a 2D version (trapping rain water on a height map / grid)? What data structure would you need there (hint: priority queue + BFS)?
// - How would you handle streaming input where bars arrive one at a time and you must report trapped water so far?
// - What if heights could be negative or the input is null? How would you validate?
// - Can you compute the water trapped at a single specific index without processing the whole array first?

import java.util.Arrays;

public class TrappingRainWater {

    // Brute force / precomputed-array approach: O(n) time, O(n) space
    public static int trapWithArrays(int[] height) {
        int n = height.length;
        if (n < 3) {
            return 0; // need at least 3 bars to trap any water
        }

        int[] leftMax = new int[n];
        int[] rightMax = new int[n];

        // fill leftMax: tallest bar from index 0 up to i
        leftMax[0] = height[0];
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i - 1], height[i]);
        }

        // fill rightMax: tallest bar from index n-1 down to i
        rightMax[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i + 1], height[i]);
        }

        // water at each index is bounded by the shorter of the two walls
        int totalWater = 0;
        for (int i = 0; i < n; i++) {
            int waterLevel = Math.min(leftMax[i], rightMax[i]);
            totalWater += waterLevel - height[i];
        }
        return totalWater;
    }

    // Optimized: two pointers, O(n) time, O(1) space
    public static int trapOptimized(int[] height) {
        int n = height.length;
        if (n < 3) {
            return 0;
        }

        int left = 0;
        int right = n - 1;
        int leftMax = 0;
        int rightMax = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                // the right side has a taller (or equal) wall, so left side is safe to finalize
                if (height[left] >= leftMax) {
                    leftMax = height[left]; // new tallest wall seen from left
                } else {
                    totalWater += leftMax - height[left]; // trapped water above this bar
                }
                left++;
            } else {
                // the left side has a taller (or equal) wall, so right side is safe to finalize
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    totalWater += rightMax - height[right];
                }
                right--;
            }
        }
        return totalWater;
    }

    public static void main(String[] args) {
        int[] example1 = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        // Expected: 6
        System.out.println("Input: " + Arrays.toString(example1));
        System.out.println("Trap with arrays: " + trapWithArrays(example1));
        System.out.println("Trap optimized: " + trapOptimized(example1));

        int[] example2 = {4, 2, 0, 3, 2, 5};
        // Expected: 9
        System.out.println("\nInput: " + Arrays.toString(example2));
        System.out.println("Trap with arrays: " + trapWithArrays(example2));
        System.out.println("Trap optimized: " + trapOptimized(example2));

        int[] example3 = {}; // edge case: empty array
        // Expected: 0
        System.out.println("\nInput: " + Arrays.toString(example3));
        System.out.println("Trap with arrays: " + trapWithArrays(example3));
        System.out.println("Trap optimized: " + trapOptimized(example3));
    }
}
