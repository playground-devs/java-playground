package com.playground.java.interview.linkedlist;

import java.math.BigInteger;

/**
 * PATTERN: Linked List / Simulation
 * PRIORITY: P1
 * PROBLEM STATEMENT: Add two non-negative integers represented as linked lists with digits stored in reverse order.
 */
public class AddTwoNumbers {

    // ================= PROBLEM =================
    // Two non-negative integers are represented as linked lists, where each node holds a
    // single digit, and the digits are stored in REVERSE order (least significant digit
    // first). Add the two numbers and return the sum in the same reverse-digit format.
    // Example: l1 = 2->4->3 (represents 342), l2 = 5->6->4 (represents 465)
    //          342 + 465 = 807 -> output: 7->0->8
    //
    // ================= SIMPLE APPROACH =================
    // Convert each list into a single number (e.g. a BigInteger, walking the list and
    // building up "digit * 10^position"), add the two numbers together using BigInteger
    // arithmetic, then convert the sum back into a reversed-digit linked list.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Converting to a primitive like long risks overflow for arbitrarily long lists (e.g.
    // a 30-digit number does not fit in a long), so a "safe" brute force needs BigInteger,
    // which brings its own overhead (object allocation, base-10 string/math conversions)
    // and sidesteps the actual point of the exercise - practicing digit-by-digit linked-list
    // traversal with carry propagation. Interviewers generally consider this "cheating" the
    // intended technique, so it's shown here only for comparison.
    //
    // ================= OPTIMIZED APPROACH =================
    // Traverse both lists simultaneously, one digit at a time, carrying over any overflow
    // just like manual addition on paper.
    // Step 1: at each step, take the current digit from l1 (or 0 if l1 is exhausted) and
    //          from l2 (or 0 if l2 is exhausted), plus the carry from the previous step.
    // Step 2: sum = digit1 + digit2 + carry. The new digit to store is sum % 10, and the
    //          new carry going forward is sum / 10 (0 or 1, since digits are 0-9).
    // Step 3: append the new digit as a node using a dummy head to avoid special-casing
    //          the first node of the result.
    // Step 4: continue while EITHER list still has nodes remaining OR the carry is nonzero
    //          (handles a final carry like 5 + 5 = 10 producing an extra leading node).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Digit-by-digit traversal with a running carry mirrors exactly how addition works by
    // hand, and the reverse-digit storage format is actually convenient here: the least
    // significant digit is at the head, so we can process digits in the natural order needed
    // for addition (right-to-left) simply by walking the lists left-to-right - no reversal or
    // recursion needed. A dummy head again removes the need to special-case "what is the
    // first node of the result list".
    //
    // ================= EDGE CASES =================
    // - Different length lists, e.g. l1 has 3 digits and l2 has 5 digits.
    // - A final carry produces an extra digit at the end, e.g. 5 + 5 = 10 -> extra node for the leading 1.
    // - Both numbers are zero: each list is a single node with value 0 -> result is a single 0 node.
    // - One list is much longer than the other (all remaining digits of the longer list just
    //   need + carry applied).
    // - Every digit pair sums to exactly 9 with an incoming carry of 1 (cascading carries), e.g. 999 + 1.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(max(m, n)) where m and n are the lengths of the two lists - we walk
    // through the longer list exactly once, doing O(1) work per digit.
    // Space Complexity: O(max(m, n)) for the result list, since the sum can have at most one
    // more digit than the longer input (not counting the O(1) extra variables used for carry/sum).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is reverse-digit order actually convenient here compared to normal (most-significant-first) order?
    // - How would you solve this if digits were stored in NORMAL (most-significant-first) order instead? (Hint: reverse both lists first, or use recursion/stacks to process from the tail.)
    // - How would you handle negative numbers?
    // - What if the numbers were represented in a different base (e.g. base 16)?
    // - Why does BigInteger avoid overflow where a primitive long would not, and what's the performance trade-off?
    // - How would you add more than two numbers represented this way?
    // - Can you do this recursively? What's the base case and how do you propagate the carry back up?

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Brute force: convert both lists to BigInteger, add, convert back. Not the expected
    // interview approach - shown only for comparison and to discuss overflow risk of long.
    public static ListNode addTwoNumbersBruteForce(ListNode l1, ListNode l2) {
        BigInteger num1 = listToBigInteger(l1);
        BigInteger num2 = listToBigInteger(l2);
        BigInteger sum = num1.add(num2);
        return bigIntegerToReversedList(sum);
    }

    private static BigInteger listToBigInteger(ListNode head) {
        // Digits are stored least-significant-first, so build up the number from the tail forward.
        StringBuilder sb = new StringBuilder();
        for (ListNode curr = head; curr != null; curr = curr.next) {
            sb.append(curr.val);
        }
        return new BigInteger(sb.reverse().toString());
    }

    private static ListNode bigIntegerToReversedList(BigInteger num) {
        String digits = num.toString();
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        // Walk the decimal string from the last character (least significant digit) to the first.
        for (int i = digits.length() - 1; i >= 0; i--) {
            curr.next = new ListNode(digits.charAt(i) - '0');
            curr = curr.next;
        }
        return dummy.next;
    }

    // Optimized: simultaneous digit-by-digit traversal with carry propagation.
    public static ListNode addTwoNumbersOptimized(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0); // sentinel, avoids special-casing the first result node
        ListNode tail = dummy;
        int carry = 0;

        // Keep going while either list still has digits, or a carry is left over.
        while (l1 != null || l2 != null || carry != 0) {
            int digit1 = (l1 != null) ? l1.val : 0;
            int digit2 = (l2 != null) ? l2.val : 0;

            int sum = digit1 + digit2 + carry;
            carry = sum / 10;          // overflow rolls into the next digit
            tail.next = new ListNode(sum % 10);
            tail = tail.next;

            if (l1 != null) l1 = l1.next;
            if (l2 != null) l2 = l2.next;
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
        ListNode l1 = buildList(new int[]{2, 4, 3}); // represents 342
        ListNode l2 = buildList(new int[]{5, 6, 4}); // represents 465
        System.out.println("Input: l1=2->4->3->null (342), l2=5->6->4->null (465)");
        // Expected: 7->0->8->null (807)
        System.out.println("Optimized: " + printList(addTwoNumbersOptimized(l1, l2)));

        ListNode l3 = buildList(new int[]{2, 4, 3});
        ListNode l4 = buildList(new int[]{5, 6, 4});
        System.out.println("\nInput: l1=2->4->3->null (342), l2=5->6->4->null (465)");
        // Expected: 7->0->8->null (807)
        System.out.println("Brute force: " + printList(addTwoNumbersBruteForce(l3, l4)));

        ListNode l5 = buildList(new int[]{5});
        ListNode l6 = buildList(new int[]{5});
        System.out.println("\nInput: l1=5->null, l2=5->null (5 + 5 = 10, carry produces extra digit)");
        // Expected: 0->1->null (represents 10)
        System.out.println("Optimized: " + printList(addTwoNumbersOptimized(l5, l6)));

        ListNode l7 = buildList(new int[]{0});
        ListNode l8 = buildList(new int[]{0});
        System.out.println("\nInput: l1=0->null, l2=0->null (both zero)");
        // Expected: 0->null
        System.out.println("Optimized: " + printList(addTwoNumbersOptimized(l7, l8)));
    }
}
