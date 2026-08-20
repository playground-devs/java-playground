package com.playground.java.interview.concurrency;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * PATTERN: ThreadLocal (Per-Thread Isolated State)
 * PRIORITY: P1 - High Priority
 * TOPIC: ThreadLocal gives each thread its own independent copy of a variable accessed
 * through a single shared reference, avoiding both shared-state races and per-call allocation
 * for expensive-to-construct or inherently non-thread-safe objects.
 */
public class ThreadLocalDemo {

    // ================= PROBLEM =================
    // Some objects are either (a) expensive to create per call (e.g. SimpleDateFormat), or
    // (b) NOT thread-safe to share (SimpleDateFormat is famously not thread-safe internally),
    // or (c) represent per-request/per-transaction context (a request id, a DB transaction,
    // the current authenticated user) that many methods down the call stack need to read
    // without threading it through every method signature as an explicit parameter.

    private static final int WORKER_THREADS = 5;

    // A single shared (non-thread-safe) SimpleDateFormat - sharing this across threads would
    // corrupt its internal Calendar state under concurrent use. Contrast with the ThreadLocal below.
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    // ================= NAIVE / UNSAFE APPROACH =================
    // Sharing ONE SimpleDateFormat instance across threads - not shown executing (it would
    // intermittently produce garbled/wrong dates or throw NumberFormatException under load),
    // but documented so the fix is clearly motivated:
    static void sharedFormatterIsUnsafe() {
        System.out.println("(anti-pattern illustrated in comments only): a single shared "
                + "'static SimpleDateFormat FORMAT = new SimpleDateFormat(...)' used concurrently "
                + "by many threads corrupts its internal Calendar state, producing wrong or "
                + "garbled formatted dates, or throwing NumberFormatException intermittently.");
    }

    // A naive per-call fix (allocate a new SimpleDateFormat every time) IS thread-safe but
    // wastes allocation if called extremely frequently in a hot path - ThreadLocal is the
    // middle ground: reuse per-thread instead of per-call, share nothing across threads.

    // ================= SAFE / OPTIMIZED APPROACH =================
    // ThreadLocal<SimpleDateFormat>: each thread lazily creates and then reuses ITS OWN
    // formatter instance. No cross-thread sharing, so no synchronization is needed at all.
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN));

    // A second, independent example: a per-thread "transaction id" simulating request-scoped
    // context that different methods in the same thread's call stack can read without it
    // being passed as a parameter everywhere.
    private static final ThreadLocal<String> TRANSACTION_ID = new ThreadLocal<>(); // no default -> null until set

    static void demonstratePerThreadDateFormat() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(WORKER_THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(WORKER_THREADS);

        for (int i = 0; i < WORKER_THREADS; i++) {
            final int threadId = i;
            pool.submit(() -> {
                try {
                    startGate.await();
                    SimpleDateFormat myFormatter = DATE_FORMAT.get(); // each thread gets its OWN instance
                    for (int call = 0; call < 3; call++) {
                        Date d = new Date(0L + threadId * 1_000_000_000L + call * 1000L);
                        String formatted = myFormatter.format(d);
                        System.out.println("Thread-" + threadId + " formatted: " + formatted
                                + " (formatter identity hash=" + System.identityHashCode(myFormatter) + ")");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startGate.countDown();
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    static void demonstratePerThreadTransactionId() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(WORKER_THREADS);
        CountDownLatch doneLatch = new CountDownLatch(WORKER_THREADS);

        for (int i = 0; i < WORKER_THREADS; i++) {
            final int requestId = i;
            pool.submit(() -> {
                try {
                    TRANSACTION_ID.set("txn-" + requestId + "-" + Thread.currentThread().getName());
                    processRequest(); // deep call chain reads TRANSACTION_ID.get() with no parameter passing
                } finally {
                    // CRITICAL in thread-pool environments: threads are REUSED across tasks.
                    // If you don't remove(), the next task run on this pooled thread will see
                    // a STALE transaction id (or the ThreadLocal map entry leaks memory/objects
                    // for the lifetime of the thread/pool, since pooled threads never terminate).
                    TRANSACTION_ID.remove();
                    doneLatch.countDown();
                }
            });
        }
        doneLatch.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void processRequest() {
        validateRequest();
        persistRequest();
    }

    private static void validateRequest() {
        System.out.println(Thread.currentThread().getName() + " validating under transaction id="
                + TRANSACTION_ID.get());
    }

    private static void persistRequest() {
        System.out.println(Thread.currentThread().getName() + " persisting under transaction id="
                + TRANSACTION_ID.get());
    }

    // ================= WHY THIS MECHANISM =================
    // - Internally, each Thread has its own ThreadLocalMap (Thread.threadLocals) keyed by the
    //   ThreadLocal instance itself (using weak references to the ThreadLocal key). Calling
    //   get()/set() on a ThreadLocal actually reads/writes an entry in the CURRENT thread's map,
    //   which is why different threads never see each other's values despite sharing the same
    //   static ThreadLocal<T> reference.
    // - This gives thread confinement without any locking: there's no shared mutable state
    //   between threads at all, so there's nothing to synchronize.

    // ================= EDGE CASES =================
    // - Thread-pool memory leaks: pooled threads live indefinitely, so a ThreadLocalMap entry
    //   set by one task and never remove()'d persists across all future tasks on that thread,
    //   potentially retaining large objects (classloaders, big context objects) forever -
    //   ALWAYS call remove() in a finally block when running inside a managed thread pool.
    // - Stale data leaking between tasks: if TRANSACTION_ID.remove() is skipped, the NEXT task
    //   executed on that reused thread could silently start with a leftover previous value.
    // - InheritableThreadLocal exists for propagating a value from a parent thread to threads
    //   it creates, but it does NOT propagate into ExecutorService-managed pooled threads
    //   automatically (the pooled threads already existed before your task started).
    // - ThreadLocal.withInitial(Supplier) vs subclassing initialValue(): withInitial (Java 8+)
    //   is the more concise modern idiom.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - get()/set() are effectively O(1) (a small per-thread open-addressed hash map lookup).
    // - Avoids repeated allocation of expensive objects (like SimpleDateFormat) per call,
    //   while still avoiding shared-state synchronization overhead - a good middle ground
    //   versus "new SimpleDateFormat() every call" (extra GC pressure) or "one shared static
    //   instance + synchronized" (serializes all threads on formatting).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How does ThreadLocal achieve per-thread isolation internally (Thread.threadLocals,
    //   ThreadLocalMap, weak references to the ThreadLocal key)?
    // - Why is failing to call remove() in a thread-pool environment a memory leak, specifically?
    // - What's the difference between ThreadLocal and InheritableThreadLocal, and why doesn't
    //   the latter help with ExecutorService-managed threads?
    // - Why is SimpleDateFormat not thread-safe, and what are the alternatives in modern Java
    //   (java.time.format.DateTimeFormatter, which IS thread-safe/immutable)?
    // - Give a real production example of ThreadLocal use (e.g. MDC in logging frameworks like
    //   SLF4J/Logback, Spring's RequestContextHolder, transaction context propagation).
    // - What happens to a ThreadLocal's value's memory if the ThreadLocal reference itself
    //   becomes unreachable but the thread is still alive?
    // - How would you propagate a ThreadLocal value into a task submitted to an ExecutorService
    //   (since child tasks don't automatically inherit it)?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) Naive: sharing one SimpleDateFormat across threads (illustration only) ===");
        sharedFormatterIsUnsafe();

        System.out.println();
        System.out.println("=== 2) Safe: ThreadLocal<SimpleDateFormat> - one formatter per thread ===");
        // Expected: each thread prints correctly formatted dates; the "formatter identity hash"
        // is DIFFERENT across threads (each thread lazily created its own instance) but STABLE
        // (same value) across the 3 calls made by the SAME thread.
        demonstratePerThreadDateFormat();

        System.out.println();
        System.out.println("=== 3) Safe: ThreadLocal transaction id propagated implicitly down the call stack ===");
        // Expected: each thread's validateRequest()/persistRequest() print the SAME txn id that
        // was set for that thread, and different threads never see each other's txn id, even
        // though TRANSACTION_ID is one shared static field. remove() prevents leaks/stale reuse.
        demonstratePerThreadTransactionId();
    }
}
