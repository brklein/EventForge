package eventforge.model;

import java.time.LocalDateTime;

/** A person registered for the convention (UC-1). checkedInAt is null until convention-level check-in (UC-7). */
public class Attendee {
    private int attendeeId;            // 0 until saved to the database
    private final int conventionId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final BadgeType badgeType;
    private final LocalDateTime checkedInAt;   // null = not checked in

    /** Creates a new attendee that has not been saved yet (not checked in). */
    public Attendee(int conventionId, String firstName, String lastName,
                    String email, String phone, BadgeType badgeType) {
        this(0, conventionId, firstName, lastName, email, phone, badgeType, null);
    }

    /** Creates an attendee loaded from the database. */
    public Attendee(int attendeeId, int conventionId, String firstName, String lastName,
                    String email, String phone, BadgeType badgeType, LocalDateTime checkedInAt) {
        this.attendeeId = attendeeId;
        this.conventionId = conventionId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.badgeType = badgeType;
        this.checkedInAt = checkedInAt;
    }

    public int getAttendeeId() { return attendeeId; }
    public int getConventionId() { return conventionId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public BadgeType getBadgeType() { return badgeType; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }

    /** Called by the DAO after the database assigns the generated ID. */
    public void setAttendeeId(int attendeeId) { this.attendeeId = attendeeId; }
}