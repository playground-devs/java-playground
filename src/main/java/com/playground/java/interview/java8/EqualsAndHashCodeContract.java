package com.playground.java.interview.java8;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * PATTERN: Value Semantics via equals()/hashCode() contract
 * PRIORITY: P0 - Must Know
 * TOPIC: Correctly implementing equals()/hashCode() together, and demonstrating what breaks
 * in a HashSet/HashMap when only equals() is overridden.
 */
public class EqualsAndHashCodeContract {

    // ================= WHAT IS BEING TESTED =================
    // Understanding of the formal equals()/hashCode() contract from Object's Javadoc, and
    // the ability to demonstrate concretely (not just recite) why breaking the contract
    // corrupts hash-based collections like HashSet/HashMap.

    // ================= APPROACH =================
    // 1. Implement Employee with equals()/hashCode() BOTH overridden consistently, based on
    //    the business identity field (id): two Employees with the same id are considered equal.
    // 2. Implement BrokenEmployee that overrides ONLY equals() (by id) and leaves hashCode()
    //    at Object's default (identity-based, essentially unique per instance).
    // 3. Add two "equal" (same id) instances of BrokenEmployee to a HashSet and show the
    //    set ends up with size 2 (both accepted) because they hash to different buckets,
    //    so HashSet never even calls equals() to detect the duplicate.
    // 4. Repeat with the correctly-implemented Employee and show the HashSet correctly
    //    rejects the duplicate (size 1).
    // 5. State the 5 contract rules explicitly: reflexive, symmetric, transitive, consistent,
    //    and "equal objects must have equal hashCodes" (the critical asymmetric rule -
    //    the reverse is NOT required: unequal objects MAY share a hashCode, i.e. a collision).

    // ================= WHY THIS MATTERS =================
    // HashSet/HashMap/Hashtable/ConcurrentHashMap all rely on hashCode() to locate the
    // correct bucket BEFORE ever calling equals() to compare candidates within that bucket.
    // If hashCode() is inconsistent with equals(), "equal" objects can land in different
    // buckets, silently causing duplicate entries, failed lookups (map.get() returns null
    // for a key that IS present), and cache/deduplication logic that quietly stops working -
    // a classic, hard-to-diagnose production bug in backend services using domain objects as
    // Set/Map keys (e.g. deduplicating orders, caching by composite key).

    // ================= COMMON MISTAKES =================
    // - Overriding equals() without overriding hashCode() (or vice versa) -> breaks the contract immediately.
    // - Using mutable fields in hashCode()/equals() for objects stored as HashMap/HashSet keys, then mutating the field after insertion -> the entry becomes "lost" (wrong bucket for its now-current state).
    // - Not using `instanceof` / class-check + null-check correctly in equals(), causing ClassCastException or violating symmetry (a.equals(b) true but b.equals(a) false).
    // - Implementing hashCode() that returns a constant (e.g. always 0) -> technically legal (consistent) but destroys HashMap/HashSet performance by forcing every entry into one bucket (O(n) lookups instead of O(1)).

    /**
     * BrokenEmployee: overrides equals() (business-identity by id) but leaves hashCode()
     * as Object's default. This VIOLATES the contract: two objects that are equals()-equal
     * do NOT have equal hashCode() values.
     */
    static class BrokenEmployee {
        private final int id;
        private final String name;

        BrokenEmployee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenEmployee)) return false;
            BrokenEmployee other = (BrokenEmployee) o;
            return this.id == other.id;
        }
        // hashCode() intentionally NOT overridden here -> uses Object's identity hash.
        // This is the bug we are demonstrating.

        @Override
        public String toString() {
            return "BrokenEmployee{id=" + id + ", name='" + name + "'}";
        }
    }

    /**
     * Employee: correctly overrides BOTH equals() and hashCode(), consistently based on
     * the same field(s) - here, business identity `id`.
     */
    static class Employee {
        private final int id;
        private final String name;

        Employee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            // Reflexive: x.equals(x) must be true.
            if (this == o) return true;
            // Handles null and different-type comparisons (symmetry-safe via instanceof).
            if (!(o instanceof Employee)) return false;
            Employee other = (Employee) o;
            // Symmetric + transitive: equality based purely on immutable business key `id`.
            return this.id == other.id;
        }

        @Override
        public int hashCode() {
            // MUST be derived from the exact same field(s) used in equals(), so that
            // equal objects are GUARANTEED to produce equal hash codes.
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Employee{id=" + id + ", name='" + name + "'}";
        }
    }

    // ================= EDGE CASES =================
    // - Comparing an object to `null` -> equals() must return false, never throw.
    // - Comparing to an instance of a different (even related/subclass) type -> should
    //   generally return false using `instanceof` checks (getClass() checks are stricter
    //   and matter for inheritance hierarchies, a common follow-up discussion point).
    // - Two distinct objects that are NOT equals()-equal but happen to share a hashCode()
    //   (a legal hash collision) -> HashSet correctly falls back to equals() to tell them apart.
    // - Mutating a field used in hashCode() AFTER the object has been inserted into a HashSet/HashMap -> the object becomes unfindable at its "new" hash bucket (a documented Java gotcha).

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) average for HashSet/HashMap add/contains/get when hashCode()
    //                  is well-distributed and the contract is honored (O(n) worst case
    //                  under pathological collisions, e.g. constant hashCode()).
    // Space Complexity: O(n) for storing n elements in the backing hash table (plus bucket overhead).

    // ================= INTERVIEW FOLLOW-UPS =================
    // - State the equals()/hashCode() contract rules from memory (reflexive, symmetric, transitive, consistent, equal->equal hashCode).
    // - Why is the reverse of the hashCode rule NOT required (i.e., can unequal objects share a hashCode)?
    // - Walk through, step by step, how HashSet.add() uses hashCode() and equals() together to detect a duplicate.
    // - What happens if you mutate a field that hashCode()/equals() depends on, after the object is already a HashMap key?
    // - Why should you generally use immutable fields (or immutable objects entirely) for HashMap/HashSet keys?
    // - Compare `instanceof` vs `getClass() ==` checks inside equals() - which is safer for inheritance and why?
    // - How does Objects.hash(...) work internally, and what are its performance trade-offs vs a hand-rolled hashCode()?

    public static void main(String[] args) {
        // Example 1: BrokenEmployee - equals() overridden, hashCode() NOT overridden.
        Set<BrokenEmployee> brokenSet = new HashSet<>();
        BrokenEmployee b1 = new BrokenEmployee(101, "Asha");
        BrokenEmployee b2 = new BrokenEmployee(101, "Asha"); // same id -> equals() says TRUE
        System.out.println("b1.equals(b2) = " + b1.equals(b2)); // Expected: true
        brokenSet.add(b1);
        brokenSet.add(b2);
        System.out.println("brokenSet.size() = " + brokenSet.size());
        // Expected: 2  <-- BUG: "equal" objects both got added because hashCode() differs,
        // so HashSet placed them in different buckets and never even called equals().

        // Example 2: Employee - equals() AND hashCode() both correctly overridden.
        Set<Employee> correctSet = new HashSet<>();
        Employee e1 = new Employee(101, "Asha");
        Employee e2 = new Employee(101, "Asha"); // same id -> equals() TRUE, hashCode() equal too
        System.out.println("e1.equals(e2) = " + e1.equals(e2)); // Expected: true
        System.out.println("e1.hashCode() == e2.hashCode() = " + (e1.hashCode() == e2.hashCode())); // Expected: true
        correctSet.add(e1);
        correctSet.add(e2);
        System.out.println("correctSet.size() = " + correctSet.size());
        // Expected: 1  <-- CORRECT: duplicate correctly rejected because both objects hash
        // to the SAME bucket, allowing HashSet to actually invoke equals() and detect the match.

        // Example 3: HashMap lookup failure scenario caused by the same root bug.
        java.util.Map<BrokenEmployee, String> brokenMap = new java.util.HashMap<>();
        brokenMap.put(new BrokenEmployee(202, "Rahul"), "Engineering");
        String lookup = brokenMap.get(new BrokenEmployee(202, "Rahul")); // "equal" key, different instance
        System.out.println("brokenMap.get(equal-but-different-instance key) = " + lookup);
        // Expected: null  <-- BUG: get() fails to find the "equal" entry because hashCode()
        // differs between the two BrokenEmployee instances, so the lookup checks the wrong bucket.
    }
}
