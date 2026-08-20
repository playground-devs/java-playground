package com.playground.java.interview.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * PATTERN: Heap (Priority Queue) / Linked List Merge
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Merge k sorted linked lists into a single sorted linked list.
 */
public class MergeKSortedLists {

    // ================= PROBLEM =================
    // You are given an array of k linked lists, each already sorted in ascending order.
    // Merge all of them into one sorted linked list and return it.
    // Example: lists = [1->4->5, 1->3->4, 2->6] -> Output: 1->1->2->3->4->4->5->6
    //
    // ================= SIMPLE APPROACH =================
    // Walk every list, collect all node values into one big array/list, sort that
    // collection, then build a brand new linked list from the sorted values.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This ignores the fact that each individual list is already sorted. Sorting N total
    // values from scratch costs O(N log N) regardless of how many lists there are or how
    // sorted they already were - wasteful when k is small and each list is long.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a min-heap (PriorityQueue) that holds at most one node per list - specifically
    // the current "head" (smallest unconsumed value) of each list.
    // Step 1: Push the head node of every non-null list into the min-heap.
    // Step 2: Poll the smallest node from the heap, append it to the result list.
    // Step 3: If the polled node has a next node, push that next node into the heap
    //          (it becomes that list's new candidate for smallest-so-far).
    // Step 4: Repeat steps 2-3 until the heap is empty.
    // The heap always contains at most k nodes, so finding the next smallest overall is
    // O(log k) instead of O(k) (which a naive "compare all k heads" approach would need).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A min-heap of size k is ideal because at every step we need "the smallest among the
    // current k candidates" - exactly what a heap's peek/poll gives in O(log k). A naive
    // approach would linearly scan all k current heads every time to find the minimum,
    // costing O(k) per element and O(N*k) overall. The heap turns that per-step cost into
    // O(log k), giving O(N log k) total, which is much better than O(N log N) when k is
    // small compared to N (e.g. merging 500 lists of 10,000 elements each: log(500) vs
    // log(5,000,000) per step).
    //
    // ================= EDGE CASES =================
    // - Empty array of lists (lists.length == 0): return null.
    // - Array of lists where some entries are null (empty lists mixed with non-empty ones):
    //   skip nulls when seeding the heap.
    // - All lists are null/empty: result is null.
    // - Only one list (k = 1): heap approach still works, degenerates to a simple pass-through.
    // - Lists of very different lengths: heap naturally handles this, shorter lists just
    //   stop contributing once exhausted.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: Brute force O(N log N) where N is the total number of nodes across
    // all lists, dominated by sorting all values.
    // Optimized (min-heap): O(N log k) where k is the number of lists - we do N total
    // poll/offer operations, each costing O(log k) since the heap never holds more than
    // k elements at once.
    // Space Complexity: Brute force O(N) to store all values plus new nodes.
    // Optimized O(k) for the heap (holds at most k nodes at any time); the result list
    // reuses existing nodes so no extra space beyond the heap itself.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is a heap of size k better than comparing all k list heads directly every step?
    // - Could you solve this with divide-and-conquer pairwise merging instead? What's its complexity? (O(N log k), same as heap, by repeatedly merging pairs of lists.)
    // - How would this change if the lists were sorted in descending order instead?
    // - What if the total number of nodes N is very large and doesn't fit in memory - how would you merge lists stored across machines?
    // - How would you extend this to merge k sorted ARRAYS instead of linked lists?
    // - What if two nodes have equal values - does order between them matter, and how would the heap comparator need to handle ties?
    // - How would you modify this to also return the total count of merged nodes efficiently?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Brute force: collect all values, sort, rebuild a new list. O(N log N).
    public static ListNode mergeKListsBruteForce(ListNode[] lists) {
        List<Integer> values = new ArrayList<>();
        for (ListNode list : lists) {
            for (ListNode curr = list; curr != null; curr = curr.next) {
                values.add(curr.val);
            }
        }
        Collections.sort(values);

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : values) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    // Optimized: min-heap holding one candidate node per list. O(N log k).
    public static ListNode mergeKListsHeap(ListNode[] lists) {
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a, b) -> a.val - b.val);

        // Step 1: seed the heap with the head of every non-null list.
        for (ListNode list : lists) {
            if (list != null) {
                minHeap.offer(list);
            }
        }

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        // Step 2-4: repeatedly take the smallest node, attach it, push its successor.
        while (!minHeap.isEmpty()) {
            ListNode smallest = minHeap.poll();
            tail.next = smallest;
            tail = tail.next;
            if (smallest.next != null) {
                minHeap.offer(smallest.next);
            }
        }
        tail.next = null; // terminate cleanly (smallest.next may still hold old links)

        return dummy.next;
    }

    private static ListNode buildList(int[] values) {
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        for (int v : values) {
            curr.next = new ListNode(v);
            curr = curr.next;
        }
        return dummy.next;
    }

    private static String printList(ListNode head) {
        StringBuilder sb = new StringBuilder();
        while (head != null) {
            sb.append(head.val).append("->");
            head = head.next;
        }
        sb.append("null");
        return sb.toString();
    }

    public static void main(String[] args) {
        ListNode[] lists1 = {
                buildList(new int[]{1, 4, 5}),
                buildList(new int[]{1, 3, 4}),
                buildList(new int[]{2, 6})
        };
        System.out.println("Input: [1->4->5, 1->3->4, 2->6]");
        // Expected: 1->1->2->3->4->4->5->6->null
        System.out.println("Heap merge: " + printList(mergeKListsHeap(lists1)));

        ListNode[] lists2 = {
                buildList(new int[]{1, 4, 5}),
                buildList(new int[]{1, 3, 4}),
                buildList(new int[]{2, 6})
        };
        // Expected: 1->1->2->3->4->4->5->6->null
        System.out.println("Brute force merge: " + printList(mergeKListsBruteForce(lists2)));

        ListNode[] lists3 = {};
        System.out.println("\nInput: [] (no lists)");
        // Expected: null
        System.out.println("Heap merge: " + printList(mergeKListsHeap(lists3)));

        ListNode[] lists4 = {null, buildList(new int[]{}), buildList(new int[]{2})};
        System.out.println("\nInput: [null, empty, 2]");
        // Expected: 2->null
        System.out.println("Heap merge: " + printList(mergeKListsHeap(lists4)));
    }
}
