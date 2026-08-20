package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * PATTERN: distinct() + sorted() + skip(1)/limit chaining on a boxed IntStream/Stream
 * PRIORITY: P0 - Must Know
 * TOPIC: Find the second highest and second lowest number in a List of Integers using Streams.
 */
public class SecondHighestLowestUsingStreams {

    // ================= WHAT IS BEING TESTED =================
    // Whether the candidate correctly reasons about DISTINCT VALUES rather than distinct positions when
    // asked for "second highest/lowest" - e.g., in [5, 5, 4], the second highest by value is 4, not the
    // second 5. Tests correct use of distinct(), sorted() (both ascending and via Comparator.reverseOrder()),
    // and safe handling of Optional results when there are fewer than 2 distinct values.

    // ================= APPROACH =================
    // Plain loop way (for contrast):
    //   1. Track two variables: highest and secondHighest, initialized to Integer.MIN_VALUE (or null/sentinel).
    //   2. Iterate the list once. For each number:
    //        - if number > highest: secondHighest = highest; highest = number
    //        - else if number > secondHighest AND number != highest: secondHighest = number
    //   3. This correctly skips duplicates of the highest value. Mirror logic (with MAX_VALUE and < ) for
    //      second lowest.
    //
    // Stream way:
    //   1. Stream the list of Integers: list.stream()
    //   2. distinct() - remove duplicate VALUES so repeated max/min values don't count twice.
    //   3. For second highest: sorted(Comparator.reverseOrder()) to sort distinct values descending.
    //      For second lowest: sorted() (natural ascending order) on distinct values.
    //   4. skip(1) - discard the first element (the highest, or the lowest) leaving the second-ranked value
    //      at the front of the remaining stream.
    //   5. findFirst() - returns an Optional<Integer>: present if there were at least 2 distinct values,
    //      empty otherwise.
    //   6. Handle the Optional explicitly (isPresent()/orElse/orElseThrow) - never call get() blindly.

    // ================= WHY THIS API =================
    // distinct() before sorted() is what makes this correct for lists with duplicate max/min values - without
    // it, "second highest" of [9, 9, 8] would incorrectly return 9 instead of 8. skip(1).findFirst() is a
    // clean, allocation-light way to grab the 2nd element of an already-sorted stream without materializing
    // the whole sorted list, letting the terminal operation short-circuit.

    // ================= COMMON MISTAKES =================
    // 1. Sorting without distinct() first, returning the same value twice as "1st and 2nd highest".
    // 2. Calling Optional.get() directly without checking presence, causing NoSuchElementException on
    //    lists with fewer than 2 distinct values.
    // 3. Using skip(1).findFirst() correctly, but on the wrong sort order (ascending when descending was
    //    needed, or vice versa) - easy to swap "highest" and "lowest" logic.
    // 4. Assuming boxed Integer sorted() with natural order handles nulls - it throws NullPointerException
    //    on comparison with a null element.
    // 5. Off-by-one: using limit(2) and taking the last element via a collect instead of the simpler skip(1).

    // ================= EDGE CASES =================
    // - Empty list -> Optional.empty() for both second highest and second lowest.
    // - Single element list -> only 1 distinct value, so second highest/lowest is Optional.empty().
    // - All-same values (e.g., [7,7,7]) -> only 1 distinct value -> Optional.empty(), NOT 7 again.
    // - Ties for second highest (e.g., [10, 9, 9, 8]) -> distinct() collapses the tie, second highest is 9
    //   (appearing once in the distinct stream), which is the expected/only sensible answer.
    // - List containing nulls -> must be filtered out before distinct()/sorted() to avoid NPE during
    //   comparison (Comparator.reverseOrder() and natural order both dereference the values).

    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) - dominated by the sorted() step; distinct() is O(n) via a hash-based
    // dedupe internally.
    // Space Complexity: O(n) - distinct()/sorted() need to buffer elements internally to produce ordered,
    // deduplicated output (stream operations that are not purely element-wise require buffering).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you find the second highest in O(n) instead of O(n log n)? (single-pass loop tracking
    //   top-2 distinct values, as shown in the loop approach above)
    // - How would you generalize this to find the Kth highest distinct value?
    // - How does distinct() determine equality for custom objects (relies on equals()/hashCode())?
    // - What would happen here if you used a TreeSet instead of streams? (TreeSet dedupes and sorts
    //   automatically; second highest = second element from the descending iterator)
    // - Why is skip(1).findFirst() preferred here over collecting to a list and indexing [1]?
    // - How would you handle nulls gracefully in the pipeline?
    // - Is sorted() on a stream stable, and does that matter for primitive Integers here?

    /**
     * Loop Approach: single-pass O(n) tracking of top-2 and bottom-2 distinct values.
     */
    public static Optional<Integer> secondHighestLoop(List<Integer> numbers) {
        Integer highest = null;
        Integer secondHighest = null;
        // Step 1: single pass, track highest and secondHighest distinct values
        for (Integer num : numbers) {
            if (num == null) {
                continue; // skip nulls
            }
            if (highest == null || num > highest) {
                secondHighest = highest;
                highest = num;
            } else if (!num.equals(highest) && (secondHighest == null || num > secondHighest)) {
                secondHighest = num;
            }
        }
        return Optional.ofNullable(secondHighest);
    }

    public static Optional<Integer> secondLowestLoop(List<Integer> numbers) {
        Integer lowest = null;
        Integer secondLowest = null;
        // Step 1: single pass, track lowest and secondLowest distinct values
        for (Integer num : numbers) {
            if (num == null) {
                continue; // skip nulls
            }
            if (lowest == null || num < lowest) {
                secondLowest = lowest;
                lowest = num;
            } else if (!num.equals(lowest) && (secondLowest == null || num < secondLowest)) {
                secondLowest = num;
            }
        }
        return Optional.ofNullable(secondLowest);
    }

    /**
     * Java 8 Streams Approach: second highest via distinct + sorted(reverse) + skip(1) + findFirst.
     */
    public static Optional<Integer> secondHighestStream(List<Integer> numbers) {
        return numbers.stream()
                .filter(n -> n != null)              // Step 1: guard against nulls before comparisons
                .distinct()                          // Step 2: dedupe by VALUE, not position
                .sorted(Comparator.reverseOrder())    // Step 3: sort distinct values descending
                .skip(1)                             // Step 4: drop the highest, leaving 2nd highest first
                .findFirst();                        // Step 5: Optional.empty() if < 2 distinct values
    }

    /**
     * Java 8 Streams Approach: second lowest via distinct + sorted (natural) + skip(1) + findFirst.
     */
    public static Optional<Integer> secondLowestStream(List<Integer> numbers) {
        return numbers.stream()
                .filter(n -> n != null)   // Step 1: guard against nulls before comparisons
                .distinct()               // Step 2: dedupe by VALUE, not position
                .sorted()                 // Step 3: sort distinct values ascending (natural order)
                .skip(1)                  // Step 4: drop the lowest, leaving 2nd lowest first
                .findFirst();             // Step 5: Optional.empty() if < 2 distinct values
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(10, 5, 8, 20, 20, 15, 5);
        System.out.println("Loop 2nd highest: " + secondHighestLoop(numbers));
        System.out.println("Stream 2nd highest: " + secondHighestStream(numbers));
        // Expected: Optional[15] (20 is highest, duplicated but distinct() collapses it)
        System.out.println("Loop 2nd lowest: " + secondLowestLoop(numbers));
        System.out.println("Stream 2nd lowest: " + secondLowestStream(numbers));
        // Expected: Optional[8] (5 is lowest, duplicated but distinct() collapses it)

        List<Integer> allSame = Arrays.asList(7, 7, 7, 7);
        System.out.println("Stream 2nd highest (all same): " + secondHighestStream(allSame));
        // Expected: Optional.empty (only one distinct value)

        List<Integer> single = Arrays.asList(42);
        System.out.println("Stream 2nd highest (single element): " + secondHighestStream(single));
        // Expected: Optional.empty

        List<Integer> empty = new ArrayList<>();
        System.out.println("Stream 2nd highest (empty): " + secondHighestStream(empty));
        // Expected: Optional.empty

        List<Integer> withTieForSecond = Arrays.asList(10, 9, 9, 8);
        System.out.println("Stream 2nd highest (tie for 2nd): " + secondHighestStream(withTieForSecond));
        // Expected: Optional[9] (distinct collapses the tie, only one 9 remains)
    }
}
