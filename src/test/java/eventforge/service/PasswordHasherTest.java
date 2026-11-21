package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for PBKDF2 password hashing (NFR-5, Design §8). */
class PasswordHasherTest {
    // The organizer account from test-data.sql: a known answer for the algorithm settings.
    private static final String SALT = "f4ffed46f63eebc898ff956b9576d829";
    private static final String HASH = "4470e991491aea4085b92c1bb2b217ad84fa19be890a44cacd09f436af96d873";

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    @DisplayName("[NFR-5] Hash matches the known PBKDF2-SHA256 value for the test organizer")
    void nfr5_knownHash() {
        assertEquals(HASH, hasher.hash("Organizer#2026", SALT));
    }

    @Test
    @DisplayName("[NFR-5] Correct password matches; wrong password and wrong case do not")
    void nfr5_matches() {
        assertTrue(hasher.matches("Organizer#2026", SALT, HASH));
        assertFalse(hasher.matches("organizer#2026", SALT, HASH));
        assertFalse(hasher.matches("", SALT, HASH));
    }

    @Test
    @DisplayName("[NFR-5] Each new salt is 16 random bytes in hex")
    void nfr5_newSaltIsRandom() {
        String first = hasher.newSalt();
        String second = hasher.newSalt();
        assertEquals(32, first.length());
        assertTrue(first.matches("[0-9a-f]{32}"), first);
        assertNotEquals(first, second);
    }
}
