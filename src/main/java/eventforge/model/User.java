package eventforge.model;

/** A staff account (Organizer or Event Staff) that must log in (NFR-5). Only a salted hash of the password is stored. */
public class User {
    private int userId;                // 0 until saved to the database
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final Role role;

    /** Creates a new user that has not been saved yet. */
    public User(String username, String passwordHash, String salt, Role role) {
        this(0, username, passwordHash, salt, role);
    }

    /** Creates a user loaded from the database. */
    public User(int userId, String username, String passwordHash, String salt, Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public Role getRole() { return role; }

    /** Called by the DAO after the database assigns the generated ID. */
    public void setUserId(int userId) { this.userId = userId; }
}