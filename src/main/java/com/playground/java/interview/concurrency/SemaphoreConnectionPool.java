package com.playground.java.interview.concurrency;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN: Semaphore-Based Bounded Resource Pool
 * PRIORITY: P1 - High Priority
 * TOPIC: A counting Semaphore caps the number of threads that can concurrently hold a
 * scarce resource (e.g. DB connections), blocking additional acquirers until a permit is released.
 */
public class SemaphoreConnectionPool {

    // ================= PROBLEM =================
    // We have a fixed number of expensive resources (e.g. 3 real DB connections) but many
    // more threads that want to use one briefly. We need: (a) at most POOL_SIZE threads use
    // a connection at once, (b) extra threads block until a connection frees up, (c) no thread
    // can accidentally "leak" a permit and starve the pool.

    static final int POOL_SIZE = 3;
    static final int CLIENT_THREADS = 8;

    // Simulated fixed-size pool of "connections" guarded by a counting Semaphore.
    static class ConnectionPool {
        private final Semaphore permits;
        private final Deque<Connection> available;

        ConnectionPool(int size) {
            this.permits = new Semaphore(size, true); // fair=true: FIFO order under contention
            this.available = new ArrayDeque<>();
            for (int i = 0; i < size; i++) {
                available.push(new Connection("conn-" + i));
            }
        }

        Connection acquire(long timeout, TimeUnit unit) throws InterruptedException {
            if (!permits.tryAcquire(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting for a free connection");
            }
            // permits acquired successfully -> guaranteed a connection is available in the deque
            synchronized (available) {
                return available.pop();
            }
        }

        void release(Connection connection) {
            synchronized (available) {
                available.push(connection);
            }
            permits.release(); // MUST always release, even on failure, or the pool shrinks forever
        }

        int availablePermits() {
            return permits.availablePermits();
        }
    }

    static class Connection {
        final String id;

        Connection(String id) {
            this.id = id;
        }
    }

    // ================= NAIVE / UNSAFE APPROACH =================
    // Without a Semaphore, an unbounded pool (or no pool at all - "new connection per request")
    // lets every thread grab a resource simultaneously, exhausting a real backing system
    // (DB max_connections, socket limits, etc). We illustrate the problem conceptually:
    static void unboundedAccessProblem() {
        System.out.println("(anti-pattern illustrated in comments only): if every one of "
                + CLIENT_THREADS + " threads opened its own DB connection with no cap, "
                + "and the real DB only allows " + POOL_SIZE + " concurrent connections, "
                + "the extra connections would be refused/error out under load.");
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // Semaphore.acquire() blocks the calling thread until a permit is available; release()
    // returns the permit. This bounds concurrent usage to POOL_SIZE regardless of how many
    // client threads are submitted.
    static void runConnectionPoolDemo() throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(POOL_SIZE);
        AtomicInteger concurrentUsers = new AtomicInteger(0);
        AtomicInteger maxObservedConcurrency = new AtomicInteger(0);

        ExecutorService clients = Executors.newFixedThreadPool(CLIENT_THREADS);
        for (int i = 0; i < CLIENT_THREADS; i++) {
            final int clientId = i;
            clients.submit(() -> {
                try {
                    System.out.println("Client-" + clientId + " requesting a connection "
                            + "(permits available before acquire=" + pool.availablePermits() + ")");
                    Connection connection = pool.acquire(5, TimeUnit.SECONDS);
                    int current = concurrentUsers.incrementAndGet();
                    maxObservedConcurrency.updateAndGet(prev -> Math.max(prev, current));
                    try {
                        System.out.println("Client-" + clientId + " using " + connection.id
                                + " (concurrent users now = " + current + ")");
                        Thread.sleep(150); // simulate work using the connection
                    } finally {
                        concurrentUsers.decrementAndGet();
                        pool.release(connection); // always release in a finally block
                        System.out.println("Client-" + clientId + " released " + connection.id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IllegalStateException timeoutEx) {
                    System.out.println("Client-" + clientId + " gave up: " + timeoutEx.getMessage());
                }
            });
        }

        clients.shutdown();
        clients.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Max concurrent connection users observed = " + maxObservedConcurrency.get()
                + " (must be <= POOL_SIZE=" + POOL_SIZE + ")");
    }

    // ================= WHY THIS MECHANISM =================
    // - Semaphore maintains an internal non-negative permit count backed by AQS; acquire()
    //   blocks (parking the thread) when the count is zero, release() increments the count
    //   and wakes a waiting thread (FIFO if constructed with fair=true).
    // - Unlike a lock, a Semaphore is not owned by a single thread - any thread can call
    //   release(), which is exactly the model needed for a resource pool (a worker thread
    //   "returns" a connection that a different thread might next acquire).

    // ================= EDGE CASES =================
    // - Forgetting to release() in a finally block leaks a permit permanently, silently
    //   shrinking the effective pool size until it deadlocks everyone.
    // - Calling release() without a matching successful acquire() (e.g. releasing twice)
    //   over-inflates the permit count, letting MORE than POOL_SIZE threads in concurrently -
    //   worse than useless. Always pair exactly one release with one successful acquire.
    // - tryAcquire(timeout) vs acquire(): prefer the timed version in production to avoid
    //   threads blocking forever if the pool is exhausted due to a bug/leak elsewhere.
    // - Fair (true) vs non-fair (false) semaphores: fair avoids starvation of long-waiting
    //   threads but has lower raw throughput due to strict FIFO handoff overhead.
    // - A Semaphore(1) is NOT the same as a Lock: it has no notion of "owner", so any thread
    //   can release it (even one that never acquired it) - useful for signaling, dangerous if misused.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - acquire()/release() are O(1) (ignoring blocking wait time), backed by AQS's CLH queue.
    // - Throughput is bounded by POOL_SIZE * (rate at which one connection's work completes);
    //   increasing CLIENT_THREADS beyond POOL_SIZE only adds queuing/waiting, not new capacity.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How is Semaphore different from ReentrantLock in terms of "ownership" of the permit/lock?
    // - What happens if you call release() more times than acquire() - is that ever a legitimate pattern?
    // - Why should release() almost always be in a finally block?
    // - What's the tradeoff between a fair and non-fair Semaphore?
    // - How would you build a real connection pool with health-checking/eviction on top of this?
    // - How does Semaphore.tryAcquire(timeout) help prevent cascading failures/thread pool
    //   exhaustion in a production service under load?
    // - Could you use a BlockingQueue<Connection> instead of Semaphore + Deque here? What
    //   are the tradeoffs (a bounded BlockingQueue actually combines both roles in one object)?

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1) Naive: unbounded concurrent access (illustration only) ===");
        unboundedAccessProblem();

        System.out.println();
        System.out.println("=== 2) Safe: Semaphore-bounded connection pool (POOL_SIZE=" + POOL_SIZE
                + ", CLIENT_THREADS=" + CLIENT_THREADS + ") ===");
        // Expected: at most POOL_SIZE clients hold a connection at the same instant; the other
        // CLIENT_THREADS - POOL_SIZE clients block until a permit frees up; final printed
        // "Max concurrent connection users observed" must be <= POOL_SIZE.
        runConnectionPoolDemo();
    }
}
