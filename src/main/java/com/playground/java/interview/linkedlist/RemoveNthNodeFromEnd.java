package com.playground.java.interview.linkedlist;

/**
 * PATTERN: Linked List / Two Pointers
 * PRIORITY: P1
 * PROBLEM STATEMENT: Remove the n-th node from the end of a singly linked list and return the new head.
 */
public class RemoveNthNodeFromEnd {

    // ================= PROBLEM =================
    // Given the head of a linked list, remove the n-th node counting from the END of the
    // list, and return the head of the resulting list.
    // Example: list = 1->2->3->4->5, n = 2 -> remove the 2nd node from the end (which is 4)
    // -> output: 1->2->3->5
    //
    // ================= SIMPLE APPROACH =================
    // Two passes: first walk the whole list once to count its length L. Then compute the
    // position from the front that needs removing (L - n), walk to the node just before it
    // using a dummy head, and unlink the target node.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // It's not wrong, and it's already O(1) space - but it requires walking the list TWICE.
    // In an interview, once you've counted the length you already "know" enough to do it in
    // a single combined pass instead, and interviewers usually want to see the two-pointer
    // "gap" trick as the more elegant one-pass solution.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a dummy head (so removing the actual head node needs no special case) and two
    // pointers, "fast" and "slow", both starting at dummy.
    // Step 1: advance "fast" n steps ahead first, so there is a gap of exactly n nodes
    //          between fast and slow.
    // Step 2: move fast and slow forward together, one step at a time, until fast reaches
    //          the last node (fast.next == null). Because the gap stays fixed at n, slow is
    //          now sitting exactly at the node just BEFORE the one that needs removing.
    // Step 3: unlink: slow.next = slow.next.next.
    // Return dummy.next as the new head.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // The two-pointer "n-gap" technique works because maintaining a constant gap of n nodes
    // between fast and slow means that when fast falls off the end, slow must be exactly n
    // nodes behind it - which is precisely the node before the one we need to remove. No
    // separate counting pass or auxiliary array is needed; a dummy head is used only so that
    // removing the very first node (n == length) doesn't require special-casing "the head is
    // changing" - we always return dummy.next.
    //
    // ================= EDGE CASES =================
    // - n equals the length of the list: the head itself must be removed - dummy handles this.
    // - n == 1: remove the last (tail) node.
    // - Single-node list with n == 1: result is an empty list (null).
    // - n greater than the list length: invalid per typical problem constraints (assumed
    //   valid input here), but worth mentioning you'd validate/throw in production code.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) where n is the list length for the optimized one-pass approach -
    // each pointer traverses the list at most once. Brute force is O(n) as well but requires
    // two full traversals (2n operations, still linear but with a larger constant).
    // Space Complexity: O(1) extra space for both approaches - only a fixed number of pointer
    // variables are used regardless of list length.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why do we advance "fast" by n steps BEFORE moving both pointers together?
    // - How would you modify this to remove the n-th node from the FRONT instead?
    // - What if the list length isn't known and n could be invalid - how do you validate safely?
    // - How would you solve this for a doubly linked list, and would it be any simpler?
    // - Can you find the middle node of a list using a similar two-pointer idea?
    // - Why is the dummy head essential specifically for the case where the head node itself must be removed?
    // - How would this generalize to removing a node at an arbitrary fixed offset from the end, streamed one node at a time (i.e. you can't re-scan)?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Brute force: two-pass - count length, then remove the target node.
    public static ListNode removeNthFromEndBruteForce(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // Pass 1: count the total length of the list.
        int length = 0;
        for (ListNode curr = head; curr != null; curr = curr.next) {
            length++;
        }

        // Pass 2: walk to the node just before the one to remove.
        ListNode prev = dummy;
        for (int i = 0; i < length - n; i++) {
            prev = prev.next;
        }
        prev.next = prev.next.next; // unlink the target node

        return dummy.next;
    }

    // Optimized: one-pass two-pointer technique with a fixed n-node gap.
    public static ListNode removeNthFromEndOptimized(ListNode head, int n) {
        ListNode dummy = new ListNode(0); // sentinel, avoids special-casing removal of the head
        dummy.next = head;

        ListNode fast = dummy;
        ListNode slow = dummy;

        // Step 1: move fast n steps ahead to create the gap.
        for (int i = 0; i < n; i++) {
            fast = fast.next;
        }

        // Step 2: move both pointers together until fast reaches the last node.
        while (fast.next != null) {
            fast = fast.next;
            slow = slow.next;
        }

        // Step 3: slow is now right before the target node - unlink it.
        slow.next = slow.next.next;

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
        ListNode list1 = buildList(new int[]{1, 2, 3, 4, 5});
        System.out.println("Input: 1->2->3->4->5->null, n=2");
        // Expected: 1->2->3->5->null
        System.out.println("Optimized: " + printList(removeNthFromEndOptimized(list1, 2)));

        ListNode list2 = buildList(new int[]{1, 2, 3, 4, 5});
        System.out.println("\nInput: 1->2->3->4->5->null, n=2");
        // Expected: 1->2->3->5->null
        System.out.println("Brute force: " + printList(removeNthFromEndBruteForce(list2, 2)));

        ListNode list3 = buildList(new int[]{1});
        System.out.println("\nInput: 1->null, n=1 (single node, remove the only node)");
        // Expected: null
        System.out.println("Optimized: " + printList(removeNthFromEndOptimized(list3, 1)));

        ListNode list4 = buildList(new int[]{1, 2, 3});
        System.out.println("\nInput: 1->2->3->null, n=3 (n equals length, remove the head)");
        // Expected: 2->3->null
        System.out.println("Optimized: " + printList(removeNthFromEndOptimized(list4, 3)));
    }
}
