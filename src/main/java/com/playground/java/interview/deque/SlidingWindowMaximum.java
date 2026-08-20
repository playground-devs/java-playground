package com.playground.java.interview.deque;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PATTERN: Monotonic Deque / Sliding Window
 * PRIORITY: P1
 * PROBLEM STATEMENT: Return the maximum value of every contiguous window of size k as it slides across an array.
 */
public class SlidingWindowMaximum {

    // ================= PROBLEM =================
    // Given an array of integers and a window size k, return an array of the maximum value
    // in every contiguous window of size k as the window slides from the start to the end
    // of the array.
    // Example: nums = [1,3,-1,-3,5,3,6,7], k = 3
    // Windows: [1,3,-1]->3, [3,-1,-3]->3, [-1,-3,5]->5, [-3,5,3]->5, [5,3,6]->6, [3,6,7]->7
    // -> Output: [3,3,5,5,6,7]
    //
    // ================= SIMPLE APPROACH =================
    // For each window starting position, scan all k elements inside that window and find the
    // maximum directly.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // There are roughly n windows, and scanning each one takes O(k) time, giving O(n*k) total
    // time. This repeats a lot of comparisons: most elements are re-examined in multiple
    // overlapping windows without reusing the fact that we already know a lot about the
    // relative ordering of nearby elements from the previous window.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a Deque<Integer> (ArrayDeque) that stores INDICES, kept in decreasing order of
    // their corresponding values (the largest value's index is always at the front).
    // Step 1: For each index i, first evict indices from the FRONT of the deque if they have
    //          fallen out of the current window (i.e. index <= i - k).
    // Step 2: Evict indices from the BACK of the deque while the value at those indices is
    //          less than or equal to nums[i] - those values can never be the maximum of any
    //          future window that still contains i, since nums[i] is both newer and at least
    //          as large.
    // Step 3: Push i onto the back of the deque.
    // Step 4: Once i has reached at least k-1 (the window is fully formed), the front of the
    //          deque holds the index of the maximum value for the current window - record
    //          nums[front] into the result.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A Deque supports O(1) removal from both ends, which is exactly what this algorithm
    // needs: expired indices are removed from the front, and dominated (smaller, older)
    // indices are removed from the back, all in constant time per operation. Each index is
    // pushed onto the deque exactly once and popped at most once (from either end) over the
    // whole run, so the total work across the entire array is O(n) amortized - a plain array
    // or ArrayList would require O(k) shifting to remove from the front, and a stack alone
    // could not efficiently evict expired elements from the "old" end.
    //
    // ================= EDGE CASES =================
    // - k = 1: every element is its own window, result equals the input array unchanged.
    // - k equals the array length: only one window exists, result is a single element: the global maximum.
    // - k greater than array length: invalid input; decide whether to throw or return an empty result.
    // - Array with duplicate maximum values, e.g. [4,4,4]: ties must still be handled correctly (using <= when evicting from the back keeps the most recent duplicate).
    // - Strictly decreasing array, e.g. [5,4,3,2,1]: deque holds indices in original order since nothing ever gets evicted from the back.
    // - Strictly increasing array, e.g. [1,2,3,4,5]: every new element evicts everything before it from the back, deque always holds just the latest index.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n*k) because each of the roughly n windows requires an
    // O(k) scan. Optimized O(n) amortized because each index is pushed and popped from the
    // deque at most once across the entire run.
    // Space Complexity: Brute force O(n) for the result array (O(1) extra beyond that).
    // Optimized O(k) worst case for the deque (it never holds more than k indices, since expired ones are evicted from the front) plus O(n) for the result array.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you adapt this to find the sliding window MINIMUM instead of maximum?
    // - Can you solve this using a balanced structure like a TreeMap (value -> count) instead of a deque? What's the complexity trade-off?
    // - Why do we evict from the back using <= rather than < when comparing values?
    // - How would this change if the array were a continuous stream and you needed the running window maximum in real time, without knowing the total length in advance?
    // - How would you extend this to report both the maximum AND the minimum of every window in one pass?
    // - What happens if k is 0 or negative - how should the function behave?
    // - Why is a plain max-heap (PriorityQueue) less efficient here than the deque approach, given you'd need lazy deletion of expired indices?

    // Brute force: scan every window directly. O(n*k).
    public static int[] maxSlidingWindowBruteForce(int[] nums, int k) {
        int n = nums.length;
        if (n == 0 || k <= 0) {
            return new int[0];
        }

        int[] result = new int[n - k + 1];
        for (int i = 0; i <= n - k; i++) {
            int max = nums[i];
            for (int j = i; j < i + k; j++) {
                max = Math.max(max, nums[j]);
            }
            result[i] = max;
        }
        return result;
    }

    // Optimized: monotonic decreasing deque of indices. O(n) amortized.
    public static int[] maxSlidingWindowOptimized(int[] nums, int k) {
        int n = nums.length;
        if (n == 0 || k <= 0) {
            return new int[0];
        }

        int[] result = new int[n - k + 1];
        Deque<Integer> deque = new ArrayDeque<>(); // indices, values decreasing from front to back

        for (int i = 0; i < n; i++) {
            // Evict indices that have fallen out of the current window.
            while (!deque.isEmpty() && deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }

            // Evict indices whose values can never be the max while nums[i] is in the window.
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }

            deque.offerLast(i);

            // Once the window is fully formed, record the current maximum.
            if (i >= k - 1) {
                result[i - k + 1] = nums[deque.peekFirst()];
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 3, -1, -3, 5, 3, 6, 7};
        int k1 = 3;
        // Expected: [3,3,5,5,6,7]
        System.out.println("Input: nums=" + java.util.Arrays.toString(nums1) + ", k=" + k1);
        System.out.println("Brute force: " + java.util.Arrays.toString(maxSlidingWindowBruteForce(nums1, k1)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(maxSlidingWindowOptimized(nums1, k1)));

        int[] nums2 = {4, 4, 4};
        int k2 = 2;
        // Expected: [4,4] (duplicate maximums)
        System.out.println("\nInput: nums=" + java.util.Arrays.toString(nums2) + ", k=" + k2);
        System.out.println("Brute force: " + java.util.Arrays.toString(maxSlidingWindowBruteForce(nums2, k2)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(maxSlidingWindowOptimized(nums2, k2)));

        int[] nums3 = {5};
        int k3 = 1;
        // Expected: [5] (single element, k = 1)
        System.out.println("\nInput: nums=" + java.util.Arrays.toString(nums3) + ", k=" + k3);
        System.out.println("Brute force: " + java.util.Arrays.toString(maxSlidingWindowBruteForce(nums3, k3)));
        System.out.println("Optimized:   " + java.util.Arrays.toString(maxSlidingWindowOptimized(nums3, k3)));
    }
}
