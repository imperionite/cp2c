package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.util.CsvFileHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of UserDao, utilizing CsvFileHandler.
 * CSV format: id,username,passwordHash,token
 */
public class CSVUserDao implements UserDao {
    // Use a generic CsvFileHandler for User objects
    private final CsvFileHandler<User> csvFileHandler;
    private static final String CSV_FILENAME = "users.csv";
    // Header for users.csv
    private static final String USER_CSV_HEADER = "id,username,passwordHash,token";

    public CSVUserDao() {
        this.csvFileHandler = new CsvFileHandler<>(
                CSV_FILENAME,
                line -> {
                    String[] tokens = line.split(",", -1); // -1 to keep trailing empty strings
                    if (tokens.length >= 4) {
                        String token = tokens[3] != null ? tokens[3] : "";
                        return new User(tokens[0], tokens[1], tokens[2], token);
                    }
                    return null;
                },
                User::toCsvLine,
                true // users.csv WILL have a header, and CsvFileHandler will skip it on read
        );

        Path userCsvPath = Paths.get("data", CSV_FILENAME);
        try {
            if (Files.exists(userCsvPath) && Files.size(userCsvPath) == 0) {
                try (BufferedWriter writer = Files.newBufferedWriter(userCsvPath, StandardOpenOption.APPEND)) {
                    writer.write(USER_CSV_HEADER);
                    writer.newLine();
                    System.out.println("Added header to data/users.csv on initial empty file creation.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error ensuring header for data/users.csv: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return getAllUsers().stream()
                .filter(u -> u != null && u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public void saveUser(User user) {
        List<User> users = getAllUsers();
        // Remove existing user if found by ID to ensure update
        users = users.stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList());
        // Also ensure username uniqueness for new users or if username is changed
        if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername()))) {
            System.err.println("CSVUserDao: Attempted to save user with duplicate username: " + user.getUsername());
            throw new IllegalArgumentException("Username already exists.");
        }
        users.add(user);
        csvFileHandler.writeHeaderAndAll(USER_CSV_HEADER, users);
        System.out.println("Saved user '" + user.getUsername() + "' to data/users.csv.");
    }

    @Override
    public void deleteUser(String userId) {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(u -> u.getId().equals(userId));
        if (removed) {
            csvFileHandler.writeHeaderAndAll(USER_CSV_HEADER, users);
            System.out.println("Deleted user with ID: " + userId + " from data/users.csv.");
        } else {
            System.out.println("User with ID: " + userId + " not found for deletion.");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return csvFileHandler.readAll();
    }
}