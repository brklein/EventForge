package eventforge.dao;

import eventforge.model.Event;
import eventforge.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Reads and writes the events table (UC-2, UC-4). Overlap queries use the room/date index (NFR-1). */
public class EventDao implements Dao<Event> {

    @Override
    public Optional<Event> findById(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Event> findAll(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Event insert(Connection connection, Event entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void update(Connection connection, Event entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the events assigned to a room, ordered by date and start time (UC-3 alt 5a). */
    public List<Event> findByRoom(Connection connection, int roomId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns every SCHEDULED event, ordered by date and start time, for the Event Browser (UC-5 step 1). */
    public List<Event> findScheduled(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the first event in the room on that date whose time overlaps start–end, ignoring excludeEventId
     * (UC-4 step 5). Overlap means start &lt; other end AND end &gt; other start, so back-to-back events do not overlap.
     */
    public Optional<Event> findOverlapping(Connection connection, int roomId, LocalDate date,
                                           LocalTime start, LocalTime end, int excludeEventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Sets an event's room and status together (UC-4 step 6). roomId is null when unassigning. */
    public void updateAssignment(Connection connection, int eventId, Integer roomId, EventStatus status) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
