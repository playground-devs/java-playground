package com.playground.java.interview.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: Race Condition (Lost Update)
 * PRIORITY: P0 - Must Know
 * TOPIC: Unsynchronized read-modify-write on shared mutable state loses updates under
 * concurrency; fix with synchronized, AtomicInteger, or a concurrent collection.
 */
public class RaceConditionDemo {

    // ================= PROBLEM =================
    // "counter++" is NOT atomic - it is read, increment, write (3 separate steps).
    // If two threads interleave those steps, one thread's increment can be silently lost.
    // Same problem for "list.add(x)" on a plain ArrayList: internal array resize / size
    // bookkeeping is not thread-safe, so concurrent adds can corrupt state or throw
    // ArrayIndexOutOfBoundsException / lose elements.

    private static final int THREADS = 8;
    private static final int INCREMENTS_PER_THREAD = 10_000;
    private static final int EXPECTED_TOTAL = THREADS * INCREMENTS_PER_THREAD;

    // ================= NAIVE / UNSAFE APPROACH =================
    // Plain "int" counter incremented from multiple threads with no synchronization.
    static int unsafeCounter = 0;

    static long runUnsafeCounterDemo() throws InterruptedException {
        unsafeCounter = 0;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                awaitQuietly(startGate);
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    unsafeCounter++; // READ-MODIFY-WRITE race: lost updates guaranteed under contention
                }
                doneLatch.countDown();
            });
        }
        startGate.countDown(); // release all threads at once to maximize interleaving
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return unsafeCounter;
    }

    // Plain ArrayList shared and mutated by multiple threads with no synchronization.
    static long runUnsafeListDemo() throws InterruptedException {
        List<Integer> unsafeList = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        AtomicInteger exceptions = new AtomicInteger(0);

        for (int i = 0; i < THREADS; i++) {
            final int threadId = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        unsafeList.add(threadId * INCREMENTS_PER_THREAD + j); // unsynchronized structural mutation
                    }
                } catch (RuntimeException e) {
                    // ArrayIndexOutOfBoundsException / ConcurrentModificationException can surface here
                    exceptions.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        if (exceptions.get() > 0) {
            System.out.println("  (unsafe list run threw " + exceptions.get() + " exception(s) mid-mutation)");
        }
        return unsafeList.size();
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // Fix #1: synchronized method/block around the read-modify-write to make it atomic.
    static long counterSyncGuard = 0;

    static synchronized void incrementSync() {
        counterSyncGuard++;
    }

    static long runSyncCounterDemo() throws InterruptedException {
        counterSyncGuard = 0;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    incrementSync();
                }
                doneLatch.countDown();
            });
        }
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return counterSyncGuard;
    }

    // Fix #2: AtomicInteger - lock-free CAS-based increment, no explicit locking needed.
    static long runAtomicCounterDemo() throws InterruptedException {
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    atomicCounter.incrementAndGet(); // atomic compare-and-swap loop internally
                }
                doneLatch.countDown();
            });
        }
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return atomicCounter.get();
    }

    // Fix #3: concurrent collection (CopyOnWriteArrayList) instead of a plain ArrayList.
    // Good for read-heavy/write-rare lists; for write-heavy lists prefer synchronizedList
    // or a ConcurrentLinkedQueue-style structure instead (see complexity notes).
    static long runConcurrentListDemo() throws InterruptedException {
        List<Integer> safeList = new CopyOnWriteArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            final int threadId = i;
            pool.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    safeList.add(threadId * INCREMENTS_PER_THREAD + j);
                }
                doneLatch.countDown();
            });
        }
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        return safeList.size();
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ================= WHY THIS MECHANISM =================
    // - synchronized: simplest fix, establishes a happens-before edge and mutual exclusion,
    //   but every increment serializes on one monitor -> throughput drops under contention.
    // - AtomicInteger: uses CPU-level CAS (compare-and-swap) via sun.misc.Unsafe/VarHandle,
    //   no thread ever blocks on a monitor; scales much better for simple numeric state.
    // - CopyOnWriteArrayList: every mutation copies the whole backing array, so writers never
    //   block readers and readers see a stable snapshot; great when reads vastly outnumber writes.

    // ================= EDGE CASES =================
    // - "volatile" alone does NOT fix this: volatile guarantees visibility, not atomicity of
    //   compound operations like counter++ (still read-modify-write races).
    // - Double-checked locking without volatile on the field is a related classic bug.
    // - CopyOnWriteArrayList iterators are snapshot-based (never throw ConcurrentModification-
    //   Exception) but may return stale data if the list is mutated during iteration.
    // - High write-volume + CopyOnWriteArrayList = poor performance (O(n) copy per write);
    //   use Collections.synchronizedList or ConcurrentLinkedDeque instead in that case.
    // - Even with a thread-safe collection, compound actions like "if (!list.contains(x)) list.add(x)"
    //   are still a race unless synchronized externally or you use an atomic method like
    //   putIfAbsent on a map-based structure.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - unsafeCounter: O(1) per op but WRONG result (undercounts) under contention.
    // - synchronized counter: O(1) per op, correct, but serializes all threads (monitor contention).
    // - AtomicInteger: O(1) amortized per op (CAS retry loop), correct, typically fastest for
    //   simple counters under moderate-to-high contention (no OS-level blocking).
    // - CopyOnWriteArrayList.add: O(n) per write (full array copy) vs O(1) amortized for ArrayList;
    //   only use it when writes are rare and reads are frequent and iteration-heavy.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is i++ not atomic even on a primitive int? Walk through the bytecode steps.
    // - Why doesn't declaring the counter `volatile` fix this race?
    // - How does AtomicInteger achieve thread safety without locks? What is CAS and the ABA problem?
    // - When would you choose synchronized over AtomicInteger, or vice versa?
    // - What's the time/space tradeoff of CopyOnWriteArrayList vs Collections.synchronizedList?
    // - How would you detect a race condition like this in code review, or reproduce it reliably
    //   in a test (e.g. using a CountDownLatch start gate, or tools like jcstress)?
    // - What is a "check-then-act" race, and how is it different from a simple lost-update race?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) UNSAFE counter (int counter++ across " + THREADS + " threads) ===");
        long unsafeResult = runUnsafeCounterDemo();
        // Expected: unsafeResult is USUALLY LESS than EXPECTED_TOTAL due to lost updates
        // (exact value is non-deterministic and depends on scheduling/timing).
        System.out.println("Expected total = " + EXPECTED_TOTAL + ", Unsafe actual = " + unsafeResult
                + (unsafeResult != EXPECTED_TOTAL ? "  <-- LOST UPDATES (race condition reproduced)" : "  (no loss this run - still unsafe, try again)"));

        System.out.println();
        System.out.println("=== 2) UNSAFE ArrayList.add across " + THREADS + " threads ===");
        long unsafeListSize = runUnsafeListDemo();
        // Expected: unsafeListSize is USUALLY LESS than EXPECTED_TOTAL, or an exception is logged,
        // because ArrayList's internal size/array bookkeeping is not thread-safe.
        System.out.println("Expected size = " + EXPECTED_TOTAL + ", Unsafe list actual size = " + unsafeListSize);

        System.out.println();
        System.out.println("=== 3) SAFE: synchronized increment ===");
        long syncResult = runSyncCounterDemo();
        // Expected: syncResult == EXPECTED_TOTAL, every single time.
        System.out.println("Expected total = " + EXPECTED_TOTAL + ", synchronized actual = " + syncResult);

        System.out.println();
        System.out.println("=== 4) SAFE: AtomicInteger increment ===");
        long atomicResult = runAtomicCounterDemo();
        // Expected: atomicResult == EXPECTED_TOTAL, every single time.
        System.out.println("Expected total = " + EXPECTED_TOTAL + ", AtomicInteger actual = " + atomicResult);

        System.out.println();
        System.out.println("=== 5) SAFE: CopyOnWriteArrayList.add ===");
        long safeListSize = runConcurrentListDemo();
        // Expected: safeListSize == EXPECTED_TOTAL, every single time.
        System.out.println("Expected size = " + EXPECTED_TOTAL + ", CopyOnWriteArrayList actual size = " + safeListSize);
    }
}
