package com.imperionite.cp2c.security;

import java.util.UUID; // For generating unique IDs

/**
 * Utility class for generating simple authentication tokens.
 * In a real-world scenario, I might use UUIDs directly as tokens,
 * or combine them with user-specific data for more robust (though still not JWT-level) verification.
 * For this non-JWT approach, I will generate a simple UUID string.
 * The `AuthService` will then validate this token by looking it up in the stored user data.
 */
public class TokenUtil {

    /**
     * Generates a unique, random token string.
     * For this simple token authentication, a UUID provides sufficient uniqueness.
     * In a more complex system, I might embed user ID or other data,
     * but for non-JWT, validation typically involves a lookup.
     *
     * @param userId   The ID of the user for whom the token is generated.
     * @param username The username of the user for whom the token is generated.
     * @return A unique token string.
     */
    public static String generateToken(String userId, String username) {
        // A simple UUID is a good start for a non-JWT token, as it's highly unique.
        // The token's validity will be checked against the stored token in the user's CSV record.
        String token = UUID.randomUUID().toString();
        System.out.println("TokenUtil: Generated token for user " + username + ": " + (token.length() > 5 ? token.substring(0, 5) + "..." : token));
        return token;
    }

    // For a non-JWT token, there's typically no 'decode' method here that verifies
    // a signature. Validation occurs by comparing the provided token to a stored token.
}
