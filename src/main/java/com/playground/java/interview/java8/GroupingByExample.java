package com.playground.java.interview.java8;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PATTERN: Java 8 Streams / Collectors
 * PRIORITY: P0 - Must Know
 * TOPIC: Group a List of Employees by department using Collectors.groupingBy(),
 * plus downstream collectors for counting() and summing salaries.
 */
public class GroupingByExample {

    // ================= WHAT IS BEING TESTED =================
    // This tests fluency with Collectors.groupingBy(), the Stream equivalent
    // of a SQL "GROUP BY" clause, and specifically the two- and three-argument
    // overloads that accept a downstream Collector (e.g. counting(),
    // summingDouble()) to aggregate each group instead of just bucketing raw
    // elements into lists. Interviewers ask this because grouping-and-
    // aggregating data is one of the most common real-world stream tasks
    // (reporting, analytics, bucketed summaries) and it demonstrates whether
    // a candidate understands collector composition, not just single
    // collectors in isolation.

    // ================= APPROACH =================
    // 1. employees.stream() -> obtain a Stream<Employee> from the source list.
    // 2. collect(Collectors.groupingBy(Employee::getDepartment))
    //    -> terminal op that partitions elements into a Map<String,
    //    List<Employee>> keyed by department, where the classifier function
    //    Employee::getDepartment decides which bucket each employee goes in.
    // 3. Variant with counting(): collect(Collectors.groupingBy(
    //    Employee::getDepartment, Collectors.counting())) -> same grouping,
    //    but the downstream collector reduces each department's List<Employee>
    //    down to a single Long (the count of employees in that department),
    //    giving Map<String, Long>.
    // 4. Variant with summingDouble(): collect(Collectors.groupingBy(
    //    Employee::getDepartment, Collectors.summingDouble(Employee::getSalary)))
    //    -> same grouping, but the downstream collector sums the salary field
    //    of each department's employees, giving Map<String, Double>.

    // ================= WHY THIS API =================
    // groupingBy() expresses "bucket by this key, then optionally reduce each
    // bucket with this downstream collector" as a single declarative
    // expression, whereas the imperative equivalent requires manually
    // creating a Map, checking computeIfAbsent()/getOrDefault() for each key,
    // and mutating a list or running counter per bucket - error-prone
    // boilerplate that groupingBy() handles internally. You would still
    // prefer an explicit loop if you need multiple different aggregations
    // computed in a single pass with complex interdependencies that don't map
    // cleanly onto a single downstream collector, or if you need fine control
    // over iteration order that a HashMap-backed groupingBy() does not
    // guarantee (use Collectors.groupingBy(..., TreeMap::new, ...) instead if
    // ordering matters).

    // ================= COMMON MISTAKES =================
    // - Forgetting that plain groupingBy(classifier) returns Map<K,
    //   List<T>>, and mistakenly treating the values as a single T.
    // - Using summingInt/summingDouble vs mapping+toList incorrectly, or
    //   using averagingDouble() when the interviewer actually asked for a sum.
    // - Assuming the returned map preserves insertion order - the default
    //   groupingBy() uses a HashMap, so iteration order is not guaranteed;
    //   use groupingBy(classifier, TreeMap::new, downstream) if order matters.
    // - Chaining groupingBy() with a classifier function that can throw or
    //   return null (e.g. a department field that is null), which causes a
    //   NullPointerException since HashMap keys generally tolerate null but
    //   groupingBy's internal merge logic can behave unexpectedly with it.

    // ================= EDGE CASES =================
    // - Empty employee list: groupingBy() returns an empty map, no exception.
    // - All employees in the same department: result map has exactly one key
    //   whose value list/aggregate contains/reflects every employee.
    // - A department with a single employee: counting() correctly yields 1,
    //   summingDouble() yields exactly that employee's salary.
    // - Employees with duplicate names but different departments: grouping is
    //   by department field only, so duplicates are handled fine as long as
    //   the key (department) is what's compared, not identity/equality of the
    //   whole Employee object.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) - each employee is visited once to compute its
    // classifier key and update the corresponding bucket/aggregate.
    // Space Complexity: O(n) in the worst case (e.g. plain groupingBy() into
    // lists retains every element), or O(k) for the counting()/summingDouble()
    // variants where k = number of distinct departments, since only the
    // aggregate value is retained per key rather than the full element list.
    // Streams are lazy: the entire groupingBy() computation is driven by the
    // terminal collect() call; nothing is grouped until collect() executes.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What's the difference between groupingBy(classifier),
    //   groupingBy(classifier, downstream), and groupingBy(classifier,
    //   mapFactory, downstream)?
    // - How would you get a Map<String, TreeMap<...>> or otherwise control
    //   the map/order implementation returned by groupingBy()?
    // - How is groupingBy() different from partitioningBy()? When would you
    //   use one over the other?
    // - How would you chain groupingBy() with mapping() to, say, group
    //   employees by department but store only their names instead of full
    //   Employee objects?
    // - What happens if two different downstream collectors are needed at
    //   once (e.g. count AND average salary per department)? How would
    //   Collectors.teeing() (Java 12+) or a custom collector help versus
    //   Java 8/11's toolkit?
    // - Is groupingBy() safe to use with parallel streams? What must be true
    //   of the downstream collector for correct parallel behavior?

    /** Simple POJO used across the grouping examples. */
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

    /** Plain groupingBy(): department -> List<Employee> in that department. */
    public static Map<String, List<Employee>> groupByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));
    }

    /** groupingBy() with a downstream counting() collector: department -> headcount. */
    public static Map<String, Long> countByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
    }

    /** groupingBy() with a downstream summingDouble() collector: department -> total salary. */
    public static Map<String, Double> totalSalaryByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.summingDouble(Employee::getSalary)));
    }

    /** Imperative equivalent of countByDepartment(), shown for contrast. */
    public static Map<String, Long> countByDepartmentImperative(List<Employee> employees) {
        Map<String, Long> counts = new java.util.HashMap<>();
        for (Employee e : employees) {
            counts.merge(e.getDepartment(), 1L, Long::sum);
        }
        return counts;
    }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", "Engineering", 120_000),
                new Employee("Bob", "Engineering", 110_000),
                new Employee("Carol", "Sales", 90_000),
                new Employee("Dave", "Sales", 95_000),
                new Employee("Eve", "HR", 80_000));

        System.out.println("Input: " + employees);

        Map<String, List<Employee>> byDept = groupByDepartment(employees);
        System.out.println("groupByDepartment = " + byDept);
        // Expected: {Engineering=[Alice, Bob], Sales=[Carol, Dave], HR=[Eve]} (order may vary, HashMap-backed)

        Map<String, Long> counts = countByDepartment(employees);
        System.out.println("countByDepartment = " + counts);
        // Expected: {Engineering=2, Sales=2, HR=1}

        Map<String, Double> totals = totalSalaryByDepartment(employees);
        System.out.println("totalSalaryByDepartment = " + totals);
        // Expected: {Engineering=230000.0, Sales=185000.0, HR=80000.0}

        System.out.println("countByDepartmentImperative = " + countByDepartmentImperative(employees));
        // Expected: same as countByDepartment -> {Engineering=2, Sales=2, HR=1}

        List<Employee> empty = Arrays.asList();
        System.out.println("Empty list -> groupByDepartment = " + groupByDepartment(empty));
        // Expected: {} (empty map, no exception)
    }
}
