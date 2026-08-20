package com.playground.java.interview.java8;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * PATTERN: Functional Interface / Strategy via Lambda
 * PRIORITY: P0 - Must Know
 * TOPIC: Defining and using a custom {@code @FunctionalInterface} (a 3-argument TriFunction),
 * contrasted with the standard java.util.function interfaces.
 */
public class CustomFunctionalInterface {

    // ================= WHAT IS BEING TESTED =================
    // Ability to design a custom functional interface (single abstract method, SAM)
    // annotated with @FunctionalInterface, and to understand how it plugs into lambdas
    // the same way the built-in java.util.function interfaces do.

    // ================= APPROACH =================
    // 1. Define an interface with exactly one abstract method (SAM) that takes three
    //    generic arguments A, B, C and returns R -> this is our TriFunction<A,B,C,R>.
    // 2. Annotate it with @FunctionalInterface so the compiler enforces the SAM rule
    //    (fails to compile if a second abstract method is added later).
    // 3. Optionally add default/static methods (e.g. andThen) without breaking the
    //    functional interface contract, since only ONE abstract method is allowed.
    // 4. Instantiate it using a lambda expression, matching the interface's method signature.
    // 5. For contrast, briefly use the four most common built-in functional interfaces:
    //    Function<T,R>, BiFunction<T,U,R>, Predicate<T>, Supplier<T>, Consumer<T>.

    // ================= WHY THIS MATTERS =================
    // Senior backend engineers frequently need behavior parameterization beyond what the
    // JDK ships (e.g. a 3-arg validation rule, a repository callback taking id+filter+context).
    // Understanding how to build a lawful functional interface (and why java.util.function
    // caps out at 2 arguments for most interfaces) is essential for clean, composable APIs,
    // dependency injection of behavior, and avoiding anonymous inner class boilerplate.

    // ================= COMMON MISTAKES =================
    // - Adding a second abstract method to an @FunctionalInterface-annotated interface (compile error).
    // - Forgetting @FunctionalInterface annotation -> loses compile-time safety net for accidental SAM violations.
    // - Reinventing TriFunction/QuadFunction inline everywhere instead of defining one reusable interface.
    // - Confusing default methods (allowed, not abstract) with abstract methods (only one allowed).

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);

        // default method is allowed - it is not abstract, so SAM contract is preserved
        default <V> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
            return (a, b, c) -> after.apply(this.apply(a, b, c));
        }
    }

    // ================= EDGE CASES =================
    // - A functional interface with zero abstract methods (all default/static) is NOT usable
    //   as a lambda target -> compiler error "no target method found".
    // - Generic functional interfaces with primitive-heavy use cases may benefit from
    //   specialized interfaces (IntBinaryOperator etc.) to avoid boxing overhead.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) per invocation of the functional interface method itself;
    //                  overall complexity depends on the lambda body supplied by the caller.
    // Space Complexity: O(1) - a lambda capturing no external mutable state allocates a
    //                  small stateless (or singleton-cacheable) instance.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why can a functional interface have only one abstract method, but multiple default/static methods?
    // - How does the compiler know which lambda "shape" (interface) to target when the lambda is ambiguous?
    // - Why doesn't java.util.function provide TriFunction/TriPredicate out of the box?
    // - What is the difference between a lambda and an anonymous inner class implementing the same interface (this-binding, class file generation via invokedynamic)?
    // - How would you make TriFunction serializable, and why would you avoid that in practice?
    // - Explain method reference compatibility: can a lambda target ANY interface with a matching SAM signature?

    public static void main(String[] args) {
        // Example 1: custom TriFunction combining three inputs into one result.
        TriFunction<Integer, Integer, Integer, Integer> sumOfThree = (a, b, c) -> a + b + c;
        int result1 = sumOfThree.apply(2, 3, 5);
        System.out.println("sumOfThree(2, 3, 5) = " + result1); // Expected: 10

        // Example 2: TriFunction composed with andThen (default method) to format output.
        TriFunction<String, String, String, String> concatenate = (a, b, c) -> a + b + c;
        TriFunction<String, String, String, String> concatenateAndUpper =
                concatenate.andThen(String::toUpperCase);
        String result2 = concatenateAndUpper.apply("mar", "ri", "ott");
        System.out.println("concatenateAndUpper(mar, ri, ott) = " + result2); // Expected: MARRIOTT

        // Example 3: contrast with standard java.util.function interfaces.
        Function<Integer, Integer> square = x -> x * x;
        BiFunction<Integer, Integer, Integer> add = (x, y) -> x + y;
        Predicate<Integer> isEven = x -> x % 2 == 0;
        Supplier<String> greeting = () -> "Hello, Senior Engineer";
        Consumer<String> printer = System.out::println;

        System.out.println("square(4) = " + square.apply(4));       // Expected: 16
        System.out.println("add(4, 6) = " + add.apply(4, 6));         // Expected: 10
        System.out.println("isEven(7) = " + isEven.test(7));          // Expected: false
        System.out.print("Supplier output: ");
        printer.accept(greeting.get());                               // Expected: Hello, Senior Engineer
    }
}
