package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import eventforge.model.EventAttendance;
import eventforge.model.RoomUtilization;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for the Administrative Dashboard reports (UC-8; Matrix #17). */
class ReportServiceTest {
    private static final LocalDate FIRST_DAY = LocalDate.of(2026, 10, 9);
    private static final LocalDate LAST_DAY = LocalDate.of(2026, 10, 11);

    @TempDir Path folder;
    private TestServices app;

    @BeforeEach
    void setUp() {
        app = new TestServices(TestDatabase.withTestData(folder));
        app.loginAsOrganizer();
    }

    @Test
    @DisplayName("[UC-8] Event Attendance report is accurate (Matrix #17)")
    void matrix17_eventAttendanceAccurate() {
        List<EventAttendance> rows = app.reports.eventAttendance(FIRST_DAY, LAST_DAY, 9);
        assertEquals(1, rows.size());
        EventAttendance photoshoot = rows.get(0);
        assertEquals("Cosplay Photoshoot", photoshoot.eventName());
        assertEquals("Room C", photoshoot.roomName());
        assertEquals(8, photoshoot.maxCapacity());
        assertEquals(8, photoshoot.confirmed());
        assertEquals(2, photoshoot.waitlisted());
        assertEquals(5, photoshoot.checkedIn());
    }

    @Test
    @DisplayName("[UC-8 step 3] Date filter keeps only that day's events, in time order")
    void uc8_dateFilter() {
        LocalDate oct10 = LocalDate.of(2026, 10, 10);
        assertEquals(List.of("Artist Alley Tour", "Cosplay Photoshoot", "Mega Fan Meetup"),
                app.reports.eventAttendance(oct10, oct10, null).stream().map(EventAttendance::eventName).toList());
    }

    @Test
    @DisplayName("[UC-8 alt 4a] No data matching the filters gives an empty report")
    void uc8Alt4a_noMatchingData() {
        assertTrue(app.reports.eventAttendance(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 31), null).isEmpty());
    }

    @Test
    @DisplayName("[UC-8 step 3] Start date after end date is rejected")
    void uc8_reversedDatesRejected() {
        assertThrows(ValidationException.class, () -> app.reports.eventAttendance(LAST_DAY, FIRST_DAY, null));
    }

    @Test
    @DisplayName("[UC-8] Attendee list filters by badge type")
    void uc8_attendeeList() {
        assertEquals(20, app.reports.attendeeList(1, null).size());
        assertEquals(List.of("Davis", "Lee"), app.reports.attendeeList(1, BadgeType.VIP).stream()
                .map(Attendee::getLastName).toList());
    }

    @Test
    @DisplayName("[UC-8] Room utilization counts events, minutes and confirmed seats per room")
    void uc8_roomUtilization() {
        Map<String, RoomUtilization> byRoom = app.reports.roomUtilization(FIRST_DAY, FIRST_DAY).stream()
                .collect(Collectors.toMap(RoomUtilization::roomName, Function.identity()));
        assertEquals(4, byRoom.size(), "every room is listed, including unused ones");
        assertEquals(2, byRoom.get("Room B").scheduledEvents());
        assertEquals(120, byRoom.get("Room B").scheduledMinutes());
        assertEquals(1, byRoom.get("Room B").confirmedSeats());
        assertEquals(0, byRoom.get("Room D").scheduledEvents());
    }

    @Test
    @DisplayName("[UC-8 step 5] Export writes a CSV file that opens in Excel")
    void uc8Step5_exportCsv() throws Exception {
        Path file = folder.resolve("attendance.csv");
        app.reports.exportEventAttendance(app.reports.eventAttendance(FIRST_DAY, LAST_DAY, 9), file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(String.join(",", EventAttendance.csvHeader()), lines.get(0));
        assertEquals("9,Cosplay Photoshoot,2026-10-10,Room C,8,8,2,5", lines.get(1));
    }

    @Test
    @DisplayName("[NFR-5] Reports are Organizer only")
    void nfr5_reportsOrganizerOnly() {
        app.auth.logout();
        app.loginAsStaff();
        assertThrows(AccessDeniedException.class, () -> app.reports.attendeeList(1, null));
    }
}
