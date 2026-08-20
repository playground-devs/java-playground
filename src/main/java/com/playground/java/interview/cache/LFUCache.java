package com.playground.java.interview.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * PATTERN: HashMap + Frequency Buckets (LinkedHashSet) + minFreq pointer
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Design a fixed-capacity cache that evicts the Least Frequently Used
 * item on overflow, breaking ties by Least Recently Used among equally-frequent items.
 */
public class LFUCache {

    // ================= PROBLEM =================
    // Design and implement a data structure for a Least Frequently Used (LFU) cache.
    //
    // Requirements:
    //   - get(key): return the value if key exists, else return -1. This counts as a "use"
    //     and increments the key's access frequency.
    //   - put(key, value): insert or update the value of the key. If the cache is at
    //     capacity, evict the least frequently used key before inserting a new key.
    //     If there is a tie (multiple keys with the same lowest frequency), evict the
    //     LEAST RECENTLY USED among them.
    //   - Both operations must run in O(1) average time.
    //
    // Concrete example (capacity = 2):
    //   put(1, "A")          // cache: {1=A(freq1)}
    //   put(2, "B")          // cache: {1=A(freq1), 2=B(freq1)}
    //   get(1)   -> "A"      // freq(1) becomes 2
    //   put(3, "C")          // capacity full; freq(2)=1 is lowest -> evict key 2
    //                        // cache: {1=A(freq2), 3=C(freq1)}
    //   get(2)   -> -1       // evicted
    //   get(3)   -> "C"      // freq(3) becomes 2
    //   put(4, "D")          // freq(1)=2 and freq(3)=2 tie -> evict LRU among them -> key 1
    //                        // cache: {3=C(freq2), 4=D(freq1)}
    //   get(1)   -> -1       // evicted

    // ================= SIMPLE APPROACH =================
    // Keep a single HashMap<Key, Node> where Node stores (value, frequency, lastUsedTimestamp).
    // On get()/put() that touches an existing key, bump frequency and timestamp.
    // On eviction, scan ALL entries to find the minimum frequency, and among ties the
    // oldest timestamp -> O(n) scan per eviction.

    // ================= WHY IT'S NOT ENOUGH =================
    // Interview requirement is O(1) average time for BOTH get and put, including eviction.
    // A linear scan across every entry to find the min-frequency / LRU-tiebreak candidate
    // violates that bound and would be unacceptably slow for large caches (millions of keys).
    // We need a structure that can jump straight to "the least frequently used key" and,
    // within that frequency, straight to "the least recently used key" -- both in O(1).

    // ================= OPTIMIZED APPROACH =================
    // The classic O(1) LFU design uses THREE coordinated structures:
    //
    // 1. keyMap: HashMap<Integer key, Node> - O(1) lookup of a key's current value & frequency.
    //
    // 2. freqMap: HashMap<Integer freq, LinkedHashSet<Integer key>> - groups all keys that
    //    currently share the same access frequency. A LinkedHashSet preserves INSERTION ORDER,
    //    so within a frequency bucket the first element is the least-recently-used key at that
    //    frequency, and the last element is the most-recently-used. This gives us the LRU
    //    tie-break for free, in O(1), via iterator().next() to peek the LRU and remove(key)/add(key)
    //    to reposition a key to "most recent" within its bucket.
    //
    // 3. minFreq: an int pointer tracking the current lowest frequency present in freqMap.
    //    Maintained incrementally (never recomputed by scanning) so eviction is O(1):
    //       - On put() of a brand-new key -> minFreq is reset to 1 (new keys always start there).
    //       - On touch (get or put-existing) of a key whose old frequency bucket becomes empty
    //         AND that bucket was minFreq -> minFreq++ (only possible when the emptied bucket
    //         was exactly the min, so a simple increment suffices, no scan needed).
    //
    // Step-by-step for touch(key) [shared by get() and put()-on-existing-key]:
    //   a. node = keyMap.get(key); oldFreq = node.freq
    //   b. freqMap.get(oldFreq).remove(key)              // O(1) LinkedHashSet removal
    //   c. if freqMap.get(oldFreq) is now empty:
    //        remove that bucket from freqMap
    //        if oldFreq == minFreq: minFreq++            // the min bucket just vanished
    //   d. node.freq = oldFreq + 1
    //   e. freqMap.computeIfAbsent(node.freq, k -> new LinkedHashSet<>()).add(key)
    //      // appended at the end -> most-recently-used within the new bucket
    //
    // Step-by-step for put(key, value):
    //   a. if capacity == 0: no-op, return.
    //   b. if key exists in keyMap: update node.value, then touch(key) as above.
    //   c. else (new key):
    //        if keyMap.size() == capacity:
    //          evictKey = freqMap.get(minFreq).iterator().next()   // O(1) peek of LRU-within-min
    //          freqMap.get(minFreq).remove(evictKey)
    //          keyMap.remove(evictKey)
    //        create Node(value, freq=1); keyMap.put(key, node)
    //        freqMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key)
    //        minFreq = 1   // any newly inserted key starts the frequency count over at 1
    //
    // get(key):
    //   a. if key not in keyMap: return -1
    //   b. touch(key); return node.value

    // ================= WHY THESE DATA STRUCTURES =================
    // - HashMap<Key, Node> (keyMap): gives O(1) average lookup/insert/remove for "does this
    //   key exist and what is its value/frequency" -- the same role HashMap plays in a plain
    //   LRU cache.
    // - HashMap<Integer freq, LinkedHashSet<Key>> (freqMap): the HashMap gives O(1) average
    //   access to "the set of keys at frequency f". The LinkedHashSet inside each bucket is
    //   the key trick: it is backed by a hash table (O(1) add/remove/contains by key) AND a
    //   doubly linked list threading the entries in insertion order (O(1) access to the
    //   oldest/first entry via iterator, and O(1) reordering by remove+re-add). This combo is
    //   exactly analogous to using a HashMap + intrusive DoublyLinkedList in plain LRU, except
    //   Java's LinkedHashSet gives us that DLL behavior "for free" without hand-rolling node
    //   pointers.
    // - minFreq (plain int): eliminates the need to ever scan freqMap's keys to find the
    //   minimum. Because frequencies only ever increase by 1 per touch, and new keys always
    //   enter at frequency 1, minFreq can be maintained with simple increment/reset rules
    //   instead of a scan or a min-heap, keeping every operation O(1) average.
    // - Net result: get() and put() are O(1) average time, matching the interview requirement.

    // ================= EDGE CASES =================
    // - capacity == 0: cache can never hold anything; put() must be a no-op (do not insert,
    //   do not evict), get() always returns -1.
    // - get() on a missing key: return -1 without mutating any frequency structures.
    // - put() on an existing key: must UPDATE the value (not create a duplicate node) and
    //   still bump frequency exactly once, same as a get() would.
    // - Tie among multiple keys at the same minFreq: evict the least-recently-touched one,
    //   which is the first element in that frequency's LinkedHashSet (insertion order).
    // - capacity == 1: every put() of a new key evicts the sole existing entry immediately.
    // - Concurrent access: this implementation is NOT thread-safe (plain HashMaps, no locks).
    //   Concurrent get()/put() from multiple threads can corrupt freqMap/keyMap or produce
    //   inconsistent minFreq. See INTERVIEW FOLLOW-UPS for how to make it safe.

    // ================= COMPLEXITY =================
    // Time Complexity:
    //   get(key):  O(1) average -- one HashMap lookup (keyMap), one LinkedHashSet remove,
    //              one LinkedHashSet add, O(1) minFreq bookkeeping.
    //   put(key,value): O(1) average -- same bounded number of O(1) average operations,
    //              plus (on eviction) one O(1) LinkedHashSet.iterator().next() peek.
    // Space Complexity: O(capacity) -- keyMap holds at most `capacity` nodes; freqMap holds
    //   each of those `capacity` keys exactly once across all buckets combined.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you make this thread-safe? Wrap all mutating operations in a single
    //   ReentrantLock/synchronized block (simple, but serializes access), or shard the cache
    //   into N independently-locked LFU segments keyed by hash(key) % N for higher throughput
    //   (see ConcurrentLRUCache.java in this package for the sharding pattern applied to LRU).
    // - How does this scale to millions of entries / a distributed cache? A single JVM's
    //   heap bounds `capacity`; for millions of entries you'd typically move to an off-heap
    //   or distributed cache (Redis, Memcached, Caffeine with W-TinyLFU) and replicate/shard
    //   across nodes with consistent hashing, accepting eventual consistency of frequency
    //   counts across replicas.
    // - Why not use a min-heap (PriorityQueue) keyed by (freq, timestamp) instead of freqMap?
    //   A heap gives O(log n) per operation, not O(1); the frequency-bucket approach exploits
    //   the fact that frequency only increases by 1 at a time to avoid needing a heap at all.
    // - What happens to minFreq when the LFU cache is completely empty? It becomes stale/unused
    //   until the next put() of a new key resets it to 1 -- callers must never read minFreq
    //   when keyMap is empty.
    // - How would you support TTL/expiry on top of LFU? Combine with a lazy-expiry check
    //   (compare against a stored expiry timestamp on Node) similar to InMemoryTTLCache.java,
    //   removing from both keyMap and freqMap when an expired key is touched.
    // - Why LinkedHashSet and not a plain doubly linked list of nodes (as in classic LRU)?
    //   LinkedHashSet gives us O(1) removal BY KEY (needed when a key's frequency changes and
    //   it must leave its old bucket) in addition to O(1) ordered iteration -- a hand-rolled
    //   DLL would need an auxiliary map from key -> DLL node to get O(1) removal by key,
    //   which is essentially what LinkedHashSet already does internally.
    // - How would real production systems approximate LFU cheaply? Approximate LFU (e.g.
    //   TinyLFU/W-TinyLFU used by Caffeine) uses probabilistic counting (count-min sketch)
    //   instead of exact per-key frequency maps, trading exactness for much lower memory
    //   overhead at scale.
    // - What if two keys are inserted and never touched again -- which is evicted first?
    //   Whichever was inserted first, since both sit in the frequency-1 bucket and
    //   LinkedHashSet preserves insertion order (oldest first).

    /** Internal node holding a key's value and current access frequency. */
    private static class Node {
        final int key;
        int value;
        int freq;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
            this.freq = 1;
        }
    }

    private final int capacity;
    private int minFreq;
    private final Map<Integer, Node> keyMap;
    private final Map<Integer, LinkedHashSet<Integer>> freqMap;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.minFreq = 0;
        this.keyMap = new HashMap<>();
        this.freqMap = new HashMap<>();
    }

    public int get(int key) {
        Node node = keyMap.get(key);
        if (node == null) {
            return -1; // missing key
        }
        touch(node);
        return node.value;
    }

    public void put(int key, int value) {
        if (capacity <= 0) {
            return; // capacity 0 -> cache can never hold anything
        }
        Node existing = keyMap.get(key);
        if (existing != null) {
            existing.value = value; // update value on existing key
            touch(existing);
            return;
        }
        if (keyMap.size() == capacity) {
            evict();
        }
        Node fresh = new Node(key, value);
        keyMap.put(key, fresh);
        freqMap.computeIfAbsent(1, f -> new LinkedHashSet<>()).add(key);
        minFreq = 1; // any newly inserted key restarts the frequency floor at 1
    }

    /** Bumps a node's frequency by one, moving it between frequency buckets. */
    private void touch(Node node) {
        int oldFreq = node.freq;
        LinkedHashSet<Integer> oldBucket = freqMap.get(oldFreq);
        oldBucket.remove(node.key);
        if (oldBucket.isEmpty()) {
            freqMap.remove(oldFreq);
            if (oldFreq == minFreq) {
                minFreq++; // the min bucket just emptied out
            }
        }
        node.freq = oldFreq + 1;
        freqMap.computeIfAbsent(node.freq, f -> new LinkedHashSet<>()).add(node.key);
    }

    /** Evicts the least-frequently-used key, tie-broken by least-recently-used. */
    private void evict() {
        LinkedHashSet<Integer> minBucket = freqMap.get(minFreq);
        int evictKey = minBucket.iterator().next(); // first = least recently used at minFreq
        minBucket.remove(evictKey);
        if (minBucket.isEmpty()) {
            freqMap.remove(minFreq);
        }
        keyMap.remove(evictKey);
    }

    public static void main(String[] args) {
        // Example 1: basic frequency-based eviction with LRU tie-break
        LFUCache cache = new LFUCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        System.out.println(cache.get(1));   // Expected: 10 (freq(1) -> 2)
        cache.put(3, 30);                   // evicts key 2 (freq1, lowest)
        System.out.println(cache.get(2));   // Expected: -1 (evicted)
        System.out.println(cache.get(3));   // Expected: 30 (freq(3) -> 2)
        cache.put(4, 40);                   // freq(1)=2, freq(3)=2 tie -> evict key 1 (older)
        System.out.println(cache.get(1));   // Expected: -1 (evicted)
        System.out.println(cache.get(3));   // Expected: 30
        System.out.println(cache.get(4));   // Expected: 40

        // Example 2: put() on an existing key updates value and bumps frequency
        LFUCache cache2 = new LFUCache(2);
        cache2.put(1, 100);
        cache2.put(1, 200); // update existing key
        System.out.println(cache2.get(1));  // Expected: 200

        // Example 3: capacity 0 -> nothing is ever stored
        LFUCache zeroCap = new LFUCache(0);
        zeroCap.put(1, 1);
        System.out.println(zeroCap.get(1)); // Expected: -1 (capacity 0, nothing stored)

        // Example 4: get on missing key from a non-empty cache
        LFUCache cache3 = new LFUCache(1);
        cache3.put(9, 99);
        System.out.println(cache3.get(42)); // Expected: -1 (key never inserted)
    }
}
