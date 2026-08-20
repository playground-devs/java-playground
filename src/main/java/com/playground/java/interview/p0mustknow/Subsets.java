package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Backtracking
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given an array of distinct integers, return all possible subsets (the power set).
 */
public class Subsets {

    // ================= PROBLEM =================
    // You are given an array of distinct integers.
    // You need to return every possible subset, including the empty subset and the
    // full array itself.
    // Example: nums = [1, 2, 3]
    // Output -> [], [1], [2], [1,2], [3], [1,3], [2,3], [1,2,3]  (order can vary)
    // There are 2^n total subsets for n distinct numbers, since each number is either
    // included or excluded.
    //
    // ================= SIMPLE APPROACH =================
    // The naive full enumeration idea: for each number in the array, make a binary
    // decision - include it in the current subset, or exclude it - then move to the
    // next number. This builds a decision tree of depth n where every path from root
    // to leaf represents one subset (a sequence of include/exclude choices).
    // At the leaves (after deciding for every number), record the subset built so far.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Not applicable in the DP "overlapping subproblems" sense - there are genuinely
    // 2^n distinct subsets to produce, so no smaller answer set exists. This is the
    // actual optimal solution; backtracking is simply the efficient way to enumerate all
    // 2^n outcomes using a single shared mutable path (instead of copying arrays or
    // building new lists at every decision), avoiding wasted allocation work.
    //
    // ================= OPTIMIZED APPROACH =================
    // Backtracking (include/exclude decision tree):
    //   1. At every recursive call, first record the currentPath as a valid subset
    //      (every prefix of decisions is itself a complete subset - not just the leaves).
    //   2. Then, for each number starting at the current index:
    //        a. Choose it: add nums[i] to currentPath.
    //        b. Explore: recurse with index i+1 (never revisit earlier indices - this
    //           avoids generating duplicate subsets like [1,2] and [2,1]).
    //        c. Un-choose (backtrack): remove nums[i] from currentPath.
    //
    // Alternative (iterative bit-manipulation trick, mentioned briefly):
    // Since each number is either "in" or "out", every subset corresponds to one
    // n-bit binary number from 0 to 2^n - 1. For mask = 0 .. 2^n-1, bit j of mask
    // tells us whether nums[j] is included in that subset. This avoids recursion
    // entirely and is shown below as subsetsUsingBitmask().
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Recursion models "decide in/out for this number, then solve the rest" naturally,
    // with the call stack tracking which index we are deciding on.
    // We use ONE mutable currentPath list across the whole recursion instead of copying
    // arrays at each call because add()/remove() are O(1) amortized, while copying a
    // list at every node of a tree with 2^n nodes would multiply the cost unnecessarily.
    // The "backtrack" (remove) step after the recursive call restores currentPath to
    // its state before we chose nums[i], so the loop can correctly try excluding it
    // (i.e., move to i+1 without nums[i]) using the very same list object.
    //
    // ================= EDGE CASES =================
    // - Empty array: exactly one subset - the empty subset [].
    // - Single element array: exactly two subsets - [] and [that element].
    // - All subsets must include the empty set and the full set.
    // - Larger arrays: result size grows exponentially (2^n), so this only scales to small n.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * 2^n) - there are 2^n subsets, and copying each subset
    // (up to length n) into the result list costs O(n).
    // Space Complexity: O(n) extra space for the recursion stack and currentPath
    // (not counting the O(n * 2^n) needed to store all output subsets).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you handle duplicate numbers in the input (Subsets II) without
    //   producing duplicate subsets?
    // - How does the bitmask approach compare in time/space to backtracking?
    // - Can you generate subsets of a specific size k only, more efficiently?
    // - How would you modify this to generate subsets lazily/on demand (an iterator)?
    // - Why do we record the subset at every node of the recursion, not just the leaves?
    // - How does this backtracking template relate to the one used for Permutations?
    // - What changes if the array can contain up to 20-30 elements - is 2^n still feasible?

    // Backtracking approach: include/exclude decision tree.
    public static List<List<Integer>> subsetsUsingBacktracking(int[] nums) {
        List<List<Integer>> results = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>();
        backtrack(nums, 0, currentPath, results);
        return results;
    }

    private static void backtrack(int[] nums, int startIndex,
                                   List<Integer> currentPath, List<List<Integer>> results) {
        // Step: every point in the recursion (not just leaves) is a valid subset - record it.
        results.add(new ArrayList<>(currentPath));

        for (int i = startIndex; i < nums.length; i++) {
            // Choose: include nums[i] in the current subset.
            currentPath.add(nums[i]);

            // Explore: move forward only (i+1), never revisit earlier indices,
            // this guarantees each subset is generated exactly once.
            backtrack(nums, i + 1, currentPath, results);

            // Un-choose (backtrack): remove nums[i] so we can try excluding it
            // and move on to the next candidate at this level.
            currentPath.remove(currentPath.size() - 1);
        }
    }

    // Iterative bit-manipulation alternative: each subset corresponds to a bitmask
    // from 0 to 2^n - 1. Bit j of the mask tells us whether nums[j] is included.
    public static List<List<Integer>> subsetsUsingBitmask(int[] nums) {
        List<List<Integer>> results = new ArrayList<>();
        int n = nums.length;
        int totalSubsets = 1 << n; // 2^n

        for (int mask = 0; mask < totalSubsets; mask++) {
            List<Integer> subset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                // Step: check if bit j is set in mask - if so, include nums[j].
                if ((mask & (1 << j)) != 0) {
                    subset.add(nums[j]);
                }
            }
            results.add(subset);
        }
        return results;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3};
        // Expected: 8 subsets (2^3), including [] and [1,2,3]
        System.out.println("Input: nums=[1,2,3]");
        System.out.println("Backtracking approach: " + subsetsUsingBacktracking(nums1));
        System.out.println("Bitmask approach:      " + subsetsUsingBitmask(nums1));

        int[] nums2 = {5};
        // Expected: [[], [5]]
        System.out.println("\nInput: nums=[5] (single element edge case)");
        System.out.println("Backtracking approach: " + subsetsUsingBacktracking(nums2));

        int[] nums3 = {};
        // Expected: [[]]  (just the empty subset)
        System.out.println("\nInput: nums=[] (empty array edge case)");
        System.out.println("Backtracking approach: " + subsetsUsingBacktracking(nums3));
    }
}
