package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * PATTERN: Monotonic Stack
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: For each element in an array, find the first element to its right that is strictly greater; use -1 if none exists.
 */
public class NextGreaterElement {

    // ================= PROBLEM =================
    // Given an array of numbers, for every element find the NEXT element to its right that
    // is strictly greater than it. If no such element exists, the answer for that position is -1.
    // Example: nums = [2, 1, 2, 4, 3]
    // For index 0 (value 2): next greater is 4 (skipping 1 and 2) -> answer 4
    // For index 1 (value 1): next greater is 2 -> answer 2
    // For index 2 (value 2): next greater is 4 -> answer 4
    // For index 3 (value 4): nothing to the right is greater -> answer -1
    // For index 4 (value 3): nothing to the right -> answer -1
    // Output: [4, 2, 4, -1, -1]
    //
    // ================= SIMPLE APPROACH =================
    // For every element, scan all elements to its right one by one until you find one
    // that is strictly greater. Stop and record it as soon as found; if you reach the end
    // without finding one, record -1. Repeat this scan for every starting index.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This is a nested loop: for each of the n elements, we may scan up to n elements to
    // its right. That's O(n^2) in the worst case (e.g. a strictly decreasing array, where
    // every element has to scan almost the whole rest of the array before giving up).
    // For large arrays this is far too slow.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a monotonic decreasing stack of INDICES (not values), scanning the array once
    // left to right. The stack always holds indices whose "next greater element" has not
    // been found yet, in decreasing order of value from bottom to top.
    // For each new element nums[i]:
    //   While the stack is not empty AND nums[i] > nums[stack.peek()]:
    //     pop the index from the stack - nums[i] IS that popped index's next greater element.
    //   Push i onto the stack (its next greater element is still unknown).
    // After processing all elements, any indices still left on the stack have no next
    // greater element, so their answer stays -1.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A stack fits perfectly because "next greater element" is inherently a LIFO problem:
    // when a big number shows up, it immediately resolves the answer for ALL smaller numbers
    // waiting before it, most-recently-seen first - which is exactly the order a stack pops
    // in. Each index is pushed exactly once and popped at most once, so the total work across
    // the whole array is O(n) instead of O(n^2), because we never re-scan an element - we
    // only revisit indices that are still "unresolved" and sitting on the stack.
    //
    // ================= EDGE CASES =================
    // - Empty array: return an empty result array.
    // - Single element array: its answer is always -1 (nothing to its right).
    // - Strictly increasing array, e.g. [1,2,3,4]: every element's next greater is the very
    //   next element; nothing ever stays long on the stack.
    // - Strictly decreasing array, e.g. [4,3,2,1]: every element's answer is -1; the stack
    //   keeps growing and nothing ever gets popped.
    // - Duplicate values, e.g. [2,2,2]: "strictly greater" means equal values do NOT resolve
    //   each other - they all remain on the stack until a truly larger value appears (or never).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized approach - each index is pushed onto the stack
    // exactly once and popped at most once, so total stack operations are bounded by 2n.
    // Brute force is O(n^2) due to the nested scan.
    // Space Complexity: O(n) for the optimized approach - the stack can hold up to n indices
    // in the worst case (e.g. strictly decreasing input), plus O(n) for the result array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve "Next Smaller Element" instead - what changes in the comparison?
    // - How would you handle a CIRCULAR array (wrap around to the beginning)?
    // - How would you find the PREVIOUS greater element instead of the next one?
    // - Can you explain why each index is pushed and popped at most once (the O(n) proof)?
    // - How does this pattern relate to "Daily Temperatures" or "Largest Rectangle in Histogram"?
    // - What if you needed the next greater element's INDEX instead of its value?
    // - Why do we store indices on the stack instead of values directly?

    // Brute force: for each element, scan rightward until a greater one is found.
    public static int[] nextGreaterBruteForce(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = -1; // default: no greater element found
            for (int j = i + 1; j < n; j++) {
                if (nums[j] > nums[i]) {
                    result[i] = nums[j];
                    break;
                }
            }
        }
        return result;
    }

    // Optimized: single pass with a monotonic decreasing stack of indices.
    public static int[] nextGreaterOptimized(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // default: no greater element found
        Deque<Integer> stack = new ArrayDeque<>(); // holds indices, values decreasing bottom-to-top

        for (int i = 0; i < n; i++) {
            // Current value resolves the answer for every smaller value waiting on the stack.
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) {
                int resolvedIndex = stack.pop();
                result[resolvedIndex] = nums[i];
            }
            stack.push(i); // this index's next greater element is still unknown
        }
        // Anything left on the stack never found a next greater element - stays -1.
        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {2, 1, 2, 4, 3};
        System.out.println("Input: " + Arrays.toString(nums1));
        // Expected: [4, 2, 4, -1, -1]
        System.out.println("Brute force: " + Arrays.toString(nextGreaterBruteForce(nums1)));
        System.out.println("Optimized:   " + Arrays.toString(nextGreaterOptimized(nums1)));

        int[] nums2 = {4, 3, 2, 1};
        System.out.println("\nInput: " + Arrays.toString(nums2) + " (strictly decreasing)");
        // Expected: [-1, -1, -1, -1]
        System.out.println("Optimized: " + Arrays.toString(nextGreaterOptimized(nums2)));

        int[] nums3 = {};
        System.out.println("\nInput: [] (empty array)");
        // Expected: []
        System.out.println("Optimized: " + Arrays.toString(nextGreaterOptimized(nums3)));

        int[] nums4 = {2, 2, 2};
        System.out.println("\nInput: " + Arrays.toString(nums4) + " (all duplicates)");
        // Expected: [-1, -1, -1]  (strictly greater required, equals don't count)
        System.out.println("Optimized: " + Arrays.toString(nextGreaterOptimized(nums4)));
    }
}
