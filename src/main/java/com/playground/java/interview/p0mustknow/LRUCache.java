package com.playground.java.interview.p0mustknow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATTERN: HashMap + Doubly Linked List (Design)
 * PRIORITY: P0 - Must Know (Very frequently asked)
 * PROBLEM STATEMENT: Design a fixed-capacity Least Recently Used (LRU) cache supporting get(key) and put(key, value) in O(1) time each.
 */
public class LRUCache {

    // ================= PROBLEM =================
    // Build a cache with a fixed capacity. It supports:
    //   get(key)   -> returns the value if key exists, else -1. Accessing a key marks it
    //                 as "most recently used".
    //   put(key,v) -> inserts or updates the key's value, marks it "most recently used".
    //                 If the cache is over capacity after inserting, evict the
    //                 LEAST recently used entry.
    // Both operations must run in O(1) time.
    // Example: capacity = 2
    //   put(1,1) -> cache: {1=1}
    //   put(2,2) -> cache: {1=1, 2=2}
    //   get(1)   -> returns 1, cache order (LRU->MRU): 2,1
    //   put(3,3) -> evicts key 2 (least recently used) -> cache: {1=1, 3=3}
    //   get(2)   -> returns -1 (evicted)
    //
    // ================= SIMPLE APPROACH =================
    // Use a plain HashMap<Integer, Integer> for storage, and keep a separate list
    // (e.g. ArrayList<Integer>) of keys in usage order to track which is least recently used.
    // On every get/put, remove the key from its current position in the list and add it
    // to the end (most recently used side). To evict, remove from the front of the list.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // Removing a key from the middle of an ArrayList (or even a plain LinkedList when you
    // only have the key, not a direct node reference) requires searching for it first,
    // which is O(n). So both get() and put() degrade to O(n) instead of the required O(1).
    // We need a structure that can (a) find any entry instantly AND (b) reorder/evict
    // entries instantly. Neither a HashMap alone nor a linked list alone can do both.
    //
    // ================= OPTIMIZED APPROACH =================
    // Combine two data structures:
    //   1) HashMap<Integer, Node> - maps a key directly to its Node object in O(1).
    //   2) Doubly Linked List of Node objects, ordered by recency: the head (right after a
    //      dummy head sentinel) is most-recently-used, and the tail (right before a dummy
    //      tail sentinel) is least-recently-used.
    // On get(key): look up the node in O(1) via the map, unlink it from its current spot in
    // O(1) (because it's a DOUBLY linked list, we know both its prev and next), and re-insert
    // it right after the dummy head (move-to-front), also O(1).
    // On put(key,val): if key exists, update its value and move it to front (same as get).
    // If it's new, create a Node, put it in the map, add it to the front. If size > capacity,
    // remove the node just before the dummy tail (the real least-recently-used node) both
    // from the list AND from the map.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // Why a plain HashMap alone is NOT enough:
    //   A HashMap gives O(1) get/put by key, but it has no concept of "order" or "recency".
    //   There is no O(1) way to ask a HashMap "which entry was used longest ago?" - you would
    //   have to scan all entries, which is O(n). We need ordering info that a HashMap simply
    //   does not track.
    //
    // Why a plain Doubly Linked List alone is NOT enough:
    //   A doubly linked list CAN maintain recency order perfectly (move a node to the front,
    //   remove the tail) in O(1) - but only if you already HAVE a direct reference to that
    //   node. Given just a "key", finding which node holds that key requires walking the list
    //   from the head, which is O(n). We need instant lookup by key, which a linked list alone
    //   cannot provide.
    //
    // Why the COMBINATION works:
    //   The HashMap<Integer, Node> gives O(1) "given a key, get me the exact Node object".
    //   The doubly linked list gives O(1) "given a Node object, remove it from wherever it is
    //   and re-insert it at the front" and O(1) "remove whatever is at the tail".
    //   Together: get(key) = map lookup (O(1)) + move node to front (O(1)) = O(1) overall.
    //   put(key,val) = map lookup/insert (O(1)) + move/insert node at front (O(1)) +
    //   possibly remove tail node from both list and map (O(1)) = O(1) overall.
    //   Each structure covers exactly the weakness of the other.
    //
    // Why it MUST be a DOUBLY linked list (not singly linked):
    //   To remove an arbitrary node in O(1), you must be able to re-link its NEIGHBORS:
    //   node.prev.next = node.next; node.next.prev = node.prev;
    //   This requires knowing both the previous node and the next node of the node being
    //   removed. A singly linked list only stores "next", not "prev" - so to remove a node
    //   you would first need to find its predecessor by walking from the head, which is O(n).
    //   Since get() needs to remove-and-reinsert an arbitrary node (not just the head or tail)
    //   in O(1), we need the "prev" pointer that only a doubly linked list provides.
    //
    // Why dummy head and dummy tail sentinel nodes:
    //   Without sentinels, adding/removing the very first or very last real node requires
    //   special null-checks (e.g. "if head == null" or "if this is the only node in the list").
    //   With permanent dummy head and dummy tail nodes that never move and never hold real
    //   data, every real node always has a valid prev and a valid next to link against.
    //   addToFront and removeNode become uniform, branch-free operations with no edge cases
    //   for "list is empty" or "removing the only node" - this removes a large class of bugs.
    //
    // ================= EDGE CASES =================
    // - capacity = 0: every put should immediately be a no-op / evict itself (no room for anything).
    // - get() on a missing key: return -1, do not modify the list.
    // - put() on an existing key: must update the value AND move it to most-recently-used,
    //   without incorrectly treating it as eviction-triggering (size does not grow).
    // - Repeated get() on the same key: should keep moving it to the front each time.
    // - Cache never reaches capacity: eviction logic should simply never trigger.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(1) for both get() and put() - each involves a constant number of
    // HashMap operations and a constant number of pointer re-links in the doubly linked list,
    // regardless of how many entries are in the cache.
    // Space Complexity: O(capacity) - the HashMap and the doubly linked list each hold at
    // most "capacity" real entries (plus two constant-size sentinel nodes).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why must the linked list be doubly linked and not singly linked? (removal needs prev+next)
    // - Why do we need dummy head/tail nodes instead of tracking head/tail references directly?
    // - How would you make this thread-safe for concurrent access?
    // - How would you implement an LFU (Least Frequently Used) cache instead - what changes?
    // - Could you implement this using Java's LinkedHashMap directly? What are the trade-offs
    //   of using a built-in vs. writing it by hand in an interview?
    // - How would you add a TTL (time-to-live) expiration on top of LRU eviction?
    // - What happens to Big-O if you used an ArrayList instead of a linked list for ordering?
    // - How would you size the cache by memory footprint instead of entry count?

    // Doubly linked list node holding a key/value pair.
    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> cache; // key -> Node, gives O(1) lookup
    private final Node head; // dummy head sentinel; head.next = most recently used
    private final Node tail; // dummy tail sentinel; tail.prev = least recently used

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(-1, -1); // sentinel, never holds real data
        this.tail = new Node(-1, -1); // sentinel, never holds real data
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1; // not present
        }
        moveToFront(node); // accessing a key marks it most-recently-used
        return node.value;
    }

    public void put(int key, int value) {
        Node existing = cache.get(key);
        if (existing != null) {
            // Key already present: update value and mark as most-recently-used.
            existing.value = value;
            moveToFront(existing);
            return;
        }

        // New key: create a node, insert at front, and register it in the map.
        Node newNode = new Node(key, value);
        cache.put(key, newNode);
        addToFront(newNode);

        // Over capacity: evict the least-recently-used node (just before dummy tail).
        if (cache.size() > capacity) {
            Node lru = removeLast();
            cache.remove(lru.key);
        }
    }

    // Insert a node right after the dummy head (most-recently-used position).
    private void addToFront(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    // Unlink a node from wherever it currently sits in the list. O(1) because we have
    // direct references to both its prev and next (this is why the list must be doubly linked).
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Move an existing node to the front (most-recently-used position).
    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }

    // Remove and return the node just before the dummy tail (least-recently-used).
    private Node removeLast() {
        Node lru = tail.prev;
        removeNode(lru);
        return lru;
    }

    // ================= SIMPLE ALTERNATIVE (interview mention only) =================
    // Java's LinkedHashMap already maintains insertion/access order internally using a
    // doubly linked list under the hood, and supports accessOrder=true (reorders on get)
    // plus removeEldestEntry() as an eviction hook. This gives a correct LRU cache in a
    // few lines - but most interviewers want the manual HashMap+DLL version above to
    // confirm you understand WHY it is O(1), not just that a library can do it for you.
    public static class SimpleLRUCache extends LinkedHashMap<Integer, Integer> {
        private final int capacity;

        public SimpleLRUCache(int capacity) {
            // initialCapacity, loadFactor (default), accessOrder=true reorders on get/put.
            super(16, 0.75f, true);
            this.capacity = capacity;
        }

        public int get(int key) {
            return super.getOrDefault(key, -1);
        }

        public void put(int key, int value) {
            super.put(key, value);
        }

        // Called automatically by LinkedHashMap after every put(); returning true evicts
        // the eldest (least recently used) entry.
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
            return size() > capacity;
        }
    }

    public static void main(String[] args) {
        System.out.println("Manual HashMap + Doubly Linked List LRUCache:");
        LRUCache lru = new LRUCache(2);
        lru.put(1, 1);
        lru.put(2, 2);
        // Expected: 1
        System.out.println("get(1) = " + lru.get(1) + " (expected 1)");
        lru.put(3, 3); // evicts key 2 (least recently used)
        // Expected: -1 (evicted)
        System.out.println("get(2) = " + lru.get(2) + " (expected -1, evicted)");
        lru.put(4, 4); // evicts key 1 (least recently used)
        // Expected: -1 (evicted)
        System.out.println("get(1) = " + lru.get(1) + " (expected -1, evicted)");
        // Expected: 3
        System.out.println("get(3) = " + lru.get(3) + " (expected 3)");
        // Expected: 4
        System.out.println("get(4) = " + lru.get(4) + " (expected 4)");

        System.out.println("\nEdge case: capacity = 0");
        LRUCache zeroCap = new LRUCache(0);
        zeroCap.put(1, 1);
        // Expected: -1 (never fit in the cache)
        System.out.println("get(1) = " + zeroCap.get(1) + " (expected -1)");

        System.out.println("\nSimpleLRUCache using LinkedHashMap (accessOrder=true):");
        SimpleLRUCache simple = new SimpleLRUCache(2);
        simple.put(1, 1);
        simple.put(2, 2);
        simple.get(1); // marks 1 as most recently used
        simple.put(3, 3); // evicts key 2
        // Expected: -1 (evicted)
        System.out.println("get(2) = " + simple.get(2) + " (expected -1, evicted)");
        // Expected: 1
        System.out.println("get(1) = " + simple.get(1) + " (expected 1)");
    }
}
