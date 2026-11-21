package eventforge.dao;

import java.sql.Connection;

/**
 * Opens connections to the SQLite file, creates the schema on first run and supplies the transaction helper
 * (Design §2.2, §5.2). Each service receives one through its constructor, so every test can point it at its
 * own temporary database (Design §4.2, constructor injection).
 */
public class DatabaseManager {
    /** Location of the schema script on the classpath (src/main/resources/db/schema.sql). */
    public static final String SCHEMA_RESOURCE = "/db/schema.sql";
    /** How long, in milliseconds, a second writer waits for the database lock before failing. */
    public static final int BUSY_TIMEOUT_MS = 5000;

    private final String jdbcUrl;      // for example jdbc:sqlite:eventforge.db

    /** Creates a manager for the database at the given JDBC URL. Does not open a connection yet. */
    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUrl() { return jdbcUrl; }

    /**
     * Opens a new connection with the settings every use case relies on:
     * PRAGMA foreign_keys = ON, PRAGMA synchronous = FULL, the busy timeout, and auto-commit off (NFR-3).
     */
    public Connection openConnection() {
        // TODO P8: DriverManager.getConnection(jdbcUrl); run the PRAGMAs; setAutoCommit(false).
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Runs schema.sql if the tables do not exist yet. Safe to call on every start (CREATE ... IF NOT EXISTS). */
    public void createSchema() {
        // TODO P8: read SCHEMA_RESOURCE, split into statements (triggers contain ';' inside BEGIN...END), run them.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Runs work in one transaction: commits once at the end, or rolls back on any exception (NFR-3).
     * Runtime exceptions from work are rethrown unchanged; SQLExceptions are rethrown wrapped in an unchecked
     * exception. The services turn trigger aborts into BusinessRuleException.
     */
    public <T> T inTransaction(TransactionWork<T> work) {
        // TODO P8: open connection, run work, commit; on exception roll back and rethrow; always close.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Same as inTransaction, but starts with BEGIN IMMEDIATE so the write lock is taken before the first read.
     * Used by event registration so the seat-count check and the insert are one atomic step (UC-5, Design §6.1).
     */
    public <T> T inImmediateTransaction(TransactionWork<T> work) {
        // TODO P8: as inTransaction, but execute "BEGIN IMMEDIATE" first (with auto-commit on, then off).
        throw new UnsupportedOperationException("Not implemented");
    }
}
