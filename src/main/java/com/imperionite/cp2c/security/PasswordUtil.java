package com.imperionite.cp2c.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using BCrypt.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain password against a hashed password.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty())
            return false;
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
