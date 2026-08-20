package com.playground.java.interview.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Backtracking / Combination Sum with Duplicate Handling
 * PRIORITY: P2 - Commonly Asked
 * PROBLEM STATEMENT: Given a list of candidate numbers (may contain duplicates) and a target,
 * find all unique combinations where the candidates sum to the target, using each candidate
 * number at most once (per its position in the array).
 */
public class CombinationSumII {

    // ================= PROBLEM =================
    // You are given an array of candidate numbers (which may contain duplicates) and a target
    // sum. Find all unique combinations of candidates that add up to the target. Each number in
    // the array may be used AT MOST ONCE per combination (unlike the classic Combination Sum,
    // where a number can be reused unlimited times). The result must not contain duplicate
    // combinations.
    // Example: candidates = [10,1,2,7,6,1,5], target = 8
    //          -> output = [[1,1,6],[1,2,5],[1,7],[2,6]]
    //
    // ================= SIMPLE APPROACH =================
    // Generate every possible subset of the array (include/exclude each element by its
    // position), check which subsets sum to the target, then de-duplicate the resulting
    // combinations using a HashSet of sorted combinations.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Generating all 2^n subsets and filtering/de-duplicating afterward wastes time exploring
    // and storing many subsets that either don't sum to target or are exact duplicates of
    // combinations already found - both of these can be pruned or avoided entirely during
    // generation instead.
    //
    // ================= OPTIMIZED APPROACH =================
    // Sort the candidates first (so duplicates become adjacent, and so we can prune early once
    // the remaining target goes negative). Then backtrack with TWO combined rules:
    // 1) "Skip duplicate siblings at the same depth" (same rule as Subsets II): if
    //    candidates[i] == candidates[i-1] AND i > start, skip it - this avoids generating the
    //    same combination twice when duplicate VALUES exist in the array.
    // 2) "Use each array position at most once": always recurse with start = i + 1 (not i),
    //    unlike the classic Combination Sum which recurses with start = i to allow reuse.
    // At each recursive call:
    // - If remainingTarget == 0, the current path is a valid combination - record it.
    // - Loop from `start` to the end: skip duplicate siblings (rule 1); if candidates[i] >
    //   remainingTarget, break entirely (since the array is sorted, everything after is even
    //   bigger - no point continuing); otherwise include candidates[i], recurse with
    //   start = i+1 and remainingTarget - candidates[i], then backtrack.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting up front enables two separate optimizations at once: it groups duplicate values
    // together so the "skip sibling duplicates" check works with a simple adjacent comparison,
    // AND it allows an early break (not just continue) once a candidate exceeds the remaining
    // target, since every candidate after it in sorted order is at least as large. This combined
    // pruning is what keeps the backtracking search efficient despite duplicates and the
    // "each element used once" constraint.
    //
    // ================= EDGE CASES =================
    // - No candidate combination sums to target: result is an empty list.
    // - Multiple identical candidate values needed together (e.g. [1,1,6] using both 1s):
    //   handled correctly since duplicates are only skipped as SIBLING choices at the same
    //   depth, not when going deeper (i > start vs recursing to i+1 are different mechanisms).
    // - Candidate value itself larger than target: naturally excluded via the early break.
    // - target = 0: the only valid combination is the empty combination (if that's considered valid per problem statement) - typically target > 0 is assumed in this problem's constraints.
    // - All candidates identical and summing exactly to target with all of them: exactly one combination uses all of them.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(2^n) in the worst case for the backtracking search space, though the
    // sorted-array early-break and duplicate-skipping prune this significantly in practice;
    // sorting itself costs O(n log n).
    // Space Complexity: O(n) for the recursion depth and current path (excluding the output
    // list, which can hold an exponential number of combinations in the worst case).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does this differ from the classic Combination Sum (where elements can be reused unlimited times) - specifically, what changes in the recursive call (start=i vs start=i+1)?
    // - Why does the duplicate-skip rule use "i > start" and not just "nums[i] == nums[i-1]" globally?
    // - Why is `break` valid here (once candidates[i] > remainingTarget) but only because the array is sorted - what would happen if it weren't sorted?
    // - How would you count just the NUMBER of valid combinations without generating them, using DP instead?
    // - How would you extend this if there's also a maximum allowed number of elements per combination?
    // - What's the relationship between Combination Sum II, Subsets II, and Permutations II in terms of the duplicate-handling pattern?

    // Optimized: sort first, then backtrack with duplicate-skip + "each index used once".
    public static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates);
        backtrack(candidates, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(int[] candidates, int remainingTarget, int start,
                                   List<Integer> currentPath, List<List<Integer>> result) {
        if (remainingTarget == 0) {
            // Step: found a valid combination summing exactly to the target.
            result.add(new ArrayList<>(currentPath));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (i > start && candidates[i] == candidates[i - 1]) {
                // Step: skip a duplicate value used as a sibling choice at this same depth.
                continue;
            }
            if (candidates[i] > remainingTarget) {
                // Step: sorted array - everything from here on is even bigger, stop early.
                break;
            }
            currentPath.add(candidates[i]);
            // Step: recurse with start = i+1 so this array position isn't reused.
            backtrack(candidates, remainingTarget - candidates[i], i + 1, currentPath, result);
            currentPath.remove(currentPath.size() - 1);
        }
    }

    public static void main(String[] args) {
        int[] candidates1 = {10, 1, 2, 7, 6, 1, 5};
        int target1 = 8;
        // Expected: [[1,1,6], [1,2,5], [1,7], [2,6]]
        System.out.println("Input: candidates=[10,1,2,7,6,1,5], target=8");
        System.out.println("Output: " + combinationSum2(candidates1, target1));

        int[] candidates2 = {2, 5, 2, 1, 2};
        int target2 = 5;
        // Expected: [[1,2,2], [5]]
        System.out.println("\nInput: candidates=[2,5,2,1,2], target=5");
        System.out.println("Output: " + combinationSum2(candidates2, target2));

        int[] candidates3 = {5};
        int target3 = 3;
        // Expected: [] (no combination sums to target)
        System.out.println("\nInput: candidates=[5], target=3 (no valid combination)");
        System.out.println("Output: " + combinationSum2(candidates3, target3));
    }
}
