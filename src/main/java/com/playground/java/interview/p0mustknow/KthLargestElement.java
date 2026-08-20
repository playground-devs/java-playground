package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Heap (Priority Queue) / QuickSelect
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * ONE-LINE PROBLEM STATEMENT: Find the k-th largest element in an unsorted array without fully sorting it.
 */
// ================= PROBLEM =================
// Given an unsorted array of integers and an integer k, find the k-th largest element
// in the array. Note: it's the k-th largest in sorted order, not the k-th distinct value.
//
// Example: nums = [3,2,1,5,6,4], k = 2
// Sorted descending: [6,5,4,3,2,1], the 2nd largest is 5 -> Output: 5
//
// ================= SIMPLE APPROACH =================
// Sort the entire array (ascending or descending), then directly index into the sorted
// result to pick the k-th largest element. For example, sort ascending and return
// nums[n - k].
//
// ================= WHY IT'S NOT ENOUGH =================
// Sorting the whole array takes O(n log n) time, but we don't actually need the whole
// array sorted -- we only need to know which single element ends up in the k-th
// largest position. This is wasted work when k is small compared to n (e.g. finding
// the 3rd largest out of a million elements does not require sorting all million).
//
// ================= OPTIMIZED APPROACH =================
// Use a Min-Heap (PriorityQueue) of size k.
// Step 1: Walk through the array, adding each element to a min-heap.
// Step 2: Whenever the heap size exceeds k, remove the smallest element (heap.poll()).
//          This keeps only the k largest elements seen so far in the heap at all times,
//          with the smallest of those k sitting at the top of the heap.
// Step 3: After processing the whole array, the top of the heap (heap.peek()) is
//          exactly the k-th largest element, because the heap holds the k largest
//          values and the smallest among them is, by definition, the k-th largest overall.
// This avoids fully sorting the array; we only ever do heap operations bounded by size k.
//
// (Interview follow-up note: QuickSelect, a partition-based selection algorithm similar
// to quicksort's partition step, can solve this in O(n) average time using O(1) extra
// space, though O(n^2) worst case unless a randomized or median-of-medians pivot
// strategy is used. It is not implemented here in full, but described in the
// followups/complexity sections since interviewers often want you to mention it as the
// "next level" optimization after the heap approach.)
//
// ================= WHY THIS DATA STRUCTURE =================
// A min-heap of bounded size k is ideal because it always gives O(1) access to the
// smallest of the "top k" elements (heap.peek()), and insertion/removal are O(log k).
// This is much cheaper than sorting the entire array when k is small, since heap
// operations only cost O(log k) instead of O(log n). A max-heap of the whole array
// would technically also work (pop k times) but that's less efficient than bounding
// the heap to size k, since a full max-heap build costs O(n) and k pops cost O(k log n)
// -- worse than maintaining a fixed size-k min-heap when k << n.
//
// ================= EDGE CASES =================
// - k equals the array length: the k-th largest is just the minimum of the array.
// - k = 1: the k-th largest is just the maximum of the array.
// - Array with duplicate values: k-th largest counts duplicates by position, not distinct values (e.g. [1,1,1], k=2 -> 1).
// - k greater than array length: invalid input, should be handled (e.g. throw exception or return sentinel).
// - Single-element array with k = 1: trivially returns that element.
// - Negative numbers: heap comparisons work the same regardless of sign, no special handling needed.
//
// ================= COMPLEXITY =================
// Time Complexity: Brute force full sort O(n log n) because sorting the entire array
//                   dominates the cost.
//                   Optimized min-heap of size k: O(n log k) because we process all n
//                   elements, and each heap insertion/removal costs O(log k).
//                   (Follow-up) QuickSelect: O(n) average case because each partition
//                   step eliminates roughly half the remaining candidates, similar to
//                   binary search, though O(n^2) worst case without a good pivot strategy.
// Space Complexity: Brute force full sort: O(log n) to O(n) depending on sort algorithm
//                     (e.g. Arrays.sort on primitives uses dual-pivot quicksort, O(log n)
//                     stack space; on objects it may use O(n) for merge sort).
//                    Optimized min-heap: O(k) extra space to hold the heap contents.
//                    QuickSelect: O(1) extra space (in-place partitioning), or O(log n)
//                     for recursion stack if implemented recursively.
//
// ================= INTERVIEW FOLLOW-UPS =================
// - Walk through why a min-heap (not a max-heap) of size k is the right choice here.
// - Can you describe how QuickSelect works and why its average case is O(n)? What causes its worst case O(n^2), and how do you avoid it (random pivot / median-of-medians)?
// - How would you find the k-th SMALLEST element instead? What changes?
// - How would this problem change if the array is a continuous stream of numbers (i.e., you can't re-scan) and you need the k-th largest at any point in time?
// - What if k can be very large, close to n? Does the min-heap approach still make sense, or would you switch strategies?
// - How would you find the k-th largest element across multiple sorted arrays (e.g. k-th largest across N machines' local sorted top-k lists)?
// - Is this problem stable with duplicate values? Does the answer definition need clarification with interviewers before coding?
// - How would you adapt this to also return the actual k largest elements, not just the k-th one?

import java.util.Arrays;
import java.util.PriorityQueue;

public class KthLargestElement {

    // Brute force: full sort, O(n log n)
    public static int findKthLargestBruteForce(int[] nums, int k) {
        int[] copy = nums.clone(); // don't mutate caller's array
        Arrays.sort(copy); // ascending order
        // the k-th largest is k positions from the end
        return copy[copy.length - k];
    }

    // Optimized: min-heap of size k, O(n log k)
    public static int findKthLargestHeap(int[] nums, int k) {
        // min-heap: smallest element always at the top
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        for (int num : nums) {
            minHeap.offer(num); // add current number
            if (minHeap.size() > k) {
                minHeap.poll(); // remove smallest, keep only top k largest so far
            }
        }
        // after processing all elements, top of heap is the k-th largest
        return minHeap.peek();
    }

    public static void main(String[] args) {
        int[] nums1 = {3, 2, 1, 5, 6, 4};
        int k1 = 2;
        // Expected: 5 (sorted descending [6,5,4,3,2,1], 2nd largest is 5)
        System.out.println("nums = " + Arrays.toString(nums1) + ", k = " + k1);
        System.out.println("Brute force: " + findKthLargestBruteForce(nums1, k1));
        System.out.println("Heap:        " + findKthLargestHeap(nums1, k1));

        int[] nums2 = {3, 2, 3, 1, 2, 4, 5, 5, 6};
        int k2 = 4;
        // Expected: 4 (sorted descending [6,5,5,4,3,3,2,2,1], 4th largest is 4)
        System.out.println("\nnums = " + Arrays.toString(nums2) + ", k = " + k2);
        System.out.println("Brute force: " + findKthLargestBruteForce(nums2, k2));
        System.out.println("Heap:        " + findKthLargestHeap(nums2, k2));

        int[] nums3 = {1};
        int k3 = 1;
        // Expected: 1 (single element array, k = 1)
        System.out.println("\nnums = " + Arrays.toString(nums3) + ", k = " + k3);
        System.out.println("Brute force: " + findKthLargestBruteForce(nums3, k3));
        System.out.println("Heap:        " + findKthLargestHeap(nums3, k3));
    }
}
