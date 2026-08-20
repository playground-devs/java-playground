package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Two Pointers
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Given heights of vertical lines, find two lines that together with the x-axis form the container holding the most water.
 */
// ================= PROBLEM =================
// We are given an array of numbers. Each number is the height of a vertical line
// drawn at that index on the x-axis. Pick any two lines. Together with the x-axis
// they form a container. We want to find the two lines that hold the maximum amount of water.
// The amount of water held = width between the two lines * the shorter of the two heights.
// (We use the shorter height because water spills over the shorter wall.)
//
// Example: height = [1,8,6,2,5,4,8,3,7]
// Best pair is index 1 (height 8) and index 8 (height 7).
// Width = 8 - 1 = 7, height = min(8,7) = 7, area = 49 -> Output: 49
//
// ================= SIMPLE APPROACH =================
// Try every possible pair of lines (i, j) with i < j.
// For each pair compute area = (j - i) * min(height[i], height[j]).
// Keep track of the maximum area seen so far.
// This is a straightforward double for-loop, easy to write, easy to reason about.
//
// ================= WHY IT'S NOT ENOUGH =================
// Checking every pair takes O(n^2) time. For large arrays (say n = 100,000) this is
// 10 billion operations, way too slow for an interview-grade or production-grade solution.
// We are re-computing a lot of redundant work: once we know one side is short, we can
// reason about which pointer to move without checking all following pairs.
//
// ================= OPTIMIZED APPROACH =================
// Use two pointers, one starting at the very left (index 0), one at the very right (index n-1).
// At each step:
//   1. Compute the area between the two pointers.
//   2. Update the max area if this is bigger.
//   3. Move the pointer that points to the SHORTER line inward by one step.
// Why move the shorter line? Because the water level is capped by the shorter line.
// If we move the taller line inward, the width shrinks but the height cannot increase
// beyond the shorter line's height, so the area can only get worse or stay the same.
// Moving the shorter line at least gives a chance of finding a taller line, which could
// increase the area despite the smaller width.
// Repeat until both pointers meet. This explores the search space in O(n) time because
// each step moves one pointer inward and we never revisit a position.
//
// ================= WHY THIS DATA STRUCTURE =================
// No extra data structure is needed here, just two integer indices ("pointers") into
// the array. This works because the problem has a monotonic property: moving the
// pointer at the shorter line is always safe (never skips over the true optimal answer).
// A hash map or sorting would add overhead without helping, since the answer depends on
// the *original* index positions (width = index difference), not on sorted order of heights.
//
// ================= EDGE CASES =================
// - Array with fewer than 2 elements: no container can be formed, area is 0.
// - All heights are the same: area is just width * height, two pointers still works fine.
// - Array with two elements: only one possible container, that's the answer directly.
// - Heights containing 0: a line of height 0 can never help form water with anything, area is 0 for pairs involving it as the shorter side.
// - Very large array: optimized approach must be used to avoid timeout.
//
// ================= COMPLEXITY =================
// Time Complexity: Brute force O(n^2) because we check every pair.
//                  Optimized O(n) because each pointer moves inward at most n times total.
// Space Complexity: Brute force O(1) extra space (just tracking max).
//                    Optimized O(1) extra space (two pointers and a max variable only).
//
// ================= INTERVIEW FOLLOW-UPS =================
// - Why is it safe to always move the pointer at the shorter line, and never the taller one?
// - Can you prove the two-pointer approach never skips the optimal answer?
// - What if the array can have negative heights (i.e., not a valid input)? How would you validate input?
// - How would you modify this to also return the actual pair of indices, not just the max area?
// - What is the difference between this problem and "Trapping Rain Water"? Why does one use two pointers moving inward and the other track running max from both sides?
// - How would your solution change if you needed the top-K containers by area instead of just the max?
// - Could you solve this with a different data structure like a stack? Why or why not?

public class ContainerWithMostWater {

    // Brute force: check every pair of lines, O(n^2)
    public static int maxAreaBruteForce(int[] height) {
        int maxArea = 0;
        // try every pair (i, j)
        for (int i = 0; i < height.length; i++) {
            for (int j = i + 1; j < height.length; j++) {
                int width = j - i;
                int shorterHeight = Math.min(height[i], height[j]);
                int area = width * shorterHeight;
                maxArea = Math.max(maxArea, area);
            }
        }
        return maxArea;
    }

    // Optimized: two pointers from both ends, O(n)
    public static int maxAreaOptimized(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int maxArea = 0;

        while (left < right) {
            int width = right - left;
            int shorterHeight = Math.min(height[left], height[right]);
            int area = width * shorterHeight;
            maxArea = Math.max(maxArea, area);

            // move the pointer with the shorter line inward
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxArea;
    }

    public static void main(String[] args) {
        int[] example1 = {1, 8, 6, 2, 5, 4, 8, 3, 7};
        // Expected: 49 (lines at index 1 and index 8, width 7, height min(8,7)=7)
        System.out.println("Input: " + java.util.Arrays.toString(example1));
        System.out.println("Brute force max area: " + maxAreaBruteForce(example1));
        System.out.println("Optimized max area: " + maxAreaOptimized(example1));

        int[] example2 = {1, 1};
        // Expected: 1 (only one pair, width 1, height 1)
        System.out.println("\nInput: " + java.util.Arrays.toString(example2));
        System.out.println("Brute force max area: " + maxAreaBruteForce(example2));
        System.out.println("Optimized max area: " + maxAreaOptimized(example2));

        int[] example3 = {0};
        // Expected: 0 (single element, cannot form a container)
        System.out.println("\nInput: " + java.util.Arrays.toString(example3));
        System.out.println("Brute force max area: " + maxAreaBruteForce(example3));
        System.out.println("Optimized max area: " + maxAreaOptimized(example3));
    }
}
