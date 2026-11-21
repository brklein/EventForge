package eventforge.service;

import eventforge.model.Event;
import java.util.List;

/** Thrown when an Organizer tries to delete a room that still has events assigned (UC-3 alt 5a). */
public class RoomInUseException extends BusinessRuleException {
    private final List<Event> assignedEvents;

    /** Creates the error, carrying the events that block the deletion so the screen can list them. */
    public RoomInUseException(List<Event> assignedEvents) {
        super("This room cannot be removed while events are assigned to it.");
        this.assignedEvents = List.copyOf(assignedEvents);
    }

    public List<Event> getAssignedEvents() { return assignedEvents; }
}
