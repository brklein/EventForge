package eventforge.dao;

import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import eventforge.model.EventAttendance;
import eventforge.model.RoomUtilization;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Read-only summary queries for the Administrative Dashboard (UC-8). Each report is one aggregate query
 * so a full convention renders within 5 seconds (NFR-1). Null filter arguments mean "no filter".
 */
public class ReportDao {

    /** Attendees of a convention, optionally only one badge type, ordered by last name then first name. */
    public List<Attendee> attendeeList(Connection connection, int conventionId, BadgeType badgeType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Confirmed, waitlisted and checked-in counts for every event between two dates (inclusive), optionally one
     * event only. Unassigned events are included with a null room name. Ordered by date, then start time.
     */
    public List<EventAttendance> eventAttendance(Connection connection, LocalDate from, LocalDate to,
                                                 Integer eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Scheduled events, minutes and confirmed seats per room between two dates (inclusive). Every room is listed,
     * including rooms with no events (zeros), ordered by room name.
     */
    public List<RoomUtilization> roomUtilization(Connection connection, LocalDate from, LocalDate to) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
