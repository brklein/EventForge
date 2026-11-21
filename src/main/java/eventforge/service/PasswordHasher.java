package eventforge.service;

/**
 * Hashes staff passwords with PBKDF2 from the JDK's javax.crypto (Design §8, OWASP 2024).
 * Algorithm PBKDF2WithHmacSHA256, 600,000 iterations, 256-bit key, 16-byte random salt; hash and salt are hex text.
 * Passwords are never stored or logged in plain text (NFR-5).
 */
public class PasswordHasher {
    public static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int ITERATIONS = 600_000;
    public static final int KEY_LENGTH_BITS = 256;
    public static final int SALT_BYTES = 16;

    /** Returns a new random salt from SecureRandom, hex encoded. */
    public String newSalt() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Returns the hex-encoded PBKDF2 hash of the password with the given hex salt. */
    public String hash(String password, String salt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** True if the password hashes to expectedHash. Compares in constant time (MessageDigest.isEqual). */
    public boolean matches(String password, String salt, String expectedHash) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
