package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * Data Access Object for User entities, managing persistence to a CSV file.
 * This class handles reading User data from and writing to `users.csv`.
 * It includes basic in-memory caching and thread-safety for concurrent access.
 */
public class UserDao {
    private final String filePath;
    private final List<User> users; // In-memory cache of users
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // For thread-safe access

    // CSV header for the users file
    private static final String CSV_HEADER = "id,username,passwordHash,token";
    // Delimiter for CSV file
    private static final String CSV_DELIMITER = ",";

    public UserDao(String filePath) {
        this.filePath = filePath;
        // Ensure the CSV file exists with its header when the DAO is initialized
        initializeCsvFile();
        // Load existing users from CSV on initialization
        this.users = loadUsersFromCsv();
        System.out.println("UserDao: Initialized with " + users.size() + " users loaded from " + filePath);
    }

    /**
     * Checks if the CSV file exists. If not, it creates the file with its header.
     */
    private void initializeCsvFile() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                // Ensure parent directory exists before creating the file
                Path parentDir = path.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                    System.out.println("UserDao: Created parent directory for users CSV: " + parentDir.toAbsolutePath());
                }
                Files.writeString(path, CSV_HEADER + System.lineSeparator());
                System.out.println("UserDao: Created new users CSV file with header: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("UserDao: Error creating users CSV file '" + filePath + "': " + e.getMessage());
                // Rethrow as runtime exception since persistence won't work without the file
                throw new RuntimeException("Failed to initialize users CSV file.", e);
            }
        }
    }

    /**
     * Loads users from the CSV file.
     *
     * @return A list of User objects.
     */
    private List<User> loadUsersFromCsv() {
        // Use CSVUtils to read lines and map them to User objects
        return CSVUtils.loadFromCsv(filePath, this::mapCsvLineToUser, true); // true to skip header
    }

    /**
     * Maps a single CSV line string to a User object.
     * Handles potential parsing errors.
     *
     * @param line The CSV line string.
     * @return A User object, or null if parsing fails.
     */
    private User mapCsvLineToUser(String line) {
        try {
            String[] parts = line.split(Pattern.quote(CSV_DELIMITER), -1); // -1 to keep trailing empty strings
            if (parts.length >= 4) { // Ensure enough parts for id, username, passwordHash, token
                String id = parts[0];
                String username = parts[1];
                String passwordHash = parts[2];
                // Handle potential empty token string from CSV
                String token = parts[3].isEmpty() ? null : parts[3];
                return new User(id, username, passwordHash, token);
            } else {
                System.err.println("UserDao: Skipping malformed CSV line (expected 4+ parts): " + line);
                return null;
            }
        } catch (Exception e) {
            System.err.println("UserDao: Error parsing CSV line to User: '" + line + "'. Error: " + e.getMessage());
            return null; // Return null for unparseable lines
        }
    }

    /**
     * Saves the current list of users to the CSV file.
     * This method is called after any modification (add, update, delete).
     */
    private void saveUsersToCsv() {
        // Use CSVUtils to write User objects to CSV
        CSVUtils.saveToCsv(filePath, users, this::mapUserToCsvLine, CSV_HEADER);
    }

    /**
     * Maps a User object to a single CSV line string.
     *
     * @param user The User object.
     * @return A CSV formatted string.
     */
    private String mapUserToCsvLine(User user) {
        // Handle null token gracefully for CSV serialization (write as empty string)
        String token = user.getToken() != null ? user.getToken() : "";
        return String.join(CSV_DELIMITER, user.getId(), user.getUsername(), user.getPasswordHash(), token);
    }

    /**
     * Adds a new user or updates an existing one if the ID matches.
     * If the user already exists (same ID), it updates their details.
     * Otherwise, it adds a new user.
     *
     * @param user The User object to save.
     */
    public void saveUser(User user) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            int index = -1;
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(user.getId())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                // Update existing user
                users.set(index, user);
                System.out.println("UserDao: Updated user with ID: " + user.getId() + " and username: " + user.getUsername());
            } else {
                // Add new user
                users.add(user);
                System.out.println("UserDao: Added new user with ID: " + user.getId() + " and username: " + user.getUsername());
            }
            saveUsersToCsv(); // Persist changes to CSV
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    public Optional<User> findByUsername(String username) {
        lock.readLock().lock(); // Acquire read lock
        try {
            return users.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst();
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Finds a user by their ID.
     *
     * @param id The user ID to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    public Optional<User> findById(String id) {
        lock.readLock().lock(); // Acquire read lock
        try {
            return users.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     * @return true if the user was deleted, false otherwise.
     */
    public boolean deleteUser(String userId) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            // Remove from in-memory list
            boolean removed = users.removeIf(user -> user.getId().equals(userId));
            if (removed) {
                saveUsersToCsv(); // Persist changes to CSV
                System.out.println("UserDao: Successfully deleted user with ID: " + userId);
            } else {
                System.out.println("UserDao: User with ID " + userId + " not found for deletion.");
            }
            return removed;
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Retrieves an unmodifiable list of all users.
     *
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        lock.readLock().lock(); // Acquire read lock
        try {
            // Return an unmodifiable list to prevent external modification of the internal cache
            return Collections.unmodifiableList(new ArrayList<>(users));
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }
}
