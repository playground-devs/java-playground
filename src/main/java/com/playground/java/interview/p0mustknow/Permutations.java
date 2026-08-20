package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Backtracking
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given an array of distinct integers, return all possible permutations.
 */
public class Permutations {

    // ================= PROBLEM =================
    // You are given an array of distinct integers.
    // You need to produce every possible ordering (permutation) of these numbers.
    // Example: nums = [1, 2, 3]
    // Output -> [1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], [3,2,1]
    // There are n! (n factorial) total permutations for n distinct numbers.
    //
    // ================= SIMPLE APPROACH =================
    // The naive idea is full enumeration: at every position in the result, try placing
    // every number that has not been used yet, then recurse to fill the next position.
    // When the current path length equals the array length, we have a complete permutation
    // - record it. This is basically building a decision tree of depth n where each level
    // branches into "remaining unused numbers" choices. There is no way to reduce this
    // further because every distinct arrangement genuinely needs to be produced - the
    // "brute force" and the "real" solution for permutations are the same core idea,
    // backtracking is simply the clean, efficient way to implement that full enumeration
    // (no partial results are duplicated or wasted).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Not applicable in the classic "overlapping subproblems" sense (that concept applies
    // to DP problems like Fibonacci or Coin Change). For permutations, the count of valid
    // answers is inherently n!, so there is no smaller optimal solution - every permutation
    // must be visited once. The concern here is not "redundant recomputation" but
    // "efficient construction without wasted copying" - which is exactly why we backtrack
    // using a shared mutable list instead of copying arrays at every recursive call.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use backtracking with a "used[]" boolean array (an alternative is the swap-based
    // in-place technique, shown as a second method below).
    // Maintain one mutable "currentPath" list shared across the whole recursion:
    //   1. If currentPath size == nums.length, we have a full permutation - copy it into results.
    //   2. Otherwise, for each number not yet used:
    //        a. Choose it: add to currentPath, mark used[i] = true.
    //        b. Explore: recurse to fill the next position.
    //        c. Un-choose (backtrack): remove it from currentPath, mark used[i] = false.
    // This "choose -> explore -> un-choose" pattern is the heart of backtracking:
    // after trying a candidate and recursing, we undo the choice so that the SAME
    // currentPath list can be reused to try the next candidate at this position,
    // without any copying.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Recursion naturally represents "choose a number for this position, then solve the
    // rest of the positions" - each recursive call is one decision point in the tree.
    // We use ONE mutable list (currentPath) instead of creating a new list/array copy at
    // every recursive call because copying is O(n) extra work and O(n) extra memory at
    // every node of the recursion tree. By using add() before recursing and remove()
    // (the "backtrack" step) after recursing, we reuse the same memory for the entire
    // depth-first traversal - this is the standard, efficient backtracking template.
    // The used[] boolean array gives O(1) lookup to check "is this number already placed
    // in the current path", instead of scanning currentPath (O(n)) each time.
    //
    // ================= EDGE CASES =================
    // - Empty array: only one permutation exists - the empty permutation [].
    // - Single element array: exactly one permutation - the array itself.
    // - Array with 2 elements: exactly 2 permutations (2! = 2).
    // - Larger arrays: result size grows factorially (n!), so this only scales to small n.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * n!) - there are n! permutations, and building/copying each
    // completed permutation of length n takes O(n) time.
    // Space Complexity: O(n) extra space for the recursion stack and the used[] array
    // (not counting the O(n * n!) needed to store all output permutations).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you handle duplicate numbers in the input (Permutations II)?
    // - Can you do this without extra used[] space, using in-place swapping instead?
    // - How would you generate permutations lazily/iteratively (e.g. next_permutation style)?
    // - What is the time complexity if you need to print/process each permutation instead
    //   of storing them all in memory?
    // - How would you generate only the k-th permutation without generating all of them?
    // - How does this pattern generalize to combinations and subsets?
    // - Why do we need to backtrack (undo) after each recursive call - what breaks if we don't?

    // Approach 1: backtracking using a "used[]" boolean array to track chosen numbers.
    public static List<List<Integer>> permuteUsingUsedArray(int[] nums) {
        List<List<Integer>> results = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        backtrackWithUsedArray(nums, used, currentPath, results);
        return results;
    }

    private static void backtrackWithUsedArray(int[] nums, boolean[] used,
                                                List<Integer> currentPath,
                                                List<List<Integer>> results) {
        // Step: if the path is as long as nums, we built one full permutation.
        if (currentPath.size() == nums.length) {
            results.add(new ArrayList<>(currentPath)); // copy, since currentPath keeps changing
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) {
                continue; // step: skip numbers already placed in this path
            }
            // Choose: place nums[i] and mark it used.
            currentPath.add(nums[i]);
            used[i] = true;

            // Explore: recurse to fill the next position.
            backtrackWithUsedArray(nums, used, currentPath, results);

            // Un-choose (backtrack): undo the choice so the next candidate can be tried.
            currentPath.remove(currentPath.size() - 1);
            used[i] = false;
        }
    }

    // Approach 2: backtracking using in-place swapping (no extra used[] array needed).
    public static List<List<Integer>> permuteUsingSwap(int[] nums) {
        List<List<Integer>> results = new ArrayList<>();
        backtrackWithSwap(nums, 0, results);
        return results;
    }

    private static void backtrackWithSwap(int[] nums, int startIndex, List<List<Integer>> results) {
        // Step: once startIndex reaches the end, nums is one full permutation.
        if (startIndex == nums.length) {
            List<Integer> permutation = new ArrayList<>();
            for (int num : nums) {
                permutation.add(num);
            }
            results.add(permutation);
            return;
        }
        for (int i = startIndex; i < nums.length; i++) {
            // Choose: swap the i-th element into the startIndex position.
            swap(nums, startIndex, i);

            // Explore: recurse on the remaining positions.
            backtrackWithSwap(nums, startIndex + 1, results);

            // Un-choose (backtrack): swap back to restore the original order.
            swap(nums, startIndex, i);
        }
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3};
        // Expected: 6 permutations of [1,2,3]
        System.out.println("Input: nums=[1,2,3]");
        System.out.println("Used-array approach: " + permuteUsingUsedArray(nums1));
        System.out.println("Swap-based approach: " + permuteUsingSwap(new int[]{1, 2, 3}));

        int[] nums2 = {0};
        // Expected: [[0]]
        System.out.println("\nInput: nums=[0] (single element edge case)");
        System.out.println("Used-array approach: " + permuteUsingUsedArray(nums2));

        int[] nums3 = {};
        // Expected: [[]]  (one empty permutation)
        System.out.println("\nInput: nums=[] (empty array edge case)");
        System.out.println("Used-array approach: " + permuteUsingUsedArray(nums3));
    }
}
