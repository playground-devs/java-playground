package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PATTERN: Collectors.joining(delimiter, prefix, suffix) and Collectors.summarizingDouble/DoubleSummaryStatistics
 * PRIORITY: P1 - High Priority
 * TOPIC: Join a List of Strings with delimiter/prefix/suffix, and compute count/sum/min/max/average of
 *        Employee salaries in a single pass using DoubleSummaryStatistics.
 */
public class CollectorsJoiningAndSummaryStatistics {

    // ================= WHAT IS BEING TESTED =================
    // Whether the candidate knows Collectors.joining() overloads for building delimited strings without
    // manual StringBuilder bookkeeping (and without a trailing/leading delimiter bug), and whether they know
    // Collectors.summarizingDouble/Int/Long which compute count, sum, min, max, and average of a numeric
    // field in a SINGLE pass over the data, instead of 5 separate stream passes.

    // ================= APPROACH =================
    // Plain loop way (for contrast - joining names with a delimiter/prefix/suffix):
    //   1. Use a StringBuilder, append prefix first.
    //   2. Iterate the list; for each element (except handling the first specially), append the delimiter
    //      before appending the element (or track an "isFirst" flag to avoid a leading delimiter).
    //   3. Append the suffix at the end.
    //
    // Plain loop way (for contrast - salary statistics):
    //   1. Initialize count = 0, sum = 0.0, min = Double.MAX_VALUE, max = Double.MIN_VALUE (or -Double.MAX_VALUE).
    //   2. Iterate employees once: count++, sum += salary, min = Math.min(min, salary), max = Math.max(max, salary).
    //   3. After the loop, average = count == 0 ? 0.0 : sum / count.
    //
    // Stream way (joining):
    //   1. Stream the list of names/strings.
    //   2. collect(Collectors.joining(", ", "[", "]")) - delimiter between elements, prefix/suffix wrap the
    //      whole result exactly once regardless of element count (even for an empty stream: "[]").
    //
    // Stream way (summary statistics):
    //   1. Stream the list of employees.
    //   2. mapToDouble(Employee::getSalary) to get a DoubleStream of just the salary values.
    //   3. .summaryStatistics() - a single terminal operation that computes count/sum/min/max/average together
    //      in ONE pass, returned as a DoubleSummaryStatistics object.
    //   4. Read getCount()/getSum()/getMin()/getMax()/getAverage() off that object as needed.
    //   (Equivalently: employees.stream().collect(Collectors.summarizingDouble(Employee::getSalary)) produces
    //    the same DoubleSummaryStatistics when you don't want to go through a primitive DoubleStream directly.)

    // ================= WHY THIS API =================
    // Collectors.joining() avoids the classic "extra trailing comma" or "extra leading comma" bug from manual
    // string building, and the prefix/suffix overload avoids special-casing the first/last element by hand.
    // summaryStatistics()/summarizingDouble() computes 5 aggregate values in a SINGLE pass over the stream,
    // which is far more efficient than calling stream().mapToDouble(...).sum(), then .min(), then .max(),
    // then .average() separately (4 separate passes/streams over the same data).

    // ================= COMMON MISTAKES =================
    // 1. Manually building a delimited string with a loop and forgetting to strip the trailing delimiter.
    // 2. Calling multiple separate stream terminal operations (sum(), min(), max(), average()) to get several
    //    statistics, not realizing summaryStatistics() does all of them together in one pass.
    // 3. Using summarizingDouble/Int/Long confusingly - must match the primitive type of the field
    //    (Int for salary-as-int, Double for salary-as-double) or you'll get a subtly wrong/truncated result.
    // 4. Calling getAverage() on an empty stream's DoubleSummaryStatistics and being surprised it returns 0.0
    //    instead of throwing or returning NaN (min()/max() on empty return Double.POSITIVE_INFINITY /
    //    Double.NEGATIVE_INFINITY respectively - easy to get wrong from memory).
    // 5. Using Collectors.joining() on a list containing null elements - throws NullPointerException,
    //    since joining() calls toString() implicitly per element via CharSequence concatenation internally
    //    (nulls must be filtered or mapped to "" first).

    // ================= EDGE CASES =================
    // - Empty list joined -> returns just the prefix + suffix concatenated (e.g., "[]"), no exception.
    // - Single element joined -> no delimiter appears at all (e.g., "[Alice]").
    // - Empty list for summary statistics -> count = 0, sum = 0.0, average = 0.0, min = Double.POSITIVE_INFINITY,
    //   max = Double.NEGATIVE_INFINITY (must be handled/documented, not assumed to be 0).
    // - Single employee -> min = max = average = that one salary, count = 1.
    // - All employees with the same salary -> min = max = average = that salary.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for joining (one pass to concatenate) and O(n) for summaryStatistics (single pass
    // computing all 5 aggregates together, vs O(5n) if computed as 5 separate stream operations).
    // Space Complexity: O(n) for the joined String's character buffer; O(1) additional space for the
    // DoubleSummaryStatistics accumulator object itself.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What does DoubleSummaryStatistics return for min/max/average on an EMPTY stream, exactly?
    // - How is summaryStatistics() different from calling sum(), min(), max(), average() as 4 separate
    //   terminal operations on 4 separate streams?
    // - How would you join a List<Employee> by first mapping each to a formatted String
    //   (e.g., "name: salary") before joining?
    // - What happens if Collectors.joining() encounters a null element in the stream?
    // - How would you compute summary statistics for salaries grouped BY DEPARTMENT
    //   (groupingBy(Employee::getDepartment, summarizingDouble(Employee::getSalary)))?
    // - Why does DoubleSummaryStatistics exist separately from IntSummaryStatistics/LongSummaryStatistics?
    // - Can DoubleSummaryStatistics be combined/merged from parallel stream computations? (yes, it supports
    //   combine(), which is how parallel streams merge partial results)

    /**
     * Minimal Employee record used for the salary statistics example.
     */
    public static class Employee {
        private final String name;
        private final double salary;

        public Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public double getSalary() {
            return salary;
        }
    }

    /**
     * Loop Approach: manually join names with delimiter/prefix/suffix.
     */
    public static String joinNamesLoop(List<String> names, String delimiter, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix); // Step 1: prefix goes first, exactly once
        // Step 2: iterate, appending delimiter only BEFORE elements after the first
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(names.get(i));
        }
        sb.append(suffix); // Step 3: suffix goes last, exactly once
        return sb.toString();
    }

    /**
     * Java 8 Streams Approach: join names using Collectors.joining(delimiter, prefix, suffix).
     */
    public static String joinNamesStream(List<String> names, String delimiter, String prefix, String suffix) {
        // Step 1: stream the names, Step 2: collect with joining - handles delimiter placement automatically
        return names.stream()
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * Loop Approach: manually compute count/sum/min/max/average of employee salaries in one pass.
     */
    public static double[] salaryStatsLoop(List<Employee> employees) {
        int count = 0;
        double sum = 0.0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        // Step 1: single pass accumulating all 5 aggregates manually
        for (Employee e : employees) {
            double salary = e.getSalary();
            count++;
            sum += salary;
            min = Math.min(min, salary);
            max = Math.max(max, salary);
        }
        double average = count == 0 ? 0.0 : sum / count;
        // Step 2: pack results as {count, sum, min, max, average} for simple demonstration
        return new double[]{count, sum, min, max, average};
    }

    /**
     * Java 8 Streams Approach: compute salary statistics in a single pass via DoubleSummaryStatistics.
     */
    public static DoubleSummaryStatistics salaryStatsStream(List<Employee> employees) {
        // Step 1: stream employees, Step 2: mapToDouble extracts the salary field as a primitive DoubleStream
        // Step 3: summaryStatistics() computes count/sum/min/max/average together in ONE pass
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .summaryStatistics();
    }

    /**
     * Java 8 Streams Approach (alternative): same result via Collectors.summarizingDouble on the Stream<Employee>
     * directly, without manually going through mapToDouble first.
     */
    public static DoubleSummaryStatistics salaryStatsStreamCollector(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.summarizingDouble(Employee::getSalary));
    }

    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        System.out.println("Loop join: " + joinNamesLoop(names, ", ", "[", "]"));
        System.out.println("Stream join: " + joinNamesStream(names, ", ", "[", "]"));
        // Expected: "[Alice, Bob, Charlie]"

        List<String> singleName = Arrays.asList("Alice");
        System.out.println("Stream join (single): " + joinNamesStream(singleName, ", ", "[", "]"));
        // Expected: "[Alice]" (no delimiter for a single element)

        List<String> emptyNames = new ArrayList<>();
        System.out.println("Stream join (empty): " + joinNamesStream(emptyNames, ", ", "[", "]"));
        // Expected: "[]" (just prefix + suffix, no exception)

        List<Employee> employees = Arrays.asList(
                new Employee("Alice", 95000.0),
                new Employee("Bob", 88000.0),
                new Employee("Charlie", 72000.0),
                new Employee("Dana", 105000.0)
        );

        DoubleSummaryStatistics stats = salaryStatsStream(employees);
        System.out.println("Stream salary stats: count=" + stats.getCount()
                + ", sum=" + stats.getSum()
                + ", min=" + stats.getMin()
                + ", max=" + stats.getMax()
                + ", average=" + stats.getAverage());
        // Expected: count=4, sum=360000.0, min=72000.0, max=105000.0, average=90000.0

        DoubleSummaryStatistics statsViaCollector = salaryStatsStreamCollector(employees);
        System.out.println("Stream salary stats (via collector): count=" + statsViaCollector.getCount()
                + ", average=" + statsViaCollector.getAverage());
        // Expected: count=4, average=90000.0 (same as above, different way to obtain it)

        double[] loopStats = salaryStatsLoop(employees);
        System.out.println("Loop salary stats: count=" + loopStats[0] + ", sum=" + loopStats[1]
                + ", min=" + loopStats[2] + ", max=" + loopStats[3] + ", average=" + loopStats[4]);
        // Expected: matches the stream results above

        List<Employee> emptyEmployees = new ArrayList<>();
        DoubleSummaryStatistics emptyStats = salaryStatsStream(emptyEmployees);
        System.out.println("Stream salary stats (empty): count=" + emptyStats.getCount()
                + ", sum=" + emptyStats.getSum()
                + ", min=" + emptyStats.getMin()
                + ", max=" + emptyStats.getMax()
                + ", average=" + emptyStats.getAverage());
        // Expected: count=0, sum=0.0, min=Infinity, max=-Infinity, average=0.0
    }
}
