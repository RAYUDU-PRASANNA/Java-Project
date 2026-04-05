package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil — Secure password hashing using SHA-256 + salt.
 * No external library required. Compatible with all JREs.
 *
 * Format stored in DB:  SALT:HASH  (both Base64-encoded)
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a plain-text password and returns a storable string.
     * Format: "base64(salt):base64(sha256(salt+password))"
     */
    public static String hash(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hashed = sha256(salt, plainPassword);
        return Base64.getEncoder().encodeToString(salt)
             + ":"
             + Base64.getEncoder().encodeToString(hashed);
    }

    /**
     * Verifies a plain-text password against the stored hash string.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) return false;
        String[] parts = storedHash.split(":", 2);
        byte[] salt   = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual   = sha256(salt, plainPassword);
        return MessageDigest.isEqual(expected, actual);
    }

    // ── Private helpers ─────────────────────────────────────

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(password.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
