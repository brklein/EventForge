package eventforge.testsupport;

import eventforge.dao.AttendeeDao;
import eventforge.dao.DatabaseManager;
import eventforge.dao.EventDao;
import eventforge.dao.RegistrationDao;
import eventforge.dao.ReportDao;
import eventforge.dao.RoomDao;
import eventforge.dao.UserDao;
import eventforge.service.AttendeeService;
import eventforge.service.AuthService;
import eventforge.service.CheckInService;
import eventforge.service.CsvExporter;
import eventforge.service.EventService;
import eventforge.service.PasswordHasher;
import eventforge.service.RegistrationService;
import eventforge.service.ReportService;
import eventforge.service.RoomService;
import eventforge.service.SchedulingRules;

/**
 * Test scaffolding: wires every DAO and service to one test database by constructor injection, the same way
 * App will (Design §4.2). Each instance has its own AuthService, so it is one user's session.
 */
public final class TestServices {
    /** Passwords of the two staff accounts in test-data.sql. */
    public static final String ORGANIZER_PASSWORD = "Organizer#2026";
    public static final String STAFF_PASSWORD = "Staff#2026";

    public final TestDatabase database;
    public final DatabaseManager db;
    public final PasswordHasher hasher = new PasswordHasher();
    public final SchedulingRules rules = new SchedulingRules();
    public final CsvExporter csv = new CsvExporter();
    public final AuthService auth;
    public final AttendeeService attendees;
    public final RoomService rooms;
    public final EventService events;
    public final RegistrationService registrations;
    public final CheckInService checkIn;
    public final ReportService reports;

    public TestServices(TestDatabase database) {
        this.database = database;
        this.db = database.manager();
        UserDao userDao = new UserDao();
        AttendeeDao attendeeDao = new AttendeeDao();
        RoomDao roomDao = new RoomDao();
        EventDao eventDao = new EventDao();
        RegistrationDao registrationDao = new RegistrationDao();
        this.auth = new AuthService(db, userDao, attendeeDao, hasher);
        this.attendees = new AttendeeService(db, attendeeDao, auth);
        this.rooms = new RoomService(db, roomDao, eventDao, auth);
        this.events = new EventService(db, eventDao, roomDao, rules, auth);
        this.registrations = new RegistrationService(db, registrationDao, eventDao, rules, auth);
        this.checkIn = new CheckInService(db, attendeeDao, registrationDao, auth);
        this.reports = new ReportService(db, new ReportDao(), csv, auth);
    }

    public void loginAsOrganizer() {
        auth.login("organizer", ORGANIZER_PASSWORD);
    }

    public void loginAsStaff() {
        auth.login("staff", STAFF_PASSWORD);
    }

    /** Starts a self-service session for an attendee in the test data, using their stored email. */
    public void loginAsAttendee(int attendeeId) {
        String email = database.text("SELECT email FROM attendees WHERE attendee_id = ?", attendeeId);
        auth.loginAttendee(attendeeId, email);
    }
}
