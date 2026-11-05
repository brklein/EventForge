package eventforge.model;

import java.time.LocalDateTime;

/** Links an {@link Attendee} to an {@link Event} as either a confirmed seat or a waitlist entry (UC-5, UC-6). */
public class Registration {
    private int registrationId;        // 0 until saved to the database
    private final int attendeeId;
    private final int eventId;
    private final RegistrationStatus status;
    private final LocalDateTime registeredAt;
    private final LocalDateTime checkedInAt;   // null = not checked in to this event
    private final boolean promotionNotice;     // true if promoted from the waitlist and not yet seen

    /** Creates a new registration that has not been saved yet. */
    public Registration(int attendeeId, int eventId, RegistrationStatus status, LocalDateTime registeredAt) {
        this(0, attendeeId, eventId, status, registeredAt, null, false);
    }

    /** Creates a registration loaded from the database. */
    public Registration(int registrationId, int attendeeId, int eventId, RegistrationStatus status,
                        LocalDateTime registeredAt, LocalDateTime checkedInAt, boolean promotionNotice) {
        this.registrationId = registrationId;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.status = status;
        this.registeredAt = registeredAt;
        this.checkedInAt = checkedInAt;
        this.promotionNotice = promotionNotice;
    }

    public int getRegistrationId() { return registrationId; }
    public int getAttendeeId() { return attendeeId; }
    public int getEventId() { return eventId; }
    public RegistrationStatus getStatus() { return status; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public boolean hasPromotionNotice() { return promotionNotice; }

    /** Called by the DAO after the database assigns the generated ID. */
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
}