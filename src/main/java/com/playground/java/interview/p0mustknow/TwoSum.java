package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Arrays / HashMap
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given an array of integers and a target, return the indices of the two
 * numbers that add up to the target.
 */
public class TwoSum {

    // ================= PROBLEM =================
    // You get a list of numbers and a target number.
    // You need to find two numbers in the list that add up to the target,
    // and return their positions (indices) in the array.
    // Example: nums = [2, 7, 11, 15], target = 9 -> output = [0, 1]
    // because nums[0] + nums[1] = 2 + 7 = 9.
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible pair of numbers using two nested loops.
    // For each number, compare it with every other number after it.
    // If the pair adds up to the target, return their indices.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Checking every pair means comparing each element with every other element.
    // For n numbers, that is roughly n*n comparisons.
    // This becomes very slow when the array is large (say, a million numbers).
    //
    // ================= OPTIMIZED APPROACH =================
    // Walk through the array once, one number at a time.
    // For each number, calculate what other number is needed to reach the target
    // (that is, "complement = target - current number").
    // Check if we have already seen that complement before (store seen numbers in a map).
    // If yes, we found our pair. If no, remember the current number and its index, and move on.
    // This way we only pass through the array once.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap gives us O(1) average time to check "have I seen this number before"
    // and O(1) average time to store a new number.
    // Compared to scanning the array again and again (which is what the brute force does),
    // the HashMap turns a "search" step into a quick lookup step.
    //
    // ================= EDGE CASES =================
    // - Empty array or array with only one element: no valid pair exists.
    // - No pair adds up to the target: decide what to return (we throw an exception here).
    // - Duplicate numbers in the array, e.g. [3, 3] with target 6: must use two different indices.
    // - Negative numbers: complement calculation still works fine.
    // - Multiple valid pairs exist: problem usually expects just one answer (the first found).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized approach - we visit each element once and
    // do O(1) work per element with the HashMap. Brute force is O(n^2).
    // Space Complexity: O(n) for the optimized approach - in the worst case we store
    // almost all elements in the HashMap before finding the pair. Brute force is O(1) extra space.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - What if there are multiple valid pairs? How would you return all of them?
    // - What if the array is sorted? Could you solve it with two pointers in O(1) space instead?
    // - What if the input is a stream of numbers that keeps growing? How does your approach change?
    // - How do you handle duplicate values correctly without reusing the same index twice?
    // - What if no pair exists - should the method return null, throw, or return an empty result?
    // - How would you extend this to "Three Sum" or "K Sum"?
    // - Is a HashMap thread-safe? What would you use in a concurrent environment?

    // Brute force: check every pair with nested loops.
    public static int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("No two sum solution found");
    }

    // Optimized: one pass with a HashMap of value -> index.
    public static int[] twoSumOptimized(int[] nums, int target) {
        Map<Integer, Integer> seenValueToIndex = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            // If we already saw the complement, we found our answer.
            if (seenValueToIndex.containsKey(complement)) {
                return new int[]{seenValueToIndex.get(complement), i};
            }
            // Otherwise remember this number and its index for later.
            seenValueToIndex.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two sum solution found");
    }

    public static void main(String[] args) {
        int[] nums1 = {2, 7, 11, 15};
        int target1 = 9;
        // Expected: [0, 1]
        System.out.println("Input: nums=[2,7,11,15], target=9");
        System.out.println("Brute force output: " + java.util.Arrays.toString(twoSumBruteForce(nums1, target1)));
        System.out.println("Optimized output: " + java.util.Arrays.toString(twoSumOptimized(nums1, target1)));

        int[] nums2 = {3, 3};
        int target2 = 6;
        // Expected: [0, 1]
        System.out.println("\nInput: nums=[3,3], target=6 (duplicate values)");
        System.out.println("Brute force output: " + java.util.Arrays.toString(twoSumBruteForce(nums2, target2)));
        System.out.println("Optimized output: " + java.util.Arrays.toString(twoSumOptimized(nums2, target2)));

        int[] nums3 = {-3, 4, 3, 90};
        int target3 = 0;
        // Expected: [0, 2]  (-3 + 3 = 0)
        System.out.println("\nInput: nums=[-3,4,3,90], target=0 (negative numbers)");
        System.out.println("Optimized output: " + java.util.Arrays.toString(twoSumOptimized(nums3, target3)));
    }
}
