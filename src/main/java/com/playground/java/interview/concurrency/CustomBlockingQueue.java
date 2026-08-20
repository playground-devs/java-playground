package com.playground.java.interview.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Concurrency / Locking + Explicit Conditions
 * PRIORITY: P1 - High Priority
 * TOPIC: Building a bounded blocking queue from scratch using ReentrantLock and two explicit
 * Condition variables (notFull / notEmpty), the technique real BlockingQueue implementations use
 * internally.
 *
 * ================= PROBLEM =================
 * We want the same bounded-buffer semantics as ArrayBlockingQueue (producers block when full,
 * consumers block when empty) but this time built from primitives one level below
 * java.util.concurrent's ready-made collections -- exactly what you'd be asked to whiteboard in a
 * senior interview to prove you understand what's happening inside ArrayBlockingQueue, and why it
 * scales better than the single-monitor wait/notifyAll version.
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate familiar only with `synchronized`/`wait`/`notifyAll` (see
 * ProducerConsumerWaitNotify.java) has just ONE implicit condition per monitor, so both "not
 * full" and "not empty" waiters sit on the same wait-set. Waking with `notify()` risks waking the
 * wrong kind of thread; `notifyAll()` fixes correctness but wakes every waiter (both producers
 * and consumers) on every single put/take, most of whom immediately re-check and go back to
 * sleep -- wasteful thundering-herd behavior under contention. There is no way, with the
 * intrinsic monitor API alone, to wake ONLY the consumers when an item is added and ONLY the
 * producers when space frees up -- and that precision is exactly what a hand-built queue using
 * `ReentrantLock` + two separate `Condition`s can achieve.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Step by step:
 *   1. Guard the internal queue with a single `ReentrantLock` (instead of an intrinsic monitor).
 *   2. Create TWO `Condition` objects from that same lock: `notFull` and `notEmpty`. A single
 *      Lock can back multiple independent conditions -- this is the key capability an intrinsic
 *      monitor does not offer (it has exactly one implicit condition).
 *   3. `put(item)`: acquire the lock; while the queue is full, `notFull.await()` (releases the
 *      lock while waiting, exactly like `Object.wait()` does for a monitor); once space exists,
 *      enqueue the item and call `notEmpty.signal()` -- waking ONLY a thread waiting for "not
 *      empty" (a consumer), never a producer that's still waiting on "not full".
 *   4. `take()`: acquire the lock; while the queue is empty, `notEmpty.await()`; once an item
 *      exists, dequeue it and call `notFull.signal()` -- waking ONLY a producer.
 *   5. Always `await()` in a `while` loop (spurious wakeups are still possible with Conditions,
 *      exactly as with `wait()`), and always release the lock in a `finally` block.
 * This precisely targeted signaling (`notFull.signal()` only reaches producers, `notEmpty.signal()`
 * only reaches consumers) is the core efficiency win over a single-monitor `notifyAll()` design.
 *
 * ================= WHY THIS MECHANISM =================
 * `ReentrantLock.newCondition()` was chosen specifically because it lets us split the single
 * "wake everyone" wait-set of an intrinsic monitor into two independent, precisely-targeted
 * wait-sets. We don't need `signalAll()` here because each condition's waiters are homogeneous
 * (all waiting for the exact same predicate: "not full" or "not empty" respectively) -- so a
 * single `signal()` wakes exactly one thread that can definitely make progress (module the
 * standard caveat that another thread could race in first, hence the while-loop re-check). This
 * is precisely the mechanism `ArrayBlockingQueue` uses internally in the JDK. We'd reach for this
 * over plain `ArrayBlockingQueue` only when: asked to demonstrate the mechanism in an interview,
 * or building a queue with custom semantics the JDK class doesn't support (e.g. priority-aware
 * blocking, custom rejection policies, batched draining).
 *
 * ================= EDGE CASES =================
 * - `await()` can still wake spuriously -- always loop, never `if`.
 * - `signal()` (not `signalAll()`) wakes only ONE waiter on that specific condition; if you
 *   accidentally call `notFull.signal()` when you meant `notEmpty.signal()`, you can stall the
 *   pipeline (wrong-condition bugs are a real risk with explicit multi-condition designs -- this
 *   is the trade-off for the efficiency gain).
 * - Interruption: `await()` throws `InterruptedException`; must restore interrupt status and
 *   propagate/abort rather than swallow it.
 * - Lock must be released in `finally`, or an exception mid-critical-section leaks the lock
 *   forever (worse than a leaked intrinsic monitor, which is automatically released by the JVM).
 * - Capacity of 0 is a degenerate edge case worth considering (a "rendezvous" queue) -- this
 *   implementation assumes capacity >= 1.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - put()/take() are O(1) amortized (backed by a LinkedList used purely as a FIFO here).
 * - Signaling is O(1) and precisely targeted -- `notEmpty.signal()` wakes at most one consumer,
 *   `notFull.signal()` wakes at most one producer -- strictly fewer wasted wakeups than
 *   `notifyAll()` on a shared monitor under contention with many producer/consumer threads.
 * - Still a single lock guards both ends of the queue (put and take both need the same lock to
 *   touch the shared `Queue`), so under very high contention this design is not as scalable as
 *   `LinkedBlockingQueue`'s two-lock algorithm (separate `putLock`/`takeLock`) -- a good follow-up
 *   discussion point about how to go even further.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - Why do we need two Conditions instead of one, and how does that improve on notifyAll()?
 * - What happens if you call notFull.signal() where you meant notEmpty.signal()?
 * - Why must Condition.await() also be called inside a while loop?
 * - How would you extend this to support a two-lock algorithm like LinkedBlockingQueue (separate
 *   locks for the head and tail) for higher throughput?
 * - What's the relationship between Lock.newCondition() and the lock that created it -- can you
 *   await() on a Condition without holding its associated lock?
 * - How would you add a timed put/take (return false/null instead of blocking forever)?
 * - How does this compare structurally to what ArrayBlockingQueue does internally in the JDK?
 */
public final class CustomBlockingQueue<T> {

    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public CustomBlockingQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
    }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // releases the lock while waiting; re-checked on wakeup
            }
            queue.add(item);
            notEmpty.signal(); // wake exactly one consumer -- never a producer
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            T item = queue.poll();
            notFull.signal(); // wake exactly one producer -- never a consumer
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    // ================= DEMONSTRATION =================

    private static final int POISON_PILL = Integer.MIN_VALUE;

    public static void main(String[] args) throws InterruptedException {
        final int capacity = 5;
        final int itemsToProduce = 999;
        final int producerCount = 3;
        final int consumerCount = 3;

        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(capacity);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);
        AtomicInteger checksum = new AtomicInteger(0);

        int itemsPerProducer = itemsToProduce / producerCount;

        ExecutorService producerPool = Executors.newFixedThreadPool(producerCount);
        ExecutorService consumerPool = Executors.newFixedThreadPool(consumerCount);

        for (int c = 0; c < consumerCount; c++) {
            consumerPool.submit(() -> {
                try {
                    while (true) {
                        int value = queue.take();
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

        for (int p = 0; p < producerCount; p++) {
            producerPool.submit(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    try {
                        queue.put(1);
                        produced.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
        }

        producerPool.shutdown();
        producerPool.awaitTermination(30, TimeUnit.SECONDS);

        for (int c = 0; c < consumerCount; c++) {
            queue.put(POISON_PILL);
        }
        consumerPool.shutdown();
        consumerPool.awaitTermination(30, TimeUnit.SECONDS);

        int totalItems = itemsPerProducer * producerCount;
        System.out.println("Produced: " + produced.get() + " / " + totalItems);
        System.out.println("Consumed: " + consumed.get() + " / " + totalItems);
        System.out.println("Checksum: " + checksum.get() + " (expected " + totalItems + ")");
        // Expected: deterministic and correct every run -- produced == consumed == totalItems,
        // checksum == totalItems, achieved with precisely-targeted signal() calls instead of a
        // single monitor's notifyAll().
        System.out.println("Final queue size (should be 0, all consumed): " + queue.size());
    }
}
