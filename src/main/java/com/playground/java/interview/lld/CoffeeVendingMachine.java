package com.playground.java.interview.lld;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATTERN: Low-Level Design / OOP (Strategy-free composition + Enum-driven recipes)
 * PRIORITY: P2 - Good to Know
 * ONE-LINE PROBLEM STATEMENT: Design a coffee vending machine that dispenses beverages
 * from configurable recipes against a shared, refillable ingredient inventory.
 *
 * ================= PROBLEM / REQUIREMENTS =================
 * - Support multiple beverage types (e.g. ESPRESSO, LATTE, CAPPUCCINO, BLACK_COFFEE),
 *   each defined by a recipe: a map of ingredient name -> quantity required.
 * - Maintain a shared ingredient inventory (e.g. WATER, MILK, COFFEE_BEANS, SUGAR) with
 *   current available quantity for each ingredient.
 * - When a beverage is requested, dispense it only if every ingredient in its recipe has
 *   sufficient quantity in inventory; otherwise reject the request and report exactly
 *   which ingredient(s) are insufficient.
 * - On successful dispense, deduct the recipe's ingredient quantities from inventory.
 * - Support refilling one or more ingredients (e.g. after a technician restocks the machine).
 * - Support querying current inventory levels at any time.
 *
 * ================= SIMPLE APPROACH =================
 * A single CoffeeMachine god-class with a switch statement per beverage type, hardcoded
 * ingredient checks via if/else chains (if water < X || milk < Y || ...), and inventory
 * stored as loose instance fields (int water, int milk, int coffeeBeans, int sugar).
 * Dispensing logic and inventory management are tangled together in one method.
 *
 * ================= WHY IT'S NOT ENOUGH =================
 * - Adding a new beverage means editing the switch statement and writing a new bespoke
 *   if/else validation block -> violates Open/Closed Principle.
 * - Adding a new ingredient means touching every beverage's validation logic and the
 *   field list -> not scalable, error-prone.
 * - No single source of truth for "what does beverage X need" -> recipe knowledge is
 *   smeared across conditionals instead of being data.
 * - Hard to unit test in isolation (inventory, recipe lookup, and dispensing are fused).
 *
 * ================= OPTIMIZED DESIGN =================
 * - Ingredient (enum): the fixed set of ingredient types the machine understands.
 * - BeverageType (enum): the fixed set of beverages the machine can make.
 * - Recipe: an immutable value object wrapping Map<Ingredient, Integer> quantities needed.
 * - RecipeBook: maps BeverageType -> Recipe (owns "what does each beverage need").
 * - Inventory: owns Map<Ingredient, Integer> stock, exposes hasEnough(Recipe),
 *   deduct(Recipe), refill(Ingredient, int), and a read-only snapshot for reporting.
 * - DispenseResult: value object describing success/failure and, on failure, the list of
 *   insufficient ingredients (so callers can report precisely what's missing).
 * - CoffeeVendingMachine: the facade/manager that composes RecipeBook + Inventory and
 *   exposes dispense(BeverageType) and refill(Ingredient, int) as the public API.
 * This is primarily a data-driven design (recipes as data) plus a thin Facade
 * (CoffeeVendingMachine) coordinating two single-responsibility collaborators
 * (RecipeBook, Inventory).
 *
 * ================= WHY THIS DESIGN =================
 * - Single Responsibility: Inventory only manages stock math; RecipeBook only manages
 *   beverage->ingredient mappings; CoffeeVendingMachine only orchestrates the two.
 * - Open/Closed: adding a new beverage is a one-line addition to RecipeBook's
 *   initialization -- no existing code path changes. Adding a new Ingredient is a new
 *   enum constant; existing recipes/inventory code is untouched (uses EnumMap generically).
 * - Encapsulation: Inventory hides its internal map and only exposes intention-revealing
 *   operations (hasEnough/deduct/refill), preventing invalid direct mutation.
 * - Testability: Inventory and RecipeBook can be unit tested independently of dispensing
 *   flow control.
 *
 * ================= EDGE CASES =================
 * - Requesting a beverage with no registered recipe -> reject with a clear error.
 * - Multiple ingredients simultaneously insufficient -> report all of them, not just the
 *   first one found.
 * - Refilling with a negative amount -> reject (invalid input).
 * - Ingredient exactly equal to required quantity -> should succeed (boundary, not "low").
 * - Concurrent dispense requests from multiple threads racing on the same inventory could
 *   over-dispense (check-then-deduct race) -> must be atomic per dispense operation.
 * - Dispensing the same beverage back-to-back until an ingredient runs out mid-sequence.
 *
 * ================= COMPLEXITY =================
 * Time Complexity: O(R) per dispense/refill check, where R = number of ingredients in a
 *                   recipe (small, bounded constant in practice).
 * Space Complexity: O(B * R) for the recipe book (B beverages, R ingredients each) plus
 *                    O(I) for inventory (I = number of distinct ingredients).
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - How would you make dispense() thread-safe under concurrent access from multiple
 *   customers hitting the same physical machine? (synchronize the check+deduct as one
 *   critical section, or use a ReentrantLock around Inventory, since a naive
 *   check-then-act is a classic TOCTOU race.)
 * - How would you extend this to support beverage customization (e.g. extra shot, less
 *   sugar) without exploding the number of BeverageType enum values?
 * - How would you support multiple simultaneous "slots"/nozzles dispensing different
 *   beverages in parallel while sharing one inventory?
 * - How would you persist inventory levels across machine restarts?
 * - How would you add a pricing/payment step before dispensing (hint: composes cleanly
 *   with the Strategy-based PaymentProcessor design)?
 * - How would you support low-stock alerting/notifications to a technician (Observer
 *   pattern) instead of only reporting failures synchronously to the requester?
 * - How would you unit test the "insufficient ingredient" branch without a real machine?
 * - How would you model a recipe that requires an ingredient the machine currently has
 *   zero units registered for (undeclared ingredient)?
 */
public class CoffeeVendingMachine {

    /** Fixed set of raw ingredients the machine can hold and consume. */
    public enum Ingredient {
        WATER, MILK, COFFEE_BEANS, SUGAR
    }

    /** Fixed set of beverages the machine knows how to make. */
    public enum BeverageType {
        ESPRESSO, LATTE, CAPPUCCINO, BLACK_COFFEE
    }

    /** Immutable recipe: how much of each ingredient a beverage requires. */
    public static final class Recipe {
        private final Map<Ingredient, Integer> requirements;

        public Recipe(Map<Ingredient, Integer> requirements) {
            // Defensive copy so the recipe cannot be mutated after construction.
            this.requirements = new EnumMap<>(requirements);
        }

        public Map<Ingredient, Integer> getRequirements() {
            return new EnumMap<>(requirements);
        }
    }

    /** Owns beverage -> recipe mapping. New beverages are added here only. */
    public static final class RecipeBook {
        private final Map<BeverageType, Recipe> recipes = new EnumMap<>(BeverageType.class);

        public void register(BeverageType type, Recipe recipe) {
            recipes.put(type, recipe);
        }

        public Recipe getRecipe(BeverageType type) {
            Recipe recipe = recipes.get(type);
            if (recipe == null) {
                throw new IllegalArgumentException("No recipe registered for " + type);
            }
            return recipe;
        }
    }

    /** Value object describing the outcome of a dispense attempt. */
    public static final class DispenseResult {
        private final boolean success;
        private final BeverageType beverage;
        private final Map<Ingredient, Integer> shortfalls; // ingredient -> amount missing

        private DispenseResult(boolean success, BeverageType beverage, Map<Ingredient, Integer> shortfalls) {
            this.success = success;
            this.beverage = beverage;
            this.shortfalls = shortfalls;
        }

        static DispenseResult ok(BeverageType beverage) {
            return new DispenseResult(true, beverage, new EnumMap<>(Ingredient.class));
        }

        static DispenseResult failed(BeverageType beverage, Map<Ingredient, Integer> shortfalls) {
            return new DispenseResult(false, beverage, shortfalls);
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            if (success) {
                return "Dispensed " + beverage + " successfully.";
            }
            StringBuilder sb = new StringBuilder("Cannot dispense " + beverage + ". Low ingredients: ");
            shortfalls.forEach((ing, missing) -> sb.append(ing).append(" (short by ").append(missing).append(") "));
            return sb.toString().trim();
        }
    }

    /** Owns current stock levels and the only logic that mutates them. */
    public static final class Inventory {
        private final Map<Ingredient, Integer> stock = new EnumMap<>(Ingredient.class);

        public Inventory(Map<Ingredient, Integer> initialStock) {
            stock.putAll(initialStock);
        }

        /** Checks whether every ingredient in the recipe is available in sufficient quantity. */
        Map<Ingredient, Integer> findShortfalls(Recipe recipe) {
            Map<Ingredient, Integer> shortfalls = new EnumMap<>(Ingredient.class);
            for (Map.Entry<Ingredient, Integer> need : recipe.getRequirements().entrySet()) {
                int available = stock.getOrDefault(need.getKey(), 0);
                if (available < need.getValue()) {
                    shortfalls.put(need.getKey(), need.getValue() - available);
                }
            }
            return shortfalls;
        }

        /** Deducts a recipe's requirements from stock. Caller must have already validated sufficiency. */
        void deduct(Recipe recipe) {
            for (Map.Entry<Ingredient, Integer> need : recipe.getRequirements().entrySet()) {
                stock.merge(need.getKey(), -need.getValue(), Integer::sum);
            }
        }

        public synchronized void refill(Ingredient ingredient, int amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Refill amount cannot be negative: " + amount);
            }
            stock.merge(ingredient, amount, Integer::sum);
        }

        public synchronized Map<Ingredient, Integer> snapshot() {
            return new LinkedHashMap<>(stock);
        }
    }

    private final RecipeBook recipeBook;
    private final Inventory inventory;

    public CoffeeVendingMachine(RecipeBook recipeBook, Inventory inventory) {
        this.recipeBook = recipeBook;
        this.inventory = inventory;
    }

    /**
     * Atomically checks and deducts inventory for the requested beverage.
     * Synchronized to prevent a check-then-deduct race under concurrent requests
     * (see EDGE CASES / INTERVIEW FOLLOW-UPS on thread-safety).
     */
    public synchronized DispenseResult dispense(BeverageType type) {
        Recipe recipe = recipeBook.getRecipe(type);
        Map<Ingredient, Integer> shortfalls = inventory.findShortfalls(recipe);
        if (!shortfalls.isEmpty()) {
            return DispenseResult.failed(type, shortfalls);
        }
        inventory.deduct(recipe);
        return DispenseResult.ok(type);
    }

    public void refillIngredient(Ingredient ingredient, int amount) {
        inventory.refill(ingredient, amount);
    }

    public Map<Ingredient, Integer> currentInventory() {
        return inventory.snapshot();
    }

    public static void main(String[] args) {
        // Wire up recipes: ingredient name -> quantity needed, per beverage.
        RecipeBook recipeBook = new RecipeBook();

        Map<Ingredient, Integer> espressoReq = new HashMap<>();
        espressoReq.put(Ingredient.WATER, 30);
        espressoReq.put(Ingredient.COFFEE_BEANS, 20);
        recipeBook.register(BeverageType.ESPRESSO, new Recipe(espressoReq));

        Map<Ingredient, Integer> latteReq = new HashMap<>();
        latteReq.put(Ingredient.WATER, 20);
        latteReq.put(Ingredient.MILK, 60);
        latteReq.put(Ingredient.COFFEE_BEANS, 15);
        recipeBook.register(BeverageType.LATTE, new Recipe(latteReq));

        Map<Ingredient, Integer> cappuccinoReq = new HashMap<>();
        cappuccinoReq.put(Ingredient.WATER, 20);
        cappuccinoReq.put(Ingredient.MILK, 40);
        cappuccinoReq.put(Ingredient.COFFEE_BEANS, 15);
        cappuccinoReq.put(Ingredient.SUGAR, 5);
        recipeBook.register(BeverageType.CAPPUCCINO, new Recipe(cappuccinoReq));

        // Seed initial inventory - milk is deliberately tight so it runs out before the
        // third drink, triggering a reported shortfall.
        Map<Ingredient, Integer> initialStock = new HashMap<>();
        initialStock.put(Ingredient.WATER, 100);
        initialStock.put(Ingredient.MILK, 90);
        initialStock.put(Ingredient.COFFEE_BEANS, 60);
        initialStock.put(Ingredient.SUGAR, 20);
        Inventory inventory = new Inventory(initialStock);

        CoffeeVendingMachine machine = new CoffeeVendingMachine(recipeBook, inventory);

        // 1. Dispense an espresso - inventory is sufficient.
        System.out.println(machine.dispense(BeverageType.ESPRESSO));
        // Expected: Dispensed ESPRESSO successfully.

        // 2. Dispense a latte - inventory is sufficient (milk: 90 -> 30 after this).
        System.out.println(machine.dispense(BeverageType.LATTE));
        // Expected: Dispensed LATTE successfully.

        // 3. Attempt a cappuccino - milk is now too low (30 available, 40 needed).
        System.out.println(machine.dispense(BeverageType.CAPPUCCINO));
        // Expected: Cannot dispense CAPPUCCINO. Low ingredients: MILK (short by 10)

        // 4. Refill milk, then retry the cappuccino successfully.
        machine.refillIngredient(Ingredient.MILK, 100);
        System.out.println(machine.dispense(BeverageType.CAPPUCCINO));
        // Expected: Dispensed CAPPUCCINO successfully.

        // 5. Print final inventory snapshot.
        System.out.println("Final inventory: " + machine.currentInventory());
        // Expected: Final inventory: {WATER=30, MILK=90, COFFEE_BEANS=10, SUGAR=15}
    }
}
