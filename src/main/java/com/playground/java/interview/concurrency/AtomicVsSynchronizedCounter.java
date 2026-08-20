package com.playground.java.interview.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PATTERN: Concurrency / Lock-Free Primitives
 * PRIORITY: P0 - Must Know
 * TOPIC: Comparing AtomicLong (lock-free, CAS-based) against a synchronized counter, and knowing
 * exactly when lock-free atomics are enough versus when a lock is still required.
 *
 * ================= PROBLEM =================
 * A high-throughput API gateway needs to count requests-per-second across many worker threads
 * (a simple shared counter incremented on every request). This is the textbook simplest possible
 * shared-mutable-state problem, and the right tool depends entirely on whether the operation is
 * a single atomic step or a multi-step compound operation.
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate writes a plain `long counter` field and increments it with `counter++`:
 *
 *   counter++;
 *
 * Why it's broken: `counter++` is NOT a single CPU instruction at the Java level -- it's read,
 * add 1, write back. Two threads incrementing concurrently can interleave as:
 *   1. T1 reads counter = 100.
 *   2. T2 reads counter = 100 (T1 hasn't written back yet).
 *   3. T1 computes 101, writes counter = 101.
 *   4. T2 computes 101 (from its stale read), writes counter = 101.
 *   Result: counter is 101 after two increments instead of the correct 102 -- one increment is
 *   silently lost. Under many threads doing many increments, this compounds into a final count
 *   noticeably lower than the true number of increments performed.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Two correct fixes, with very different mechanics:
 *   1. `synchronized` counter: wrap the read-modify-write in a synchronized method. Correct, but
 *      pays full mutual-exclusion cost (lock acquisition, potential thread parking/context switch
 *      under contention) on every single increment.
 *   2. `AtomicLong` (java.util.concurrent.atomic): `counter.incrementAndGet()` performs the
 *      read-modify-write as a single hardware-supported Compare-And-Swap (CAS) loop: read the
 *      current value, compute the new value, attempt to atomically swap it in ONLY if the value
 *      hasn't changed since the read; if another thread beat it to the update, retry the whole
 *      read-compute-swap cycle. No thread ever blocks another -- there's no lock at all, just a
 *      tight retry loop backed by a CPU-level atomic instruction (e.g. `cmpxchg` on x86). This
 *      makes AtomicLong "lock-free": progress is guaranteed system-wide (some thread always
 *      succeeds), even though an individual thread could in theory retry multiple times under
 *      heavy contention.
 * Both give the mathematically correct final count. The difference is entirely about mechanism
 * and performance under contention, not correctness.
 *
 * ================= WHY THIS MECHANISM =================
 * - `AtomicLong` is the right choice here because incrementing a counter is a SINGLE logical
 *   operation on a SINGLE variable -- exactly what CAS-based atomics are built for. Under low to
 *   moderate contention, CAS retries are rare and the operation is essentially as cheap as a
 *   plain memory write plus a hardware fence -- no thread ever blocks, no context switch, no
 *   OS scheduler involvement.
 * - `synchronized`/`ReentrantLock` is still REQUIRED when an operation spans MULTIPLE variables
 *   or MULTIPLE steps that must be observed atomically together -- for example, "if balance >=
 *   amount then debit balance and increment a separate withdrawalCount" cannot be expressed as a
 *   single CAS on one variable; a lock (or a hand-built CAS-retry loop coordinating BOTH fields,
 *   which is effectively re-deriving a lock) is required so no other thread can observe or act on
 *   an inconsistent intermediate state.
 * - Under VERY high contention with many threads hammering the same AtomicLong, CAS retry storms
 *   can actually make atomics perform worse than a lock (a lock queues waiters efficiently via
 *   the OS, whereas failed CAS attempts burn CPU cycles retrying) -- this is a real, testable
 *   trade-off, not just theory, and is why `LongAdder` (which stripes counters across cells to
 *   reduce contention) often outperforms a single `AtomicLong` for very hot counters with many
 *   threads.
 *
 * ================= EDGE CASES =================
 * - Compound/multi-field invariants (e.g. "min and max must both update together") are NOT safe
 *   with independent Atomics on each field -- a lock or a single combined atomic reference
 *   (AtomicReference to an immutable pair) is needed instead.
 * - ABA problem: a CAS can succeed even if the value changed and changed back between read and
 *   swap, which is invisible to a simple value-based CAS -- rarely an issue for a monotonically
 *   incrementing counter, but a classic follow-up for atomics on pointers/references.
 * - Livelock in theory: under pathological scheduling, a thread could retry CAS repeatedly and
 *   never win, though the JMM/hardware make this vanishly rare in practice compared to lock-based
 *   starvation.
 * - `synchronized`: no interruption support and no timeout -- a thread blocked waiting for the
 *   monitor cannot be told to give up (unlike `ReentrantLock.tryLock(timeout)`).
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - Both approaches are O(1) per increment in the uncontended case.
 * - Under low/moderate contention: AtomicLong is typically faster (no OS-level blocking, no
 *   context switches, cheap hardware CAS).
 * - Under very high contention (many threads, same hot cell): AtomicLong throughput can degrade
 *   due to repeated CAS failures/retries, sometimes converging with or falling below a
 *   well-tuned lock; `LongAdder` is the JDK's answer for this specific scenario (amortizes
 *   contention across multiple internal cells, combined only when the total is read).
 * - `synchronized` throughput is bounded by lock hand-off/context-switch overhead but is more
 *   predictable and doesn't suffer CAS retry storms.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - Why is `counter++` not atomic even though it looks like one operation?
 * - How does Compare-And-Swap (CAS) work at the hardware level, and what does "lock-free" really
 *   mean (no blocking, not "no retries")?
 * - When would AtomicLong actually perform WORSE than a synchronized counter?
 * - What is LongAdder and how does it improve on AtomicLong under high contention?
 * - Give an example of an operation that CANNOT be made safe with a single Atomic field and
 *   genuinely requires a lock.
 * - What is the ABA problem, and does it affect a simple incrementing counter?
 * - What's the difference between AtomicLong.incrementAndGet() and getAndIncrement()?
 * - How would you prove, empirically, that the naive counter loses updates under concurrency?
 */
public final class AtomicVsSynchronizedCounter {

    private AtomicVsSynchronizedCounter() {
    }

    // ================= UNSAFE VERSION =================
    static final class UnsafeCounter {
        private long count;

        void increment() {
            count++; // read-modify-write, not atomic
        }

        long get() {
            return count;
        }
    }

    // ================= SAFE VERSION: synchronized =================
    static final class SynchronizedCounter {
        private long count;

        synchronized void increment() {
            count++;
        }

        synchronized long get() {
            return count;
        }
    }

    // ================= SAFE VERSION: AtomicLong (lock-free CAS) =================
    static final class AtomicCounter {
        private final AtomicLong count = new AtomicLong(0);

        void increment() {
            count.incrementAndGet(); // single CAS-loop operation, no lock
        }

        long get() {
            return count.get();
        }
    }

    // ================= COMPOUND OPERATION THAT STILL NEEDS A LOCK =================
    /**
     * Demonstrates an operation that CANNOT be made safe by simply swapping in independent
     * Atomics: withdraw() must check the balance and update two related fields (balance and
     * withdrawalCount) as a single atomic unit, or a thread could observe / act on a torn
     * intermediate state (e.g. balance already debited but count not yet incremented, or two
     * threads both passing the "sufficient funds" check based on a stale balance).
     */
    static final class BankAccountRequiringLock {
        private long balanceCents;
        private long withdrawalCount;

        BankAccountRequiringLock(long initialCents) {
            this.balanceCents = initialCents;
        }

        synchronized boolean withdraw(long cents) {
            if (balanceCents < cents) {
                return false; // insufficient funds
            }
            balanceCents -= cents;
            withdrawalCount++; // must be consistent with balanceCents -- one lock covers both
            return true;
        }

        synchronized long getBalance() {
            return balanceCents;
        }

        synchronized long getWithdrawalCount() {
            return withdrawalCount;
        }
    }

    // ================= DEMONSTRATION =================

    private static long runUnsafe(int threads, int incrementsPerThread) throws InterruptedException {
        UnsafeCounter counter = new UnsafeCounter();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return counter.get();
    }

    private static long runSynchronized(int threads, int incrementsPerThread) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  synchronized elapsed: " + elapsedMs + " ms");
        return counter.get();
    }

    private static long runAtomic(int threads, int incrementsPerThread) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  AtomicLong elapsed:   " + elapsedMs + " ms");
        return counter.get();
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 16;
        int incrementsPerThread = 200_000;
        long expected = (long) threads * incrementsPerThread;

        System.out.println("Expected correct final count: " + expected);

        long unsafeResult = runUnsafe(threads, incrementsPerThread);
        // Expected: usually LESS than "expected" due to lost updates (non-deterministic; may
        // rarely match by luck, but reliably wrong at this scale on a multi-core machine).
        System.out.println("UNSAFE (counter++) result:     " + unsafeResult
                + (unsafeResult == expected ? "  (matched this run, but NOT guaranteed)" : "  (LOST UPDATES, as expected)"));

        System.out.println("SAFE (synchronized) benchmark:");
        long syncResult = runSynchronized(threads, incrementsPerThread);
        // Expected: always exactly "expected".
        System.out.println("  result: " + syncResult);
        if (syncResult != expected) {
            throw new IllegalStateException("synchronized counter is broken!");
        }

        System.out.println("SAFE (AtomicLong) benchmark:");
        long atomicResult = runAtomic(threads, incrementsPerThread);
        // Expected: always exactly "expected". Typically faster than synchronized under this
        // moderate contention level because increments never block -- though exact timings are
        // JVM/hardware-dependent and not the focus of correctness here.
        System.out.println("  result: " + atomicResult);
        if (atomicResult != expected) {
            throw new IllegalStateException("AtomicLong counter is broken!");
        }

        System.out.println();
        System.out.println("Compound operation demo (requires a lock even though each field");
        System.out.println("individually could be an Atomic -- the invariant spans two fields):");
        BankAccountRequiringLock account = new BankAccountRequiringLock(1000);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) {
            pool.submit(() -> account.withdraw(200));
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        // Expected: exactly 5 successful withdrawals of 200 fit within a starting balance of
        // 1000 (5 * 200 = 1000), leaving balance 0 and withdrawalCount consistent with it --
        // both fields always agree because a single lock protects the compound invariant.
        System.out.println("Final balance: " + account.getBalance()
                + ", withdrawalCount: " + account.getWithdrawalCount()
                + " (balance / 200 should equal successful count actually processed: "
                + ((1000 - account.getBalance()) / 200) + " == " + account.getWithdrawalCount() + ")");
    }
}
