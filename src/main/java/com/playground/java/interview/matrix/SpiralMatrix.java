package com.playground.java.interview.matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Matrix / Boundary Pointers
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Given an m x n matrix, return all its elements in spiral order
 * (right along the top, down the right side, left along the bottom, up the left side, repeat).
 */
public class SpiralMatrix {

    // ================= PROBLEM =================
    // You are given a matrix with m rows and n columns. Return all the elements of the matrix
    // visited in spiral order, starting from the top-left corner, going right, then down, then
    // left, then up, and spiraling inward until every element has been visited.
    // Example: matrix = [[1,2,3],
    //                     [4,5,6],
    //                     [7,8,9]]
    //          -> output = [1,2,3,6,9,8,7,4,5]
    //
    // ================= SIMPLE APPROACH =================
    // Simulate walking through the matrix cell by cell, keeping track of the current direction
    // (right, down, left, up) and a "visited" grid. Move forward in the current direction; if
    // the next cell is out of bounds or already visited, turn 90 degrees and continue.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works, but needs an extra m x n "visited" grid to know when to turn, which is extra
    // space that isn't strictly necessary. It also requires careful bounds/visited checks on
    // every single step, which is more error-prone than tracking shrinking boundaries directly.
    //
    // ================= OPTIMIZED APPROACH =================
    // Track four boundary pointers: top, bottom, left, right - representing the current
    // unvisited rectangle of the matrix. Repeatedly:
    // 1) Walk left-to-right along the "top" row, then increment top (that row is done).
    // 2) Walk top-to-bottom along the "right" column, then decrement right (that column is done).
    // 3) If top <= bottom still, walk right-to-left along the "bottom" row, then decrement bottom.
    // 4) If left <= right still, walk bottom-to-top along the "left" column, then increment left.
    // Repeat until the boundaries cross (top > bottom or left > right), meaning every cell has
    // been visited.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Four simple integer boundary pointers are enough to represent "what part of the matrix is
    // still unvisited" - this eliminates the need for a separate visited grid entirely, because
    // the shrinking rectangle IS the visited/unvisited boundary. This makes the algorithm O(1)
    // extra space (aside from the output list) instead of O(m*n) for a visited grid.
    //
    // ================= EDGE CASES =================
    // - Empty matrix (0 rows or 0 columns): return an empty list.
    // - Single row matrix: spiral is just that row left to right.
    // - Single column matrix: spiral is just that column top to bottom.
    // - Square matrix vs rectangular (non-square) matrix - the boundary checks (top<=bottom,
    //   left<=right) after each side are essential to avoid re-visiting or double-counting
    //   cells when rows != columns.
    // - 1x1 matrix: output is just that single element.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(m*n) - every cell in the matrix is visited exactly once.
    // Space Complexity: O(1) extra space (excluding the output list, which necessarily holds
    // all m*n elements).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you generate a spiral matrix (the reverse problem: fill an n x n matrix with 1..n^2 in spiral order)?
    // - How would you spiral in the opposite direction (counter-clockwise, or starting from a different corner)?
    // - How do the boundary checks change for a non-square (rectangular) matrix versus a square one?
    // - Can you solve this recursively instead of iteratively, peeling off one "ring" per recursive call?
    // - How would you handle a matrix given as a stream of rows rather than fully in memory?
    // - What extra check prevents double-counting elements when the remaining rectangle collapses to a single row or column?

    // Optimized: four shrinking boundary pointers (top, bottom, left, right).
    public static List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return result;
        }

        int top = 0;
        int bottom = matrix.length - 1;
        int left = 0;
        int right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // Step 1: walk left-to-right along the top row.
            for (int col = left; col <= right; col++) {
                result.add(matrix[top][col]);
            }
            top++;

            // Step 2: walk top-to-bottom along the right column.
            for (int row = top; row <= bottom; row++) {
                result.add(matrix[row][right]);
            }
            right--;

            // Step 3: walk right-to-left along the bottom row (if a bottom row still remains).
            if (top <= bottom) {
                for (int col = right; col >= left; col--) {
                    result.add(matrix[bottom][col]);
                }
                bottom--;
            }

            // Step 4: walk bottom-to-top along the left column (if a left column still remains).
            if (left <= right) {
                for (int row = bottom; row >= top; row--) {
                    result.add(matrix[row][left]);
                }
                left++;
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int[][] matrix1 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        // Expected: [1, 2, 3, 6, 9, 8, 7, 4, 5]
        System.out.println("Input: [[1,2,3],[4,5,6],[7,8,9]]");
        System.out.println("Output: " + spiralOrder(matrix1));

        int[][] matrix2 = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}};
        // Expected: [1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7]
        System.out.println("\nInput: [[1,2,3,4],[5,6,7,8],[9,10,11,12]] (rectangular)");
        System.out.println("Output: " + spiralOrder(matrix2));

        int[][] matrix3 = {};
        // Expected: [] (empty matrix)
        System.out.println("\nInput: [] (empty)");
        System.out.println("Output: " + spiralOrder(matrix3));
    }
}
