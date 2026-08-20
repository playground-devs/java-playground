package com.playground.java.interview.java8;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * PATTERN: Immutable Object
 * PRIORITY: P0 - Must Know
 * TOPIC: Building a truly immutable Money class with defensive copying of mutable fields.
 */
public class ImmutableClassExample {

    // ================= WHAT IS BEING TESTED =================
    // Ability to construct a class that is genuinely immutable even in the presence of
    // mutable field types (List, Date), not just "final fields with no setters" superficially.

    // ================= APPROACH =================
    // 1. Declare the class `final` so it cannot be subclassed to add mutable state or
    //    override behavior in a way that breaks the immutability guarantee.
    // 2. Make every field `private final`.
    // 3. Provide no setters - only a constructor and getters.
    // 4. For mutable field types (Date, List, arrays, etc.):
    //    a. Defensively COPY the input in the constructor, so external mutation of the
    //       caller's original reference cannot affect this object's internal state.
    //    b. Defensively COPY (or wrap unmodifiable) the field again in the getter, so the
    //       caller cannot mutate this object's internals via the returned reference.
    // 5. For genuinely immutable field types (BigDecimal, String, primitives, enums),
    //    simple assignment is sufficient - no defensive copy needed.

    // ================= WHY THIS MATTERS =================
    // Immutable objects are inherently thread-safe (no synchronization needed for concurrent
    // reads), safe to share/cache/use as Map keys, and eliminate a whole class of bugs where
    // one part of the system mutates an object another part still depends on. This is
    // critical in backend services handling shared config, monetary values, DTOs passed
    // across thread pools/executors, and event payloads published to multiple consumers.

    // ================= COMMON MISTAKES =================
    // - Declaring fields `final` but exposing a mutable field type directly via getter (e.g. `return this.tags;`) -> caller can still mutate internal state.
    // - Copying the mutable input in the constructor but forgetting to copy again in the getter (or vice versa) -> only "half-defended."
    // - Not declaring the class `final`, allowing a subclass to add mutable state or override a getter to leak internals.
    // - Using `Collections.unmodifiableList(list)` as the ONLY defense -> it wraps the SAME backing list, so external mutation of the original list still leaks through; a real copy is required.

    public static final class Money {
        private final BigDecimal amount;
        private final String currency;
        private final Date lastModified;      // mutable JDK type - needs defensive copy
        private final List<String> auditTrail; // mutable collection - needs defensive copy

        public Money(BigDecimal amount, String currency, Date lastModified, List<String> auditTrail) {
            // BigDecimal and String are themselves immutable -> safe to assign directly.
            this.amount = amount;
            this.currency = currency;
            // Defensive copy #1: constructor-time copy protects against the CALLER mutating
            // the Date/List they passed in AFTER construction.
            this.lastModified = new Date(lastModified.getTime());
            this.auditTrail = new ArrayList<>(auditTrail);
        }

        public BigDecimal getAmount() {
            return amount; // BigDecimal is immutable - safe to return directly
        }

        public String getCurrency() {
            return currency; // String is immutable - safe to return directly
        }

        public Date getLastModified() {
            // Defensive copy #2: getter-time copy protects THIS object's internal state
            // from being mutated by the caller through the returned reference.
            return new Date(lastModified.getTime());
        }

        public List<String> getAuditTrail() {
            // Return an unmodifiable VIEW of a fresh copy - belt-and-braces: even if a
            // caller tries list.add(...), it fails fast with UnsupportedOperationException
            // rather than silently succeeding on a shared backing list.
            return Collections.unmodifiableList(new ArrayList<>(auditTrail));
        }

        // Immutable "mutators" return a NEW instance instead of modifying this one.
        public Money withAdditionalAuditEntry(String entry) {
            List<String> newTrail = new ArrayList<>(this.auditTrail);
            newTrail.add(entry);
            return new Money(this.amount, this.currency, this.lastModified, newTrail);
        }

        @Override
        public String toString() {
            return "Money{amount=" + amount + ", currency='" + currency
                    + "', lastModified=" + lastModified + ", auditTrail=" + auditTrail + "}";
        }
    }

    // ================= EDGE CASES =================
    // - Caller mutates the Date/List object AFTER passing it to the constructor -> must NOT
    //   affect the Money instance (verified in main() below).
    // - Caller mutates the List/Date object returned BY a getter -> must NOT affect the
    //   Money instance's internal state, and for the List getter it should throw
    //   UnsupportedOperationException on mutation attempts.
    // - Null input to constructor for a mutable field -> would NPE on defensive copy;
    //   production code should validate with Objects.requireNonNull() first.

    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for each defensive copy of the audit trail list (n = list size);
    //                  O(1) for the Date copy and all other field assignments.
    // Space Complexity: O(n) extra space per defensive copy of the list (constructor copy +
    //                  each getter call allocates a new list), trading memory for safety.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is `Collections.unmodifiableList(originalList)` alone NOT sufficient for immutability?
    // - Why must the class itself be declared `final`?
    // - How do immutable objects simplify concurrent programming (no locks needed for reads)?
    // - What is the performance cost of defensive copying, and how would you mitigate it for hot paths (e.g. copy-on-write, persistent/immutable collection libraries)?
    // - How does Java's String immutability enable safe string pool interning and use as HashMap keys?
    // - How would you implement a "wither" method pattern (withX()) to simulate mutation on an immutable object?
    // - What role does immutability play in Java's `record` feature (Java 16+), and why doesn't a record alone guarantee deep immutability?

    public static void main(String[] args) {
        List<String> initialTrail = new ArrayList<>();
        initialTrail.add("CREATED");
        Date initialDate = new Date(1_700_000_000_000L);

        Money money = new Money(new BigDecimal("199.99"), "USD", initialDate, initialTrail);
        System.out.println("Initial money = " + money);
        // Expected: Money{amount=199.99, currency='USD', lastModified=..., auditTrail=[CREATED]}

        // Example 1: mutate the caller's ORIGINAL list/date after construction - must NOT affect `money`.
        initialTrail.add("HACKED_ENTRY");
        initialDate.setTime(0L);
        System.out.println("After external mutation, money = " + money);
        // Expected: unchanged -> auditTrail=[CREATED], lastModified reflects original 1_700_000_000_000L

        // Example 2: mutate the reference returned BY the getter - must NOT affect internal state.
        Date leaked = money.getLastModified();
        leaked.setTime(0L);
        System.out.println("Internal lastModified still safe: " + money.getLastModified());
        // Expected: still reflects original timestamp, not 0L

        try {
            money.getAuditTrail().add("SHOULD_FAIL");
        } catch (UnsupportedOperationException ex) {
            System.out.println("Caught expected UnsupportedOperationException on auditTrail mutation attempt");
            // Expected: this line prints, confirming the returned list is unmodifiable
        }

        // Example 3: "mutation" via returning a new instance (immutable update pattern).
        Money updated = money.withAdditionalAuditEntry("REVIEWED");
        System.out.println("Original money unchanged = " + money);   // Expected: auditTrail=[CREATED]
        System.out.println("Updated money (new instance) = " + updated); // Expected: auditTrail=[CREATED, REVIEWED]
    }
}
