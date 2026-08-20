package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Arrays / Sorting / Two Pointers
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given an array of integers, find all unique triplets that sum to zero.
 */
public class ThreeSum {

    // ================= PROBLEM =================
    // You get a list of numbers. You need to find all unique groups of three numbers
    // (triplets) whose sum is exactly zero. The same triplet (in any order) should not
    // be reported twice.
    // Example: nums = [-1, 0, 1, 2, -1, -4] -> output = [[-1, -1, 2], [-1, 0, 1]]
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible group of three numbers using three nested loops.
    // For each triplet, check if the sum is zero.
    // Use a Set to avoid adding the same triplet (after sorting it) more than once.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Three nested loops means roughly n*n*n comparisons, which is O(n^3).
    // For even a few thousand numbers this becomes too slow.
    // Also, de-duplicating triplets with a Set of sorted lists adds extra overhead.
    //
    // ================= OPTIMIZED APPROACH =================
    // First, sort the array. Sorting helps us skip duplicates easily and use two pointers.
    // Then, fix one number at a time (call it the "anchor") and look for two other numbers
    // in the remaining part of the array that add up to "negative of the anchor".
    // To find those two numbers quickly, use two pointers: one starting right after the anchor
    // (left pointer) and one at the end of the array (right pointer).
    // If the sum of the three numbers is too small, move the left pointer right (to increase sum).
    // If the sum is too big, move the right pointer left (to decrease sum).
    // If the sum is exactly zero, record the triplet, then move both pointers inward while
    // skipping over duplicate values to avoid repeating the same triplet.
    // Also skip duplicate anchor values so we don't process the same starting number twice.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting the array (an O(n log n) operation) lets us use the two-pointer technique,
    // which finds a pair summing to a target in O(n) instead of O(n^2) with nested loops.
    // Sorting also makes duplicate values sit next to each other, so skipping duplicates
    // is a simple "if same as previous, skip" check instead of needing a Set/HashMap.
    //
    // ================= EDGE CASES =================
    // - Array with fewer than 3 elements: no triplet is possible, return empty list.
    // - All elements are zero, e.g. [0, 0, 0, 0]: only one unique triplet [0, 0, 0].
    // - All positive or all negative numbers: no triplet can sum to zero.
    // - Many duplicate values: must not return duplicate triplets.
    // - Array already sorted or reverse sorted: algorithm should still work correctly.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2) - sorting takes O(n log n), then for each of the n anchor
    // elements we do an O(n) two-pointer scan, giving O(n^2) overall (which dominates).
    // Space Complexity: O(log n) to O(n) for the sort itself (depends on JDK sort implementation),
    // plus O(k) for the output list where k is the number of triplets found.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you modify this to solve "Four Sum" (find quadruplets summing to zero)?
    // - How would you find triplets that sum to an arbitrary target, not just zero?
    // - What if the array is extremely large and cannot be fully loaded into memory?
    // - How do you avoid duplicate triplets without sorting (e.g. if order must be preserved)?
    // - Can you do this in less than O(n^2) time? (No known general solution better than O(n^2).)
    // - What if the input has many repeated values - does your duplicate-skipping logic still work?
    // - How would you return the actual count of triplets instead of the triplets themselves, more efficiently?

    // Optimized: sort + two pointers, skipping duplicates.
    public static List<List<Integer>> threeSumOptimized(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums == null || nums.length < 3) {
            return result;
        }

        Arrays.sort(nums);

        for (int i = 0; i < nums.length - 2; i++) {
            // Since array is sorted, if the smallest number is already positive,
            // no triplet starting here (or later) can sum to zero.
            if (nums[i] > 0) {
                break;
            }
            // Skip duplicate anchors to avoid duplicate triplets.
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }

            int left = i + 1;
            int right = nums.length - 1;
            int targetForPair = -nums[i];

            while (left < right) {
                int pairSum = nums[left] + nums[right];
                if (pairSum == targetForPair) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    left++;
                    right--;
                    // Skip duplicate left values.
                    while (left < right && nums[left] == nums[left - 1]) {
                        left++;
                    }
                    // Skip duplicate right values.
                    while (left < right && nums[right] == nums[right + 1]) {
                        right--;
                    }
                } else if (pairSum < targetForPair) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {-1, 0, 1, 2, -1, -4};
        // Expected: [[-1, -1, 2], [-1, 0, 1]]
        System.out.println("Input: [-1, 0, 1, 2, -1, -4]");
        System.out.println("Output: " + threeSumOptimized(nums1));

        int[] nums2 = {0, 0, 0, 0};
        // Expected: [[0, 0, 0]]
        System.out.println("\nInput: [0, 0, 0, 0]");
        System.out.println("Output: " + threeSumOptimized(nums2));

        int[] nums3 = {1, 2};
        // Expected: [] (fewer than 3 elements)
        System.out.println("\nInput: [1, 2]");
        System.out.println("Output: " + threeSumOptimized(nums3));
    }
}
