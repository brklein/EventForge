package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Event;
import eventforge.model.Room;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for room management (UC-3; Matrix #8, #9). */
class RoomServiceTest {
    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
        app.loginAsOrganizer();
    }

    private int roomCount() {
        return database.count("SELECT COUNT(*) FROM rooms");
    }

    @Test
    @DisplayName("[UC-3 main flow] Organizer can add a room (Matrix #8)")
    void matrix8_addRoom() {
        // The matrix names 'Room B'; the test data already has one, so a new name is used here.
        Room added = app.rooms.addRoom("Room E", 30);
        assertTrue(added.getRoomId() > 0);
        assertTrue(app.rooms.listRooms().stream().anyMatch(r -> r.getName().equals("Room E")));
        assertEquals(30, database.count("SELECT capacity FROM rooms WHERE name = 'Room E'"));
    }

    @Test
    @DisplayName("[UC-3 step 4] Room with zero or negative capacity, or a blank name, is rejected")
    void uc3Step4_invalidRoomRejected() {
        assertEquals("capacity", assertThrows(ValidationException.class,
                () -> app.rooms.addRoom("Room E", 0)).getField());
        assertEquals("capacity", assertThrows(ValidationException.class,
                () -> app.rooms.addRoom("Room E", -1)).getField());
        assertEquals("name", assertThrows(ValidationException.class,
                () -> app.rooms.addRoom("  ", 20)).getField());
        assertEquals(4, roomCount());
    }

    @Test
    @DisplayName("[UC-3 alt 5a] Deleting a room with assigned events is blocked (Matrix #9)")
    void matrix9_deleteRoomInUseBlocked() {
        RoomInUseException e = assertThrows(RoomInUseException.class, () -> app.rooms.removeRoom(2));
        assertEquals(List.of("Prop Making 101", "Costume Contest Prep"),
                e.getAssignedEvents().stream().map(Event::getName).toList());
        assertEquals(4, roomCount());
    }

    @Test
    @DisplayName("[UC-3 boundary] Room with no events can be deleted")
    void uc3Boundary_deleteEmptyRoom() {
        app.rooms.removeRoom(4);
        assertEquals(0, database.count("SELECT COUNT(*) FROM rooms WHERE room_id = 4"));
    }

    @Test
    @DisplayName("[NFR-4(1)] Room capacity cannot drop below an assigned event's capacity")
    void nfr4_capacityBelowAssignedEventRejected() {
        // Room B (30) hosts Prop Making 101 (capacity 25).
        assertThrows(BusinessRuleException.class, () -> app.rooms.updateRoom(2, "Room B", 20));
        assertEquals(30, database.count("SELECT capacity FROM rooms WHERE room_id = 2"));
    }

    @Test
    @DisplayName("[UC-3 main flow] Room can be renamed and enlarged")
    void uc3_updateRoom() {
        app.rooms.updateRoom(2, "Room B (Main)", 40);
        assertEquals("Room B (Main)", database.text("SELECT name FROM rooms WHERE room_id = 2"));
        assertEquals(40, database.count("SELECT capacity FROM rooms WHERE room_id = 2"));
    }
}
