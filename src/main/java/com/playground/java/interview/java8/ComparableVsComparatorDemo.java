package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * PATTERN: Strategy (Sorting Strategy via Comparable / Comparator)
 * PRIORITY: P0 - Must Know
 * TOPIC: Natural ordering via Comparable vs. pluggable external ordering via Comparator.
 */
public class ComparableVsComparatorDemo {

    // ================= WHAT IS BEING TESTED =================
    // Understanding when a class should define its own natural ordering (Comparable)
    // versus when sorting logic should live outside the class (Comparator), and how to
    // compose/chain comparators cleanly using Java 8 Comparator static/default methods.

    // ================= APPROACH =================
    // 1. Implement Comparable<Employee> on Employee itself, defining a single "natural"
    //    ordering (by id) - this is intrinsic to the class and used by Collections.sort()
    //    / TreeSet / TreeMap by default when no Comparator is supplied.
    // 2. Define external Comparator<Employee> instances for orderings that are use-case
    //    specific (by salary, by name) WITHOUT modifying the Employee class at all.
    // 3. Use Comparator.comparing(...) and thenComparing(...) (Java 8 fluent style) plus
    //    reversed() to build composite orderings declaratively.
    // 4. Demonstrate sorting a List with each strategy and show the different resulting orders.

    // ================= WHY THIS MATTERS =================
    // Real backend domain objects (Employee, Order, Product) often need multiple, context
    // dependent sort orders (by date for one API, by amount for a report, by name for a UI
    // dropdown). Baking every possible ordering into the domain class via Comparable would
    // violate single-responsibility and force circular/awkward changes. Knowing which
    // interface to reach for is a very common senior-level system design/code-review question.

    // ================= COMMON MISTAKES =================
    // - Implementing multiple "natural" orderings by mutating Comparable logic per use case instead of using Comparator.
    // - compareTo()/compare() inconsistent with equals() (e.g. compareTo returns 0 for objects that are NOT equals()-equal) -> breaks TreeSet/TreeMap uniqueness semantics.
    // - Integer overflow bug: writing `return a.getId() - b.getId();` instead of Integer.compare(a, b) (overflows for large/negative values).
    // - Forgetting null-handling in Comparators when fields can be null (NullPointerException at sort time).

    static class Employee implements Comparable<Employee> {
        private final int id;
        private final String name;
        private final double salary;

        Employee(int id, String name, double salary) {
            this.id = id;
            this.name = name;
            this.salary = salary;
        }

        int getId() {
            return id;
        }

        String getName() {
            return name;
        }

        double getSalary() {
            return salary;
        }

        // Natural ordering: by id ascending. This is THE single canonical ordering
        // intrinsic to what an Employee "is" (its identity/insertion key).
        @Override
        public int compareTo(Employee other) {
            return Integer.compare(this.id, other.id); // safe, no overflow risk
        }

        @Override
        public String toString() {
            return "Employee{id=" + id + ", name='" + name + "', salary=" + salary + "}";
        }
    }

    // ================= EDGE CASES =================
    // - Sorting with a Comparator built from a field that can be null -> use
    //   Comparator.nullsFirst()/nullsLast() wrappers.
    // - Two employees with identical salary -> thenComparing(name) breaks ties deterministically.
    // - Sorting an empty or single-element list -> no-op, must not throw.
    // - Comparable.compareTo() must be consistent with equals() ideally (TreeSet/TreeMap
    //   use compareTo() alone for uniqueness, ignoring equals()/hashCode()).

    // ================= COMPLEXITY =================
    // Time Complexity: O(n log n) for List.sort()/Collections.sort() (TimSort), where each
    //                  comparison itself is O(1) for these simple field comparisons.
    // Space Complexity: O(n) auxiliary space for TimSort's merge buffer (or O(log n) typical).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - When would you choose Comparable over Comparator, and vice versa?
    // - Why is it a bug to write `a.getId() - b.getId()` for compareTo/compare instead of Integer.compare()?
    // - How do TreeSet/TreeMap use compareTo()/compare() for uniqueness instead of equals()/hashCode()?
    // - How would you build a multi-field comparator (salary desc, then name asc) using Comparator.comparing/thenComparing/reversed?
    // - What happens if compareTo() is inconsistent with equals()? Give a concrete failure scenario.
    // - How do you safely sort a list containing nulls or objects with nullable fields?
    // - Is Comparable/Comparator stable? Does Collections.sort()/List.sort() guarantee stability, and why does that matter for multi-key sorts?

    public static void main(String[] args) {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(3, "Charlie", 75000.0));
        employees.add(new Employee(1, "Alice", 95000.0));
        employees.add(new Employee(2, "Bob", 85000.0));

        // Example 1: natural ordering via Comparable (by id ascending).
        List<Employee> byId = new ArrayList<>(employees);
        byId.sort(null); // null Comparator -> uses Employee.compareTo()
        System.out.println("Sorted by id (Comparable): " + byId);
        // Expected: [Employee{id=1,...}, Employee{id=2,...}, Employee{id=3,...}]

        // Example 2: external Comparator by salary descending (no change to Employee class).
        Comparator<Employee> bySalaryDesc = Comparator.comparingDouble(Employee::getSalary).reversed();
        List<Employee> bySalary = new ArrayList<>(employees);
        bySalary.sort(bySalaryDesc);
        System.out.println("Sorted by salary desc (Comparator): " + bySalary);
        // Expected: [Alice(95000), Bob(85000), Charlie(75000)]

        // Example 3: external Comparator by name, with a composite tie-breaker example.
        Comparator<Employee> byNameThenId = Comparator.comparing(Employee::getName)
                .thenComparing(Employee::getId);
        List<Employee> byName = new ArrayList<>(employees);
        byName.sort(byNameThenId);
        System.out.println("Sorted by name then id (Comparator): " + byName);
        // Expected: [Alice, Bob, Charlie] (already alphabetical here, id breaks any ties)
    }
}
