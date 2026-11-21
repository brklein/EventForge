package eventforge.dao;

import eventforge.model.Registration;
import eventforge.model.RegistrationStatus;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Reads and writes the registrations table (UC-5, UC-5a, UC-6, UC-7). Uses the event/status index (NFR-1). */
public class RegistrationDao implements Dao<Registration> {

    @Override
    public Optional<Registration> findById(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Registration> findAll(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Registration insert(Connection connection, Registration entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void update(Connection connection, Registration entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the attendee's registration for an event, or empty (UC-5 alt 2a). */
    public Optional<Registration> findByAttendeeAndEvent(Connection connection, int attendeeId, int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns all of one attendee's registrations, ordered by event date and time (UC-6 step 1). */
    public List<Registration> findByAttendee(Connection connection, int attendeeId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Counts CONFIRMED registrations for an event: the seat-count check of UC-5 step 3. */
    public int countConfirmed(Connection connection, int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the oldest WAITLISTED registration for an event (by registered_at, then ID), or empty (UC-6 step 5). */
    public Optional<Registration> findFirstWaitlisted(Connection connection, int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Changes a registration's status and promotion notice flag, for waitlist promotion (UC-6 step 5). */
    public void updateStatus(Connection connection, int registrationId, RegistrationStatus status,
                             boolean promotionNotice) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Clears the promotion notice once the attendee has seen the message (Design §6.3). */
    public void clearPromotionNotice(Connection connection, int registrationId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Records event-level check-in by setting checked_in_at (UC-7). */
    public void setCheckedIn(Connection connection, int registrationId, LocalDateTime checkedInAt) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
