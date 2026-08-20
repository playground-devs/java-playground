package com.playground.java.interview.linkedlist;

/**
 * PATTERN: Linked List / Two Pointers (Fast & Slow)
 * PRIORITY: P1
 * PROBLEM STATEMENT: Determine whether a singly linked list reads the same forwards and backwards.
 */
public class LinkedListPalindromeCheck {

    // ================= PROBLEM =================
    // Given the head of a singly linked list, determine whether it is a palindrome (the
    // sequence of values reads the same forwards and backwards).
    // Example: list = 1->2->2->1 -> output: true
    //          list = 1->2->3     -> output: false
    //
    // ================= SIMPLE APPROACH =================
    // Copy every value in the list into an ArrayList<Integer> by walking the list once.
    // Then use two index pointers, one starting at the front and one at the back of the
    // ArrayList, moving toward each other and comparing values until they meet or cross.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This works and is O(n) time, but it uses O(n) extra space to store a full copy of
    // every value, even though a singly linked list actually allows an O(1)-space technique
    // by physically reversing half of it. Interviewers commonly ask for the O(1) space
    // follow-up specifically for this problem.
    //
    // ================= OPTIMIZED APPROACH =================
    // Step 1: find the middle of the list using the classic slow/fast pointer technique -
    //          slow moves one step at a time, fast moves two steps at a time; when fast
    //          reaches the end, slow is at the middle.
    // Step 2: reverse the second half of the list in place (starting from slow), using the
    //          standard iterative linked-list reversal.
    // Step 3: compare the first half and the reversed second half node-by-node from their
    //          respective heads; if all values match, it's a palindrome.
    // Step 4 (optional but good practice): reverse the second half back to restore the
    //          original list structure, leaving no observable side effect for the caller.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // The fast/slow pointer technique finds the middle in a single pass without needing to
    // know the list's length up front or storing any extra data. Reversing the second half
    // in place (rather than copying it into an array) means comparisons can be done directly
    // between the two list halves using existing nodes and O(1) extra pointer variables,
    // which is why this approach needs no auxiliary array unlike the brute force.
    //
    // ================= EDGE CASES =================
    // - Empty list: trivially a palindrome (no elements to contradict each other).
    // - Single-node list: trivially a palindrome.
    // - Even-length list, e.g. 1->2->2->1: both halves have equal length, straightforward compare.
    // - Odd-length list, e.g. 1->2->3->2->1: the middle node doesn't need to be compared against
    //   anything (fast/slow split naturally excludes it, or includes it harmlessly depending on split point).
    // - All values identical, e.g. 7->7->7->7: trivially a palindrome.
    // - Clearly non-palindrome list, e.g. 1->2->3.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for both approaches - brute force does one pass to copy plus one
    // comparison pass; optimized does one pass to find the middle, one pass to reverse half,
    // and one pass to compare (still linear overall, just three passes over roughly n/2 nodes each).
    // Space Complexity: Brute force O(n) for the ArrayList copy of all values.
    // Optimized O(1) extra space - only a fixed number of pointer variables, since the second
    // half is reversed in place rather than copied.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does reversing the second half (rather than the first) make the comparison straightforward?
    // - How would you restore the list to its original order after checking, and why might that matter to a caller?
    // - How does the fast/slow pointer split differently for even vs odd length lists, and does it matter for correctness?
    // - How would you solve this recursively instead, and what's the space trade-off (call stack)?
    // - How would this change for a doubly linked list (can you avoid reversing anything)?
    // - What if the list is circular - how would you detect that first before attempting this?
    // - How would you generalize "palindrome check" to compare values with a custom equality function instead of primitive equality?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Brute force: copy values into an ArrayList, compare with two index pointers.
    public static boolean isPalindromeBruteForce(ListNode head) {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        for (ListNode curr = head; curr != null; curr = curr.next) {
            values.add(curr.val);
        }

        int left = 0;
        int right = values.size() - 1;
        while (left < right) {
            if (!values.get(left).equals(values.get(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    // Optimized: find middle, reverse second half in place, compare, then restore.
    public static boolean isPalindromeOptimized(ListNode head) {
        if (head == null || head.next == null) {
            return true; // empty or single-node list is trivially a palindrome
        }

        // Step 1: find the middle using slow/fast pointers.
        ListNode slow = head;
        ListNode fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Step 2: reverse the second half, starting right after "slow".
        ListNode secondHalfHead = reverse(slow.next);

        // Step 3: compare first half against reversed second half.
        boolean isPalindrome = true;
        ListNode p1 = head;
        ListNode p2 = secondHalfHead;
        while (p2 != null) {
            if (p1.val != p2.val) {
                isPalindrome = false;
                break;
            }
            p1 = p1.next;
            p2 = p2.next;
        }

        // Step 4: restore the list to its original structure before returning.
        slow.next = reverse(secondHalfHead);

        return isPalindrome;
    }

    private static ListNode reverse(ListNode head) {
        ListNode prev = null;
        while (head != null) {
            ListNode next = head.next;
            head.next = prev;
            prev = head;
            head = next;
        }
        return prev;
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

    public static void main(String[] args) {
        ListNode list1 = buildList(new int[]{1, 2, 2, 1});
        System.out.println("Input: 1->2->2->1->null (even length)");
        // Expected: true
        System.out.println("Optimized: " + isPalindromeOptimized(list1));

        ListNode list2 = buildList(new int[]{1, 2, 3, 2, 1});
        System.out.println("\nInput: 1->2->3->2->1->null (odd length)");
        // Expected: true
        System.out.println("Optimized: " + isPalindromeOptimized(list2));

        ListNode list3 = buildList(new int[]{1, 2, 3});
        System.out.println("\nInput: 1->2->3->null");
        // Expected: false
        System.out.println("Brute force: " + isPalindromeBruteForce(list3));
        System.out.println("Optimized:   " + isPalindromeOptimized(list3));

        ListNode empty = buildList(new int[]{});
        System.out.println("\nInput: empty list");
        // Expected: true
        System.out.println("Optimized: " + isPalindromeOptimized(empty));
    }
}
