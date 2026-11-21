package eventforge.service;

import eventforge.dao.DatabaseManager;
import eventforge.dao.EventDao;
import eventforge.dao.RoomDao;
import eventforge.model.Event;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Event creation and room assignment (UC-2, UC-4). Creating and assigning need the Organizer role; listing is
 * open to Event Staff as view only. The rules themselves live in SchedulingRules (Design §6.2).
 */
public class EventService {
    private final DatabaseManager db;
    private final EventDao eventDao;
    private final RoomDao roomDao;
    private final SchedulingRules rules;
    private final AuthService auth;

    public EventService(DatabaseManager db, EventDao eventDao, RoomDao roomDao, SchedulingRules rules,
                        AuthService auth) {
        this.db = db;
        this.eventDao = eventDao;
        this.roomDao = roomDao;
        this.rules = rules;
        this.auth = auth;
    }

    /**
     * Creates an event with status UNASSIGNED and no room (UC-2 steps 4–5). Throws ValidationException if the
     * name is blank, the end time is not after the start time, or the capacity is not positive (alt 4a).
     * Capacity text is parsed first with InputValidator.parsePositiveInt.
     */
    public Event createEvent(int conventionId, String name, String description, LocalDate date,
                             LocalTime startTime, LocalTime endTime, int maxCapacity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Assigns an event to a room in one transaction (UC-4, Design §6.2):
     * 1. requireRole(ORGANIZER); 2. load both, reject if missing or already scheduled;
     * 3. rules.checkRoomCapacity (alt 4a); 4. rules.checkNoOverlap with the room's events (alt 5a);
     * 5. set room and status SCHEDULED, commit. The room trigger repeats steps 3–4.
     * Returns the updated event.
     */
    public Event assignRoom(int eventId, int roomId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Removes an event's room and sets it back to UNASSIGNED (Assign Room dialog "Remove", Design §7). */
    public Event unassignRoom(int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** All events with their status, for the Event List (Organizer, or Event Staff view only). */
    public List<Event> listEvents() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
