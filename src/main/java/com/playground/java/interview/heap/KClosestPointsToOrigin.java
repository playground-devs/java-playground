package com.playground.java.interview.heap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * PATTERN: Heap (Priority Queue)
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Find the k points closest to the origin among a list of 2D points.
 */
public class KClosestPointsToOrigin {

    // ================= PROBLEM =================
    // Given an array of 2D points (each point is [x, y]) and an integer k, return the k
    // points closest to the origin (0, 0), using Euclidean distance. Comparing squared
    // distance is enough (avoids sqrt and floating point).
    // Example: points = [[1,3],[-2,2],[5,8],[0,1]], k = 2
    // Distances^2: [1,3]->10, [-2,2]->8, [5,8]->89, [0,1]->1
    // Closest 2: [0,1] (1), [-2,2] (8) -> Output: [[0,1],[-2,2]] (order may vary)
    //
    // ================= SIMPLE APPROACH =================
    // Compute the squared distance for every point, sort ALL points by that distance
    // ascending, then take the first k points from the sorted result.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Sorting the entire array costs O(n log n), even though we only need the k smallest
    // distances, not a full ordering of all n points. When k is small compared to n
    // (e.g. k=5 out of a million points), this does far more work than necessary.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a MAX-heap (PriorityQueue with a comparator ordering by distance DESCENDING)
    // bounded to size k.
    // Step 1: For each point, compute its squared distance from the origin.
    // Step 2: Push the point onto the max-heap.
    // Step 3: If the heap size exceeds k, poll (remove) the top of the heap - that's
    //          currently the FARTHEST point among the k+1 candidates, so it's safe to evict.
    // Step 4: After processing all points, the heap holds exactly the k closest points.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A max-heap bounded to size k keeps the "current farthest of the best k so far" at
    // the top, so we can compare each new point against it in O(1) (peek) and evict in
    // O(log k) if the new point is closer. This costs O(n log k) total instead of the
    // O(n log n) full sort - a real win when k is much smaller than n. A min-heap of the
    // whole array would let the smallest bubble to the top, but we'd still need to pop k
    // times AND we'd have paid O(n) to build the heap over all n elements anyways; bounding
    // the heap to size k is what actually saves work here.
    //
    // ================= EDGE CASES =================
    // - k == points.length: return all points (order may differ from input order).
    // - k == 1: only the single closest point.
    // - Duplicate points, e.g. two points both at [1,1]: both count separately toward k.
    // - A point exactly at the origin, [0,0]: squared distance 0, always closest.
    // - Points with negative coordinates: squaring handles sign automatically.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(n log n) for the full sort.
    // Optimized: O(n log k) - each of the n points does at most one O(log k) heap operation.
    // Space Complexity: Brute force O(n) for the sorted copy (or O(log n) if sorting in place).
    // Optimized: O(k) for the bounded heap.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why a MAX-heap here instead of a MIN-heap?
    // - How would QuickSelect (partition-based) solve this in O(n) average time?
    // - How would you handle this if points arrived as a continuous stream (can't re-scan)?
    // - What if k is very close to n - does the heap approach still make sense?
    // - How would you adapt this for 3D points or general n-dimensional points?
    // - Does the order of the k returned points matter? What if the interviewer wants them sorted by distance?
    // - How would ties (equal distances) be handled if the interviewer wants a stable, deterministic result?

    // Brute force: sort all points by squared distance, take first k. O(n log n)
    public static int[][] kClosestBruteForce(int[][] points, int k) {
        int[][] copy = points.clone(); // shallow copy is fine, we only reorder references
        Arrays.sort(copy, Comparator.comparingLong(KClosestPointsToOrigin::squaredDistance));
        return Arrays.copyOfRange(copy, 0, k);
    }

    // Optimized: max-heap of size k. O(n log k)
    public static int[][] kClosestHeap(int[][] points, int k) {
        // Max-heap ordered by squared distance descending, so the farthest of the "best k" is on top.
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>(
                (a, b) -> Long.compare(squaredDistance(b), squaredDistance(a)));

        for (int[] point : points) {
            maxHeap.offer(point);
            if (maxHeap.size() > k) {
                maxHeap.poll(); // evict the current farthest among k+1 candidates
            }
        }

        int[][] result = new int[maxHeap.size()][];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = maxHeap.poll();
        }
        return result;
    }

    private static long squaredDistance(int[] point) {
        long x = point[0];
        long y = point[1];
        return x * x + y * y;
    }

    public static void main(String[] args) {
        int[][] points1 = {{1, 3}, {-2, 2}, {5, 8}, {0, 1}};
        int k1 = 2;
        System.out.println("points = " + Arrays.deepToString(points1) + ", k = " + k1);
        // Expected: [[0,1],[-2,2]] (order may vary), distances^2 = 1 and 8
        System.out.println("Brute force: " + Arrays.deepToString(kClosestBruteForce(points1, k1)));
        System.out.println("Heap:        " + Arrays.deepToString(kClosestHeap(points1, k1)));

        int[][] points2 = {{3, 3}, {5, -1}, {-2, 4}};
        int k2 = 2;
        // Expected: [[3,3],[-2,4]] (distances^2: 18, 26, 20 -> closest two are 18 and 20)
        System.out.println("\npoints = " + Arrays.deepToString(points2) + ", k = " + k2);
        System.out.println("Brute force: " + Arrays.deepToString(kClosestBruteForce(points2, k2)));
        System.out.println("Heap:        " + Arrays.deepToString(kClosestHeap(points2, k2)));

        int[][] points3 = {{0, 0}, {1, 1}};
        int k3 = 2;
        // Expected: both points returned since k == points.length; [0,0] is closest (distance 0)
        System.out.println("\npoints = " + Arrays.deepToString(points3) + ", k = " + k3 + " (k == points.length)");
        System.out.println("Brute force: " + Arrays.deepToString(kClosestBruteForce(points3, k3)));
        System.out.println("Heap:        " + Arrays.deepToString(kClosestHeap(points3, k3)));
    }
}
