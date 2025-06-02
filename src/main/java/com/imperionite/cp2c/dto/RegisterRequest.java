package com.imperionite.cp2c.dto;

/**
 * DTO for user registration request payload.
 */
public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}