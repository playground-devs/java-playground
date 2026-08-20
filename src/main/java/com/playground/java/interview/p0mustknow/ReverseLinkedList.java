package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Linked List
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Given the head of a singly linked list, reverse the list and return the new head.
 */
public class ReverseLinkedList {

    // ================= PROBLEM =================
    // You are given the head of a singly linked list.
    // Reverse the direction of every "next" pointer so the list points the other way,
    // and return the new head (which was the old tail).
    // Example: 1->2->3->4->5->null  ->  output: 5->4->3->2->1->null
    //
    // ================= SIMPLE APPROACH =================
    // Walk the list once and collect all node values into an array or stack.
    // Then build a brand new list by reading the values back in reverse order
    // (or pop them off the stack), creating new nodes as you go.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This uses O(n) extra space to store all values, when the problem can be solved
    // in-place with O(1) extra space by just re-pointing existing nodes.
    // It also does unnecessary work creating brand new nodes instead of reusing them.
    //
    // ================= OPTIMIZED APPROACH =================
    // Iterative: walk through the list once with three pointers - previous, current, and next.
    // At each node, save current.next before overwriting it, then point current.next back
    // to previous. Move previous and current one step forward. When current becomes null,
    // previous is the new head.
    //
    // Recursive: recurse all the way to the last node (which becomes the new head).
    // On the way back up the call stack, make each node's next node point back to it,
    // and set the current node's next to null (important for the original head, and to
    // avoid cycles as you unwind).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No auxiliary data structure is needed at all - a singly linked list already gives us
    // direct access to "next". Reversal is purely a matter of re-wiring existing next
    // pointers in place, which is why O(1) extra space (iterative) is achievable.
    // The recursive version trades that O(1) space for O(n) call-stack space, which is
    // an important trade-off to call out in an interview.
    //
    // ================= EDGE CASES =================
    // - Empty list (head == null): return null immediately.
    // - Single node list: reversing it returns the same single node, next stays null.
    // - Two node list: make sure both directions get updated correctly (classic off-by-one spot).
    // - Very long list: recursive approach can cause a StackOverflowError; iterative is safer.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - each node is visited exactly once.
    // Space Complexity: O(1) for iterative (only a few pointers used);
    // O(n) for recursive due to the call stack depth equal to the list length.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Can you reverse the list recursively without extra space? (No - recursion inherently uses stack space.)
    // - How would you reverse only a sub-list between position m and n?
    // - How would you reverse a linked list in groups of k nodes?
    // - What changes if this were a doubly linked list?
    // - How do you detect and avoid creating a cycle by mistake while reversing?
    // - Can you do this reversal using a stack explicitly instead of pointer manipulation?
    // - What is the tail-call optimization concern with the recursive Java version (Java does not do TCO)?

    // Simple ListNode definition used by this file.
    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Iterative: re-point next pointers one node at a time using prev/curr/next.
    public static ListNode reverseIterative(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode nextNode = curr.next; // save the rest of the list before we overwrite next
            curr.next = prev;              // reverse the pointer
            prev = curr;                   // move prev forward
            curr = nextNode;                // move curr forward
        }
        return prev; // prev is the new head once curr runs off the end
    }

    // Recursive: recurse to the end, then fix pointers while unwinding the call stack.
    public static ListNode reverseRecursive(ListNode head) {
        // Base case: empty list or the last node - it becomes the new head.
        if (head == null || head.next == null) {
            return head;
        }
        ListNode newHead = reverseRecursive(head.next);
        // head.next is the node right after head; make it point back to head.
        head.next.next = head;
        // Break the old forward link to avoid a two-node cycle.
        head.next = null;
        return newHead;
    }

    // Helper: build a linked list from an array of values.
    private static ListNode buildList(int[] values) {
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        for (int v : values) {
            curr.next = new ListNode(v);
            curr = curr.next;
        }
        return dummy.next;
    }

    // Helper: print a linked list as "1->2->3->null".
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
        ListNode list1 = buildList(new int[]{1, 2, 3, 4, 5});
        System.out.println("Input: " + printList(list1));
        // Expected: 5->4->3->2->1->null
        System.out.println("Reversed (iterative): " + printList(reverseIterative(list1)));

        ListNode list2 = buildList(new int[]{1, 2, 3, 4, 5});
        // Expected: 5->4->3->2->1->null
        System.out.println("Reversed (recursive): " + printList(reverseRecursive(list2)));

        ListNode single = buildList(new int[]{42});
        // Expected: 42->null
        System.out.println("Single node reversed: " + printList(reverseIterative(single)));

        ListNode empty = buildList(new int[]{});
        // Expected: null
        System.out.println("Empty list reversed: " + printList(reverseIterative(empty)));
    }
}
