package eventforge.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Attendee;
import eventforge.model.BadgeType;
import eventforge.testsupport.TestDatabase;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** DAO tests against a temporary SQLite database (Design §3.1). */
class AttendeeDaoTest {
    @TempDir Path folder;
    private TestDatabase database;
    private final AttendeeDao dao = new AttendeeDao();

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
    }

    @Test
    @DisplayName("[UC-1 step 5] Insert assigns the generated ID; findById reads the same values back")
    void insertAndFind() throws Exception {
        try (Connection connection = database.connect()) {
            Attendee saved = dao.insert(connection, new Attendee(1, "Jane", "Doe", "jane@example.com", null,
                    BadgeType.GENERAL));
            assertEquals(21, saved.getAttendeeId());
            Attendee loaded = dao.findById(connection, 21).orElseThrow();
            assertEquals("Jane", loaded.getFirstName());
            assertEquals(BadgeType.GENERAL, loaded.getBadgeType());
            assertTrue(dao.findById(connection, 999).isEmpty());
        }
    }

    @Test
    @DisplayName("[UC-7 step 1] Search matches name starts, ignoring case, ordered by last name")
    void searchByName() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(List.of(1), dao.search(connection, "RIV").stream().map(Attendee::getAttendeeId).toList());
            // "ja" matches the start of Jamie (first name) only; Jordan and Johnson start with "Jo".
            assertEquals(List.of("Moore"), dao.search(connection, "ja").stream().map(Attendee::getLastName).toList());
        }
    }

    @Test
    @DisplayName("[UC-1 alt 5a] Possible duplicates match full name or email, ignoring case")
    void possibleDuplicates() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(1, dao.findPossibleDuplicates(connection, "alex", "RIVERA", "other@example.com").size());
            assertEquals(1, dao.findPossibleDuplicates(connection, "X", "Y", "Sam.Lee@example.com").size());
            assertTrue(dao.findPossibleDuplicates(connection, "Alex", "Lee", "new@example.com").isEmpty());
        }
    }
}
