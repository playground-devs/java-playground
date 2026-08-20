package com.playground.java.interview.concurrency;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * PATTERN: Fork/Join Framework (RecursiveTask + Work-Stealing Pool)
 * PRIORITY: P2 - Good to Know
 * TOPIC: ForkJoinPool + RecursiveTask let CPU-bound divide-and-conquer work (like summing a
 * huge array) be split recursively into small subtasks that are load-balanced across worker
 * threads via work-stealing, then combined back into a single result.
 */
public class ForkJoinTaskDemo {

    // ================= PROBLEM =================
    // We need to sum a very large array. A single-threaded loop is O(n) but uses only one
    // core. A naive "split into N chunks up front, one thread per chunk" approach can suffer
    // from load imbalance if chunks take different amounts of time. Fork/Join solves this by
    // recursively splitting the problem until pieces are small ("threshold"), submitting each
    // piece as a task, and letting IDLE worker threads "steal" queued sub-tasks from BUSY
    // worker threads' queues, so work self-balances across all available cores.

    private static final int ARRAY_SIZE = 20_000_000;
    private static final int THRESHOLD = 10_000; // below this size, compute sequentially (no more splitting)

    // ================= NAIVE / UNSAFE APPROACH =================
    // Plain sequential sum - correct, but only uses one CPU core no matter how many are available.
    static long sequentialSum(long[] data) {
        long sum = 0;
        for (long value : data) {
            sum += value;
        }
        return sum;
    }

    // ================= SAFE / OPTIMIZED APPROACH =================
    // RecursiveTask<Long>: split the range in half until it's small enough (<= THRESHOLD),
    // then compute directly; combine left+right results after both complete (fork the left
    // half to run asynchronously, compute the right half on the current thread, then join).
    static class SumTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int start; // inclusive
        private final int end;   // exclusive

        SumTask(long[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += data[i];
                }
                return sum;
            }
            int mid = start + length / 2;
            SumTask leftTask = new SumTask(data, start, mid);
            SumTask rightTask = new SumTask(data, mid, end);

            leftTask.fork();                       // schedule left half asynchronously on the pool
            long rightResult = rightTask.compute(); // compute right half on THIS thread (avoids one extra fork)
            long leftResult = leftTask.join();       // wait for (or help execute, via work-stealing) the left half

            return leftResult + rightResult;
        }
    }

    static long parallelSum(long[] data) {
        ForkJoinPool pool = ForkJoinPool.commonPool(); // shared JVM-wide pool, sized to Runtime.availableProcessors()
        SumTask rootTask = new SumTask(data, 0, data.length);
        return pool.invoke(rootTask); // submits and blocks the calling thread until the result is ready
    }

    // ================= WHY THIS MECHANISM =================
    // - ForkJoinPool gives each worker thread its own DOUBLE-ENDED work queue (deque). A
    //   worker pushes/pops its OWN new sub-tasks from the head of its own deque (LIFO, good
    //   cache locality for depth-first recursive work), but when a worker runs out of local
    //   work, it "steals" from the TAIL of another (busy) worker's deque (FIFO-ish from the
    //   victim's perspective) - this is the "work-stealing" algorithm.
    // - fork() schedules a subtask for (potential) execution by another thread without
    //   blocking; join() waits for that subtask's result, and if it hasn't started yet, the
    //   calling thread can help execute OTHER queued work (including stolen tasks) while waiting.
    // - RecursiveTask<V> is for computations that RETURN a value; RecursiveAction is the
    //   sibling class for void-returning divide-and-conquer work.

    // ================= EDGE CASES =================
    // - THRESHOLD too small: excessive task-object overhead and scheduling churn can make it
    //   SLOWER than sequential; THRESHOLD too large: not enough parallelism to use all cores.
    // - Calling compute() directly (instead of fork()+join(), or invoke()) on a task runs it
    //   synchronously on the current thread - no parallelism gained; the pattern above
    //   ("fork left, compute right directly, then join left") is the standard idiom that
    //   avoids one unnecessary fork per level of recursion.
    // - Blocking I/O or lock-waiting inside compute() defeats the pool's design (worker threads
    //   are meant for CPU-bound work); for blocking work use ForkJoinPool.ManagedBlocker or a
    //   separate ExecutorService entirely.
    // - Using ForkJoinPool.commonPool() means you SHARE it with parallel streams and other
    //   library code in the same JVM; a misbehaving task (e.g. one that blocks) can starve
    //   unrelated parallel stream operations elsewhere in the application.
    // - Exceptions thrown inside compute() are captured and re-thrown (wrapped) from join()/get()
    //   on the calling thread - always be prepared to catch them at the invoke() call site.

    // ================= COMPLEXITY / PERFORMANCE NOTES =================
    // - Sequential sum: O(n) time, uses 1 core.
    // - Parallel fork/join sum: O(n / P + log n) ideal time with P available cores (the log n
    //   term is the recursive splitting overhead), so speedup approaches P for large n and a
    //   well-tuned THRESHOLD; for SMALL n the fork/join overhead can make it slower than sequential.
    // - Work-stealing keeps all cores busy even when subtasks have uneven runtime, unlike a
    //   naive fixed up-front partitioning across a fixed thread pool.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Explain the work-stealing algorithm: why do workers push/pop locally from one end but
    //   steal from the other end of a victim's deque?
    // - What's the difference between RecursiveTask and RecursiveAction?
    // - Why does the idiom "fork one half, compute the other half directly, then join" outperform
    //   forking both halves?
    // - How do you choose a good THRESHOLD for splitting, and what happens if it's too small/large?
    // - What's the relationship between ForkJoinPool and Java 8 parallel streams
    //   (Stream.parallel() uses ForkJoinPool.commonPool() by default)?
    // - Why is blocking I/O inside a ForkJoinTask problematic, and what's ManagedBlocker for?
    // - How does exception propagation work when a subtask throws - where does the exception
    //   surface?

    public static void main(String[] args) {
        long[] data = new long[ARRAY_SIZE];
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1L; // known sum via Gauss formula for verification: n*(n+1)/2
        }
        long expected = (long) ARRAY_SIZE * (ARRAY_SIZE + 1) / 2;

        long seqStart = System.nanoTime();
        long seqResult = sequentialSum(data);
        long seqMs = (System.nanoTime() - seqStart) / 1_000_000;

        long parStart = System.nanoTime();
        long parResult = parallelSum(data);
        long parMs = (System.nanoTime() - parStart) / 1_000_000;

        System.out.println("Array size = " + ARRAY_SIZE + ", available processors = "
                + Runtime.getRuntime().availableProcessors());
        // Expected: seqResult == parResult == expected (Gauss sum), for every run - fork/join
        // must produce the EXACT same result as sequential, just faster on multi-core machines.
        System.out.println("Expected sum   = " + expected);
        System.out.println("Sequential sum = " + seqResult + " in " + seqMs + " ms");
        System.out.println("Parallel sum   = " + parResult + " in " + parMs + " ms");
        System.out.println("Results match expected: " + (seqResult == expected && parResult == expected));
        System.out.println("(Parallel is typically faster on multi-core machines for arrays this large; "
                + "on a single-core machine or very small arrays it can be slower due to task overhead.)");

        // Give the common pool a moment to settle before JVM exit (not required, just tidy for demo output).
        ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.SECONDS);
    }
}
