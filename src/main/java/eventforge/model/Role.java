package eventforge.model;

/** Privilege level of a logged-in {@link User}. Attendees do not log in, so they have no role. */
public enum Role {
    ORGANIZER,
    EVENT_STAFF
}