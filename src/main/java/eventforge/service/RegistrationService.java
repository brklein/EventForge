package eventforge.service;

import eventforge.dao.DatabaseManager;
import eventforge.dao.EventDao;
import eventforge.dao.RegistrationDao;
import eventforge.model.Event;
import eventforge.model.Registration;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Event registration, waitlist and cancellation for the attendee in the current self-service session
 * (UC-5, UC-5a, UC-6). Seat checks run inside BEGIN IMMEDIATE so two attendees cannot take the last seat
 * at once (NFR-4(3), Design §6.1). The capacity trigger is the backstop.
 */
public class RegistrationService {
    private final DatabaseManager db;
    private final RegistrationDao registrationDao;
    private final EventDao eventDao;
    private final SchedulingRules rules;
    private final AuthService auth;

    public RegistrationService(DatabaseManager db, RegistrationDao registrationDao, EventDao eventDao,
                               SchedulingRules rules, AuthService auth) {
        this.db = db;
        this.registrationDao = registrationDao;
        this.eventDao = eventDao;
        this.rules = rules;
        this.auth = auth;
    }

    /** Scheduled events open for registration (UC-5 step 1). */
    public List<Event> browseEvents() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Seats left: maxCapacity minus confirmed registrations. Never negative. */
    public int remainingSeats(int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Registers the current attendee for a scheduled event (UC-5 steps 2–5). Returns ALREADY_REGISTERED if they
     * hold any registration for it (alt 2a), FULL if no seat is left (alt 3a; nothing is saved), otherwise saves a
     * CONFIRMED registration and returns CONFIRMED. Throws BusinessRuleException if the event is not scheduled.
     */
    public RegistrationResult register(int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Adds the current attendee to an event's waitlist after register returned FULL (UC-5a). Throws
     * BusinessRuleException if the event has a free seat (register instead) or they are already registered.
     */
    public Registration joinWaitlist(int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Cancels the current attendee's registration for an event (UC-6 step 4). The confirmation prompt (step 3)
     * is shown by the screen before this is called. If a CONFIRMED seat was freed, promoteNextWaitlisted runs in
     * the same transaction (step 5). Throws BusinessRuleException if there is no registration to cancel.
     */
    public void cancelRegistration(int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Promotes the oldest waitlisted registration to CONFIRMED and sets its promotion notice (Design §6.3).
     * Called only inside cancelRegistration's transaction. Returns the promoted registration, or empty.
     */
    Optional<Registration> promoteNextWaitlisted(Connection connection, int eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** The current attendee's registrations for "My Registrations" (UC-6 step 1). */
    public List<Registration> myRegistrations() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the current attendee's registrations promoted from the waitlist since their last visit and clears
     * the notices, so the in-app message shows once (Design §6.3).
     */
    public List<Registration> takePromotionNotices() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
