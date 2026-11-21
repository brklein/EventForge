package eventforge.service;

import eventforge.dao.DatabaseManager;
import eventforge.dao.EventDao;
import eventforge.dao.RoomDao;
import eventforge.model.Room;
import java.util.List;

/** Room management (UC-3). Organizer only. Capacity changes apply to the next capacity check immediately (NFR-4). */
public class RoomService {
    private final DatabaseManager db;
    private final RoomDao roomDao;
    private final EventDao eventDao;
    private final AuthService auth;

    public RoomService(DatabaseManager db, RoomDao roomDao, EventDao eventDao, AuthService auth) {
        this.db = db;
        this.roomDao = roomDao;
        this.eventDao = eventDao;
        this.auth = auth;
    }

    /** Returns all rooms for the Room Management list (UC-3 step 2). */
    public List<Room> listRooms() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Adds a room after checking the name is not blank and the capacity is positive (UC-3 steps 3–5). */
    public Room addRoom(String name, int capacity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Changes a room's name or capacity. Throws BusinessRuleException if the new capacity is below the
     * maximum capacity of an event already assigned to the room (NFR-4(1)).
     */
    public Room updateRoom(int roomId, String name, int capacity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Deletes a room. Throws RoomInUseException listing the assigned events if there are any (UC-3 alt 5a). */
    public void removeRoom(int roomId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
