package eventforge.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eventforge.testsupport.TestDatabase;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for connections, schema creation and transactions (Design §5.2, NFR-3). */
class DatabaseManagerTest {
    @TempDir Path folder;

    private static int queryInt(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            return rows.next() ? rows.getInt(1) : 0;
        }
    }

    @Test
    @DisplayName("[Design §5.1] First run creates the six tables; a second run is harmless")
    void createSchemaOnFirstRun() throws Exception {
        DatabaseManager db = new DatabaseManager("jdbc:sqlite:" + folder.resolve("new.db"));
        db.createSchema();
        assertDoesNotThrow(db::createSchema);
        try (Connection connection = db.openConnection()) {
            assertEquals(6, queryInt(connection, "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                    + "AND name IN ('conventions','users','attendees','rooms','events','registrations')"));
            assertEquals(6, queryInt(connection, "SELECT COUNT(*) FROM sqlite_master WHERE type = 'trigger'"));
        }
    }

    @Test
    @DisplayName("[Design §5.2] Connections have foreign keys on and auto-commit off")
    void connectionSettings() throws Exception {
        DatabaseManager db = TestDatabase.empty(folder).manager();
        try (Connection connection = db.openConnection()) {
            assertEquals(1, queryInt(connection, "PRAGMA foreign_keys"));
            assertFalse(connection.getAutoCommit());
        }
    }

    @Test
    @DisplayName("[NFR-3] Committed work is still there when a new connection opens")
    void committedWorkPersists() {
        TestDatabase database = TestDatabase.empty(folder);
        database.manager().inTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                return statement.executeUpdate("INSERT INTO rooms (name, capacity) VALUES ('Room Z', 5)");
            }
        });
        assertEquals(1, database.count("SELECT COUNT(*) FROM rooms"));
    }

    @Test
    @DisplayName("[NFR-3] An exception rolls the whole transaction back")
    void exceptionRollsBack() {
        TestDatabase database = TestDatabase.empty(folder);
        // Expect the exact exception thrown below, so a stub's UnsupportedOperationException cannot pass this test.
        assertThrows(IllegalStateException.class, () -> database.manager().inTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO rooms (name, capacity) VALUES ('Room Z', 5)");
            }
            throw new IllegalStateException("simulated failure after the insert");
        }));
        assertEquals(0, database.count("SELECT COUNT(*) FROM rooms"));
    }
}
