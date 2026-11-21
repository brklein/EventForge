package eventforge.service;

/**
 * Thrown when user input is missing or malformed (UC-1 alt 3a, UC-2 alt 4a, UC-3 step 4).
 * The message is shown to the user as-is, so it must say what is wrong and how to fix it (NFR-2).
 */
public class ValidationException extends RuntimeException {
    private final String field;        // parameter name of the input at fault (for example "capacity"), or null

    /** Creates a validation error for one form field, so the screen can highlight it. */
    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    /** Creates a validation error that is not tied to a single field. */
    public ValidationException(String message) {
        this(null, message);
    }

    public String getField() { return field; }
}
