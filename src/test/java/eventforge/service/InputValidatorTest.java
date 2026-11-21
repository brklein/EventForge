package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for form input parsing shared by every screen (NFR-2). */
class InputValidatorTest {

    @ParameterizedTest(name = "capacity \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"0", "-5", "2.5", "abc", "   "})
    @DisplayName("[UC-2 alt 4a] Non-positive or non-integer capacity is rejected (Matrix #7)")
    void uc2Alt4a_badCapacityRejected(String text) {
        ValidationException e = assertThrows(ValidationException.class,
                () -> InputValidator.parsePositiveInt("capacity", text));
        assertEquals("capacity", e.getField());
    }

    @Test
    @DisplayName("[UC-2 main flow] Positive whole-number capacity is accepted, ignoring spaces")
    void uc2_validCapacityParsed() {
        assertEquals(25, InputValidator.parsePositiveInt("capacity", "25"));
        assertEquals(30, InputValidator.parsePositiveInt("capacity", " 30 "));
        assertEquals(1, InputValidator.parsePositiveInt("capacity", "1"));
    }

    @Test
    @DisplayName("[UC-1 alt 3a] Blank required text is rejected and the field named")
    void uc1Alt3a_blankTextRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> InputValidator.requireText("firstName", "  "));
        assertEquals("firstName", e.getField());
        assertEquals("Jane", InputValidator.requireText("firstName", " Jane "));
    }

    @Test
    @DisplayName("[UC-1 alt 3a] Email without '@' is rejected")
    void uc1Alt3a_invalidEmailRejected() {
        assertThrows(ValidationException.class, () -> InputValidator.requireEmail("email", "jane.example.com"));
        assertThrows(ValidationException.class, () -> InputValidator.requireEmail("email", "@example.com"));
        assertEquals("jane@example.com", InputValidator.requireEmail("email", "jane@example.com"));
    }

    @Test
    @DisplayName("[UC-2 step 4] Dates and times parse from YYYY-MM-DD and HH:MM; invalid values are rejected")
    void uc2_dateAndTimeParsing() {
        assertEquals(LocalDate.of(2026, 10, 10), InputValidator.parseDate("date", "2026-10-10"));
        assertEquals(LocalTime.of(10, 0), InputValidator.parseTime("startTime", "10:00"));
        assertThrows(ValidationException.class, () -> InputValidator.parseDate("date", "10/10/26"));
        assertThrows(ValidationException.class, () -> InputValidator.parseTime("startTime", "25:00"));
    }
}
