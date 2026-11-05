package eventforge.model;

/** Lifecycle state of an {@link Event}: it is UNASSIGNED until a room is assigned, then SCHEDULED. */
public enum EventStatus {
    UNASSIGNED,
    SCHEDULED
}