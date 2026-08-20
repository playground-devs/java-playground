package com.playground.java.interview.java8;

/**
 * PATTERN: Custom Exception Hierarchy (Checked vs Unchecked)
 * PRIORITY: P1 - High Priority
 * TOPIC: Designing a custom checked exception and a custom unchecked exception, and
 * choosing correctly between them in backend API design.
 */
public class CustomCheckedUncheckedException {

    // ================= WHAT IS BEING TESTED =================
    // Ability to design domain-specific exceptions correctly, and to articulate WHEN a
    // checked vs. an unchecked exception is the appropriate choice in a backend service.

    // ================= APPROACH =================
    // 1. Define InsufficientFundsException extending Exception (checked): represents an
    //    EXPECTED, RECOVERABLE business condition - the caller is expected to handle it
    //    (e.g. show a "insufficient funds" message, retry with a different account).
    // 2. Define InvalidOrderStateException extending RuntimeException (unchecked):
    //    represents a PROGRAMMING ERROR or invariant violation - e.g. calling ship() on an
    //    order that was never paid. The caller generally cannot "recover" meaningfully;
    //    it indicates a bug in the calling code or corrupted state.
    // 3. Add a meaningful constructor set (message, message+cause) and extra context fields
    //    (e.g. accountId, requiredAmount) to make the exception actionable/loggable.
    // 4. Write methods that throw each, and show the different handling styles required:
    //    checked exceptions force a try-catch or `throws` declaration at compile time;
    //    unchecked exceptions do not, and typically bubble up to a global handler/logger.

    // ================= WHY THIS MATTERS =================
    // In real backend/API design (e.g. Spring REST controllers), the checked vs. unchecked
    // choice directly shapes the API contract: checked exceptions force every caller up the
    // stack to explicitly acknowledge an expected failure mode (e.g. a payment API where
    // insufficient funds is a normal, expected outcome that MUST be handled), whereas
    // unchecked exceptions represent bugs/invariant violations that should be caught centrally
    // (e.g. a global @ExceptionHandler) rather than littering every call site with boilerplate
    // catch blocks for conditions that "should never happen" in correct code.

    // ================= COMMON MISTAKES =================
    // - Using checked exceptions for programming errors (e.g. NullPointerException-like bugs) -> forces defensive try-catch everywhere and pollutes method signatures with `throws`.
    // - Using unchecked exceptions for expected, common business outcomes (e.g. "insufficient funds" happens routinely) -> callers can silently forget to handle it, causing unhandled failures in production.
    // - Swallowing the original cause by not chaining it (`new MyException(msg)` instead of `new MyException(msg, cause)`) -> loses the root-cause stack trace needed for debugging.
    // - Catching a checked exception too broadly (`catch (Exception e)`) instead of the specific type -> masks unrelated bugs and defeats the purpose of a typed checked exception.

    /**
     * Checked exception: represents an expected, recoverable business condition.
     * Extends Exception (NOT RuntimeException) so the compiler forces every caller to
     * either handle it or explicitly declare `throws InsufficientFundsException`.
     */
    static class InsufficientFundsException extends Exception {
        private final String accountId;
        private final double shortfall;

        public InsufficientFundsException(String accountId, double shortfall) {
            super("Account " + accountId + " is short by " + shortfall);
            this.accountId = accountId;
            this.shortfall = shortfall;
        }

        public InsufficientFundsException(String accountId, double shortfall, Throwable cause) {
            super("Account " + accountId + " is short by " + shortfall, cause);
            this.accountId = accountId;
            this.shortfall = shortfall;
        }

        public String getAccountId() {
            return accountId;
        }

        public double getShortfall() {
            return shortfall;
        }
    }

    /**
     * Unchecked exception: represents a programming error / invariant violation that
     * should not normally occur if the calling code is correct. Extends RuntimeException
     * so it does NOT force callers to catch or declare it.
     */
    static class InvalidOrderStateException extends RuntimeException {
        private final String orderId;
        private final String currentState;

        public InvalidOrderStateException(String orderId, String currentState, String attemptedAction) {
            super("Cannot perform '" + attemptedAction + "' on order " + orderId
                    + " because it is in state '" + currentState + "'");
            this.orderId = orderId;
            this.currentState = currentState;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCurrentState() {
            return currentState;
        }
    }

    // Simulated bank account service - withdraw() can FAIL for a normal, expected reason.
    static class BankAccountService {
        double withdraw(String accountId, double balance, double requestedAmount)
                throws InsufficientFundsException {
            // Checked exception: caller MUST handle or propagate - this is a routine,
            // expected outcome of a withdraw operation, not a bug.
            if (requestedAmount > balance) {
                throw new InsufficientFundsException(accountId, requestedAmount - balance);
            }
            return balance - requestedAmount;
        }
    }

    // Simulated order service - shipping an unpaid order is a BUG in the calling code
    // (an invariant that should have been checked before calling ship()).
    static class OrderService {
        void ship(String orderId, String currentState) {
            if (!"PAID".equals(currentState)) {
                // Unchecked exception: no `throws` declaration needed, signals a defect.
                throw new InvalidOrderStateException(orderId, currentState, "SHIP");
            }
            System.out.println("Order " + orderId + " shipped successfully.");
        }
    }

    // ================= EDGE CASES =================
    // - Chaining a lower-level checked exception (e.g. a DB timeout) into a domain-specific
    //   checked exception via the (message, cause) constructor, preserving the root cause.
    // - Unchecked exceptions thrown deep in a call stack with no try-catch anywhere ->
    //   propagate all the way to the thread's UncaughtExceptionHandler / framework's global handler.
    // - Catching InsufficientFundsException but ignoring/losing the shortfall amount ->
    //   defeats the purpose of adding contextual fields to the exception.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) not applicable in the algorithmic sense - exception construction
    //                  and throw/catch are constant-time operations (stack trace capture is
    //                  the dominant cost, proportional to current call-stack depth).
    // Space Complexity: O(d) where d = call stack depth, due to the captured stack trace
    //                  stored in each Throwable instance.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - What is the concrete rule you use to decide checked vs. unchecked for a new exception type?
    // - Why do many modern frameworks (Spring, most Java 8+ style APIs) lean toward unchecked exceptions even for some "expected" failures?
    // - What is exception chaining (initCause / (message, cause) constructor) and why does it matter for root-cause analysis?
    // - How do checked exceptions interact poorly with functional interfaces/lambdas (e.g. Stream.map()) and how would you work around that?
    // - What is the performance cost of exceptions, and why should they not be used for routine control flow?
    // - How would you design a global exception handler (e.g. Spring @ControllerAdvice) to map these two exception types to different HTTP status codes?
    // - What's the difference between Error, Exception, and RuntimeException in the Throwable hierarchy, and should you ever catch Error?

    public static void main(String[] args) {
        BankAccountService bankAccountService = new BankAccountService();
        OrderService orderService = new OrderService();

        // Example 1: checked exception - caller MUST handle (compiler-enforced).
        try {
            double newBalance = bankAccountService.withdraw("ACC-1001", 100.0, 250.0);
            System.out.println("New balance = " + newBalance);
        } catch (InsufficientFundsException e) {
            System.out.println("Caught checked exception: " + e.getMessage()
                    + " (shortfall=" + e.getShortfall() + ")");
            // Expected: Caught checked exception: Account ACC-1001 is short by 150.0 (shortfall=150.0)
        }

        // Example 2: checked exception - success path, no exception thrown.
        try {
            double newBalance = bankAccountService.withdraw("ACC-1002", 500.0, 200.0);
            System.out.println("New balance = " + newBalance); // Expected: New balance = 300.0
        } catch (InsufficientFundsException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        // Example 3: unchecked exception - no `throws` declaration required anywhere;
        // typically caught at a coarse-grained boundary (or not caught at all, here we catch
        // it to demonstrate the failure without crashing main()).
        try {
            orderService.ship("ORD-5001", "PENDING_PAYMENT");
        } catch (InvalidOrderStateException e) {
            System.out.println("Caught unchecked exception: " + e.getMessage());
            // Expected: Caught unchecked exception: Cannot perform 'SHIP' on order ORD-5001 because it is in state 'PENDING_PAYMENT'
        }

        // Example 4: unchecked exception - success path.
        orderService.ship("ORD-5002", "PAID"); // Expected: Order ORD-5002 shipped successfully.
    }
}
