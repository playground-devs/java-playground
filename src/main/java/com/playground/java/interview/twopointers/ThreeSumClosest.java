package com.playground.java.interview.twopointers;

import java.util.Arrays;

/**
 * PATTERN: Two Pointers / Sorting
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Find the sum of three numbers in an array that is closest to a
 * given target value.
 */
public class ThreeSumClosest {

    // ================= PROBLEM =================
    // You get an array of integers and a target number.
    // You need to find three numbers whose sum is as close as possible to the target,
    // and return that sum (not the indices or the triplet itself).
    // Example: nums = [-1, 2, 1, -4], target = 1 -> output = 2
    // because (-1 + 2 + 1) = 2, which is the closest possible sum to 1.
    //
    // ================= SIMPLE APPROACH =================
    // Try every possible triplet of three different numbers using three nested loops.
    // For each triplet, compute its sum and compare how close it is to the target
    // compared to the best sum found so far. Keep track of the closest sum.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Three nested loops give O(n^3) time complexity, which becomes very slow as
    // the array grows (e.g., thousands of elements).
    //
    // ================= OPTIMIZED APPROACH =================
    // First, sort the array. Then, fix one number at a time (the first number of
    // the triplet) and use two pointers - one starting right after the fixed number
    // (left) and one at the end of the array (right) - to find the best pair to go
    // with it.
    // At each step, compute the sum of the fixed number plus nums[left] plus nums[right].
    // If this sum is closer to the target than the best found so far, update the answer.
    // If the sum is less than the target, move left forward (to increase the sum).
    // If the sum is more than the target, move right backward (to decrease the sum).
    // If the sum equals the target exactly, that's the closest possible, return immediately.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Sorting the array first allows the two-pointer technique to work: because the
    // array is ordered, moving left forward always increases the sum and moving right
    // backward always decreases the sum, letting us systematically narrow down the
    // best pair in O(n) time per fixed number, instead of checking all pairs in O(n^2).
    //
    // ================= EDGE CASES =================
    // - Array with exactly three elements: the only possible triplet is the answer.
    // - Array with fewer than three elements: invalid input, no triplet exists.
    // - Multiple triplets with the same closest sum: any one of them is a valid answer.
    // - All numbers identical: the sum is just three times that number.
    // - Target is exactly achievable by some triplet: return that exact sum immediately.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2) for the optimized approach - O(n log n) to sort, then
    // an outer loop of O(n) combined with an inner two-pointer scan of O(n), giving
    // O(n^2) overall. Brute force is O(n^3).
    // Space Complexity: O(log n) to O(n) for the optimized approach due to the sort's
    // internal recursion/temp storage (implementation-dependent); O(1) extra space
    // beyond that. Brute force is O(1) extra space but far slower in time.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does sorting the array first make the two-pointer technique valid here?
    // - How would you modify this to return the actual triplet, not just the sum?
    // - What if you needed all triplets whose sum is within some tolerance of the target, not just the closest one?
    // - How would you extend this idea to "Four Sum Closest" or "K Sum Closest"?
    // - Can you skip duplicate values while fixing the first number to avoid redundant work (optimization, not correctness)?
    // - What happens if there are multiple triplets that tie for the closest sum - does it matter which one you return?
    // - How would early-exit (sum == target) improve average-case performance?

    // Brute force: check every triplet with three nested loops. O(n^3).
    public static int threeSumClosestBruteForce(int[] nums, int target) {
        if (nums == null || nums.length < 3) {
            throw new IllegalArgumentException("Array must have at least three elements");
        }
        int closestSum = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                for (int k = j + 1; k < nums.length; k++) {
                    int sum = nums[i] + nums[j] + nums[k];
                    if (Math.abs(sum - target) < Math.abs(closestSum - target)) {
                        closestSum = sum;
                    }
                }
            }
        }
        return closestSum;
    }

    // Optimized: sort + two pointers, fixing one number at a time. O(n^2).
    public static int threeSumClosestOptimized(int[] nums, int target) {
        if (nums == null || nums.length < 3) {
            throw new IllegalArgumentException("Array must have at least three elements");
        }
        int[] sorted = nums.clone();
        Arrays.sort(sorted);

        int closestSum = sorted[0] + sorted[1] + sorted[2];

        for (int i = 0; i < sorted.length - 2; i++) {
            int left = i + 1;
            int right = sorted.length - 1;

            while (left < right) {
                int sum = sorted[i] + sorted[left] + sorted[right];

                // Update the best answer if this sum is closer to the target.
                if (Math.abs(sum - target) < Math.abs(closestSum - target)) {
                    closestSum = sum;
                }

                if (sum == target) {
                    // Cannot get any closer than an exact match.
                    return sum;
                } else if (sum < target) {
                    // Sum too small, move left pointer forward to increase it.
                    left++;
                } else {
                    // Sum too large, move right pointer backward to decrease it.
                    right--;
                }
            }
        }
        return closestSum;
    }

    public static void main(String[] args) {
        int[] nums1 = {-1, 2, 1, -4};
        int target1 = 1;
        // Expected: 2 (-1 + 2 + 1 = 2)
        System.out.println("Input: nums=[-1,2,1,-4], target=1");
        System.out.println("Brute force output: " + threeSumClosestBruteForce(nums1, target1));
        System.out.println("Optimized output: " + threeSumClosestOptimized(nums1, target1));

        int[] nums2 = {0, 0, 0};
        int target2 = 1;
        // Expected: 0 (only possible triplet sum)
        System.out.println("\nInput: nums=[0,0,0], target=1");
        System.out.println("Optimized output: " + threeSumClosestOptimized(nums2, target2));

        int[] nums3 = {1, 1, 1, 0};
        int target3 = 100;
        // Expected: 3 (largest possible sum, since target is far beyond any triplet)
        System.out.println("\nInput: nums=[1,1,1,0], target=100 (target far outside range)");
        System.out.println("Optimized output: " + threeSumClosestOptimized(nums3, target3));
    }
}
