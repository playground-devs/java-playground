package com.playground.java.interview.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * PATTERN: Method Reference (lambda shorthand)
 * PRIORITY: P0 - Must Know
 * TOPIC: Demonstrating all four kinds of method references and their equivalent lambdas.
 */
public class MethodReferencesAllFourKinds {

    // ================= WHAT IS BEING TESTED =================
    // Recognition and correct usage of the four method reference forms in Java, and
    // the ability to explain what lambda each one is syntactic sugar for.

    // ================= APPROACH =================
    // 1. Static method reference: ClassName::staticMethod
    //    -> equivalent to (args) -> ClassName.staticMethod(args)
    // 2. Bound instance method reference: instance::method (on a specific, already-created object)
    //    -> equivalent to (args) -> instance.method(args)
    // 3. Unbound instance method reference: ClassName::instanceMethod (on an arbitrary object
    //    of that type, supplied as the first lambda parameter at call time)
    //    -> equivalent to (obj, args) -> obj.instanceMethod(args)
    // 4. Constructor reference: ClassName::new
    //    -> equivalent to (args) -> new ClassName(args)
    // Each kind below is shown side-by-side with the lambda it replaces.

    // ================= WHY THIS MATTERS =================
    // Method references are pervasive in real backend code: Stream pipelines (map(Dto::from),
    // Comparator.comparing(Employee::getSalary)), repository/service wiring, and functional
    // composition. Senior engineers must instantly recognize which of the 4 kinds is in play
    // to reason about what gets invoked, on what receiver, and to avoid subtle NPEs or
    // referencing the wrong overload.

    // ================= COMMON MISTAKES =================
    // - Confusing unbound instance reference (ClassName::method, receiver is a lambda param)
    //   with bound instance reference (instance::method, receiver is fixed) - they look similar syntactically.
    // - Using a method reference that captures `this` inside a static context (compile error).
    // - Assuming ClassName::new works for any class -- it requires a constructor matching the target functional interface's shape.
    // - Overload ambiguity: multiple overloaded methods matching the same reference name can cause compiler errors or pick the wrong one.

    static class Employee {
        private final String name;
        private final double salary;

        Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
        }

        Employee(String name) {
            this(name, 0.0);
        }

        String getName() {
            return name;
        }

        double getSalary() {
            return salary;
        }

        static Employee ofDefault() {
            return new Employee("Unassigned", 0.0);
        }

        @Override
        public String toString() {
            return "Employee{name='" + name + "', salary=" + salary + "}";
        }
    }

    // ================= EDGE CASES =================
    // - Constructor reference targeting an abstract class or interface -> impossible, must be concrete.
    // - Unbound instance method reference where the type has overloaded methods of the same
    //   name/arity -> compiler resolves based on target functional interface's parameter types.
    // - Static method reference to a method with side effects on shared/static state -> same
    //   thread-safety concerns as calling it directly.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) - a method reference is resolved once at the invokedynamic call
    //                  site; the underlying method's own complexity applies when invoked.
    // Space Complexity: O(1) - the JVM typically synthesizes a lightweight adapter class only
    //                  once per call site (via LambdaMetafactory), not per invocation.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What is the difference between a bound and an unbound instance method reference?
    // - How does the compiler decide which functional interface a method reference targets?
    // - Can a method reference be used with a checked-exception-throwing method inside a functional interface that doesn't declare it?
    // - Explain how LambdaMetafactory and invokedynamic relate to method references under the hood.
    // - Why can't you use `this::method` inside a static method?
    // - Give an example where method references improve readability over lambdas in a Stream pipeline.
    // - Can a constructor reference be used to implement a Supplier<T> vs a Function<Arg,T>? How does arity/return type matching work?

    public static void main(String[] args) {
        // ---------- 1. Static method reference ----------
        // Lambda form:
        Function<String, Integer> parseIntLambda = s -> Integer.parseInt(s);
        // Method reference form (ClassName::staticMethod):
        Function<String, Integer> parseIntRef = Integer::parseInt;
        System.out.println("parseIntLambda(\"42\") = " + parseIntLambda.apply("42")); // Expected: 42
        System.out.println("parseIntRef(\"42\")    = " + parseIntRef.apply("42"));    // Expected: 42

        // ---------- 2. Bound instance method reference (instance::method) ----------
        String greeting = "Hello Marriott";
        // Lambda form:
        Supplier<String> upperLambda = () -> greeting.toUpperCase();
        // Method reference form (instance::method) - receiver `greeting` is already bound:
        Supplier<String> upperRef = greeting::toUpperCase;
        System.out.println("upperLambda() = " + upperLambda.get()); // Expected: HELLO MARRIOTT
        System.out.println("upperRef()    = " + upperRef.get());    // Expected: HELLO MARRIOTT

        // ---------- 3. Unbound instance method reference (ClassName::instanceMethod) ----------
        List<String> names = Arrays.asList("charlie", "alice", "bob");
        // Lambda form: receiver `s` supplied as the first lambda parameter at call time.
        Function<String, String> toUpperLambda = s -> s.toUpperCase();
        // Method reference form:
        Function<String, String> toUpperRef = String::toUpperCase;
        System.out.println("toUpperLambda(\"bob\") = " + toUpperLambda.apply("bob")); // Expected: BOB
        System.out.println("toUpperRef(\"bob\")    = " + toUpperRef.apply("bob"));    // Expected: BOB

        // A more realistic unbound example: sorting with an arbitrary-receiver comparator method.
        List<String> mutableNames = new ArrayList<>(names);
        mutableNames.sort(String::compareToIgnoreCase); // (a, b) -> a.compareToIgnoreCase(b)
        System.out.println("sorted names = " + mutableNames); // Expected: [alice, bob, charlie]

        // ---------- 4. Constructor reference (ClassName::new) ----------
        // Lambda form:
        BiFunction<String, Double, Employee> employeeFactoryLambda = (n, s) -> new Employee(n, s);
        // Method reference form:
        BiFunction<String, Double, Employee> employeeFactoryRef = Employee::new;
        Employee e1 = employeeFactoryLambda.apply("Priya", 95000.0);
        Employee e2 = employeeFactoryRef.apply("Arjun", 105000.0);
        System.out.println("e1 = " + e1); // Expected: Employee{name='Priya', salary=95000.0}
        System.out.println("e2 = " + e2); // Expected: Employee{name='Arjun', salary=105000.0}

        // Single-arg constructor reference matching a Function<String, Employee>.
        Function<String, Employee> singleArgFactory = Employee::new;
        System.out.println("singleArgFactory(\"Kim\") = " + singleArgFactory.apply("Kim"));
        // Expected: Employee{name='Kim', salary=0.0}

        // No-arg static method reference matching a Supplier<Employee>.
        Supplier<Employee> defaultFactory = Employee::ofDefault;
        System.out.println("defaultFactory() = " + defaultFactory.get());
        // Expected: Employee{name='Unassigned', salary=0.0}
    }
}
