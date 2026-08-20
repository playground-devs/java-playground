package com.playground.java.interview.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: HashMap + Doubly Linked List, guarded by a single ReentrantLock (with sharding
 * described as the scale-out alternative)
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Make the classic O(1) LRU cache safe for concurrent multi-threaded
 * access without corrupting its internal linked-list/map state.
 */
public class ConcurrentLRUCache<K, V> {

    // ================= PROBLEM =================
    // Take the standard LRU cache (fixed capacity, evict least-recently-used entry on
    // overflow, O(1) get/put) and make get()/put() safe to call concurrently from many
    // threads without race conditions corrupting the internal doubly linked list or map.
    //
    //   - get(key): return the value and mark key as most-recently-used, or null if absent.
    //   - put(key, value): insert/update value and mark most-recently-used; evict the
    //     least-recently-used entry if over capacity.
    //   - Must behave correctly (no lost updates, no corrupted list pointers, no duplicate
    //     nodes) when called from multiple threads at once.
    //
    // Concrete example (capacity = 2, single-threaded semantics must still hold under
    // concurrent load):
    //   put(1, "A"); put(2, "B")            // MRU order: [2, 1] (2 most recent)
    //   get(1)                               // -> "A"; MRU order becomes [1, 2]
    //   put(3, "C")                          // evicts LRU = 2; MRU order: [3, 1]
    //   get(2) -> null                       // evicted
    //   // The above must produce these same results even if, e.g., two threads call
    //   // put(3,"C") and get(1) at almost the same instant -- the operations must be
    //   // linearizable (appear to happen in SOME consistent total order), not interleaved
    //   // in a way that corrupts the linked list (e.g. a node pointing to a freed node).

    // ================= SIMPLE APPROACH =================
    // Reuse the plain HashMap<K,Node> + doubly linked list LRU design unmodified and just
    // call it from multiple threads with no synchronization at all.

    // ================= WHY IT'S NOT ENOUGH =================
    // The classic LRU implementation mutates two data structures per operation (the HashMap
    // AND the linked list's prev/next pointers) as a multi-step, non-atomic sequence (e.g.
    // "unlink node from its old position, then relink at the head"). If two threads run
    // put()/get() concurrently without synchronization:
    //   - Two threads could both try to unlink/relink the SAME node concurrently, corrupting
    //     prev/next pointers (e.g. a node ends up pointing to itself, or the list becomes a
    //     broken chain / loses entries entirely).
    //   - HashMap itself is not thread-safe for concurrent structural modification (put/remove
    //     while another thread iterates or resizes can corrupt bucket chains or lose entries).
    //   - Even switching the map alone to a ConcurrentHashMap is NOT sufficient, because the
    //     doubly linked list (the part that tracks recency order) still has no protection at
    //     all -- LRU ordering fundamentally requires coordinating the map lookup and the list
    //     reordering as one atomic operation.

    // ================= OPTIMIZED APPROACH =================
    // Approach implemented here: wrap EVERY public operation (get, put) in a single
    // ReentrantLock that guards both the HashMap and the doubly linked list together, so
    // each get()/put() call executes as one atomic, linearizable unit -- identical semantics
    // to the single-threaded LRU, just safe under concurrent callers.
    //
    // Step-by-step for get(key):
    //   a. lock.lock()
    //   b. node = map.get(key); if null -> unlock, return null.
    //   c. unlink node from its current position in the list.
    //   d. insert node at the head (most-recently-used end).
    //   e. lock.unlock(); return node.value.
    //
    // Step-by-step for put(key, value):
    //   a. lock.lock()
    //   b. if key exists: update node.value; unlink + move to head (same as get's reordering).
    //   c. else (new key):
    //        if map.size() == capacity: evict tail.prev (the LRU node) -- unlink it from the
    //          list AND remove it from the map.
    //        create new node; map.put(key, node); insert node at head.
    //   d. lock.unlock()
    //
    // Trade-off vs. sharding (described here, NOT implemented, per the assignment):
    //   A single lock is simple and gives perfect global LRU ordering, but it SERIALIZES all
    //   access -- every thread, even ones touching completely unrelated keys, contends for
    //   the same lock, so throughput does not scale with additional CPU cores under
    //   concurrent load.
    //   The alternative, sharded design: partition the keyspace into N independent LRU
    //   segments (each an ordinary single-threaded HashMap+DLL LRU with its OWN
    //   ReentrantLock and its OWN capacity = totalCapacity / N), and route each key to
    //   exactly one segment via segmentIndex = Math.floorMod(key.hashCode(), N). Concurrent
    //   operations on keys landing in DIFFERENT segments proceed in true parallel with zero
    //   contention (this is exactly the design ConcurrentHashMap itself uses internally,
    //   historically via lock striping). The cost is that LRU ordering/eviction is now only
    //   correct WITHIN each shard, not globally: it's possible for a globally-rarely-used key
    //   to survive while a globally-more-recently-used key in a different (busier) shard gets
    //   evicted first, because each shard only knows about its own slice of the traffic.
    //   This is generally an acceptable trade in practice (e.g. Guava's CacheBuilder /
    //   Caffeine both shard internally for this reason), and is the standard answer to "how
    //   do you make this scale to more cores" in interviews.

    // ================= WHY THESE DATA STRUCTURES =================
    // - HashMap<K, Node<K,V>>: O(1) average lookup from key to its position (Node) in the
    //   list -- identical role to plain LRU; NOT a ConcurrentHashMap here because all access
    //   to it is already fully serialized by the outer ReentrantLock, so the extra internal
    //   striping/CAS machinery of ConcurrentHashMap would be pure overhead with no benefit
    //   (only one thread is ever inside the critical section at a time).
    // - Doubly linked list with sentinel head/tail nodes: gives O(1) unlink of an arbitrary
    //   node (needed to move an accessed node) and O(1) insertion at the head / removal at
    //   the tail (needed for MRU promotion and LRU eviction) -- same justification as classic
    //   single-threaded LRU. Using sentinel (dummy) head/tail nodes (rather than nullable
    //   head/tail references) removes null-checks for empty-list edge cases from every
    //   insert/remove operation.
    // - ReentrantLock (vs. plain `synchronized`): chosen so the design can be discussed
    //   alongside the sharded alternative naturally (one ReentrantLock per shard) and because
    //   ReentrantLock exposes tryLock()/lockInterruptibly() if a caller ever wanted
    //   fail-fast-under-contention semantics instead of blocking -- functionally, a
    //   `synchronized(this)` block would provide equivalent correctness for this simple
    //   single-lock version.
    // - Net result: because the ENTIRE map+list mutation sequence for a given operation runs
    //   while holding the one lock, each get()/put() is atomic and linearizable -- concurrent
    //   callers observe results consistent with SOME serial execution order, eliminating the
    //   corruption risks described above. The cost is that this atomicity is achieved by
    //   forcing full serialization (only one thread makes progress at a time), which is
    //   exactly the trade-off called out above vs. sharding.

    // ================= EDGE CASES =================
    // - capacity == 0: put() must never insert (map.size() == capacity is true even before
    //   the first insert, i.e. 0 == 0, but there's nothing to evict) -- must guard capacity<=0
    //   explicitly as a no-op, since attempting to evict from an empty list would be invalid.
    // - get() on a missing key: returns null immediately without touching the list (must not
    //   accidentally insert a sentinel or otherwise mutate state on a miss).
    // - put() on an existing key: updates the value in place (does NOT create a second node
    //   for the same key) and still promotes it to most-recently-used.
    // - capacity == 1: every new-key put() evicts the sole existing entry.
    // - Concurrent access (the central concern of this file): all mutation (map AND list) for
    //   a single get()/put() call happens while holding the single lock, so no interleaving
    //   of two threads' unlink/relink sequences is possible -- verified conceptually by the
    //   fact that the lock is held for the ENTIRE duration of each public method, not just
    //   part of it.
    // - Fairness/starvation: a plain (non-fair) ReentrantLock does not guarantee FIFO ordering
    //   among waiting threads by default; under sustained heavy contention some threads could
    //   theoretically be repeatedly overtaken (rare in practice, but constructible with
    //   `new ReentrantLock(true)` for strict fairness at a throughput cost if ever required).

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) average per get()/put() call while the lock is uncontended --
    //   one HashMap lookup/insert plus a constant number of doubly-linked-list pointer
    //   updates. Under contention, additional calls must wait for the lock to be released,
    //   so WALL-CLOCK latency under concurrent load is O(1) work per call but throughput is
    //   bounded by full serialization (effectively 1 operation executing at a time across the
    //   whole cache, regardless of core count) -- this is exactly the cost called out in the
    //   OPTIMIZED APPROACH trade-off discussion above.
    // Space Complexity: O(capacity) -- one HashMap entry and one list node per cached key,
    //   plus two constant-size sentinel nodes.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is a single lock "simple but serializes all access", concretely? Because the lock
    //   guards the ENTIRE map+list, two threads accessing completely unrelated keys (e.g.
    //   thread A touching key 1, thread B touching key 999) still cannot run concurrently --
    //   one must wait for the other to finish its whole operation, even though their actual
    //   data doesn't overlap at all.
    // - How would you implement the sharded alternative concretely? An array of N
    //   independent ConcurrentLRUCache-like segments, each with capacity/N and its own lock;
    //   route get/put to segmentIndex = Math.floorMod(key.hashCode(), N); total capacity is
    //   approximate (rounding across segments) and LRU eviction order is only exact
    //   per-segment, not globally -- exactly as ConcurrentHashMap historically striped its
    //   internal locks before Java 8's CAS-based bins.
    // - How would you scale this to millions of entries / a distributed cache across multiple
    //   machines? A single JVM's heap and a single lock (or even sharded locks) cap out well
    //   before millions of hot entries under heavy concurrent load; production systems at
    //   that scale typically move to Caffeine (lock-free ring-buffer based recency tracking,
    //   no single global lock) in-process, or an external distributed cache (Redis cluster
    //   with LRU/LFU eviction policies built in, sharded via consistent hashing) so no single
    //   node's lock is a bottleneck.
    // - Could you avoid locking entirely with lock-free structures? Yes in principle (e.g.
    //   Caffeine uses a lock-free ring buffer to batch/record accesses and replays them
    //   against the eviction policy asynchronously off the hot path), but a fully correct
    //   lock-free doubly-linked-list with arbitrary-node removal is notoriously hard to get
    //   right (ABA problems, safe memory reclamation) -- interviewers generally want you to
    //   recognize this complexity exists, not necessarily implement it live.
    // - What's the difference between using `synchronized` and `ReentrantLock` here? For this
    //   single-lock design, functionally equivalent; ReentrantLock adds tryLock (fail fast
    //   instead of blocking), timed tryLock, lockInterruptibly, and the ability to have
    //   multiple wait-conditions (Condition objects) if ever needed -- none of which this
    //   simple version exploits beyond the design flexibility.
    // - How would you add read/write differentiation for higher throughput (e.g.
    //   ReadWriteLock)? LRU's get() is NOT actually a pure read -- it mutates recency order
    //   (moves a node in the list) -- so a standard ReadWriteLock doesn't straightforwardly
    //   help here the way it would for a cache with no access-order tracking; this is a good
    //   discussion point about why LRU specifically resists naive reader/writer lock splitting.
    // - How would you test that this is actually free of race conditions? Spin up many
    //   threads hammering put()/get() on overlapping keys concurrently (e.g. via an
    //   ExecutorService + CountDownLatch to start them simultaneously), then assert the final
    //   cache size never exceeds capacity and that no exceptions/corrupted state occur --
    //   true linearizability is hard to assert directly, but invariant checks (size bound,
    //   no duplicate keys, list length matches map size) catch most corruption bugs.
    // - Why must eviction and insertion happen under the SAME lock acquisition as the
    //   existence check, rather than as two separate locked steps? To avoid a
    //   check-then-act race: if "check if key exists" and "insert/evict" were two separate
    //   critical sections, two threads could both see capacity is full, both decide to evict,
    //   and end up evicting two entries for what should have been a single insert (or worse,
    //   both insert past capacity).

    /** Doubly linked list node holding a key/value pair. */
    private static final class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final Node<K, V> head; // sentinel: head.next = most-recently-used
    private final Node<K, V> tail; // sentinel: tail.prev = least-recently-used
    private final ReentrantLock lock = new ReentrantLock();

    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) {
                return null; // missing key
            }
            moveToHead(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            if (capacity <= 0) {
                return; // capacity 0 -> never store anything
            }
            Node<K, V> existing = map.get(key);
            if (existing != null) {
                existing.value = value; // update value on existing key
                moveToHead(existing);
                return;
            }
            if (map.size() == capacity) {
                Node<K, V> lru = tail.prev;
                unlink(lru);
                map.remove(lru.key);
            }
            Node<K, V> fresh = new Node<>(key, value);
            map.put(key, fresh);
            insertAtHead(fresh);
        } finally {
            lock.unlock();
        }
    }

    /** Detaches a node from its current position in the list (pointers only, not from map). */
    private void unlink(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /** Inserts a node immediately after the head sentinel (most-recently-used position). */
    private void insertAtHead(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    /** Moves an already-linked node to the most-recently-used position. */
    private void moveToHead(Node<K, V> node) {
        unlink(node);
        insertAtHead(node);
    }

    public static void main(String[] args) throws InterruptedException {
        // Example 1: basic LRU eviction order, single-threaded sanity check
        ConcurrentLRUCache<Integer, String> cache = new ConcurrentLRUCache<>(2);
        cache.put(1, "A");
        cache.put(2, "B");
        System.out.println(cache.get(1)); // Expected: A (1 promoted to MRU)
        cache.put(3, "C");                // evicts LRU = key 2
        System.out.println(cache.get(2)); // Expected: null (evicted)
        System.out.println(cache.get(1)); // Expected: A
        System.out.println(cache.get(3)); // Expected: C

        // Example 2: put() on an existing key updates the value without evicting
        ConcurrentLRUCache<Integer, String> cache2 = new ConcurrentLRUCache<>(2);
        cache2.put(1, "old");
        cache2.put(1, "new");
        System.out.println(cache2.get(1)); // Expected: new

        // Example 3: capacity 0 -> nothing is ever stored
        ConcurrentLRUCache<Integer, String> zeroCap = new ConcurrentLRUCache<>(0);
        zeroCap.put(1, "X");
        System.out.println(zeroCap.get(1)); // Expected: null (capacity 0)

        // Example 4: concurrent access from multiple threads -- verifies no corruption and
        // that the cache never exceeds its configured capacity under concurrent load.
        final int capacity = 16;
        final ConcurrentLRUCache<Integer, Integer> concurrentCache = new ConcurrentLRUCache<>(capacity);
        final int threadCount = 8;
        final int opsPerThread = 2000;
        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    int key = (threadId * 31 + i) % 50; // overlapping keyspace across threads
                    concurrentCache.put(key, key * 10);
                    concurrentCache.get(key);
                }
            });
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("cache size after concurrent load (expect <= capacity="
                + capacity + "): " + concurrentCache.map.size()); // Expected: <= 16, no corruption/exception
    }
}
