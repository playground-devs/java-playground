package com.playground.java.interview.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: Concurrency / Object Creation &amp; Initialization
 * PRIORITY: P0 - Must Know
 * TOPIC: Building a Singleton that is safe under concurrent, first-time access from multiple threads.
 *
 * ================= PROBLEM =================
 * A Singleton must guarantee exactly ONE instance ever exists, even when multiple threads race
 * to call getInstance() for the very first time (e.g. a shared ConfigurationManager or a
 * ConnectionPoolRegistry that is lazily created the first time any request thread touches it in
 * a web application). If two threads both observe "instance == null" at the same time, both may
 * proceed to construct an object, and one construction gets silently thrown away (or worse, both
 * halves of the app end up holding different "singleton" references).
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate under pressure often writes plain lazy initialization with no synchronization:
 *
 *   if (instance == null) {
 *       instance = new BrokenLazySingleton();
 *   }
 *
 * Why it's broken: this is a classic check-then-act race condition. Consider two threads T1 and
 * T2 calling getInstance() at almost the same time:
 *   1. T1 reads instance == null -> true, is about to construct.
 *   2. Context switch. T2 also reads instance == null -> true (T1 hasn't assigned yet).
 *   3. T1 constructs a new instance and assigns it to the field.
 *   4. T2 also constructs a *different* new instance and assigns it to the field, overwriting T1's.
 * Now two different objects were constructed, and any caller that cached a reference from step 3
 * is now out of sync with the field's current value. If the singleton holds mutable state (a
 * counter, a cache), you now have two separate instances of "the one true state" -- classic lost
 * update / broken invariant bug. This is invisible in single-threaded tests and shows up only
 * under real concurrent load, which is exactly why interviewers ask about it.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Three progressively better fixes are demonstrated below:
 *   1. Double-Checked Locking (DCL) with a `volatile` field:
 *        - Check instance == null WITHOUT a lock (fast path, no contention once initialized).
 *        - If null, acquire a lock and check AGAIN inside the lock (another thread may have
 *          finished constructing it while we were waiting for the lock).
 *        - Only construct if still null.
 *      The field MUST be `volatile`. Without it, `new DoubleCheckedSingleton()` can be observed
 *      by another thread as PARTIALLY constructed: object construction is not one atomic step --
 *      the JIT/CPU may reorder (a) allocate memory, (b) run constructor, (c) assign reference to
 *      the field, into the order (a), (c), (b). A second thread doing the outer null-check could
 *      then see a non-null reference whose constructor hasn't finished running yet, and read
 *      default/garbage field values. `volatile` establishes a happens-before edge on the write to
 *      the field, forbidding that reordering and guaranteeing visibility of the fully-constructed
 *      object to all threads.
 *   2. Enum-based Singleton: the simplest, fully safe alternative. The JVM guarantees that enum
 *      constants are instantiated exactly once, at class-loading time, in a thread-safe manner
 *      per the class-loading spec (JLS). No locks, no volatile, no boilerplate -- and it is also
 *      naturally serialization-safe (no risk of a deserialization exploit creating a second
 *      instance, unlike a hand-rolled singleton that implements Serializable).
 *
 * ================= WHY THIS MECHANISM =================
 * - `synchronized` alone (synchronizing the whole getInstance() method) is correct but pays a
 *   monitor-acquisition cost on EVERY call forever, even decades after the instance was created --
 *   pure waste for a hot-path accessor.
 * - Double-checked locking + volatile keeps the common case (already initialized) lock-free,
 *   paying the synchronization cost only during the narrow initialization window.
 * - Enum singleton needs no explicit locking primitive at all -- it piggybacks on the JVM's own
 *   class-initialization guarantees (JLS 12.4.2 says class/interface initialization is
 *   synchronized and idempotent), which is the least error-prone option and is Joshua Bloch's
 *   recommended approach in Effective Java.
 * - A static holder class (`Holder` idiom) is another zero-lock alternative worth mentioning in
 *   an interview: it relies on the same class-initialization guarantee but keeps a normal class
 *   shape instead of an enum.
 *
 * ================= EDGE CASES =================
 * - Forgetting `volatile` on the DCL field: compiles fine, passes single-threaded tests, fails
 *   intermittently in production under real multi-core reordering -- very hard to reproduce.
 * - Reflection can break a "safe" singleton by calling a private constructor directly; enum
 *   singletons are immune to this (the JVM forbids reflective enum instantiation).
 * - Serialization can create a second instance unless `readResolve()` is implemented for a
 *   class-based singleton; enum singletons are immune to this too.
 * - Cloning: a class-based singleton must override `clone()` to throw, or it can be cloned into a
 *   second instance.
 * - Class loaders: in rare multi-classloader environments (e.g. some app servers), the "same"
 *   singleton class loaded by two classloaders yields two singletons -- worth mentioning as an
 *   advanced edge case.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - Naive unsafe version: O(1) but WRONG under contention.
 * - Fully synchronized accessor: O(1) but pays lock-acquisition overhead on every single call for
 *   the lifetime of the application -- significant under high call-rate hot paths.
 * - Double-checked locking: O(1) amortized; only the first N racing threads pay lock overhead
 *   during the brief construction window, after which every call is a single volatile read with
 *   no lock -- effectively free on modern JVMs (volatile reads are cheap, not free, but far
 *   cheaper than monitor entry/exit).
 * - Enum / static holder: zero explicit synchronization cost; cost is paid once at class-load
 *   time, which the JVM already synchronizes internally.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - Why must the field be `volatile` in double-checked locking? What exactly can be reordered
 *   without it?
 * - Why do we check `instance == null` twice instead of once?
 * - How does the enum singleton protect against reflection and serialization attacks that break
 *   a hand-rolled singleton?
 * - What is the "initialization-on-demand holder" idiom and how does it achieve laziness without
 *   any locking at all?
 * - Is a Singleton itself a code smell / anti-pattern from a testability standpoint? How would
 *   you make this more testable (e.g. dependency injection instead of static access)?
 * - What happens if the singleton's constructor throws an exception under DCL?
 * - How would you make a singleton per-classloader-safe in an OSGi or app-server environment?
 */
public final class ThreadSafeSingleton {

    private ThreadSafeSingleton() {
    }

    // ================= UNSAFE VERSION =================
    /** Broken lazy singleton: check-then-act race, no synchronization at all. */
    static final class BrokenLazySingleton {
        private static BrokenLazySingleton instance;
        // Tracks how many distinct instances were actually constructed, to prove the bug.
        static final AtomicInteger constructionCount = new AtomicInteger(0);

        private BrokenLazySingleton() {
            constructionCount.incrementAndGet();
        }

        static BrokenLazySingleton getInstance() {
            if (instance == null) {
                // Simulate a small window where a context switch could land, widening the race
                // so the bug is reliably reproducible in a demo (real races don't need this).
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                instance = new BrokenLazySingleton();
            }
            return instance;
        }
    }

    // ================= SAFE VERSION: DOUBLE-CHECKED LOCKING =================
    /** Correct lazy singleton using double-checked locking with a volatile field. */
    static final class DoubleCheckedSingleton {
        // volatile is REQUIRED: prevents reordering of (allocate, assign) vs (run constructor),
        // and guarantees visibility of the fully-constructed object across threads.
        private static volatile DoubleCheckedSingleton instance;
        static final AtomicInteger constructionCount = new AtomicInteger(0);

        private DoubleCheckedSingleton() {
            constructionCount.incrementAndGet();
        }

        static DoubleCheckedSingleton getInstance() {
            DoubleCheckedSingleton result = instance;      // 1 volatile read on the fast path
            if (result == null) {
                synchronized (DoubleCheckedSingleton.class) {
                    result = instance;                      // re-check inside the lock
                    if (result == null) {
                        instance = result = new DoubleCheckedSingleton();
                    }
                }
            }
            return result;
        }
    }

    // ================= SAFE VERSION: ENUM SINGLETON =================
    /** Simplest fully-safe singleton: JVM guarantees single, thread-safe instantiation. */
    enum EnumSingleton {
        INSTANCE;

        private int state;

        public void increment() {
            state++;
        }

        public int getState() {
            return state;
        }
    }

    // ================= DEMONSTRATION =================

    /** Hammers BrokenLazySingleton with many threads and reports how many instances were built. */
    private static int demonstrateBrokenSingleton(int threadCount) throws InterruptedException {
        BrokenLazySingleton.constructionCount.set(0);
        // Reset the static field between demo runs via reflection-free trick: not resettable
        // cleanly since it's static; instead we just report the construction count observed.
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.submit(BrokenLazySingleton::getInstance);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        return BrokenLazySingleton.constructionCount.get();
    }

    /** Hammers DoubleCheckedSingleton with many threads and reports how many instances were built. */
    private static int demonstrateSafeSingleton(int threadCount) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.submit(DoubleCheckedSingleton::getInstance);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        return DoubleCheckedSingleton.constructionCount.get();
    }

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 50;

        System.out.println("=== UNSAFE: BrokenLazySingleton ===");
        int brokenConstructions = demonstrateBrokenSingleton(threadCount);
        // Expected: usually MORE than 1 (non-deterministic; race condition is not guaranteed to
        // reproduce every run, but with 50 racing threads and an artificial sleep widening the
        // window, it reliably shows > 1 on virtually every run).
        System.out.println("Instances actually constructed (should be 1, is buggy if > 1): "
                + brokenConstructions);

        System.out.println();
        System.out.println("=== SAFE: DoubleCheckedSingleton ===");
        int safeConstructions = demonstrateSafeSingleton(threadCount);
        // Expected: always exactly 1.
        System.out.println("Instances actually constructed (must always be 1): " + safeConstructions);
        if (safeConstructions != 1) {
            throw new IllegalStateException("Double-checked locking singleton is broken!");
        }

        System.out.println();
        System.out.println("=== SAFE: EnumSingleton ===");
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> EnumSingleton.INSTANCE.increment());
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        // Expected: exactly threadCount, since it's the same single instance every time and each
        // increment is executed once per submitted task (note: increment() itself is NOT atomic;
        // this demo only proves single-instance identity, not thread-safety of increment()).
        System.out.println("Enum singleton identity confirmed: only one INSTANCE exists by construction.");
        System.out.println("Final (non-atomic) state after " + threadCount + " increments: "
                + EnumSingleton.INSTANCE.getState() + " (may be < " + threadCount
                + " because increment() itself is not synchronized -- a separate concern from singleton identity)");
    }
}
