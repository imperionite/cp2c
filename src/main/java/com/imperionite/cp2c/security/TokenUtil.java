package com.imperionite.cp2c.security;

import java.util.UUID;

/**
 * Utility class for token generation.
 */
public class TokenUtil {

    /**
     * Generates a random UUID token.
     * In a real application, consider using JWT (JSON Web Tokens) for more robust
     * and secure token management (e.g., expiration, signing, claims).
     * 
     * @param id       The ID of the user for whom the token is generated.
     * @param username The username for whom the token is generated.
     * @return A randomly generated UUID string.
     */
    public static String generateToken(String id, String username) { // Method signature updated
        // For a simple implementation, a UUID is sufficient.
        // For production, consider libraries like jjwt for JWT implementation.
        return UUID.randomUUID().toString();
    }
}