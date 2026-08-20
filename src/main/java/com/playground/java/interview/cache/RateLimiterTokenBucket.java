package com.playground.java.interview.cache;

import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Token Bucket (lazy refill based on elapsed time)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Rate-limit requests by draining tokens from a fixed-capacity bucket
 * that refills at a steady rate, allowing short bursts up to the bucket's capacity.
 */
public class RateLimiterTokenBucket {

    // ================= PROBLEM =================
    // Implement a rate limiter that allows at most `capacity` tokens to be consumed in a
    // burst, and otherwise sustains a steady throughput of `refillRatePerSecond` tokens/sec.
    //
    //   - allowRequest(): returns true and consumes one token if a token is available,
    //     else returns false (request is rejected/throttled).
    //   - The bucket refills continuously over time at refillRatePerSecond, up to capacity.
    //
    // Concrete example (capacity = 5, refillRate = 5 tokens/sec):
    //   t=0.0s: bucket starts full with 5 tokens.
    //   5 rapid calls to allowRequest() at t=0.0s -> all 5 succeed (burst absorbed), bucket now 0.
    //   6th call immediately after -> false (no tokens left).
    //   Wait 200ms (t=0.2s) -> bucket has refilled 5 tokens/sec * 0.2s = 1 token.
    //   allowRequest() -> true (consumes that 1 token), bucket back to 0.

    // ================= SIMPLE APPROACH =================
    // A naive "fixed window counter": keep a count of requests in the current 1-second
    // window; reset the count to 0 every time the wall-clock second changes; reject once
    // count exceeds the limit for that window.

    // ================= WHY IT'S NOT ENOUGH =================
    // The fixed window counter has a boundary-burst problem: it allows up to 2x the intended
    // rate right at a window edge. E.g. with limit=100/min, 100 requests can land at 0:59
    // (all counted in window [0:00-1:00)) and another 100 at 1:01 (all counted in window
    // [1:00-2:00)) -- that's 200 requests within a 2-second span, even though the configured
    // limit was only 100 per minute. A naive fixed counter cannot smooth this because it
    // has zero memory of what happened in the previous window. (This exact problem is what
    // motivates the Sliding Window Counter design in RateLimiterSlidingWindowCounter.java.)
    // Token bucket instead models capacity as a continuously-refilling resource, not a
    // window-aligned counter, so it has no artificial window-edge cliff -- and, unlike a
    // naive counter, it explicitly ALLOWS intentional short bursts up to `capacity`, which
    // is often exactly the desired behavior (e.g. letting a user briefly burst above their
    // sustained rate without penalty, then throttling smoothly back to the steady rate).

    // ================= OPTIMIZED APPROACH =================
    // Model the bucket lazily -- no background thread needed. Store only:
    //   - capacity: max tokens the bucket can hold.
    //   - refillRatePerSecond: tokens added per second (a double, may be fractional/sec).
    //   - availableTokens: current token count (double, so fractional refill accumulates
    //     precisely instead of being lost to integer truncation on tiny/rapid calls).
    //   - lastRefillTimestampNanos: wall-clock time of the last refill calculation.
    //
    // Step-by-step for allowRequest():
    //   a. Acquire lock (guard shared mutable state against concurrent callers).
    //   b. refill(): compute elapsedSeconds = (now - lastRefillTimestampNanos) / 1e9.
    //      tokensToAdd = elapsedSeconds * refillRatePerSecond.
    //      availableTokens = min(capacity, availableTokens + tokensToAdd).
    //      lastRefillTimestampNanos = now.
    //   c. if availableTokens >= 1: availableTokens -= 1; return true.
    //      else: return false.
    //   d. Release lock.
    //
    // Refilling lazily (computed on-demand at each call, instead of via a ticking background
    // thread) avoids wasted CPU/timer overhead when the limiter is idle, and is exact
    // because it's driven by actual elapsed wall-clock time rather than a fixed tick period.

    // ================= WHY THESE DATA STRUCTURES =================
    // - No collection is needed at all -- just a few primitive fields (capacity,
    //   refillRatePerSecond, availableTokens as a double, lastRefillTimestampNanos as a long)
    //   protected by a single ReentrantLock. The entire state of the algorithm is these four
    //   numbers; there is nothing to look up or index, so O(1) time and O(1) space are
    //   trivially achieved by construction, not by choosing a clever data structure.
    // - ReentrantLock (vs `synchronized`): chosen here for explicitness and because it composes
    //   well if this class were later extended with tryLock()/timed acquisition (e.g. to fail
    //   fast under contention instead of blocking indefinitely) -- for this simple case a
    //   `synchronized` block would work equally well; the key point for interviews is that
    //   SOME mutual-exclusion mechanism is required because refill-then-decrement is a
    //   read-modify-write on shared state that is not atomic on its own.
    // - Using a double for availableTokens (not an int/long) matters: with a
    //   sub-1-token-per-call refill rate (e.g. 5 tokens/sec means each millisecond only
    //   refills 0.005 tokens), truncating to an integer after every refill would silently
    //   round tiny fractional refills down to zero forever, making the bucket appear to
    //   never refill under frequent polling. Storing the fractional remainder preserves
    //   precision across many small time slices.

    // ================= EDGE CASES =================
    // - capacity == 0: bucket can never hold a token; allowRequest() always returns false.
    // - Burst immediately at startup: bucket starts FULL (availableTokens = capacity), so the
    //   very first `capacity` calls succeed back-to-back before any throttling kicks in --
    //   this is intentional token-bucket behavior, not a bug.
    // - Requesting more than one token at once (not modeled here but a common follow-up):
    //   would need allowRequest(int tokensRequested) that checks availableTokens >= tokensRequested.
    // - Rapid repeated calls with no elapsed time between them: elapsedSeconds ~ 0, so
    //   tokensToAdd ~ 0 -- correctly refills almost nothing, preventing a caller from
    //   "free-refilling" the bucket by just calling allowRequest() in a tight loop.
    // - Refill exactly reaching capacity boundary: availableTokens is clamped with
    //   Math.min(capacity, ...), so it can never exceed capacity even after a long idle period.
    // - Concurrent access: multiple threads calling allowRequest() simultaneously must not
    //   both read the same availableTokens value and both decrement from it (double-spend of
    //   a single token) -- the lock around refill+decrement makes the whole operation atomic.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) per allowRequest() call -- a fixed number of arithmetic
    //   operations under the lock, regardless of how much time has elapsed or how many
    //   tokens are involved.
    // Space Complexity: O(1) -- four fixed-size fields per limiter instance, independent of
    //   request volume or elapsed time.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How is this thread-safe, precisely? The ReentrantLock makes "read availableTokens,
    //   compute refill, write availableTokens, conditionally decrement" one atomic critical
    //   section, preventing two threads from both observing 1 available token and both
    //   successfully consuming it (which would let 2 requests through when only 1 token
    //   existed).
    // - How would you scale a token bucket across a distributed system / multiple app
    //   servers? Centralize the bucket's state in Redis (e.g. using the Redis-native
    //   `CL.THROTTLE` from RedisCell, or a Lua script doing the refill+decrement atomically
    //   via EVAL) so all instances share one logical bucket per rate-limited key, instead of
    //   each JVM enforcing its own independent limit (which would effectively multiply the
    //   allowed rate by the number of instances).
    // - Why allow bursts at all -- isn't that against the point of rate limiting? Token
    //   bucket intentionally decouples "protect the backend from sustained overload" (the
    //   refill rate) from "tolerate short legitimate spikes" (the capacity) -- e.g. a user
    //   loading a page that fires 5 API calls at once shouldn't be throttled just because
    //   they're momentarily simultaneous, as long as the long-run average stays under the
    //   refill rate.
    // - How would you rate-limit per-user instead of globally? Maintain a
    //   ConcurrentHashMap<UserId, RateLimiterTokenBucket> and look up/create the bucket for
    //   each user, being careful about unbounded map growth for very many distinct users
    //   (e.g. combine with a TTL eviction like InMemoryTTLCache.java for idle users' buckets).
    // - What's the difference between Token Bucket and Leaky Bucket? Leaky Bucket enforces a
    //   strictly constant output rate (like a queue draining at a fixed rate, smoothing
    //   bursts away entirely), whereas Token Bucket permits bursts up to capacity as long as
    //   tokens have accumulated -- Token Bucket is generally preferred for APIs since it
    //   doesn't penalize a legitimately bursty-but-overall-compliant client.
    // - How would you test this deterministically without relying on real sleeps? Inject a
    //   time-supplier (e.g. a `LongSupplier nanoClock`) instead of calling System.nanoTime()
    //   directly, so unit tests can simulate elapsed time without actually waiting.
    // - What happens under extremely high contention (thousands of threads hammering one
    //   limiter)? A single lock becomes a bottleneck; consider sharding capacity across
    //   several sub-buckets (similar in spirit to the LRU sharding trade-off described in
    //   ConcurrentLRUCache.java) or using lock-free CAS loops on a packed (tokens,timestamp)
    //   state word for higher throughput.
    // - How would you support fractional-token costs (e.g. an expensive endpoint costs 5
    //   tokens per call)? Change allowRequest() to allowRequest(double cost) and check
    //   availableTokens >= cost before subtracting cost instead of a hardcoded 1.

    private final long capacity;
    private final double refillRatePerSecond;
    private double availableTokens;
    private long lastRefillTimestampNanos;
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiterTokenBucket(long capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.availableTokens = capacity; // bucket starts full
        this.lastRefillTimestampNanos = System.nanoTime();
    }

    /** Attempts to consume one token; returns true if allowed, false if throttled. */
    public boolean allowRequest() {
        lock.lock();
        try {
            refill();
            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /** Adds tokens proportional to elapsed time since the last refill, capped at capacity. */
    private void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillTimestampNanos) / 1_000_000_000.0;
        if (elapsedSeconds > 0) {
            double tokensToAdd = elapsedSeconds * refillRatePerSecond;
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTimestampNanos = now;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Example 1: burst up to capacity succeeds, then throttles
        RateLimiterTokenBucket limiter = new RateLimiterTokenBucket(5, 5.0); // capacity=5, 5 tokens/sec
        for (int i = 1; i <= 5; i++) {
            System.out.println("request " + i + ": " + limiter.allowRequest()); // Expected: true (x5)
        }
        System.out.println("request 6 (no tokens left): " + limiter.allowRequest()); // Expected: false

        // Example 2: waiting allows partial refill (short burst tolerated, steady rate enforced)
        Thread.sleep(220); // ~1.1 tokens refilled at 5/sec
        System.out.println("after 220ms wait: " + limiter.allowRequest()); // Expected: true
        System.out.println("immediately again : " + limiter.allowRequest()); // Expected: false (no token left)

        // Example 3: capacity 0 -> always rejected
        RateLimiterTokenBucket zeroCap = new RateLimiterTokenBucket(0, 10.0);
        System.out.println("capacity 0 request: " + zeroCap.allowRequest()); // Expected: false

        // Example 4: after a long idle period, bucket refills back up to (but not beyond) capacity
        Thread.sleep(2000); // far more than enough to fully refill a capacity-5 bucket at 5/sec
        int allowedCount = 0;
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest()) {
                allowedCount++;
            }
        }
        System.out.println("allowed after long idle (expect capped at capacity=5): " + allowedCount); // Expected: 5
    }
}
