package com.imperionite.cp2c.model; // Changed package to cp2c

/**
 * Represents a User entity with id, username, hashed password, and token.
 */
public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String token;

    public User(String id, String username, String passwordHash, String token) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.token = token;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    /**
     * Serializes a User object into a CSV line string.
     * Order: id,username,passwordHash,token
     * @return A CSV formatted string representing the User.
     */
    public String toCsvLine() {
        return String.join(",",
                id,
                username,
                passwordHash,
                token != null ? token : "" // Ensure token is not null
        );
    }
}