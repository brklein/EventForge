package eventforge.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Event;
import eventforge.model.EventStatus;
import eventforge.testsupport.TestDatabase;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** DAO tests for event queries used by room assignment (UC-3, UC-4). */
class EventDaoTest {
    private static final LocalDate OCT_9 = LocalDate.of(2026, 10, 9);

    @TempDir Path folder;
    private TestDatabase database;
    private final EventDao dao = new EventDao();

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
    }

    @Test
    @DisplayName("[UC-4 step 5] Overlap query finds Event A, and ignores back-to-back times")
    void findOverlapping() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals("Prop Making 101", dao.findOverlapping(connection, 2, OCT_9,
                    LocalTime.of(10, 30), LocalTime.of(11, 30), 5).orElseThrow().getName());
            assertTrue(dao.findOverlapping(connection, 2, OCT_9,
                    LocalTime.of(11, 0), LocalTime.of(12, 0), 6).isEmpty());
            assertTrue(dao.findOverlapping(connection, 2, OCT_9,             // an event never conflicts with itself
                    LocalTime.of(10, 0), LocalTime.of(11, 0), 2).isEmpty());
        }
    }

    @Test
    @DisplayName("[UC-3 alt 5a] Events in a room are listed in date and time order")
    void findByRoom() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(List.of(2, 3), dao.findByRoom(connection, 2).stream().map(Event::getEventId).toList());
            assertTrue(dao.findByRoom(connection, 4).isEmpty());
        }
    }

    @Test
    @DisplayName("[UC-4 step 6] Assignment sets room and status together")
    void updateAssignment() throws Exception {
        try (Connection connection = database.connect()) {
            dao.updateAssignment(connection, 5, 4, EventStatus.SCHEDULED);
            Event event = dao.findById(connection, 5).orElseThrow();
            assertEquals(EventStatus.SCHEDULED, event.getStatus());
            assertEquals(4, event.getRoomId());
        }
    }

    @Test
    @DisplayName("[UC-5 step 1] Only scheduled events are offered for registration")
    void findScheduled() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(List.of(1, 2, 3, 4, 9),
                    dao.findScheduled(connection).stream().map(Event::getEventId).toList());
        }
    }
}
