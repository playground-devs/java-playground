package com.playground.java.interview.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Concurrency / Locking
 * PRIORITY: P0 - Must Know
 * TOPIC: Implementing the same critical section with `synchronized` and with `ReentrantLock`,
 * and knowing when the extra power of ReentrantLock is worth its extra ceremony.
 *
 * ================= PROBLEM =================
 * A shared bank account balance is updated by many teller threads concurrently (deposit /
 * withdraw). Only one thread may read-modify-write the balance at a time, or we get lost updates.
 * The question isn't just "how do I make this safe" (both mechanisms below do that correctly) --
 * it's "which locking primitive should I reach for, and why."
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * Updating balance without any lock:
 *
 *   balance = balance + amount;
 *
 * Why it's broken: `balance + amount` is really three steps -- read balance, compute new value,
 * write it back. Two threads depositing 100 concurrently can interleave as:
 *   1. T1 reads balance = 500.
 *   2. T2 reads balance = 500 (T1 hasn't written back yet).
 *   3. T1 computes 600, writes balance = 600.
 *   4. T2 computes 600 (from its stale read of 500), writes balance = 600.
 *   Result: balance is 600 instead of the correct 700 -- one deposit was silently lost.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Two equivalent, correct implementations of the same critical section:
 *   1. `synchronized` (intrinsic lock / monitor): wrap the read-modify-write in a synchronized
 *      block or method. The JVM automatically acquires the monitor on entry and releases it on
 *      exit -- including on exceptional exit -- so it can never be "forgotten."
 *   2. `ReentrantLock`: explicitly `lock()` before the critical section and `unlock()` in a
 *      `finally` block so the lock is released even if the critical section throws. This is
 *      functionally equivalent to synchronized for the basic case, but exposes extra
 *      capabilities: `tryLock()` (non-blocking attempt), `tryLock(timeout, unit)` (bounded wait,
 *      avoids waiting forever for a stuck lock holder), `lockInterruptibly()` (a blocked thread
 *      can be interrupted out of waiting instead of being stuck until the lock is free), and a
 *      fairness policy (`new ReentrantLock(true)` grants the lock to the longest-waiting thread,
 *      trading throughput for freedom from starvation).
 *
 * ================= WHY THIS MECHANISM =================
 * - `synchronized`: simplest, least error-prone (can't forget to unlock), integrates with the
 *   JVM's lock-elision / biased-locking optimizations, and is the right default when you don't
 *   need any of ReentrantLock's extra features.
 * - `ReentrantLock`: choose it specifically when you need (a) a timed or non-blocking lock
 *   attempt to avoid indefinite blocking, (b) interruptible lock acquisition for responsive
 *   cancellation, (c) fairness guarantees, or (d) multiple Condition objects on a single lock
 *   (see CustomBlockingQueue.java, which needs two independent conditions -- not possible with a
 *   single intrinsic monitor's single implicit condition).
 * - Trade-off: ReentrantLock requires disciplined try/finally usage -- forgetting `unlock()`
 *   causes a permanent deadlock for every future acquirer, a bug class `synchronized` structurally
 *   cannot have.
 *
 * ================= EDGE CASES =================
 * - Forgetting `unlock()` in a `finally` block with ReentrantLock -> permanent lock leak.
 * - Both mechanisms are reentrant: the same thread can re-acquire a lock it already holds
 *   (e.g. a synchronized method calling another synchronized method on the same object, or
 *   nested `lock()` calls) without deadlocking itself -- but the lock must be released the same
 *   number of times it was acquired.
 * - `tryLock(timeout, unit)` can throw `InterruptedException` -- must be handled/propagated.
 * - Unfair (default) ReentrantLock can starve a particular thread indefinitely under sustained
 *   contention if newer threads keep "barging in"; fair mode fixes this at a throughput cost.
 * - Deadlock potential is identical for both: two locks acquired in inconsistent order by two
 *   threads -> classic deadlock. Lock ordering discipline is required regardless of mechanism.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - Under low contention, both are extremely fast (JVMs optimize uncontended synchronized
 *   heavily via biased/thin locking); the difference is usually negligible.
 * - Under high contention, ReentrantLock's non-fair mode typically has higher throughput than
 *   fair mode or than synchronized, because it allows barging (a thread arriving may steal the
 *   lock before queued waiters are woken), reducing context-switch overhead -- at the cost of
 *   fairness.
 * - Fair ReentrantLock has notably lower throughput than unfair, due to strict FIFO hand-off and
 *   more context switches, but eliminates starvation.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - When would you reach for ReentrantLock instead of synchronized?
 * - What does "reentrant" mean and why does it matter for recursive or nested locking calls?
 * - How does `tryLock(timeout, unit)` help avoid deadlock compared to a plain blocking lock?
 * - What is lock fairness, and what's the throughput cost of using `new ReentrantLock(true)`?
 * - How do you correctly release a ReentrantLock in the presence of exceptions?
 * - What's the difference between `wait()/notify()` (tied to intrinsic locks) and
 *   `Condition.await()/signal()` (tied to explicit Locks)?
 * - Can two threads deadlock using ReentrantLock the same way they can with synchronized? How?
 */
public final class SynchronizedVsReentrantLock {

    private SynchronizedVsReentrantLock() {
    }

    // ================= UNSAFE VERSION =================
    static final class UnsafeAccount {
        private long balanceCents;

        UnsafeAccount(long initialCents) {
            this.balanceCents = initialCents;
        }

        void deposit(long cents) {
            balanceCents = balanceCents + cents; // read-modify-write, no protection
        }

        long getBalance() {
            return balanceCents;
        }
    }

    // ================= SAFE VERSION: synchronized =================
    static final class SynchronizedAccount {
        private long balanceCents;

        SynchronizedAccount(long initialCents) {
            this.balanceCents = initialCents;
        }

        synchronized void deposit(long cents) {
            balanceCents = balanceCents + cents;
        }

        synchronized long getBalance() {
            return balanceCents;
        }
    }

    // ================= SAFE VERSION: ReentrantLock =================
    static final class ReentrantLockAccount {
        private long balanceCents;
        private final Lock lock = new ReentrantLock(); // pass true for fairness

        ReentrantLockAccount(long initialCents) {
            this.balanceCents = initialCents;
        }

        void deposit(long cents) {
            lock.lock();
            try {
                balanceCents = balanceCents + cents;
            } finally {
                lock.unlock(); // MUST be in finally, or an exception here leaks the lock forever
            }
        }

        long getBalance() {
            lock.lock();
            try {
                return balanceCents;
            } finally {
                lock.unlock();
            }
        }

        /** Demonstrates the extra power ReentrantLock has that synchronized does not. */
        boolean tryDepositWithinTimeout(long cents, long timeout, TimeUnit unit) throws InterruptedException {
            if (lock.tryLock(timeout, unit)) {
                try {
                    balanceCents = balanceCents + cents;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false; // gave up rather than blocking forever
        }
    }

    // ================= DEMONSTRATION =================

    private static long runUnsafe(int threads, int depositsPerThread, long depositAmount) throws InterruptedException {
        UnsafeAccount account = new UnsafeAccount(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < depositsPerThread; j++) {
                    account.deposit(depositAmount);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return account.getBalance();
    }

    private static long runSynchronized(int threads, int depositsPerThread, long depositAmount) throws InterruptedException {
        SynchronizedAccount account = new SynchronizedAccount(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < depositsPerThread; j++) {
                    account.deposit(depositAmount);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return account.getBalance();
    }

    private static long runReentrantLock(int threads, int depositsPerThread, long depositAmount) throws InterruptedException {
        ReentrantLockAccount account = new ReentrantLockAccount(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < depositsPerThread; j++) {
                    account.deposit(depositAmount);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return account.getBalance();
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 20;
        int depositsPerThread = 10_000;
        long depositAmount = 1;
        long expected = (long) threads * depositsPerThread * depositAmount;

        System.out.println("Expected correct final balance: " + expected);

        long unsafeResult = runUnsafe(threads, depositsPerThread, depositAmount);
        // Expected: usually LESS than "expected" due to lost updates (non-deterministic; may
        // occasionally equal "expected" by luck on a given run, but reliably wrong at this scale).
        System.out.println("UNSAFE result:            " + unsafeResult
                + (unsafeResult == expected ? "  (matched this run, but NOT guaranteed)" : "  (LOST UPDATES, as expected)"));

        long syncResult = runSynchronized(threads, depositsPerThread, depositAmount);
        // Expected: always exactly "expected".
        System.out.println("synchronized result:     " + syncResult);
        if (syncResult != expected) {
            throw new IllegalStateException("synchronized version is broken!");
        }

        long lockResult = runReentrantLock(threads, depositsPerThread, depositAmount);
        // Expected: always exactly "expected".
        System.out.println("ReentrantLock result:    " + lockResult);
        if (lockResult != expected) {
            throw new IllegalStateException("ReentrantLock version is broken!");
        }

        // Demonstrate tryLock with timeout: a lock held by another thread causes bounded waiting
        // instead of indefinite blocking.
        ReentrantLockAccount timedAccount = new ReentrantLockAccount(0);
        Thread holder = new Thread(() -> {
            timedAccount.lock.lock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                timedAccount.lock.unlock();
            }
        });
        holder.start();
        Thread.sleep(50); // ensure holder grabs the lock first
        boolean acquired = timedAccount.tryDepositWithinTimeout(100, 100, TimeUnit.MILLISECONDS);
        // Expected: false, because the holder thread sleeps 500ms while holding the lock, longer
        // than our 100ms timeout budget.
        System.out.println("tryLock with 100ms timeout while lock is held elsewhere -> acquired = " + acquired);
        holder.join();
    }
}
