package com.playground.java.interview.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Deadlock (Lock Ordering Violation)
 * PRIORITY: P0 - Must Know
 * TOPIC: Two threads locking two shared resources in opposite order can deadlock forever;
 * fix by enforcing a consistent global lock-acquisition order (or using tryLock with a timeout).
 */
public class DeadlockDemo {

    // ================= PROBLEM =================
    // Thread-1 locks A then wants B. Thread-2 locks B then wants A.
    // If both threads grab their first lock at roughly the same time, each then blocks
    // forever waiting for the lock the other one holds -> classic circular-wait deadlock.
    // The 4 Coffman conditions are all present here: mutual exclusion, hold-and-wait,
    // no preemption, and circular wait.

    // ================= NAIVE / UNSAFE APPROACH =================
    // Two threads acquire two locks in OPPOSITE order -> real deadlock, reproduced below.
    static void unsafeDemo() throws InterruptedException {
        final Object lockA = new Object();
        final Object lockB = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println(Thread.currentThread().getName() + " acquired lockA, waiting for lockB...");
                sleepQuietly(200); // widen the window so t2 has time to grab lockB
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + " acquired lockB");
                }
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println(Thread.currentThread().getName() + " acquired lockB, waiting for lockA...");
                sleepQuietly(200);
                synchronized (lockA) {
                    System.out.println(Thread.currentThread().getName() + " acquired lockA");
                }
            }
        }, "Thread-2");

        // Mark them daemon: once truly deadlocked, they can NEVER be unblocked (interrupt()
        // does not release a thread stuck on a synchronized/intrinsic lock), so they would
        // otherwise keep the JVM alive forever waiting for these non-daemon threads to finish.
        // Marking them daemon lets the JVM exit normally after main() returns, even though
        // these two specific threads remain permanently stuck.
        t1.setDaemon(true);
        t2.setDaemon(true);

        t1.start();
        t2.start();

        // Timeout-guarded join so main() never hangs forever even though the threads will.
        t1.join(3000);
        t2.join(3000);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("DEADLOCK DETECTED - threads did not complete within timeout");
            // The two threads are permanently blocked on each other's intrinsic lock.
            // interrupt() has NO effect here because Thread.interrupt() does not release a
            // thread blocked entering a synchronized block (only Lock.lockInterruptibly()/
            // tryLock are interruptible) - the threads simply remain deadlocked forever.
            // Since they are daemon threads, the JVM can still exit cleanly at the end of main().
        } else {
            System.out.println("No deadlock this run (timing-dependent) - both threads finished");
        }
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // Fix #1: always acquire locks in the SAME global order (e.g. by identity hash, or by
    // an assigned numeric id). If every thread agrees on an order, circular wait is impossible.
    static void safeDemoConsistentOrder() throws InterruptedException {
        final Object lockA = new Object();
        final Object lockB = new Object();

        Runnable useAThenB = () -> {
            synchronized (lockA) {
                System.out.println(Thread.currentThread().getName() + " acquired lockA");
                sleepQuietly(100);
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + " acquired lockB");
                }
            }
        };
        // Both threads use the SAME order: A then B. No thread ever waits for B while
        // holding A when another thread waits for A while holding B, so no cycle can form.
        Thread t1 = new Thread(useAThenB, "Ordered-Thread-1");
        Thread t2 = new Thread(useAThenB, "Ordered-Thread-2");

        t1.start();
        t2.start();
        t1.join(3000);
        t2.join(3000);
        System.out.println("Consistent-order demo finished cleanly (t1 alive=" + t1.isAlive()
                + ", t2 alive=" + t2.isAlive() + ")");
    }

    // Fix #2: use tryLock with a timeout so a thread that can't get the second lock backs off,
    // releases what it holds, and retries -> breaks "hold and wait" instead of relying on ordering.
    static void safeDemoTryLock() throws InterruptedException {
        final ReentrantLock lockA = new ReentrantLock();
        final ReentrantLock lockB = new ReentrantLock();

        Runnable worker1 = () -> tryLockBothWithBackoff(lockA, lockB, "TryLock-Thread-1");
        Runnable worker2 = () -> tryLockBothWithBackoff(lockB, lockA, "TryLock-Thread-2");

        Thread t1 = new Thread(worker1, "TryLock-Thread-1");
        Thread t2 = new Thread(worker2, "TryLock-Thread-2");
        t1.start();
        t2.start();
        t1.join(5000);
        t2.join(5000);
        System.out.println("TryLock demo finished cleanly (t1 alive=" + t1.isAlive()
                + ", t2 alive=" + t2.isAlive() + ")");
    }

    private static void tryLockBothWithBackoff(Lock first, Lock second, String name) {
        int attempts = 0;
        while (attempts++ < 10) {
            boolean gotFirst = false;
            boolean gotSecond = false;
            try {
                gotFirst = first.tryLock(50, TimeUnit.MILLISECONDS);
                if (gotFirst) {
                    sleepQuietly(20); // simulate work, widen window for contention
                    gotSecond = second.tryLock(50, TimeUnit.MILLISECONDS);
                    if (gotSecond) {
                        System.out.println(name + " acquired BOTH locks on attempt " + attempts);
                        return;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                if (gotSecond) {
                    second.unlock();
                }
                if (gotFirst) {
                    first.unlock();
                }
            }
            System.out.println(name + " could not get both locks on attempt " + attempts + ", backing off");
            sleepQuietly(10 + (int) (Math.random() * 30)); // randomized backoff avoids livelock
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ================= WHY THIS MECHANISM =================
    // - synchronized/ReentrantLock give mutual exclusion but say nothing about ordering;
    //   ordering discipline (or tryLock timeouts) is an application-level responsibility.
    // - Consistent lock ordering is the simplest, zero-overhead fix and is preferred whenever
    //   you control all the call sites that touch the same set of locks.
    // - tryLock-with-timeout is useful when ordering can't be guaranteed (e.g. locks chosen
    //   dynamically at runtime, like "lock whichever two accounts are involved in a transfer").

    // ================= EDGE CASES =================
    // - Locking N objects chosen at runtime (e.g. bank transfer between two arbitrary accounts):
    //   sort them by a stable id (System.identityHashCode or a business key) before locking.
    // - Nested/transitive deadlocks across 3+ locks and 3+ threads (A->B->C->A) - same fix applies.
    // - Deadlock vs livelock: tryLock+backoff avoids deadlock but naive retry-forever-immediately
    //   can livelock (both back off and retry in lockstep) - use randomized backoff.
    // - JVM-level detection: jstack / ThreadMXBean.findDeadlockedThreads() can detect deadlocks
    //   in production; this demo uses a manual timeout since we can't block main() forever.
    // - Interrupting threads stuck in synchronized(...) does NOT unblock them (intrinsic locks
    //   are not interruptible); only Lock.lockInterruptibly()/tryLock are interruptible.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - Lock ordering: O(1) extra cost, just a discipline, no runtime overhead.
    // - tryLock + backoff: adds retry overhead and latency under contention, but keeps the
    //   system live; throughput degrades gracefully instead of hanging entirely.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What are the four Coffman conditions for deadlock, and how does each fix break one of them?
    // - Why doesn't Thread.interrupt() release a thread blocked on a synchronized block?
    // - How would you detect deadlocks in a running production JVM (jstack, ThreadMXBean)?
    // - How do you pick a consistent lock order when the "locks" are business objects with no
    //   natural ordering (e.g. two Account objects)?
    // - What's the difference between deadlock, livelock, and starvation?
    // - Why is tryLock(timeout) sometimes preferred over synchronized even without a deadlock risk?
    // - How does ReentrantLock's fairness setting interact with deadlock/starvation tradeoffs?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) UNSAFE: opposite lock order -> deadlock (timeout-guarded) ===");
        // Expected: both threads print "acquired lock*" for their FIRST lock, then hang;
        // after ~3s we print "DEADLOCK DETECTED - threads did not complete within timeout".
        unsafeDemo();

        System.out.println();
        System.out.println("=== 2) SAFE: consistent global lock order (A then B) ===");
        // Expected: both threads acquire lockA then lockB and finish cleanly, no hang.
        safeDemoConsistentOrder();

        System.out.println();
        System.out.println("=== 3) SAFE: tryLock with timeout + backoff ===");
        // Expected: threads may retry a few times under contention but both eventually
        // acquire both locks and finish; no permanent hang.
        safeDemoTryLock();
    }
}
