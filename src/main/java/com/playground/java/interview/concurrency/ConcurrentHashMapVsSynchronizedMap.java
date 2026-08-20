package com.playground.java.interview.concurrency;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: ConcurrentHashMap vs Collections.synchronizedMap(HashMap)
 * PRIORITY: P1 - High Priority
 * TOPIC: ConcurrentHashMap uses fine-grained internal locking (bucket/node-level, CAS for
 * many reads) for much higher concurrency than a single global lock around a HashMap, and
 * its iterators are weakly-consistent so they never throw ConcurrentModificationException.
 */
public class ConcurrentHashMapVsSynchronizedMap {

    // ================= PROBLEM =================
    // We need a thread-safe Map. Two common off-the-shelf options:
    //   1) Collections.synchronizedMap(new HashMap<>())  - wraps every method call in
    //      "synchronized(mutex)" where mutex is ONE shared lock object for the whole map.
    //   2) java.util.concurrent.ConcurrentHashMap - internally partitions locking so that
    //      unrelated buckets/keys can be updated concurrently, and most reads (get) don't
    //      lock at all (they rely on volatile reads of internal nodes).
    // They differ in (a) throughput under concurrent access and (b) iteration semantics.

    private static final int THREADS = 8;
    private static final int OPS_PER_THREAD = 5_000;

    // ================= NAIVE / UNSAFE APPROACH =================
    // "Naive" here = correct but coarse-grained: synchronizedMap serializes EVERY operation
    // (get AND put) behind one global monitor, so concurrent readers/writers queue up.
    static long benchmarkSynchronizedMap() throws InterruptedException {
        Map<Integer, Integer> map = java.util.Collections.synchronizedMap(new HashMap<>());
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        long start = System.nanoTime();
        for (int t = 0; t < THREADS; t++) {
            final int threadId = t;
            pool.submit(() -> {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    int key = (threadId * OPS_PER_THREAD + i) % 1000;
                    map.put(key, i);       // whole map locked for this call
                    map.get(key);          // whole map locked for this call too
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return (System.nanoTime() - start) / 1_000_000; // ms
    }

    // Iterating a synchronizedMap WITHOUT external synchronization while another thread
    // mutates it -> real ConcurrentModificationException (or worse, in edge cases).
    static boolean demonstrateSynchronizedMapIterationHazard() throws InterruptedException {
        Map<Integer, Integer> map = java.util.Collections.synchronizedMap(new HashMap<>());
        for (int i = 0; i < 1000; i++) {
            map.put(i, i);
        }
        AtomicInteger caught = new AtomicInteger(0);

        Thread mutator = new Thread(() -> {
            for (int i = 1000; i < 5000; i++) {
                map.put(i, i); // structural modification while iterator below may be mid-traversal
                Thread.yield();
            }
        }, "mutator");

        Thread iterator = new Thread(() -> {
            try {
                // Per the Javadoc, iterating a synchronizedMap requires the CALLER to manually
                // synchronize on the map itself for the whole traversal - we deliberately
                // skip that here to demonstrate the hazard.
                for (Integer key : map.keySet()) {
                    // touch it
                    Integer ignored = map.get(key);
                }
            } catch (ConcurrentModificationException e) {
                caught.incrementAndGet();
            }
        }, "iterator");

        mutator.start();
        iterator.start();
        mutator.join(5000);
        iterator.join(5000);
        return caught.get() > 0;
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // ConcurrentHashMap: fine-grained locking internally (historically per-segment in Java 7,
    // now per-bin/node with CAS + synchronized only on the specific bin being modified since
    // Java 8), so unrelated keys can be written concurrently, and get() is typically lock-free.
    static long benchmarkConcurrentHashMap() throws InterruptedException {
        Map<Integer, Integer> map = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        long start = System.nanoTime();
        for (int t = 0; t < THREADS; t++) {
            final int threadId = t;
            pool.submit(() -> {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    int key = (threadId * OPS_PER_THREAD + i) % 1000;
                    map.put(key, i);
                    map.get(key);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return (System.nanoTime() - start) / 1_000_000; // ms
    }

    // Iterating a ConcurrentHashMap DURING concurrent mutation is safe by design: the
    // iterator is "weakly consistent" - it reflects the state at some point at or since
    // creation, never throws ConcurrentModificationException, and never needs external locking.
    static boolean demonstrateConcurrentHashMapIterationIsSafe() throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 1000; i++) {
            map.put(i, i);
        }
        AtomicInteger caught = new AtomicInteger(0);

        Thread mutator = new Thread(() -> {
            for (int i = 1000; i < 5000; i++) {
                map.put(i, i); // safe to mutate concurrently with iteration below
                Thread.yield();
            }
        }, "mutator");

        Thread iterator = new Thread(() -> {
            try {
                for (Integer key : map.keySet()) { // no external synchronization needed
                    Integer ignored = map.get(key);
                }
            } catch (ConcurrentModificationException e) {
                caught.incrementAndGet();
            }
        }, "iterator");

        mutator.start();
        iterator.start();
        mutator.join(5000);
        iterator.join(5000);
        return caught.get() == 0; // true means "no exception, as expected"
    }

    // ================= WHY THIS MECHANISM =================
    // - Collections.synchronizedMap wraps a delegate map with a single intrinsic lock; simple,
    //   safe, but effectively single-threaded access (one global bottleneck).
    // - ConcurrentHashMap avoids one global lock: writes lock only the affected bin (linked
    //   list/tree node) via synchronized on that bin's first node, combined with CAS for bin
    //   creation; reads (get) traverse volatile-published nodes without locking at all, so
    //   readers never block writers or other readers in the common case.
    // - This is why CHM iterators are documented as "weakly consistent" - they walk live,
    //   volatile-linked structures rather than taking a lock for the whole traversal.

    // ================= EDGE CASES =================
    // - synchronizedMap: compound actions like "if (!map.containsKey(k)) map.put(k, v)" are
    //   STILL races unless you manually synchronize on the map for the whole compound action.
    // - ConcurrentHashMap: provides atomic compound methods for exactly this - putIfAbsent,
    //   computeIfAbsent, merge, compute - use these instead of check-then-act.
    // - ConcurrentHashMap disallows null keys and null values (throws NullPointerException) -
    //   this is intentional, to avoid ambiguity between "absent" and "mapped to null" in a
    //   concurrent context; HashMap/synchronizedMap allow one null key and multiple null values.
    // - size() on ConcurrentHashMap is an approximation under concurrent modification (it was
    //   historically computed without a full lock); use mappingCount() for a long-based estimate.
    // - Iterating synchronizedMap safely requires: `synchronized(map) { for (... : map.keySet()) ... }`
    //   which reintroduces the global-lock bottleneck for the whole iteration.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - synchronizedMap: O(1) amortized per op, but effectively serialized -> throughput does
    //   NOT scale with additional threads/cores; can even regress due to lock contention/overhead.
    // - ConcurrentHashMap: O(1) amortized per op, and throughput scales much better with thread
    //   count for disjoint keys, because contention is limited to threads hitting the SAME bin.
    // - Expect the ConcurrentHashMap benchmark below to complete in noticeably less wall time
    //   than the synchronizedMap benchmark, especially as THREADS increases.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Describe how ConcurrentHashMap's internal locking evolved from Java 7 (segments) to
    //   Java 8+ (per-bin locking via synchronized + CAS, treeified bins for long chains).
    // - Why does ConcurrentHashMap forbid null keys/values while HashMap allows them?
    // - What does "weakly consistent iterator" mean precisely, and how is it different from
    //   fail-fast iterators (which throw ConcurrentModificationException)?
    // - Why is `if (!map.containsKey(k)) map.put(k, v)` broken even on a synchronizedMap, and
    //   how does computeIfAbsent solve it atomically on ConcurrentHashMap?
    // - Is `size()` reliable on ConcurrentHashMap under concurrent writes? What about mappingCount()?
    // - When would Collections.synchronizedMap still be an acceptable/better choice (e.g. simplicity,
    //   need for a null key, wrapping an existing Map instance)?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) Throughput: synchronizedMap vs ConcurrentHashMap ===");
        long syncMs = benchmarkSynchronizedMap();
        long chmMs = benchmarkConcurrentHashMap();
        // Expected: chmMs is typically lower than syncMs (fine-grained locking beats one
        // global lock under concurrent put/get from multiple threads); exact numbers vary by machine.
        System.out.println("synchronizedMap:    " + syncMs + " ms");
        System.out.println("ConcurrentHashMap:  " + chmMs + " ms");

        System.out.println();
        System.out.println("=== 2) Iteration hazard: synchronizedMap without external lock ===");
        boolean cmeSeen = demonstrateSynchronizedMapIterationHazard();
        // Expected: true (or at least likely) - concurrent structural modification during
        // unsynchronized iteration commonly throws ConcurrentModificationException. This is
        // timing-dependent, so an occasional run may not trigger it, but it CAN happen and
        // the Javadoc explicitly warns external synchronization is required.
        System.out.println("ConcurrentModificationException observed on synchronizedMap iteration: " + cmeSeen);

        System.out.println();
        System.out.println("=== 3) Safe iteration: ConcurrentHashMap during concurrent mutation ===");
        boolean noExceptionOnChm = demonstrateConcurrentHashMapIterationIsSafe();
        // Expected: true, always - ConcurrentHashMap's weakly consistent iterator never
        // throws ConcurrentModificationException regardless of concurrent puts.
        System.out.println("No exception during ConcurrentHashMap iteration (expected true): " + noExceptionOnChm);
    }
}
