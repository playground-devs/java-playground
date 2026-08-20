package com.playground.java.interview.p0mustknow;

/**
 * PATTERN: Linked List / Merge
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Merge two sorted linked lists into one sorted linked list by splicing their nodes together.
 */
public class MergeTwoSortedLists {

    // ================= PROBLEM =================
    // You are given two linked lists, each already sorted in ascending order.
    // Merge them into a single sorted linked list, reusing the existing nodes
    // (no need to create new nodes, just re-link).
    // Example: list1 = 1->3->5, list2 = 2->4->6 -> output: 1->2->3->4->5->6
    //
    // ================= SIMPLE APPROACH =================
    // Copy all values from both lists into an array or ArrayList, sort the combined
    // list, then build a brand new linked list from the sorted values.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Both lists are already sorted, so re-sorting from scratch (O((n+m) log(n+m)))
    // completely ignores that fact and wastes time. It also uses extra space for the
    // array/list and creates brand new nodes instead of just re-linking existing ones.
    //
    // ================= OPTIMIZED APPROACH =================
    // Since both lists are already sorted, walk them simultaneously with two pointers.
    // At each step, compare the current node of list1 and list2, pick the smaller one,
    // attach it to the result, and advance that list's pointer.
    // Use a dummy head node to avoid special-casing "what is the first node of the result".
    // When one list runs out, attach the remainder of the other list directly (it's
    // already sorted, so no more comparisons are needed).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // A dummy (sentinel) head node removes the need for an if/else to handle "is this the
    // very first node of the merged list". Without it, you'd need special logic to decide
    // which of list1's or list2's head becomes the result's head. With a dummy node, you
    // always attach to "dummy.next chain" and return dummy.next at the end - simpler and
    // less error prone. No extra memory-heavy structure (like arrays) is needed at all;
    // we just re-point existing next pointers, which is O(1) space beyond a couple of pointers.
    //
    // ================= EDGE CASES =================
    // - One or both lists are empty: return whichever one is non-empty, or null if both are empty.
    // - Lists of very different lengths: the leftover tail of the longer list must be
    //   attached directly once the shorter list is exhausted.
    // - Duplicate values across lists, e.g. list1 has 3 and list2 has 3: order between
    //   equal elements should be stable (usually list1's node goes first, but confirm with interviewer).
    // - Both lists have exactly one node each.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n + m) where n and m are the lengths of the two lists -
    // we visit every node from both lists exactly once.
    // Space Complexity: O(1) extra space - we only reuse existing nodes and a few
    // pointer variables; no new nodes or auxiliary collections are created.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you merge k sorted linked lists instead of just two? (Divide and conquer, or a min-heap.)
    // - Can you do this recursively instead of iteratively? What's the space trade-off?
    // - What if the lists were sorted in descending order instead?
    // - What if you needed to remove duplicates while merging?
    // - How would this change if the lists were doubly linked?
    // - What if the input lists could be circular (need cycle-safety)?
    // - Why is the dummy node pattern so common in linked list problems?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Brute force: dump all values, sort, rebuild a new list.
    public static ListNode mergeBruteForce(ListNode list1, ListNode list2) {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        for (ListNode curr = list1; curr != null; curr = curr.next) {
            values.add(curr.val);
        }
        for (ListNode curr = list2; curr != null; curr = curr.next) {
            values.add(curr.val);
        }
        java.util.Collections.sort(values);

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : values) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    // Optimized: iterative merge using a dummy head, re-linking existing nodes.
    public static ListNode mergeOptimized(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(0); // sentinel node, avoids special-casing the first node
        ListNode tail = dummy;

        // Walk both lists while both still have nodes left.
        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                tail.next = list1; // attach the smaller node
                list1 = list1.next;
            } else {
                tail.next = list2;
                list2 = list2.next;
            }
            tail = tail.next;
        }

        // Attach whichever list still has leftover nodes (already sorted).
        tail.next = (list1 != null) ? list1 : list2;

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
        ListNode list1 = buildList(new int[]{1, 3, 5});
        ListNode list2 = buildList(new int[]{2, 4, 6});
        System.out.println("Input: list1=1->3->5->null, list2=2->4->6->null");
        // Expected: 1->2->3->4->5->6->null
        System.out.println("Merged (optimized): " + printList(mergeOptimized(list1, list2)));

        ListNode list3 = buildList(new int[]{1, 3, 5});
        ListNode list4 = buildList(new int[]{2, 4, 6});
        // Expected: 1->2->3->4->5->6->null
        System.out.println("Merged (brute force): " + printList(mergeBruteForce(list3, list4)));

        ListNode empty1 = buildList(new int[]{});
        ListNode nonEmpty = buildList(new int[]{1, 2, 3});
        System.out.println("\nInput: list1=null, list2=1->2->3->null");
        // Expected: 1->2->3->null
        System.out.println("Merged: " + printList(mergeOptimized(empty1, nonEmpty)));

        System.out.println("\nInput: list1=null, list2=null");
        // Expected: null
        System.out.println("Merged: " + printList(mergeOptimized(null, null)));
    }
}
