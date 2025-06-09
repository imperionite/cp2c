package com.imperionite.cp2c.dto;

/**
 * DTO for authentication responses, including user details and token.
 */
public class AuthResponse {
    private String userId;
    private String username;
    private String token;
    private String message; // For success or error messages

    // Default constructor for Jackson serialization/deserialization
    public AuthResponse() {
    }

    public AuthResponse(String userId, String username, String token, String message) {
        this.userId = userId;
        this.username = username;
        this.token = token;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
