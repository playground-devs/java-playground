package com.playground.java.interview.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Backtracking / Subsets with Duplicate Handling
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given an integer array that may contain duplicates, return all possible
 * unique subsets (the power set, with no duplicate subsets in the output).
 */
public class SubsetsII {

    // ================= PROBLEM =================
    // You are given an array of integers that may contain duplicate values. Return all possible
    // subsets (the power set), but the result must not contain any duplicate subset, even
    // though duplicate elements exist in the input.
    // Example: nums = [1, 2, 2] -> output = [[],[1],[1,2],[1,2,2],[2],[2,2]]
    // (note: [1,2] appears only once even though there are two 2s that could each pair with 1).
    //
    // ================= SIMPLE APPROACH =================
    // Generate every subset the same way as the no-duplicates version (include/exclude each
    // element via backtracking), collect all subsets, then use a HashSet (of sorted subsets, or
    // some canonical string form) to filter out duplicate subsets afterward.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Generating all 2^n subsets first and then de-duplicating afterward does unnecessary work -
    // many of those generated subsets are duplicates that could have been skipped entirely
    // during generation, and hashing/comparing subsets for de-duplication adds extra overhead.
    //
    // ================= OPTIMIZED APPROACH =================
    // Sort the array first, so that duplicate values become adjacent. Then backtrack with a
    // "skip duplicates at the same recursion depth" rule:
    // 1) At each recursive call, first add the current path as one valid subset (every partial
    //    path, including the empty one, is itself a valid subset).
    // 2) Loop through candidates starting from the current `start` index. For each index i:
    //    - Skip it if nums[i] == nums[i-1] AND i > start (this is the key rule - it means we've
    //      already considered "starting a new choice at THIS depth" with this same value once
    //      before, so trying it again here would just re-produce a subset we already generated).
    //    - Otherwise, include nums[i] in the current path, recurse with start = i+1, then
    //      backtrack by removing nums[i] before moving to the next i.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting first is essential - it's what allows duplicate values to be detected as adjacent
    // in a single linear condition (nums[i] == nums[i-1]). The "i > start" condition
    // specifically distinguishes "using a duplicate value for the first time at this recursion
    // depth" (allowed) from "using the same duplicate value again as a sibling choice at this
    // same depth" (skipped) - this is the precise mechanism that prevents duplicate subsets
    // without needing any extra hashing or set-based de-duplication.
    //
    // ================= EDGE CASES =================
    // - No duplicates in the input at all: behaves identically to the standard Subsets problem.
    // - All elements identical (e.g. [2,2,2]): produces only n+1 distinct subsets ([], [2],
    //   [2,2], [2,2,2]) instead of 2^n raw combinations.
    // - Empty input array: only the empty subset exists.
    // - Duplicates not adjacent originally (e.g. [2,1,2]): sorting first is what makes the
    //   "nums[i] == nums[i-1]" check valid at all - without sorting, duplicates could be far
    //   apart and this check would fail to catch them.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n * 2^n) in the worst case - there can be up to 2^n subsets, and
    // copying each path into the result costs O(n); sorting up front costs O(n log n), dominated
    // by the subset generation.
    // Space Complexity: O(n) for the recursion depth and current path (excluding the space for
    // the output list itself, which can hold up to O(2^n) subsets).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the duplicate-skip condition check "i > start" specifically, rather than just "nums[i] == nums[i-1]" alone?
    // - How does this same duplicate-skip pattern apply to Permutations II and Combination Sum II?
    // - Could you solve this without sorting, using a HashSet-based de-duplication instead - what's the tradeoff?
    // - How would you count the number of DISTINCT subsets without generating them all explicitly (combinatorics with repeated elements)?
    // - How would you extend this to subsets of a fixed target size k, still avoiding duplicates?
    // - What's the relationship between this backtracking structure and generating subsets via bitmasking (0 to 2^n - 1)? Does bitmasking still work cleanly with duplicates?

    // Optimized: sort first, then backtrack skipping duplicate siblings at the same depth.
    public static List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        backtrack(nums, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(int[] nums, int start, List<Integer> currentPath, List<List<Integer>> result) {
        // Step: every partial path (including the empty one) is itself a valid subset.
        result.add(new ArrayList<>(currentPath));

        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) {
                // Step: skip a duplicate value used as a sibling choice at this same depth.
                continue;
            }
            currentPath.add(nums[i]);
            backtrack(nums, i + 1, currentPath, result);
            currentPath.remove(currentPath.size() - 1);
        }
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 2};
        // Expected: [[], [1], [1,2], [1,2,2], [2], [2,2]]
        System.out.println("Input: [1,2,2]");
        System.out.println("Output: " + subsetsWithDup(nums1));

        int[] nums2 = {0};
        // Expected: [[], [0]]
        System.out.println("\nInput: [0] (single element)");
        System.out.println("Output: " + subsetsWithDup(nums2));

        int[] nums3 = {2, 2, 2};
        // Expected: [[], [2], [2,2], [2,2,2]] (all duplicates collapse)
        System.out.println("\nInput: [2,2,2] (all identical)");
        System.out.println("Output: " + subsetsWithDup(nums3));
    }
}
