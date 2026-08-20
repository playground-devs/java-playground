package com.playground.java.interview.lld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Low-Level Design / OOP
 * PRIORITY: P2 - Good to Know
 * Track product stock levels by SKU and automatically surface products that have fallen at/below their reorder threshold.
 */
public class InventoryManagementSystem {

    // ================= PROBLEM / REQUIREMENTS =================
    // - Track products uniquely identified by SKU, each with a current quantity and a reorder threshold.
    // - Support adding stock (restock) for a SKU.
    // - Support removing stock (sale/consumption) for a SKU; reject the operation if insufficient quantity is available.
    // - Provide a low-stock alert list: all products whose quantity is at or below their reorder threshold.
    // - Support registering new products and looking up current stock for a SKU.

    // ================= SIMPLE APPROACH =================
    // A single god-class holding a Map<String, Integer> of SKU -> quantity and a Map<String, Integer> of SKU -> threshold,
    // with add/remove methods directly mutating the maps and a loop over all keys every time an alert list is needed.
    // No dedicated Product abstraction; SKU metadata (name, threshold, quantity) is scattered across parallel maps.

    // ================= WHY IT'S NOT ENOUGH =================
    // - Parallel maps keyed by SKU are error-prone: easy to update one map and forget the other, causing them to drift.
    // - No single owner of "product" invariants (e.g. quantity can never go negative) - validation logic gets duplicated
    //   wherever the maps are touched.
    // - Hard to extend with new per-product behavior (e.g. expiry dates, supplier info) without adding yet another
    //   parallel map, which violates Single Responsibility and Open/Closed principles.
    // - Concurrent access from multiple threads (e.g. multiple order-processing workers) is unsafe without explicit
    //   per-SKU synchronization, and a naive global lock would serialize unrelated SKUs unnecessarily.

    // ================= OPTIMIZED DESIGN =================
    // - Product: a nested class encapsulating SKU, display name, current quantity, and reorder threshold, with its own
    //   addStock/removeStock methods enforcing invariants (quantity never negative) and a per-product lock for thread safety.
    // - InventoryManager: owns a Map<String, Product> keyed by SKU; exposes registerProduct, addStock, removeStock,
    //   getQuantity, and getLowStockAlerts. Uses ConcurrentHashMap so concurrent lookups/registrations across different
    //   SKUs don't contend on a single lock.
    // - StockOperationResult: a small value type (via enum StockOperationStatus) returned by addStock/removeStock so
    //   callers can distinguish SUCCESS from INSUFFICIENT_STOCK or UNKNOWN_SKU without relying on exceptions for
    //   expected business outcomes.
    // This is primarily an Encapsulation-driven design (Product owns its own state and invariants); no heavyweight
    // GoF pattern is required for a system this small, which itself is a valid interview answer.

    // ================= WHY THIS DESIGN =================
    // - Single Responsibility: Product owns quantity/threshold invariants; InventoryManager owns the SKU->Product
    //   registry and cross-cutting queries (low-stock scan).
    // - Open/Closed: new per-product attributes (expiry, supplier) can be added to Product without touching
    //   InventoryManager's public API.
    // - Encapsulation prevents quantity drift: all mutation goes through Product.addStock/removeStock, which validate
    //   before mutating, so the "quantity never negative" invariant can never be violated from outside.
    // - Per-product locking (ReentrantLock inside Product) allows concurrent stock updates on different SKUs to proceed
    //   in parallel while still serializing updates to the same SKU.

    // ================= EDGE CASES =================
    // - Removing stock greater than current quantity -> rejected, quantity unchanged, INSUFFICIENT_STOCK returned.
    // - Removing/adding stock for an unknown SKU -> UNKNOWN_SKU returned, no exception thrown for this expected case.
    // - Adding/removing a negative or zero quantity -> rejected with an IllegalArgumentException (programmer error,
    //   not a business outcome).
    // - Product exactly at the reorder threshold -> included in the low-stock alert list ("at or below").
    // - Registering a SKU that already exists -> rejected to avoid silently overwriting existing stock counts.
    // - Concurrent addStock/removeStock calls on the same SKU from multiple threads -> serialized correctly via the
    //   product's own lock; concurrent calls on different SKUs proceed independently.

    // ================= COMPLEXITY =================
    // Time Complexity: O(1) for registerProduct, addStock, removeStock, getQuantity (hash map + per-product lock).
    //                  O(n) for getLowStockAlerts, where n is the number of distinct products.
    // Space Complexity: O(n) for storing n products.

    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you make this thread-safe under high concurrency without a single global lock? (Per-product
    //   ReentrantLock + ConcurrentHashMap, as implemented, so unrelated SKUs never contend.)
    // - How would you persist inventory across restarts? (Swap the in-memory map for a repository interface backed
    //   by a database; Product stays the same, only the storage layer changes.)
    // - How would you extend this to support multiple warehouses/locations per SKU? (Key by (SKU, locationId) or add
    //   a Map<Location, Integer> inside Product, aggregating for the global low-stock view.)
    // - How would you avoid lost updates if two threads read-then-write quantity concurrently? (Mutation happens
    //   inside the lock, not read-modify-write from the caller, so this is already avoided.)
    // - How would you notify a purchasing team automatically when a product goes low-stock instead of polling?
    //   (Observer pattern: Product/InventoryManager publishes a LowStockEvent to registered listeners.)
    // - How would you batch a large restock (e.g. from a supplier shipment) atomically across many SKUs?
    // - What happens if reorder threshold itself needs to change dynamically (e.g. seasonal demand)? (Expose an
    //   updateThreshold method on Product; alert list recomputes on next query since it's not cached.)
    // - How would you scale getLowStockAlerts if there were millions of SKUs? (Maintain a separate sorted/indexed
    //   structure or precomputed set updated incrementally on each mutation, instead of a full O(n) scan.)

    /** Outcome of a stock mutation attempt. */
    public enum StockOperationStatus {
        SUCCESS,
        INSUFFICIENT_STOCK,
        UNKNOWN_SKU,
        DUPLICATE_SKU
    }

    /** A product tracked in inventory, owning its own quantity/threshold invariants. */
    public static class Product {
        private final String sku;
        private final String name;
        private final int reorderThreshold;
        private int quantity;
        private final ReentrantLock lock = new ReentrantLock();

        public Product(String sku, String name, int initialQuantity, int reorderThreshold) {
            if (initialQuantity < 0 || reorderThreshold < 0) {
                throw new IllegalArgumentException("Quantity and threshold must be non-negative");
            }
            this.sku = sku;
            this.name = name;
            this.quantity = initialQuantity;
            this.reorderThreshold = reorderThreshold;
        }

        StockOperationStatus addStock(int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount to add must be positive");
            }
            lock.lock();
            try {
                quantity += amount;
                return StockOperationStatus.SUCCESS;
            } finally {
                lock.unlock();
            }
        }

        StockOperationStatus removeStock(int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount to remove must be positive");
            }
            lock.lock();
            try {
                // Validate under the lock so a concurrent addStock/removeStock can't race between check and mutate.
                if (amount > quantity) {
                    return StockOperationStatus.INSUFFICIENT_STOCK;
                }
                quantity -= amount;
                return StockOperationStatus.SUCCESS;
            } finally {
                lock.unlock();
            }
        }

        int getQuantity() {
            lock.lock();
            try {
                return quantity;
            } finally {
                lock.unlock();
            }
        }

        boolean isLowStock() {
            lock.lock();
            try {
                return quantity <= reorderThreshold;
            } finally {
                lock.unlock();
            }
        }

        public String getSku() {
            return sku;
        }

        public String getName() {
            return name;
        }

        public int getReorderThreshold() {
            return reorderThreshold;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) qty=%d threshold=%d", name, sku, getQuantity(), reorderThreshold);
        }
    }

    /** Owns the SKU -> Product registry and cross-cutting inventory queries. */
    public static class InventoryManager {
        private final Map<String, Product> productsBySku = new ConcurrentHashMap<>();

        public StockOperationStatus registerProduct(Product product) {
            Product existing = productsBySku.putIfAbsent(product.getSku(), product);
            return existing == null ? StockOperationStatus.SUCCESS : StockOperationStatus.DUPLICATE_SKU;
        }

        public StockOperationStatus addStock(String sku, int amount) {
            Product product = productsBySku.get(sku);
            if (product == null) {
                return StockOperationStatus.UNKNOWN_SKU;
            }
            return product.addStock(amount);
        }

        public StockOperationStatus removeStock(String sku, int amount) {
            Product product = productsBySku.get(sku);
            if (product == null) {
                return StockOperationStatus.UNKNOWN_SKU;
            }
            return product.removeStock(amount);
        }

        public int getQuantity(String sku) {
            Product product = productsBySku.get(sku);
            if (product == null) {
                throw new IllegalArgumentException("Unknown SKU: " + sku);
            }
            return product.getQuantity();
        }

        /** Products whose quantity has fallen at or below their reorder threshold. */
        public List<Product> getLowStockAlerts() {
            List<Product> lowStock = new ArrayList<>();
            for (Product product : productsBySku.values()) {
                if (product.isLowStock()) {
                    lowStock.add(product);
                }
            }
            return lowStock;
        }

        public Collection<Product> getAllProducts() {
            return productsBySku.values();
        }
    }

    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();

        // 1. Register products.
        manager.registerProduct(new Product("SKU-001", "USB-C Cable", 50, 10));
        manager.registerProduct(new Product("SKU-002", "Wireless Mouse", 12, 15));
        manager.registerProduct(new Product("SKU-003", "Laptop Stand", 5, 5));

        // 2. Add stock to an existing product.
        StockOperationStatus addResult = manager.addStock("SKU-001", 20);
        System.out.println("Add 20 to SKU-001: " + addResult + ", new qty=" + manager.getQuantity("SKU-001"));
        // Expected: Add 20 to SKU-001: SUCCESS, new qty=70

        // 3. Remove stock within available quantity.
        StockOperationStatus removeResult = manager.removeStock("SKU-002", 4);
        System.out.println("Remove 4 from SKU-002: " + removeResult + ", new qty=" + manager.getQuantity("SKU-002"));
        // Expected: Remove 4 from SKU-002: SUCCESS, new qty=8

        // 4. Attempt to remove more stock than available -> rejected.
        StockOperationStatus overRemove = manager.removeStock("SKU-003", 100);
        System.out.println("Remove 100 from SKU-003: " + overRemove + ", qty unchanged=" + manager.getQuantity("SKU-003"));
        // Expected: Remove 100 from SKU-003: INSUFFICIENT_STOCK, qty unchanged=5

        // 5. Check low-stock alerts (SKU-002 qty=8 <= threshold=15; SKU-003 qty=5 <= threshold=5).
        List<Product> lowStock = manager.getLowStockAlerts();
        System.out.println("Low stock alerts: " + lowStock);
        // Expected: Low stock alerts: [Wireless Mouse (SKU-002) qty=8 threshold=15, Laptop Stand (SKU-003) qty=5 threshold=5]
    }
}
