package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eventforge.model.Registration;
import eventforge.model.RegistrationStatus;
import eventforge.testsupport.TestDatabase;
import eventforge.testsupport.TestServices;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for event registration, waitlist and cancellation (UC-5, UC-5a, UC-6, NFR-4(3);
 * Matrix #13–#15). Event 4 (Sketch Workshop) is the empty 10-seat event; event 9 (Cosplay
 * Photoshoot) is full with attendees 9 and 10 waitlisted; attendee 1 is confirmed for event 2.
 */
class RegistrationServiceTest {
    private static final int EMPTY_EVENT = 4;
    private static final int FULL_EVENT = 9;

    @TempDir Path folder;
    private TestServices app;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        database = TestDatabase.withTestData(folder);
        app = new TestServices(database);
    }

    private int confirmed(int eventId) {
        return database.count("SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'",
                eventId);
    }

    private String statusOf(int attendeeId, int eventId) {
        return database.text("SELECT status FROM registrations WHERE attendee_id = ? AND event_id = ?",
                attendeeId, eventId);
    }

    /** Fills seats directly in SQL for attendees first..last, so a test starts with a nearly full event. */
    private void confirmSeats(int eventId, int first, int last) {
        for (int id = first; id <= last; id++) {
            database.exec("INSERT INTO registrations (attendee_id, event_id, status, registered_at) "
                    + "VALUES (?, ?, 'CONFIRMED', '2026-09-05T09:00:00')", id, eventId);
        }
    }

    private RegistrationResult registerAs(int attendeeId, int eventId) {
        app.auth.logout();
        app.loginAsAttendee(attendeeId);
        return app.registrations.register(eventId);
    }

    @Test
    @DisplayName("[UC-5 main flow] Attendee registers for an event with seats available (Matrix #13)")
    void matrix13_registerWithSeatsAvailable() {
        app.loginAsAttendee(11);
        assertEquals(10, app.registrations.remainingSeats(EMPTY_EVENT));
        assertEquals(RegistrationResult.CONFIRMED, app.registrations.register(EMPTY_EVENT));
        assertEquals("CONFIRMED", statusOf(11, EMPTY_EVENT));
        assertEquals(9, app.registrations.remainingSeats(EMPTY_EVENT));
    }

    @Test
    @DisplayName("[NFR-4(3)] Confirmed registrations never exceed event capacity (Matrix #14)")
    void matrix14_capacityNeverExceeded() {
        for (int id = 11; id <= 20; id++) {
            assertEquals(RegistrationResult.CONFIRMED, registerAs(id, EMPTY_EVENT), "attendee " + id);
        }
        for (int id = 1; id <= 5; id++) {
            assertEquals(RegistrationResult.FULL, registerAs(id, EMPTY_EVENT), "attendee " + id);
        }
        assertEquals(10, confirmed(EMPTY_EVENT));
        assertEquals(10, database.count("SELECT COUNT(*) FROM registrations WHERE event_id = ?", EMPTY_EVENT));
    }

    @Test
    @DisplayName("[UC-5 boundary] The last seat can be taken; the next request is told the event is full (supports Matrix #14)")
    void uc5Boundary_lastSeat() {
        confirmSeats(EMPTY_EVENT, 11, 19);                       // 9 of 10 seats taken
        assertEquals(RegistrationResult.CONFIRMED, registerAs(20, EMPTY_EVENT));
        assertEquals(0, app.registrations.remainingSeats(EMPTY_EVENT));
        assertEquals(RegistrationResult.FULL, registerAs(2, EMPTY_EVENT));
        assertEquals(10, confirmed(EMPTY_EVENT));
    }

    @Test
    @DisplayName("[UC-5 alt 2a] Registering twice for the same event is refused")
    void uc5Alt2a_alreadyRegistered() {
        assertEquals(RegistrationResult.ALREADY_REGISTERED, registerAs(1, 2));
        assertEquals(1, database.count("SELECT COUNT(*) FROM registrations WHERE attendee_id = 1 AND event_id = 2"));
        assertEquals(RegistrationResult.ALREADY_REGISTERED, registerAs(9, FULL_EVENT));   // waitlisted counts too
    }

    @Test
    @DisplayName("[UC-5a] Attendee joins the waitlist of a full event")
    void uc5a_joinWaitlist() {
        app.loginAsAttendee(11);
        assertEquals(RegistrationResult.FULL, app.registrations.register(FULL_EVENT));
        Registration entry = app.registrations.joinWaitlist(FULL_EVENT);

        assertEquals(RegistrationStatus.WAITLISTED, entry.getStatus());
        assertEquals("WAITLISTED", statusOf(11, FULL_EVENT));
        assertEquals(8, confirmed(FULL_EVENT));
    }

    @Test
    @DisplayName("[UC-5a] Waitlist is refused while seats are free")
    void uc5a_waitlistRefusedWithFreeSeats() {
        app.loginAsAttendee(11);
        assertThrows(BusinessRuleException.class, () -> app.registrations.joinWaitlist(EMPTY_EVENT));
        assertEquals(0, database.count("SELECT COUNT(*) FROM registrations WHERE event_id = ?", EMPTY_EVENT));
    }

    @Test
    @DisplayName("[UC-5 precondition] Registration for an unscheduled event is refused")
    void uc5_unscheduledEventRefused() {
        app.loginAsAttendee(11);
        assertThrows(BusinessRuleException.class, () -> app.registrations.register(5));
    }

    @Test
    @DisplayName("[UC-6 main flow] Attendee cancels a confirmed registration after confirmation (Matrix #15)")
    void matrix15_cancelRegistration() {
        app.loginAsAttendee(1);
        assertEquals(24, app.registrations.remainingSeats(2));
        app.registrations.cancelRegistration(2);
        assertEquals(0, database.count("SELECT COUNT(*) FROM registrations WHERE attendee_id = 1 AND event_id = 2"));
        assertEquals(25, app.registrations.remainingSeats(2));
    }

    @Test
    @DisplayName("[UC-6 step 5] Cancelling a seat promotes the first waitlisted attendee (supports Matrix #15)")
    void uc6Step5_cancelPromotesFirstWaitlisted() {
        app.loginAsAttendee(1);
        app.registrations.cancelRegistration(FULL_EVENT);

        assertEquals("CONFIRMED", statusOf(9, FULL_EVENT));
        assertEquals(1, database.count("SELECT promotion_notice FROM registrations WHERE attendee_id = 9 AND event_id = 9"));
        assertEquals("WAITLISTED", statusOf(10, FULL_EVENT));
        assertEquals(8, confirmed(FULL_EVENT));
    }

    @Test
    @DisplayName("[UC-6 step 5; Design §6.3] Promoted attendee sees the notice once")
    void uc6_promotionNoticeShownOnce() {
        app.loginAsAttendee(1);
        app.registrations.cancelRegistration(FULL_EVENT);

        app.auth.logout();
        app.loginAsAttendee(9);
        List<Registration> notices = app.registrations.takePromotionNotices();
        assertEquals(List.of(FULL_EVENT), notices.stream().map(Registration::getEventId).toList());
        assertTrue(app.registrations.takePromotionNotices().isEmpty());
    }

    @Test
    @DisplayName("[UC-6] Leaving the waitlist frees no seat and promotes nobody")
    void uc6_cancelWaitlistEntry() {
        app.loginAsAttendee(10);
        app.registrations.cancelRegistration(FULL_EVENT);
        assertEquals(8, confirmed(FULL_EVENT));
        assertEquals("WAITLISTED", statusOf(9, FULL_EVENT));
    }

    @Test
    @DisplayName("[UC-6 precondition] Cancelling with no registration is refused")
    void uc6_nothingToCancel() {
        app.loginAsAttendee(11);
        assertThrows(BusinessRuleException.class, () -> app.registrations.cancelRegistration(2));
    }

    @Test
    @DisplayName("[NFR-5] Attendee sees only their own registrations")
    void nfr5_ownRegistrationsOnly() {
        app.loginAsAttendee(1);
        List<Registration> mine = app.registrations.myRegistrations();
        assertEquals(2, mine.size());
        assertTrue(mine.stream().allMatch(r -> r.getAttendeeId() == 1));
    }

    @Test
    @DisplayName("[NFR-5] Registration needs an attendee session; a staff login is not one")
    void nfr5_registrationNeedsAttendeeSession() {
        assertThrows(AccessDeniedException.class, () -> app.registrations.register(EMPTY_EVENT));
        app.loginAsStaff();
        assertThrows(AccessDeniedException.class, () -> app.registrations.register(EMPTY_EVENT));
    }

    @Test
    @DisplayName("[UC-5 NFR note] Two attendees racing for the last seat: exactly one gets it")
    void uc5_concurrentLastSeat() throws Exception {
        confirmSeats(EMPTY_EVENT, 11, 19);                       // one seat left
        TestServices first = new TestServices(database);         // two separate sessions
        TestServices second = new TestServices(database);
        first.loginAsAttendee(20);
        second.loginAsAttendee(2);

        CountDownLatch start = new CountDownLatch(1);
        List<Callable<RegistrationResult>> racers = new ArrayList<>();
        for (TestServices session : List.of(first, second)) {
            racers.add(() -> {
                start.await();
                return session.registrations.register(EMPTY_EVENT);
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<RegistrationResult>> futures = new ArrayList<>();
            for (Callable<RegistrationResult> racer : racers) {
                futures.add(pool.submit(racer));
            }
            start.countDown();
            List<RegistrationResult> results = new ArrayList<>();
            for (Future<RegistrationResult> f : futures) {
                results.add(f.get());
            }
            assertTrue(results.contains(RegistrationResult.CONFIRMED), results.toString());
            assertTrue(results.contains(RegistrationResult.FULL), results.toString());
        } finally {
            pool.shutdownNow();
        }
        assertEquals(10, confirmed(EMPTY_EVENT));
    }
}
