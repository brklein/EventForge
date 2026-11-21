package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests for attendee registration, duplicates and search (UC-1; Matrix #3, #4). */
class AttendeeServiceTest {
    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
    }

    private int attendeeCount() {
        return database.count("SELECT COUNT(*) FROM attendees");
    }

    @Test
    @DisplayName("[UC-1 main flow] Organizer creates an attendee record with valid data and receives a unique ID (Matrix #3)")
    void matrix3_registerAttendee() {
        app.loginAsOrganizer();
        Attendee jane = app.attendees.registerAttendee(1, "Jane", "Doe", "jane@example.com", "555-0199",
                BadgeType.GENERAL, false);

        assertTrue(jane.getAttendeeId() > 20, "expected a new ID after the 20 test attendees");
        assertEquals(1, database.count("SELECT COUNT(*) FROM attendees WHERE email = 'jane@example.com'"));
        assertEquals("Doe", database.text("SELECT last_name FROM attendees WHERE attendee_id = ?",
                jane.getAttendeeId()));
        assertEquals("GENERAL", database.text("SELECT badge_type FROM attendees WHERE attendee_id = ?",
                jane.getAttendeeId()));
    }

    @ParameterizedTest(name = "missing {0}")
    @CsvSource(value = {
            "firstName, '',     Doe, jane@example.com, GENERAL",
            "lastName,  Jane,   '',  jane@example.com, GENERAL",
            "email,     Jane,   Doe, '',               GENERAL",
            "badgeType, Jane,   Doe, jane@example.com, NULL"
    }, nullValues = "NULL")
    @DisplayName("[UC-1 alt 3a] Missing required fields are rejected with a validation error (Matrix #4)")
    void matrix4_missingFieldRejected(String field, String first, String last, String email, BadgeType badge) {
        app.loginAsOrganizer();
        ValidationException e = assertThrows(ValidationException.class,
                () -> app.attendees.registerAttendee(1, first, last, email, null, badge, false));
        assertEquals(field, e.getField());
        assertEquals(20, attendeeCount());
    }

    @Test
    @DisplayName("[UC-1 alt 5a] Same name as an existing attendee warns before saving")
    void uc1Alt5a_duplicateNameWarned() {
        app.loginAsOrganizer();
        DuplicateAttendeeException e = assertThrows(DuplicateAttendeeException.class,
                () -> app.attendees.registerAttendee(1, "Alex", "Rivera", "alex.r@example.net", null,
                        BadgeType.GENERAL, false));
        assertEquals(List.of(1), e.getMatches().stream().map(Attendee::getAttendeeId).toList());
        assertEquals(20, attendeeCount());
    }

    @Test
    @DisplayName("[UC-1 alt 5a] Same email as an existing attendee warns before saving")
    void uc1Alt5a_duplicateEmailWarned() {
        app.loginAsOrganizer();
        assertThrows(DuplicateAttendeeException.class,
                () -> app.attendees.registerAttendee(1, "Lex", "Rivers", "ALEX.RIVERA@example.com", null,
                        BadgeType.GENERAL, false));
        assertEquals(20, attendeeCount());
    }

    @Test
    @DisplayName("[UC-1 alt 5a] Choosing to proceed saves the second record")
    void uc1Alt5a_duplicateSavedWhenConfirmed() {
        app.loginAsOrganizer();
        app.attendees.registerAttendee(1, "Alex", "Rivera", "alex.r@example.net", null, BadgeType.GENERAL, true);
        assertEquals(21, attendeeCount());
    }

    @Test
    @DisplayName("[UC-1 actors] Attendee self-registration works without a staff login")
    void uc1_selfRegistrationWithoutLogin() {
        Attendee self = app.attendees.registerAttendee(1, "Kai", "Ortiz", "kai@example.com", null,
                BadgeType.GUEST, false);
        assertTrue(self.getAttendeeId() > 0);
    }

    @Test
    @DisplayName("[UC-1; UC-7] Search finds attendees by name start, ignoring case, and by badge ID")
    void uc1_searchByNameAndBadge() {
        app.loginAsStaff();
        assertEquals(List.of("Rivera"), app.attendees.search("riv").stream().map(Attendee::getLastName).toList());
        assertEquals(List.of(12), app.attendees.search("12").stream().map(Attendee::getAttendeeId).toList());
    }

    @Test
    @DisplayName("[NFR-5] Searching attendee records needs a staff login")
    void nfr5_searchNeedsLogin() {
        assertThrows(AccessDeniedException.class, () -> app.attendees.search("Lee"));
    }

    @Test
    @DisplayName("[UC-1] Staff can edit an attendee record")
    void uc1_updateAttendee() {
        app.loginAsStaff();
        Attendee sam = app.attendees.findById(2).orElseThrow();
        Attendee edited = new Attendee(sam.getAttendeeId(), sam.getConventionId(), sam.getFirstName(),
                sam.getLastName(), "sam.new@example.com", sam.getPhone(), BadgeType.VENDOR, null);
        app.attendees.updateAttendee(edited);
        assertEquals("sam.new@example.com", database.text("SELECT email FROM attendees WHERE attendee_id = 2"));
        assertEquals("VENDOR", database.text("SELECT badge_type FROM attendees WHERE attendee_id = 2"));
    }
}
