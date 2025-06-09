package com.imperionite.cp2c.model;

import java.util.Objects; // For Objects.hash and Objects.equals

/**
 * Represents a user in the system, primarily for authentication.
 * This class stores user ID, username, hashed password, and their current token.
 */
public class User {
    private String id; // Unique ID for the user
    private String username;
    private String passwordHash; // Stores the BCrypt hashed password
    private String token; // Stores the current valid token for the user

    // Default constructor for Jackson deserialization (if needed, though direct
    // construction is often preferred)
    public User() {
    }

    /**
     * Constructor for creating a new User object.
     *
     * @param id           Unique identifier for the user.
     * @param username     The user's unique username.
     * @param passwordHash The BCrypt hashed password.
     * @param token        The current authentication token for the user (can be null or empty initially).
     */
    public User(String id, String username, String passwordHash, String token) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.token = token;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getToken() {
        return token;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // --- Overrides for equals(), hashCode(), and toString() for proper object comparison and logging ---

    /**
     * Compares this User object with another object for equality.
     * Users are considered equal if their IDs are the same.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Generates a hash code for this User object based on its ID.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the User object.
     *
     * @return A string containing user ID, username, and token (partial for security).
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='[PROTECTED]'" + // Do not log password hash directly
                ", token='" + (token != null && token.length() > 5 ? token.substring(0, 5) + "..." : token) + '\'' +
                '}';
    }
}
