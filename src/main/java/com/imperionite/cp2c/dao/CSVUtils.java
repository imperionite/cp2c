package com.imperionite.cp2c.dao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for reading from and writing to CSV files.
 * Provides generic methods to load a list of objects from a CSV and save a list of objects to a CSV.
 */
public class CSVUtils {

    // Header line for the CSV (optional, but good for readability and parsing)
    // The specific header will be managed by individual DAOs.

    /**
     * Loads a list of objects from a CSV file.
     * Each line is converted to an object using the provided `mapper` function.
     *
     * @param filePath The path to the CSV file.
     * @param mapper   A function that takes a CSV line (String) and converts it to an object of type T.
     * @param skipHeader True if the first line of the CSV (header) should be skipped.
     * @param <T>      The type of objects to load.
     * @return A list of objects loaded from the CSV.
     */
    public static <T> List<T> loadFromCsv(String filePath, Function<String, T> mapper, boolean skipHeader) {
        Path path = Paths.get(filePath);
        List<T> data = new ArrayList<>();
        System.out.println("CSVUtils: Attempting to load from CSV: " + path.toAbsolutePath());

        // Ensure the file exists before attempting to read
        if (!Files.exists(path) || !Files.isReadable(path)) {
            System.out.println("CSVUtils: CSV file not found or not readable: " + path.toAbsolutePath() + ". Returning empty list.");
            return data; // Return empty list if file doesn't exist
        }

        try (Stream<String> lines = Files.lines(path)) {
            Stream<String> dataLines = skipHeader ? lines.skip(1) : lines; // Skip header if requested
            data = dataLines
                    .map(line -> {
                        try {
                            return mapper.apply(line); // Apply the mapper to convert line to object
                        } catch (Exception e) {
                            System.err.println("CSVUtils: Error mapping CSV line: '" + line + "'. Skipping line. Error: " + e.getMessage());
                            return null; // Return null for lines that failed to map
                        }
                    })
                    .filter(Objects::nonNull) // Filter out any nulls from failed mappings
                    .collect(Collectors.toList());
            System.out.println("CSVUtils: Loaded " + data.size() + " records from " + filePath);
        } catch (IOException e) {
            System.err.println("CSVUtils: Error reading CSV file '" + filePath + "': " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
        return data;
    }

    /**
     * Saves a list of objects to a CSV file.
     * Each object is converted to a CSV line using the provided `serializer` function.
     *
     * @param filePath   The path to the CSV file.
     * @param data       The list of objects to save.
     * @param serializer A function that takes an object of type T and converts it to a CSV line (String).
     * @param header     The header line to write at the beginning of the CSV file. If null, no header is written.
     * @param <T>        The type of objects to save.
     */
    public static <T> void saveToCsv(String filePath, List<T> data, Function<T, String> serializer, String header) {
        Path path = Paths.get(filePath);
        System.out.println("CSVUtils: Attempting to save " + data.size() + " records to CSV: " + path.toAbsolutePath());

        // Ensure the parent directory exists
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
                System.out.println("CSVUtils: Created parent directory for CSV: " + parentDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("CSVUtils: Error creating parent directory for CSV '" + filePath + "': " + e.getMessage());
                // Don't throw, try to proceed, but log the error
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (header != null && !header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }
            for (T item : data) {
                writer.write(serializer.apply(item));
                writer.newLine();
            }
            System.out.println("CSVUtils: Successfully saved " + data.size() + " records to " + filePath);
        } catch (IOException e) {
            System.err.println("CSVUtils: Error writing to CSV file '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save data to CSV file.", e);
        }
    }
}
