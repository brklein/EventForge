package eventforge.service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Turns text typed into a form into checked values, so every screen reports errors the same way (NFR-2).
 * Every method throws ValidationException naming the field and saying how to fix it.
 */
public final class InputValidator {

    private InputValidator() { }       // static helpers only

    /** Returns the trimmed text, or throws if it is null or blank (UC-1 alt 3a). */
    public static String requireText(String field, String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Parses a whole number greater than zero, such as an event or room capacity (UC-2 alt 4a, UC-3 step 4).
     * Rejects blank, zero, negative, decimal ("2.5") and non-numeric ("abc") input (Matrix #7).
     */
    public static int parsePositiveInt(String field, String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Parses a date typed as YYYY-MM-DD. */
    public static LocalDate parseDate(String field, String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Parses a time typed as HH:MM (24-hour). */
    public static LocalTime parseTime(String field, String text) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the trimmed email, or throws if it is blank or has no '@' with text on both sides. */
    public static String requireEmail(String field, String text) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
