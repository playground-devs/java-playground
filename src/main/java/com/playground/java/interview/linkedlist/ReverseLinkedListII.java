package com.playground.java.interview.linkedlist;

/**
 * PATTERN: Linked List / In-place Reversal
 * PRIORITY: P1
 * PROBLEM STATEMENT: Reverse only the nodes between position left and right (1-indexed, inclusive) of a singly linked list.
 */
public class ReverseLinkedListII {

    // ================= PROBLEM =================
    // Given the head of a singly linked list and two positions left and right (1-indexed),
    // reverse only the nodes from position left to position right, leaving the rest of the
    // list untouched, and do it in one pass.
    // Example: list = 1->2->3->4->5, left = 2, right = 4 -> output: 1->4->3->2->5
    //
    // ================= SIMPLE APPROACH =================
    // Walk the list and collect the values of the nodes from position left to right into a
    // small list. Reverse that collected list. Walk the sublist region again and overwrite
    // each node's val field with the reversed values, one by one.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works but mutates VALUES instead of pointers, which many interviewers consider
    // "cheating" the point of a linked-list reversal question (it would fail if nodes carried
    // extra state, like external references to a specific node object, since the object
    // identities never actually move). It also needs O(right-left) extra space for the
    // collected values and walks the sublist twice.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use a dummy head so we never special-case "left == 1" (reversing from the very head).
    // Step 1: walk (left - 1) steps from dummy to reach "prev" - the node right before the
    //          sublist that needs reversing.
    // Step 2: let "curr" = prev.next - this will end up as the LAST node of the reversed
    //          sublist once we're done (it never moves, everything else gets pulled in front of it).
    // Step 3: repeat (right - left) times: take the node right after curr (call it "next"),
    //          unlink it from curr, and re-insert it immediately after prev. This is called
    //          "head insertion" - each iteration moves one node to the front of the sublist.
    // Step 4: after the loop, the sublist is reversed in place, and prev.next / curr.next
    //          already point correctly to the rest of the list.
    // Return dummy.next as the new head (handles left == 1 automatically).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A dummy head removes the need for special-casing "the sublist starts at the actual head
    // of the list" (left == 1) - without it we'd need an if/else to decide whether "prev" is
    // a real node or a sentinel representing "before the head". The head-insertion technique
    // only needs a few pointer variables (prev, curr, and a temporary "next"), so unlike the
    // simple approach we never need any auxiliary array/list - we just repoint existing next
    // pointers, giving O(1) extra space instead of O(right-left).
    //
    // ================= EDGE CASES =================
    // - left == right: no reversal needed, sublist of length 1 is already "reversed".
    // - left == 1: reversing starts at the actual head, so the returned head changes -
    //   the dummy node handles this without special logic.
    // - right == length of the list: reversing goes all the way to the last node.
    // - left == 1 and right == length: reversing the entire list.
    // - Single-node list with left == right == 1: no-op.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - we walk to position left (up to n steps) then do (right - left)
    // head-insertion moves, each O(1), so overall linear in the list length.
    // Space Complexity: O(1) extra space for the optimized approach - only a few pointer
    // variables are used, no auxiliary array or new nodes are created.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Walk through why "curr" ends up as the tail of the reversed sublist without ever moving itself.
    // - How would you reverse the WHOLE list using this same head-insertion idea?
    // - How would this change for a doubly linked list (need to fix "prev" pointers too)?
    // - Can you do this recursively instead? What's the trade-off in space?
    // - What if left and right could be out of bounds - how would you validate them?
    // - How would you reverse every other group of k nodes in a list (a variant using this same building block)?
    // - Why is the dummy-head pattern especially valuable here compared to a plain reversal?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Simple approach: collect values in the sublist, reverse them, write back into the same nodes.
    public static ListNode reverseBetweenBruteForce(ListNode head, int left, int right) {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        ListNode curr = head;
        int pos = 1;
        // Collect the values within [left, right].
        while (curr != null) {
            if (pos >= left && pos <= right) {
                values.add(curr.val);
            }
            curr = curr.next;
            pos++;
        }
        java.util.Collections.reverse(values);

        // Write the reversed values back into the same node positions.
        curr = head;
        pos = 1;
        int idx = 0;
        while (curr != null) {
            if (pos >= left && pos <= right) {
                curr.val = values.get(idx++);
            }
            curr = curr.next;
            pos++;
        }
        return head;
    }

    // Optimized: one-pass pointer surgery using head insertion, O(1) extra space.
    public static ListNode reverseBetweenOptimized(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0); // sentinel, avoids special-casing left == 1
        dummy.next = head;

        // Step 1: walk to the node just before position "left".
        ListNode prev = dummy;
        for (int i = 0; i < left - 1; i++) {
            prev = prev.next;
        }

        // Step 2: "curr" will become the tail of the reversed sublist (it never moves).
        ListNode curr = prev.next;

        // Step 3: repeatedly move the node right after curr to right after prev.
        for (int i = 0; i < right - left; i++) {
            ListNode moved = curr.next;   // node to relocate to the front of the sublist
            curr.next = moved.next;       // unlink "moved" from its current spot
            moved.next = prev.next;       // point "moved" to the current front of the sublist
            prev.next = moved;            // attach "moved" right after prev - new front
        }

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
        System.out.println("Input: 1->2->3->4->5->null, left=2, right=4");
        // Expected: 1->4->3->2->5->null
        System.out.println("Optimized: " + printList(reverseBetweenOptimized(list1, 2, 4)));

        ListNode list2 = buildList(new int[]{1, 2, 3, 4, 5});
        System.out.println("\nInput: 1->2->3->4->5->null, left=2, right=4");
        // Expected: 1->4->3->2->5->null
        System.out.println("Brute force: " + printList(reverseBetweenBruteForce(list2, 2, 4)));

        ListNode list3 = buildList(new int[]{1, 2, 3});
        System.out.println("\nInput: 1->2->3->null, left=1, right=3 (reverse entire list)");
        // Expected: 3->2->1->null
        System.out.println("Optimized: " + printList(reverseBetweenOptimized(list3, 1, 3)));

        ListNode list4 = buildList(new int[]{5});
        System.out.println("\nInput: 5->null, left=1, right=1 (single node, no-op)");
        // Expected: 5->null
        System.out.println("Optimized: " + printList(reverseBetweenOptimized(list4, 1, 1)));
    }
}
