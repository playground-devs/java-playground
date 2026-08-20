package com.playground.java.interview.lld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PATTERN: Low-Level Design / OOP - Strategy Pattern
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Design a checkout/payment processing system that can process a payment
 * amount using any of several interchangeable payment methods, chosen at runtime.
 */
public class PaymentProcessingStrategy {

    // ================= PROBLEM / REQUIREMENTS =================
    // - Support multiple payment methods: Credit Card, UPI (or PayPal), Wallet.
    // - A checkout/processor component must accept a payment method chosen at runtime
    //   and process a given amount using that method.
    // - Each payment method has its own validation and processing logic
    //   (e.g. card number/CVV checks, UPI id checks, wallet balance checks).
    // - Adding a brand new payment method (e.g. NetBanking) must NOT require modifying
    //   the processor or any existing payment method class (Open/Closed Principle).
    // - Return a structured result (success/failure + transaction id + message) for every payment.
    // - Track a simple transaction history for auditing.

    // ================= SIMPLE APPROACH =================
    // A single PaymentProcessor class with a method like:
    //   processPayment(String type, double amount, Map<String,String> details)
    // containing a big if/else or switch on "type" that inlines the validation and
    // "processing" logic for every payment method directly inside that one method.

    // ================= WHY IT'S NOT ENOUGH =================
    // - Violates Single Responsibility: one class knows the internal validation rules
    //   of every payment method that will ever exist.
    // - Violates Open/Closed Principle: adding a new payment method means editing the
    //   big switch statement inside PaymentProcessor, risking regressions in existing
    //   payment paths.
    // - Hard to unit test a single payment method in isolation.
    // - No way to swap/configure payment behavior at runtime without touching the
    //   processor's source code (e.g. plugging in a new gateway per merchant).

    // ================= OPTIMIZED DESIGN =================
    // - PaymentStrategy: interface with pay(double amount) -> PaymentResult. Each concrete
    //   payment method implements this interface independently (Strategy Pattern).
    // - CreditCardPayment, UpiPayment, WalletPayment: concrete strategies encapsulating
    //   their own validation + "gateway call" simulation.
    // - PaymentResult: immutable value object capturing success flag, transaction id,
    //   message, and amount.
    // - PaymentProcessor: the context class. Holds a reference to a PaymentStrategy
    //   (injected/selected at runtime via setStrategy or checkout(strategy, amount)) and
    //   simply delegates to strategy.pay(amount). Also keeps a transaction log.
    // - To add a new payment method (e.g. NetBankingPayment), we just implement
    //   PaymentStrategy in a new class - zero changes to PaymentProcessor or existing
    //   strategies.

    // ================= WHY THIS DESIGN =================
    // - Open/Closed Principle: PaymentProcessor is closed for modification but open for
    //   extension - new strategies are added, not edited-in.
    // - Single Responsibility: each strategy owns only its own validation/processing logic.
    // - Liskov Substitution: any PaymentStrategy implementation can replace another without
    //   breaking PaymentProcessor's expectations.
    // - Dependency Inversion: PaymentProcessor depends on the PaymentStrategy abstraction,
    //   not on concrete payment classes.
    // - Runtime flexibility: the strategy can be chosen per-request (e.g. based on user
    //   selection in a UI) without any conditional logic in the processor.

    // ================= EDGE CASES =================
    // - Zero or negative payment amount -> reject before delegating to strategy.
    // - Insufficient wallet balance -> WalletPayment reports failure with a clear message.
    // - Invalid card number / expired card -> CreditCardPayment reports failure.
    // - Invalid UPI id format -> UpiPayment reports failure.
    // - No strategy set on the processor -> checkout should throw/reject clearly.
    // - Concurrent checkouts against a shared WalletPayment balance (multiple threads
    //   debiting the same wallet) must not double-spend or corrupt the balance.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) per payment (excluding simulated gateway/network calls).
    // Space Complexity: O(n) for the transaction history, where n = number of payments processed.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you make WalletPayment's balance updates thread-safe under concurrent
    //   checkouts (e.g. synchronized methods, AtomicReference/compare-and-swap, or a
    //   per-wallet lock)?
    // - How would you add a brand-new payment method (e.g. NetBanking, BuyNowPayLater)
    //   without touching PaymentProcessor or existing strategies?
    // - How would you support partial payments or splitting one amount across two strategies?
    // - How would you add retry logic with exponential backoff for transient gateway failures?
    // - How would you plug in a real payment gateway SDK behind each strategy while keeping
    //   the PaymentStrategy interface unchanged?
    // - How would you make payments idempotent so a client retry doesn't double-charge?
    // - How would you audit/log every transaction for compliance (PCI-DSS considerations
    //   for credit card data)?
    // - How would you combine Strategy with Factory so the processor selects a strategy
    //   from a string key (e.g. "CREDIT_CARD") instead of the caller constructing it?

    /** Strategy interface: every concrete payment method implements this. */
    public interface PaymentStrategy {
        PaymentResult pay(double amount);
        String methodName();
    }

    /** Immutable result of a payment attempt. */
    public static final class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;
        private final double amount;

        public PaymentResult(boolean success, String transactionId, String message, double amount) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.amount = amount;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return String.format("PaymentResult{success=%s, txnId=%s, amount=%.2f, message='%s'}",
                    success, transactionId, amount, message);
        }
    }

    /** Credit card payment strategy. */
    public static class CreditCardPayment implements PaymentStrategy {
        private final String cardNumber;
        private final String cvv;

        public CreditCardPayment(String cardNumber, String cvv) {
            this.cardNumber = cardNumber;
            this.cvv = cvv;
        }

        @Override
        public PaymentResult pay(double amount) {
            // Basic validation - a real system would call out to a card network/gateway.
            if (cardNumber == null || cardNumber.length() != 16) {
                return new PaymentResult(false, null, "Invalid card number", amount);
            }
            if (cvv == null || cvv.length() != 3) {
                return new PaymentResult(false, null, "Invalid CVV", amount);
            }
            String txnId = "CC-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Charged credit card ending " + cardNumber.substring(12), amount);
        }

        @Override
        public String methodName() {
            return "CREDIT_CARD";
        }
    }

    /** UPI payment strategy. */
    public static class UpiPayment implements PaymentStrategy {
        private final String upiId;

        public UpiPayment(String upiId) {
            this.upiId = upiId;
        }

        @Override
        public PaymentResult pay(double amount) {
            // A real UPI id looks like "name@bank" - simple format check here.
            if (upiId == null || !upiId.contains("@")) {
                return new PaymentResult(false, null, "Invalid UPI id", amount);
            }
            String txnId = "UPI-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Debited via UPI id " + upiId, amount);
        }

        @Override
        public String methodName() {
            return "UPI";
        }
    }

    /** Wallet payment strategy backed by an in-memory balance. */
    public static class WalletPayment implements PaymentStrategy {
        private double balance;

        public WalletPayment(double initialBalance) {
            this.balance = initialBalance;
        }

        // Synchronized to keep the debit-check-then-update sequence atomic under
        // concurrent checkouts against the same wallet instance.
        @Override
        public synchronized PaymentResult pay(double amount) {
            if (amount > balance) {
                return new PaymentResult(false, null,
                        String.format("Insufficient wallet balance (available=%.2f, required=%.2f)", balance, amount),
                        amount);
            }
            balance -= amount;
            String txnId = "WALLET-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txnId, "Debited from wallet, remaining balance=" + balance, amount);
        }

        @Override
        public String methodName() {
            return "WALLET";
        }
    }

    /**
     * Context class: delegates to whichever PaymentStrategy is supplied at checkout time.
     * Adding a new payment method never requires changing this class.
     */
    public static class PaymentProcessor {
        private final Map<String, PaymentResult> transactionLog = new HashMap<>();

        public PaymentResult checkout(PaymentStrategy strategy, double amount) {
            if (strategy == null) {
                throw new IllegalArgumentException("Payment strategy must not be null");
            }
            if (amount <= 0) {
                return new PaymentResult(false, null, "Payment amount must be positive", amount);
            }
            PaymentResult result = strategy.pay(amount);
            String key = result.transactionId != null ? result.transactionId
                    : ("FAILED-" + UUID.randomUUID().toString().substring(0, 8));
            transactionLog.put(key, result);
            System.out.println("[" + strategy.methodName() + "] " + result);
            return result;
        }

        public int transactionCount() {
            return transactionLog.size();
        }
    }

    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();

        // 1. Pay via credit card - valid card, should succeed.
        PaymentResult r1 = processor.checkout(new CreditCardPayment("1234567812345678", "123"), 150.00);
        System.out.println("r1 success = " + r1.isSuccess()); // Expected: r1 success = true

        // 2. Pay via UPI - valid id, should succeed.
        PaymentResult r2 = processor.checkout(new UpiPayment("ram@okbank"), 75.50);
        System.out.println("r2 success = " + r2.isSuccess()); // Expected: r2 success = true

        // 3. Pay via wallet with sufficient balance - should succeed.
        WalletPayment wallet = new WalletPayment(100.00);
        PaymentResult r3 = processor.checkout(wallet, 40.00);
        System.out.println("r3 success = " + r3.isSuccess()); // Expected: r3 success = true

        // 4. Pay via same wallet exceeding remaining balance (100 - 40 = 60 left) - should fail.
        PaymentResult r4 = processor.checkout(wallet, 90.00);
        System.out.println("r4 success = " + r4.isSuccess()); // Expected: r4 success = false

        // 5. Invalid credit card number - should fail without touching wallet/UPI code.
        PaymentResult r5 = processor.checkout(new CreditCardPayment("1234", "123"), 20.00);
        System.out.println("r5 success = " + r5.isSuccess()); // Expected: r5 success = false

        System.out.println("Total transactions logged: " + processor.transactionCount()); // Expected: Total transactions logged: 5
    }
}
