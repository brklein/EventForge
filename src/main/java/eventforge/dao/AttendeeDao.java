package eventforge.dao;

import eventforge.model.Attendee;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Reads and writes the attendees table (UC-1, UC-7). Searches use the name and email indexes (NFR-1). */
public class AttendeeDao implements Dao<Attendee> {

    @Override
    public Optional<Attendee> findById(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Attendee> findAll(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Attendee insert(Connection connection, Attendee entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void update(Connection connection, Attendee entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Finds attendees by name or badge ID for search and check-in (UC-1, UC-7 step 1).
     * A whole number is treated as a badge (attendee) ID; other text matches the start of the first or last name,
     * ignoring case. Results are ordered by last name, then first name.
     */
    public List<Attendee> search(Connection connection, String query) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns attendees with the same first and last name, or the same email, ignoring case (UC-1 alt 5a). */
    public List<Attendee> findPossibleDuplicates(Connection connection, String firstName, String lastName,
                                                 String email) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Records convention-level check-in by setting checked_in_at (UC-7 step 4). */
    public void setCheckedIn(Connection connection, int attendeeId, LocalDateTime checkedInAt) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
