package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams
 * PRIORITY: P0 - Must Know
 * TOPIC: Contrast map() (one-to-one, produces nested Stream<List<X>>) with
 * flatMap() (one-to-many, flattens a List<List<X>> into a single Stream<X>).
 */
public class MapVsFlatMap {

    // ================= WHAT IS BEING TESTED =================
    // This tests whether a candidate understands the fundamental difference
    // between map() (a one-to-one element transformation, T -> R) and
    // flatMap() (a one-to-many transformation, T -> Stream<R>, whose resulting
    // streams are then flattened into a single stream). Interviewers ask this
    // because it is the #1 source of confusion for developers new to
    // functional streams: applying map() where flatMap() is needed produces a
    // Stream<List<X>> (a stream of collections) instead of the flat Stream<X>
    // that most downstream code actually wants.

    // ================= APPROACH =================
    // Given a List<String> of sentences, split each sentence into words:
    //   1a. map() version: sentences.stream().map(s -> Arrays.asList(s.split(" ")))
    //       -> each sentence is transformed into a List<String> of its words,
    //       giving a Stream<List<String>> (a stream of lists, still nested).
    //   1b. flatMap() version: sentences.stream()
    //       .flatMap(s -> Arrays.stream(s.split(" ")))
    //       -> each sentence is transformed into a Stream<String> of its words,
    //       and flatMap() merges ("flattens") all of those per-sentence
    //       streams into one single Stream<String> of all words.
    // 2. collect(Collectors.toList()) is the terminal op that materializes the
    //    result in both cases, but the *shape* of the result differs: nested
    //    list-of-lists for map(), flat list for flatMap().

    // ================= WHY THIS API =================
    // flatMap() is the idiomatic way to express "each input produces zero or
    // more outputs, and I want all outputs combined into one flat stream" -
    // doing this with a for-loop would require an explicit nested loop plus a
    // mutable output list to accumulate results, which is exactly the
    // boilerplate flatMap() eliminates. You would still reach for an explicit
    // loop if you needed to track per-sentence context (e.g. "word + which
    // sentence index it came from") alongside the flattening, since plain
    // flatMap() discards that association unless you explicitly carry it
    // through in the mapped stream's elements (e.g. via a small pair/record).

    // ================= COMMON MISTAKES =================
    // - Using map() when the lambda itself returns a collection/stream,
    //   leaving you with an unwanted Stream<List<X>> or Stream<Stream<X>>
    //   instead of a flat Stream<X>.
    // - Forgetting that flatMap()'s function must return a Stream (or be
    //   converted to one, e.g. via Arrays.stream(...) or list.stream()), not
    //   the raw collection/array itself.
    // - Using flatMap() when a simple map() would suffice (over-flattening),
    //   e.g. flattening a stream of Strings that are already scalar values.
    // - Not handling null/empty inner collections, which can produce NPEs or
    //   silently contribute zero elements to the flattened stream.

    // ================= EDGE CASES =================
    // - Empty list of sentences: both map() and flatMap() pipelines produce
    //   an empty result with no exceptions.
    // - A sentence that is an empty string "": split(" ") on "" yields a
    //   single-element array [""], so an empty "word" is included; real code
    //   may want to filter blank tokens.
    // - A sentence with only whitespace or multiple spaces: split(" ") can
    //   produce empty-string tokens between consecutive delimiters.
    // - Single sentence with a single word: degenerate case, flatMap still
    //   works correctly, producing a one-element flat stream.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n * m) where n = number of sentences and m = average
    // number of words per sentence, since every word in every sentence is
    // visited exactly once by the flattened pipeline.
    // Space Complexity: O(n * m) for the final flattened List<String> of all
    // words (or O(n) top-level lists each of size m for the map() version).
    // Streams are lazy: flatMap() does not eagerly build all per-sentence
    // streams up front; each sentence's word-stream is opened, consumed into
    // the flattened output, and closed one at a time as the terminal
    // collect() operation pulls elements through the pipeline.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What is the functional signature difference between the lambda passed
    //   to map() (Function<T, R>) vs flatMap() (Function<T, Stream<R>>)?
    // - How would you flatten a List<List<Integer>> that is already fully
    //   nested (no splitting needed) using flatMap()?
    // - How does Optional.flatMap() relate conceptually to Stream.flatMap()?
    // - What happens if the function passed to flatMap() returns an empty
    //   Stream for some elements? Does it throw, or just contribute nothing?
    // - Can you chain multiple flatMap() calls for doubly-nested structures
    //   (e.g. List<List<List<X>>>)? What would that look like?
    // - Why can't you directly collect() a Stream<List<String>> into a flat
    //   List<String> without an explicit flatMap() or flatten step first?

    /**
     * map() version: produces a Stream<List<String>> - one List<String> of
     * words PER sentence, i.e. still nested (a "list of lists" shape).
     */
    public static List<List<String>> wordsPerSentenceNested(List<String> sentences) {
        return sentences.stream()
                .map(sentence -> Arrays.asList(sentence.split(" ")))
                .collect(Collectors.toList());
    }

    /**
     * flatMap() version: produces a single flat Stream<String> of every word
     * across every sentence, with the per-sentence boundaries flattened away.
     */
    public static List<String> allWordsFlattened(List<String> sentences) {
        return sentences.stream()
                .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
                .collect(Collectors.toList());
    }

    /**
     * Classic flatMap() example: flatten a List<List<Integer>> into a single
     * Stream<Integer> / List<Integer>. Shown because "flatten a list of
     * lists" is the canonical flatMap() interview prompt.
     */
    public static List<Integer> flattenListOfLists(List<List<Integer>> nested) {
        return nested.stream()
                .flatMap(List::stream) // each inner List<Integer> -> Stream<Integer>
                .collect(Collectors.toList());
    }

    /**
     * Imperative equivalent of flattenListOfLists(), for contrast: requires
     * an explicit nested loop and a mutable accumulator list.
     */
    public static List<Integer> flattenListOfListsImperative(List<List<Integer>> nested) {
        List<Integer> result = new java.util.ArrayList<>();
        for (List<Integer> inner : nested) {
            for (Integer value : inner) {
                result.add(value);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> sentences = Arrays.asList(
                "Java 8 introduced streams",
                "Streams support map and flatMap",
                "flatMap flattens nested structures");

        System.out.println("Input sentences: " + sentences);

        List<List<String>> nested = wordsPerSentenceNested(sentences);
        System.out.println("map() -> nested Stream<List<String>>: " + nested);
        // Expected: [[Java, 8, introduced, streams], [Streams, support, map, and, flatMap], [flatMap, flattens, nested, structures]]

        List<String> flatWords = allWordsFlattened(sentences);
        System.out.println("flatMap() -> flat Stream<String>: " + flatWords);
        // Expected: [Java, 8, introduced, streams, Streams, support, map, and, flatMap, flatMap, flattens, nested, structures]

        List<List<Integer>> nestedInts = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(),
                Arrays.asList(6));
        System.out.println("Nested int lists: " + nestedInts);
        System.out.println("Flattened via flatMap(): " + flattenListOfLists(nestedInts));
        // Expected: [1, 2, 3, 4, 5, 6] (empty inner list contributes nothing)

        System.out.println("Flattened imperatively: " + flattenListOfListsImperative(nestedInts));
        // Expected: [1, 2, 3, 4, 5, 6] (same result, imperative style)

        List<String> edgeCase = Arrays.asList("", "single");
        System.out.println("Edge case sentences: " + edgeCase);
        System.out.println("flatMap() on edge case: " + allWordsFlattened(edgeCase));
        // Expected: ["", single] - splitting "" on " " yields a single empty-string token
    }
}
