package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PATTERN: Collectors.groupingBy(identity, counting) to build a frequency Map
 * PRIORITY: P0 - Must Know
 * TOPIC: Frequency count of elements in a List (words / characters) using Streams.
 */
public class FrequencyCountUsingStreams {

    // ================= WHAT IS BEING TESTED =================
    // Whether the candidate can produce a frequency map (element -> occurrence count) for both a list of
    // words and the characters of a String, using Collectors.groupingBy + Collectors.counting - one of the
    // most commonly asked "quick Java 8" warm-up questions.

    // ================= APPROACH =================
    // Plain loop way (for contrast):
    //   1. Create a Map<T, Integer> (or Long) to accumulate counts.
    //   2. Iterate the collection.
    //   3. For each element, do map.put(element, map.getOrDefault(element, 0) + 1) or map.merge(element, 1, Integer::sum).
    //   4. Print/return the map.
    //
    // Stream way (word frequency from a List<String>):
    //   1. Stream the list of words.
    //   2. collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    //      - groups equal words together, counts each group's size.
    //   3. Resulting Map<String, Long> has each word mapped to its frequency.
    //
    // Stream way (character frequency from a String):
    //   1. Convert the String to a stream of characters: str.chars() (returns IntStream of code points).
    //   2. mapToObj(c -> (char) c) to box each int code point back into a Character.
    //   3. collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).
    //   4. Resulting Map<Character, Long> has each character mapped to its frequency.

    // ================= WHY THIS API =================
    // str.chars() avoids manually splitting a String into a char[] and looping with an index. groupingBy +
    // counting turns two operations (bucket + tally) into one declarative collector, and returns exactly the
    // Map shape most interviewers expect (element -> count) with no boilerplate mutable state.

    // ================= COMMON MISTAKES =================
    // 1. Forgetting that String.chars() returns an IntStream, not Stream<Character> - must mapToObj to box.
    // 2. Not considering case sensitivity - "A" and "a" are counted separately unless normalized first
    //    (e.g., toLowerCase()).
    // 3. Including whitespace/punctuation in character frequency when only alphabetic frequency is wanted -
    //    should filter with Character::isLetter if that's the intent.
    // 4. Using Collectors.toMap(identity(), e -> 1, Integer::sum) instead of groupingBy+counting - it works,
    //    but is more verbose and easier to get the merge function wrong on.
    // 5. Expecting a specific iteration order from the default HashMap-backed result when the interviewer
    //    wants insertion order (need LinkedHashMap::new as the map factory).

    // ================= EDGE CASES =================
    // - Empty list/String -> empty map, no exceptions.
    // - All-same values (e.g., "aaaa") -> single key with count equal to length.
    // - Single element/character -> map with one entry, count = 1.
    // - Ties in frequency (multiple elements with the same max count) - frequency map itself doesn't need to
    //   resolve ties; only relevant if asked to find "the most frequent element".
    // - Null elements in the list - groupingBy on a HashMap allows one null key, but will NPE if the map
    //   factory used is a TreeMap or if further natural-ordering operations are applied to the null key.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) where n is the number of elements (words or characters) - single pass to build
    // the frequency map.
    // Space Complexity: O(k) where k is the number of distinct elements (at most n).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you find the single most frequent word/character from this map? (stream entrySet, max by
    //   Map.Entry.comparingByValue())
    // - How would you make character counting case-insensitive?
    // - How would you preserve insertion order of first occurrence in the resulting map?
    // - How would you sort the frequency map by value descending for a "top K frequent" style question?
    // - What's the difference between Collectors.counting() and manually using merge(key, 1L, Long::sum)?
    // - How would you parallelize this for a very large input (parallelStream + groupingByConcurrent)?
    // - How would you count frequency of words in a sentence while ignoring punctuation?

    /**
     * Loop Approach: manual word frequency count.
     */
    public static Map<String, Integer> wordFrequencyLoop(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        // Step 1: iterate and increment count for each word
        for (String word : words) {
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        return freq;
    }

    /**
     * Java 8 Streams Approach: word frequency using groupingBy + counting.
     */
    public static Map<String, Long> wordFrequencyStream(List<String> words) {
        // Step 1: group identical words together, step 2: count each group
        return words.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * Java 8 Streams Approach: word frequency preserving insertion order (LinkedHashMap).
     */
    public static Map<String, Long> wordFrequencyStreamOrdered(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    }

    /**
     * Loop Approach: manual character frequency count.
     */
    public static Map<Character, Integer> charFrequencyLoop(String text) {
        Map<Character, Integer> freq = new HashMap<>();
        // Step 1: iterate characters by index and increment count
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        return freq;
    }

    /**
     * Java 8 Streams Approach: character frequency using chars() + groupingBy + counting.
     */
    public static Map<Character, Long> charFrequencyStream(String text) {
        // Step 1: text.chars() -> IntStream of char code points
        // Step 2: mapToObj to box each int into a Character
        // Step 3: groupingBy(identity(), counting()) to tally occurrences
        return text.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "banana", "apple");
        System.out.println("Loop word frequency: " + wordFrequencyLoop(words));
        System.out.println("Stream word frequency: " + wordFrequencyStream(words));
        // Expected: {apple=3, banana=2, cherry=1} (order may vary with HashMap)
        System.out.println("Ordered word frequency: " + wordFrequencyStreamOrdered(words));
        // Expected: {apple=3, banana=2, cherry=1} (insertion order preserved)

        String text = "mississippi";
        System.out.println("Loop char frequency: " + charFrequencyLoop(text));
        System.out.println("Stream char frequency: " + charFrequencyStream(text));
        // Expected: {m=1, i=4, s=4, p=2} (order may vary with HashMap)

        List<String> empty = new ArrayList<>();
        System.out.println("Stream word frequency (empty): " + wordFrequencyStream(empty));
        // Expected: {} (empty map)

        String single = "z";
        System.out.println("Stream char frequency (single char): " + charFrequencyStream(single));
        // Expected: {z=1}
    }
}
