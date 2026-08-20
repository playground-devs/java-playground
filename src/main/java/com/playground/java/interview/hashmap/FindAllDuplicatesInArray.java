package com.playground.java.interview.hashmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PATTERN: HashMap / In-place Index Marking
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an array of integers where each value is between 1 and n
 * (n = array length) and some values appear twice, find all the duplicate values.
 */
public class FindAllDuplicatesInArray {

    // ================= PROBLEM =================
    // You get an array of n integers where every value is between 1 and n (inclusive),
    // and some values appear exactly twice while others appear exactly once.
    // You need to find all the values that appear twice.
    // Example: nums = [4, 3, 2, 7, 8, 2, 3, 1] -> output = [2, 3]
    //
    // ================= SIMPLE APPROACH =================
    // Use a HashSet. Walk through the array; for each number, check if it's already
    // in the set. If yes, it's a duplicate, add it to the result list. If no, add
    // it to the set for future comparisons.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works correctly and is O(n) time, but it uses O(n) extra space for the
    // HashSet. The problem often specifically asks for an O(1) extra space solution
    // by taking advantage of the special constraint that values are in range [1, n].
    //
    // ================= OPTIMIZED APPROACH =================
    // Since every value is between 1 and n, each value can be mapped to a valid
    // index (value - 1). Walk through the array; for each number, use its absolute
    // value to find the corresponding index, and negate the value stored at that
    // index. If we visit an index and find the value there is already negative,
    // that means we've seen this number before - it's a duplicate.
    // This uses the array itself as a "visited" marker, avoiding any extra structure.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - the array's own values, combined with
    // their sign, act as an implicit boolean "seen" array. Because values are
    // guaranteed to be in the range [1, n], every value maps to a unique valid
    // index, letting us repurpose the array itself as O(1) extra space bookkeeping.
    //
    // ================= EDGE CASES =================
    // - No duplicates at all: result is an empty list.
    // - All values are duplicated: result contains every distinct value.
    // - Array with a single element: cannot have a duplicate (trivial case).
    // - Negative-looking values encountered mid-algorithm - remember to use Math.abs()
    //   when reading, since we intentionally negate values as markers.
    // - Restoring the array afterward, if the caller needs the original values preserved.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized in-place approach - one pass through
    // the array with O(1) work per element. Brute force with a HashSet is also O(n).
    // Space Complexity: O(1) extra space for the optimized approach (not counting
    // the output list) - it reuses the input array. Brute force uses O(n) extra
    // space for the HashSet.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does this technique only work because values are guaranteed to be in range [1, n]?
    // - How would you restore the original array (un-negate all values) after finding duplicates?
    // - What if a value could appear three or more times - does this technique still correctly detect it as "a duplicate"?
    // - How would you find the single missing number in a similar [1,n] array using the same style of trick?
    // - What if the array could contain zero or negative numbers - would this in-place trick still work?
    // - How would you modify this to also return the count of how many times each duplicate occurred?
    // - Is mutating the input array an acceptable trade-off in an interview - what would you ask the interviewer?

    // Brute force: HashSet tracks seen values. O(n) time, O(n) space.
    public static List<Integer> findDuplicatesBruteForce(int[] nums) {
        List<Integer> duplicates = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            if (!seen.add(num)) {
                // add() returns false if the value was already present.
                duplicates.add(num);
            }
        }
        return duplicates;
    }

    // Optimized: negate the value at the index corresponding to each number seen. O(n) time, O(1) space.
    public static List<Integer> findDuplicatesOptimized(int[] nums) {
        List<Integer> duplicates = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            // Use the absolute value since earlier iterations may have negated this entry.
            int value = Math.abs(nums[i]);
            int index = value - 1;
            if (nums[index] < 0) {
                // Already negated once before, meaning we've seen "value" before.
                duplicates.add(value);
            } else {
                // Mark this value as "seen" by negating the number at its index.
                nums[index] = -nums[index];
            }
        }
        // Restore the array to its original values (good practice, avoids surprising the caller).
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Math.abs(nums[i]);
        }
        return duplicates;
    }

    public static void main(String[] args) {
        int[] nums1 = {4, 3, 2, 7, 8, 2, 3, 1};
        // Expected: [2, 3]
        System.out.println("Input: [4,3,2,7,8,2,3,1]");
        System.out.println("Brute force output: " + findDuplicatesBruteForce(nums1.clone()));
        System.out.println("Optimized output: " + findDuplicatesOptimized(nums1));

        int[] nums2 = {1, 1, 2};
        // Expected: [1]
        System.out.println("\nInput: [1,1,2]");
        System.out.println("Optimized output: " + findDuplicatesOptimized(nums2));

        int[] nums3 = {1, 2, 3};
        // Expected: [] (no duplicates)
        System.out.println("\nInput: [1,2,3] (no duplicates)");
        System.out.println("Optimized output: " + findDuplicatesOptimized(nums3));
    }
}
