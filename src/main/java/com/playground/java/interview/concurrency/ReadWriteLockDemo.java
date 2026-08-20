package com.playground.java.interview.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * PATTERN: ReentrantReadWriteLock (Reader-Writer Locking)
 * PRIORITY: P1 - High Priority
 * TOPIC: Plain mutual exclusion serializes concurrent readers unnecessarily; a
 * ReadWriteLock lets many readers proceed in parallel while still giving writers exclusivity.
 */
public class ReadWriteLockDemo {

    // ================= PROBLEM =================
    // A cache/config map is read very frequently (many threads calling get()) and written
    // very rarely (occasional refresh/put()). Reads don't conflict with each other - they
    // only need to be safe with respect to concurrent writes. A single mutual-exclusion lock
    // (synchronized or a plain Mutex) treats reads exactly like writes: only ONE thread total
    // can be inside the guarded section at a time, even if 100 threads only want to read.

    private static final int READER_THREADS = 6;
    private static final int READS_PER_THREAD = 3;
    private static final long SIMULATED_READ_LATENCY_MS = 150; // exaggerate to make contention visible

    // ================= NAIVE / UNSAFE APPROACH =================
    // (Not "unsafe" in the sense of corrupting data - synchronized IS correct - but it is
    // needlessly SLOW because it forces all readers to run one-at-a-time.)
    static class SynchronizedCache {
        private final Map<String, String> map = new HashMap<>();

        public synchronized void put(String key, String value) {
            map.put(key, value);
        }

        public synchronized String get(String key) {
            simulateSlowRead();
            return map.get(key);
        }

        private void simulateSlowRead() {
            try {
                Thread.sleep(SIMULATED_READ_LATENCY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static long runSynchronizedCacheDemo() throws InterruptedException {
        SynchronizedCache cache = new SynchronizedCache();
        cache.put("region", "us-east-1");

        ExecutorService pool = Executors.newFixedThreadPool(READER_THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < READER_THREADS; i++) {
            final int id = i;
            pool.submit(() -> {
                for (int r = 0; r < READS_PER_THREAD; r++) {
                    String value = cache.get("region");
                    System.out.println("[synchronized] reader-" + id + " read '" + value + "'");
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return System.currentTimeMillis() - start;
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // ReentrantReadWriteLock: multiple readers can hold the read lock SIMULTANEOUSLY as long
    // as no writer holds (or is waiting for, under fair mode) the write lock. The write lock
    // is exclusive against both other writers AND all readers.
    static class ReadWriteLockCache {
        private final Map<String, String> map = new HashMap<>();
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

        public void put(String key, String value) {
            rwLock.writeLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " acquired WRITE lock");
                map.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public String get(String key) {
            rwLock.readLock().lock();
            try {
                simulateSlowRead();
                return map.get(key);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        private void simulateSlowRead() {
            try {
                Thread.sleep(SIMULATED_READ_LATENCY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static long runReadWriteLockCacheDemo() throws InterruptedException {
        ReadWriteLockCache cache = new ReadWriteLockCache();
        cache.put("region", "us-east-1");

        ExecutorService pool = Executors.newFixedThreadPool(READER_THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < READER_THREADS; i++) {
            final int id = i;
            pool.submit(() -> {
                for (int r = 0; r < READS_PER_THREAD; r++) {
                    String value = cache.get("region");
                    System.out.println("[rwlock] reader-" + id + " read '" + value + "'");
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return System.currentTimeMillis() - start;
    }

    // ================= WHY THIS MECHANISM =================
    // - ReentrantReadWriteLock separates the "read lock" (shared, many holders) from the
    //   "write lock" (exclusive, one holder, excludes all readers too), so read-heavy
    //   workloads scale with the number of CPU cores instead of serializing on one monitor.
    // - It is reentrant like ReentrantLock, and can be configured fair or non-fair.

    // ================= EDGE CASES =================
    // - Lock downgrading (write -> read) IS supported: acquire write lock, then acquire read
    //   lock before releasing write lock, then release write lock - this keeps the data
    //   consistent view without letting another writer sneak in. Upgrading (read -> write)
    //   is NOT supported directly and will deadlock if attempted naively (must release read
    //   lock fully before acquiring write lock).
    // - Writer starvation: with a non-fair RRWL, a steady stream of readers can starve a
    //   waiting writer indefinitely; use `new ReentrantReadWriteLock(true)` for fairness.
    // - If reads are cheap (fast) and writes/contention are rare, plain synchronized or even
    //   ConcurrentHashMap may outperform RRWL because RRWL has higher per-acquisition overhead.
    // - Read locks do not provide mutual exclusion among themselves, so any state mutated
    //   inside a "read" method is still a race - read lock only protects against writers.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - synchronized cache: N readers each take SIMULATED_READ_LATENCY_MS *sequentially*,
    //   total wall time roughly N * latency / threads-available-but-serialized ~= N * latency.
    // - RWLock cache: readers overlap, total wall time approaches ~latency * (READS_PER_THREAD)
    //   regardless of READER_THREADS count (bounded by CPU/scheduler), a large speedup.
    // - Write operations are still fully serialized and exclude all readers in both designs.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - When would ReadWriteLock actually perform WORSE than a plain synchronized block?
    // - How do you safely downgrade from a write lock to a read lock? Why can't you upgrade?
    // - What is writer starvation and how does the `fair` constructor argument address it?
    // - How does ReentrantReadWriteLock differ from StampedLock (added in Java 8), and when
    //   would you pick StampedLock's optimistic read mode instead?
    // - Is ReentrantReadWriteLock reentrant across read and write for the SAME thread? What
    //   are the reentrancy rules (e.g. a thread holding the write lock CAN acquire the read lock)?
    // - Why might ConcurrentHashMap alone be a better fit than a hand-rolled RWLock cache?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) synchronized cache: readers block each other ===");
        long syncMillis = runSynchronizedCacheDemo();
        // Expected: total time approx READER_THREADS * READS_PER_THREAD * SIMULATED_READ_LATENCY_MS
        // (reads run one-at-a-time), e.g. 6 * 3 * 150ms ~= 2700ms.
        System.out.println("synchronized cache total time = " + syncMillis + " ms");

        System.out.println();
        System.out.println("=== 2) ReadWriteLock cache: readers run concurrently ===");
        long rwMillis = runReadWriteLockCacheDemo();
        // Expected: total time is much closer to READS_PER_THREAD * SIMULATED_READ_LATENCY_MS
        // (e.g. ~450-600ms) since readers overlap; should be noticeably less than syncMillis.
        System.out.println("ReadWriteLock cache total time = " + rwMillis + " ms");

        System.out.println();
        System.out.println("Speedup factor ~= " + String.format("%.2f", (double) syncMillis / rwMillis) + "x");
    }
}
