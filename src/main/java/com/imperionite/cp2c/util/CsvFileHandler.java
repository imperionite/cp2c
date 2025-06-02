package com.imperionite.cp2c.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic utility for reading from and writing to CSV files.
 * This class abstracts away the file I/O details.
 *
 * @param <T> The type of object to read/write (e.g., User, Employee).
 */
public class CsvFileHandler<T> {

    private final Path filePath;
    private final Function<String, T> lineParser; // Function to parse a CSV line into an object
    private final Function<T, String> objectSerializer; // Function to serialize an object into a CSV line
    private final boolean skipHeader; // New field to indicate if the first line is a header

    public CsvFileHandler(String filename, Function<String, T> lineParser, Function<T, String> objectSerializer, boolean skipHeader) {
        // Ensure the data directory exists
        Path dataDir = Paths.get("data"); // Changed to "data" for consistent storage
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("Created data directory: " + dataDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
            throw new RuntimeException("Failed to create data directory for CSV files.", e);
        }

        this.filePath = Paths.get(dataDir.toString(), filename); // Use dataDir for path
        this.lineParser = lineParser;
        this.objectSerializer = objectSerializer;
        this.skipHeader = skipHeader; // Initialize skipHeader

        // Ensure the CSV file itself exists
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("Created CSV file: " + filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating CSV file: " + e.getMessage());
            throw new RuntimeException("Failed to create CSV file: " + filePath.getFileName(), e);
        }
    }

    /**
     * Reads all records from the CSV file.
     * If the file does not exist, an empty list is returned.
     *
     * @return A list of objects read from the CSV.
     */
    public List<T> readAll() {
        if (!Files.exists(filePath)) {
            System.out.println("CsvFileHandler: CSV file does not exist, returning empty list: " + filePath.toAbsolutePath());
            return new ArrayList<>(); // Return an empty list if file doesn't exist yet
        }

        List<T> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            if (skipHeader) {
                String headerLine = reader.readLine(); // Read and discard header
                System.out.println("CsvFileHandler: Skipping header: " + headerLine);
            }
            records = reader.lines()
                            .filter(line -> !line.trim().isEmpty()) // Ignore empty lines
                            .map(lineParser)
                            .filter(java.util.Objects::nonNull) // Filter out nulls from parser
                            .collect(Collectors.toList());
            System.out.println("CsvFileHandler: Read " + records.size() + " records from " + filePath.getFileName());

        } catch (IOException e) {
            System.err.println("CsvFileHandler: Error reading CSV file " + filePath.getFileName() + ": " + e.getMessage());
            // Optionally rethrow as a custom runtime exception if critical
        }
        return records;
    }

    /**
     * Writes a list of records to the CSV file, overwriting existing content.
     *
     * @param records The list of objects to write.
     */
    public void writeAll(List<T> records) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) {
            // Note: This method does NOT write a header. If a header is needed, use writeHeaderAndAll.
            for (T record : records) {
                writer.write(objectSerializer.apply(record));
                writer.newLine();
            }
            System.out.println("CsvFileHandler: Wrote " + records.size() + " records to " + filePath.getFileName());
        } catch (IOException e) {
            System.err.println("CsvFileHandler: Error writing to CSV file " + filePath.getFileName() + ": " + e.getMessage());
            // Optionally rethrow as a custom runtime exception if critical
        }
    }

    /**
     * Writes a header and then a list of records to the CSV file, overwriting existing content.
     * This is useful for initial file creation where a header is desired.
     *
     * @param header The header string to write as the first line.
     * @param records The list of objects to write.
     */
    public void writeHeaderAndAll(String header, List<T> records) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) { // Use TRUNCATION_EXISTING
            writer.write(header);
            writer.newLine();
            for (T record : records) {
                writer.write(objectSerializer.apply(record));
                writer.newLine();
            }
            System.out.println("CsvFileHandler: Wrote header and " + records.size() + " records to " + filePath.getFileName());
        } catch (IOException e) {
            System.err.println("CsvFileHandler: Error writing header and records to CSV file " + filePath.getFileName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to write header and records to CSV file.", e);
        }
    }
}