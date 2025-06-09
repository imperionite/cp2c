package com.imperionite.cp2c.service;

import com.imperionite.cp2c.dao.UserDao;
import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.security.PasswordUtil;
import com.imperionite.cp2c.security.TokenUtil;
import com.imperionite.cp2c.dto.AuthResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service class handling authentication and user management logic.
 * Interacts with UserDao to persist user data and uses security utilities.
 */
public class AuthService {
    private final UserDao userDao;

    // Regex pattern for username: must start with "user-" followed by 5 digits
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^user-\\d{5}$");

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Attempts to login with username and password.
     * If successful, generates a new token for the user, saves it, and returns AuthResponse.
     *
     * @param username The username for login.
     * @param password The plain-text password for login.
     * @return AuthResponse with token if successful, or null token and error message if not.
     */
    public AuthResponse login(String username, String password) {
        System.out.println("AuthService: Attempting login for username: " + username);
        Optional<User> userOptional = userDao.findByUsername(username);

        if (userOptional.isEmpty()) {
            System.out.println("AuthService: User '" + username + "' not found.");
            return new AuthResponse(null, null, null, "Invalid username or password");
        }

        User user = userOptional.get();
        System.out.println("AuthService: User found: " + user.getUsername());
        // System.out.println("AuthService: Stored password hash: " + user.getPasswordHash()); // Avoid logging sensitive data
        System.out.println("AuthService: Verifying password...");

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            System.out.println("AuthService: Password verification successful.");
            // Generate a NEW token on successful login
            String token = TokenUtil.generateToken(user.getId(), user.getUsername());
            user.setToken(token);
            userDao.saveUser(user); // Save user with new token to update the CSV
            System.out.println(
                    "AuthService: Login successful for user: " + user.getUsername() + ", generated token: " + (token.length() > 5 ? token.substring(0, 5) + "..." : token));
            return new AuthResponse(user.getId(), user.getUsername(), token, "Login successful");
        } else {
            System.out.println("AuthService: Password verification failed for user: " + user.getUsername());
            return new AuthResponse(null, null, null, "Invalid username or password");
        }
    }

    /**
     * Registers a new user.
     * Includes validation for username format and uniqueness.
     * A token is generated immediately upon successful registration and saved with the user.
     *
     * @param username The desired username.
     * @param password The desired password.
     * @return AuthResponse with user details and token if successful, or an error message.
     */
    public AuthResponse registerUser(String username, String password) {
        System.out.println("AuthService: Attempting to register new user: " + username);

        // 1. Validate username format
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.out.println("AuthService: Registration failed - Username '" + username
                    + "' does not match required format (user-#####).");
            return new AuthResponse(null, null, null, "Username must be in the format user-##### (e.g., user-12345)");
        }

        // 2. Validate username uniqueness
        if (userDao.findByUsername(username).isPresent()) {
            System.out.println("AuthService: Registration failed - Username '" + username + "' already exists.");
            return new AuthResponse(null, null, null, "Username already exists");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        String userId = UUID.randomUUID().toString();
        // Token will be generated and set before saving, for immediate use after registration
        String token = TokenUtil.generateToken(userId, username);
        User newUser = new User(userId, username, hashedPassword, token);

        try {
            userDao.saveUser(newUser); // Saves the user with their newly generated token
            System.out.println("AuthService: User '" + username + "' registered successfully with token: " + (token.length() > 5 ? token.substring(0, 5) + "..." : token));
            return new AuthResponse(newUser.getId(), newUser.getUsername(), token, "Registration successful");
        } catch (Exception e) {
            System.err.println("AuthService: Error registering user '" + username + "': " + e.getMessage());
            return new AuthResponse(null, null, null, "Error during registration");
        }
    }

    /**
     * Deletes a user by their ID.
     * This method is intended to be called by other services (e.g., EmployeeService) for cascading deletions.
     *
     * @param userId The ID of the user to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteUser(String userId) {
        System.out.println("AuthService: Attempting to delete user with ID: " + userId);
        try {
            // DAO's deleteUser handles finding and removing.
            boolean deleted = userDao.deleteUser(userId);
            if (deleted) {
                System.out.println("AuthService: User (ID: " + userId + ") deleted successfully.");
            } else {
                System.out.println("AuthService: User with ID '" + userId + "' not found for deletion.");
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("AuthService: Error deleting user with ID '" + userId + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates a given token by checking if it matches the token currently stored for any user.
     * This is the core of your non-JWT token authentication.
     *
     * @param token The token string to validate.
     * @return The User object associated with the token if valid, otherwise null.
     */
    public User validateToken(String token) {
        System.out.println("AuthService: validateToken called with token: "
                + (token != null && token.length() > 10 ? token.substring(0, 10) + "..." : token));

        if (token == null || token.isEmpty()) {
            System.out.println("AuthService: Token is null or empty during validation. Returning null.");
            return null;
        }

        // Iterate through all users to find one whose stored token matches the provided token.
        // This relies on the assumption that each user has a unique, current token stored in their record.
        User foundUser = userDao.getAllUsers().stream()
                .filter(user -> token.equals(user.getToken())) // Strict equality check
                .findFirst()
                .orElse(null);

        if (foundUser != null) {
            System.out.println("AuthService: Token valid for user: " + foundUser.getUsername());
        } else {
            System.out.println("AuthService: Token '" + (token.length() > 10 ? token.substring(0, 10) + "..." : token)
                    + "' NOT found or not associated with any active user. Returning null.");
        }
        return foundUser;
    }
}
