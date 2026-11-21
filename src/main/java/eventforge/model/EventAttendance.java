package eventforge.model;

import java.time.LocalDate;
import java.util.List;

/** One row of the Event Attendance report (UC-8): seats, waitlist and check-ins for one event. */
public record EventAttendance(int eventId, String eventName, LocalDate date, String roomName,
                              int maxCapacity, int confirmed, int waitlisted, int checkedIn) {

    /** Column headings used for the on-screen table and the CSV export. */
    public static List<String> csvHeader() {
        return List.of("Event ID", "Event", "Date", "Room", "Capacity", "Confirmed", "Waitlisted", "Checked In");
    }

    /** This row's values in the same order as csvHeader(). */
    public List<String> toCsvRow() {
        // TODO P8: return the fields as text, in csvHeader() order.
        throw new UnsupportedOperationException("Not implemented");
    }
}
