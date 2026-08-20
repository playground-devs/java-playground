package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams / Collectors
 * PRIORITY: P0 - Must Know
 * TOPIC: Collectors.toMap() with duplicate keys - the default two-arg overload
 * throws IllegalStateException on collisions; the three-arg merge-function
 * overload resolves them (keep first, keep highest salary, etc.).
 */
public class CollectorsToMapDuplicateKeys {

    // ================= WHAT IS BEING TESTED =================
    // This tests whether a candidate knows that Collectors.toMap(keyMapper,
    // valueMapper) throws IllegalStateException("Duplicate key ...") at
    // runtime the moment two elements produce the same key, and - more
    // importantly - that they know how to fix it with the three-argument
    // overload toMap(keyMapper, valueMapper, mergeFunction) that tells the
    // collector what to do when a collision occurs. Interviewers ask this
    // because it is a very common production bug: toMap() looks safe at
    // compile time but silently ships a runtime crash risk unless the data's
    // key-uniqueness is actually guaranteed.

    // ================= APPROACH =================
    // 1. CRASH SCENARIO: employees.stream().collect(Collectors.toMap(
    //    Employee::getDepartment, Function.identity())) - since multiple
    //    employees can share a department, the second employee mapped to an
    //    already-used department key triggers IllegalStateException at
    //    collection time. (Demonstrated in a comment, not actually executed,
    //    since it would crash the program.)
    // 2. FIX with merge function: employees.stream().collect(Collectors.toMap(
    //    Employee::getDepartment, Function.identity(), (existing, incoming) ->
    //    existing)) -> the BinaryOperator<Employee> merge function decides
    //    what happens on a key collision; here it "keeps first" by always
    //    returning the existing value already in the map.
    // 3. VARIANT merge function: (existing, incoming) -> existing.getSalary()
    //    >= incoming.getSalary() ? existing : incoming -> "keep highest
    //    salary" - the merge function compares both colliding values and
    //    picks the one to retain in the map.

    // ================= WHY THIS API =================
    // The three-arg toMap() overload lets you declare the collision-
    // resolution policy declaratively as part of the same stream pipeline,
    // rather than falling back to a manual loop with an explicit
    // map.containsKey()/map.get() check and conditional overwrite - which is
    // exactly what the merge function does internally. It's idiomatic because
    // the intent ("on collision, keep the higher salary") is expressed right
    // where the map is built. You would still prefer an explicit loop if the
    // merge decision needs to consult external state beyond the two
    // colliding values (e.g. logging every collision to an audit trail,
    // which a pure BinaryOperator<T> merge function isn't well suited to do
    // cleanly, though it CAN have side effects if truly necessary).

    // ================= COMMON MISTAKES =================
    // - Using the two-arg toMap() overload on data where key uniqueness is
    //   not actually guaranteed, causing an IllegalStateException in
    //   production the first time duplicate keys appear.
    // - Writing a merge function that doesn't handle both orderings
    //   symmetrically (relying on encounter order for correctness) when the
    //   stream is processed in parallel, where encounter order for the merge
    //   function's arguments isn't guaranteed to match the original list order.
    // - Forgetting the four-arg overload toMap(keyMapper, valueMapper,
    //   mergeFunction, mapSupplier) exists when you need a specific Map
    //   implementation (e.g. TreeMap for sorted keys, LinkedHashMap to
    //   preserve insertion order).
    // - Assuming toMap() tolerates null values the way HashMap.put() does -
    //   toMap() throws NullPointerException if valueMapper produces null,
    //   regardless of the merge function.

    // ================= EDGE CASES =================
    // - Empty employee list: toMap() returns an empty map, no exception, no
    //   merge function ever invoked.
    // - No duplicate keys at all: merge function is never invoked; behaves
    //   identically to the two-arg overload.
    // - Exactly two employees with the same department: merge function is
    //   invoked exactly once for that key.
    // - Three or more employees sharing the same department: merge function
    //   is invoked repeatedly, each time folding the next colliding value
    //   against the running "winner" so far (left-to-right fold per key).

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each employee is visited once; on collisions,
    // the merge function does O(1) extra work per collision to decide the
    // winner (assuming the merge logic itself is O(1), e.g. a salary compare).
    // Space Complexity: O(k) where k = number of distinct keys, since
    // colliding values are merged down rather than all retained.
    // Streams are lazy: nothing is inserted into the resulting map, and no
    // collision is detected, until the terminal collect(Collectors.toMap(...))
    // call actually runs the pipeline element by element.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What exact exception and message does the two-arg toMap() throw on a
    //   duplicate key, and at what point in the pipeline does it happen?
    // - What is the signature of the merge function - why is it a
    //   BinaryOperator<V> (i.e. BiFunction<V, V, V>) rather than something
    //   that also sees the key?
    // - How would you use the four-arg toMap() overload to collect into a
    //   TreeMap for sorted department keys?
    // - How does Collectors.toMap() differ from Collectors.groupingBy() when
    //   you have duplicate keys - why might groupingBy() be a better fit if
    //   you want to keep ALL colliding values rather than merge them?
    // - Is toMap()'s runtime map mutable in later JDKs? Is that guaranteed?
    // - How would parallel execution affect which of two colliding values is
    //   passed as "existing" vs "incoming" to the merge function?

    /** Simple POJO reused for the toMap() duplicate-key example. */
    static class Employee {
        private final String name;
        private final String department;
        private final double salary;

        Employee(String name, String department, double salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        String getName() {
            return name;
        }

        String getDepartment() {
            return department;
        }

        double getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + "(" + department + ", $" + salary + ")";
        }
    }

    /**
     * CRASH SCENARIO (commented out - do not call with data containing
     * duplicate department keys, it will throw at runtime):
     *
     * <pre>
     * Map&lt;String, Employee&gt; byDept = employees.stream()
     *         .collect(Collectors.toMap(Employee::getDepartment, Function.identity()));
     * // Throws: java.lang.IllegalStateException: Duplicate key Engineering
     * // (attempted merging values Alice(Engineering, $120000.0)
     * // and Bob(Engineering, $110000.0))
     * </pre>
     *
     * This method demonstrates the crash deliberately, wrapped in a try/catch
     * purely so main() can show the exception being thrown without actually
     * killing the whole program - in real code you would simply not call the
     * two-arg overload on non-unique keys.
     */
    public static void demonstrateDuplicateKeyCrash(List<Employee> employees) {
        try {
            Map<String, Employee> byDept = employees.stream()
                    .collect(Collectors.toMap(Employee::getDepartment, e -> e));
            System.out.println("No collision occurred, result = " + byDept);
        } catch (IllegalStateException e) {
            System.out.println("Caught expected crash: " + e.getMessage());
        }
    }

    /** FIX #1: merge function that keeps the FIRST employee seen per department. */
    public static Map<String, Employee> toMapKeepFirst(List<Employee> employees) {
        BinaryOperator<Employee> keepFirst = (existing, incoming) -> existing;
        return employees.stream()
                .collect(Collectors.toMap(Employee::getDepartment, e -> e, keepFirst));
    }

    /** FIX #2: merge function that keeps whichever colliding employee has the HIGHEST salary. */
    public static Map<String, Employee> toMapKeepHighestSalary(List<Employee> employees) {
        BinaryOperator<Employee> keepHighestSalary = (existing, incoming) ->
                existing.getSalary() >= incoming.getSalary() ? existing : incoming;
        return employees.stream()
                .collect(Collectors.toMap(Employee::getDepartment, e -> e, keepHighestSalary));
    }

    /** Imperative equivalent of toMapKeepHighestSalary(), shown for contrast. */
    public static Map<String, Employee> toMapKeepHighestSalaryImperative(List<Employee> employees) {
        Map<String, Employee> result = new java.util.HashMap<>();
        for (Employee e : employees) {
            result.merge(e.getDepartment(), e,
                    (existing, incoming) -> existing.getSalary() >= incoming.getSalary() ? existing : incoming);
        }
        return result;
    }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", "Engineering", 120_000),
                new Employee("Bob", "Engineering", 110_000),
                new Employee("Carol", "Sales", 90_000),
                new Employee("Dave", "Sales", 95_000),
                new Employee("Eve", "HR", 80_000));

        System.out.println("Input: " + employees);

        System.out.println("\n-- Demonstrating the crash scenario --");
        demonstrateDuplicateKeyCrash(employees);
        // Expected: "Caught expected crash: Duplicate key Engineering (attempted merging
        // values Alice(...) and Bob(...))" - since Engineering has 2 employees

        System.out.println("\n-- Fix #1: keep first employee per department --");
        Map<String, Employee> keepFirst = toMapKeepFirst(employees);
        System.out.println("toMapKeepFirst = " + keepFirst);
        // Expected: {Engineering=Alice(...), Sales=Carol(...), HR=Eve(...)}
        // (first employee encountered per department wins)

        System.out.println("\n-- Fix #2: keep highest-salary employee per department --");
        Map<String, Employee> keepHighest = toMapKeepHighestSalary(employees);
        System.out.println("toMapKeepHighestSalary = " + keepHighest);
        // Expected: {Engineering=Alice($120000, > Bob's $110000), Sales=Dave($95000, > Carol's $90000), HR=Eve($80000)}

        System.out.println("Imperative equivalent = " + toMapKeepHighestSalaryImperative(employees));
        // Expected: same as toMapKeepHighestSalary above

        List<Employee> noDuplicates = Arrays.asList(
                new Employee("Frank", "Legal", 100_000),
                new Employee("Grace", "Finance", 105_000));
        System.out.println("\n-- No duplicate keys --");
        demonstrateDuplicateKeyCrash(noDuplicates);
        // Expected: "No collision occurred, result = {Legal=Frank(...), Finance=Grace(...)}"

        List<Employee> empty = Arrays.asList();
        System.out.println("\nEmpty list -> toMapKeepFirst = " + toMapKeepFirst(empty));
        // Expected: {} (empty map, no exception, merge function never invoked)
    }
}
