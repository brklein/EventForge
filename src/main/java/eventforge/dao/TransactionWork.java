package eventforge.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** A unit of database work run inside one transaction by DatabaseManager (Design §5.2). */
@FunctionalInterface
public interface TransactionWork<T> {

    /** Does the work on the given connection. Throwing any exception rolls the transaction back. */
    T run(Connection connection) throws SQLException;
}
