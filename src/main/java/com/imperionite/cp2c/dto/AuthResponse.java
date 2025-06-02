package com.imperionite.cp2c.dto;

/**
 * DTO for authentication response.
 */
public class AuthResponse {
    private String id;
    private String username;
    private String token;
    private String message;
    
    public AuthResponse(String id, String username, String token, String message) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
