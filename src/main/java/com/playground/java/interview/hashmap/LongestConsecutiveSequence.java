package com.playground.java.interview.hashmap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * PATTERN: HashMap / HashSet
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Find the length of the longest run of consecutive integers in an
 * unsorted array, in O(n) time.
 */
public class LongestConsecutiveSequence {

    // ================= PROBLEM =================
    // You get an unsorted array of integers. You need to find the length of the
    // longest sequence of consecutive integers (they don't need to be consecutive
    // in the array, just consecutive in value).
    // Example: nums = [100, 4, 200, 1, 3, 2] -> output = 4
    // because the longest consecutive sequence is [1, 2, 3, 4].
    //
    // ================= SIMPLE APPROACH =================
    // Sort the array first. Then walk through the sorted array once, keeping a
    // running length of the current consecutive streak, and resetting the streak
    // whenever the next number is not exactly one more than the current number
    // (skip duplicates without breaking the streak).
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting takes O(n log n) time, but the problem specifically asks for an
    // O(n) time solution, which sorting cannot achieve.
    //
    // ================= OPTIMIZED APPROACH =================
    // Put all numbers into a HashSet for O(1) average lookups.
    // For each number, only start counting a new sequence if (number - 1) is NOT
    // in the set - this guarantees we only start counting from the true beginning
    // of a sequence, not from the middle.
    // From that starting number, keep checking if the next consecutive number
    // (number + 1, then +2, etc.) exists in the set, extending the streak length
    // each time. Track the maximum streak length found.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashSet gives O(1) average time to check "does this number exist in the
    // array" without needing the array to be sorted. The "only start from a true
    // beginning" check ensures that even though we scan every number, the total
    // work across all sequences adds up to O(n), not O(n^2), because each number
    // is only ever part of one streak-counting walk.
    //
    // ================= EDGE CASES =================
    // - Empty array: longest sequence length is 0.
    // - Array with duplicate values: duplicates should not inflate the sequence length.
    // - All numbers are the same: longest sequence length is 1.
    // - Array where all numbers are already consecutive: answer is the array length (after dedup).
    // - Negative numbers and numbers spanning zero (e.g., -1, 0, 1, 2).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized HashSet approach - even though there's
    // a nested while loop, each number is visited by the inner loop at most once
    // across the entire algorithm's lifetime, because we only start counting from
    // true sequence beginnings. Brute force sorting is O(n log n).
    // Space Complexity: O(n) for the optimized approach - the HashSet stores up to
    // n distinct numbers. Brute force sorting can be O(1) extra space if done in-place,
    // but that mutates the input.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is it safe to say the overall time is still O(n) even though there's a while loop inside a for loop?
    // - What is the key check that prevents us from re-counting the same sequence multiple times?
    // - How would you also return the actual sequence (not just its length)?
    // - How would this change if the array was too large to fit in memory (streaming)?
    // - What if duplicates should count as extending the sequence length (a variant rule)?
    // - Can you solve this with Union-Find (disjoint set) instead of a HashSet? What's the trade-off?
    // - How would you adapt this to find the longest consecutive sequence of even numbers only?

    // Brute force: sort first, then scan once counting consecutive runs. O(n log n).
    public static int longestConsecutiveBruteForce(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int[] sorted = nums.clone();
        Arrays.sort(sorted);

        int longest = 1;
        int currentStreak = 1;
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] == sorted[i - 1]) {
                // Duplicate value, skip without breaking or extending the streak.
                continue;
            }
            if (sorted[i] == sorted[i - 1] + 1) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            longest = Math.max(longest, currentStreak);
        }
        return longest;
    }

    // Optimized: HashSet, only start counting from true sequence beginnings. O(n).
    public static int longestConsecutiveOptimized(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        Set<Integer> numSet = new HashSet<>();
        for (int num : nums) {
            numSet.add(num);
        }

        int longest = 0;
        for (int num : numSet) {
            // Only start counting if this is the beginning of a sequence
            // (i.e., num - 1 is not in the set).
            if (!numSet.contains(num - 1)) {
                int currentNum = num;
                int currentStreak = 1;
                while (numSet.contains(currentNum + 1)) {
                    currentNum++;
                    currentStreak++;
                }
                longest = Math.max(longest, currentStreak);
            }
        }
        return longest;
    }

    public static void main(String[] args) {
        int[] nums1 = {100, 4, 200, 1, 3, 2};
        // Expected: 4 (sequence [1,2,3,4])
        System.out.println("Input: [100,4,200,1,3,2]");
        System.out.println("Brute force output: " + longestConsecutiveBruteForce(nums1));
        System.out.println("Optimized output: " + longestConsecutiveOptimized(nums1));

        int[] nums2 = {1, 2, 0, 1};
        // Expected: 3 (sequence [0,1,2], duplicate 1 ignored)
        System.out.println("\nInput: [1,2,0,1] (has duplicate)");
        System.out.println("Optimized output: " + longestConsecutiveOptimized(nums2));

        int[] nums3 = {};
        // Expected: 0 (empty array)
        System.out.println("\nInput: [] (empty array)");
        System.out.println("Optimized output: " + longestConsecutiveOptimized(nums3));
    }
}
