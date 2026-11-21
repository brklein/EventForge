-- EventForge database schema (Preliminary Design §5.1–5.2).
-- Creates the six tables, their indexes and the integrity triggers. DatabaseManager runs this on first start.
-- Conventions: INTEGER primary keys; dates as ISO-8601 text (YYYY-MM-DD); times as HH:MM; timestamps as
-- YYYY-MM-DDTHH:MM:SS. All of these sort and compare correctly as text.
-- Foreign keys are only enforced when each connection runs PRAGMA foreign_keys = ON (DatabaseManager does this).

-- A convention. Attendees and events belong to one convention (UC-1 and UC-2 preconditions).
CREATE TABLE IF NOT EXISTS conventions (
    convention_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT    NOT NULL CHECK (length(trim(name)) > 0),
    start_date     TEXT    NOT NULL,
    end_date       TEXT    NOT NULL,
    CHECK (end_date >= start_date)
);

-- Staff accounts that log in (NFR-5). Standalone: not linked to attendee data.
CREATE TABLE IF NOT EXISTS users (
    user_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    username       TEXT    NOT NULL UNIQUE,
    password_hash  TEXT    NOT NULL,                 -- PBKDF2 hash, hex encoded. Never the plain password.
    salt           TEXT    NOT NULL,                 -- random per-user salt, hex encoded
    role           TEXT    NOT NULL CHECK (role IN ('ORGANIZER', 'EVENT_STAFF'))
);

-- People registered for the convention (UC-1). checked_in_at is NULL until convention-level check-in (UC-7).
CREATE TABLE IF NOT EXISTS attendees (
    attendee_id    INTEGER PRIMARY KEY AUTOINCREMENT,  -- also printed on the badge (badge ID, UC-7)
    convention_id  INTEGER NOT NULL REFERENCES conventions (convention_id) ON DELETE RESTRICT,
    first_name     TEXT    NOT NULL CHECK (length(trim(first_name)) > 0),
    last_name      TEXT    NOT NULL CHECK (length(trim(last_name)) > 0),
    email          TEXT    NOT NULL CHECK (length(trim(email)) > 0),
    phone          TEXT,
    badge_type     TEXT    NOT NULL CHECK (badge_type IN ('GENERAL', 'VIP', 'VENDOR', 'GUEST')),
    checked_in_at  TEXT
);

-- Physical rooms (UC-3).
CREATE TABLE IF NOT EXISTS rooms (
    room_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT    NOT NULL CHECK (length(trim(name)) > 0),
    capacity       INTEGER NOT NULL CHECK (capacity > 0)
);

-- Convention events (UC-2). room_id is NULL while UNASSIGNED and set once SCHEDULED (UC-4).
CREATE TABLE IF NOT EXISTS events (
    event_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    convention_id  INTEGER NOT NULL REFERENCES conventions (convention_id) ON DELETE RESTRICT,
    name           TEXT    NOT NULL CHECK (length(trim(name)) > 0),
    description    TEXT,
    event_date     TEXT    NOT NULL,
    start_time     TEXT    NOT NULL,
    end_time       TEXT    NOT NULL,
    max_capacity   INTEGER NOT NULL CHECK (max_capacity > 0),
    status         TEXT    NOT NULL DEFAULT 'UNASSIGNED' CHECK (status IN ('UNASSIGNED', 'SCHEDULED')),
    room_id        INTEGER REFERENCES rooms (room_id) ON DELETE RESTRICT,
    CHECK (end_time > start_time),
    -- Status and room must agree: UNASSIGNED has no room, SCHEDULED has one (Design Figure 6a).
    CHECK ((status = 'UNASSIGNED' AND room_id IS NULL) OR (status = 'SCHEDULED' AND room_id IS NOT NULL))
);

-- Links an attendee to an event as a confirmed seat or a waitlist entry (UC-5, UC-5a, UC-6).
CREATE TABLE IF NOT EXISTS registrations (
    registration_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    attendee_id      INTEGER NOT NULL REFERENCES attendees (attendee_id) ON DELETE RESTRICT,
    event_id         INTEGER NOT NULL REFERENCES events (event_id) ON DELETE RESTRICT,
    status           TEXT    NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED')),
    registered_at    TEXT    NOT NULL,                -- waitlist order is oldest first
    checked_in_at    TEXT,                            -- NULL = not checked in to this event
    promotion_notice INTEGER NOT NULL DEFAULT 0 CHECK (promotion_notice IN (0, 1)),
    UNIQUE (attendee_id, event_id)                    -- one registration per attendee per event (UC-5 alt 2a)
);

-- Indexes for the NFR-1 search, check-in and overlap timings (Design §5.1).
CREATE INDEX IF NOT EXISTS idx_attendees_name   ON attendees (last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_attendees_email  ON attendees (email);
CREATE INDEX IF NOT EXISTS idx_registrations_event_status ON registrations (event_id, status);
CREATE INDEX IF NOT EXISTS idx_events_room_date ON events (room_id, event_date);

-- ===== Integrity triggers (NFR-4). The service layer checks first; these are the second safeguard. =====

-- NFR-4(3): a CONFIRMED registration cannot be added to an event that already has max_capacity confirmed seats.
CREATE TRIGGER IF NOT EXISTS trg_registrations_capacity_insert
BEFORE INSERT ON registrations
WHEN NEW.status = 'CONFIRMED'
BEGIN
    SELECT RAISE(ABORT, 'Event is full')
    WHERE (SELECT COUNT(*) FROM registrations
           WHERE event_id = NEW.event_id AND status = 'CONFIRMED')
          >= (SELECT max_capacity FROM events WHERE event_id = NEW.event_id);
END;

-- NFR-4(3): the same rule when a waitlist entry is promoted to CONFIRMED (UC-6 step 5).
CREATE TRIGGER IF NOT EXISTS trg_registrations_capacity_update
BEFORE UPDATE OF status ON registrations
WHEN NEW.status = 'CONFIRMED' AND OLD.status <> 'CONFIRMED'
BEGIN
    SELECT RAISE(ABORT, 'Event is full')
    WHERE (SELECT COUNT(*) FROM registrations
           WHERE event_id = NEW.event_id AND status = 'CONFIRMED')
          >= (SELECT max_capacity FROM events WHERE event_id = NEW.event_id);
END;

-- NFR-4(1) and (2) for events inserted already holding a room (for example, demo data loaded by SQL).
CREATE TRIGGER IF NOT EXISTS trg_events_room_insert
BEFORE INSERT ON events
WHEN NEW.room_id IS NOT NULL
BEGIN
    SELECT RAISE(ABORT, 'Room capacity is below the event capacity')
    WHERE (SELECT capacity FROM rooms WHERE room_id = NEW.room_id) < NEW.max_capacity;
    -- Overlap: new start < existing end AND new end > existing start. Back-to-back events are allowed.
    SELECT RAISE(ABORT, 'Another event in this room overlaps this time')
    WHERE EXISTS (SELECT 1 FROM events e
                  WHERE e.room_id = NEW.room_id AND e.event_date = NEW.event_date
                    AND NEW.start_time < e.end_time AND NEW.end_time > e.start_time);
END;

-- NFR-4(1) and (2) whenever an event's room, date, times or capacity change (UC-4).
CREATE TRIGGER IF NOT EXISTS trg_events_room_update
BEFORE UPDATE OF room_id, event_date, start_time, end_time, max_capacity ON events
WHEN NEW.room_id IS NOT NULL
BEGIN
    SELECT RAISE(ABORT, 'Room capacity is below the event capacity')
    WHERE (SELECT capacity FROM rooms WHERE room_id = NEW.room_id) < NEW.max_capacity;
    SELECT RAISE(ABORT, 'Another event in this room overlaps this time')
    WHERE EXISTS (SELECT 1 FROM events e
                  WHERE e.room_id = NEW.room_id AND e.event_date = NEW.event_date
                    AND e.event_id <> NEW.event_id
                    AND NEW.start_time < e.end_time AND NEW.end_time > e.start_time);
END;

-- NFR-4(3): an event's capacity cannot be lowered below its current confirmed seats.
CREATE TRIGGER IF NOT EXISTS trg_events_capacity_below_confirmed
BEFORE UPDATE OF max_capacity ON events
BEGIN
    SELECT RAISE(ABORT, 'Capacity is below the current confirmed registrations')
    WHERE (SELECT COUNT(*) FROM registrations
           WHERE event_id = NEW.event_id AND status = 'CONFIRMED') > NEW.max_capacity;
END;

-- NFR-4(1) and UC-3 NFR note: a room's capacity cannot be lowered below an assigned event's capacity.
-- Because confirmed seats never exceed an event's capacity, this also keeps confirmed seats within the room.
CREATE TRIGGER IF NOT EXISTS trg_rooms_capacity_update
BEFORE UPDATE OF capacity ON rooms
BEGIN
    SELECT RAISE(ABORT, 'Room capacity is below an assigned event capacity')
    WHERE EXISTS (SELECT 1 FROM events WHERE room_id = NEW.room_id AND max_capacity > NEW.capacity);
END;
