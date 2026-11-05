package eventforge.model;

import java.time.LocalDate;

/** A convention. Attendees and events belong to one convention (UC-1 and UC-2 preconditions). */
public class Convention {
    private int conventionId;          // 0 until saved to the database
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;

    /** Creates a new convention that has not been saved yet. */
    public Convention(String name, LocalDate startDate, LocalDate endDate) {
        this(0, name, startDate, endDate);
    }

    /** Creates a convention loaded from the database. */
    public Convention(int conventionId, String name, LocalDate startDate, LocalDate endDate) {
        this.conventionId = conventionId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getConventionId() { return conventionId; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    /** Called by the DAO after the database assigns the generated ID. */
    public void setConventionId(int conventionId) { this.conventionId = conventionId; }
}