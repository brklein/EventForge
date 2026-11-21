package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Event;
import eventforge.model.EventStatus;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for event creation and room assignment (UC-2, UC-4; Matrix #5–#7, #10–#12). */
class EventServiceTest {
    private static final LocalDate OCT_10 = LocalDate.of(2026, 10, 10);

    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
        app.loginAsOrganizer();
    }

    private int eventCount() {
        return database.count("SELECT COUNT(*) FROM events");
    }

    private String statusOf(int eventId) {
        return database.text("SELECT status FROM events WHERE event_id = ?", eventId);
    }

    @Test
    @DisplayName("[UC-2 main flow] Organizer creates a valid event with status 'Unassigned' (Matrix #5)")
    void matrix5_createEvent() {
        Event created = app.events.createEvent(1, "Intro to Cosplay", "Getting started", OCT_10,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 25);

        assertEquals(EventStatus.UNASSIGNED, created.getStatus());
        assertNull(created.getRoomId());
        assertEquals("UNASSIGNED", statusOf(created.getEventId()));
        assertNull(database.text("SELECT room_id FROM events WHERE event_id = ?", created.getEventId()));
        assertEquals(10, eventCount());
    }

    @Test
    @DisplayName("[UC-2 alt 4a] End time before start time is rejected (Matrix #6)")
    void matrix6_endBeforeStartRejected() {
        assertThrows(ValidationException.class, () -> app.events.createEvent(1, "Intro to Cosplay", "", OCT_10,
                LocalTime.of(11, 0), LocalTime.of(10, 0), 25));
        assertEquals(9, eventCount());
    }

    @Test
    @DisplayName("[UC-2 alt 4a] Non-positive capacity is rejected (Matrix #7)")
    void matrix7_nonPositiveCapacityRejected() {
        // Non-integer text ("2.5", "abc", blank) is rejected earlier by InputValidator; see InputValidatorTest.
        for (int capacity : new int[] {0, -5}) {
            ValidationException e = assertThrows(ValidationException.class, () -> app.events.createEvent(1,
                    "Intro to Cosplay", "", OCT_10, LocalTime.of(10, 0), LocalTime.of(11, 0), capacity));
            assertEquals("maxCapacity", e.getField());
        }
        assertEquals(9, eventCount());
    }

    @Test
    @DisplayName("[UC-2 alt 4a] Blank event name is rejected")
    void uc2_blankNameRejected() {
        assertThrows(ValidationException.class, () -> app.events.createEvent(1, " ", "", OCT_10,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 25));
        assertEquals(9, eventCount());
    }

    @Test
    @DisplayName("[UC-4 main flow] Valid assignment saves and sets status to 'Scheduled' (Matrix #10)")
    void matrix10_assignRoom() {
        // Panel: Indie Comics (capacity 25) into Room D (capacity 40, no events).
        Event scheduled = app.events.assignRoom(5, 4);
        assertEquals(EventStatus.SCHEDULED, scheduled.getStatus());
        assertEquals(4, scheduled.getRoomId());
        assertEquals("SCHEDULED", statusOf(5));
        assertEquals(4, database.count("SELECT room_id FROM events WHERE event_id = 5"));
    }

    @Test
    @DisplayName("[UC-4 alt 4a] Room with insufficient capacity is rejected and its capacity shown (Matrix #11)")
    void matrix11_roomTooSmallRejected() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> app.events.assignRoom(7, 2));
        assertTrue(e.getMessage().contains("30"), e.getMessage());
        assertEquals("UNASSIGNED", statusOf(7));
    }

    @Test
    @DisplayName("[UC-4 alt 5a] Overlapping event in same room is rejected and the conflicting event identified (Matrix #12)")
    void matrix12_overlapRejected() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> app.events.assignRoom(5, 2));
        assertTrue(e.getMessage().contains("Prop Making 101"), e.getMessage());
        assertEquals("UNASSIGNED", statusOf(5));
    }

    @Test
    @DisplayName("[UC-4 boundary] Room capacity equal to event capacity is accepted (supports Matrix #11)")
    void uc4Boundary_equalCapacityAccepted() {
        assertEquals(EventStatus.SCHEDULED, app.events.assignRoom(8, 2).getStatus());
    }

    @Test
    @DisplayName("[UC-4 boundary] Back-to-back events in the same room are accepted (supports Matrix #12)")
    void uc4Boundary_backToBackAccepted() {
        // Voice Acting Q&A 11:00–12:00 starts as Prop Making 101 (10:00–11:00) ends in Room B.
        assertEquals(EventStatus.SCHEDULED, app.events.assignRoom(6, 2).getStatus());
    }

    @Test
    @DisplayName("[UC-4 step 2] Already-scheduled event, missing event or missing room is rejected")
    void uc4_invalidTargetsRejected() {
        assertThrows(BusinessRuleException.class, () -> app.events.assignRoom(2, 4));     // already scheduled
        assertThrows(BusinessRuleException.class, () -> app.events.assignRoom(999, 4));   // no such event
        assertThrows(BusinessRuleException.class, () -> app.events.assignRoom(5, 999));   // no such room
        assertEquals(0, database.count("SELECT COUNT(*) FROM events WHERE room_id = 4"));
        assertEquals("UNASSIGNED", statusOf(5));
    }

    @Test
    @DisplayName("[UC-4; Design §7] Removing a room assignment returns the event to 'Unassigned'")
    void uc4_unassignRoom() {
        Event event = app.events.unassignRoom(3);
        assertEquals(EventStatus.UNASSIGNED, event.getStatus());
        assertNull(database.text("SELECT room_id FROM events WHERE event_id = 3"));
    }

    @Test
    @DisplayName("[UC-2 actors] Event Staff can view the event list")
    void uc2_staffCanViewEvents() {
        app.auth.logout();
        app.loginAsStaff();
        assertEquals(9, app.events.listEvents().size());
    }
}
