package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Role;
import eventforge.model.User;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for login, sessions and role checks (NFR-5; Matrix #1, #2). */
class AuthServiceTest {
    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
    }

    @Test
    @DisplayName("[NFR-5; Prop. §2] Organizer can authenticate and reach all management functions (Matrix #1)")
    void matrix1_organizerLoginReachesAllFunctions() {
        User user = app.auth.login("organizer", TestServices.ORGANIZER_PASSWORD);
        assertEquals(Role.ORGANIZER, user.getRole());
        assertEquals("organizer", app.auth.getCurrentUser().orElseThrow().getUsername());
        assertTrue(app.auth.permissionsFor(Role.ORGANIZER).containsAll(Set.of(
                Permission.CREATE_EVENT, Permission.MANAGE_ROOMS, Permission.ASSIGN_ROOM,
                Permission.SEARCH_EDIT_ATTENDEE, Permission.CHECK_IN, Permission.GENERATE_REPORT)));
    }

    @Test
    @DisplayName("[NFR-5; Prop. §2] Event Staff menu hides Organizer-only functions (Matrix #2)")
    void matrix2_staffPermissionsExcludeOrganizerFunctions() {
        Set<Permission> staff = app.auth.permissionsFor(Role.EVENT_STAFF);
        assertAll(
                () -> assertFalse(staff.contains(Permission.CREATE_EVENT)),
                () -> assertFalse(staff.contains(Permission.MANAGE_ROOMS)),
                () -> assertFalse(staff.contains(Permission.ASSIGN_ROOM)),
                () -> assertFalse(staff.contains(Permission.GENERATE_REPORT)),
                () -> assertTrue(staff.contains(Permission.CHECK_IN)),
                () -> assertTrue(staff.contains(Permission.SEARCH_EDIT_ATTENDEE)),
                () -> assertTrue(staff.contains(Permission.VIEW_EVENTS)));
    }

    @Test
    @DisplayName("[NFR-5; Prop. §2] Event Staff cannot perform Organizer-only actions; no database changes (Matrix #2)")
    void matrix2_staffCannotPerformOrganizerActions() {
        app.loginAsStaff();
        int events = database.count("SELECT COUNT(*) FROM events");
        int rooms = database.count("SELECT COUNT(*) FROM rooms");
        int scheduled = database.count("SELECT COUNT(*) FROM events WHERE status = 'SCHEDULED'");

        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> app.events.createEvent(1, "Staff Event", "",
                        LocalDate.of(2026, 10, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), 10)),
                () -> assertThrows(AccessDeniedException.class, () -> app.rooms.addRoom("Room Z", 20)),
                () -> assertThrows(AccessDeniedException.class, () -> app.rooms.updateRoom(4, "Room D", 50)),
                () -> assertThrows(AccessDeniedException.class, () -> app.rooms.removeRoom(4)),
                () -> assertThrows(AccessDeniedException.class, () -> app.events.assignRoom(8, 4)),
                () -> assertThrows(AccessDeniedException.class, () -> app.reports.eventAttendance(
                        LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11), null)));

        assertEquals(events, database.count("SELECT COUNT(*) FROM events"));
        assertEquals(rooms, database.count("SELECT COUNT(*) FROM rooms"));
        assertEquals(scheduled, database.count("SELECT COUNT(*) FROM events WHERE status = 'SCHEDULED'"));
    }

    @Test
    @DisplayName("[NFR-5] Wrong password and unknown username give the same error (supports Matrix #1)")
    void nfr5_badCredentialsRejected() {
        ValidationException wrongPassword = assertThrows(ValidationException.class,
                () -> app.auth.login("organizer", "not-the-password"));
        ValidationException unknownUser = assertThrows(ValidationException.class,
                () -> app.auth.login("nobody", TestServices.ORGANIZER_PASSWORD));
        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage());
        assertTrue(app.auth.getCurrentUser().isEmpty());
    }

    @Test
    @DisplayName("[NFR-5] New accounts store a salted hash, never the password (supports Matrix #1)")
    void nfr5_passwordStoredAsHash() {
        app.loginAsOrganizer();
        app.auth.createUser("newstaff", "Secret#123", Role.EVENT_STAFF);

        String hash = database.text("SELECT password_hash FROM users WHERE username = 'newstaff'");
        String salt = database.text("SELECT salt FROM users WHERE username = 'newstaff'");
        assertNotEquals("Secret#123", hash);
        assertEquals(32, salt.length());
        assertTrue(app.hasher.matches("Secret#123", salt, hash));
    }

    @Test
    @DisplayName("[NFR-5] Event Staff cannot create accounts")
    void nfr5_staffCannotCreateUsers() {
        app.loginAsStaff();
        assertThrows(AccessDeniedException.class,
                () -> app.auth.createUser("sneaky", "Secret#123", Role.ORGANIZER));
        assertEquals(2, database.count("SELECT COUNT(*) FROM users"));
    }

    @Test
    @DisplayName("[NFR-5] Role check fails when nobody is logged in")
    void nfr5_requireRoleWithoutLogin() {
        assertThrows(AccessDeniedException.class, () -> app.auth.requireRole(Role.ORGANIZER));
    }

    @Test
    @DisplayName("[NFR-5] Attendee self-service needs a matching attendee ID and email")
    void nfr5_attendeeLogin() {
        assertThrows(ValidationException.class, () -> app.auth.loginAttendee(1, "sam.lee@example.com"));
        app.auth.loginAttendee(1, "alex.rivera@example.com");
        assertEquals(1, app.auth.requireAttendee());
        assertThrows(AccessDeniedException.class, () -> app.auth.requireRole(Role.EVENT_STAFF));
    }

    @Test
    @DisplayName("[NFR-5] Logout ends the session")
    void nfr5_logoutEndsSession() {
        app.loginAsOrganizer();
        app.auth.logout();
        assertTrue(app.auth.getCurrentUser().isEmpty());
        assertThrows(AccessDeniedException.class, () -> app.auth.requireRole(Role.ORGANIZER));
    }
}
