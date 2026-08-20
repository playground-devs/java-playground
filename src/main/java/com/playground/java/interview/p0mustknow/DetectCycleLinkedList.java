package com.playground.java.interview.p0mustknow;

import java.util.HashSet;
import java.util.Set;

/**
 * PATTERN: Linked List / Two Pointers
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given the head of a linked list, determine if it has a cycle, and if so, find the node where the cycle begins.
 */
public class DetectCycleLinkedList {

    // ================= PROBLEM =================
    // A linked list normally ends in null. But sometimes a node's "next" pointer
    // accidentally (or intentionally) points back to an earlier node, creating a loop.
    // We need to detect if such a loop (cycle) exists, and as a bonus, find the exact
    // node where the cycle starts.
    // Example: 1->2->3->4->5->back to 3 (cycle starts at node with value 3)
    // Visually: 1->2->3->4->5
    //                 ^      |
    //                 |______|
    // Output: hasCycle = true, cycle starts at node with value 3.
    //
    // ================= SIMPLE APPROACH =================
    // Walk the list and keep a HashSet of every node reference you have already visited.
    // Before moving to the next node, check if it is already in the set.
    // If yes, you found a cycle (and that node is exactly where the cycle begins,
    // since it's the first node you are seeing for the second time).
    // If you reach null, there is no cycle.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works but uses O(n) extra space to remember every visited node.
    // For a very long list, this could be a lot of memory just to answer a yes/no
    // question (plus find one node). We can do better with O(1) extra space.
    //
    // ================= OPTIMIZED APPROACH =================
    // Floyd's Cycle Detection (the "tortoise and hare" algorithm):
    // Use two pointers, slow (moves 1 step at a time) and fast (moves 2 steps at a time).
    // If there is no cycle, fast reaches null first and we stop.
    // If there is a cycle, fast will eventually "lap" slow and they meet inside the cycle -
    // this proves a cycle exists.
    //
    // To find the START of the cycle once a meeting point is found:
    // Reset one pointer to head, leave the other at the meeting point.
    // Move both one step at a time - the node where they meet again is the cycle start.
    // This works because of the math relationship between the distance from head to the
    // cycle start, and the distance around the cycle (proven by Floyd's algorithm).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed for the optimized approach - just two pointer
    // variables. That is exactly why it beats the HashSet approach: we trade O(n) space
    // for O(1) space by relying on relative pointer speed instead of memory of visited nodes.
    //
    // ================= EDGE CASES =================
    // - Empty list (head == null): no cycle, return false / null immediately.
    // - Single node with no self-loop: no cycle.
    // - Single node that points to itself: cycle of length 1, cycle start is that node.
    // - Cycle starts at the head itself (entire list is one big loop).
    // - No cycle at all, normal list ending in null.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - each node is visited a bounded number
    // of times (HashSet: once each; Floyd's: fast pointer traverses at most ~2n steps).
    // Space Complexity: O(n) for the HashSet approach (storing visited node references);
    // O(1) for Floyd's algorithm (only two pointer variables regardless of list size).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you prove why slow and fast pointers must eventually meet if there is a cycle?
    // - Can you prove why resetting one pointer to head finds the exact cycle start?
    // - How would you find the length of the cycle once detected?
    // - How would you remove the cycle (make the list null-terminated again)?
    // - What if the list is extremely large - does Floyd's algorithm still work in O(1) space?
    // - How is this related to the "find duplicate number" problem using array-as-linked-list?
    // - What would happen if fast moved 3 steps at a time instead of 2?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Simple approach: track visited nodes in a HashSet.
    public static boolean hasCycleHashSet(ListNode head) {
        Set<ListNode> visited = new HashSet<>();
        ListNode curr = head;
        while (curr != null) {
            // If we've seen this exact node reference before, we're looping.
            if (!visited.add(curr)) {
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    // Optimized approach: Floyd's slow/fast pointer, O(1) space.
    public static boolean hasCycleFloyd(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;          // move 1 step
            fast = fast.next.next;     // move 2 steps
            if (slow == fast) {
                return true; // fast lapped slow inside the cycle
            }
        }
        return false; // fast hit the end, no cycle
    }

    // Bonus: find the node where the cycle begins (returns null if no cycle).
    public static ListNode findCycleStart(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        boolean cycleFound = false;

        // Phase 1: detect if a cycle exists and find the meeting point.
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                cycleFound = true;
                break;
            }
        }

        if (!cycleFound) {
            return null;
        }

        // Phase 2: move one pointer back to head; advance both one step at a time.
        // They meet exactly at the start of the cycle.
        slow = head;
        while (slow != fast) {
            slow = slow.next;
            fast = fast.next;
        }
        return slow;
    }

    public static void main(String[] args) {
        // Build 1->2->3->4->5 with a cycle back to node with value 3.
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(4);
        ListNode n5 = new ListNode(5);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;
        n5.next = n3; // creates the cycle back to node 3

        System.out.println("Input: 1->2->3->4->5->(back to 3)");
        // Expected: true
        System.out.println("hasCycleHashSet: " + hasCycleHashSet(n1));
        // Expected: true
        System.out.println("hasCycleFloyd: " + hasCycleFloyd(n1));
        // Expected: 3
        ListNode start = findCycleStart(n1);
        System.out.println("Cycle starts at node with value: " + (start != null ? start.val : "none"));

        // No-cycle list: 1->2->3->null
        ListNode m1 = new ListNode(1);
        ListNode m2 = new ListNode(2);
        ListNode m3 = new ListNode(3);
        m1.next = m2;
        m2.next = m3;
        System.out.println("\nInput: 1->2->3->null");
        // Expected: false
        System.out.println("hasCycleFloyd: " + hasCycleFloyd(m1));
        // Expected: none
        ListNode noStart = findCycleStart(m1);
        System.out.println("Cycle starts at node with value: " + (noStart != null ? noStart.val : "none"));

        // Edge case: empty list.
        System.out.println("\nInput: empty list");
        // Expected: false
        System.out.println("hasCycleFloyd: " + hasCycleFloyd(null));
    }
}
