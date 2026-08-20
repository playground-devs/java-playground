package com.playground.java.interview.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * PATTERN: Stack / Queue (Design, Simulating One With The Other)
 * PRIORITY: P1
 * ONE-LINE PROBLEM STATEMENT: Implement a FIFO queue using only stacks, and a LIFO stack using only queues.
 */
public class ImplementQueueUsingStacksAndStackUsingQueues {

    // ================= PROBLEM =================
    // Part A (MyQueue): implement a FIFO queue (push, pop, peek, empty) using only two
    // stack instances - no direct queue/list access.
    // Example: push(1), push(2), push(3) -> pop() = 1, pop() = 2, pop() = 3 (FIFO order)
    //
    // Part B (MyStack): implement a LIFO stack (push, pop, top, empty) using only queue
    // instances - no direct stack/list access.
    // Example: push(1), push(2), push(3) -> pop() = 3, pop() = 2, pop() = 1 (LIFO order)
    //
    // ================= SIMPLE APPROACH =================
    // MyQueue (naive): after every push, immediately reverse the order so the oldest
    // element is always accessible - e.g. move everything to a second stack and back on
    // every operation. Works, but redoes the reversal far more often than necessary.
    // MyStack (naive): after every push, move all OTHER queue elements around it instead
    // of rotating - similar redundant work idea.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Naively re-reversing or re-shuffling on every single push/pop wastes work, because
    // most of the existing order is already correct and doesn't need to be touched again.
    // We want a way to only pay the "reordering cost" when it's actually necessary.
    //
    // ================= OPTIMIZED APPROACH =================
    // MyQueue via two stacks (inputStack, outputStack):
    //   push(x): always push x onto inputStack. O(1).
    //   pop()/peek(): if outputStack is empty, transfer ALL elements from inputStack to
    //     outputStack (popping each from inputStack, pushing onto outputStack) - this
    //     reverses their order, turning "most recent on top" into "oldest on top". Then
    //     pop()/peek() from outputStack.
    //   This transfer looks like O(n) on the operation that triggers it, but each element
    //   is moved from inputStack to outputStack AT MOST ONCE in its lifetime (it entered
    //   inputStack once, gets transferred once) - so across a whole sequence of operations,
    //   the total transfer work is bounded by the number of pushes, giving amortized O(1)
    //   per operation.
    //
    // MyStack via one queue with rotation:
    //   push(x): enqueue x at the back, then rotate the queue by dequeuing and immediately
    //     re-enqueuing (size - 1) times. This walks every OLDER element around to the back,
    //     one by one, leaving the just-pushed x sitting at the front.
    //   pop()/top(): just dequeue/peek from the front, since the most-recently-pushed
    //     element is always kept at the front by the rotation done during push.
    //   (Alternative: use two queues, always enqueue new elements into an empty queue,
    //    then drain the other queue into it so the new element ends up first, then swap
    //    queue references. This is functionally the same rotation idea using two queues instead of in-place rotation.)
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // MyQueue: reversing an order once (LIFO push order) then reversing it AGAIN during
    // transfer produces the original FIFO order back - two reversals cancel out. Splitting
    // the work into inputStack (absorbs pushes cheaply) and outputStack (serves pops/peeks
    // cheaply, until it runs dry) means we only pay for a reversal when we've actually run
    // out of already-reversed elements to serve.
    // MyStack: a queue's natural order is oldest-first; rotating after every push physically
    // walks the newest element to the front (where FIFO naturally reads from), effectively
    // borrowing the queue's own dequeue/enqueue operations to fake LIFO ordering on top of
    // FIFO storage.
    //
    // ================= EDGE CASES =================
    // - pop()/peek()/top() on an empty structure: must guard (here, throws NoSuchElementException-style via Deque/Queue poll/peek returning null, so we check first).
    // - Single element pushed then immediately popped.
    // - Many pushes with zero pops in between (MyQueue: outputStack stays empty the whole
    //   time; transfer only happens once, on the very first pop/peek, then amortizes).
    // - Alternating push/pop patterns (MyQueue: may trigger a transfer more than once, but
    //   each element still only ever moves stacks once total).
    // - Popping all the way down to empty, then pushing again (structure must behave like fresh).
    //
    // ================= COMPLEXITY =================
    // Time Complexity: MyQueue - push O(1), pop/peek amortized O(1) (worst case single
    // call O(n) during a transfer, but each element is transferred at most once overall).
    // MyStack - push O(n) (rotation touches every existing element), pop/top O(1).
    // Space Complexity: O(n) for both - all elements must be held somewhere across the two
    // underlying stacks or the single queue.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is MyQueue's pop "amortized" O(1) rather than worst-case O(1) for every single call?
    // - Could you implement MyStack with push O(1) and pop O(n) instead? What would that look like? (Rotate on pop instead of on push.)
    // - How would you implement MyQueue using only ONE stack plus recursion (using the call stack as the second stack)?
    // - What's the trade-off between the one-queue-rotation approach and a two-queue approach for MyStack?
    // - How would you make either of these thread-safe for concurrent producers/consumers?
    // - Why can't you achieve true O(1) worst-case for every single operation in either direction using only two of the opposite structure?
    // - How does this pattern relate to how some real-world systems implement a queue on top of two disks/log segments (write-ahead + compaction)?

    // Part A: FIFO queue built from two LIFO stacks.
    public static class MyQueue {
        private final Deque<Integer> inputStack = new ArrayDeque<>();
        private final Deque<Integer> outputStack = new ArrayDeque<>();

        public void push(int x) {
            inputStack.push(x); // always absorb new pushes into inputStack
        }

        public int pop() {
            transferIfNeeded();
            return outputStack.pop();
        }

        public int peek() {
            transferIfNeeded();
            return outputStack.peek();
        }

        public boolean empty() {
            return inputStack.isEmpty() && outputStack.isEmpty();
        }

        // Only transfer when outputStack has run dry - this is what makes it amortized O(1).
        private void transferIfNeeded() {
            if (outputStack.isEmpty()) {
                while (!inputStack.isEmpty()) {
                    outputStack.push(inputStack.pop()); // reverses LIFO order back into FIFO order
                }
            }
        }
    }

    // Part B: LIFO stack built from a single FIFO queue, using rotation on push.
    public static class MyStack {
        private final Queue<Integer> queue = new LinkedList<>();

        public void push(int x) {
            queue.offer(x); // enqueue at the back
            // Rotate: move every element that was already there around to the back,
            // one at a time, so the just-pushed x ends up at the front.
            int rotations = queue.size() - 1;
            for (int i = 0; i < rotations; i++) {
                queue.offer(queue.poll());
            }
        }

        public int pop() {
            return queue.poll(); // front is always the most recently pushed element
        }

        public int top() {
            return queue.peek();
        }

        public boolean empty() {
            return queue.isEmpty();
        }
    }

    public static void main(String[] args) {
        System.out.println("MyQueue (FIFO via two stacks):");
        MyQueue myQueue = new MyQueue();
        myQueue.push(1);
        myQueue.push(2);
        myQueue.push(3);
        // Expected: 1, 2, 3 (FIFO order)
        System.out.println("push(1,2,3) -> pop()=" + myQueue.pop() + ", pop()=" + myQueue.pop() + ", pop()=" + myQueue.pop() + " (expected 1,2,3)");
        System.out.println("empty() after draining = " + myQueue.empty() + " (expected true)");

        System.out.println("\nEdge case: push after drain, then peek");
        myQueue.push(9);
        // Expected: 9
        System.out.println("push(9) -> peek() = " + myQueue.peek() + " (expected 9)");

        System.out.println("\nMyStack (LIFO via one queue with rotation):");
        MyStack myStack = new MyStack();
        myStack.push(1);
        myStack.push(2);
        myStack.push(3);
        // Expected: 3, 2, 1 (LIFO order)
        System.out.println("push(1,2,3) -> pop()=" + myStack.pop() + ", pop()=" + myStack.pop() + ", pop()=" + myStack.pop() + " (expected 3,2,1)");
        System.out.println("empty() after draining = " + myStack.empty() + " (expected true)");

        System.out.println("\nEdge case: single push then top (does not remove)");
        myStack.push(42);
        // Expected: 42
        System.out.println("push(42) -> top() = " + myStack.top() + " (expected 42)");
    }
}
