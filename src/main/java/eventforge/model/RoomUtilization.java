package eventforge.model;

import java.util.List;

/** One row of the Room Utilization report (UC-8): how much each room is used. */
public record RoomUtilization(int roomId, String roomName, int capacity,
                              int scheduledEvents, int scheduledMinutes, int confirmedSeats) {

    /** Column headings used for the on-screen table and the CSV export. */
    public static List<String> csvHeader() {
        return List.of("Room ID", "Room", "Capacity", "Events", "Minutes Scheduled", "Confirmed Seats");
    }

    /** This row's values in the same order as csvHeader(). */
    public List<String> toCsvRow() {
        // TODO P8: return the fields as text, in csvHeader() order.
        throw new UnsupportedOperationException("Not implemented");
    }
}
