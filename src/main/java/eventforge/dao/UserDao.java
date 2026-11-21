package eventforge.dao;

import eventforge.model.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Reads and writes the users table: staff accounts that log in (NFR-5). */
public class UserDao implements Dao<User> {

    @Override
    public Optional<User> findById(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<User> findAll(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public User insert(Connection connection, User entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void update(Connection connection, User entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the account with this username, or empty. Used by AuthService.login. */
    public Optional<User> findByUsername(Connection connection, String username) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
