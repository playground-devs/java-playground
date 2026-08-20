package com.playground.java.interview.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PATTERN: ConcurrentHashMap + Lazy Expiration + Background Sweep (ScheduledExecutorService)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Build a thread-safe in-memory cache where each entry expires after a
 * configurable time-to-live, combining lazy (on-read) expiry with a periodic background sweep.
 */
public class InMemoryTTLCache<K, V> {

    // ================= PROBLEM =================
    // Design an in-memory key-value cache where every entry is inserted with a TTL
    // (time-to-live). Once an entry's TTL has elapsed, it must be treated as absent:
    //   - get(key): if the key doesn't exist OR has expired, return null (and remove it
    //     if expired). Otherwise return the value.
    //   - put(key, value, ttlMillis): store the value with an expiration timestamp
    //     computed as now + ttlMillis.
    //   - Entries should not live forever in memory just because nobody calls get() on
    //     them again -- a background thread must periodically sweep and purge expired
    //     entries even without being asked.
    //
    // Concrete example:
    //   cache.put("session:42", "userA", 100);   // expires in 100ms
    //   cache.get("session:42") -> "userA"        // immediately, still valid
    //   Thread.sleep(150);
    //   cache.get("session:42") -> null            // lazily detected as expired on read
    //   // OR, even without ever calling get() again, the background sweeper (running every
    //   // N ms) will have already removed "session:42" from the underlying map on its own.

    // ================= SIMPLE APPROACH =================
    // Use a plain HashMap<K, V> for values and a separate HashMap<K, Long> for expiry
    // timestamps, with no expiry checks anywhere -- entries just live forever until
    // manually removed. Not thread-safe either.

    // ================= WHY IT'S NOT ENOUGH =================
    // - No enforcement of TTL means memory grows unbounded with stale data (a classic memory
    //   leak in long-running services -- e.g. session caches, idempotency-key caches).
    // - Plain HashMap is not safe for concurrent get()/put() from multiple request-handling
    //   threads -- it can corrupt internal bucket structure or lose updates.
    // - Relying ONLY on lazy expiration (checking on get) means keys that are written once
    //   and never read again stay in memory forever even after expiring, since nothing ever
    //   triggers the check.

    // ================= OPTIMIZED APPROACH =================
    // Combine two complementary expiration strategies over a ConcurrentHashMap:
    //
    // 1. Storage: ConcurrentHashMap<K, CacheEntry<V>> where CacheEntry bundles the value with
    //    its absolute expiration timestamp (System.currentTimeMillis() + ttlMillis at write
    //    time). Using an absolute timestamp (not a remaining-duration counter) avoids needing
    //    to update every entry's countdown on every clock tick.
    //
    // 2. Lazy expiration (on get): before returning a value, compare
    //    entry.expiresAtMillis <= System.currentTimeMillis(). If expired:
    //      - remove the key from the map (using the atomic conditional
    //        ConcurrentHashMap.remove(key, expectedEntry) to avoid racing with a concurrent
    //        put() that may have already refreshed the same key)
    //      - return null as if the key never existed.
    //    This guarantees correctness for READERS even if the background sweep hasn't run yet.
    //
    // 3. Background sweep (ScheduledExecutorService): a single daemon thread runs a sweep
    //    task on a fixed period (e.g. every 500ms via scheduleAtFixedRate). Each sweep
    //    iterates entrySet() and removes any entry whose expiresAtMillis has passed. This
    //    guarantees that even keys nobody ever reads again are eventually reclaimed --
    //    bounding memory usage independent of read traffic.
    //
    // 4. Shutdown: expose a close()/shutdown() method that calls
    //    scheduler.shutdown() (allowing the daemon executor to be cleanly stopped, e.g. in
    //    application shutdown hooks or tests) -- otherwise the JVM may be kept alive or
    //    resources leaked in long test suites.
    //
    // Step-by-step for get(key):
    //   a. entry = map.get(key); if entry == null -> return null.
    //   b. if entry.isExpired(now) -> map.remove(key, entry) [conditional/atomic]; return null.
    //   c. else -> return entry.value.
    //
    // Step-by-step for put(key, value, ttlMillis):
    //   a. compute expiresAt = now + ttlMillis.
    //   b. map.put(key, new CacheEntry<>(value, expiresAt)) -- overwrites any prior entry,
    //      value or expiry, in O(1).
    //
    // Step-by-step for the background sweep (runs every sweepIntervalMillis):
    //   a. now = System.currentTimeMillis()
    //   b. for each (key, entry) in map: if entry.isExpired(now) -> map.remove(key, entry)
    //      [conditional remove so we never delete an entry that was refreshed concurrently]

    // ================= WHY THESE DATA STRUCTURES =================
    // - ConcurrentHashMap<K, CacheEntry<V>>: provides lock-striped (segment-level) concurrency
    //   so multiple threads can get()/put() different keys truly in parallel, unlike a
    //   synchronized HashMap which would serialize ALL access behind one lock. Its atomic
    //   remove(key, value) (compare-and-remove) is essential here: it lets both the lazy-get
    //   path and the background sweeper path independently and safely delete an expired entry
    //   without a race where one thread deletes a freshly-put() replacement value belonging
    //   to a NEW write that happened to land in the same key after expiry was detected.
    // - CacheEntry as an immutable holder of (value, expiresAtMillis): storing an absolute
    //   timestamp (not "seconds remaining") means checking expiry is a single O(1) comparison
    //   against the current time, with no need to update stored entries as time passes.
    // - ScheduledExecutorService (single-thread, daemon): decouples cleanup from request
    //   traffic. It is the standard JDK tool for "run this repeatedly on a fixed period" and
    //   integrates with ordinary Runnable/lambda code, unlike hand-rolling a Thread + sleep
    //   loop (which the JDK class already handles correctly, including drift and cancellation).
    // - Net effect: memory is bounded by (a) lazy cleanup on access for hot keys, and (b)
    //   periodic global sweep for cold/never-read-again keys -- the two strategies are
    //   complementary, not redundant: lazy expiry gives correctness instantly on the read
    //   path even if the sweep interval hasn't elapsed yet; the sweep guarantees eventual
    //   reclamation for keys nobody reads.

    // ================= EDGE CASES =================
    // - ttlMillis <= 0 on put(): treat as "already expired" / effectively a no-op that a
    //   subsequent get() would see as a miss (expiresAt computed as now + ttl would be <= now).
    // - get() on a missing key: returns null, same as a key that expired -- callers cannot
    //   distinguish "never existed" from "expired" from the return value alone (documented).
    // - put() on an existing key: fully overwrites both the value and TTL clock -- the TTL is
    //   NOT extended from the old expiry, it restarts fresh from "now" with the new ttlMillis.
    // - Expiry exactly at the boundary: this implementation treats expiresAtMillis <= now as
    //   EXPIRED (strictly less-than would let the value linger for one extra tick), i.e. an
    //   entry is considered expired at the instant its TTL elapses, not one tick after.
    // - Concurrent access: get()/put() from many threads are safe due to ConcurrentHashMap;
    //   the sweep thread and reader threads racing to expire the SAME key are safe because
    //   both use the conditional remove(key, expectedEntry) so only one of them actually wins
    //   the removal and a concurrent put() replacing the value is never accidentally deleted.
    // - Cache never shut down: the ScheduledExecutorService thread is a daemon thread so it
    //   will not prevent JVM exit, but calling shutdown() explicitly is still best practice
    //   to release the thread promptly.

    // ================= COMPLEXITY =================
    // Time Complexity:
    //   get(key):  O(1) average -- one ConcurrentHashMap lookup plus an O(1) timestamp check
    //              and, if expired, one O(1) conditional remove.
    //   put(key,value,ttl): O(1) average -- one ConcurrentHashMap put.
    //   background sweep tick: O(n) where n = number of entries currently in the map, since
    //              it must visit every entry once per sweep interval; this is amortized
    //              background cost, not part of the per-request critical path.
    // Space Complexity: O(n) where n = number of live (non-expired-and-not-yet-swept) entries;
    //   each entry stores a value reference plus one long timestamp.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you avoid the O(n) full-map scan on every sweep tick for a huge cache?
    //   Maintain a min-heap (PriorityQueue) or a time-bucketed wheel (like a "timer wheel")
    //   ordered by expiresAtMillis so the sweeper only pops entries that have actually
    //   expired instead of scanning everything.
    // - Is this cache thread-safe, and what specifically makes it so? Yes for map operations,
    //   via ConcurrentHashMap's internal lock striping; correctness under concurrent expiry
    //   races is guaranteed specifically by using the atomic remove(key, expectedValue)
    //   overload rather than a plain remove(key), which would risk deleting a value written
    //   by a concurrent put() after the expiry check but before the removal executes.
    // - How would you scale this to millions of entries / a distributed deployment? Move to
    //   Redis/Memcached which have native TTL support (EXPIRE/PEXPIRE) and eviction policies,
    //   or use an off-heap embedded cache (Caffeine, Ehcache) to avoid GC pressure from
    //   millions of heap objects; for multi-node consistency, centralize TTL state in the
    //   shared store rather than per-JVM local caches.
    // - What happens if the sweep interval is very large (or the sweeper thread dies)? Only
    //   lazy expiration protects correctness (reads still never return stale expired data);
    //   memory reclamation for cold keys is simply delayed. Consider monitoring/alerting on
    //   the sweeper's health and unbounded map growth.
    // - Why store an absolute expiresAtMillis instead of a remaining-TTL duration? So that
    //   checking expiry never requires touching/updating unread entries as time passes --
    //   it turns "is this expired" into a single stateless comparison against the current
    //   clock, independent of how much time has passed since it was written.
    // - How would you support "refresh on read" / sliding-window TTL instead of a fixed
    //   absolute expiry? On get(), atomically replace the entry with a new CacheEntry whose
    //   expiresAtMillis is recomputed as now + originalTtl, effectively resetting the clock
    //   every time the key is touched.
    // - How would you unit test time-dependent expiry without flaky real sleeps? Inject a
    //   Clock/time-supplier abstraction instead of calling System.currentTimeMillis()
    //   directly, so tests can advance a fake clock deterministically.
    // - What's the risk of the background thread pool being non-daemon in a server app?
    //   It would prevent the JVM from exiting cleanly on shutdown unless explicitly
    //   shut down, which is why this implementation creates the executor with a
    //   daemon-thread factory.

    /** Immutable holder for a cached value plus its absolute expiration timestamp. */
    private static final class CacheEntry<V> {
        final V value;
        final long expiresAtMillis;

        CacheEntry(V value, long expiresAtMillis) {
            this.value = value;
            this.expiresAtMillis = expiresAtMillis;
        }

        boolean isExpired(long nowMillis) {
            return expiresAtMillis <= nowMillis;
        }
    }

    private final ConcurrentHashMap<K, CacheEntry<V>> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private static final AtomicLong THREAD_COUNTER = new AtomicLong();

    /**
     * @param sweepIntervalMillis how often the background sweeper scans for expired entries.
     */
    public InMemoryTTLCache(long sweepIntervalMillis) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "ttl-cache-sweeper-" + THREAD_COUNTER.incrementAndGet());
            t.setDaemon(true); // never blocks JVM shutdown
            return t;
        });
        this.scheduler.scheduleAtFixedRate(
                this::sweepExpiredEntries, sweepIntervalMillis, sweepIntervalMillis, TimeUnit.MILLISECONDS);
    }

    /** Stores value under key with the given TTL, restarting the expiry clock from now. */
    public void put(K key, V value, long ttlMillis) {
        long expiresAt = System.currentTimeMillis() + ttlMillis;
        map.put(key, new CacheEntry<>(value, expiresAt));
    }

    /** Returns the value if present and not expired; otherwise null (lazy expiration). */
    public V get(K key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null) {
            return null; // never existed (or already swept away)
        }
        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            map.remove(key, entry); // conditional remove avoids racing a concurrent put()
            return null;
        }
        return entry.value;
    }

    /** Returns the number of entries currently stored, INCLUDING any not-yet-swept expired ones. */
    public int size() {
        return map.size();
    }

    /** Background task: purge all entries whose TTL has elapsed. */
    private void sweepExpiredEntries() {
        long now = System.currentTimeMillis();
        map.forEach((key, entry) -> {
            if (entry.isExpired(now)) {
                map.remove(key, entry); // conditional remove: safe against concurrent refresh
            }
        });
    }

    /** Cleanly stops the background sweeper thread. */
    public void shutdown() {
        scheduler.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        // Example 1: basic put/get before expiry
        InMemoryTTLCache<String, String> cache = new InMemoryTTLCache<>(200);
        cache.put("session:42", "userA", 300);
        System.out.println(cache.get("session:42")); // Expected: userA (still valid)

        // Example 2: lazy expiration on get() after TTL elapses
        Thread.sleep(350);
        System.out.println(cache.get("session:42")); // Expected: null (expired, lazily detected)

        // Example 3: background sweeper removes entries even without a get() call
        cache.put("temp:1", "willBeSwept", 100);
        System.out.println("size before sweep window: " + cache.size()); // Expected: 1
        Thread.sleep(500); // longer than sweepIntervalMillis(200) + ttl(100)
        System.out.println("size after sweep window: " + cache.size()); // Expected: 0 (swept by background thread)

        // Example 4: put() on existing key restarts the TTL clock and updates the value
        cache.put("counter", "v1", 1000);
        Thread.sleep(50);
        cache.put("counter", "v2", 1000); // overwritten before it expired; clock restarts
        System.out.println(cache.get("counter")); // Expected: v2

        // Example 5: get() on a key that was never inserted
        System.out.println(cache.get("does-not-exist")); // Expected: null

        cache.shutdown(); // stop the daemon sweeper thread cleanly
    }
}
