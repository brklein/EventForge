package eventforge.service;

import eventforge.dao.AttendeeDao;
import eventforge.dao.DatabaseManager;
import eventforge.dao.UserDao;
import eventforge.model.Attendee;
import eventforge.model.Role;
import eventforge.model.User;
import java.util.Optional;
import java.util.Set;

/**
 * Logs users in and holds the current session (NFR-5, Design §8). Other services call requireRole or
 * requireAttendee before acting, so permissions are enforced in the service layer, not only by hiding buttons.
 * Only one session is active at a time: logging in as staff ends any attendee session, and the reverse.
 */
public class AuthService {
    private final DatabaseManager db;
    private final UserDao userDao;
    private final AttendeeDao attendeeDao;
    private final PasswordHasher hasher;
    private User currentUser;          // null = no staff session
    private Attendee currentAttendee;  // null = no attendee self-service session

    public AuthService(DatabaseManager db, UserDao userDao, AttendeeDao attendeeDao, PasswordHasher hasher) {
        this.db = db;
        this.userDao = userDao;
        this.attendeeDao = attendeeDao;
        this.hasher = hasher;
    }

    /**
     * Logs in an Organizer or Event Staff account. Throws ValidationException with one message for an unknown
     * username or a wrong password, so the screen does not reveal which one was wrong.
     */
    public User login(String username, String password) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Starts an attendee self-service session. Attendees identify themselves with attendee ID and email, with
     * no password (Design §7). Throws ValidationException if the pair does not match a record.
     */
    public Attendee loginAttendee(int attendeeId, String email) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Ends the current session, staff or attendee. */
    public void logout() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<User> getCurrentUser() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<Attendee> getCurrentAttendee() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Throws AccessDeniedException unless a staff user is logged in with one of the allowed roles. */
    public User requireRole(Role... allowed) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the ID of the attendee in the current self-service session, or throws AccessDeniedException.
     * Attendee operations take their ID from here, never from free-form input (Design §8).
     */
    public int requireAttendee() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Menu functions a role may use (Design §7; Matrix #1). Organizer: all. Event Staff: register and
     * search/edit attendees, view events, check in.
     */
    public Set<Permission> permissionsFor(Role role) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Creates a staff account with a new salt and hash. Organizer only. Used for setup and the demo data. */
    public User createUser(String username, String password, Role role) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
