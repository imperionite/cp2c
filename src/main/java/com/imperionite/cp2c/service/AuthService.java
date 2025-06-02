package com.imperionite.cp2c.service;

import com.imperionite.cp2c.dao.UserDao;
import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.security.PasswordUtil;
import com.imperionite.cp2c.security.TokenUtil;
import com.imperionite.cp2c.dto.AuthResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern; // Import Pattern

/**
 * Service class handling authentication and user management logic.
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
     * Returns AuthResponse with token if successful.
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
        System.out.println("AuthService: Stored password hash: " + user.getPasswordHash());
        System.out.println("AuthService: Verifying password...");

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            System.out.println("AuthService: Password verification successful.");
            String token = TokenUtil.generateToken(user.getId(), user.getUsername());
            user.setToken(token);
            userDao.saveUser(user); // Save user with new token
            System.out.println("AuthService: Login successful for user: " + user.getUsername() + ", generated token: " + token);
            return new AuthResponse(user.getId(), user.getUsername(), token, "Login successful");
        } else {
            System.out.println("AuthService: Password verification failed for user: " + user.getUsername());
            return new AuthResponse(null, null, null, "Invalid username or password");
        }
    }

    /**
     * Registers a new user.
     * Includes validation for username format.
     * @param username The desired username.
     * @param password The desired password.
     * @return AuthResponse with user details and token if successful, or an error message.
     */
    public AuthResponse registerUser(String username, String password) {
        System.out.println("AuthService: Attempting to register new user: " + username);

        // 1. Validate username format
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.out.println("AuthService: Registration failed - Username '" + username + "' does not match required format (user-#####).");
            return new AuthResponse(null, null, null, "Username must be in the format user-##### (e.g., user-12345)");
        }

        // 2. Validate username uniqueness
        if (userDao.findByUsername(username).isPresent()) {
            System.out.println("AuthService: Registration failed - Username '" + username + "' already exists.");
            return new AuthResponse(null, null, null, "Username already exists");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, username, hashedPassword, ""); // Token will be generated on first login

        try {
            userDao.saveUser(newUser);
            System.out.println("AuthService: User '" + username + "' registered successfully.");
            // For immediate use, generate a token after registration
            String token = TokenUtil.generateToken(newUser.getId(), newUser.getUsername());
            newUser.setToken(token);
            userDao.saveUser(newUser); // Save again with token
            return new AuthResponse(newUser.getId(), newUser.getUsername(), token, "Registration successful");
        } catch (Exception e) {
            System.err.println("AuthService: Error registering user '" + username + "': " + e.getMessage());
            return new AuthResponse(null, null, null, "Error during registration");
        }
    }

    /**
     * Deletes a user by their ID.
     * This method is intended to be called by other services (e.g., EmployeeService) for cascading deletions.
     * @param userId The ID of the user to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteUser(String userId) {
        System.out.println("AuthService: Attempting to delete user with ID: " + userId);
        try {
            // Need to find the user by ID first to get the username for logging,
            // and to ensure the user exists before attempting deletion.
            Optional<User> userOptional = userDao.getAllUsers().stream()
                                            .filter(u -> u.getId().equals(userId))
                                            .findFirst();
            if (userOptional.isPresent()) {
                userDao.deleteUser(userOptional.get().getId()); // Pass the actual ID to DAO
                System.out.println("AuthService: User '" + userOptional.get().getUsername() + "' (ID: " + userId + ") deleted successfully.");
                return true;
            } else {
                System.out.println("AuthService: User with ID '" + userId + "' not found for deletion.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("AuthService: Error deleting user with ID '" + userId + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates a given token. In a real application, this would involve
     * checking token expiration, signature (for JWTs), etc.
     * For this simple implementation, we just check if any user has this token.
     * @param token The token to validate.
     * @return The User associated with the token if valid, null otherwise.
     */
    public User validateToken(String token) {
        System.out.println("AuthService: validateToken called with token: " + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null")); // Log first 10 chars
        if (token == null || token.isEmpty()) {
            System.out.println("AuthService: Token is null or empty during validation. Returning null.");
            return null;
        }
        
        User foundUser = userDao.getAllUsers().stream()
                .filter(u -> u.getToken() != null && u.getToken().equals(token))
                .findFirst()
                .orElse(null);

        if (foundUser != null) {
            System.out.println("AuthService: Token valid for user: " + foundUser.getUsername());
        } else {
            System.out.println("AuthService: Token '" + (token.substring(0, Math.min(token.length(), 10)) + "...") + "' not found for any user. Returning null.");
        }
        return foundUser;
    }
}