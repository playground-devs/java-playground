package com.playground.java.interview.lld;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATTERN: Low-Level Design / OOP (Interval Scheduling)
 * PRIORITY: P0 - Must Know
 * Design a hotel-room reservation system that checks availability, books a room for a
 * date range while rejecting overlapping bookings, and supports cancellation.
 */
public class BookingReservationSystem {

    // ================= PROBLEM / REQUIREMENTS =================
    // - The system manages a hotel with multiple rooms, each identified by a room number and a RoomType.
    // - A guest must be able to check whether a given room (or any room of a given type) is available
    //   for a requested check-in/check-out date range.
    // - A guest must be able to book a room for a date range; the booking must be rejected if it
    //   overlaps with any existing CONFIRMED booking for that room.
    // - A guest must be able to cancel an existing booking, freeing up the room for that date range.
    // - The system should be able to list all bookings for a room, and support many rooms/bookings.
    // - Must behave correctly under concurrent booking attempts for the same room (no double-booking).

    // ================= SIMPLE APPROACH =================
    // A single god-class "Hotel" holds a List<Booking> for the entire hotel. To check availability,
    // linearly scan ALL bookings (regardless of room) and compare date ranges. Booking mutates the
    // same list directly with no synchronization, no clean overlap abstraction, and room/guest/booking
    // concepts are all mashed into loose fields with no clear ownership.

    // ================= WHY IT'S NOT ENOUGH =================
    // - Scanning every booking across the whole hotel for a single room's availability wastes time and
    //   couples unrelated rooms together (violates Single Responsibility).
    // - No encapsulation: overlap-detection logic gets copy-pasted wherever availability is checked,
    //   inviting subtle off-by-one bugs (e.g. is a same-day checkout/checkin a conflict?).
    // - No thread-safety: two guests booking the same room at the same time can both "succeed",
    //   producing a double-booking.
    // - Hard to extend: adding room types, pricing, or partial-day holds requires touching one giant
    //   class with no clear seams.

    // ================= OPTIMIZED DESIGN =================
    // - DateRange: a small value type encapsulating [checkIn, checkOut) and the overlap-detection logic
    //   (single source of truth for "do two ranges conflict").
    // - RoomType (enum): SINGLE, DOUBLE, DELUXE, SUITE — carries a base nightly rate.
    // - BookingStatus (enum): CONFIRMED, CANCELLED — simple State-like flag on a Booking.
    // - Room: owns its room number, RoomType, and its OWN list of bookings. Only Room knows how to
    //   check its own availability and add/cancel a booking for itself (Single Responsibility +
    //   Encapsulation). A per-room ReentrantLock guarantees no double-booking under concurrency.
    // - Booking: an immutable-ish record of {id, room, guestName, DateRange, status}.
    // - Hotel: the manager/facade class that owns the collection of Rooms and exposes
    //   checkAvailability / bookRoom / cancelBooking to callers, delegating the actual overlap
    //   checks to the relevant Room. This keeps Hotel thin (Facade pattern over Rooms).
    // - Design pattern used: encapsulated interval-overlap check inside DateRange (reusable value
    //   object), Facade pattern (Hotel delegates to Room), and per-resource locking for concurrency
    //   control (similar in spirit to how a Singleton manager would serialize access, but scoped
    //   per-room instead of globally to maximize concurrency).

    // ================= WHY THIS DESIGN =================
    // - Single Responsibility: Room owns its own bookings/availability; Hotel only coordinates across
    //   rooms; DateRange owns interval math. Each class has exactly one reason to change.
    // - Open/Closed: new RoomTypes or booking rules (e.g. minimum stay) can be added without touching
    //   the overlap-detection algorithm itself.
    // - Encapsulation prevents inconsistent state: the only way to mutate a Room's bookings is through
    //   its own synchronized methods, so double-booking is structurally prevented, not just checked.
    // - Per-room locking (rather than one global lock) lets bookings on different rooms proceed fully
    //   in parallel, which matters at hotel scale (hundreds of rooms).

    // ================= EDGE CASES =================
    // - Booking a date range that touches but does not overlap an existing one (checkout day == next
    //   check-in day) must be ALLOWED — handled by using half-open intervals [in, out).
    // - checkIn >= checkOut (zero or negative-length stay) must be rejected as invalid input.
    // - Cancelling a booking that is already cancelled, or an unknown bookingId, must fail gracefully.
    // - Two threads booking the same room for overlapping dates concurrently: only one must succeed.
    // - Requesting availability for a room number that doesn't exist in the hotel.
    // - A fully booked room across a wide date range should simply report "not available", not throw.

    // ================= COMPLEXITY =================
    // Time Complexity: checkAvailability/bookRoom on a single room is O(b) where b = number of
    //   existing bookings for that room (linear scan for overlap). Could be O(log b) with an
    //   interval tree / TreeMap keyed by check-in date for very high-booking-volume rooms.
    // Space Complexity: O(R + B) where R = number of rooms, B = total number of bookings stored.
    // ================= INTERVIEW FOLLOW-UPS =================
    // - How would you scale overlap checks if a single room had thousands of bookings/holds?
    //   (interval tree, or a sorted TreeMap<LocalDate, Booking> for O(log n) neighbor lookups.)
    // - How do you guarantee no double-booking under concurrent requests across multiple app servers,
    //   not just multiple threads in one JVM? (DB-level unique constraint / SELECT ... FOR UPDATE,
    //   or optimistic locking with a version column, since an in-process lock only helps single-node.)
    // - How would you extend this to support room "holds" that expire after N minutes (temporary
    //   reservation while a guest completes payment)?
    // - How would you support searching "any available room of type DELUXE" across the whole hotel
    //   efficiently instead of scanning every room?
    // - How would you add pricing (nightly rate * nights, seasonal multipliers) without touching the
    //   booking/overlap logic? (Strategy pattern for a PricingStrategy.)
    // - How would you support overbooking strategies used by real hotel chains (e.g. allow a small % of
    //   overbooking with a waitlist/relocation policy)?
    // - How do you make cancellation auditable (who cancelled, when, refund policy)?
    // - What happens if the system needs to support multi-room bookings (a family reserving 2 rooms)
    //   atomically — i.e. either both rooms are booked or neither is?

    /** Half-open date interval [checkIn, checkOut) with reusable overlap-detection logic. */
    static final class DateRange {
        final LocalDate checkIn;
        final LocalDate checkOut;

        DateRange(LocalDate checkIn, LocalDate checkOut) {
            if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
                throw new IllegalArgumentException("checkIn must be strictly before checkOut");
            }
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        // Two half-open ranges [a, b) and [c, d) overlap iff a < d && c < b.
        // This correctly allows back-to-back stays: checkout day == next check-in day is NOT an overlap.
        boolean overlaps(DateRange other) {
            return this.checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(this.checkOut);
        }

        @Override
        public String toString() {
            return checkIn + " to " + checkOut;
        }
    }

    enum RoomType {
        SINGLE(89.0), DOUBLE(129.0), DELUXE(189.0), SUITE(299.0);

        final double nightlyRate;

        RoomType(double nightlyRate) {
            this.nightlyRate = nightlyRate;
        }
    }

    enum BookingStatus { CONFIRMED, CANCELLED }

    static final class Booking {
        final String id;
        final String roomNumber;
        final String guestName;
        final DateRange dateRange;
        volatile BookingStatus status;

        Booking(String roomNumber, String guestName, DateRange dateRange) {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.roomNumber = roomNumber;
            this.guestName = guestName;
            this.dateRange = dateRange;
            this.status = BookingStatus.CONFIRMED;
        }

        @Override
        public String toString() {
            return "Booking{id=" + id + ", room=" + roomNumber + ", guest=" + guestName
                    + ", dates=" + dateRange + ", status=" + status + "}";
        }
    }

    /** A single hotel room; owns its own bookings and serializes access to itself. */
    static final class Room {
        final String roomNumber;
        final RoomType type;
        private final List<Booking> bookings = new ArrayList<>();
        // Per-room lock: bookings on different rooms can proceed fully in parallel.
        private final ReentrantLock lock = new ReentrantLock();

        Room(String roomNumber, RoomType type) {
            this.roomNumber = roomNumber;
            this.type = type;
        }

        boolean isAvailable(DateRange requested) {
            lock.lock();
            try {
                for (Booking b : bookings) {
                    if (b.status == BookingStatus.CONFIRMED && b.dateRange.overlaps(requested)) {
                        return false;
                    }
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        /** Returns the created Booking, or null if the room is not available for the requested range. */
        Booking book(String guestName, DateRange requested) {
            lock.lock();
            try {
                // Re-check under the lock to avoid a race between isAvailable() and book().
                for (Booking b : bookings) {
                    if (b.status == BookingStatus.CONFIRMED && b.dateRange.overlaps(requested)) {
                        return null;
                    }
                }
                Booking booking = new Booking(roomNumber, guestName, requested);
                bookings.add(booking);
                return booking;
            } finally {
                lock.unlock();
            }
        }

        boolean cancel(String bookingId) {
            lock.lock();
            try {
                for (Booking b : bookings) {
                    if (b.id.equals(bookingId) && b.status == BookingStatus.CONFIRMED) {
                        b.status = BookingStatus.CANCELLED;
                        return true;
                    }
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        List<Booking> getBookings() {
            lock.lock();
            try {
                return new ArrayList<>(bookings);
            } finally {
                lock.unlock();
            }
        }
    }

    /** Facade over all rooms in the hotel. */
    static final class Hotel {
        private final Map<String, Room> rooms = new HashMap<>();

        void addRoom(String roomNumber, RoomType type) {
            rooms.put(roomNumber, new Room(roomNumber, type));
        }

        boolean checkAvailability(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
            Room room = rooms.get(roomNumber);
            if (room == null) {
                throw new IllegalArgumentException("Unknown room: " + roomNumber);
            }
            return room.isAvailable(new DateRange(checkIn, checkOut));
        }

        Booking bookRoom(String roomNumber, String guestName, LocalDate checkIn, LocalDate checkOut) {
            Room room = rooms.get(roomNumber);
            if (room == null) {
                throw new IllegalArgumentException("Unknown room: " + roomNumber);
            }
            return room.book(guestName, new DateRange(checkIn, checkOut));
        }

        boolean cancelBooking(String roomNumber, String bookingId) {
            Room room = rooms.get(roomNumber);
            if (room == null) {
                throw new IllegalArgumentException("Unknown room: " + roomNumber);
            }
            return room.cancel(bookingId);
        }

        List<Booking> getBookingsForRoom(String roomNumber) {
            Room room = rooms.get(roomNumber);
            if (room == null) {
                throw new IllegalArgumentException("Unknown room: " + roomNumber);
            }
            return room.getBookings();
        }
    }

    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        hotel.addRoom("101", RoomType.DELUXE);
        hotel.addRoom("102", RoomType.SUITE);

        LocalDate jan10 = LocalDate.of(2026, 1, 10);
        LocalDate jan15 = LocalDate.of(2026, 1, 15);
        LocalDate jan12 = LocalDate.of(2026, 1, 12);
        LocalDate jan18 = LocalDate.of(2026, 1, 18);

        // 1. Check availability before any booking exists.
        System.out.println("Room 101 available Jan10-15? " + hotel.checkAvailability("101", jan10, jan15));
        // Expected: Room 101 available Jan10-15? true

        // 2. Book room 101 for Jan10-15.
        Booking booking1 = hotel.bookRoom("101", "Alice", jan10, jan15);
        System.out.println("Booked: " + booking1);
        // Expected: Booked: Booking{... room=101, guest=Alice, dates=2026-01-10 to 2026-01-15, status=CONFIRMED}

        // 3. Attempt an overlapping booking (Jan12-18 overlaps Jan10-15) -> must be rejected.
        Booking booking2 = hotel.bookRoom("101", "Bob", jan12, jan18);
        System.out.println("Overlapping booking result: " + booking2);
        // Expected: Overlapping booking result: null

        // 4. Book a non-overlapping, back-to-back stay starting exactly on the checkout day -> allowed.
        Booking booking3 = hotel.bookRoom("101", "Carol", jan15, jan18);
        System.out.println("Back-to-back booking: " + booking3);
        // Expected: Back-to-back booking: Booking{... room=101, guest=Carol, dates=2026-01-15 to 2026-01-18, status=CONFIRMED}

        // 5. Cancel Alice's booking, then confirm the room is available again for those dates.
        boolean cancelled = hotel.cancelBooking("101", booking1.id);
        System.out.println("Cancelled Alice's booking: " + cancelled);
        System.out.println("Room 101 available Jan10-15 after cancel? " + hotel.checkAvailability("101", jan10, jan15));
        // Expected: Cancelled Alice's booking: true
        // Expected: Room 101 available Jan10-15 after cancel? true
    }
}
