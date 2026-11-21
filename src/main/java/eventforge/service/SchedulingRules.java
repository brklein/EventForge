package eventforge.service;

import eventforge.model.Event;
import eventforge.model.Room;
import java.time.LocalTime;
import java.util.List;

/**
 * The capacity and overlap rules shared by EventService and RegistrationService (Design §4.2, NFR-4).
 * Pure logic with no database access, so it can be unit tested on its own. The database triggers repeat
 * these checks as a second safeguard.
 */
public class SchedulingRules {

    /** Throws ValidationException unless end is after start (UC-2 step 4 / alt 4a). */
    public void checkTimeRange(LocalTime start, LocalTime end) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Throws BusinessRuleException unless room capacity is at least the event's maximum capacity
     * (UC-4 step 4 / alt 4a). The message states the room's maximum capacity. Equal capacity is allowed.
     */
    public void checkRoomCapacity(Room room, Event event) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * True if two time ranges on the same day overlap: startA &lt; endB AND endA &gt; startB.
     * Back-to-back ranges (one ends when the other starts) do not overlap (Design §5.2).
     */
    public boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Throws BusinessRuleException naming the conflicting event if any event in roomEvents, other than the event
     * itself, is on the same date and overlaps it (UC-4 step 5 / alt 5a).
     */
    public void checkNoOverlap(Event event, List<Event> roomEvents) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** True if another confirmed seat fits: confirmedCount &lt; maxCapacity (UC-5 step 3, NFR-4(3)). */
    public boolean hasSeat(int confirmedCount, int maxCapacity) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
