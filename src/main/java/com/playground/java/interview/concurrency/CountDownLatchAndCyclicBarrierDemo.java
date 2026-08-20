package com.playground.java.interview.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * PATTERN: CountDownLatch vs CyclicBarrier
 * PRIORITY: P1 - High Priority
 * TOPIC: CountDownLatch is a one-shot gate for "wait until N events have happened", while
 * CyclicBarrier is a reusable rendezvous point for "wait until N threads all arrive, then
 * release them all together".
 */
public class CountDownLatchAndCyclicBarrierDemo {

    // ================= PROBLEM =================
    // Scenario A (CountDownLatch): a main/orchestrator thread must not proceed until several
    // independent worker services have each finished initializing. This only needs to happen
    // ONCE - after the count hits zero the latch stays open forever (it cannot be reset).
    //
    // Scenario B (CyclicBarrier): a group of worker threads run in synchronized rounds/phases -
    // e.g. a simulation where every worker must finish "phase 1" before ANY worker starts
    // "phase 2". Unlike CountDownLatch, this needs to happen repeatedly (each phase is a new
    // rendezvous), so the barrier must be reusable ("cyclic").

    // ================= NAIVE / UNSAFE APPROACH =================
    // Without a coordination primitive, the naive fix is polling / sleeping arbitrary amounts
    // of time and hoping workers are done - fragile, wastes CPU, and can race under load.
    static void naivePollingIsFragile() {
        // Deliberately not implemented as "safe" code - just documenting the anti-pattern:
        // Thread.sleep(5000); // "workers are probably done by now" -- guesswork, not a guarantee.
        System.out.println("(anti-pattern illustrated in comments only: Thread.sleep()-based guessing "
                + "instead of a real synchronization barrier is unreliable and slow)");
    }

    // ================= SAFE / OPTIMIZED APPROACH =================

    // --- CountDownLatch: "wait until N services report ready, exactly once" ---
    static void countDownLatchServiceStartupDemo() throws InterruptedException {
        final int serviceCount = 4;
        CountDownLatch readyLatch = new CountDownLatch(serviceCount);
        ExecutorService pool = Executors.newFixedThreadPool(serviceCount);

        String[] serviceNames = {"AuthService", "InventoryService", "PaymentService", "NotificationService"};
        for (int i = 0; i < serviceCount; i++) {
            final String name = serviceNames[i];
            final long startupDelayMs = 100L * (i + 1); // simulate varied startup time
            pool.submit(() -> {
                try {
                    System.out.println(name + " starting up...");
                    Thread.sleep(startupDelayMs);
                    System.out.println(name + " READY");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    readyLatch.countDown(); // signal "I'm done" regardless of success/failure
                }
            });
        }

        System.out.println("main() waiting for all " + serviceCount + " services to become ready...");
        boolean allReadyInTime = readyLatch.await(5, TimeUnit.SECONDS); // bounded wait, avoids infinite hang
        System.out.println("All services ready? " + allReadyInTime + " -> main() proceeds to accept traffic");

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // Note: readyLatch is now permanently open (count == 0) and CANNOT be reset/reused.
        // A second await() call would return immediately, even for a totally new "round".
    }

    // --- CyclicBarrier: "wait until all workers reach the checkpoint, then release together, repeatedly" ---
    static void cyclicBarrierPhaseSyncDemo() throws InterruptedException {
        final int workerCount = 4;
        final int phases = 3;

        // The barrier action runs ONCE per barrier trip, on one of the arriving threads,
        // after the last worker arrives but before any worker is released - useful for
        // per-phase aggregation/logging.
        CyclicBarrier barrier = new CyclicBarrier(workerCount,
                () -> System.out.println(">>> All workers reached the barrier - advancing to next phase <<<"));

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        for (int w = 0; w < workerCount; w++) {
            final int workerId = w;
            pool.submit(() -> {
                try {
                    for (int phase = 1; phase <= phases; phase++) {
                        long workMs = 50L * (workerId + 1);
                        Thread.sleep(workMs); // simulate uneven per-worker work in this phase
                        System.out.println("Worker-" + workerId + " finished phase " + phase + ", waiting at barrier");
                        barrier.await(5, TimeUnit.SECONDS); // blocks until ALL workerCount threads arrive
                        // Because CyclicBarrier is reusable, the same barrier instance serves
                        // every phase - it automatically resets once tripped.
                    }
                    System.out.println("Worker-" + workerId + " completed all phases");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (BrokenBarrierException | TimeoutException e) {
                    System.out.println("Worker-" + workerId + " barrier broken/timed out: " + e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    // ================= WHY THIS MECHANISM =================
    // - CountDownLatch: built on AQS (AbstractQueuedSynchronizer) in shared mode; countDown()
    //   decrements a counter, await() blocks until it reaches zero. Once zero, it stays zero
    //   forever - it models a single, one-time event/gate, not a repeating rendezvous.
    // - CyclicBarrier: built on a private ReentrantLock + Condition internally; tracks how many
    //   parties have called await() for the CURRENT generation, releases them all when the
    //   count is reached, then automatically resets ("cycles") for the next generation - this
    //   is the key structural difference from CountDownLatch.

    // ================= EDGE CASES =================
    // - CountDownLatch.countDown() can be called MORE times than the initial count with no
    //   error (count just floors at zero); it can also be called by threads that never await().
    // - CountDownLatch cannot be reset. For repeated coordination you need a NEW latch each round,
    //   or CyclicBarrier / Phaser instead.
    // - CyclicBarrier: if ANY one of the N threads is interrupted, times out, or throws while
    //   waiting, the barrier becomes "broken" and ALL other waiting/future threads get a
    //   BrokenBarrierException - one bad thread poisons the whole round.
    // - CyclicBarrier's optional Runnable barrier action runs on the LAST thread to arrive,
    //   not on a separate thread - keep it fast and non-blocking.
    // - java.util.concurrent.Phaser (Java 7+) generalizes both: dynamically registerable
    //   parties and reusable phases - worth mentioning as the more flexible successor.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - Both primitives are O(1) per await/countDown call (ignoring the wait itself); the
    //   coordination overhead is negligible compared to the work being synchronized.
    // - CyclicBarrier requires ALL parties to be known/fixed up front (constructor parameter);
    //   CountDownLatch's count is also fixed at construction but doesn't require "parties" to
    //   individually call await() - any number of threads can await, decoupled from who counts down.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What's the fundamental structural difference between CountDownLatch and CyclicBarrier
    //   (one-shot vs cyclic/reusable, and "any thread can count down" vs "fixed set of parties")?
    // - What happens if a thread waiting on a CyclicBarrier is interrupted - what happens to
    //   the OTHER waiting threads?
    // - Can you reset a CountDownLatch? How would you implement repeated one-time gating anyway?
    // - What does the CyclicBarrier's barrierAction Runnable run on, and what are the risks of
    //   putting slow/blocking code in it?
    // - How does java.util.concurrent.Phaser improve on CyclicBarrier (dynamic party registration,
    //   multiple phases, tiered phasers)?
    // - Why use latch.await(timeout, unit) instead of the no-arg await() in production code?
    // - How would you use CountDownLatch to implement a simple "start gate" that releases many
    //   threads at exactly the same instant to maximize race-condition reproduction (as used in
    //   RaceConditionDemo)?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) Naive polling anti-pattern (illustration only) ===");
        naivePollingIsFragile();

        System.out.println();
        System.out.println("=== 2) CountDownLatch: wait for N services to start (one-time gate) ===");
        // Expected: prints each service starting/ready in staggered order, then
        // "All services ready? true -> main() proceeds to accept traffic" only after ALL 4 report ready.
        countDownLatchServiceStartupDemo();

        System.out.println();
        System.out.println("=== 3) CyclicBarrier: synchronize workers across repeated phases ===");
        // Expected: for each of 3 phases, ALL 4 workers must finish that phase's simulated work
        // before ANY worker proceeds to the next phase; ">>> All workers reached the barrier <<<"
        // prints exactly once per phase (3 times total), and the barrier is reused automatically.
        cyclicBarrierPhaseSyncDemo();
    }
}
