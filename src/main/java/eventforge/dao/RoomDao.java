package eventforge.dao;

import eventforge.model.Room;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Reads and writes the rooms table (UC-3). */
public class RoomDao implements Dao<Room> {

    @Override
    public Optional<Room> findById(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Room> findAll(Connection connection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Room insert(Connection connection, Room entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void update(Connection connection, Room entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(Connection connection, int id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
