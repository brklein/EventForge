package eventforge.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.RegistrationStatus;
import eventforge.testsupport.TestDatabase;
import java.nio.file.Path;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** DAO tests for seat counts and the waitlist order (UC-5, UC-6). */
class RegistrationDaoTest {
    @TempDir Path folder;
    private TestDatabase database;
    private final RegistrationDao dao = new RegistrationDao();

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
    }

    @Test
    @DisplayName("[UC-5 step 3] Confirmed count ignores waitlist entries")
    void countConfirmed() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(8, dao.countConfirmed(connection, 9));
            assertEquals(0, dao.countConfirmed(connection, 4));
        }
    }

    @Test
    @DisplayName("[UC-6 step 5] First waitlisted is the oldest entry")
    void firstWaitlisted() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(9, dao.findFirstWaitlisted(connection, 9).orElseThrow().getAttendeeId());
            assertTrue(dao.findFirstWaitlisted(connection, 2).isEmpty());
        }
    }

    @Test
    @DisplayName("[UC-5 alt 2a] Finds an attendee's existing registration for an event")
    void findByAttendeeAndEvent() throws Exception {
        try (Connection connection = database.connect()) {
            assertEquals(RegistrationStatus.CONFIRMED,
                    dao.findByAttendeeAndEvent(connection, 1, 2).orElseThrow().getStatus());
            assertTrue(dao.findByAttendeeAndEvent(connection, 11, 2).isEmpty());
        }
    }
}
