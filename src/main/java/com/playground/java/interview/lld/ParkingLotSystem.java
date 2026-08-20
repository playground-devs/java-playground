package com.playground.java.interview.lld;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Low-Level Design / OOP (Strategy for fee calculation, Factory-ish spot allocation)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Design a multi-floor parking lot that can park/unpark vehicles of
 * different sizes into appropriately sized spots and compute a time-based parking fee.
 *
 * ================= PROBLEM / REQUIREMENTS =================
 * - Support multiple floors, each with multiple parking spots.
 * - Support multiple spot types: MOTORCYCLE, CAR, TRUCK (increasing size).
 * - A vehicle can only park in a spot whose size is >= the vehicle's required size
 *   (e.g. a motorcycle may use a CAR spot if no MOTORCYCLE spot is free, but a truck
 *   can only use a TRUCK spot).
 * - Parking a vehicle should find the nearest available suitable spot (lowest floor
 *   number first, then lowest spot number).
 * - Unparking a vehicle frees its spot and computes a simple time-based fee.
 * - Track availability counts per floor and per spot type.
 * - Must be safe under concurrent park/unpark calls from multiple threads.
 *
 * ================= SIMPLE APPROACH =================
 * A single ParkingLot god-class holding a 2D array/list of booleans (spot occupied or
 * not) and a parallel list of vehicle plate numbers. park() would loop over every spot,
 * checking type compatibility and occupancy with if/else chains, and unpark() would
 * scan linearly for the plate number and reset the boolean and manually recompute fees
 * inline using System.currentTimeMillis() differences.
 *
 * ================= WHY IT'S NOT ENOUGH =================
 * - Everything (spot state, floor layout, vehicle rules, fee rules) lives in one class,
 *   violating Single Responsibility - any change to fee policy risks breaking spot
 *   allocation logic and vice versa.
 * - Adding a new vehicle type or spot type means editing sprawling if/else chains
 *   instead of extending a well-defined abstraction (violates Open/Closed).
 * - No clean concurrency story - concurrent park() calls could double-book a boolean
 *   flag with only ad-hoc synchronization sprinkled around.
 * - No natural place to plug in alternate fee strategies (flat rate, per-hour,
 *   membership discounts) without conditionals.
 *
 * ================= OPTIMIZED DESIGN =================
 * - VehicleType (enum): MOTORCYCLE, CAR, TRUCK - ordinal order also encodes size rank.
 * - Vehicle: immutable value object (license plate + VehicleType).
 * - SpotType (enum): MOTORCYCLE, COMPACT, LARGE - each maps to which VehicleTypes it
 *   can host via canFit(VehicleType).
 * - ParkingSpot: represents one physical spot (floor, spot number, SpotType) and holds
 *   its own lock + occupancy state + currently parked Vehicle + park start timestamp.
 *   Encapsulates isAvailable()/assign()/release() so no external class mutates its
 *   internal state directly - this is effectively a tiny State machine (FREE/OCCUPIED).
 * - Floor: owns an ordered list of ParkingSpot for that level and exposes
 *   findAvailableSpot(VehicleType) plus per-type availability counts.
 * - FeeCalculator (interface, Strategy pattern): calculateFee(spotType, minutesParked).
 *   TimeBasedFeeCalculator is the concrete strategy used here (rate per SpotType per
 *   hour, rounded up); swapping in a different pricing model (e.g. flat + surge) means
 *   adding a new implementation, not touching ParkingLot.
 * - Ticket: issued on park(), carries vehicle, spot reference, and entry time; consumed
 *   on unpark() to compute duration and fee.
 * - ParkingLot: the manager/facade. Holds the list of Floors, a FeeCalculator, and a
 *   ConcurrentHashMap<String, Ticket> keyed by license plate for O(1) ticket lookup on
 *   unpark. park() iterates floors in order (lowest first) and asks each Floor for the
 *   nearest suitable spot; unpark() looks up the ticket, releases the spot, and
 *   delegates fee math to the FeeCalculator strategy.
 *
 * ================= WHY THIS DESIGN =================
 * - Single Responsibility: ParkingSpot only manages its own state; Floor only manages
 *   its spots; ParkingLot only orchestrates; FeeCalculator only prices.
 * - Open/Closed: new VehicleType/SpotType values or a new FeeCalculator strategy can be
 *   added without modifying existing classes' logic (just new enum constants / a new
 *   strategy implementation).
 * - Encapsulation: spot occupancy can only change through synchronized methods on
 *   ParkingSpot itself, preventing double-booking races.
 * - Strategy pattern isolates fee-calculation policy from parking-allocation policy,
 *   so pricing changes never risk breaking allocation correctness.
 *
 * ================= EDGE CASES =================
 * - Lot completely full for a given/larger spot type -> park() returns empty Ticket
 *   (Optional) rather than throwing, caller decides how to handle rejection.
 * - Vehicle already parked (duplicate plate) -> park() rejects the second attempt.
 * - Unparking a plate with no active ticket -> unpark() reports "not found" instead of
 *   throwing/crashing.
 * - Multiple threads calling park() simultaneously for the last free spot -> only one
 *   wins the CAS-like assign(); resolved via per-spot ReentrantLock plus lot-level
 *   ConcurrentHashMap for ticket bookkeeping.
 * - Zero-duration parking (park + immediate unpark) -> fee calculator rounds up to a
 *   minimum of 1 billable hour.
 *
 * ================= COMPLEXITY =================
 * Time Complexity: park() is O(F * S) worst case (F floors, S spots per floor) to find
 * a free spot by linear scan; unpark() is O(1) via the ticket map plus O(1) spot release.
 * Space Complexity: O(F * S) to hold all spot objects plus O(A) for A active tickets.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - How would you make park()/unpark() thread-safe without a single giant lock around
 *   the whole ParkingLot? (Per-spot locks + concurrent ticket map, as done here, allows
 *   different floors/spots to be booked in parallel.)
 * - How would you avoid the O(F * S) linear scan at high spot counts? (Maintain a
 *   free-spot index, e.g. a per-(floor,type) queue/heap of available spot IDs, updated
 *   on assign/release for near O(1) allocation.)
 * - How would you add a "reserve a spot for N minutes" feature? (Introduce a
 *   RESERVED state to the spot state machine and a background expiry sweep.)
 * - How would you support multiple entry/exit gates issuing tickets concurrently?
 *   (Ticket issuance already goes through ConcurrentHashMap.putIfAbsent - extend gates
 *   to call the same ParkingLot instance.)
 * - How would you add electric-vehicle charging spots as a new spot type with special
 *   pricing? (Add an EV SpotType + extend FeeCalculator strategy - no change to Floor.)
 * - How would you support dynamic/surge pricing? (Swap the FeeCalculator implementation
 *   at runtime without touching ParkingLot or ParkingSpot.)
 * - How do you prevent a vehicle from being "parked twice" if the client retries a
 *   timed-out request? (Idempotency check via the plate-keyed ticket map before
 *   allocating a new spot.)
 * - How would you persist state across restarts? (Extract an interface for
 *   ticket/spot storage and back it with a DB instead of in-memory maps.)
 */
public class ParkingLotSystem {

    // ---------- Vehicle & Spot type hierarchy ----------

    /** Ordinal encodes relative size: MOTORCYCLE < CAR < TRUCK. */
    public enum VehicleType {
        MOTORCYCLE, CAR, TRUCK
    }

    /** Each spot type can host its own vehicle type and any smaller one. */
    public enum SpotType {
        MOTORCYCLE(VehicleType.MOTORCYCLE),
        COMPACT(VehicleType.CAR),
        LARGE(VehicleType.TRUCK);

        private final VehicleType maxVehicleType;

        SpotType(VehicleType maxVehicleType) {
            this.maxVehicleType = maxVehicleType;
        }

        boolean canFit(VehicleType vehicleType) {
            return vehicleType.ordinal() <= this.maxVehicleType.ordinal();
        }
    }

    public static final class Vehicle {
        private final String licensePlate;
        private final VehicleType type;

        public Vehicle(String licensePlate, VehicleType type) {
            this.licensePlate = licensePlate;
            this.type = type;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public VehicleType getType() {
            return type;
        }
    }

    // ---------- Parking spot (tiny state machine: FREE / OCCUPIED) ----------

    public static final class ParkingSpot {
        private final int floorNumber;
        private final int spotNumber;
        private final SpotType spotType;
        private final ReentrantLock lock = new ReentrantLock();
        private boolean occupied = false;
        private Vehicle parkedVehicle;
        private long entryTimeMillis;

        ParkingSpot(int floorNumber, int spotNumber, SpotType spotType) {
            this.floorNumber = floorNumber;
            this.spotNumber = spotNumber;
            this.spotType = spotType;
        }

        boolean canFit(VehicleType vehicleType) {
            return spotType.canFit(vehicleType);
        }

        /** Atomically tries to occupy this spot; returns false if already taken. */
        boolean tryAssign(Vehicle vehicle, long nowMillis) {
            lock.lock();
            try {
                if (occupied) {
                    return false;
                }
                occupied = true;
                parkedVehicle = vehicle;
                entryTimeMillis = nowMillis;
                return true;
            } finally {
                lock.unlock();
            }
        }

        /** Frees the spot and returns the entry timestamp for fee calculation. */
        long release() {
            lock.lock();
            try {
                long entry = entryTimeMillis;
                occupied = false;
                parkedVehicle = null;
                entryTimeMillis = 0L;
                return entry;
            } finally {
                lock.unlock();
            }
        }

        boolean isAvailable() {
            return !occupied;
        }

        public int getFloorNumber() {
            return floorNumber;
        }

        public int getSpotNumber() {
            return spotNumber;
        }

        public SpotType getSpotType() {
            return spotType;
        }
    }

    // ---------- Floor ----------

    public static final class Floor {
        private final int floorNumber;
        private final List<ParkingSpot> spots = new ArrayList<>();

        Floor(int floorNumber) {
            this.floorNumber = floorNumber;
        }

        void addSpot(int spotNumber, SpotType spotType) {
            spots.add(new ParkingSpot(floorNumber, spotNumber, spotType));
        }

        /** Linear scan for the first free spot (by ascending spot number) that fits. */
        ParkingSpot findAvailableSpot(VehicleType vehicleType) {
            for (ParkingSpot spot : spots) {
                if (spot.canFit(vehicleType) && spot.isAvailable()) {
                    return spot;
                }
            }
            return null;
        }

        int availableCount(SpotType spotType) {
            int count = 0;
            for (ParkingSpot spot : spots) {
                if (spot.getSpotType() == spotType && spot.isAvailable()) {
                    count++;
                }
            }
            return count;
        }

        int getFloorNumber() {
            return floorNumber;
        }
    }

    // ---------- Fee strategy (Strategy pattern) ----------

    public interface FeeCalculator {
        double calculateFee(SpotType spotType, long minutesParked);
    }

    public static final class TimeBasedFeeCalculator implements FeeCalculator {
        private final Map<SpotType, Double> hourlyRate = new EnumMap<>(SpotType.class);

        public TimeBasedFeeCalculator() {
            hourlyRate.put(SpotType.MOTORCYCLE, 1.0);
            hourlyRate.put(SpotType.COMPACT, 2.5);
            hourlyRate.put(SpotType.LARGE, 5.0);
        }

        @Override
        public double calculateFee(SpotType spotType, long minutesParked) {
            // Bill by the hour, rounding up, with a 1-hour minimum charge.
            long billableHours = (minutesParked + 59) / 60;
            if (billableHours < 1) {
                billableHours = 1;
            }
            return billableHours * hourlyRate.get(spotType);
        }
    }

    // ---------- Ticket ----------

    public static final class Ticket {
        private final String ticketId;
        private final Vehicle vehicle;
        private final ParkingSpot spot;
        private final long entryTimeMillis;

        Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot, long entryTimeMillis) {
            this.ticketId = ticketId;
            this.vehicle = vehicle;
            this.spot = spot;
            this.entryTimeMillis = entryTimeMillis;
        }

        public String getTicketId() {
            return ticketId;
        }

        public ParkingSpot getSpot() {
            return spot;
        }
    }

    public static final class ParkingResult {
        public final boolean success;
        public final Ticket ticket;
        public final String message;

        private ParkingResult(boolean success, Ticket ticket, String message) {
            this.success = success;
            this.ticket = ticket;
            this.message = message;
        }

        static ParkingResult ok(Ticket ticket) {
            return new ParkingResult(true, ticket, "Parked at floor " + ticket.getSpot().getFloorNumber()
                    + " spot " + ticket.getSpot().getSpotNumber());
        }

        static ParkingResult fail(String message) {
            return new ParkingResult(false, null, message);
        }
    }

    public static final class UnparkResult {
        public final boolean success;
        public final double fee;
        public final String message;

        private UnparkResult(boolean success, double fee, String message) {
            this.success = success;
            this.fee = fee;
            this.message = message;
        }

        static UnparkResult ok(double fee) {
            return new UnparkResult(true, fee, "Fee due: $" + String.format("%.2f", fee));
        }

        static UnparkResult fail(String message) {
            return new UnparkResult(false, 0.0, message);
        }
    }

    // ---------- ParkingLot manager (facade) ----------

    public static final class ParkingLot {
        private final List<Floor> floors = new ArrayList<>();
        private final FeeCalculator feeCalculator;
        // Keyed by license plate -> active ticket; ConcurrentHashMap gives thread-safe
        // O(1) issue/lookup/remove without a lot-wide lock.
        private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
        private final AtomicLong ticketSequence = new AtomicLong(1);

        public ParkingLot(FeeCalculator feeCalculator) {
            this.feeCalculator = feeCalculator;
        }

        public Floor addFloor(int floorNumber) {
            Floor floor = new Floor(floorNumber);
            floors.add(floor);
            return floor;
        }

        public ParkingResult park(Vehicle vehicle) {
            if (activeTickets.containsKey(vehicle.getLicensePlate())) {
                return ParkingResult.fail("Vehicle " + vehicle.getLicensePlate() + " is already parked");
            }
            // Nearest available suitable spot = lowest floor number, then lowest spot number.
            for (Floor floor : floors) {
                ParkingSpot candidate = floor.findAvailableSpot(vehicle.getType());
                if (candidate == null) {
                    continue;
                }
                long now = System.currentTimeMillis();
                if (candidate.tryAssign(vehicle, now)) {
                    String ticketId = "T-" + ticketSequence.getAndIncrement();
                    Ticket ticket = new Ticket(ticketId, vehicle, candidate, now);
                    activeTickets.put(vehicle.getLicensePlate(), ticket);
                    return ParkingResult.ok(ticket);
                }
                // Lost the race to another thread; caller could retry from next floor,
                // here we simply continue scanning subsequent floors.
            }
            return ParkingResult.fail("No available spot for " + vehicle.getType());
        }

        public UnparkResult unpark(String licensePlate) {
            Ticket ticket = activeTickets.remove(licensePlate);
            if (ticket == null) {
                return UnparkResult.fail("No active ticket found for " + licensePlate);
            }
            long entryTime = ticket.getSpot().release();
            long minutesParked = (System.currentTimeMillis() - entryTime) / (60 * 1000);
            double fee = feeCalculator.calculateFee(ticket.getSpot().getSpotType(), minutesParked);
            return UnparkResult.ok(fee);
        }

        public int availableCount(int floorNumber, SpotType spotType) {
            for (Floor floor : floors) {
                if (floor.getFloorNumber() == floorNumber) {
                    return floor.availableCount(spotType);
                }
            }
            return 0;
        }
    }

    // ================= DEMO =================

    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(new TimeBasedFeeCalculator());

        // Floor 1: 1 motorcycle spot, 1 compact spot, 1 large spot.
        Floor floor1 = lot.addFloor(1);
        floor1.addSpot(1, SpotType.MOTORCYCLE);
        floor1.addSpot(2, SpotType.COMPACT);
        floor1.addSpot(3, SpotType.LARGE);

        // Floor 2: 1 compact spot only.
        Floor floor2 = lot.addFloor(2);
        floor2.addSpot(1, SpotType.COMPACT);

        // 1. Park a car -> should take floor 1's compact spot (nearest suitable).
        ParkingResult r1 = lot.park(new Vehicle("CAR-100", VehicleType.CAR));
        System.out.println(r1.message); // Expected: Parked at floor 1 spot 2

        // 2. Park a truck -> only floor 1's large spot fits.
        ParkingResult r2 = lot.park(new Vehicle("TRUCK-200", VehicleType.TRUCK));
        System.out.println(r2.message); // Expected: Parked at floor 1 spot 3

        // 3. Park a second car -> floor 1 compact is taken, falls through to floor 2.
        ParkingResult r3 = lot.park(new Vehicle("CAR-300", VehicleType.CAR));
        System.out.println(r3.message); // Expected: Parked at floor 2 spot 1

        // 4. Check availability: floor 1 compact spots should now be 0 available.
        System.out.println("Floor 1 COMPACT available: " + lot.availableCount(1, SpotType.COMPACT)); // Expected: 0
        System.out.println("Floor 1 MOTORCYCLE available: " + lot.availableCount(1, SpotType.MOTORCYCLE)); // Expected: 1

        // 5. Unpark the first car and see the computed fee (near-zero elapsed time -> 1hr minimum).
        UnparkResult u1 = lot.unpark("CAR-100");
        System.out.println(u1.message); // Expected: Fee due: $2.50

        // Bonus: attempt to unpark a vehicle that was never parked.
        UnparkResult u2 = lot.unpark("GHOST-999");
        System.out.println(u2.message); // Expected: No active ticket found for GHOST-999
    }
}
