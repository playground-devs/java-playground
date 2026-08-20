package com.playground.java.interview.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: Concurrency / Coordination (java.util.concurrent)
 * PRIORITY: P0 - Must Know
 * TOPIC: Producer-consumer using the standard library's ArrayBlockingQueue instead of hand-rolled
 * wait()/notifyAll(), and understanding why this is the idiomatic modern choice.
 *
 * ================= PROBLEM =================
 * Same real-world scenario as the manual version: a log-shipping pipeline where producer threads
 * generate records faster than consumers can flush them, and we need bounded backpressure so
 * producers block when the buffer is full and consumers block when it's empty -- but this time
 * solved with the tool almost every production codebase actually uses.
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate might reach for a plain `java.util.ArrayDeque` or `LinkedList` shared across
 * threads with no synchronization at all, or worse, guard it with synchronized but implement
 * "blocking" via a manual spin-loop (`while (queue.isEmpty()) { -- do nothing -- }`). Why it's
 * broken: ArrayDeque/LinkedList are NOT thread-safe -- concurrent add()/poll() calls can corrupt
 * internal linked-list pointers or array indices, causing lost elements, duplicated elements, or
 * even infinite loops/ClassCastException-style corruption inside the collection itself. And a
 * busy-spin "blocking" implementation burns 100% CPU on every waiting thread instead of parking
 * them, which is both wasteful and, without a memory barrier, may never even observe another
 * thread's update due to visibility issues (a plain field read in a tight loop can be hoisted out
 * of the loop by the JIT compiler if nothing establishes happens-before).
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Use `java.util.concurrent.ArrayBlockingQueue`, a ready-made, fully thread-safe bounded FIFO
 * queue:
 *   1. Construct it once with a fixed capacity: `new ArrayBlockingQueue<>(capacity)`.
 *   2. Producers call `queue.put(item)` -- blocks (parks the thread, no spinning) if the queue is
 *      full, until space is available.
 *   3. Consumers call `queue.take()` -- blocks if the queue is empty, until an item is available.
 *   4. That's it. All the locking, condition variables, spurious-wakeup handling, and
 *      notify-the-right-waiter logic from the hand-rolled version (see
 *      ProducerConsumerWaitNotify.java) is already implemented correctly and efficiently inside
 *      ArrayBlockingQueue using a ReentrantLock with two separate Conditions (notFull / notEmpty)
 *      internally.
 * Contrast with the wait/notify version: that implementation needed ~15 lines of carefully
 * reasoned locking code per method (while-loop re-check, notifyAll, exception handling) just to
 * get put/take right. Here it's a single method call. This is the single biggest practical
 * argument for using java.util.concurrent over hand-rolled monitors: less code, fewer places to
 * get subtly wrong, and it's been battle-tested by millions of production deployments.
 *
 * ================= WHY THIS MECHANISM =================
 * `ArrayBlockingQueue` (or `LinkedBlockingQueue` for an optionally-unbounded variant) is chosen
 * specifically because:
 *   - It provides exactly the blocking put()/take() semantics we need, pre-built and correct.
 *   - Internally it uses two separate `Condition` objects (not a single shared monitor), so a
 *     `signal()` on "not full" only wakes a producer, and "not empty" only wakes a consumer --
 *     avoiding the thundering-herd `notifyAll()` inefficiency of the hand-rolled version.
 *   - It also offers non-blocking (`offer()`/`poll()`) and timed (`offer(timeout)`/`poll(timeout)`)
 *     variants for when indefinite blocking isn't acceptable (e.g. under backpressure with a
 *     deadline).
 * We would still write a hand-rolled monitor-based version (or the ReentrantLock+Condition
 * version in CustomBlockingQueue.java) only when: implementing a custom queue discipline that
 * BlockingQueue doesn't support out of the box, in an interview to prove understanding of the
 * underlying mechanism, or in extremely latency-sensitive code needing custom lock-free tricks.
 *
 * ================= EDGE CASES =================
 * - `put()`/`take()` both throw `InterruptedException` -- must be handled properly (typically:
 *   restore interrupt status and exit the loop/thread rather than swallowing it).
 * - Choosing a bounded vs unbounded queue matters: an unbounded `LinkedBlockingQueue` given to a
 *   producer that outpaces consumers forever will grow without limit and can OOM the JVM --
 *   always prefer a bounded queue for real backpressure.
 * - Graceful shutdown: a common pattern is a "poison pill" sentinel value put onto the queue once
 *   per consumer after producers finish, so consumers can distinguish "queue temporarily empty"
 *   from "no more work is coming, please terminate."
 * - `ArrayBlockingQueue` supports an optional fairness flag (`new ArrayBlockingQueue<>(cap,
 *   true)`) for FIFO ordering of blocked producers/consumers, at a throughput cost, same
 *   trade-off as ReentrantLock's fairness flag.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - put()/take() are O(1) amortized.
 * - Because ArrayBlockingQueue uses two independent Conditions, a signal only wakes threads that
 *   can actually make progress -- strictly better wakeup behavior than a single-monitor
 *   `notifyAll()` design under contention with many producers and consumers.
 * - Backed by a plain circular array (no per-node allocation like LinkedBlockingQueue), so
 *   ArrayBlockingQueue has lower memory overhead and better cache locality for a fixed, moderate
 *   capacity, but LinkedBlockingQueue can have marginally higher put/take throughput at high
 *   concurrency because its put and take locks are independent (two-lock algorithm) whereas
 *   ArrayBlockingQueue uses a single lock for both ends.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - Why is ArrayBlockingQueue preferred over a hand-rolled wait/notify buffer in production code?
 * - What's the difference between ArrayBlockingQueue and LinkedBlockingQueue in terms of locking
 *   strategy and memory characteristics?
 * - What happens if you use an unbounded queue between a fast producer and a slow consumer?
 * - How would you implement graceful shutdown of a producer-consumer pipeline using
 *   BlockingQueue (poison pill pattern)?
 * - What's the difference between put()/take() and offer()/poll() and when would you use each?
 * - How does ArrayBlockingQueue avoid the "wake the wrong thread" problem that a single-monitor
 *   wait/notifyAll design has?
 * - How would you size the queue capacity in a real system, and what happens under sustained
 *   producer/consumer throughput mismatch?
 */
public final class ProducerConsumerBlockingQueue {

    private ProducerConsumerBlockingQueue() {
    }

    private static final int POISON_PILL = Integer.MIN_VALUE;

    public static void main(String[] args) throws InterruptedException {
        final int capacity = 5;
        final int itemsToProduce = 999;
        final int producerCount = 3;
        final int consumerCount = 3;

        // The entire "hand-rolled buffer" from ProducerConsumerWaitNotify.java collapses to this
        // single line -- all the locking/condition logic is already implemented correctly inside.
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(capacity);

        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        AtomicInteger checksum = new AtomicInteger(0);

        int itemsPerProducer = itemsToProduce / producerCount;

        Thread[] producers = new Thread[producerCount];
        for (int p = 0; p < producerCount; p++) {
            producers[p] = new Thread(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    try {
                        queue.put(1); // blocks if full -- no manual wait/notify needed
                        produced.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
        }

        Thread[] consumers = new Thread[consumerCount];
        for (int c = 0; c < consumerCount; c++) {
            consumers[c] = new Thread(() -> {
                try {
                    while (true) {
                        int value = queue.take(); // blocks if empty
                        if (value == POISON_PILL) {
                            return;
                        }
                        consumed.incrementAndGet();
                        checksum.addAndGet(value);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Thread c : consumers) {
            c.start();
        }
        for (Thread p : producers) {
            p.start();
        }
        for (Thread p : producers) {
            p.join();
        }
        for (int c = 0; c < consumerCount; c++) {
            queue.put(POISON_PILL);
        }
        for (Thread c : consumers) {
            c.join(5000);
        }

        int totalItems = itemsPerProducer * producerCount;
        System.out.println("Produced: " + produced.get() + " / " + totalItems);
        System.out.println("Consumed: " + consumed.get() + " / " + totalItems);
        System.out.println("Checksum: " + checksum.get() + " (expected " + totalItems + ")");
        // Expected: deterministic and correct every run -- produced == consumed == totalItems,
        // checksum == totalItems, exactly like the hand-rolled version, but with far less code
        // and no risk of a subtly-wrong wait/notify implementation.
        System.out.println("Final queue size (should be 0, all consumed): " + queue.size());
    }
}
