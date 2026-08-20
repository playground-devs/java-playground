package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams
 * PRIORITY: P0 - Must Know
 * TOPIC: Chain map(), filter(), and reduce() on a List of numbers to square the
 * even numbers and sum the results.
 */
public class StreamsBasicsMapFilterReduce {

    // ================= WHAT IS BEING TESTED =================
    // This tests basic fluency with the java.util.stream.Stream pipeline: the
    // ability to compose filter() (selection), map() (transformation), and
    // reduce() (aggregation) into a single declarative pipeline instead of a
    // hand-rolled for-loop with mutable accumulators. Interviewers ask this
    // because it is the single most common "warm up" Java 8 question and it
    // reveals whether a candidate actually thinks in terms of stream stages
    // (source -> intermediate ops -> terminal op) or is just reciting syntax.

    // ================= APPROACH =================
    // 1. Obtain a Stream<Integer> from the source List via list.stream().
    // 2. filter(n -> n % 2 == 0)  -> intermediate op, keeps only even numbers.
    // 3. map(n -> n * n)          -> intermediate op, transforms each surviving
    //    element into its square.
    // 4. reduce(0, Integer::sum)  -> terminal op, folds all squared values into
    //    a single sum, starting from an identity value of 0.
    // Steps 2-3 are lazy and only actually execute element-by-element once the
    // terminal reduce() is invoked (see COMPLEXITY below).

    // ================= WHY THIS API =================
    // Streams express *what* to compute (even -> square -> sum) rather than
    // *how* to loop, index, and mutate an accumulator variable. That makes the
    // pipeline read like the specification itself, it composes/refactors
    // easily (e.g. swapping reduce() for collect(Collectors.summingInt(...))),
    // and it is trivially parallelizable via parallelStream() if needed.
    // You would still prefer an explicit for-loop when: you need to break/
    // return early based on complex multi-variable state, you need to mutate
    // several unrelated accumulators in one pass (streams push you toward one
    // result per pipeline), or the loop body has side effects that make a
    // functional pipeline harder to read than the imperative version.

    // ================= COMMON MISTAKES =================
    // - Calling reduce() without an identity value on a possibly empty stream
    //   and then calling .get() on the returned Optional without checking
    //   isPresent(), causing NoSuchElementException.
    // - Reusing the same Stream instance twice (streams are single-use; a
    //   second terminal operation throws IllegalStateException: stream has
    //   already been operated upon or closed).
    // - Putting side effects (e.g. incrementing an external counter) inside
    //   map()/filter() lambdas instead of using proper terminal/collector ops.
    // - Confusing reduce(identity, accumulator) with reduce(accumulator) which
    //   returns Optional<T> instead of T.

    // ================= EDGE CASES =================
    // - Empty list: filter/map produce an empty stream, reduce(0, ...) safely
    //   returns the identity value 0.
    // - List with no even numbers: sum is 0 (identity), not an error.
    // - Single-element list: pipeline still works, degenerate case of N=1.
    // - Null elements in the list: filter's n % 2 == 0 would NPE on unboxing;
    //   real code should filter Objects::nonNull first.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each element is visited once through the fused
    // filter/map/reduce pipeline (streams do not create intermediate
    // collections between stages; stages are fused per element).
    // Space Complexity: O(1) additional space beyond the input list, since we
    // reduce into a single accumulator rather than collecting into a new list.
    // Streams are lazy: filter() and map() are intermediate operations that
    // build up a pipeline description but do not touch any elements until the
    // terminal operation reduce() is invoked; only then does data flow through
    // the whole pipeline one element at a time.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What is the difference between reduce(identity, accumulator) and the
    //   three-argument reduce(identity, accumulator, combiner)? When is the
    //   combiner actually invoked?
    // - Why are streams described as "lazy"? What would happen if you removed
    //   the terminal operation entirely?
    // - How would you rewrite this using Collectors.summingInt() instead of
    //   reduce()? Which is more idiomatic here and why?
    // - Can you reuse a Stream object for two terminal operations? What
    //   exception is thrown if you try?
    // - How would parallelStream() change the semantics of this reduce() call?
    //   What constraints must the accumulator/combiner satisfy for parallel
    //   reduction to be correct (associativity, statelessness)?
    // - What's the performance/readability trade-off of chaining many small
    //   lambdas vs a single more complex lambda in a stream pipeline?

    /**
     * Streams version: filter even numbers, square them, sum via reduce().
     */
    public static int sumOfSquaresOfEvens(List<Integer> numbers) {
        return numbers.stream()
                .filter(n -> n % 2 == 0)   // keep only even numbers
                .map(n -> n * n)           // square each surviving number
                .reduce(0, Integer::sum);  // fold into a single sum, identity = 0
    }

    /**
     * Imperative equivalent, shown for contrast with the Stream version above.
     */
    public static int sumOfSquaresOfEvensImperative(List<Integer> numbers) {
        int sum = 0;
        for (Integer n : numbers) {
            if (n % 2 == 0) {
                sum += n * n;
            }
        }
        return sum;
    }

    /**
     * Bonus variant: same idea but on a List<String>, converting each string's
     * length to its square only for strings of even length, joined via
     * reduce() into a comma separated description (shows reduce() over a
     * non-numeric transformation as well).
     */
    public static List<Integer> squaresOfEvenLengthWordLengths(List<String> words) {
        return words.stream()
                .map(String::length)          // transform word -> its length
                .filter(len -> len % 2 == 0)  // keep only even lengths
                .map(len -> len * len)        // square the even lengths
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("Input: " + numbers);
        System.out.println("sumOfSquaresOfEvens = " + sumOfSquaresOfEvens(numbers));
        // Expected: 2^2 + 4^2 + 6^2 + 8^2 + 10^2 = 4 + 16 + 36 + 64 + 100 = 220

        System.out.println("Imperative equivalent = " + sumOfSquaresOfEvensImperative(numbers));
        // Expected: 220 (same result, imperative style)

        List<Integer> emptyList = Arrays.asList();
        System.out.println("Empty list -> " + sumOfSquaresOfEvens(emptyList));
        // Expected: 0 (identity value from reduce, no exception)

        List<Integer> allOdds = Arrays.asList(1, 3, 5, 7);
        System.out.println("All odds -> " + sumOfSquaresOfEvens(allOdds));
        // Expected: 0 (nothing survives the filter)

        List<String> words = Arrays.asList("java", "is", "fun", "streams", "api");
        System.out.println("Word lengths squared (even lengths only) = "
                + squaresOfEvenLengthWordLengths(words));
        // Expected: "java"(4) -> 16, "is"(2) -> 4  => [16, 4]
    }
}
