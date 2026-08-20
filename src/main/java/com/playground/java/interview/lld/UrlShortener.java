package com.playground.java.interview.lld;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PATTERN: Low-Level Design / OOP (Counter + Base62 Encoding)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Design a URL shortening service that converts long URLs into short,
 * unique codes and resolves those codes back to the original URLs.
 */
public class UrlShortener {

    // ================= PROBLEM / REQUIREMENTS =================
    // - Given a long URL, generate a short unique code (e.g. "aZ9kQ1").
    // - Given a short code, resolve back to the original long URL.
    // - Short codes must never collide - two different URLs must never map to the same code.
    // - The same long URL may be shortened multiple times (simplify: no dedup required, but
    //   could be added by keeping a reverse-lookup map from long URL -> existing code).
    // - System should support a very large number of URLs (billions) without running out
    //   of code space, so a compact, scalable encoding scheme is needed.
    // - (Bonus) Support optional expiry / custom aliases - not required for core problem.

    // ================= SIMPLE APPROACH =================
    // A single god-class holds a HashMap<String, String> (code -> longUrl) and generates
    // short codes using something like a random 6-character string or an MD5/SHA hash of
    // the URL truncated to a few characters. Every operation (generate, store, lookup,
    // collision check) lives directly in one class with no abstractions at all.

    // ================= WHY IT'S NOT ENOUGH =================
    // - Random strings or truncated hashes CAN collide, requiring retry loops with locking,
    //   which gets messy and slow under concurrent load.
    // - No clean separation between "ID generation strategy", "encoding scheme", and
    //   "storage" - swapping any one piece (e.g. moving storage to a real database, or
    //   swapping base62 for a hash-based scheme) means rewriting the whole class.
    // - Hard to extend for multi-server deployments where each server must generate IDs
    //   independently without colliding with other servers.
    // - No enforcement of a single responsibility - ID generation, encoding, and
    //   persistence are all tangled together, violating SRP and making unit testing hard.

    // ================= OPTIMIZED DESIGN =================
    // - IdGenerator (interface): abstracts "give me the next unique numeric ID". Default
    //   implementation AtomicCounterIdGenerator uses an AtomicLong (simulates an
    //   auto-incrementing DB sequence / centralized ID service). This is the seam where a
    //   distributed ID generator (see note below) would be plugged in.
    // - Base62Encoder (utility class): pure, stateless encode(long) / decode(String)
    //   between a numeric ID and a short alphanumeric string. Guarantees a 1:1, collision
    //   free mapping because it is a deterministic bijection over the ID space - no
    //   retries, no locking needed for uniqueness.
    // - UrlRepository (interface) + InMemoryUrlRepository: abstracts persistence
    //   (code -> longUrl, and reverse longUrl -> code for dedup). A real system would swap
    //   this for a database/cache-backed implementation without touching the rest.
    // - UrlShortenerService: orchestrates the three collaborators above - given a long URL,
    //   asks IdGenerator for the next ID, encodes it via Base62Encoder, stores the mapping
    //   via UrlRepository, and returns the short code. Given a code, decodes and looks up.
    // Design pattern: Strategy (IdGenerator and UrlRepository are pluggable strategies),
    // and the overall composition is a simple Facade (UrlShortenerService) over three
    // single-purpose collaborators.
    //
    // SCALING TO A REAL DISTRIBUTED SYSTEM (multiple app servers):
    // A single in-process AtomicLong only works on one JVM. In a real distributed
    // deployment with N stateless app servers behind a load balancer, two strategies are
    // common:
    //   1) Centralized ID-generation service: a dedicated service (e.g. a Snowflake-style
    //      generator, or a simple service backed by a DB sequence / Redis INCR) that every
    //      app server calls to obtain the next globally unique ID before encoding it.
    //      Pros: simple mental model, strictly increasing IDs. Cons: extra network hop,
    //      potential single point of contention (mitigate with sharded counters).
    //   2) Range-based ID allocation (ID block reservation): each app server, on startup
    //      or when it exhausts its current block, asks a coordinator (DB row with
    //      "next_available_start" updated atomically, or Zookeeper/etcd) to reserve a
    //      contiguous range of IDs (e.g. 1,000,000 IDs at a time). The server then hands
    //      out IDs from that local range using its own AtomicLong, with no further network
    //      calls until the range is exhausted. This trades a small amount of ID-space
    //      "waste" (unused IDs if a server crashes mid-range) for far less coordination
    //      overhead and much higher throughput. This is the approach used by Snowflake/
    //      Ticket Server style designs (e.g. Flickr's ticket servers, Instagram's ID gen).

    // ================= WHY THIS DESIGN =================
    // - Single Responsibility: ID generation, encoding, and storage are each isolated and
    //   independently testable.
    // - Open/Closed: swapping AtomicCounterIdGenerator for a distributed range-based
    //   generator, or InMemoryUrlRepository for a DB-backed one, requires zero changes to
    //   UrlShortenerService or Base62Encoder.
    // - Collision-free by construction: because Base62Encoder is a bijection over strictly
    //   increasing unique IDs, no collision-detection/retry logic is ever needed - a major
    //   simplification over hash-based schemes.
    // - Testability: Base62Encoder is pure and trivially unit-testable; IdGenerator and
    //   UrlRepository can be mocked via their interfaces.

    // ================= EDGE CASES =================
    // - Null/blank long URL input -> reject with IllegalArgumentException.
    // - Unknown/invalid short code on expand -> return Optional.empty() / throw a clear
    //   NoSuchElementException rather than a silent null.
    // - Same long URL shortened twice -> without dedup, produces two distinct valid codes
    //   (both resolve correctly); dedup can be added via the reverse map shown below.
    // - ID counter overflow (Long.MAX_VALUE) -> effectively unreachable at real-world scale
    //   (~9.2 quintillion IDs), but a production system would rotate to a wider ID space.
    // - Concurrent shorten() calls from multiple threads -> AtomicLong.incrementAndGet()
    //   guarantees each thread gets a distinct ID with no lost updates or duplicates.
    // - Case sensitivity of codes -> base62 alphabet is case-sensitive by design (0-9, a-z,
    //   A-Z), so codes must be compared/stored exactly as generated.

    // ================= COMPLEXITY =================
    // Time Complexity: shorten() = O(1) amortized (atomic increment + O(log62 N) encode,
    // effectively O(1) for N up to 62^~11 ~ fits in a long); expand() = O(1) map lookup.
    // Space Complexity: O(K) where K = number of shortened URLs stored (two map entries
    // per URL if dedup reverse-lookup is maintained).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you make ID generation work correctly across multiple app server
    //   instances without a shared AtomicLong? (centralized ID service vs. per-server
    //   range/block allocation, as discussed above.)
    // - How do you guarantee thread-safety for concurrent shorten()/expand() calls on a
    //   single instance? (AtomicLong for the counter, ConcurrentHashMap for storage -
    //   both used here; no explicit locks needed since neither requires compound
    //   check-then-act across the two structures.)
    // - How would you support custom aliases (user-chosen short codes) alongside
    //   auto-generated ones? (Reserve a namespace, or check-and-reserve in the repository
    //   with a putIfAbsent, rejecting if taken.)
    // - How would you add expiring links (TTL)? (Store an expiry timestamp alongside the
    //   mapping; check it on expand() and treat expired entries as not-found; a background
    //   sweeper or lazy deletion could reclaim storage.)
    // - How would you prevent abuse (shortening malicious URLs) or add analytics (click
    //   counts)? (Add a URL validation/allow-blacklist step before storing, and a
    //   click-count field incremented atomically on each expand().)
    // - Why base62 instead of base64? (base64 includes '+' and '/' which are not URL-safe
    //   without extra encoding; base62 (0-9a-zA-Z) is safe to embed directly in a URL path.)
    // - How would you persist this for durability across restarts? (Swap
    //   InMemoryUrlRepository for a database-backed implementation; the counter itself
    //   would need to be backed by a DB sequence or the range-allocation scheme so it
    //   survives restarts without reusing IDs.)
    // - How would you shard storage once the dataset outgrows a single database? (Shard by
    //   short code hash or by ID range, since the code/ID is already the natural partition
    //   key.)

    /** Encodes/decodes between a non-negative long ID and a base62 short code. Stateless and pure. */
    static final class Base62Encoder {
        private static final String ALPHABET =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final int BASE = ALPHABET.length();

        String encode(long id) {
            if (id == 0) {
                return String.valueOf(ALPHABET.charAt(0));
            }
            StringBuilder sb = new StringBuilder();
            long value = id;
            // Repeatedly take the remainder mod 62 as the next base62 digit, then divide down.
            while (value > 0) {
                int remainder = (int) (value % BASE);
                sb.append(ALPHABET.charAt(remainder));
                value /= BASE;
            }
            return sb.reverse().toString();
        }

        long decode(String code) {
            long value = 0;
            for (int i = 0; i < code.length(); i++) {
                value = value * BASE + ALPHABET.indexOf(code.charAt(i));
            }
            return value;
        }
    }

    /** Strategy interface for obtaining the next globally unique numeric ID. */
    interface IdGenerator {
        long nextId();
    }

    /**
     * Single-JVM implementation backed by an AtomicLong (stands in for a DB auto-increment
     * sequence). In a distributed deployment, replace with a centralized ID service call or
     * a per-server range-allocator (see OPTIMIZED DESIGN note above).
     */
    static final class AtomicCounterIdGenerator implements IdGenerator {
        private final AtomicLong counter = new AtomicLong(0);

        @Override
        public long nextId() {
            return counter.incrementAndGet();
        }
    }

    /** Storage abstraction so persistence can be swapped without touching the service. */
    interface UrlRepository {
        void save(String code, String longUrl);
        String findLongUrl(String code);
    }

    /** Thread-safe in-memory repository; swap for a DB-backed implementation in production. */
    static final class InMemoryUrlRepository implements UrlRepository {
        private final Map<String, String> codeToLongUrl = new ConcurrentHashMap<>();

        @Override
        public void save(String code, String longUrl) {
            codeToLongUrl.put(code, longUrl);
        }

        @Override
        public String findLongUrl(String code) {
            return codeToLongUrl.get(code);
        }
    }

    /** Facade orchestrating ID generation, encoding, and storage. */
    static final class UrlShortenerService {
        private final IdGenerator idGenerator;
        private final Base62Encoder encoder;
        private final UrlRepository repository;

        UrlShortenerService(IdGenerator idGenerator, Base62Encoder encoder, UrlRepository repository) {
            this.idGenerator = idGenerator;
            this.encoder = encoder;
            this.repository = repository;
        }

        String shorten(String longUrl) {
            if (longUrl == null || longUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("longUrl must not be null or blank");
            }
            long id = idGenerator.nextId();
            String code = encoder.encode(id);
            repository.save(code, longUrl);
            return code;
        }

        String expand(String code) {
            String longUrl = repository.findLongUrl(code);
            if (longUrl == null) {
                throw new java.util.NoSuchElementException("No URL found for short code: " + code);
            }
            return longUrl;
        }
    }

    public static void main(String[] args) {
        UrlShortenerService service = new UrlShortenerService(
                new AtomicCounterIdGenerator(), new Base62Encoder(), new InMemoryUrlRepository());

        // Operation 1: shorten a long URL -> ID 1 encodes to base62 "1".
        String code1 = service.shorten("https://www.marriott.com/hotels/reservation");
        System.out.println("Short code for URL 1: " + code1); // Expected: Short code for URL 1: 1

        // Operation 2: shorten a second, different URL -> ID 2 encodes to base62 "2".
        String code2 = service.shorten("https://www.marriott.com/loyalty/bonvoy");
        System.out.println("Short code for URL 2: " + code2); // Expected: Short code for URL 2: 2

        // Operation 3: expand code1 back to its original URL.
        System.out.println("Expanded code1: " + service.expand(code1));
        // Expected: Expanded code1: https://www.marriott.com/hotels/reservation

        // Operation 4: expand code2 back to its original URL.
        System.out.println("Expanded code2: " + service.expand(code2));
        // Expected: Expanded code2: https://www.marriott.com/loyalty/bonvoy

        // Operation 5: attempt to expand an unknown code -> demonstrates edge-case handling.
        try {
            service.expand("doesNotExist");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Lookup failed as expected: " + e.getMessage());
            // Expected: Lookup failed as expected: No URL found for short code: doesNotExist
        }
    }
}
