package com.playground.java.interview.concurrency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * PATTERN: Concurrency / Asynchronous Composition
 * PRIORITY: P0 - Must Know
 * TOPIC: Chaining asynchronous work with CompletableFuture -- thenApply vs thenCompose, combining
 * independent futures with thenCombine, and handling failures with exceptionally/handle.
 *
 * ================= PROBLEM =================
 * An order-checkout flow needs to: (1) look up a user's account asynchronously, (2) use that
 * account to asynchronously fetch their loyalty discount tier (a dependent async call), (3)
 * concurrently and independently fetch current shipping cost, then combine the discount and
 * shipping into a final price, all without blocking a thread for each network call, and with
 * clean error handling if any step fails (e.g. the account lookup fails).
 *
 * ================= NAIVE / UNSAFE APPROACH =================
 * A candidate used to synchronous code chains blocking calls, or worse, mixes raw Future.get()
 * calls with manual thread creation:
 *
 *   Account account = fetchAccountBlocking(userId);       // blocks calling thread
 *   Tier tier = fetchTierBlocking(account);                // blocks again, sequentially
 *   double shipping = fetchShippingBlocking(orderId);      // blocks again, even though this has
 *                                                            // NO dependency on account/tier and
 *                                                            // could have run concurrently
 *
 * Why it's broken (not a race condition here, but a real design/performance bug and an error-
 * handling trap): each blocking call ties up a thread doing nothing but waiting on I/O, so
 * throughput under load is bounded by thread-pool size rather than actual work; the shipping
 * lookup is needlessly serialized after the account/tier lookups even though it's fully
 * independent, wasting latency; and a plain try/catch around this chain conflates "which step
 * failed" and offers no composable way to attach fallback/recovery logic per stage or await
 * multiple independent results concurrently.
 *
 * ================= SAFE / OPTIMIZED APPROACH =================
 * Compose the pipeline with CompletableFuture:
 *   1. `CompletableFuture.supplyAsync(() -> fetchAccount(userId))` kicks off the account lookup
 *      asynchronously on a thread pool (default: the common ForkJoinPool, or a custom executor
 *      passed as a second argument -- important for isolating blocking I/O work from CPU-bound
 *      work elsewhere in the app).
 *   2. `.thenCompose(account -> fetchTierAsync(account))`: since `fetchTierAsync` ITSELF returns
 *      a `CompletableFuture<Tier>`, using `thenApply` here would produce a nested
 *      `CompletableFuture<CompletableFuture<Tier>>` -- a "future of a future," which is almost
 *      never what you want and forces an awkward extra unwrap. `thenCompose` "flattens" this,
 *      analogous to `flatMap` on a Stream/Optional: it takes a function that itself returns a
 *      CompletableFuture and splices its result directly into the outer chain, producing a plain
 *      `CompletableFuture<Tier>`.
 *   3. Meanwhile, independently, `CompletableFuture.supplyAsync(() -> fetchShipping(orderId))`
 *      starts running CONCURRENTLY with step 1/2 -- no artificial serialization.
 *   4. `.thenCombine(shippingFuture, (tier, shippingCost) -> computeFinalPrice(tier, shippingCost))`
 *      joins two INDEPENDENT futures once both complete, combining their results with a
 *      BiFunction -- this is how you fan-in concurrent, unrelated async work.
 *   5. `.exceptionally(ex -> fallbackPrice())` supplies a fallback VALUE if any upstream stage
 *      failed, swallowing the exception (use when you have a safe default). `.handle((result,
 *      ex) -> ...)` runs regardless of success or failure and lets you inspect BOTH the result
 *      and the exception (null if none), useful for logging or transforming either outcome
 *      without deciding to swallow the error.
 * `thenApply` is used purely to TRANSFORM an already-available value with a plain function (e.g.
 * converting a raw price into a formatted string) -- it never itself returns a
 * CompletableFuture, unlike the function passed to thenCompose.
 *
 * ================= WHY THIS MECHANISM =================
 * `CompletableFuture` (over raw `Future`) was chosen because it supports NON-BLOCKING
 * composition: callbacks (`thenApply`, `thenCompose`, `thenCombine`, `exceptionally`, `handle`)
 * are attached to a future and run automatically when it completes, without any thread blocking
 * on `.get()` in the middle of the pipeline. Raw `Future` only offers a blocking `.get()` with no
 * way to chain, combine, or react to completion asynchronously -- CompletableFuture is what
 * turned Java's Future from "a handle to wait on" into "a composable async value," conceptually
 * similar to Promises in JavaScript. We reach for plain synchronous code only when there's truly
 * no concurrent/independent work to overlap and no async I/O boundary to avoid blocking a thread
 * on.
 *
 * ================= EDGE CASES =================
 * - An unhandled exception in ANY stage short-circuits all subsequent `thenApply`/`thenCompose`
 *   stages (they're skipped) and propagates to the first `exceptionally`/`handle`/`whenComplete`
 *   downstream, or surfaces as `ExecutionException` (wrapping the real cause) if you eventually
 *   call blocking `.get()`.
 * - `.join()` vs `.get()`: `join()` throws an unchecked `CompletionException` instead of a
 *   checked `ExecutionException`, convenient in lambdas where checked exceptions are awkward.
 * - Which thread runs a callback matters: `thenApply`/`thenCompose` (no "Async" suffix) may run
 *   the callback on the SAME thread that completed the previous stage (could be the calling
 *   thread if already complete, or a pool thread) -- use `thenApplyAsync`/`thenComposeAsync`
 *   (optionally with an explicit `Executor` argument) to force execution on a specific pool and
 *   avoid accidentally running heavy work on a shared thread (e.g. the common ForkJoinPool used
 *   by parallel streams elsewhere in the app).
 * - Blocking I/O inside a stage without a custom executor risks starving the default common pool
 *   used by all CompletableFutures app-wide -- always supply a dedicated executor for blocking
 *   calls.
 * - `exceptionally()` only fires on failure and cannot inspect a successful result; `handle()`
 *   fires on both success and failure and must itself decide what to return, so a mistake there
 *   can accidentally swallow a value or a needed exception if not handled carefully.
 *
 * ================= COMPLEXITY / PERFORMANCE NOTES =================
 * - The composed pipeline here has "critical path" latency equal to
 *   max(account+tier lookup, shipping lookup), NOT the sum of all three, because shipping runs
 *   concurrently with the account/tier chain -- a real, measurable latency win over sequential
 *   blocking code proportional to how much work is independent.
 * - No thread is blocked waiting on I/O between stages, so a small thread pool can service many
 *   more concurrent pipelines than an equivalent blocking-call-per-thread design -- much better
 *   thread utilization under high concurrent load (this is precisely why reactive/async stacks
 *   use this model).
 * - Overhead: each `.then*` call allocates a new CompletableFuture and callback wrapper -- for
 *   very hot, simple synchronous paths this indirection has a small overhead over direct
 *   synchronous calls; the win only materializes once there's genuine I/O latency or independent
 *   work to overlap.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - What's the difference between thenApply and thenCompose, and when does using the wrong one
 *   produce a CompletableFuture&lt;CompletableFuture&lt;T&gt;&gt;?
 * - How does thenCombine differ from thenCompose -- independent vs dependent async work?
 * - What's the difference between exceptionally() and handle()?
 * - What's the difference between thenApply and thenApplyAsync, and why would you pass a custom
 *   Executor to the Async variants?
 * - What happens to a chain of thenApply calls if an earlier stage throws?
 * - Difference between CompletableFuture.get() and join() in terms of exception types?
 * - How would you time out a CompletableFuture chain (orTimeout / completeOnTimeout, Java 9+)?
 * - How would you fan out N independent async calls and wait for all of them (allOf) or the
 *   first to complete (anyOf)?
 */
public final class CompletableFutureChaining {

    private CompletableFutureChaining() {
    }

    static final class Account {
        final String userId;
        Account(String userId) { this.userId = userId; }
    }

    static final class Tier {
        final String name;
        final double discountPercent;
        Tier(String name, double discountPercent) { this.name = name; this.discountPercent = discountPercent; }
    }

    private static Account fetchAccount(String userId, boolean simulateFailure) {
        sleep(30);
        if (simulateFailure) {
            throw new RuntimeException("account service unavailable for user " + userId);
        }
        return new Account(userId);
    }

    /** Returns a nested CompletableFuture itself, to illustrate why thenCompose is needed. */
    private static CompletableFuture<Tier> fetchTierAsync(Account account, ExecutorService pool) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(30);
            return new Tier("GOLD", 0.10);
        }, pool);
    }

    private static double fetchShippingCost(String orderId) {
        sleep(40);
        return 9.99;
    }

    private static double computeFinalPrice(Tier tier, double shippingCost) {
        double basePrice = 100.0;
        double discounted = basePrice * (1 - tier.discountPercent);
        return discounted + shippingCost;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Builds the full composed pipeline: dependent chain via thenCompose, fan-in via thenCombine. */
    private static CompletableFuture<Double> buildPricingPipeline(
            String userId, String orderId, ExecutorService pool, boolean simulateAccountFailure) {

        CompletableFuture<Tier> tierFuture = CompletableFuture
                .supplyAsync(() -> fetchAccount(userId, simulateAccountFailure), pool)
                // thenCompose: fetchTierAsync ITSELF returns a CompletableFuture<Tier>; thenApply
                // would have produced CompletableFuture<CompletableFuture<Tier>> here instead.
                .thenCompose(account -> fetchTierAsync(account, pool));

        CompletableFuture<Double> shippingFuture = CompletableFuture
                .supplyAsync(() -> fetchShippingCost(orderId), pool); // runs concurrently with tierFuture

        return tierFuture
                // thenCombine: joins two INDEPENDENT futures once both complete.
                .thenCombine(shippingFuture, CompletableFutureChaining::computeFinalPrice)
                // thenApply: pure value transformation, no nested future involved.
                .thenApply(price -> Math.round(price * 100.0) / 100.0)
                // exceptionally: supply a fallback VALUE if any upstream stage failed.
                .exceptionally(ex -> {
                    System.out.println("  pipeline failed (" + ex.getMessage() + "), using fallback price");
                    return -1.0;
                });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            System.out.println("=== Happy path: independent shipping lookup overlaps with account+tier chain ===");
            long start = System.nanoTime();
            CompletableFuture<Double> pricingFuture = buildPricingPipeline("user-42", "order-7", pool, false);
            double finalPrice = pricingFuture.get(); // block only once, at the very end, for this demo
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            // Expected: 100 * (1 - 0.10) + 9.99 = 90 + 9.99 = 99.99, computed in roughly
            // max(account 30ms + tier 30ms, shipping 40ms) ~= 60ms critical path, NOT the naive
            // sequential sum of 30+30+40=100ms, because shipping overlaps with account+tier.
            System.out.println("Final price: " + finalPrice + " (expected 99.99), elapsed ~" + elapsedMs + "ms");

            System.out.println();
            System.out.println("=== Failure path: account lookup fails, exceptionally() supplies fallback ===");
            CompletableFuture<Double> failingPipeline = buildPricingPipeline("user-99", "order-8", pool, true);
            double fallbackPrice = failingPipeline.get();
            // Expected: -1.0, the fallback value from exceptionally(), because fetchAccount threw.
            System.out.println("Final price after failure: " + fallbackPrice + " (expected -1.0)");

            System.out.println();
            System.out.println("=== handle(): observes both success and failure without swallowing by default ===");
            CompletableFuture<String> handledSuccess = CompletableFuture
                    .supplyAsync(() -> fetchAccount("user-1", false), pool)
                    .handle((account, ex) -> ex == null
                            ? "OK: account for " + account.userId
                            : "FAILED: " + ex.getMessage());
            CompletableFuture<String> handledFailure = CompletableFuture
                    .supplyAsync(() -> fetchAccount("user-2", true), pool)
                    .handle((account, ex) -> ex == null
                            ? "OK: account for " + account.userId
                            : "FAILED: " + ex.getMessage());
            // Expected: first prints "OK: account for user-1", second prints a "FAILED: ..." message.
            System.out.println("  " + handledSuccess.get());
            System.out.println("  " + handledFailure.get());
        } finally {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
