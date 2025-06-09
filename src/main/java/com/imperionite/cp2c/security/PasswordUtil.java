package com.imperionite.cp2c.security;

import org.mindrot.jbcrypt.BCrypt; // Import for BCrypt operations

/**
 * Utility class for handling password hashing and verification using BCrypt.
 * BCrypt is a strong, adaptive hashing algorithm, essential for secure password storage.
 */
public class PasswordUtil {

    // Default salt rounds for BCrypt. A higher value increases security but also processing time.
    // 10-12 is a common balance.
    private static final int SALT_ROUNDS = 12;

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainPassword The password string to hash.
     * @return The BCrypt hashed password string.
     */
    public static String hashPassword(String plainPassword) {
        System.out.println("PasswordUtil: Hashing password...");
        // genSalt() generates a new salt with the specified rounds.
        // hashpw() hashes the password using the generated salt.
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(SALT_ROUNDS));
        System.out.println("PasswordUtil: Password hashed successfully.");
        return hashedPassword;
    }

    /**
     * Verifies a plain-text password against a BCrypt hashed password.
     *
     * @param plainPassword  The plain-text password provided by the user.
     * @param hashedPassword The BCrypt hashed password retrieved from storage.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        System.out.println("PasswordUtil: Verifying password...");
        // checkpw() internally re-hashes the plainPassword with the salt extracted from
        // the hashedPassword and compares the result.
        boolean isMatch = BCrypt.checkpw(plainPassword, hashedPassword);
        System.out.println("PasswordUtil: Password verification result: " + isMatch);
        return isMatch;
    }
}
