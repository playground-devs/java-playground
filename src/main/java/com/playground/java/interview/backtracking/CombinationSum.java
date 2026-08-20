package com.playground.java.interview.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Backtracking / Combination Sum with Unlimited Reuse
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Given a list of distinct candidate numbers and a target, find all unique
 * combinations where the candidates sum to the target, where each candidate can be reused an
 * unlimited number of times.
 */
public class CombinationSum {

    // ================= PROBLEM =================
    // You are given an array of distinct integers (candidates) and a target sum. Find all
    // unique combinations of candidates that sum to the target. The SAME candidate number may
    // be chosen an UNLIMITED number of times within a combination. The result must not contain
    // duplicate combinations (as sets of numbers, regardless of order).
    // Example: candidates = [2, 3, 6, 7], target = 7 -> output = [[2,2,3],[7]]
    // Example: candidates = [2, 3, 5], target = 8 -> output = [[2,2,2,2],[2,3,3],[3,5]]
    //
    // ================= SIMPLE APPROACH =================
    // Generate every possible sequence of candidate picks (allowing repeats) up to some depth,
    // check which sequences sum to the target, then de-duplicate results that are the same
    // multiset of numbers but in a different order (e.g. [2,3,2] and [2,2,3] would otherwise be
    // counted as separate answers).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Generating every ordered sequence explores permutations of the same combination
    // repeatedly (multiple orderings of the same multiset), wasting time on duplicate work that
    // can be avoided entirely by enforcing a canonical (non-decreasing) order during generation
    // instead of filtering duplicates afterward.
    //
    // ================= OPTIMIZED APPROACH =================
    // Sort the candidates first (not strictly required for correctness here since candidates are
    // distinct, but it enables an early-exit optimization). Then backtrack with a start index
    // that prevents generating permutation-order duplicates:
    // - At each recursive call, loop from `start` to the end of candidates.
    // - If remainingTarget == 0, record the current path as a valid combination.
    // - If candidates[i] > remainingTarget, break early (sorted array, nothing further fits).
    // - Otherwise, include candidates[i] and recurse with start = i (NOT i + 1) - this is the key
    //   difference from Combination Sum II: reusing `i` as the next start allows the same
    //   candidate to be picked again, while still forbidding "going backwards" to an earlier
    //   candidate, which is exactly what prevents permutation-order duplicates like [2,3] and
    //   [3,2] from both being generated.
    // - Backtrack (remove the last added candidate) before trying the next candidate in the loop.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No special data structure beyond a simple list (used as a stack for the current path) is
    // needed - the crucial design choice is purely in how the recursion's start index is
    // threaded through: reusing `i` (not `i+1`) is what enables unlimited reuse of the same
    // candidate, while still enforcing a non-decreasing selection order that eliminates
    // duplicate combinations without needing a HashSet-based de-duplication pass.
    //
    // ================= EDGE CASES =================
    // - No candidates sum to target: result is an empty list.
    // - A single candidate exactly equals the target: that candidate alone is a valid
    //   combination (e.g. [7] when target = 7 and 7 is a candidate).
    // - Smallest candidate used many times to reach target (e.g. [2,2,2,2] for target 8 with
    //   candidates [2,3,5]): handled naturally since reuse is unlimited via start = i.
    // - target = 0: only the empty combination is valid (again, dependent on problem's exact
    //   constraints - typically target > 0 is assumed).
    // - Candidate value itself larger than target: naturally excluded via the early break.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: exponential in the worst case (number of combinations can grow quickly
    // with unlimited reuse), bounded roughly by O(2^target) in the worst case for small
    // candidate values; sorting itself costs O(n log n).
    // Space Complexity: O(target / minCandidate) for the maximum recursion depth and current
    // path (excluding the output list itself).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does this differ from Combination Sum II (candidates may contain duplicates, each used at most once) - specifically the start=i vs start=i+1 distinction?
    // - Why is sorting not strictly required for correctness here (since candidates are distinct), but still useful for the early-break optimization?
    // - How would you count just the NUMBER of valid combinations without generating them, using DP (this becomes the "Coin Change II" style problem)?
    // - How would you modify this if there's also a maximum allowed number of elements per combination?
    // - What happens to the recursion depth and combination count if a candidate of value 1 is present?
    // - How would you adapt this to find just ONE valid combination (or determine none exists) instead of all of them?

    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
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
            if (candidates[i] > remainingTarget) {
                // Step: sorted array - everything from here on is even bigger, stop early.
                break;
            }
            currentPath.add(candidates[i]);
            // Step: recurse with start = i (not i+1) so this candidate can be reused.
            backtrack(candidates, remainingTarget - candidates[i], i, currentPath, result);
            currentPath.remove(currentPath.size() - 1);
        }
    }

    public static void main(String[] args) {
        int[] candidates1 = {2, 3, 6, 7};
        int target1 = 7;
        // Expected: [[2,2,3],[7]]
        System.out.println("Input: candidates=[2,3,6,7], target=7");
        System.out.println("Output: " + combinationSum(candidates1, target1));

        int[] candidates2 = {2, 3, 5};
        int target2 = 8;
        // Expected: [[2,2,2,2],[2,3,3],[3,5]]
        System.out.println("\nInput: candidates=[2,3,5], target=8");
        System.out.println("Output: " + combinationSum(candidates2, target2));

        int[] candidates3 = {5};
        int target3 = 3;
        // Expected: [] (no combination can sum to target)
        System.out.println("\nInput: candidates=[5], target=3 (no valid combination)");
        System.out.println("Output: " + combinationSum(candidates3, target3));
    }
}
