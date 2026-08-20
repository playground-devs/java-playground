package com.playground.java.interview.java8;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * PATTERN: Optional.of/ofNullable/empty, orElse vs orElseGet vs orElseThrow, map/filter/ifPresent chaining
 * PRIORITY: P0 - Must Know
 * TOPIC: Deep-dive into java.util.Optional - creation, safe unwrapping, and functional chaining.
 */
public class OptionalDeepDive {

    // ================= WHAT IS BEING TESTED =================
    // Whether the candidate understands Optional as a container object used to avoid null checks and
    // NullPointerExceptions, knows the three creation factory methods, and - most critically - understands
    // the EAGER vs LAZY evaluation difference between orElse(T) and orElseGet(Supplier<T>), plus why calling
    // get() without checking isPresent() (or, better, avoiding get() entirely) is considered bad practice.

    // ================= APPROACH =================
    // Plain "loop"/imperative way (for contrast - Optional doesn't loop, but here's the null-check equivalent):
    //   1. Given a possibly-null reference, do: if (value != null) { ... use value ... } else { ... fallback ... }
    //   2. This is exactly what Optional replaces with a fluent, null-safe API so callers can't forget the
    //      null check (the type system nudges you toward handling absence).
    //
    // Optional way, step by step:
    //   1. Creation:
    //        - Optional.of(value)          -> throws NPE immediately if value is null (use when null is a bug).
    //        - Optional.ofNullable(value)  -> wraps value, becomes Optional.empty() if value is null (use when
    //                                          null is a valid/expected possibility).
    //        - Optional.empty()            -> explicitly represents "no value" with no wrapped value at all.
    //   2. Unwrapping safely:
    //        - orElse(defaultValue)        -> ALWAYS evaluates/constructs defaultValue eagerly, even if the
    //                                          Optional is present and the default is discarded.
    //        - orElseGet(supplier)         -> LAZY: the Supplier is only invoked if the Optional is empty.
    //        - orElseThrow(exceptionSupplier) -> throws a custom exception (via a Supplier) if empty, otherwise
    //                                          returns the value.
    //   3. Functional chaining:
    //        - map(fn)     -> transforms the wrapped value if present, otherwise stays empty; return type can
    //                         change (Optional<T> -> Optional<R>).
    //        - filter(pred)-> keeps the value only if it matches the predicate, otherwise becomes empty.
    //        - ifPresent(consumer) -> runs a side-effecting action only if a value is present, does nothing
    //                         otherwise (no branching needed by the caller).
    //   4. Never call get() blindly - prefer orElse/orElseGet/orElseThrow/ifPresent/map so that the empty case
    //      is always handled explicitly by the API rather than by an implicit runtime exception.

    // ================= WHY THIS API =================
    // Optional makes "this value might be absent" part of the method signature/type, forcing callers to
    // consciously deal with absence instead of silently risking a NullPointerException deep in unrelated code.
    // orElseGet's laziness matters for performance/correctness whenever the fallback is expensive to compute
    // (e.g., a DB call, a new object allocation, throwing/logging) - orElse would pay that cost every single
    // time regardless of whether it's needed.

    // ================= COMMON MISTAKES =================
    // 1. Calling optional.get() directly without isPresent() - throws NoSuchElementException on empty Optional,
    //    which is really just a null check that has been deferred and renamed, not eliminated.
    // 2. Using orElse(expensiveMethodCall()) - the expensive call executes even when the Optional has a value,
    //    silently wasting work (or, worse, causing side effects) on the "happy path".
    // 3. Using Optional fields in classes / Optional method parameters - Optional is designed as a RETURN TYPE
    //    for "value might be absent" results, not as a general-purpose nullable wrapper for fields or args.
    // 4. Calling Optional.of(null) - throws NPE immediately; should use ofNullable() when null is possible.
    // 5. Chaining isPresent() + get() instead of using map/ifPresent/orElse - defeats the purpose of the
    //    functional API and reintroduces the classic null-check style bugs (e.g., forgetting the check).
    // 6. Wrapping collections in Optional (e.g., Optional<List<T>>) instead of just returning an empty list -
    //    an empty collection is usually a better "no results" signal than Optional.empty().

    // ================= EDGE CASES =================
    // - Optional.empty() passed through map()/filter() -> pipeline short-circuits and stays empty, no NPE.
    // - orElse(null) -> legal, returns null if the Optional was empty (defeats some of the null-safety intent,
    //   worth calling out in interviews).
    // - Optional.ofNullable(null) -> becomes Optional.empty(), no exception (unlike Optional.of(null)).
    // - Chaining filter() with a predicate that never matches -> becomes empty even though a value was present.
    // - orElseThrow() with no arguments (Java 10+) throws NoSuchElementException by default; orElseThrow(Supplier)
    //   lets you throw a custom/checked-friendly exception.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) for all Optional operations shown here (creation, map, filter, orElse family) -
    // Optional is a thin wrapper, not a collection to iterate.
    // Space Complexity: O(1) - a single wrapper object holding at most one reference.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why shouldn't Optional be used for class fields or method parameters?
    // - What's the difference between Optional.of() and Optional.ofNullable()?
    // - When would orElse's eager evaluation actually cause a bug (not just a performance issue)?
    //   (e.g., orElse(computeAndLogSideEffect()) running even when present)
    // - How does Optional.flatMap differ from Optional.map, and when is flatMap required?
    //   (when the mapping function itself returns an Optional, to avoid Optional<Optional<T>>)
    // - How would you combine two Optionals (e.g., only proceed if both are present)?
    // - Is Optional serializable, and why does that matter for JavaBeans/entities?
    // - What does Optional.or() (Java 9+) do differently from orElseGet()? (returns another Optional lazily
    //   instead of unwrapping to a raw value)

    /**
     * Loop/imperative Approach: classic null-check style, shown for contrast with Optional.
     */
    public static String greetLoopStyle(String name) {
        // Step 1: manual null check instead of Optional
        String resolvedName;
        if (name != null) {
            resolvedName = name;
        } else {
            resolvedName = "Guest"; // fallback computed only when needed (naturally lazy in an if/else)
        }
        return "Hello, " + resolvedName;
    }

    /**
     * Optional Approach: creation, orElse vs orElseGet vs orElseThrow, map/filter/ifPresent chaining.
     */
    public static String greetOptionalStyle(String name) {
        // Step 1: ofNullable handles both a real name and a null gracefully (no exception either way)
        Optional<String> nameOpt = Optional.ofNullable(name);

        // Step 2: map transforms the value only if present; filter keeps it only if it passes the predicate
        Optional<String> validatedName = nameOpt
                .map(String::trim)
                .filter(n -> !n.isEmpty());

        // Step 3: orElseGet is LAZY - the supplier lambda only runs if validatedName is empty
        return "Hello, " + validatedName.orElseGet(() -> "Guest");
    }

    /**
     * Demonstrates the eager-vs-lazy difference between orElse and orElseGet explicitly.
     */
    public static void demonstrateEagerVsLazy() {
        Optional<String> present = Optional.of("Existing Value");

        System.out.println("--- orElse (EAGER) ---");
        // Step 1: expensiveFallback() executes here EVEN THOUGH 'present' already has a value
        String r1 = present.orElse(expensiveFallback());
        System.out.println("Result: " + r1);

        System.out.println("--- orElseGet (LAZY) ---");
        // Step 1: the lambda is never invoked because 'present' has a value - no wasted work
        String r2 = present.orElseGet(OptionalDeepDive::expensiveFallback);
        System.out.println("Result: " + r2);
    }

    private static String expensiveFallback() {
        System.out.println("  >>> expensiveFallback() was called!");
        return "Fallback Value";
    }

    /**
     * Demonstrates orElseThrow with a custom exception supplier.
     */
    public static String getOrThrow(Optional<String> optional) {
        // Step 1: orElseThrow with a Supplier<Exception> avoids constructing the exception unless needed
        return optional.orElseThrow(() -> new NoSuchElementException("No value present!"));
    }

    /**
     * Demonstrates the anti-pattern of calling get() without checking isPresent() first,
     * shown for educational contrast - do NOT do this in real code.
     */
    public static String unsafeGetAntiPattern(Optional<String> optional) {
        // ANTI-PATTERN: calling get() directly - throws NoSuchElementException if empty.
        // Prefer orElse/orElseGet/orElseThrow/ifPresent/map instead.
        return optional.get();
    }

    public static void main(String[] args) {
        System.out.println(greetLoopStyle(null));
        System.out.println(greetOptionalStyle(null));
        // Expected: "Hello, Guest" for both (name is null)

        System.out.println(greetOptionalStyle("  Alex  "));
        // Expected: "Hello, Alex" (trimmed)

        System.out.println(greetOptionalStyle("   "));
        // Expected: "Hello, Guest" (blank after trim is filtered out)

        demonstrateEagerVsLazy();
        // Expected: orElse prints ">>> expensiveFallback() was called!" even though present has a value;
        // orElseGet does NOT print that line at all, since the supplier is never invoked.

        try {
            getOrThrow(Optional.empty());
        } catch (NoSuchElementException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
            // Expected: "Caught expected exception: No value present!"
        }

        try {
            unsafeGetAntiPattern(Optional.empty());
        } catch (NoSuchElementException e) {
            System.out.println("Anti-pattern caused: " + e.getClass().getSimpleName());
            // Expected: "Anti-pattern caused: NoSuchElementException" - illustrating why get() is unsafe
        }

        System.out.println(Optional.ofNullable(null).isPresent());
        // Expected: false (ofNullable never throws, just becomes empty)
    }
}
