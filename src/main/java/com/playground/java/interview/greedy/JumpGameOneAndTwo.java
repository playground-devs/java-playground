package com.playground.java.interview.greedy;

import java.util.Arrays;

/**
 * PATTERN: Greedy
 * PRIORITY: P2
 * ONE-LINE PROBLEM STATEMENT: Determine if the last index is reachable (Jump Game I), and if so, find the minimum jumps to reach it (Jump Game II).
 */
public class JumpGameOneAndTwo {

    // ================= PROBLEM =================
    // Given an array nums where nums[i] is the maximum jump length from index i:
    // Jump Game I  - canJump(nums): can you reach the last index starting from index 0?
    // Jump Game II - minJumps(nums): assuming the end IS reachable, what is the minimum
    //                number of jumps needed to reach it?
    // Example: nums = [2,3,1,1,4] -> canJump = true, minJumps = 2 (jump 0->1 (len 2 available), then 1->4)
    // Example: nums = [3,2,1,0,4] -> canJump = false (index 3 has a 0 and traps you before reaching index 4)
    //
    // ================= SIMPLE APPROACH =================
    // Jump Game I: recursively/backtracking try every possible jump length from every
    // reachable position and see if any path reaches the end (exponential), or use DP where
    // dp[i] = true if index i is reachable, checking all earlier reachable indices that can
    // jump to i (O(n^2)).
    // Jump Game II: DP where dp[i] = minimum jumps to reach index i, computed by checking
    // every earlier index j that can reach i (dp[i] = min(dp[j] + 1) over all valid j), O(n^2).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Both brute-force/DP versions redundantly re-examine many index pairs. In particular,
    // for Jump Game II's O(n^2) DP, for every index i we rescan all previous indices j to
    // find the best one, even though a single greedy left-to-right pass can track "how far
    // can I reach with one more jump" without ever looking backward.
    //
    // ================= OPTIMIZED APPROACH =================
    // Jump Game I - greedy single pass, O(n):
    //   Track maxReach = the farthest index reachable so far.
    //   Walk i from 0 to n-1: if i > maxReach, index i itself is unreachable, so return false
    //   immediately. Otherwise update maxReach = max(maxReach, i + nums[i]). If at any point
    //   maxReach >= last index, return true right away.
    //
    // Jump Game II - greedy "BFS-level" pass, O(n):
    //   Think of this like BFS on a graph where each jump is one "level" - all positions
    //   reachable using exactly k jumps form level k. We don't build the graph explicitly;
    //   instead we track: currentEnd = the farthest index reachable using the jumps taken so
    //   far (end of the CURRENT level), and farthest = the farthest index reachable using
    //   ONE MORE jump from anywhere within the current level.
    //   Walk i from 0 to n-2: update farthest = max(farthest, i + nums[i]). When i reaches
    //   currentEnd (we've now considered every position in the current level), we are forced
    //   to take one more jump to make further progress: increment jumps, and set
    //   currentEnd = farthest (this is now the end of the NEXT level).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed for either - just a few running integers - because
    // both problems only need "the farthest boundary reachable so far", not the full set of
    // reachable indices. Jump Game I only cares whether that boundary ever reaches or passes
    // the last index. Jump Game II's BFS-level analogy works because jump distance behaves
    // exactly like graph distance in an unweighted graph: everything reachable with k jumps
    // forms a contiguous "frontier" [previous currentEnd + 1, new currentEnd], so tracking
    // just the frontier's rightmost edge is enough to simulate BFS level-by-level without an
    // explicit queue or visited set.
    //
    // ================= EDGE CASES =================
    // - Single-element array (n = 1): already at the last index. canJump = true trivially,
    //   minJumps = 0 (no jumps needed).
    // - A 0 at some reachable index that could trap you if you land exactly on it with no
    //   further jump length available (e.g. [3,2,1,0,4] - the 0 at index 3 blocks progress
    //   past it, but index 4 is only unreachable if nothing before index 3 could jump over it).
    // - Every element equal to 1: must take exactly n-1 jumps, one step at a time.
    // - nums[0] == 0 and length > 1: canJump is immediately false (maxReach never exceeds 0, so index 1 is unreachable).
    // - The first jump alone reaches (or exceeds) the last index: minJumps = 1, canJump = true.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force/DP versions: O(n^2) (checking all index pairs).
    // Optimized greedy versions (both Jump Game I and II): O(n) - a single left-to-right pass.
    // Space Complexity: O(n) for the DP array in the brute-force DP versions (or O(2^n) call
    // stack for pure recursive backtracking on Jump Game I).
    // Optimized: O(1) extra space - only a constant number of tracking variables.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the greedy approach for Jump Game I correctly return false as soon as i > maxReach, without needing to look further?
    // - Explain the BFS-level analogy for Jump Game II in your own words - what does "currentEnd" represent physically?
    // - How would you also reconstruct the actual sequence of indices used to reach the end (not just the count)?
    // - What if jump lengths could be negative or you could also jump backward - does the greedy approach still work?
    // - How would Jump Game I change if you needed to find ALL reachable indices, not just whether the last one is reachable?
    // - Can Jump Game II be solved with actual BFS on an explicit graph? Why is the greedy version preferred in an interview?
    // - What if nums contained very large jump values that could overflow when computing i + nums[i]?

    // ---------- Jump Game I ----------

    // Brute force / DP: dp[i] = true if index i is reachable from index 0. O(n^2)
    public static boolean canJumpBruteForce(int[] nums) {
        int n = nums.length;
        boolean[] reachable = new boolean[n];
        reachable[0] = true; // start is always reachable

        for (int i = 0; i < n; i++) {
            if (!reachable[i]) {
                continue; // can't extend from an unreachable index
            }
            // Mark every index reachable from i.
            for (int step = 1; step <= nums[i] && i + step < n; step++) {
                reachable[i + step] = true;
            }
        }
        return reachable[n - 1];
    }

    // Optimized: greedy single pass tracking farthest reachable index. O(n)
    public static boolean canJumpGreedy(int[] nums) {
        int n = nums.length;
        int maxReach = 0;

        for (int i = 0; i < n; i++) {
            if (i > maxReach) {
                return false; // this index itself can't be reached
            }
            maxReach = Math.max(maxReach, i + nums[i]);
            if (maxReach >= n - 1) {
                return true; // last index already reachable, no need to scan further
            }
        }
        return true;
    }

    // ---------- Jump Game II ----------

    // Brute force / DP: dp[i] = minimum jumps to reach index i. O(n^2)
    public static int minJumpsBruteForce(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            if (dp[i] == Integer.MAX_VALUE) {
                continue; // index i itself is unreachable
            }
            for (int step = 1; step <= nums[i] && i + step < n; step++) {
                dp[i + step] = Math.min(dp[i + step], dp[i] + 1);
            }
        }
        return dp[n - 1];
    }

    // Optimized: greedy BFS-level-like pass. O(n)
    public static int minJumpsGreedy(int[] nums) {
        int n = nums.length;
        int jumps = 0;
        int currentEnd = 0; // farthest index reachable with jumps taken so far (end of current level)
        int farthest = 0; // farthest index reachable with one more jump from the current level

        for (int i = 0; i < n - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);
            if (i == currentEnd) {
                // We've exhausted the current level - must take one more jump to progress.
                jumps++;
                currentEnd = farthest;
            }
        }
        return jumps;
    }

    public static void main(String[] args) {
        int[] nums1 = {2, 3, 1, 1, 4};
        System.out.println("nums = " + Arrays.toString(nums1));
        // Expected: canJump = true, minJumps = 2
        System.out.println("canJump (brute force) = " + canJumpBruteForce(nums1) + " (expected true)");
        System.out.println("canJump (greedy)      = " + canJumpGreedy(nums1) + " (expected true)");
        System.out.println("minJumps (brute force) = " + minJumpsBruteForce(nums1) + " (expected 2)");
        System.out.println("minJumps (greedy)       = " + minJumpsGreedy(nums1) + " (expected 2)");

        int[] nums2 = {3, 2, 1, 0, 4};
        // Expected: canJump = false (the 0 at index 3 traps you before reaching index 4)
        System.out.println("\nnums = " + Arrays.toString(nums2));
        System.out.println("canJump (brute force) = " + canJumpBruteForce(nums2) + " (expected false)");
        System.out.println("canJump (greedy)      = " + canJumpGreedy(nums2) + " (expected false)");

        int[] nums3 = {0};
        // Expected: canJump = true (single element, already at the end), minJumps = 0
        System.out.println("\nnums = " + Arrays.toString(nums3) + " (single element)");
        System.out.println("canJump (greedy) = " + canJumpGreedy(nums3) + " (expected true)");
        System.out.println("minJumps (greedy) = " + minJumpsGreedy(nums3) + " (expected 0)");
    }
}
