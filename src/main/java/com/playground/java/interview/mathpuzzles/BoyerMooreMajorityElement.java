package com.playground.java.interview.mathpuzzles;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Math / Boyer-Moore Voting Algorithm
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Find the element that appears more than n/2 times in an array
 * (the "majority element"), assuming such an element always exists.
 */
public class BoyerMooreMajorityElement {

    // ================= PROBLEM =================
    // You get an array of numbers, and you're told one value appears more than
    // n/2 times (a strict majority). You need to find that value.
    // Example: nums = [2, 2, 1, 1, 1, 2, 2] -> output = 2
    // because 2 appears 4 times out of 7 elements, which is more than 7/2 = 3.5.
    //
    // ================= SIMPLE APPROACH =================
    // Use a HashMap to count how many times each number appears.
    // Walk through the array once, incrementing the count for each number.
    // Then scan the map to find the number whose count exceeds n/2.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works in O(n) time, but it uses O(n) extra space for the HashMap in the
    // worst case (many distinct values). The Boyer-Moore Voting Algorithm achieves
    // the same O(n) time with O(1) extra space, which is often what interviewers
    // want to see as the "clever" follow-up.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use the Boyer-Moore Voting Algorithm. Keep a "candidate" value and a "count".
    // Walk through the array: if count is 0, set the current number as the new
    // candidate. If the current number equals the candidate, increment count;
    // otherwise, decrement count.
    // Because the majority element appears more than n/2 times, it always "wins"
    // this cancellation process, and the candidate at the end is guaranteed to be
    // the majority element (as long as a true majority is guaranteed to exist).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just two variables (candidate and count).
    // The voting algorithm relies on the pigeonhole-like guarantee that a true
    // majority element cannot be fully "cancelled out" by all the other elements
    // combined, since it outnumbers everything else put together.
    //
    // ================= EDGE CASES =================
    // - Array with exactly one element: that element is trivially the majority.
    // - Array where the majority element is more than 50% but not overwhelming (e.g., 51%).
    // - Array without any true majority element: the algorithm will still return
    //   some candidate, but it may be wrong - a verification pass is needed if a
    //   majority isn't guaranteed.
    // - All elements identical: that element is the (extreme) majority.
    // - Alternating elements that almost cancel out perfectly, testing the cancellation logic.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - a single pass through the array.
    // Space Complexity: O(1) for the Boyer-Moore Voting Algorithm - only two
    // variables. O(n) for the HashMap counting approach in the worst case.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does the Boyer-Moore algorithm work - can you explain the cancellation intuition?
    // - What happens if you run this algorithm on an array with no true majority element? How would you verify correctness?
    // - How would you find all elements that appear more than n/3 times (a related but different problem)?
    // - Can you adapt the counting approach to find the top-K most frequent elements instead?
    // - How would this work in a streaming scenario where you can't store the whole array?
    // - What if ties were possible (exactly n/2, not "more than" n/2) - does the algorithm still guarantee a correct answer?
    // - How would you extend Boyer-Moore voting to track two candidates simultaneously (for the n/3 majority variant)?

    // Brute force: count frequencies with a HashMap. O(n) time, O(n) space.
    public static int majorityElementBruteForce(int[] nums) {
        Map<Integer, Integer> counts = new HashMap<>();
        int n = nums.length;
        for (int num : nums) {
            int updatedCount = counts.getOrDefault(num, 0) + 1;
            counts.put(num, updatedCount);
            if (updatedCount > n / 2) {
                return num;
            }
        }
        throw new IllegalArgumentException("No majority element found");
    }

    // Optimized: Boyer-Moore Voting Algorithm. O(n) time, O(1) space.
    public static int majorityElementOptimized(int[] nums) {
        int candidate = nums[0];
        int count = 0;

        for (int num : nums) {
            if (count == 0) {
                // No current "leader", pick this number as the new candidate.
                candidate = num;
            }
            // Increment if it matches the candidate, decrement (cancel out) otherwise.
            count += (num == candidate) ? 1 : -1;
        }
        return candidate;
    }

    public static void main(String[] args) {
        int[] nums1 = {2, 2, 1, 1, 1, 2, 2};
        // Expected: 2
        System.out.println("Input: [2,2,1,1,1,2,2]");
        System.out.println("Brute force output: " + majorityElementBruteForce(nums1));
        System.out.println("Optimized output: " + majorityElementOptimized(nums1));

        int[] nums2 = {3, 3, 4};
        // Expected: 3
        System.out.println("\nInput: [3,3,4]");
        System.out.println("Optimized output: " + majorityElementOptimized(nums2));

        int[] nums3 = {7};
        // Expected: 7 (single element)
        System.out.println("\nInput: [7] (single element)");
        System.out.println("Optimized output: " + majorityElementOptimized(nums3));
    }
}
