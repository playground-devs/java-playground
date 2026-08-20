package com.playground.java.interview.bitmanipulation;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN: Bit Manipulation / XOR
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an array where every element appears exactly twice except for one
 * element which appears exactly once, find that single element.
 */
public class SingleNumber {

    // ================= PROBLEM =================
    // You get an array of integers. Every number appears exactly twice, except for exactly one
    // number which appears only once. Find that number.
    // Example: nums = [4, 1, 2, 1, 2] -> output = 4 (4 is the only number appearing once).
    //
    // ================= SIMPLE APPROACH =================
    // Use a HashMap to count how many times each number appears. Then scan the map and return
    // the number whose count is 1.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works in O(n) time but uses O(n) extra space for the HashMap. Since the numbers pair
    // up perfectly except for one, there is a purely arithmetic trick that needs O(1) space.
    //
    // ================= OPTIMIZED APPROACH =================
    // XOR every element in the array together into a single accumulator, starting at 0.
    // Because XOR-ing a number with itself gives 0 (a ^ a = 0), and XOR-ing any number with 0
    // leaves it unchanged (a ^ 0 = a), every pair of duplicate numbers cancels itself out to 0
    // as we XOR through the array, leaving only the single unpaired number by the end.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No data structure is needed at all - a single integer accumulator and the algebraic
    // properties of XOR (self-canceling, commutative, associative) do all the work, which is
    // exactly what brings the space complexity down from O(n) to O(1).
    //
    // ================= EDGE CASES =================
    // - Array with a single element: that element is the answer (it has no pair).
    // - Negative numbers: XOR works correctly on two's complement representation regardless of sign.
    // - The unpaired number appears first, last, or in the middle of the array - order does not
    //   matter since XOR is commutative and associative.
    // - Large arrays: still O(n) time, O(1) space, no overflow concerns since XOR operates bitwise.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - one pass XOR-ing every element exactly once.
    // Space Complexity: O(1) - only a single integer accumulator is used, versus O(n) for the
    // HashMap-based brute force.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you solve this if every number appeared THREE times except for one (Single Number II)?
    // - How would you solve this if there were exactly TWO unpaired numbers instead of one (Single Number III)?
    // - Why does XOR specifically work here, and would addition/subtraction based tricks work as well or fail?
    // - How would you find the single number if the "pair count" were not fixed (arbitrary duplicates, only one truly unique)?
    // - Can this XOR trick be extended to find a missing number in a range (e.g. 1..n with one missing)?
    // - What's the time/space tradeoff versus using a HashSet where you add on first sight and remove on second sight?

    // Brute force: HashMap counting.
    public static int singleNumberBruteForce(int[] nums) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int num : nums) {
            counts.merge(num, 1, Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("No single number found");
    }

    // Optimized: XOR all elements together; pairs cancel out, leaving the unpaired number.
    public static int singleNumberOptimized(int[] nums) {
        int result = 0;
        for (int num : nums) {
            // Step: a^a = 0 cancels duplicates, a^0 = a preserves the unpaired number.
            result ^= num;
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {4, 1, 2, 1, 2};
        // Expected: 4
        System.out.println("Input: [4,1,2,1,2]");
        System.out.println("Output: " + singleNumberOptimized(nums1));

        int[] nums2 = {-1, -1, 7};
        // Expected: 7 (negative numbers handled correctly)
        System.out.println("\nInput: [-1,-1,7]");
        System.out.println("Output: " + singleNumberOptimized(nums2));

        int[] nums3 = {99};
        // Expected: 99 (single element, no pair)
        System.out.println("\nInput: [99] (single element)");
        System.out.println("Output: " + singleNumberOptimized(nums3));
    }
}
