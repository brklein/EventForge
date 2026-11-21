package eventforge.testsupport;

import eventforge.dao.DatabaseManager;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test scaffolding: a fresh SQLite file in a JUnit temporary folder, loaded with schema.sql and test-data.sql
 * (Design §3.1: "a fresh temporary SQLite database per test"). It loads the scripts itself with plain JDBC, so
 * tests can set up data before DatabaseManager is implemented. A file is used rather than :memory: because
 * every DatabaseManager connection to :memory: would see a different, empty database.
 */
public final class TestDatabase {
    public static final String SCHEMA = "/db/schema.sql";
    public static final String TEST_DATA = "/db/test-data.sql";

    private final String jdbcUrl;

    private TestDatabase(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /** Creates the schema only (no rows) in folder/test.db. */
    public static TestDatabase empty(Path folder) {
        return create(folder, false);
    }

    /** Creates the schema and loads the P7 test data in folder/test.db. */
    public static TestDatabase withTestData(Path folder) {
        return create(folder, true);
    }

    private static TestDatabase create(Path folder, boolean loadData) {
        TestDatabase database = new TestDatabase("jdbc:sqlite:" + folder.resolve("test.db"));
        try (Connection connection = database.connect()) {
            SqlScript.run(connection, SCHEMA);
            if (loadData) {
                SqlScript.run(connection, TEST_DATA);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not build the test database", e);
        }
        return database;
    }

    public String getJdbcUrl() { return jdbcUrl; }

    /** A DatabaseManager pointed at this database, as the application would build it. */
    public DatabaseManager manager() {
        return new DatabaseManager(jdbcUrl);
    }

    /** A raw connection with foreign keys on and auto-commit on, for checking results directly in SQL. */
    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /** Runs one INSERT, UPDATE or DELETE with parameters. */
    public void exec(String sql, Object... params) {
        try (Connection connection = connect(); PreparedStatement statement = prepare(connection, sql, params)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    /** Runs a query that returns one number, such as SELECT COUNT(*). */
    public int count(String sql, Object... params) {
        try (Connection connection = connect(); PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    /** Runs a query that returns one text value, or null if there is no row. */
    public String text(String sql, Object... params) {
        try (Connection connection = connect(); PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getString(1) : null;
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... params)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
