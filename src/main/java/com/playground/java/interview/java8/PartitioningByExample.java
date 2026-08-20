package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams / Collectors
 * PRIORITY: P1 - High Priority
 * TOPIC: Partition a List<Integer> into even/odd, and a List<Employee> into
 * salary >= threshold vs below, using Collectors.partitioningBy().
 */
public class PartitioningByExample {

    // ================= WHAT IS BEING TESTED =================
    // This tests knowledge of Collectors.partitioningBy(), a specialized form
    // of grouping where the classifier is always a boolean predicate, so the
    // result is always exactly a Map<Boolean, List<T>> with precisely two
    // keys (true and false) - unlike groupingBy() which can produce an
    // arbitrary number of keys. Interviewers ask this to see if a candidate
    // knows when partitioningBy() is the more expressive/efficient choice
    // over groupingBy() for a strictly binary split (e.g. pass/fail,
    // above/below threshold, even/odd).

    // ================= APPROACH =================
    // 1. numbers.stream() -> obtain a Stream<Integer> from the source list.
    // 2. collect(Collectors.partitioningBy(n -> n % 2 == 0))
    //    -> terminal op that evaluates the predicate "is this number even?"
    //    for every element and buckets each one under key TRUE (even) or
    //    FALSE (odd) in the resulting Map<Boolean, List<Integer>>.
    // 3. Same idea for employees: collect(Collectors.partitioningBy(
    //    e -> e.getSalary() >= threshold)) buckets each Employee under TRUE
    //    (salary at/above threshold) or FALSE (salary below threshold).
    // 4. The resulting map always contains both the TRUE and FALSE keys, even
    //    if one bucket ends up empty, which distinguishes it from groupingBy().

    // ================= WHY THIS API =================
    // partitioningBy() communicates intent more precisely than groupingBy()
    // when the split is inherently binary - it guarantees exactly two keys
    // (true/false) are always present in the output map, which callers can
    // rely on without null/missing-key checks, and internally it can be
    // marginally more efficient since it doesn't need general-purpose hashing
    // of arbitrary keys. The imperative alternative would require two
    // separate ArrayLists and an if/else inside a for-loop to route each
    // element - straightforward, but partitioningBy() avoids the ceremony of
    // declaring and managing two mutable lists manually. You would still
    // prefer an explicit loop if you need more than a binary split (that's
    // groupingBy() territory), or if the partitioning logic depends on
    // stateful/order-sensitive computation that a stateless predicate can't
    // express cleanly.

    // ================= COMMON MISTAKES =================
    // - Using groupingBy(predicate) instead of partitioningBy(predicate) -
    //   it "works" for a boolean classifier, but loses the guarantee that
    //   both true/false keys are always present and is less idiomatic.
    // - Assuming a missing bucket means "no such key" - partitioningBy()
    //   always returns both keys, even mapping to an empty list, so
    //   map.get(true) never returns null (unlike groupingBy(), where a key
    //   with zero matches simply won't appear at all).
    // - Using boxed Boolean.TRUE/FALSE inconsistently when looking up results,
    //   though map.get(true) auto-boxes correctly in practice.
    // - Forgetting to also apply a downstream collector (partitioningBy has a
    //   two-arg overload just like groupingBy) when you need e.g. counts per
    //   partition instead of the full List<T>.

    // ================= EDGE CASES =================
    // - Empty input list: partitioningBy() still returns a map with both
    //   TRUE and FALSE keys mapped to empty lists, never an empty map itself.
    // - All elements go to one partition (e.g. all numbers even): the other
    //   key's value is simply an empty list, not absent.
    // - Threshold exactly equal to an employee's salary: using >= means a
    //   salary exactly at the threshold lands in the TRUE (at-or-above)
    //   bucket - be explicit about >= vs > semantics in interview discussion.
    // - Single-element list: still produces a two-key map, with one bucket
    //   holding the single element and the other empty.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each element is evaluated against the predicate
    // exactly once and routed to its partition.
    // Space Complexity: O(n) - overall, every input element ends up in
    // exactly one of the two output lists, so total retained elements equal
    // the input size (unlike some groupingBy() downstream variants that
    // reduce to O(k) aggregates).
    // Streams are lazy: the predicate isn't evaluated against any element
    // until the terminal collect(Collectors.partitioningBy(...)) call runs.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why does partitioningBy() always return exactly two keys, unlike
    //   groupingBy()? What Java class/interface backs this guarantee
    //   (Collectors.Partition internally, not a general Map)?
    // - How would you combine partitioningBy() with a downstream collector,
    //   e.g. partitioningBy(predicate, counting()) to get counts per bucket
    //   instead of the full element lists?
    // - When would you choose groupingBy(predicate) over partitioningBy(),
    //   if ever?
    // - How would you partition employees into three or more salary bands
    //   instead of just two? (Answer: that's a groupingBy() problem, not
    //   partitioningBy(), since partitioningBy is strictly binary.)
    // - What's the performance characteristic of partitioningBy() vs
    //   groupingBy() for a purely boolean classifier?
    // - How would you retrieve just the "true" partition safely without
    //   worrying about null, given partitioningBy()'s guarantees?

    /** Simple POJO reused for the salary-threshold partitioning example. */
    static class Employee {
        private final String name;
        private final double salary;

        Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
        }

        double getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + "($" + salary + ")";
        }
    }

    /** Partition a List<Integer> into evens (true) and odds (false). */
    public static Map<Boolean, List<Integer>> partitionEvenOdd(List<Integer> numbers) {
        return numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
    }

    /** Partition employees by whether salary is at/above (true) or below (false) threshold. */
    public static Map<Boolean, List<Employee>> partitionBySalaryThreshold(
            List<Employee> employees, double threshold) {
        return employees.stream()
                .collect(Collectors.partitioningBy(e -> e.getSalary() >= threshold));
    }

    /**
     * Variant with a downstream collector: partition employees by salary
     * threshold, but count how many fall in each bucket instead of listing
     * them, using the two-arg partitioningBy(predicate, downstream) overload.
     */
    public static Map<Boolean, Long> countBySalaryThreshold(
            List<Employee> employees, double threshold) {
        return employees.stream()
                .collect(Collectors.partitioningBy(
                        e -> e.getSalary() >= threshold, Collectors.counting()));
    }

    /** Imperative equivalent of partitionEvenOdd(), shown for contrast. */
    public static Map<Boolean, List<Integer>> partitionEvenOddImperative(List<Integer> numbers) {
        List<Integer> evens = new java.util.ArrayList<>();
        List<Integer> odds = new java.util.ArrayList<>();
        for (Integer n : numbers) {
            if (n % 2 == 0) {
                evens.add(n);
            } else {
                odds.add(n);
            }
        }
        Map<Boolean, List<Integer>> result = new java.util.HashMap<>();
        result.put(true, evens);
        result.put(false, odds);
        return result;
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("Input numbers: " + numbers);

        Map<Boolean, List<Integer>> evenOdd = partitionEvenOdd(numbers);
        System.out.println("partitionEvenOdd = " + evenOdd);
        // Expected: {false=[1, 3, 5, 7, 9], true=[2, 4, 6, 8, 10]}

        System.out.println("Imperative equivalent = " + partitionEvenOddImperative(numbers));
        // Expected: same as above -> {false=[1, 3, 5, 7, 9], true=[2, 4, 6, 8, 10]}

        List<Employee> employees = Arrays.asList(
                new Employee("Alice", 120_000),
                new Employee("Bob", 95_000),
                new Employee("Carol", 100_000),
                new Employee("Dave", 80_000));
        double threshold = 100_000;
        System.out.println("Input employees: " + employees + ", threshold = " + threshold);

        Map<Boolean, List<Employee>> bySalary = partitionBySalaryThreshold(employees, threshold);
        System.out.println("partitionBySalaryThreshold = " + bySalary);
        // Expected: {false=[Bob($95000.0), Dave($80000.0)], true=[Alice($120000.0), Carol($100000.0)]}
        // Note: Carol's salary equals the threshold exactly and lands in true (>= semantics)

        Map<Boolean, Long> countsBySalary = countBySalaryThreshold(employees, threshold);
        System.out.println("countBySalaryThreshold = " + countsBySalary);
        // Expected: {false=2, true=2}

        List<Integer> emptyList = Arrays.asList();
        System.out.println("Empty list -> partitionEvenOdd = " + partitionEvenOdd(emptyList));
        // Expected: {false=[], true=[]} (both keys always present, even when empty)
    }
}
