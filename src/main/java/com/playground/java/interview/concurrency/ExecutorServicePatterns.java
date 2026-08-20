package com.playground.java.interview.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * PATTERN: Concurrency / Task Execution &amp; Thread Pool Management
 * PRIORITY: P0 - Must Know
 * TOPIC: Choosing the right ExecutorService flavor, submitting Callable tasks and collecting
 * Future results, and shutting a pool down correctly.
 *
 * ================= PROBLEM =================
 * A batch job needs to fetch pricing data for 100 SKUs from a downstream service concurrently
 * (bounded parallelism, so we don't open 100 sockets at once), a background maintenance task
 * needs to run every 30 seconds forever, and a set of short-lived ad hoc tasks needs to run
 * without a fixed pool size limit. Manually creating and managing raw `Thread` objects for all of
 * this is error-prone and doesn't scale as a pattern.
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate might spin up a raw `Thread` per task:
 *
 *   for (Sku sku : skus) {
 *       new Thread(() -> fetchPrice(sku)).start();
 *   }
 *
 * Why it's broken (not a data race this time, but still a real production bug class): with 100
 * SKUs this creates 100 OS threads simultaneously with NO bound on concurrency -- this can
 * exhaust memory (each thread reserves stack space, often ~512KB-1MB), overwhelm the downstream
 * service, or exceed OS thread limits under load. There's also no easy way to collect each task's
 * RESULT (raw Thread's `run()` returns void), no structured way to wait for "all done," and no
 * shutdown/cancellation story -- if the JVM needs to shut down cleanly, these threads have no
 * coordinated lifecycle.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Use the right `ExecutorService` for each shape of workload:
 *   1. `Executors.newFixedThreadPool(n)`: a bounded pool of exactly n worker threads backed by an
 *      unbounded work queue -- right for CPU/IO-bound workloads with a known, deliberate
 *      concurrency cap (our 100-SKU fetch, capped at e.g. 10 concurrent requests).
 *   2. `Executors.newCachedThreadPool()`: creates threads on demand, reuses idle ones, and lets
 *      the pool size grow unbounded and shrink back down after 60s of idleness -- suited to many
 *      short-lived, bursty tasks, but dangerous under sustained heavy load (no cap -> can create
 *      unbounded threads just like the raw-Thread approach, defeating the point).
 *   3. `Executors.newScheduledThreadPool(n)`: supports `scheduleAtFixedRate` / `scheduleWithFixedDelay`
 *      for recurring background work (our 30-second maintenance task), replacing manual
 *      `Thread.sleep()` loops.
 *   4. Submit `Callable<V>` (not `Runnable`) when a task produces a RESULT or can throw a checked
 *      exception; `executor.submit(callable)` returns a `Future<V>` that `.get()` blocks on to
 *      retrieve the result (or unwraps the task's exception as `ExecutionException`).
 *   5. Shutdown discipline: call `shutdown()` (stop accepting new tasks, let submitted ones
 *      finish), then `awaitTermination(timeout, unit)` to wait bounded time for completion; if it
 *      times out, escalate to `shutdownNow()` (attempts to cancel in-flight tasks via
 *      interruption and returns the tasks that never started).
 *
 * ================= WHY THIS MECHANISM =================
 * - Fixed pool: predictable, bounded resource usage -- the right default for most production
 *   workloads where you want explicit control over max concurrency (protects downstream
 *   dependencies and your own memory footprint).
 * - Cached pool: optimizes for bursty, short-lived, low-volume task patterns where thread reuse
 *   matters more than a hard cap; a poor choice under sustained high load precisely because it
 *   has no bound.
 * - Scheduled pool: purpose-built for recurring/delayed execution, replacing hand-rolled
 *   Timer/Thread.sleep loops with proper cancellation and pooling support.
 * - `Callable` + `Future` over `Runnable`: `Runnable.run()` cannot return a value or throw a
 *   checked exception; `Callable<V>.call()` can do both, and `Future<V>` gives you a handle to
 *   block for the result, cancel the task, or poll for completion -- essential when you need the
 *   actual answer from concurrent work, not just "did it run."
 * - `shutdown()`/`awaitTermination()`/`shutdownNow()` sequence: gives in-flight work a fair
 *   chance to finish gracefully first, only escalating to forceful interruption-based
 *   cancellation if that grace period is exceeded -- avoids abruptly killing tasks mid-write to a
 *   file or mid-transaction unless truly necessary.
 *
 * ================= EDGE CASES =================
 * - Forgetting to shut down an ExecutorService leaks its worker threads, preventing JVM exit
 *   (non-daemon threads keep the process alive) -- always shut down in a finally block or via
 *   try-with-resources-like patterns (or use a try/finally as shown below).
 * - `shutdownNow()` only INTERRUPTS running tasks; a task that ignores `InterruptedException` or
 *   never checks `Thread.interrupted()` will keep running regardless -- cooperative cancellation
 *   requires the task itself to respect interruption.
 * - `Future.get()` blocks forever by default; always prefer `get(timeout, unit)` in code that
 *   must remain responsive, and handle `TimeoutException`.
 * - `Future.get()` unwraps the task's thrown exception inside an `ExecutionException` -- a common
 *   mistake is not unwrapping `getCause()` and losing the real error type/message.
 * - `newCachedThreadPool()` under unbounded task submission can create unbounded threads and
 *   crash the JVM (OutOfMemoryError: unable to create new native thread) -- avoid it for
 *   uncontrolled workloads.
 * - Deadlock risk: submitting a task to a fixed pool that itself blocks waiting on the result of
 *   ANOTHER task submitted to the SAME bounded pool can deadlock if the pool is fully occupied by
 *   tasks all waiting on each other (classic thread-pool-exhaustion deadlock).
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - Fixed pool bounds worst-case concurrent resource usage to n threads; throughput is limited by
 *   n and the unbounded backing queue can grow without bound if tasks arrive faster than they can
 *   be processed (a memory risk worth flagging, distinct from thread-count risk).
 * - Cached pool minimizes latency for bursty low-volume workloads (no queueing when a thread is
 *   free) at the cost of no upper bound on resource usage under sustained load.
 * - Scheduled pool's fixed-rate scheduling can "catch up" and fire back-to-back if a task
 *   execution overruns its period, while fixed-delay always waits the full delay after each
 *   completion -- a real behavioral difference worth knowing.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - What's the difference between shutdown(), shutdownNow(), and awaitTermination()?
 * - Why prefer Callable + Future over spawning raw Threads for a batch of parallel work?
 * - What happens if you never shut down an ExecutorService?
 * - What's the danger of newCachedThreadPool() under sustained heavy load?
 * - How does Future.get() propagate an exception thrown inside the task?
 * - What's the difference between scheduleAtFixedRate and scheduleWithFixedDelay?
 * - How can submitting inter-dependent tasks to a single bounded thread pool cause deadlock?
 * - How would you bound the work queue of a custom ThreadPoolExecutor to apply backpressure
 *   instead of letting it grow unbounded?
 */
public final class ExecutorServicePatterns {

    private ExecutorServicePatterns() {
    }

    /** Simulates a slow downstream price lookup for a SKU. */
    private static int fetchPrice(int skuId) throws InterruptedException {
        Thread.sleep(20); // simulate network latency
        return 100 + skuId; // fake computed price
    }

    /** Pattern 1: fixed thread pool bounding concurrency for a batch of Callable tasks. */
    private static void demonstrateFixedThreadPool() throws InterruptedException {
        System.out.println("=== newFixedThreadPool: bounded parallel SKU price fetch ===");
        int skuCount = 20;
        int poolSize = 5;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<Integer>> futures = new ArrayList<>();
        try {
            for (int sku = 0; sku < skuCount; sku++) {
                int skuId = sku;
                Callable<Integer> task = () -> fetchPrice(skuId);
                futures.add(pool.submit(task));
            }

            long total = 0;
            for (Future<Integer> future : futures) {
                try {
                    total += future.get(2, TimeUnit.SECONDS); // bounded wait, never block forever
                } catch (ExecutionException e) {
                    System.out.println("Task failed: " + e.getCause());
                } catch (TimeoutException e) {
                    System.out.println("Task timed out");
                }
            }
            // Expected: sum of (100 + skuId) for skuId in [0, 20) = 20*100 + (0+1+...+19)
            long expected = (long) skuCount * 100 + (skuCount - 1) * skuCount / 2;
            System.out.println("Total price collected: " + total + " (expected " + expected + ")");
        } finally {
            shutdownGracefully(pool);
        }
    }

    /** Pattern 2: cached thread pool for bursty, short-lived tasks. */
    private static void demonstrateCachedThreadPool() throws InterruptedException {
        System.out.println();
        System.out.println("=== newCachedThreadPool: bursty short-lived tasks ===");
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                int id = i;
                futures.add(pool.submit(() -> "task-" + id + " on " + Thread.currentThread().getName()));
            }
            for (Future<String> f : futures) {
                try {
                    System.out.println("  " + f.get(1, TimeUnit.SECONDS));
                } catch (ExecutionException | TimeoutException e) {
                    System.out.println("  task error: " + e);
                }
            }
        } finally {
            shutdownGracefully(pool);
        }
    }

    /** Pattern 3: scheduled thread pool for recurring background work. */
    private static void demonstrateScheduledThreadPool() throws InterruptedException {
        System.out.println();
        System.out.println("=== newScheduledThreadPool: recurring maintenance task ===");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            int[] runCount = {0};
            Runnable maintenanceTask = () -> {
                runCount[0]++;
                System.out.println("  maintenance tick #" + runCount[0]);
            };
            // Runs every 100ms, first execution after an initial 0ms delay.
            scheduler.scheduleAtFixedRate(maintenanceTask, 0, 100, TimeUnit.MILLISECONDS);
            Thread.sleep(450); // let it fire a few times
            // Expected: roughly 4-5 ticks in ~450ms at a 100ms period (timing-dependent, not exact).
            System.out.println("  observed ticks in ~450ms window: " + runCount[0]);
        } finally {
            // shutdownNow() to cancel the still-recurring task immediately; a recurring scheduled
            // task never "finishes" on its own, so graceful shutdown() would wait forever for it.
            List<Runnable> cancelled = scheduler.shutdownNow();
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("  cancelled pending tasks at shutdown: " + cancelled.size());
        }
    }

    /** Demonstrates the shutdown() -> awaitTermination() -> shutdownNow() escalation pattern. */
    private static void shutdownGracefully(ExecutorService pool) throws InterruptedException {
        pool.shutdown(); // stop accepting new tasks; let submitted tasks finish
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("  pool did not terminate in time, forcing shutdownNow()");
            pool.shutdownNow(); // attempt to cancel in-flight tasks via interruption
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("  pool still did not terminate after shutdownNow()");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        demonstrateFixedThreadPool();
        demonstrateCachedThreadPool();
        demonstrateScheduledThreadPool();
    }
}
