package eventforge.service;

/**
 * Thrown when valid input breaks a business rule: room too small, schedule overlap, room still in use
 * (UC-3 alt 5a, UC-4 alt 4a/5a). The message is user-readable and is shown in a dialog (NFR-2).
 */
public class BusinessRuleException extends RuntimeException {

    /** Creates a rule violation with a message for the user. */
    public BusinessRuleException(String message) {
        super(message);
    }

    /** Creates a rule violation that wraps a lower-level cause, such as a trigger abort from SQLite. */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
