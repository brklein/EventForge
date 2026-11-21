package eventforge.service;

import eventforge.dao.AttendeeDao;
import eventforge.dao.DatabaseManager;
import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import java.util.List;
import java.util.Optional;

/**
 * Attendee registration, search and editing (UC-1). Registration is open to Organizers, Event Staff and the
 * self-registration screen; search and edit need a staff login. Validates and saves within 2 seconds (NFR-1).
 */
public class AttendeeService {
    private final DatabaseManager db;
    private final AttendeeDao attendeeDao;
    private final AuthService auth;

    public AttendeeService(DatabaseManager db, AttendeeDao attendeeDao, AuthService auth) {
        this.db = db;
        this.attendeeDao = attendeeDao;
        this.auth = auth;
    }

    /**
     * Creates an attendee record and returns it with its new attendee ID (UC-1 steps 4–6).
     * Validates name, email and badge type first (alt 3a: ValidationException naming the field). If a record with
     * the same name or email exists and allowDuplicate is false, throws DuplicateAttendeeException (alt 5a);
     * the screen then asks the user and calls again with allowDuplicate true to proceed. Phone is optional.
     */
    public Attendee registerAttendee(int conventionId, String firstName, String lastName, String email,
                                     String phone, BadgeType badgeType, boolean allowDuplicate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Searches by name or badge ID (Organizer or Event Staff). */
    public List<Attendee> search(String query) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns one attendee (Organizer or Event Staff). */
    public Optional<Attendee> findById(int attendeeId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Saves edits to an attendee record after the same validation as registration (Organizer or Event Staff). */
    public Attendee updateAttendee(Attendee attendee) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
