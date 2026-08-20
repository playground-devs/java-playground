package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PATTERN: Generics (Bounded Types, Generic Classes, Wildcards / PECS)
 * PRIORITY: P1 - High Priority
 * TOPIC: Bounded type parameters, a generic Pair<K,V> class, and producer/consumer wildcards.
 */
public class GenericsBoundedTypesDemo {

    // ================= WHAT IS BEING TESTED =================
    // Depth of understanding of Java generics beyond basic `List<String>` usage: bounded
    // type parameters (`<T extends Comparable<T>>`), writing a reusable generic class, and
    // correctly applying wildcard bounds (`? extends`, `? super`) per the PECS principle.

    // ================= APPROACH =================
    // 1. Write a generic bounded-type method `max(List<T>)` where T is constrained to
    //    `Comparable<T>`, so the method body can call compareTo() safely at compile time.
    // 2. Write a generic class Pair<K, V> holding two heterogeneous typed values, with
    //    type-safe getters and a generic static factory method (`of`).
    // 3. Demonstrate wildcards:
    //    - `List<? extends Number>` as a PRODUCER (read-only source) - you can safely read
    //      Numbers out of it, but cannot add to it (compiler doesn't know the exact subtype).
    //    - `List<? super Integer>` as a CONSUMER (write-only target) - you can safely add
    //      Integers into it, but reads only guarantee an Object.
    // 4. State PECS explicitly: Producer Extends, Consumer Super - a mnemonic for choosing
    //    the correct wildcard direction based on whether the collection is read-from or written-to.

    // ================= WHY THIS MATTERS =================
    // Generics are the backbone of type-safe collection APIs, repository/DAO layers
    // (`Repository<T, ID>`), and reusable utility methods in backend codebases. Getting
    // wildcard variance wrong either over-restricts APIs (forcing callers to use exact
    // types) or under-restricts them (allowing heap pollution / unsafe raw-type usage).
    // PECS is a routine senior-level interview and code-review topic because it directly
    // affects how flexible and safe a public API's generic method signatures are.

    // ================= COMMON MISTAKES =================
    // - Using raw types (`List` instead of `List<T>`) -> loses all compile-time type safety and emits unchecked warnings.
    // - Trying to `add()` to a `List<? extends Number>` -> compile error, because the compiler cannot guarantee the runtime type matches (this is by design, not a bug to "work around" with casts).
    // - Forgetting the bound on a generic method (`<T> T max(List<T> list)` without `extends Comparable<T>`) -> cannot call compareTo() inside the method body.
    // - Mixing up PECS direction -- using `? super` for a read-heavy producer parameter or `? extends` for a write-heavy consumer parameter, which either doesn't compile or unnecessarily restricts callers.

    // Generic bounded-type method: T must be Comparable to itself so compareTo() is legal.
    static <T extends Comparable<T>> T max(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("list must not be null or empty");
        }
        T candidate = list.get(0);
        for (T item : list) {
            if (item.compareTo(candidate) > 0) {
                candidate = item;
            }
        }
        return candidate;
    }

    // Generic class: holds two independently-typed values.
    static class Pair<K, V> {
        private final K key;
        private final V value;

        private Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Pair{" + key + " -> " + value + "}";
        }
    }

    // PECS - Producer Extends: this list is only ever READ from (a "producer" of Numbers),
    // so `? extends Number` lets callers pass List<Integer>, List<Double>, etc.
    static double sumOf(List<? extends Number> producer) {
        double sum = 0.0;
        for (Number n : producer) { // safe: every element is guaranteed to be at least a Number
            sum += n.doubleValue();
        }
        // producer.add(42); // would NOT compile - compiler can't guarantee the actual list type
        return sum;
    }

    // PECS - Consumer Super: this list is only ever WRITTEN to (a "consumer" of Integers),
    // so `? super Integer` lets callers pass List<Integer>, List<Number>, List<Object>, etc.
    static void addOneToTen(List<? super Integer> consumer) {
        for (int i = 1; i <= 10; i++) {
            consumer.add(i); // safe: every possible target type can accept an Integer
        }
        // Integer first = (Integer) consumer.get(0); // reading requires an unsafe cast -
        // the compiler only guarantees elements are AT LEAST Object.
    }

    // ================= EDGE CASES =================
    // - Empty list passed to `max()` -> handled explicitly with IllegalArgumentException
    //   rather than an ArrayIndexOutOfBoundsException.
    // - `List<? extends Number>` passed an empty list -> sumOf() correctly returns 0.0.
    // - Pair<K,V> with a null key or value -> allowed (no null-checks enforced here); a
    //   stricter production implementation might reject nulls via Objects.requireNonNull().
    // - Passing `List<Object>` to `addOneToTen(List<? super Integer>)` -> legal and safe,
    //   since Object is a valid supertype of Integer.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for max() and sumOf() (single pass over the list, n = size);
    //                  O(1) for Pair construction/access; O(k) for addOneToTen (k = 10 fixed).
    // Space Complexity: O(1) additional space for max()/sumOf() (excluding the input list
    //                  itself); O(1) for Pair (fixed two references).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Explain PECS (Producer Extends, Consumer Super) with a concrete example of each.
    // - Why can't you add elements to a `List<? extends Number>`?
    // - What is type erasure, and how does it affect what you can and cannot do with generics at runtime (e.g. `new T[]`, `instanceof T`)?
    // - Why is `List<Object>` NOT a supertype of `List<String>`, even though `String` is a subtype of `Object`?
    // - How would you write a generic method that accepts and returns the same bounded type, ensuring type safety without wildcards?
    // - What are unbounded wildcards (`List<?>`) used for, and how do they differ from raw types (`List`)?
    // - How do bounded type parameters (`<T extends SomeClass & SomeInterface>`) support multiple bounds, and what are the restrictions (at most one class, plus any number of interfaces)?

    public static void main(String[] args) {
        // Example 1: bounded generic method - find max in a List<Integer> and List<String>.
        List<Integer> numbers = Arrays.asList(4, 9, 2, 7, 5);
        System.out.println("max(numbers) = " + max(numbers)); // Expected: 9

        List<String> words = Arrays.asList("marriott", "hilton", "hyatt");
        System.out.println("max(words) = " + max(words)); // Expected: marriott (lexicographically largest)

        // Example 2: generic Pair<K, V> class.
        Pair<String, Integer> employeeAgePair = Pair.of("Asha", 34);
        System.out.println("employeeAgePair = " + employeeAgePair); // Expected: Pair{Asha -> 34}
        Pair<Integer, List<String>> idToRoles = Pair.of(101, Arrays.asList("ADMIN", "AUDITOR"));
        System.out.println("idToRoles = " + idToRoles); // Expected: Pair{101 -> [ADMIN, AUDITOR]}

        // Example 3: PECS wildcards - producer (? extends Number) and consumer (? super Integer).
        List<Integer> intList = Arrays.asList(1, 2, 3);
        List<Double> doubleList = Arrays.asList(1.5, 2.5, 3.5);
        System.out.println("sumOf(intList) = " + sumOf(intList));       // Expected: 6.0
        System.out.println("sumOf(doubleList) = " + sumOf(doubleList)); // Expected: 7.5

        List<Number> numberConsumer = new ArrayList<>();
        addOneToTen(numberConsumer); // List<Number> accepts Integer via ? super Integer
        System.out.println("numberConsumer = " + numberConsumer);
        // Expected: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

        List<Object> objectConsumer = new ArrayList<>();
        addOneToTen(objectConsumer); // List<Object> also accepts Integer via ? super Integer
        System.out.println("objectConsumer = " + objectConsumer);
        // Expected: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    }
}
