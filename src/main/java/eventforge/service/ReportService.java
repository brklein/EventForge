package eventforge.service;

import eventforge.dao.DatabaseManager;
import eventforge.dao.ReportDao;
import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import eventforge.model.EventAttendance;
import eventforge.model.RoomUtilization;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Administrative Dashboard reports (UC-8). Organizer only. An empty list means no data matched the filters;
 * the screen shows a "no results" message (alt 4a). Screens run these on a background JavaFX Task (Design §8).
 */
public class ReportService {
    private final DatabaseManager db;
    private final ReportDao reportDao;
    private final CsvExporter csv;
    private final AuthService auth;

    public ReportService(DatabaseManager db, ReportDao reportDao, CsvExporter csv, AuthService auth) {
        this.db = db;
        this.reportDao = reportDao;
        this.csv = csv;
        this.auth = auth;
    }

    /** Attendee list for a convention, optionally one badge type (null = all). */
    public List<Attendee> attendeeList(int conventionId, BadgeType badgeType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Event attendance between two dates, optionally one event (null = all). Throws ValidationException if
     * from is after to.
     */
    public List<EventAttendance> eventAttendance(LocalDate from, LocalDate to, Integer eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Room utilization between two dates. Throws ValidationException if from is after to. */
    public List<RoomUtilization> roomUtilization(LocalDate from, LocalDate to) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Exports event attendance rows to a CSV file that opens in Excel (UC-8 step 5). */
    public void exportEventAttendance(List<EventAttendance> rows, Path file) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
