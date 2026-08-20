package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PATTERN: Collectors.toMap(keyMapper, valueMapper[, mergeFunction]) and Collectors.groupingBy
 * PRIORITY: P0 - Must Know
 * TOPIC: Convert a List of Employee objects into various Map shapes using Streams.
 */
public class ListToMapConversion {

    // ================= WHAT IS BEING TESTED =================
    // Whether the candidate knows the difference between Collectors.toMap() (1-to-1 key->value, throws on
    // duplicate keys unless a merge function is supplied) and Collectors.groupingBy() (1-to-many key->List,
    // naturally handles duplicate keys by bucketing). This is one of the most common "convert a list of
    // domain objects into a lookup map" interview tasks.

    // ================= APPROACH =================
    // Plain loop way (for contrast, for Map<Integer, Employee>):
    //   1. Create a HashMap<Integer, Employee>.
    //   2. Iterate the list of employees.
    //   3. For each employee, map.put(employee.getId(), employee).
    //   4. (If IDs could repeat and you wanted the loop equivalent of groupingBy, you'd instead do
    //      map.computeIfAbsent(key, k -> new ArrayList<>()).add(employee).)
    //
    // Stream way (Map<Integer id, Employee>):
    //   1. Stream the employee list.
    //   2. collect(Collectors.toMap(Employee::getId, Function.identity()))
    //      - keyMapper extracts the id, valueMapper keeps the whole Employee object.
    //      - Throws IllegalStateException at runtime if two employees share the same id (duplicate key).
    //
    // Stream way (Map<String department, List<Employee>>):
    //   1. Stream the employee list.
    //   2. collect(Collectors.groupingBy(Employee::getDepartment))
    //      - groups employees by department; naturally produces List<Employee> values, no merge conflicts.
    //
    // Stream way (Map<String name, Double salary>):
    //   1. Stream the employee list.
    //   2. collect(Collectors.toMap(Employee::getName, Employee::getSalary, (existing, replacement) -> existing))
    //      - keyMapper extracts name, valueMapper extracts salary.
    //      - A merge function is supplied in case two employees share the same name, to avoid a runtime
    //        exception (keeps the first one seen here; could also sum/average/replace instead).

    // ================= WHY THIS API =================
    // Collectors.toMap is the natural choice when the result should be a strict 1-to-1 mapping (e.g., id is
    // unique). Collectors.groupingBy is the natural choice when the key is NOT expected to be unique and you
    // want all matching elements bucketed together. Supplying an explicit merge function to toMap is the
    // idiomatic way to make the collector duplicate-key-safe instead of letting it throw at runtime.

    // ================= COMMON MISTAKES =================
    // 1. Using Collectors.toMap() on a non-unique key (e.g., department, or a name that can collide) without
    //    a merge function -> throws IllegalStateException: "Duplicate key ..." at runtime, often only
    //    surfacing in production with real duplicate data.
    // 2. Using groupingBy() when a flat 1-to-1 Map was actually wanted, ending up with unwanted List wrapping.
    // 3. Forgetting that the default toMap()/groupingBy() return a HashMap - if a specific Map implementation
    //    or ordering is required, must supply the 3-arg toMap (with mapFactory) or groupingBy overload.
    // 4. Passing the wrong order of arguments to toMap (keyMapper, valueMapper) - easy to swap logically when
    //    working quickly under interview pressure.
    // 5. Not handling null keys - Collectors.toMap()'s default HashMap-backed implementation throws NPE on a
    //    null key (unlike a plain HashMap.put which allows one null key).

    // ================= EDGE CASES =================
    // - Empty list -> empty map for all three conversions, no exceptions.
    // - Duplicate ids (should not happen but if it does) -> toMap throws IllegalStateException unless a merge
    //   function is supplied.
    // - Duplicate names -> handled here via a merge function that keeps the existing entry.
    // - Single department shared by all employees -> groupingBy produces one key with a List containing all
    //   employees.
    // - Null department or null name on an Employee -> would throw NPE from the default HashMap-backed
    //   collectors; must be filtered/defaulted before collecting if nulls are possible.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for each conversion - one pass over the employee list.
    // Space Complexity: O(n) for each resulting map - one entry (or list slot) per employee.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What happens if two employees have the same id and you use plain Collectors.toMap() with no merge
    //   function? (IllegalStateException at runtime)
    // - How would you convert to a Map<String, List<String>> of department -> employee names instead of full
    //   Employee objects? (groupingBy(Employee::getDepartment, mapping(Employee::getName, toList())))
    // - How would you get a Map<String, Double> of department -> total/average salary?
    //   (groupingBy(Employee::getDepartment, summingDouble(Employee::getSalary)) or averagingDouble)
    // - How would you force the resulting map to be a TreeMap (sorted by key)?
    //   (4-arg toMap with a TreeMap::new supplier, or groupingBy's 3-arg overload)
    // - Why does Collectors.toMap() use HashMap by default, and how would you change that?
    // - What's the practical difference between Function.identity() and (e -> e) here?
    // - How would you handle the case where salary should be summed for employees with duplicate names
    //   instead of just keeping one?

    /**
     * Simple immutable-ish Employee domain object used across the examples.
     */
    public static class Employee {
        private final int id;
        private final String name;
        private final String department;
        private final double salary;

        public Employee(int id, String name, String department, double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public double getSalary() {
            return salary;
        }

        @Override
        public String toString() {
            return name + "(id=" + id + ", dept=" + department + ", salary=" + salary + ")";
        }
    }

    /**
     * Loop Approach: manually build all three map shapes.
     */
    public static Map<Integer, Employee> idToEmployeeLoop(List<Employee> employees) {
        Map<Integer, Employee> map = new HashMap<>();
        // Step 1: iterate and put each employee keyed by id
        for (Employee e : employees) {
            map.put(e.getId(), e);
        }
        return map;
    }

    public static Map<String, List<Employee>> departmentToEmployeesLoop(List<Employee> employees) {
        Map<String, List<Employee>> map = new HashMap<>();
        // Step 1: iterate, bucket employees under their department key
        for (Employee e : employees) {
            map.computeIfAbsent(e.getDepartment(), k -> new ArrayList<>()).add(e);
        }
        return map;
    }

    public static Map<String, Double> nameToSalaryLoop(List<Employee> employees) {
        Map<String, Double> map = new HashMap<>();
        // Step 1: iterate, put name -> salary, keep first seen on name collision
        for (Employee e : employees) {
            map.putIfAbsent(e.getName(), e.getSalary());
        }
        return map;
    }

    /**
     * Java 8 Streams Approach: Map<Integer id, Employee> via Collectors.toMap.
     */
    public static Map<Integer, Employee> idToEmployeeStream(List<Employee> employees) {
        // Step 1: keyMapper = Employee::getId, valueMapper = identity (keep whole object)
        return employees.stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));
    }

    /**
     * Java 8 Streams Approach: Map<String department, List<Employee>> via Collectors.groupingBy.
     */
    public static Map<String, List<Employee>> departmentToEmployeesStream(List<Employee> employees) {
        // Step 1: groupingBy department, default downstream collector is toList()
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));
    }

    /**
     * Java 8 Streams Approach: Map<String name, Double salary> via Collectors.toMap with a merge function.
     */
    public static Map<String, Double> nameToSalaryStream(List<Employee> employees) {
        // Step 1: keyMapper = getName, valueMapper = getSalary
        // Step 2: mergeFunction handles duplicate names by keeping the existing (first-seen) value
        return employees.stream()
                .collect(Collectors.toMap(Employee::getName, Employee::getSalary, (existing, replacement) -> existing));
    }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", "Engineering", 95000.0),
                new Employee(2, "Bob", "Engineering", 88000.0),
                new Employee(3, "Charlie", "Sales", 72000.0),
                new Employee(4, "Dana", "Sales", 75000.0),
                new Employee(5, "Eve", "HR", 68000.0)
        );

        System.out.println("Loop id->Employee: " + idToEmployeeLoop(employees));
        System.out.println("Stream id->Employee: " + idToEmployeeStream(employees));
        // Expected: map with 5 entries keyed 1..5, each mapped to its Employee

        System.out.println("Loop dept->Employees: " + departmentToEmployeesLoop(employees));
        System.out.println("Stream dept->Employees: " + departmentToEmployeesStream(employees));
        // Expected: {Engineering=[Alice, Bob], Sales=[Charlie, Dana], HR=[Eve]} (order may vary)

        System.out.println("Loop name->salary: " + nameToSalaryLoop(employees));
        System.out.println("Stream name->salary: " + nameToSalaryStream(employees));
        // Expected: {Alice=95000.0, Bob=88000.0, Charlie=72000.0, Dana=75000.0, Eve=68000.0}

        List<Employee> withDuplicateName = Arrays.asList(
                new Employee(6, "Alice", "Marketing", 60000.0),
                new Employee(7, "Alice", "Finance", 61000.0)
        );
        System.out.println("Stream name->salary (duplicate names, merge keeps first): "
                + nameToSalaryStream(withDuplicateName));
        // Expected: {Alice=60000.0} (merge function keeps the existing/first-seen value)

        List<Employee> empty = new ArrayList<>();
        System.out.println("Stream id->Employee (empty): " + idToEmployeeStream(empty));
        // Expected: {} (empty map, no exception)
    }
}
