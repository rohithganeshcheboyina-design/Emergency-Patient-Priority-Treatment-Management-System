package com.hospital.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt password hashing and verification.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 10;

    /**
     * Hashes a plain-text password using BCrypt with a secure salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain-text password against a BCrypt hash.
     * Also supports plain-text fallback for development demo data if ever needed.
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (Exception e) {
                return false;
            }
        }
        // Fallback check if plain text
        return plainPassword.equals(storedHash);
    }

    public static void main(String[] args) {
        System.out.println("admin123=" + hashPassword("admin123"));
        System.out.println("doctor123=" + hashPassword("doctor123"));
        System.out.println("nurse123=" + hashPassword("nurse123"));
    }
}
