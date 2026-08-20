package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PATTERN: Collectors.groupingBy(identity, counting) + filter(count > 1)
 * PRIORITY: P0 - Must Know
 * TOPIC: Find duplicate elements in a List using Java 8 Streams.
 */
public class FindDuplicatesUsingStreams {

    // ================= WHAT IS BEING TESTED =================
    // Ability to build a frequency map with Collectors.groupingBy(Function.identity(), Collectors.counting())
    // and then filter that map down to only the keys whose count is greater than 1.
    // Tests understanding of downstream collectors and how to chain a filter on a Map's entrySet stream.

    // ================= APPROACH =================
    // Plain loop way (for contrast):
    //   1. Create a HashMap<T, Integer> to hold running counts.
    //   2. Iterate the list; for each element, increment its count in the map (getOrDefault + put).
    //   3. Iterate the map; collect keys whose count > 1 into a result Set/List.
    //
    // Stream way:
    //   1. Stream the input list.
    //   2. Collect into a Map<T, Long> using Collectors.groupingBy(Function.identity(), Collectors.counting())
    //      - groupingBy(identity()) groups equal elements (via equals/hashCode) together as keys.
    //      - counting() is the downstream collector that counts elements in each group.
    //   3. Stream the resulting Map's entrySet.
    //   4. filter(entry -> entry.getValue() > 1) to keep only duplicates.
    //   5. map(Map.Entry::getKey) to extract just the duplicated values.
    //   6. collect(Collectors.toSet()) (or toList()) to materialize the result.

    // ================= WHY THIS API =================
    // groupingBy + counting is the idiomatic Java 8 replacement for a manual "count occurrences" map -
    // it is a single, declarative pipeline instead of imperative mutation. counting() avoids boxing pitfalls
    // that come from manually doing map.merge(key, 1, Integer::sum) incorrectly. Using Function.identity()
    // instead of a lambda like x -> x is purely idiomatic/readable, both work identically.

    // ================= COMMON MISTAKES =================
    // 1. Forgetting that groupingBy relies on equals()/hashCode() being correctly implemented on the elements
    //    (a common trap when duplicates are custom objects without overridden equals/hashCode).
    // 2. Using Collectors.toSet() directly on the original list to "find" duplicates - that only gives unique
    //    elements, not which ones were duplicated.
    // 3. Off-by-one on the count threshold: using >= 1 (every element) instead of > 1.
    // 4. Not handling null elements - groupingBy will happily group nulls as a key in a HashMap-backed map,
    //    but NPEs can occur with certain Map implementations (e.g., TreeMap) or if identity() is combined with
    //    a naturally-ordered collector.
    // 5. Assuming insertion order is preserved - the default groupingBy uses a HashMap, so order of the
    //    resulting duplicates is not guaranteed.

    // ================= EDGE CASES =================
    // - Empty input list -> empty result set, no exceptions.
    // - All-same values (e.g., [5,5,5,5]) -> single duplicate value returned once.
    // - Single element list -> no duplicates possible, empty result.
    // - List with no duplicates at all -> empty result.
    // - List containing nulls -> null can be grouped as a key like any other value (HashMap allows one null key).

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - one pass to build the frequency map, one pass over at most n map entries to filter.
    // Space Complexity: O(n) - the frequency map holds up to n distinct keys in the worst case.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you find duplicates while preserving the original order of first occurrence?
    //   (use a LinkedHashMap via groupingBy(identity(), LinkedHashMap::new, counting()))
    // - How would you do this with a single stream pass without an intermediate Map (e.g., using a Set to
    //   detect repeats on the fly)?
    // - What happens if the elements are mutable and their hashCode changes after being added to the map?
    // - How would you count duplicates case-insensitively for Strings?
    // - How would you extend this to find elements that occur exactly N times?
    // - Is this approach thread-safe / parallelizable? (groupingBy has a parallel-friendly toConcurrentMap variant)
    // - What's the difference between Collectors.counting() and Collectors.summingInt(e -> 1)?

    /**
     * Loop Approach: manual frequency map + manual filter.
     */
    public static <T> Set<T> findDuplicatesLoop(List<T> input) {
        Map<T, Integer> countMap = new HashMap<>();
        // Step 1: count occurrences manually
        for (T item : input) {
            countMap.put(item, countMap.getOrDefault(item, 0) + 1);
        }
        // Step 2: collect keys with count > 1
        Set<T> duplicates = new HashSet<>();
        for (Map.Entry<T, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }

    /**
     * Java 8 Streams Approach: groupingBy(identity(), counting()) then filter count > 1.
     */
    public static <T> Set<T> findDuplicatesStream(List<T> input) {
        // Step 1: build frequency map T -> Long count
        Map<T, Long> frequencyMap = input.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Step 2: filter entries with count > 1 and extract keys
        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 5, 1, 1);
        System.out.println("Loop duplicates: " + findDuplicatesLoop(numbers));
        System.out.println("Stream duplicates: " + findDuplicatesStream(numbers));
        // Expected: [1, 2] (order may vary, backed by HashSet)

        List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "banana", "banana");
        System.out.println("Stream duplicates (words): " + findDuplicatesStream(words));
        // Expected: [apple, banana] (order may vary)

        List<Integer> empty = new ArrayList<>();
        System.out.println("Stream duplicates (empty): " + findDuplicatesStream(empty));
        // Expected: [] (empty set)

        List<Integer> allSame = Arrays.asList(7, 7, 7, 7);
        System.out.println("Stream duplicates (all same): " + findDuplicatesStream(allSame));
        // Expected: [7]

        List<Integer> single = Arrays.asList(42);
        System.out.println("Stream duplicates (single element): " + findDuplicatesStream(single));
        // Expected: [] (no duplicates possible with one element)
    }
}
