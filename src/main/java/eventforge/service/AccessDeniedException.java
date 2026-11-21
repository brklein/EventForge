package eventforge.service;

/**
 * Thrown by AuthService.requireRole when the current user may not perform an action (NFR-5).
 * A subclass of BusinessRuleException so controllers can show it the same way.
 */
public class AccessDeniedException extends BusinessRuleException {

    /** Creates a permission error with a message for the user. */
    public AccessDeniedException(String message) {
        super(message);
    }
}
