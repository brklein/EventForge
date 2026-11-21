package eventforge.service;

import eventforge.dao.AttendeeDao;
import eventforge.dao.DatabaseManager;
import eventforge.dao.RegistrationDao;
import eventforge.model.Attendee;
import eventforge.model.Registration;
import java.util.List;

/** Day-of check-in (UC-7). Event Staff or Organizer. Search must return within 2 seconds (NFR-1). */
public class CheckInService {
    private final DatabaseManager db;
    private final AttendeeDao attendeeDao;
    private final RegistrationDao registrationDao;
    private final AuthService auth;

    public CheckInService(DatabaseManager db, AttendeeDao attendeeDao, RegistrationDao registrationDao,
                          AuthService auth) {
        this.db = db;
        this.attendeeDao = attendeeDao;
        this.registrationDao = registrationDao;
        this.auth = auth;
    }

    /**
     * Finds attendees by name or badge ID (UC-7 step 1). Returns an empty list when nothing matches; the screen
     * then suggests checking the details or registering a new attendee (alt 1a).
     */
    public List<Attendee> searchAttendees(String query) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Records convention-level check-in with the current time and returns the updated attendee (UC-7 step 4).
     * Throws BusinessRuleException if the attendee is already checked in.
     */
    public Attendee checkIn(int attendeeId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Records check-in to one event. The attendee must hold a CONFIRMED registration for it; otherwise throws
     * BusinessRuleException (waitlisted attendees have no seat).
     */
    public Registration checkInToEvent(int attendeeId, int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
