package com.playground.java.interview.slidingwindow;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Sliding Window / HashMap
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an array representing fruit types on a row of trees, find
 * the maximum number of fruits you can collect if you have only 2 baskets, each basket
 * holding only one type of fruit (equivalent to "longest subarray with at most 2 distinct values").
 */
public class FruitIntoBaskets {

    // ================= PROBLEM =================
    // You get an array where each element represents the type of fruit on a tree.
    // You have exactly 2 baskets, and each basket can only hold one type of fruit
    // (unlimited amount of that type). You start at any tree and move right, picking
    // exactly one fruit per tree, and must stop once you can't fit a fruit's type
    // into either basket. Find the maximum number of fruits you can collect.
    // This is really just: find the longest subarray with at most 2 distinct values.
    // Example: fruits = [1, 2, 1] -> output = 3 (all fit in 2 baskets: type 1 and type 2)
    // Example: fruits = [0, 1, 2, 2] -> output = 3 (subarray [1, 2, 2])
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible subarray (every start and end pair). For each subarray,
    // count how many distinct fruit types it contains. If it has at most 2 distinct
    // types and is longer than the best found so far, remember its length.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // There are O(n^2) subarrays, and counting distinct types for each can take up
    // to O(n) time, giving O(n^3) total time in the worst case - too slow for large inputs.
    //
    // ================= OPTIMIZED APPROACH =================
    // This is the sliding window "at most K distinct values" pattern with K fixed at 2.
    // Use two pointers, left and right, and a HashMap counting fruit-type frequency
    // in the current window. Expand right, adding the new fruit type to the map.
    // Whenever the map has more than 2 distinct fruit types, shrink from the left:
    // decrement the leftmost fruit's count, remove it from the map if it hits zero,
    // then move left forward. Track the maximum window length seen.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A HashMap<Integer, Integer> mapping fruit type to its count in the current
    // window gives O(1) average updates and lets the map's size directly represent
    // "how many distinct fruit types are currently in our baskets", enabling a
    // single O(n) pass instead of repeatedly rescanning subarrays.
    //
    // ================= EDGE CASES =================
    // - Array with only one type of fruit: the whole array is collectible.
    // - Array with exactly 2 types throughout: the whole array is collectible.
    // - Array with more than 2 types scattered evenly: window shrinks frequently.
    // - Empty array: answer is 0.
    // - Single tree/element: answer is 1.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the sliding window approach - both pointers move
    // forward a total of at most n steps combined. Brute force is O(n^3) (or O(n^2)
    // with incremental distinct counting).
    // Space Complexity: O(1) for the optimized approach in this specific variant -
    // the map holds at most 3 entries at any time (2 valid + 1 that triggers shrink)
    // before being pruned back down to 2, which is a constant bound.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How is this problem exactly the same as "longest substring with at most K distinct characters" with K=2?
    // - How would you generalize this to allow M baskets/distinct types instead of just 2?
    // - Why is the space complexity O(1) here specifically, when the general K-distinct version is O(K)?
    // - How would you also return the actual start and end indices of the best subarray?
    // - What if you could swap out one basket's fruit type mid-window under certain rules - how would that change the approach?
    // - Could this be solved with two pointers and no HashMap, using two counter variables instead, given only 2 types allowed?
    // - How would this scale for a very long row of trees, e.g., streamed one at a time?

    // Brute force: check every subarray, count distinct fruit types. O(n^3) worst case.
    public static int totalFruitBruteForce(int[] fruits) {
        if (fruits == null || fruits.length == 0) {
            return 0;
        }
        int maxCollected = 0;
        for (int start = 0; start < fruits.length; start++) {
            Map<Integer, Integer> typeCount = new HashMap<>();
            for (int end = start; end < fruits.length; end++) {
                typeCount.put(fruits[end], typeCount.getOrDefault(fruits[end], 0) + 1);
                if (typeCount.size() <= 2) {
                    maxCollected = Math.max(maxCollected, end - start + 1);
                } else {
                    break;
                }
            }
        }
        return maxCollected;
    }

    // Optimized: sliding window with at most 2 distinct fruit types. O(n) time.
    public static int totalFruitOptimized(int[] fruits) {
        if (fruits == null || fruits.length == 0) {
            return 0;
        }
        Map<Integer, Integer> basketCounts = new HashMap<>();
        int left = 0;
        int maxCollected = 0;

        for (int right = 0; right < fruits.length; right++) {
            basketCounts.put(fruits[right], basketCounts.getOrDefault(fruits[right], 0) + 1);

            // Shrink the window while more than 2 distinct fruit types are present.
            while (basketCounts.size() > 2) {
                int leftFruit = fruits[left];
                basketCounts.put(leftFruit, basketCounts.get(leftFruit) - 1);
                if (basketCounts.get(leftFruit) == 0) {
                    basketCounts.remove(leftFruit);
                }
                left++;
            }

            // Window [left, right] now has at most 2 distinct fruit types.
            maxCollected = Math.max(maxCollected, right - left + 1);
        }
        return maxCollected;
    }

    public static void main(String[] args) {
        int[] fruits1 = {1, 2, 1};
        // Expected: 3 (all fruits fit, only 2 types present)
        System.out.println("Input: [1,2,1]");
        System.out.println("Brute force output: " + totalFruitBruteForce(fruits1));
        System.out.println("Optimized output: " + totalFruitOptimized(fruits1));

        int[] fruits2 = {0, 1, 2, 2};
        // Expected: 3 (subarray [1,2,2])
        System.out.println("\nInput: [0,1,2,2]");
        System.out.println("Optimized output: " + totalFruitOptimized(fruits2));

        int[] fruits3 = {1, 2, 3, 2, 2};
        // Expected: 4 (subarray [2,3,2,2])
        System.out.println("\nInput: [1,2,3,2,2]");
        System.out.println("Optimized output: " + totalFruitOptimized(fruits3));
    }
}
