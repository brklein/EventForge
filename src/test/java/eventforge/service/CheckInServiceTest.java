package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Attendee;
import eventforge.model.Registration;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for day-of check-in (UC-7; Matrix #16). */
class CheckInServiceTest {
    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
    }

    @Test
    @DisplayName("[UC-7 main flow] Check-in records timestamp and marks attendee present (Matrix #16)")
    void matrix16_checkIn() {
        app.loginAsStaff();
        Attendee checkedIn = app.checkIn.checkIn(11);

        assertTrue(checkedIn.isCheckedIn(), "attendee should show as Present");
        String stored = database.text("SELECT checked_in_at FROM attendees WHERE attendee_id = 11");
        assertNotNull(stored);
        Duration age = Duration.between(LocalDateTime.parse(stored), LocalDateTime.now()).abs();
        assertTrue(age.toMinutes() < 1, "timestamp should be within 1 minute of now: " + stored);
    }

    @Test
    @DisplayName("[UC-7] Checking in the same attendee twice is refused")
    void uc7_alreadyCheckedIn() {
        app.loginAsStaff();
        app.checkIn.checkIn(11);
        assertThrows(BusinessRuleException.class, () -> app.checkIn.checkIn(11));
    }

    @Test
    @DisplayName("[UC-7 alt 1a] Search with no matching attendee returns no results")
    void uc7Alt1a_noMatch() {
        app.loginAsStaff();
        assertTrue(app.checkIn.searchAttendees("Zzyzx").isEmpty());
        assertTrue(app.checkIn.searchAttendees("999").isEmpty());
    }

    @Test
    @DisplayName("[UC-7 step 1] Search finds an attendee by badge ID or by name, ignoring case")
    void uc7_searchByBadgeOrName() {
        app.loginAsStaff();
        List<Attendee> byBadge = app.checkIn.searchAttendees("12");
        assertEquals(List.of("Jamie Moore"), byBadge.stream()
                .map(a -> a.getFirstName() + " " + a.getLastName()).toList());
        assertEquals(List.of(2), app.checkIn.searchAttendees("lee").stream().map(Attendee::getAttendeeId).toList());
    }

    @Test
    @DisplayName("[UC-7] Event-level check-in needs a confirmed seat")
    void uc7_eventCheckIn() {
        app.loginAsStaff();
        Registration seat = app.checkIn.checkInToEvent(6, 9);           // attendee 6: confirmed, not checked in
        assertNotNull(seat.getCheckedInAt());
        assertNotNull(database.text("SELECT checked_in_at FROM registrations WHERE attendee_id = 6 AND event_id = 9"));
        assertThrows(BusinessRuleException.class, () -> app.checkIn.checkInToEvent(9, 9));   // waitlisted
        assertThrows(BusinessRuleException.class, () -> app.checkIn.checkInToEvent(11, 9));  // not registered
    }

    @Test
    @DisplayName("[NFR-5] Check-in needs a staff login")
    void nfr5_checkInNeedsStaff() {
        app.loginAsAttendee(11);
        assertThrows(AccessDeniedException.class, () -> app.checkIn.checkIn(11));
        assertTrue(database.text("SELECT checked_in_at FROM attendees WHERE attendee_id = 11") == null);
    }
}
