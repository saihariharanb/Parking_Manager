package com.college.parking.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying passwords using SHA-256.
 * Demonstrates secure password hashing for educational viva demonstrations.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using SHA-256 and returns a 64-character hexadecimal string.
     *
     * @param plainTextPassword password string to hash
     * @return hex encoded SHA-256 hash
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies that a plain text candidate matches the expected SHA-256 hash.
     *
     * @param candidatePassword candidate password entered by user
     * @param storedHash stored hash from database
     * @return true if candidate matches stored hash, false otherwise
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword);
        return candidateHash.equalsIgnoreCase(storedHash.trim());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
