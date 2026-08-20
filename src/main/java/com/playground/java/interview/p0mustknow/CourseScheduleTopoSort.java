package com.playground.java.interview.p0mustknow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * PATTERN: Graph / Topological Sort (Kahn's Algorithm - BFS with In-degree)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given numCourses and prerequisite pairs [a, b] meaning "b must be taken
 * before a", determine if all courses can be finished, and if so, return one valid order.
 */
public class CourseScheduleTopoSort {

    // ================= PROBLEM =================
    // You have numCourses courses labeled 0 to numCourses-1, and a list of prerequisite
    // pairs [a, b] meaning "to take course a, you must first take course b" (edge b -> a).
    // Determine whether it's possible to finish ALL courses (i.e. no circular dependency),
    // and if possible, return a valid order to take them in.
    //
    // Example (solvable, no cycle):
    //   numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]
    //   Graph edges: 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3
    //   0
    //  / \
    // 1   2
    //  \ /
    //   3
    // Expected output: canFinish = true, one valid order = [0, 1, 2, 3]
    //
    // Example (unsolvable, has a cycle):
    //   numCourses = 2, prerequisites = [[1,0],[0,1]]
    //   Graph edges: 0 -> 1, 1 -> 0  (a cycle: 0 needs 1, and 1 needs 0)
    // Expected output: canFinish = false, order = [] (empty, impossible)
    //
    // ================= SIMPLE APPROACH =================
    // Try to simulate taking courses one at a time: repeatedly scan all courses and take any
    // course whose prerequisites are already satisfied. Keep looping until no more courses
    // can be taken. If all courses got taken, it's possible; otherwise there's a cycle.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Repeatedly scanning ALL courses on every pass to find one that's "ready" is wasteful -
    // each full pass is O(numCourses), and you might need up to O(numCourses) passes, giving
    // O(numCourses^2) or worse. It also doesn't cleanly generalize once you also need to
    // track "how many prerequisites remain" per course, which is exactly what an in-degree
    // array formalizes.
    //
    // ================= OPTIMIZED APPROACH =================
    // Kahn's Algorithm (BFS using an in-degree array + queue) - the primary approach here:
    // 1) Build an adjacency list: for each prerequisite [a, b], add edge b -> a (b must come first).
    // 2) Build an in-degree array: inDegree[a] = number of prerequisites course a still has.
    // 3) Push every course with inDegree == 0 into a queue (these can be taken right away).
    // 4) Repeatedly pop a course, add it to the result order, and for each course that depends
    //    on it, decrement that course's in-degree; if it hits 0, push it into the queue.
    // 5) If the result order contains all numCourses courses, it's possible (no cycle);
    //    otherwise some courses never reached in-degree 0, meaning a cycle exists among them.
    //
    // Alternative: DFS with 3-color (white/gray/black) cycle detection.
    // - WHITE = unvisited, GRAY = currently on the recursion stack (being explored),
    //   BLACK = fully explored (safe, no cycle through it).
    // - DFS from each unvisited node; if you ever step into a GRAY node, that's a back-edge
    //   -> cycle found -> return false immediately.
    // - When a node's DFS finishes, mark it BLACK and prepend it to the order list (so the
    //   final list, built via post-order, is naturally in reverse topological order - reverse it once at the end).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // In-degree array (int[]): tracks exactly how many unmet prerequisites each course has,
    // in O(1) space per course and O(1) decrement per edge processed - this is the core
    // signal for "is this course ready to be taken yet" (ready when it hits zero).
    // Queue (ArrayDeque) holding all currently in-degree-zero (ready) courses: this is what
    // makes it Kahn's ALGORITHM (a BFS-flavored traversal) - we always process courses in the
    // order they become ready, guaranteeing a valid topological order emerges naturally as we
    // dequeue them, with no extra sorting step needed.
    // Adjacency list (Map<Integer, List<Integer>>): lets us efficiently find "which courses
    // depend on this one" in O(1) average lookup plus O(out-degree) iteration, needed to
    // decrement the right in-degrees when a course is completed.
    //
    // ================= EDGE CASES =================
    // - numCourses = 0: trivially finishable, empty order.
    // - No prerequisites at all: any order works; Kahn's algorithm naturally outputs 0..n-1
    //   (or whatever order courses were pushed with in-degree 0 initially).
    // - Self-loop prerequisite [0, 0] (course depends on itself): in-degree never reaches 0,
    //   correctly detected as a cycle (impossible).
    // - Direct 2-cycle (as in the second example): both courses stay stuck with in-degree 1
    //   forever, correctly detected as impossible.
    // - Disconnected groups of courses (e.g. courses 0-1 have no relation to courses 2-3):
    //   both groups' in-degree-zero nodes start in the queue together; algorithm handles this fine.
    // - Duplicate prerequisite pairs: adjacency list may have duplicate edges, which just
    //   means an in-degree gets decremented an extra time for a duplicate edge - can cause
    //   incorrect results if not deduplicated; worth mentioning in an interview.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(V + E) where V = numCourses and E = number of prerequisite pairs -
    // building the adjacency list and in-degree array is O(V + E), and the BFS processes
    // every node once and every edge once (when decrementing in-degrees).
    // Space Complexity: O(V + E) - adjacency list stores all edges (O(E)), in-degree array
    // and queue are O(V), and the result order list is O(V).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you modify this to return ALL valid topological orders, not just one?
    // - How does Kahn's algorithm detect a cycle without explicit "visited/visiting" coloring?
    //   (If final order size < numCourses, whatever's left must be stuck in a cycle.)
    // - Compare Kahn's (BFS) vs DFS-based topological sort - when would you prefer one over the other?
    // - How would you extend this if prerequisites have weights/priorities (e.g. prefer
    //   taking cheaper/faster courses first when multiple are ready)?
    // - What if the course dependency graph is huge and edges arrive as a stream - can you
    //   detect a cycle incrementally rather than re-running the whole algorithm?
    // - How would you identify exactly WHICH courses are involved in a cycle (not just that one exists)?
    // - Why does DFS post-order + reversal produce a valid topological order?

    // Primary approach: Kahn's Algorithm - BFS using in-degree array + queue.
    // Returns a valid course order if possible, or an empty list if impossible (cycle exists).
    public static List<Integer> findCourseOrder(int numCourses, int[][] prerequisites) {
        // Step 1: build adjacency list (edge b -> a for prerequisite [a, b]) and in-degree array.
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        int[] inDegree = new int[numCourses];
        for (int i = 0; i < numCourses; i++) {
            adjacency.put(i, new ArrayList<>());
        }
        for (int[] pair : prerequisites) {
            int course = pair[0];
            int prereq = pair[1];
            adjacency.get(prereq).add(course); // prereq -> course
            inDegree[course]++;
        }

        // Step 2: seed the queue with every course that has no prerequisites left.
        Queue<Integer> queue = new ArrayDeque<>();
        for (int course = 0; course < numCourses; course++) {
            if (inDegree[course] == 0) {
                queue.add(course);
            }
        }

        // Step 3: repeatedly take a ready course, then "free up" courses that depended on it.
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int current = queue.poll();
            order.add(current);
            for (int next : adjacency.get(current)) {
                inDegree[next]--;
                if (inDegree[next] == 0) {
                    // Step: this course's last prerequisite was just satisfied - it's ready now.
                    queue.add(next);
                }
            }
        }

        // Step 4: if we could not schedule every course, some remain stuck in a cycle.
        if (order.size() != numCourses) {
            return new ArrayList<>(); // impossible
        }
        return order;
    }

    // Convenience wrapper matching the classic LeetCode signature: just true/false.
    public static boolean canFinish(int numCourses, int[][] prerequisites) {
        return !findCourseOrder(numCourses, prerequisites).isEmpty() || numCourses == 0;
    }

    // ---- Alternative approach (described in comments above): DFS with 3-color cycle detection ----
    // private static final int WHITE = 0, GRAY = 1, BLACK = 2;
    // boolean hasCycleDFS(int node, int[] color, Map<Integer, List<Integer>> adjacency) {
    //     color[node] = GRAY;
    //     for (int neighbor : adjacency.get(node)) {
    //         if (color[neighbor] == GRAY) return true;             // back-edge -> cycle
    //         if (color[neighbor] == WHITE && hasCycleDFS(neighbor, color, adjacency)) return true;
    //     }
    //     color[node] = BLACK;
    //     return false;
    // }

    public static void main(String[] args) {
        int numCourses1 = 4;
        int[][] prerequisites1 = {{1, 0}, {2, 0}, {3, 1}, {3, 2}};
        // Expected: canFinish = true, order = [0, 1, 2, 3]
        System.out.println("Case 1 (diamond dependency, solvable):");
        System.out.println("  canFinish: " + canFinish(numCourses1, prerequisites1));
        System.out.println("  order: " + findCourseOrder(numCourses1, prerequisites1));

        int numCourses2 = 2;
        int[][] prerequisites2 = {{1, 0}, {0, 1}};
        // Expected: canFinish = false, order = []
        System.out.println("Case 2 (direct 2-cycle, impossible):");
        System.out.println("  canFinish: " + canFinish(numCourses2, prerequisites2));
        System.out.println("  order: " + findCourseOrder(numCourses2, prerequisites2));

        int numCourses3 = 3;
        int[][] prerequisites3 = {}; // no prerequisites at all
        // Expected: canFinish = true, order = [0, 1, 2]
        System.out.println("Case 3 (no prerequisites):");
        System.out.println("  canFinish: " + canFinish(numCourses3, prerequisites3));
        System.out.println("  order: " + findCourseOrder(numCourses3, prerequisites3));

        int numCourses4 = 1;
        int[][] prerequisites4 = {{0, 0}}; // self-loop
        // Expected: canFinish = false, order = []
        System.out.println("Case 4 (self-loop, impossible):");
        System.out.println("  canFinish: " + canFinish(numCourses4, prerequisites4));
        System.out.println("  order: " + findCourseOrder(numCourses4, prerequisites4));

        int numCourses5 = 4;
        int[][] prerequisites5 = {{1, 0}}; // disconnected: courses 2 and 3 have no relation
        // Expected: canFinish = true, order contains 0,1,2,3 with 0 before 1
        System.out.println("Case 5 (disconnected groups of courses):");
        System.out.println("  canFinish: " + canFinish(numCourses5, prerequisites5));
        System.out.println("  order: " + findCourseOrder(numCourses5, prerequisites5));
    }
}
