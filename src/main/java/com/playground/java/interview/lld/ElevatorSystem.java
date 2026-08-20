package com.playground.java.interview.lld;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Low-Level Design / OOP (State Pattern + Scheduling Strategy)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Design a multi-elevator system that serves external hall
 * calls (up/down from a floor) and internal cabin requests (select a floor),
 * choosing which elevator should serve each new request.
 *
 * ================= PROBLEM / REQUIREMENTS =================
 * - Support multiple elevators serving a fixed range of floors (e.g. 0..N-1).
 * - Handle EXTERNAL requests: a person on floor F presses UP or DOWN.
 * - Handle INTERNAL requests: a person inside a cabin selects a destination floor.
 * - Each elevator has a state: IDLE, MOVING_UP, MOVING_DOWN, DOOR_OPEN.
 * - When a new request arrives, the system must pick the "best" elevator to serve it
 *   (e.g. nearest idle elevator, or an elevator already moving in the same direction
 *   that will pass the requested floor).
 * - Elevator must process its pending floor stops in an order that avoids
 *   needless back-and-forth (basic SCAN-like behavior in the same direction).
 * - Door opens at each stop, then elevator continues to next requested floor or goes IDLE.
 *
 * ================= SIMPLE APPROACH =================
 * One God-class `ElevatorController` holds a single `currentFloor` int and a
 * `List<Integer> requests`. Every request is served by the same elevator,
 * moving one floor at a time in whatever order requests were added, printing
 * "moving" / "door open" statements directly inside the same method. No
 * notion of state machine, no support for multiple elevators, no scheduling.
 *
 * ================= WHY IT'S NOT ENOUGH =================
 * - Cannot scale to multiple elevators - there's nothing to choose between.
 * - Mixing movement logic, request queueing, and state transitions in one
 *   method violates Single Responsibility and makes it hard to test each piece.
 * - No explicit state machine means invalid transitions (e.g. door opening
 *   while moving) are easy to introduce by accident.
 * - Hard to swap in a smarter scheduling algorithm later since elevator
 *   selection logic is not isolated behind an interface.
 *
 * ================= OPTIMIZED DESIGN =================
 * - Direction (enum): UP, DOWN, IDLE - direction of travel.
 * - ElevatorState (interface, State pattern): IdleState, MovingUpState,
 *   MovingDownState, DoorOpenState each implement handle(Elevator) to decide
 *   the next transition. Elevator delegates step() to its current state object.
 * - Request (class): represents either an external hall call (floor + direction)
 *   or an internal cabin call (destination floor) - modeled simply as a target floor.
 * - Elevator (class): owns id, currentFloor, current ElevatorState, and a
 *   sorted set of pending stop floors (TreeSet split into "up stops" and
 *   "down stops" conceptually via simple sorted structures). Exposes
 *   addStop(floor), step() to advance one time unit, and getters used by
 *   the scheduler (currentFloor, direction, isIdle).
 * - SchedulingStrategy (interface, Strategy pattern): selectElevator(List<Elevator>, floor, direction).
 *   NearestElevatorStrategy implementation picks the idle elevator with the
 *   smallest |currentFloor - requestedFloor|, falling back to an elevator
 *   already moving toward the requested floor in the same direction.
 * - ElevatorController (Facade/manager, effectively a Singleton per building):
 *   holds the list of Elevators and the SchedulingStrategy, exposes
 *   requestElevator(floor, direction) for external hall calls and
 *   selectFloor(elevatorId, floor) for internal cabin calls, and tick() to
 *   advance simulation time for all elevators by one step.
 *
 * ================= WHY THIS DESIGN =================
 * - State pattern makes every legal transition explicit and keeps
 *   direction-specific movement logic out of the Elevator class itself
 *   (Single Responsibility, Open/Closed - new states/behaviors can be added
 *   without touching existing state classes).
 * - Strategy pattern isolates elevator-selection logic behind an interface so
 *   the scheduling algorithm (nearest idle, look-ahead, load balancing, etc.)
 *   can be swapped without changing Elevator or ElevatorController.
 * - ElevatorController centralizes coordination (Facade) so callers do not
 *   need to know how many elevators exist or how they are chosen.
 * - Each class has one reason to change: Elevator changes only if per-cabin
 *   mechanics change; SchedulingStrategy changes only if selection policy
 *   changes; states change only if transition rules change.
 *
 * ================= EDGE CASES =================
 * - Request for a floor outside valid range [0, maxFloor] must be rejected.
 * - Request for the floor the elevator is already on and idle - should just open doors.
 * - All elevators busy moving away from the requested floor - fallback to
 *   nearest one; system should never throw, just may be slower.
 * - Duplicate stop requests for the same floor should not queue twice.
 * - Concurrent external requests from multiple floors hitting the same
 *   controller simultaneously (multi-threaded callers) must not corrupt the
 *   elevator's internal stop queue.
 * - Elevator with zero pending stops on step() should transition to IDLE, not error.
 *
 * ================= COMPLEXITY =================
 * // Time Complexity: requestElevator O(E log F) where E = elevators, F = pending
 * // stops per elevator (sorted set operations); step() O(log F) per elevator per tick.
 * // Space Complexity: O(E * F) for storing pending stops across all elevators.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - How would you make Elevator thread-safe if requests can arrive from
 *   multiple threads concurrently (e.g. synchronize addStop, or use a
 *   concurrent sorted collection / single-threaded actor per elevator)?
 * - How would you extend this to support an "express" elevator that only
 *   serves specific floors (e.g. lobby + top floors)?
 * - How would you implement a look-ahead scheduling strategy that considers
 *   an elevator already moving in the same direction and about to pass the
 *   requested floor, not just idle elevators?
 * - How would you handle capacity limits (max weight/people) per elevator?
 * - How would you add fairness/starvation prevention so a far-away floor is
 *   not perpetually skipped in favor of closer requests?
 * - How would you persist elevator state so the system can recover after a restart?
 * - How would you unit test the State transitions in isolation from scheduling?
 * - How would you add metrics/logging (e.g. average wait time) without
 *   polluting the core domain classes?
 */
public class ElevatorSystem {

    // ================= Direction =================
    public enum Direction {
        UP, DOWN, IDLE
    }

    // ================= ElevatorState (State Pattern) =================
    public interface ElevatorState {
        /** Advance the elevator by one simulation step and return the resulting state. */
        ElevatorState step(Elevator elevator);

        Direction direction();
    }

    public static class IdleState implements ElevatorState {
        @Override
        public ElevatorState step(Elevator elevator) {
            Integer nextStop = elevator.peekNextStop();
            if (nextStop == null) {
                return this; // remain idle, nothing to do
            }
            if (nextStop == elevator.getCurrentFloor()) {
                elevator.removeStop(nextStop);
                return new DoorOpenState();
            }
            return nextStop > elevator.getCurrentFloor() ? new MovingUpState() : new MovingDownState();
        }

        @Override
        public Direction direction() {
            return Direction.IDLE;
        }

        @Override
        public String toString() {
            return "IDLE";
        }
    }

    public static class MovingUpState implements ElevatorState {
        @Override
        public ElevatorState step(Elevator elevator) {
            elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
            if (elevator.hasStop(elevator.getCurrentFloor())) {
                elevator.removeStop(elevator.getCurrentFloor());
                return new DoorOpenState();
            }
            Integer nextStop = elevator.peekNextStop();
            if (nextStop == null) {
                return new IdleState();
            }
            return nextStop >= elevator.getCurrentFloor() ? this : new MovingDownState();
        }

        @Override
        public Direction direction() {
            return Direction.UP;
        }

        @Override
        public String toString() {
            return "MOVING_UP";
        }
    }

    public static class MovingDownState implements ElevatorState {
        @Override
        public ElevatorState step(Elevator elevator) {
            elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
            if (elevator.hasStop(elevator.getCurrentFloor())) {
                elevator.removeStop(elevator.getCurrentFloor());
                return new DoorOpenState();
            }
            Integer nextStop = elevator.peekNextStop();
            if (nextStop == null) {
                return new IdleState();
            }
            return nextStop <= elevator.getCurrentFloor() ? this : new MovingUpState();
        }

        @Override
        public Direction direction() {
            return Direction.DOWN;
        }

        @Override
        public String toString() {
            return "MOVING_DOWN";
        }
    }

    public static class DoorOpenState implements ElevatorState {
        @Override
        public ElevatorState step(Elevator elevator) {
            // Door was just opened this tick; next tick decide where to go (or stay idle).
            Integer nextStop = elevator.peekNextStop();
            if (nextStop == null) {
                return new IdleState();
            }
            return nextStop > elevator.getCurrentFloor() ? new MovingUpState()
                    : nextStop < elevator.getCurrentFloor() ? new MovingDownState() : this;
        }

        @Override
        public Direction direction() {
            return Direction.IDLE;
        }

        @Override
        public String toString() {
            return "DOOR_OPEN";
        }
    }

    // ================= Elevator =================
    public static class Elevator {
        private final int id;
        private final int maxFloor;
        private int currentFloor;
        private ElevatorState state;
        // Pending stop floors, kept sorted; simple sorted structure is enough at this scale.
        private final java.util.TreeSet<Integer> pendingStops = new java.util.TreeSet<>();

        public Elevator(int id, int maxFloor) {
            this.id = id;
            this.maxFloor = maxFloor;
            this.currentFloor = 0;
            this.state = new IdleState();
        }

        public synchronized void addStop(int floor) {
            if (floor < 0 || floor > maxFloor) {
                throw new IllegalArgumentException("Floor out of range: " + floor);
            }
            pendingStops.add(floor);
            if (state instanceof IdleState) {
                // Kick off movement toward the newly added stop immediately.
                state = state.step(this);
            }
        }

        public synchronized void step() {
            state = state.step(this);
        }

        synchronized Integer peekNextStop() {
            if (pendingStops.isEmpty()) {
                return null;
            }
            // Prefer continuing in current direction; otherwise nearest stop overall.
            if (state.direction() == Direction.UP) {
                Integer ceil = pendingStops.ceiling(currentFloor);
                return ceil != null ? ceil : pendingStops.first();
            }
            if (state.direction() == Direction.DOWN) {
                Integer floor = pendingStops.floor(currentFloor);
                return floor != null ? floor : pendingStops.last();
            }
            return pendingStops.first();
        }

        synchronized boolean hasStop(int floor) {
            return pendingStops.contains(floor);
        }

        synchronized void removeStop(int floor) {
            pendingStops.remove(floor);
        }

        public synchronized int getCurrentFloor() {
            return currentFloor;
        }

        synchronized void setCurrentFloor(int floor) {
            this.currentFloor = floor;
        }

        public synchronized ElevatorState getState() {
            return state;
        }

        public int getId() {
            return id;
        }

        public synchronized boolean isIdle() {
            return state instanceof IdleState;
        }

        public synchronized Direction getDirection() {
            return state.direction();
        }

        @Override
        public synchronized String toString() {
            return "Elevator#" + id + "[floor=" + currentFloor + ", state=" + state + ", stops=" + pendingStops + "]";
        }
    }

    // ================= SchedulingStrategy (Strategy Pattern) =================
    public interface SchedulingStrategy {
        Elevator selectElevator(List<Elevator> elevators, int requestedFloor, Direction requestedDirection);
    }

    /** Picks the nearest idle elevator; falls back to nearest elevator moving toward the floor. */
    public static class NearestElevatorStrategy implements SchedulingStrategy {
        @Override
        public Elevator selectElevator(List<Elevator> elevators, int requestedFloor, Direction requestedDirection) {
            Elevator best = null;
            int bestDistance = Integer.MAX_VALUE;
            // First pass: prefer an idle elevator with the smallest distance.
            for (Elevator e : elevators) {
                if (e.isIdle()) {
                    int distance = Math.abs(e.getCurrentFloor() - requestedFloor);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = e;
                    }
                }
            }
            if (best != null) {
                return best;
            }
            // Second pass: elevator already moving toward the requested floor in the same direction.
            for (Elevator e : elevators) {
                boolean movingTowards = (e.getDirection() == Direction.UP && e.getCurrentFloor() <= requestedFloor)
                        || (e.getDirection() == Direction.DOWN && e.getCurrentFloor() >= requestedFloor);
                if (e.getDirection() == requestedDirection && movingTowards) {
                    int distance = Math.abs(e.getCurrentFloor() - requestedFloor);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = e;
                    }
                }
            }
            if (best != null) {
                return best;
            }
            // Fallback: just the nearest elevator overall.
            for (Elevator e : elevators) {
                int distance = Math.abs(e.getCurrentFloor() - requestedFloor);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = e;
                }
            }
            return best;
        }
    }

    // ================= ElevatorController (Facade / manager) =================
    public static class ElevatorController {
        private final List<Elevator> elevators = new ArrayList<>();
        private final SchedulingStrategy schedulingStrategy;

        public ElevatorController(int numElevators, int maxFloor, SchedulingStrategy schedulingStrategy) {
            this.schedulingStrategy = schedulingStrategy;
            for (int i = 0; i < numElevators; i++) {
                elevators.add(new Elevator(i, maxFloor));
            }
        }

        /** External hall call: someone on `floor` presses UP or DOWN. */
        public Elevator requestElevator(int floor, Direction direction) {
            Elevator chosen = schedulingStrategy.selectElevator(elevators, floor, direction);
            chosen.addStop(floor);
            return chosen;
        }

        /** Internal cabin call: someone inside elevator `elevatorId` selects a destination floor. */
        public void selectFloor(int elevatorId, int floor) {
            elevators.get(elevatorId).addStop(floor);
        }

        /** Advance simulation by one time unit for every elevator. */
        public void tick() {
            for (Elevator e : elevators) {
                e.step();
            }
        }

        public List<Elevator> getElevators() {
            return elevators;
        }
    }

    public static void main(String[] args) {
        ElevatorController controller = new ElevatorController(3, 10, new NearestElevatorStrategy());

        // Operation 1: external hall call from floor 5, going up -> nearest idle elevator (all at floor 0) is elevator 0.
        Elevator chosen1 = controller.requestElevator(5, Direction.UP);
        System.out.println("Request floor=5 UP assigned to " + chosen1); // Expected: Elevator#0 chosen (first idle, tie-break by list order)

        // Operation 2: internal cabin call - passenger inside elevator 0 selects floor 8.
        controller.selectFloor(0, 8);
        System.out.println("Elevator 0 after internal request: " + controller.getElevators().get(0));

        // Operation 3: another external call from floor 2 going up - elevator 1 (idle at floor 0) is nearer than elevator 0 (busy).
        Elevator chosen2 = controller.requestElevator(2, Direction.UP);
        System.out.println("Request floor=2 UP assigned to " + chosen2); // Expected: Elevator#1 (idle and nearest)

        // Operation 4: simulate several ticks and observe state transitions for elevator 0.
        for (int i = 0; i < 6; i++) {
            controller.tick();
            System.out.println("Tick " + (i + 1) + " -> " + controller.getElevators().get(0));
        }
        // Expected: Elevator#0 moves UP each tick, stops (DOOR_OPEN) at floor 5, then continues to floor 8.

        // Operation 5: request a floor outside range should be rejected.
        try {
            controller.requestElevator(50, Direction.UP);
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected invalid floor request: " + ex.getMessage()); // Expected: Rejected invalid floor request: Floor out of range: 50
        }
    }
}
