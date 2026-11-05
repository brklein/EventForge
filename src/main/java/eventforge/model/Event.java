package eventforge.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** A convention event (UC-2). roomId is null while the event is UNASSIGNED; see UC-4 for assignment rules. */
public class Event {
    private int eventId;               // 0 until saved to the database
    private final int conventionId;
    private final String name;
    private final String description;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int maxCapacity;
    private final EventStatus status;
    private final Integer roomId;      // null = no room assigned

    /** Creates a new, unassigned event that has not been saved yet. */
    public Event(int conventionId, String name, String description, LocalDate date,
                 LocalTime startTime, LocalTime endTime, int maxCapacity) {
        this(0, conventionId, name, description, date, startTime, endTime,
             maxCapacity, EventStatus.UNASSIGNED, null);
    }

    /** Creates an event loaded from the database. */
    public Event(int eventId, int conventionId, String name, String description, LocalDate date,
                 LocalTime startTime, LocalTime endTime, int maxCapacity,
                 EventStatus status, Integer roomId) {
        this.eventId = eventId;
        this.conventionId = conventionId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.status = status;
        this.roomId = roomId;
    }

    public int getEventId() { return eventId; }
    public int getConventionId() { return conventionId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getMaxCapacity() { return maxCapacity; }
    public EventStatus getStatus() { return status; }
    public Integer getRoomId() { return roomId; }

    /** Called by the DAO after the database assigns the generated ID. */
    public void setEventId(int eventId) { this.eventId = eventId; }
}