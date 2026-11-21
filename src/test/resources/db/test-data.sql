-- EventForge test database (P7). Loaded after schema.sql into a fresh database before every test.
-- Each block names the P3 Test Matrix rows it supports. Keep IDs fixed: the tests refer to them.
-- Staff passwords (test only): organizer / Organizer#2026, staff / Staff#2026.
-- Hashes are PBKDF2WithHmacSHA256, 600,000 iterations, 256-bit key, hex encoded (see PasswordHasher).

-- Convention 1: required by UC-1 and UC-2 preconditions.
INSERT INTO conventions (convention_id, name, start_date, end_date)
VALUES (1, 'ForgeCon 2026', '2026-10-09', '2026-10-11');

-- Users: one Organizer and one Event Staff account (Matrix #1, #2).
INSERT INTO users (user_id, username, password_hash, salt, role) VALUES
 (1, 'organizer', '4470e991491aea4085b92c1bb2b217ad84fa19be890a44cacd09f436af96d873',
     'f4ffed46f63eebc898ff956b9576d829', 'ORGANIZER'),
 (2, 'staff',     '0ec98c6e69a5c6d7b4ee87837aea6b86881ad4b290e5a21841b7391c0dd36090',
     'a66a2580ac4aad600a226eccc2699017', 'EVENT_STAFF');

-- Rooms (Matrix #8–#12).
--   Room B holds two events, so it cannot be deleted (#9); Room D holds none, so it can.
INSERT INTO rooms (room_id, name, capacity) VALUES
 (1, 'Room A', 100),
 (2, 'Room B', 30),
 (3, 'Room C', 10),
 (4, 'Room D', 40);

-- Attendees 1–20. Attendee 1 is the duplicate-check target (UC-1 alt 5a); none are checked in yet.
INSERT INTO attendees (attendee_id, convention_id, first_name, last_name, email, phone, badge_type) VALUES
 (1,  1, 'Alex',   'Rivera',   'alex.rivera@example.com',   '555-0101', 'GENERAL'),
 (2,  1, 'Sam',    'Lee',      'sam.lee@example.com',       '555-0102', 'VIP'),
 (3,  1, 'Pat',    'Kim',      'pat.kim@example.com',       '555-0103', 'GENERAL'),
 (4,  1, 'Morgan', 'Chen',     'morgan.chen@example.com',   '555-0104', 'VENDOR'),
 (5,  1, 'Jordan', 'Patel',    'jordan.patel@example.com',  '555-0105', 'GENERAL'),
 (6,  1, 'Casey',  'Nguyen',   'casey.nguyen@example.com',  '555-0106', 'GUEST'),
 (7,  1, 'Riley',  'Garcia',   'riley.garcia@example.com',  '555-0107', 'GENERAL'),
 (8,  1, 'Avery',  'Johnson',  'avery.johnson@example.com', '555-0108', 'GENERAL'),
 (9,  1, 'Quinn',  'Brown',    'quinn.brown@example.com',   '555-0109', 'GENERAL'),
 (10, 1, 'Drew',   'Davis',    'drew.davis@example.com',    '555-0110', 'VIP'),
 (11, 1, 'Taylor', 'Wilson',   'taylor.wilson@example.com', '555-0111', 'GENERAL'),
 (12, 1, 'Jamie',  'Moore',    'jamie.moore@example.com',   '555-0112', 'GENERAL'),
 (13, 1, 'Robin',  'Clark',    'robin.clark@example.com',   '555-0113', 'GENERAL'),
 (14, 1, 'Skyler', 'Lewis',    'skyler.lewis@example.com',  '555-0114', 'GENERAL'),
 (15, 1, 'Reese',  'Walker',   'reese.walker@example.com',  '555-0115', 'GENERAL'),
 (16, 1, 'Harper', 'Hall',     'harper.hall@example.com',   '555-0116', 'GENERAL'),
 (17, 1, 'Emerson','Allen',    'emerson.allen@example.com', '555-0117', 'GENERAL'),
 (18, 1, 'Finley', 'Young',    'finley.young@example.com',  '555-0118', 'GENERAL'),
 (19, 1, 'Rowan',  'King',     'rowan.king@example.com',    '555-0119', 'GENERAL'),
 (20, 1, 'Sage',   'Wright',   'sage.wright@example.com',   '555-0120', 'GENERAL');

-- Scheduled events.
--   2 = "Event A" of Matrix #12 (Room B, 10:00–11:00). 2 and 3 are Room B's two events (#9).
--   4 = the empty 10-seat event (#13, #14). 9 = the report event: 8 seats, all taken (#17).
INSERT INTO events (event_id, convention_id, name, description, event_date, start_time, end_time,
                    max_capacity, status, room_id) VALUES
 (1, 1, 'Opening Ceremony',     'Welcome and schedule overview', '2026-10-09', '09:00', '10:00', 100, 'SCHEDULED', 1),
 (2, 1, 'Prop Making 101',      'Foam and thermoplastics basics', '2026-10-09', '10:00', '11:00', 25, 'SCHEDULED', 2),
 (3, 1, 'Costume Contest Prep', 'Judging criteria and walk-through', '2026-10-09', '13:00', '14:00', 20, 'SCHEDULED', 2),
 (4, 1, 'Sketch Workshop',      'Figure drawing for beginners', '2026-10-09', '15:00', '16:00', 10, 'SCHEDULED', 3),
 (9, 1, 'Cosplay Photoshoot',   'Small-group photo session', '2026-10-10', '13:00', '14:00', 8, 'SCHEDULED', 3);

-- Unassigned events (UC-4 tests).
--   5 = "Event C" of #12: overlaps event 2 in Room B.   6 = starts when event 2 ends (back-to-back boundary).
--   7 = capacity 50, too large for Room B (#11).       8 = capacity 30, exactly Room B's capacity (boundary).
INSERT INTO events (event_id, convention_id, name, description, event_date, start_time, end_time,
                    max_capacity, status, room_id) VALUES
 (5, 1, 'Panel: Indie Comics', 'Creators discuss self-publishing', '2026-10-09', '10:30', '11:30', 25, 'UNASSIGNED', NULL),
 (6, 1, 'Voice Acting Q&A',    'Audience questions',               '2026-10-09', '11:00', '12:00', 25, 'UNASSIGNED', NULL),
 (7, 1, 'Mega Fan Meetup',     'Large community gathering',        '2026-10-10', '14:00', '16:00', 50, 'UNASSIGNED', NULL),
 (8, 1, 'Artist Alley Tour',   'Guided walk through artist alley', '2026-10-10', '10:00', '11:00', 30, 'UNASSIGNED', NULL);

-- Registrations.
--   Event 9: attendees 1–8 CONFIRMED (1–5 checked in), 9 and 10 WAITLISTED (9 first) -> #17 counts 8 / 2 / 5.
--   Event 2: attendee 1 CONFIRMED -> already-registered (UC-5 alt 2a) and cancellation (#15) tests.
INSERT INTO registrations (registration_id, attendee_id, event_id, status, registered_at, checked_in_at) VALUES
 (1,  1, 9, 'CONFIRMED',  '2026-09-01T09:00:00', '2026-10-10T12:55:00'),
 (2,  2, 9, 'CONFIRMED',  '2026-09-01T09:05:00', '2026-10-10T12:56:00'),
 (3,  3, 9, 'CONFIRMED',  '2026-09-01T09:10:00', '2026-10-10T12:57:00'),
 (4,  4, 9, 'CONFIRMED',  '2026-09-01T09:15:00', '2026-10-10T12:58:00'),
 (5,  5, 9, 'CONFIRMED',  '2026-09-01T09:20:00', '2026-10-10T12:59:00'),
 (6,  6, 9, 'CONFIRMED',  '2026-09-01T09:25:00', NULL),
 (7,  7, 9, 'CONFIRMED',  '2026-09-01T09:30:00', NULL),
 (8,  8, 9, 'CONFIRMED',  '2026-09-01T09:35:00', NULL),
 (9,  9, 9, 'WAITLISTED', '2026-09-02T10:00:00', NULL),
 (10, 10, 9, 'WAITLISTED', '2026-09-02T11:00:00', NULL),
 (11, 1, 2, 'CONFIRMED',  '2026-09-03T08:00:00', NULL);
