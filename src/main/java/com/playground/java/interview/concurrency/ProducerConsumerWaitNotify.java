package com.playground.java.interview.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: Concurrency / Coordination (wait/notify)
 * PRIORITY: P0 - Must Know
 * TOPIC: Implementing a bounded buffer producer-consumer pipeline by hand using the intrinsic
 * monitor's wait()/notifyAll(), with no help from java.util.concurrent.
 *
 * ================= PROBLEM =================
 * A log-shipping service has producer threads generating log records faster than consumer
 * threads can flush them to disk. We need a fixed-capacity shared buffer where producers block
 * when the buffer is full (backpressure) and consumers block when the buffer is empty, without
 * busy-waiting (spinning and burning CPU) and without losing or duplicating any item.
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate might reach for a plain LinkedList guarded only by `synchronized`, with a spin loop
 * to "wait" for space or data:
 *
 *   synchronized void put(T item) {
 *       while (queue.size() == capacity) {
 *           // busy-spin, re-checking the condition
 *       }
 *       queue.add(item);
 *   }
 *
 * Why it's broken: this HOLDS the monitor while "waiting", which means no consumer can ever
 * acquire the lock to remove an item and make room -- deadlock/livelock, the producer spins
 * forever holding the very lock the consumer needs. Even a version that spins OUTSIDE the lock
 * wastes CPU and adds needless latency. The correct primitive for "block until a condition
 * becomes true, releasing the lock while waiting" is `Object.wait()`, which atomically releases
 * the monitor and parks the thread, to be woken by `notify()/notifyAll()` when the condition
 * might have changed.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Step by step:
 *   1. Guard the shared queue with a single intrinsic lock (the buffer object's monitor) via
 *      synchronized methods.
 *   2. In put(): while the queue is full, call `wait()` -- this atomically releases the monitor
 *      and suspends the thread, letting consumers run.
 *   3. After being woken, RE-CHECK the condition in a `while` loop (not `if`): the thread may
 *      have woken up due to a "spurious wakeup" (the JVM is allowed by spec to wake a waiting
 *      thread with no corresponding notify() at all), or another producer may have raced in and
 *      refilled the space between notify and this thread actually resuming. An `if` would
 *      proceed incorrectly on a false wakeup; `while` re-validates the actual state every time.
 *   4. Add the item, then call `notifyAll()` to wake up any consumers waiting on "queue not
 *      empty" (and, symmetrically, any other producers waiting on "queue not full" who might
 *      have raced to find it full again -- notifyAll wakes everyone so the JVM re-checks who
 *      should proceed).
 *   5. take() is the mirror image: wait while empty, remove, notifyAll().
 * Why `notifyAll()` and not `notify()`: a single `notify()` wakes ONE arbitrary waiting thread,
 * chosen by the JVM -- but on a buffer with BOTH producers and consumers waiting on the SAME
 * monitor (as here, since there's only one implicit condition per object), `notify()` might wake
 * a thread that still can't proceed (e.g. it wakes another producer when only a consumer could
 * actually make progress), leaving genuinely runnable threads asleep indefinitely -- a "lost
 * wakeup" / stuck pipeline. `notifyAll()` wakes every waiter; each re-checks its own while-loop
 * condition and only the ones that can proceed do so, the rest go back to waiting. This is
 * slightly less efficient (extra wakeups) but is the only generally SAFE choice on a
 * multi-condition monitor like this one.
 *
 * ================= WHY THIS MECHANISM =================
 * wait()/notifyAll() is the lowest-level JVM-native coordination primitive, tied directly to an
 * object's intrinsic monitor. It's the right tool to demonstrate deep understanding of what
 * `BlockingQueue` does internally, and it's still occasionally the right real-world tool when you
 * need a bespoke condition on a simple monitor without pulling in `java.util.concurrent.locks`.
 * In production code today, `ArrayBlockingQueue` (see ProducerConsumerBlockingQueue.java) is
 * almost always preferred because it encapsulates exactly this logic correctly and efficiently
 * with separate internal conditions for "not full" and "not empty" -- avoiding the "wake
 * everyone" inefficiency of a single-monitor notifyAll().
 *
 * ================= EDGE CASES =================
 * - Spurious wakeups: MUST use `while`, never `if`, around every `wait()` call (JLS explicitly
 *   permits spurious wakeups).
 * - Lost notification: if `notify()`/`notifyAll()` is called before the other thread starts
 *   waiting, the "signal" is not queued/remembered by the monitor -- this is why we always
 *   re-check the actual shared state in a loop rather than relying on notification counting.
 * - InterruptedException from `wait()`: must be handled -- typically by restoring the interrupt
 *   status (`Thread.currentThread().interrupt()`) and propagating/aborting, never swallowing it
 *   silently.
 * - Using `notify()` instead of `notifyAll()` here would risk waking the wrong kind of thread and
 *   stalling the pipeline indefinitely under certain interleavings.
 * - Deadlock potential: none here since there's only one lock in play, but if this buffer's
 *   methods ever called out to another lock-guarded object while holding this monitor, lock
 *   ordering would need to be considered.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - Every put()/take() call contends for a single monitor; `notifyAll()` wakes ALL waiters every
 *   time, most of whom immediately re-check their condition and go back to sleep -- O(n) wakeups
 *   per signal in the worst case, where n is the number of waiting threads. This is strictly less
 *   scalable than a BlockingQueue implementation using two separate Conditions (see
 *   CustomBlockingQueue.java), which only wakes the relevant waiters.
 * - Throughput degrades as contention (number of producer/consumer threads) increases, due to
 *   both monitor contention and the thundering-herd effect of notifyAll().
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - Why must wait() always be called in a while-loop, never an if-statement?
 * - What is a spurious wakeup and does the JVM guarantee it won't happen?
 * - Why notifyAll() instead of notify() on a buffer shared by both producers and consumers?
 * - What happens if you call wait()/notify() on an object without holding its monitor?
 * - How would you convert this to use ReentrantLock + two Conditions to avoid waking unrelated
 *   threads? (see CustomBlockingQueue.java)
 * - How does ArrayBlockingQueue solve this same problem more efficiently internally?
 * - What's a "lost wakeup" and how does checking the condition in a loop protect against it?
 */
public final class ProducerConsumerWaitNotify {

    private ProducerConsumerWaitNotify() {
    }

    /** Hand-rolled bounded buffer using only intrinsic locking + wait/notifyAll. */
    static final class BoundedBuffer<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;

        BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        synchronized void put(T item) throws InterruptedException {
            while (queue.size() == capacity) {
                wait(); // releases monitor, re-checked in a while loop on wakeup
            }
            queue.add(item);
            notifyAll(); // wake any consumer(s) waiting on "not empty" (and racing producers)
        }

        synchronized T take() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            T item = queue.poll();
            notifyAll(); // wake any producer(s) waiting on "not full"
            return item;
        }

        synchronized int size() {
            return queue.size();
        }
    }

    // ================= DEMONSTRATION =================

    // Sentinel "poison pill" used to tell a consumer to stop; not a valid produced value.
    private static final int POISON_PILL = Integer.MIN_VALUE;

    public static void main(String[] args) throws InterruptedException {
        final int capacity = 5;
        final int itemsToProduce = 999;
        final int producerCount = 3;
        final int consumerCount = 3;

        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(capacity);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        AtomicInteger checksum = new AtomicInteger(0); // sum of all consumed values

        int itemsPerProducer = itemsToProduce / producerCount;
        int totalItems = itemsPerProducer * producerCount;

        Thread[] producers = new Thread[producerCount];
        for (int p = 0; p < producerCount; p++) {
            producers[p] = new Thread(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    try {
                        buffer.put(1); // each item contributes exactly 1 to the checksum
                        produced.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
        }

        // Each consumer stops upon receiving exactly one poison pill -- the standard, race-free
        // way to shut down an unknown number of consumer threads draining a shared queue, instead
        // of racing on a shared "remaining count" (which can under- or over-consume).
        Thread[] consumers = new Thread[consumerCount];
        for (int c = 0; c < consumerCount; c++) {
            consumers[c] = new Thread(() -> {
                try {
                    while (true) {
                        int value = buffer.take();
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
        // All real items are produced; now hand each consumer exactly one poison pill so it
        // terminates cleanly after draining all real work.
        for (int c = 0; c < consumerCount; c++) {
            buffer.put(POISON_PILL);
        }
        for (Thread c : consumers) {
            c.join(5000);
        }

        System.out.println("Produced: " + produced.get() + " / " + totalItems);
        System.out.println("Consumed: " + consumed.get() + " / " + totalItems);
        System.out.println("Checksum: " + checksum.get() + " (expected " + totalItems + ")");
        // Expected: produced == consumed == totalItems, checksum == totalItems, and the buffer
        // size never exceeded `capacity` at any point (by construction of put()/take()) -- this
        // is deterministic and correct every run, unlike the unsafe interleavings described above.
        System.out.println("Final buffer size (should be 0, all consumed): " + buffer.size());
    }
}
