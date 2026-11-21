package eventforge.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Basic operations shared by every DAO (Design Figure 3). Each method takes the caller's connection so a
 * service can combine several DAO calls in one transaction. All SQL uses PreparedStatements.
 */
public interface Dao<T> {

    /** Returns the record with this ID, or empty if there is none. */
    Optional<T> findById(Connection connection, int id);

    /** Returns every record, in a stable order suited to on-screen lists. */
    List<T> findAll(Connection connection);

    /** Inserts a new record, sets the generated ID on it and returns it. */
    T insert(Connection connection, T entity);

    /** Saves changes to an existing record, matched by its ID. */
    void update(Connection connection, T entity);

    /** Deletes the record with this ID. Foreign keys (ON DELETE RESTRICT) stop deletes of records still in use. */
    void delete(Connection connection, int id);
}
