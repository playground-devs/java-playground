package com.playground.java.interview.cache;

import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Sliding Window Counter (weighted average of current + previous fixed windows)
 * PRIORITY: P2 - Good to Know
 * PROBLEM STATEMENT: Approximate a true sliding-window rate limit cheaply by blending counts
 * from the current and previous fixed time windows, avoiding the fixed-window boundary burst.
 */
public class RateLimiterSlidingWindowCounter {

    // ================= PROBLEM =================
    // Implement a rate limiter allowing at most `limit` requests per rolling `windowSizeMillis`
    // window, WITHOUT the boundary-burst flaw of a naive fixed-window counter, and without the
    // O(limit) memory cost of storing every individual request timestamp (true sliding log).
    //
    //   - allowRequest(): returns true if the ESTIMATED number of requests in the trailing
    //     window (now - windowSizeMillis, now] is still under `limit`, else false.
    //
    // Concrete example of the problem this solves (limit = 100 requests/minute):
    //   Naive fixed window: windows are [0:00,1:00), [1:00,2:00), etc.
    //     - 100 requests arrive at 0:59 -> all 100 counted in window [0:00,1:00) -> allowed.
    //     - 100 more requests arrive at 1:01 -> counted in a FRESH window [1:00,2:00), whose
    //       counter just reset to 0 -> all 100 allowed again.
    //     - Result: 200 requests passed within a 2-SECOND span (0:59 to 1:01), even though
    //       the configured limit was 100 per 60 seconds -- a naive fixed counter has no memory
    //       of the previous window and so cannot see this burst at all.
    //   Sliding window counter fixes this by blending the previous window's count (weighted
    //   by how much of it still overlaps the trailing 60-second lookback) with the current
    //   window's count, so the estimate at 1:01 correctly reflects that most of the previous
    //   window's 100 requests are still "recent" and should count against the limit.

    // ================= SIMPLE APPROACH =================
    // Fixed window counter: one counter per aligned windowSizeMillis-wide bucket, incremented
    // on every request and reset to 0 whenever the current time rolls into a new window.

    // ================= WHY IT'S NOT ENOUGH =================
    // As shown above, a fixed window counter allows up to 2x the intended limit right at a
    // window boundary because it discards all memory of the previous window the instant the
    // clock ticks over -- it is blind to how recently those previous-window requests actually
    // happened relative to "now". A TRUE sliding window (storing every request's exact
    // timestamp in a queue/log and evicting ones older than windowSizeMillis on each check)
    // fixes the accuracy problem perfectly, but costs O(limit) memory per limited entity
    // (e.g. per user) and O(limit) time to evict stale timestamps on every single call --
    // expensive at scale (millions of users x high request limits).

    // ================= OPTIMIZED APPROACH =================
    // Sliding Window COUNTER approximates the true sliding log cheaply using just two integer
    // counters (previous window count, current window count) instead of a full timestamp log:
    //
    //   - windowSizeMillis: width of each fixed window (e.g. 60_000 for per-minute limiting).
    //   - currentWindowStart: the aligned start timestamp of the window `now` falls into,
    //     computed as (now / windowSizeMillis) * windowSizeMillis.
    //   - currentWindowCount, previousWindowCount: request counts in those two windows.
    //
    // Step-by-step for allowRequest():
    //   a. now = System.currentTimeMillis(); windowStart = floor(now / windowSizeMillis) * windowSizeMillis.
    //   b. If windowStart != currentWindowStart, the clock has rolled into a new fixed window:
    //        - If windowStart == currentWindowStart + windowSizeMillis (the very next window,
    //          i.e. no window was skipped): previousWindowCount = currentWindowCount (the
    //          window that just ended becomes "previous").
    //        - Else (more than one window elapsed with no traffic, e.g. a long idle gap):
    //          previousWindowCount = 0 (nothing relevant survived that far back).
    //        - currentWindowCount = 0; currentWindowStart = windowStart.
    //   c. elapsedIntoCurrentWindow = now - currentWindowStart.
    //      overlapFractionOfPrevious = (windowSizeMillis - elapsedIntoCurrentWindow) / windowSizeMillis
    //        // e.g. if we are 10% into the current window, 90% of the previous window's
    //        // requests are still considered "within" the trailing windowSizeMillis lookback.
    //   d. estimatedCount = previousWindowCount * overlapFractionOfPrevious + currentWindowCount
    //   e. if estimatedCount < limit: currentWindowCount++; return true.
    //      else: return false.
    //
    // Worked check against the example above (limit=100, windowSizeMillis=60_000):
    //   At t=0:59 (59_000ms into window [0,60_000)): 100 requests recorded ->
    //     currentWindowCount = 100 for window [0,60000).
    //   At t=1:01 (61_000ms, now in window [60000,120000)): window rolls over ->
    //     previousWindowCount = 100 (from the window that just ended), currentWindowCount = 0.
    //     elapsedIntoCurrentWindow = 1_000ms -> overlapFractionOfPrevious = (60000-1000)/60000 ~ 0.983.
    //     estimatedCount = 100 * 0.983 + 0 (before adding the new request) ~ 98.3.
    //     A 101st request at t=1:01 would see estimatedCount ~98.3 < 100 -> STILL allowed once
    //     more, but critically the counter now correctly reflects that ~98 of the prior
    //     window's requests are "still recent" -- so only a couple more requests are allowed
    //     before hitting the limit, instead of a full fresh 100-request allowance like the
    //     naive fixed window gives. This is the "smoothing" the algorithm provides: it is an
    //     approximation of the true sliding log, not a perfect guarantee, but it closes the
    //     2x-burst gap down to a small, bounded overestimate near boundaries.

    // ================= WHY THESE DATA STRUCTURES =================
    // - Just two `long`/`int` counters (previousWindowCount, currentWindowCount) plus one
    //   timestamp (currentWindowStart) -- no timestamp log, no queue, no per-request storage
    //   at all. This is the entire point of the "counter" variant vs. the "true sliding log"
    //   variant: it trades a small amount of accuracy (a linear-interpolation ASSUMPTION that
    //   requests were evenly distributed within the previous window) for O(1) space and O(1)
    //   time per check, regardless of how large `limit` is.
    // - ReentrantLock guarding window-rollover + count/read: window rollover (resetting
    //   currentWindowCount/previousWindowCount and advancing currentWindowStart) and the
    //   count increment must be applied as one atomic unit; otherwise two threads racing at a
    //   window boundary could both decide "this is a new window" and each reset the counters
    //   independently, undercounting real concurrent traffic.
    // - Compare with true sliding log (would use a Deque<Long> of timestamps): that structure
    //   gives exact correctness but costs O(k) memory and O(k) eviction/scan time where k can
    //   be up to `limit`; the counter approach here is chosen specifically because interview
    //   discussions of scaling rate limiters to millions of users care about O(1) memory per
    //   limited entity, not exactness to the millisecond.

    // ================= EDGE CASES =================
    // - limit == 0: estimatedCount (even 0) is never < 0, so allowRequest() always returns
    //   false (0 < 0 is false).
    // - First-ever call (no windows established yet): currentWindowStart initialized to the
    //   window containing construction time; previousWindowCount starts at 0, contributing
    //   nothing to the estimate, which is correct (no prior traffic exists).
    // - Long idle gap spanning multiple windows: handled explicitly by resetting
    //   previousWindowCount to 0 when the new window is NOT immediately adjacent to the old
    //   one, since a window from long ago has zero overlap with the current trailing lookback.
    // - Expiry exactly at the window boundary: `now` landing exactly on a multiple of
    //   windowSizeMillis computes elapsedIntoCurrentWindow = 0, giving
    //   overlapFractionOfPrevious = 1.0 -- i.e. at the very first instant of a new window, the
    //   ENTIRE previous window's count is still weighted in, which is the correct limiting
    //   case (matches intuition: 0ms into the new window, the trailing 1-window lookback is
    //   almost entirely the previous window).
    // - Concurrent access: guarded by a single ReentrantLock around the read-check-increment
    //   sequence, so no two threads can both pass a check when only one slot remained.
    // - Known approximation caveat (worth stating out loud in an interview): this algorithm
    //   assumes uniform request distribution within the previous window: if all of the
    //   previous window's requests were actually clustered in its last millisecond (worst
    //   case), the estimate can still modestly overcount or undercount the TRUE sliding
    //   window value -- it bounds the fixed-window boundary problem but does not eliminate
    //   approximation error entirely; only a true sliding log is exact.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) per allowRequest() call -- a fixed number of arithmetic
    //   operations and comparisons under the lock, regardless of `limit` or elapsed time.
    // Space Complexity: O(1) per limiter instance -- three fixed fields, independent of
    //   `limit` or request volume (contrast with O(limit) for a true sliding-log limiter).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Concretely, what specific flaw does this fix vs. a naive fixed-window counter, and
    //   with a number? A fixed window can admit up to 2x `limit` requests within a span as
    //   short as just over 0 seconds if they straddle a window boundary just right (100 at
    //   0:59 + 100 at 1:01 = 200 in ~2 seconds vs. an intended 100/60s); the sliding window
    //   counter bounds this by weighting the previous window's contribution down as the
    //   current window progresses, so the overcounting shrinks continuously to 0 by the time
    //   a full window has elapsed, rather than resetting instantly to 0 at the boundary.
    // - Is this exact or approximate, and why does that matter? Approximate -- it assumes
    //   uniform distribution of requests within the previous window. This matters because an
    //   adversarial client could still theoretically exploit non-uniform clustering for a
    //   small overcount, but in practice it's a good trade for O(1) space vs. a true sliding
    //   log, which is the standard justification interviewers look for.
    // - How would you make this thread-safe, precisely? A ReentrantLock (or synchronized
    //   block) around the "detect window rollover, compute estimate, conditionally
    //   increment" sequence, since it's a compound read-modify-write on shared mutable state
    //   (previousWindowCount, currentWindowCount, currentWindowStart) that must appear atomic.
    // - How would you scale this to millions of rate-limited keys (e.g. per-user or per-IP) /
    //   a distributed system with multiple app instances? Store (previousWindowCount,
    //   currentWindowCount, windowStart) per key in Redis using INCR + EXPIRE or a Lua script
    //   for atomicity, so all app instances share one counter per key instead of each
    //   instance enforcing an independent limit; combine with sharding/consistent hashing of
    //   keys across Redis nodes for horizontal scale.
    // - How does this compare to Token Bucket (RateLimiterTokenBucket.java in this package)?
    //   Token bucket explicitly ALLOWS bursts up to a configured capacity by design; sliding
    //   window counter tries to enforce a smoother, more evenly-distributed rate over each
    //   rolling window and is more naturally expressed as "N requests per fixed period"
    //   (e.g. API quotas), which product/business requirements often specify directly.
    // - What happens if `windowSizeMillis` is very small (e.g. 10ms) under high-resolution
    //   timing? Clock resolution and lock contention overhead per call can dominate; very
    //   short windows are better served by Token Bucket's continuous refill model.
    // - How would you extend this to log/observe WHY a request was rejected (for
    //   debuggability)? Return a richer result type (e.g. an enum or a small record) carrying
    //   the estimatedCount and limit at rejection time instead of a bare boolean.
    // - What's a scenario where the uniform-distribution assumption clearly breaks down?
    //   A client that always sends all of its traffic in the FIRST millisecond of every
    //   window then goes silent -- the algorithm would underestimate how "front-loaded"
    //   (and thus how close to violating a true sliding window) that traffic actually is
    //   relative to assuming even spread.

    private final long windowSizeMillis;
    private final long limit;
    private long currentWindowStart;
    private long currentWindowCount;
    private long previousWindowCount;
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiterSlidingWindowCounter(long limit, long windowSizeMillis) {
        this.limit = limit;
        this.windowSizeMillis = windowSizeMillis;
        long now = System.currentTimeMillis();
        this.currentWindowStart = alignToWindow(now);
        this.currentWindowCount = 0;
        this.previousWindowCount = 0;
    }

    private long alignToWindow(long timestampMillis) {
        return (timestampMillis / windowSizeMillis) * windowSizeMillis;
    }

    /** Returns true if the request is allowed under the weighted sliding-window estimate. */
    public boolean allowRequest() {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            long windowStart = alignToWindow(now);

            if (windowStart != currentWindowStart) {
                // Rolled into a new fixed window (possibly skipping windows during idle gaps).
                boolean isImmediatelyAdjacentWindow = windowStart == currentWindowStart + windowSizeMillis;
                previousWindowCount = isImmediatelyAdjacentWindow ? currentWindowCount : 0;
                currentWindowCount = 0;
                currentWindowStart = windowStart;
            }

            long elapsedIntoCurrentWindow = now - currentWindowStart;
            double overlapFractionOfPrevious =
                    (windowSizeMillis - elapsedIntoCurrentWindow) / (double) windowSizeMillis;
            double estimatedCount = previousWindowCount * overlapFractionOfPrevious + currentWindowCount;

            if (estimatedCount < limit) {
                currentWindowCount++;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Example 1: within-limit requests in a single window all succeed
        RateLimiterSlidingWindowCounter limiter = new RateLimiterSlidingWindowCounter(5, 1000); // 5 req/sec
        for (int i = 1; i <= 5; i++) {
            System.out.println("request " + i + ": " + limiter.allowRequest()); // Expected: true (x5)
        }
        System.out.println("request 6 (limit reached): " + limiter.allowRequest()); // Expected: false

        // Example 2: after the full window elapses, previous count's weight decays and
        // fresh capacity becomes available again (smoothing, not a hard reset like fixed window)
        Thread.sleep(1050);
        System.out.println("after full window elapsed: " + limiter.allowRequest()); // Expected: true

        // Example 3: limit 0 -> always rejected
        RateLimiterSlidingWindowCounter zeroLimit = new RateLimiterSlidingWindowCounter(0, 1000);
        System.out.println("limit 0 request: " + zeroLimit.allowRequest()); // Expected: false

        // Example 4: demonstrate smoothing vs. the naive fixed-window boundary burst.
        // Fill the limit near the end of a window, then immediately request again just after
        // the window rolls over -- a naive fixed counter would allow a fresh full burst here;
        // the sliding window counter still weighs in most of the previous window's count.
        RateLimiterSlidingWindowCounter boundaryDemo = new RateLimiterSlidingWindowCounter(3, 500);
        for (int i = 1; i <= 3; i++) {
            boundaryDemo.allowRequest(); // fill the limit for the current window
        }
        Thread.sleep(510); // cross into the next window almost immediately
        System.out.println("just after window rollover (previous count still weighted heavily): "
                + boundaryDemo.allowRequest()); // Expected: true (small residual capacity from decay, not a full reset)
        System.out.println("second request right after rollover: "
                + boundaryDemo.allowRequest()); // Expected: false (previous window's weight still blocks a full fresh burst)
    }
}
