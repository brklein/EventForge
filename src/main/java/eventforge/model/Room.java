package eventforge.model;
 
/** A physical room that events can be assigned to (UC-3, UC-4). */
public class Room {
    private int roomId;                // 0 until saved to the database
    private final String name;
    private final int capacity;
 
    /** Creates a new room that has not been saved yet. */
    public Room(String name, int capacity) {
        this(0, name, capacity);
    }
 
    /** Creates a room loaded from the database. */
    public Room(int roomId, String name, int capacity) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
    }
 
    public int getRoomId() { return roomId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
 
    /** Called by the DAO after the database assigns the generated ID. */
    public void setRoomId(int roomId) { this.roomId = roomId; }
}
 