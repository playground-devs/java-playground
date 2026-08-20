package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams / Comparator
 * PRIORITY: P1 - High Priority
 * TOPIC: Sort a List<Employee> using Comparator.comparing(), thenComparing()
 * for tie-breaking, reversed(), and comparingInt() to avoid autoboxing.
 */
public class SortingWithComparatorStreams {

    // ================= WHAT IS BEING TESTED =================
    // This tests fluency with the Java 8 Comparator factory/combinator
    // methods - comparing(), thenComparing(), reversed(), and the primitive
    // specializations like comparingInt()/comparingDouble() - used together
    // with Stream.sorted() to express multi-key sort logic declaratively.
    // Interviewers ask this because building a Comparator by hand
    // (implementing compare() with nested if/else on multiple fields) is
    // verbose and error-prone, and the Java 8 combinator style is now the
    // expected idiom for anything beyond a trivial single-field sort.

    // ================= APPROACH =================
    // 1. employees.stream() -> obtain a Stream<Employee>.
    // 2. sorted(Comparator.comparing(Employee::getDepartment)
    //        .thenComparing(Employee::getName))
    //    -> sorted() is an intermediate op; the Comparator built via
    //    comparing() (primary key: department) chained with thenComparing()
    //    (tie-breaker key: name) resolves ties within the same department by
    //    name, ascending on both keys.
    // 3. .reversed() -> flips a previously built Comparator's ordering,
    //    e.g. Comparator.comparing(Employee::getSalary).reversed() sorts by
    //    salary descending (highest first).
    // 4. Comparator.comparingInt(Employee::getAge) -> a primitive-specialized
    //    comparator factory that compares int values directly without boxing
    //    each Integer for the comparison, unlike comparing(Employee::getAge)
    //    which would implicitly box the int return type into an Integer.
    // 5. collect(Collectors.toList()) materializes the sorted stream into a
    //    new List, leaving the original list unmodified (Stream.sorted() does
    //    not sort in place).

    // ================= WHY THIS API =================
    // Comparator.comparing().thenComparing().reversed() lets you compose a
    // multi-key sort declaratively, key by key, instead of hand-writing a
    // compare(Employee a, Employee b) method with manual field-by-field
    // if/else logic and explicit tie-breaking - the combinator chain reads
    // top-to-bottom as "sort by X, then by Y, descending" which mirrors how
    // you'd describe the requirement in English. Prefer comparingInt()/
    // comparingDouble()/comparingLong() over comparing() with a primitive-
    // returning key extractor to avoid unnecessary autoboxing overhead on
    // large datasets. You would still write a custom Comparator class (or an
    // explicit loop-based sort) if the comparison logic is highly complex,
    // stateful, or reused across many call sites in a way that benefits from
    // being named and unit-tested as its own type rather than an inline chain.

    // ================= COMMON MISTAKES =================
    // - Using Comparator.comparing(Employee::getAge) where getAge() returns a
    //   primitive int, causing unnecessary autoboxing; comparingInt() avoids
    //   this and is the more efficient, idiomatic choice for primitive keys.
    // - Calling .reversed() on the wrong part of the chain, e.g. reversing
    //   only the primary key when the intent was to reverse the entire
    //   composed comparator, producing subtly wrong tie-break ordering.
    // - Assuming Stream.sorted() mutates the original list in place - it does
    //   not; the original list is left untouched, and the sorted result must
    //   be captured via a terminal operation like collect().
    // - Forgetting that natural ordering (e.g. Comparator.naturalOrder()) or
    //   comparing() with a key extractor that returns null will throw
    //   NullPointerException during comparison unless nullsFirst()/
    //   nullsLast() wrappers are used.

    // ================= EDGE CASES =================
    // - Empty list: sorted() returns an empty stream/list, no exception.
    // - Single-element list: trivially "sorted" already, no comparisons made.
    // - All elements have the same primary key (e.g. same department): the
    //   thenComparing() tie-breaker fully determines the final order.
    // - Duplicate elements/ties on every key: sorted() is stable (per the
    //   Collections.sort()/Arrays.sort() contract for objects), so
    //   originally-equal elements retain their relative input order.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) - Stream.sorted() with a Comparator uses a
    // stable merge/Tim-sort-style algorithm under the hood (same as
    // Arrays.sort() for objects), regardless of how many keys the composed
    // Comparator chains together (each comparison is still O(1) per pair,
    // assuming O(1) key extraction).
    // Space Complexity: O(n) - sorted() must buffer all elements to sort
    // them (unlike filter/map, sorting is a stateful intermediate operation),
    // plus O(n) for the materialized result list from collect().
    // Streams are lazy: even though sorted() must buffer the whole stream
    // internally to sort it, the buffering doesn't happen until a terminal
    // operation (here, collect()) triggers execution of the pipeline.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is Stream.sorted() considered a "stateful" intermediate
    //   operation, unlike map()/filter()? What does that imply about memory
    //   use and laziness?
    // - What's the difference between Comparator.comparing(keyExtractor) and
    //   Comparator.comparing(keyExtractor, keyComparator) (the two-arg
    //   overload)?
    // - How would you sort nulls first or last using
    //   Comparator.nullsFirst()/nullsLast()?
    // - Is Stream.sorted() guaranteed to be a stable sort? Why does that
    //   matter for thenComparing() tie-breaking to behave predictably?
    // - How would you sort a list in place instead of via a Stream (i.e.
    //   List.sort(Comparator))? When would that be preferable to
    //   stream().sorted().collect(...)?
    // - Why does comparingInt()/comparingLong()/comparingDouble() exist
    //   separately from comparing() - what's the actual performance
    //   difference for large datasets?

    /** Simple POJO reused for the sorting examples. */
    static class Employee {
        private final String name;
        private final String department;
        private final int age;
        private final double salary;

        Employee(String name, String department, int age, double salary) {
            this.name = name;
            this.department = department;
            this.age = age;
            this.salary = salary;
        }

        String getName() {
            return name;
        }

        String getDepartment() {
            return department;
        }

        int getAge() {
            return age;
        }

        double getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + "(" + department + ", age " + age + ", $" + salary + ")";
        }
    }

    /** Sort by department (primary key), then by name (tie-breaker), both ascending. */
    public static List<Employee> sortByDepartmentThenName(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getDepartment)
                        .thenComparing(Employee::getName))
                .collect(Collectors.toList());
    }

    /** Sort by salary descending (highest first) using reversed(). */
    public static List<Employee> sortBySalaryDescending(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .collect(Collectors.toList());
    }

    /** Sort by age ascending using comparingInt() to avoid autoboxing the primitive int. */
    public static List<Employee> sortByAgeAscendingNoBoxing(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparingInt(Employee::getAge))
                .collect(Collectors.toList());
    }

    /**
     * Combined example: sort by department ascending, then by salary
     * descending within each department (mixing an ascending primary key
     * with a reversed secondary key via thenComparing(comparator)).
     */
    public static List<Employee> sortByDepartmentThenSalaryDescending(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getDepartment)
                        .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()))
                .collect(Collectors.toList());
    }

    /** Imperative equivalent of sortBySalaryDescending(), shown for contrast. */
    public static List<Employee> sortBySalaryDescendingImperative(List<Employee> employees) {
        List<Employee> copy = new java.util.ArrayList<>(employees);
        copy.sort((a, b) -> Double.compare(b.getSalary(), a.getSalary()));
        return copy;
    }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("Bob", "Engineering", 35, 110_000),
                new Employee("Alice", "Engineering", 29, 120_000),
                new Employee("Dave", "Sales", 41, 95_000),
                new Employee("Carol", "Sales", 38, 95_000),
                new Employee("Eve", "HR", 26, 80_000));

        System.out.println("Input: " + employees);

        System.out.println("\nsortByDepartmentThenName = " + sortByDepartmentThenName(employees));
        // Expected: [Alice(Engineering), Bob(Engineering), Eve(HR), Carol(Sales), Dave(Sales)]
        // (department ascending, name ascending as tie-breaker within department)

        System.out.println("\nsortBySalaryDescending = " + sortBySalaryDescending(employees));
        // Expected: [Alice($120000), Bob($110000), Dave($95000), Carol($95000), Eve($80000)]
        // (Dave/Carol tie at $95000 - stable sort keeps their relative input order)

        System.out.println("\nsortByAgeAscendingNoBoxing = " + sortByAgeAscendingNoBoxing(employees));
        // Expected: [Eve(26), Alice(29), Bob(35), Carol(38), Dave(41)]

        System.out.println("\nsortByDepartmentThenSalaryDescending = "
                + sortByDepartmentThenSalaryDescending(employees));
        // Expected: [Alice(Engineering,$120000), Bob(Engineering,$110000), Eve(HR,$80000),
        //            Carol(Sales,$95000) or Dave(Sales,$95000) tied, Dave/Carol]

        System.out.println("\nsortBySalaryDescendingImperative = "
                + sortBySalaryDescendingImperative(employees));
        // Expected: same result as sortBySalaryDescending

        List<Employee> empty = Arrays.asList();
        System.out.println("\nEmpty list -> sortByDepartmentThenName = " + sortByDepartmentThenName(empty));
        // Expected: [] (empty list, no exception)

        List<Employee> single = Arrays.asList(new Employee("Solo", "Ops", 30, 70_000));
        System.out.println("Single element -> sortBySalaryDescending = " + sortBySalaryDescending(single));
        // Expected: [Solo(Ops, age 30, $70000.0)] (trivially sorted)
    }
}
