package eventforge.db;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.testsupport.TestDatabase;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * NFR-4 database-layer safeguards. Each test bypasses the application and writes raw SQL, so these
 * test schema.sql itself. Because the schema is complete in P7, these tests are expected to pass already.
 */
class SchemaIntegrityTest {
    @TempDir Path folder;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
    }

    /** Runs one statement and returns the SQLite error it raised. */
    private SQLException rejected(String sql) {
        return assertThrows(SQLException.class, () -> {
            try (Connection connection = database.connect(); Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        });
    }

    private void accepted(String sql) {
        assertDoesNotThrow(() -> {
            try (Connection connection = database.connect(); Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        });
    }

    @Test
    @DisplayName("[NFR-4(3)] Trigger blocks a confirmed seat beyond capacity (supports Matrix #14)")
    void nfr4_registrationBeyondCapacity() {
        SQLException e = rejected("INSERT INTO registrations (attendee_id, event_id, status, registered_at) "
                + "VALUES (11, 9, 'CONFIRMED', '2026-09-10T09:00:00')");
        assertTrue(e.getMessage().contains("Event is full"), e.getMessage());
        assertEquals(8, database.count("SELECT COUNT(*) FROM registrations WHERE event_id = 9 AND status = 'CONFIRMED'"));
    }

    @Test
    @DisplayName("[NFR-4(3)] Trigger blocks promoting a waitlist entry into a full event (supports Matrix #14)")
    void nfr4_promotionBeyondCapacity() {
        rejected("UPDATE registrations SET status = 'CONFIRMED' WHERE registration_id = 9");
    }

    @Test
    @DisplayName("[NFR-4(1)] Trigger blocks assigning an event to a room that is too small (supports Matrix #11)")
    void nfr4_roomTooSmall() {
        SQLException e = rejected("UPDATE events SET room_id = 2, status = 'SCHEDULED' WHERE event_id = 7");
        assertTrue(e.getMessage().contains("Room capacity"), e.getMessage());
    }

    @Test
    @DisplayName("[NFR-4(2)] Trigger blocks overlapping events in one room; back-to-back is allowed (supports Matrix #12)")
    void nfr4_overlap() {
        SQLException e = rejected("UPDATE events SET room_id = 2, status = 'SCHEDULED' WHERE event_id = 5");
        assertTrue(e.getMessage().contains("overlaps"), e.getMessage());
        accepted("UPDATE events SET room_id = 2, status = 'SCHEDULED' WHERE event_id = 6");
    }

    @Test
    @DisplayName("[NFR-4(1)] Trigger blocks shrinking a room below an assigned event's capacity")
    void nfr4_roomShrink() {
        rejected("UPDATE rooms SET capacity = 20 WHERE room_id = 2");
    }

    @Test
    @DisplayName("[NFR-4(3)] Trigger blocks lowering an event's capacity below its confirmed seats")
    void nfr4_eventCapacityShrink() {
        rejected("UPDATE events SET max_capacity = 5 WHERE event_id = 9");
    }

    @Test
    @DisplayName("[UC-5 alt 2a] UNIQUE stops a second registration for the same attendee and event")
    void unique_registration() {
        rejected("INSERT INTO registrations (attendee_id, event_id, status, registered_at) "
                + "VALUES (1, 2, 'CONFIRMED', '2026-09-10T09:00:00')");
    }

    @Test
    @DisplayName("[UC-3 alt 5a] Foreign key stops deleting a room that has events")
    void foreignKey_roomInUse() {
        rejected("DELETE FROM rooms WHERE room_id = 2");
        accepted("DELETE FROM rooms WHERE room_id = 4");
    }

    @Test
    @DisplayName("[UC-2 alt 4a; UC-3 step 4] CHECK constraints reject bad times, capacities and states")
    void checkConstraints() {
        rejected("INSERT INTO events (convention_id, name, event_date, start_time, end_time, max_capacity) "
                + "VALUES (1, 'Bad Times', '2026-10-10', '11:00', '10:00', 10)");
        rejected("INSERT INTO events (convention_id, name, event_date, start_time, end_time, max_capacity) "
                + "VALUES (1, 'No Seats', '2026-10-10', '10:00', '11:00', 0)");
        rejected("INSERT INTO rooms (name, capacity) VALUES ('Closet', 0)");
        rejected("UPDATE events SET status = 'SCHEDULED' WHERE event_id = 5");       // scheduled with no room
        rejected("UPDATE attendees SET badge_type = 'PLATINUM' WHERE attendee_id = 1");
    }
}
