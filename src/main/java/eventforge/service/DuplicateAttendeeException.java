package eventforge.service;

import eventforge.model.Attendee;
import java.util.List;

/**
 * Thrown when a new attendee matches an existing record by name or email (UC-1 alt 5a).
 * The screen shows the matches and asks whether to proceed or view the existing record.
 */
public class DuplicateAttendeeException extends BusinessRuleException {
    private final List<Attendee> matches;

    /** Creates the warning, carrying the existing records that matched. */
    public DuplicateAttendeeException(List<Attendee> matches) {
        super("An attendee with this name or email already exists.");
        this.matches = List.copyOf(matches);
    }

    public List<Attendee> getMatches() { return matches; }
}
