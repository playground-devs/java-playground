package com.playground.java.interview.p0mustknow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * PATTERN: Arrays / HashMap / Heap / Bucket Sort
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given an array of numbers, return the K most frequently occurring elements.
 */
public class TopKFrequentElements {

    // ================= PROBLEM =================
    // You get a list of numbers and a number K.
    // You need to find the K numbers that appear most often in the list.
    // Example: nums = [1, 1, 1, 2, 2, 3], k = 2 -> output = [1, 2]
    // because 1 appears 3 times, 2 appears 2 times, and 3 appears once - the top 2 are 1 and 2.
    //
    // ================= SIMPLE APPROACH =================
    // Count how many times each number appears (using a map).
    // Then sort all the numbers by their count, from highest to lowest.
    // Take the first K numbers from that sorted list.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting all unique numbers by frequency takes O(m log m) time, where m is the number
    // of distinct values. If m is close to n (the array size) and K is small, this does more
    // work than necessary - we don't need a FULL sort, just the top K.
    //
    // ================= OPTIMIZED APPROACH =================
    // Option A (Heap): Count frequencies with a map, then use a Min-Heap of size K.
    // Walk through each unique number and its frequency; push it onto the heap.
    // If the heap grows beyond size K, remove the smallest-frequency element.
    // At the end, the heap contains exactly the K most frequent elements.
    // Option B (Bucket Sort): Count frequencies with a map. Create an array of "buckets" where
    // the index represents a frequency count (0 to n), and each bucket holds all numbers that
    // appear that many times. Then walk the buckets from highest frequency down to lowest,
    // collecting numbers until we have K of them. This avoids sorting entirely and is O(n).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // The HashMap gives O(1) average frequency counting.
    // The Min-Heap (PriorityQueue) keeps only K elements at a time, so each insert/remove is
    // O(log K) instead of O(log m) for a full sort - much cheaper when K is small.
    // Bucket sort avoids comparison-based sorting entirely: since frequency can only range from
    // 1 to n, we can use frequency directly as an array index, giving O(n) time with no log factor.
    //
    // ================= EDGE CASES =================
    // - K equals the number of distinct elements: return all of them.
    // - K is 0: return an empty result.
    // - All elements have the same frequency: any K of them is a valid answer (ties).
    // - Array with a single element: that element is the only possible answer.
    // - Empty array: return an empty result.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Heap approach is O(n log k) - n for counting, then log k per heap
    // operation for each of the m unique elements. Bucket sort approach is O(n) - counting is O(n),
    // and bucket placement plus collection is also O(n), no sorting/log factor involved.
    // Space Complexity: O(n) for the frequency map, plus O(k) for the heap, or O(n) for the buckets.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - When would you prefer the heap approach over bucket sort, or vice versa?
    // - What if K is very close to the number of distinct elements - does bucket sort still win?
    // - How would you handle ties in frequency (e.g. multiple valid answers) deterministically?
    // - What if the data is a continuous stream and you need to maintain "top K" at all times?
    // - How would this scale for a massive dataset that doesn't fit in memory (external sorting, Count-Min Sketch)?
    // - Can you solve this using Quickselect (partition-based) for average O(n) time?
    // - How would you adapt this to find the K LEAST frequent elements instead?

    // Approach A: Min-Heap of size K, built from frequency counts.
    public static int[] topKFrequentHeap(int[] nums, int k) {
        // Step 1: count how often each number appears.
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int num : nums) {
            frequencyMap.merge(num, 1, Integer::sum);
        }

        // Step 2: min-heap ordered by frequency (smallest frequency at the top).
        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());

        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                // Remove the least frequent element to keep heap size at K.
                minHeap.poll();
            }
        }

        // Step 3: drain the heap into the result array.
        int[] result = new int[minHeap.size()];
        int index = result.length - 1;
        while (!minHeap.isEmpty()) {
            result[index--] = minHeap.poll().getKey();
        }
        return result;
    }

    // Approach B: Bucket sort by frequency, O(n) time, no heap/sort needed.
    public static int[] topKFrequentBucketSort(int[] nums, int k) {
        // Step 1: count how often each number appears.
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int num : nums) {
            frequencyMap.merge(num, 1, Integer::sum);
        }

        // Step 2: create buckets where index = frequency, value = list of numbers with that frequency.
        // Frequency can range from 1 to nums.length, so we need nums.length + 1 buckets.
        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i <= nums.length; i++) {
            buckets.add(new ArrayList<>());
        }
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            buckets.get(entry.getValue()).add(entry.getKey());
        }

        // Step 3: walk buckets from highest frequency to lowest, collecting numbers until we have K.
        List<Integer> result = new ArrayList<>();
        for (int freq = buckets.size() - 1; freq >= 0 && result.size() < k; freq--) {
            for (int num : buckets.get(freq)) {
                if (result.size() < k) {
                    result.add(num);
                }
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {
        int[] nums1 = {1, 1, 1, 2, 2, 3};
        int k1 = 2;
        // Expected: [1, 2] (order may vary depending on approach)
        System.out.println("Input: nums=[1,1,1,2,2,3], k=2");
        System.out.println("Heap output: " + java.util.Arrays.toString(topKFrequentHeap(nums1, k1)));
        System.out.println("Bucket sort output: " + java.util.Arrays.toString(topKFrequentBucketSort(nums1, k1)));

        int[] nums2 = {1};
        int k2 = 1;
        // Expected: [1]
        System.out.println("\nInput: nums=[1], k=1 (single element)");
        System.out.println("Bucket sort output: " + java.util.Arrays.toString(topKFrequentBucketSort(nums2, k2)));

        int[] nums3 = {4, 4, 5, 5, 6, 6};
        int k3 = 3;
        // Expected: [4, 5, 6] (all tied at frequency 2, any order)
        System.out.println("\nInput: nums=[4,4,5,5,6,6], k=3 (all tied frequencies)");
        System.out.println("Bucket sort output: " + java.util.Arrays.toString(topKFrequentBucketSort(nums3, k3)));
    }
}
