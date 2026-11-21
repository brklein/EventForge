package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Event;
import eventforge.model.EventStatus;
import eventforge.model.Room;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for the capacity and overlap rules (UC-2, UC-4, NFR-4). No database needed. */
class SchedulingRulesTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 9);
    private final SchedulingRules rules = new SchedulingRules();

    private static LocalTime t(String hhmm) { return LocalTime.parse(hhmm); }

    private static Event event(int id, String name, LocalDate date, String start, String end, int capacity,
                               Integer roomId) {
        EventStatus status = roomId == null ? EventStatus.UNASSIGNED : EventStatus.SCHEDULED;
        return new Event(id, 1, name, "", date, t(start), t(end), capacity, status, roomId);
    }

    @Test
    @DisplayName("[UC-2 alt 4a] End time before start time is rejected (Matrix #6)")
    void uc2Alt4a_endBeforeStartRejected() {
        assertThrows(ValidationException.class, () -> rules.checkTimeRange(t("11:00"), t("10:00")));
    }

    @Test
    @DisplayName("[UC-2 boundary] End time equal to start time is rejected")
    void uc2Boundary_zeroLengthEventRejected() {
        assertThrows(ValidationException.class, () -> rules.checkTimeRange(t("10:00"), t("10:00")));
    }

    @Test
    @DisplayName("[UC-2 main flow] End time after start time is accepted")
    void uc2_validTimeRangeAccepted() {
        assertDoesNotThrow(() -> rules.checkTimeRange(t("10:00"), t("11:00")));
    }

    @Test
    @DisplayName("[UC-4 alt 4a] Room smaller than the event is rejected and its capacity shown (Matrix #11)")
    void uc4Alt4a_roomTooSmallRejected() {
        Room roomB = new Room(2, "Room B", 30);
        Event big = event(7, "Mega Fan Meetup", DAY, "14:00", "16:00", 50, null);
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkRoomCapacity(roomB, big));
        assertTrue(e.getMessage().contains("30"), "message should state the room's capacity: " + e.getMessage());
    }

    @Test
    @DisplayName("[UC-4 boundary] Room capacity equal to event capacity is accepted (supports Matrix #11)")
    void uc4Boundary_equalCapacityAccepted() {
        Room roomB = new Room(2, "Room B", 30);
        Event exact = event(8, "Artist Alley Tour", DAY, "10:00", "11:00", 30, null);
        assertDoesNotThrow(() -> rules.checkRoomCapacity(roomB, exact));
    }

    @Test
    @DisplayName("[UC-4 step 5] Partly overlapping, contained and identical time ranges overlap")
    void uc4_overlappingRangesDetected() {
        assertTrue(rules.overlaps(t("10:00"), t("11:00"), t("10:30"), t("11:30")));   // partial
        assertTrue(rules.overlaps(t("10:00"), t("12:00"), t("10:30"), t("11:00")));   // contained
        assertTrue(rules.overlaps(t("10:00"), t("11:00"), t("10:00"), t("11:00")));   // identical
        assertTrue(rules.overlaps(t("10:30"), t("11:30"), t("10:00"), t("11:00")));   // order reversed
    }

    @Test
    @DisplayName("[UC-4 boundary] Back-to-back time ranges do not overlap (supports Matrix #12)")
    void uc4Boundary_backToBackDoesNotOverlap() {
        assertFalse(rules.overlaps(t("10:00"), t("11:00"), t("11:00"), t("12:00")));
        assertFalse(rules.overlaps(t("11:00"), t("12:00"), t("10:00"), t("11:00")));
    }

    @Test
    @DisplayName("[UC-4 alt 5a] Overlap check names the conflicting event (Matrix #12)")
    void uc4Alt5a_conflictingEventNamed() {
        Event eventA = event(2, "Prop Making 101", DAY, "10:00", "11:00", 25, 2);
        Event eventC = event(5, "Panel: Indie Comics", DAY, "10:30", "11:30", 25, null);
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkNoOverlap(eventC, List.of(eventA)));
        assertTrue(e.getMessage().contains("Prop Making 101"), e.getMessage());
    }

    @Test
    @DisplayName("[UC-4 step 5] Same times on a different date do not conflict")
    void uc4_differentDateDoesNotConflict() {
        Event eventA = event(2, "Prop Making 101", DAY, "10:00", "11:00", 25, 2);
        Event nextDay = event(5, "Panel: Indie Comics", DAY.plusDays(1), "10:00", "11:00", 25, null);
        assertDoesNotThrow(() -> rules.checkNoOverlap(nextDay, List.of(eventA)));
    }

    @Test
    @DisplayName("[UC-4 step 5] An event does not conflict with itself when rescheduled")
    void uc4_eventIgnoresItself() {
        Event eventA = event(2, "Prop Making 101", DAY, "10:00", "11:00", 25, 2);
        assertDoesNotThrow(() -> rules.checkNoOverlap(eventA, List.of(eventA)));
    }

    @Test
    @DisplayName("[NFR-4(3) boundary] A seat is free below capacity and not at capacity")
    void nfr4_hasSeatAtBoundary() {
        assertTrue(rules.hasSeat(9, 10));
        assertFalse(rules.hasSeat(10, 10));
        assertFalse(rules.hasSeat(11, 10));
    }
}
