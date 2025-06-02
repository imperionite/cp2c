package com.imperionite.cp2c.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for reading CSV files from the application's resources directory.
 * This is specifically for static CSV files bundled with the application.
 * Now primarily used for initial copying of static resources to a writable data directory.
 */
public class ResourceCsvReader {

    /**
     * Reads a CSV file from the resources directory and parses each line into objects of type T.
     *
     * @param <T> The type of object to parse each CSV line into.
     * @param resourceName The name of the CSV file in the resources directory (e.g., "data.csv").
     * @param lineParser A function that takes a String (CSV line) and returns an object of type T.
     * @param skipHeader True if the first line of the CSV should be skipped (header row), false otherwise.
     * @return A list of objects of type T read from the CSV.
     * @throws RuntimeException if the resource is not found or an I/O error occurs.
     */
    public static <T> List<T> readCsv(String resourceName, Function<String, T> lineParser, boolean skipHeader) {
        System.out.println("ResourceCsvReader: Attempting to read resource: " + resourceName);
        List<T> records = new ArrayList<>();
        int lineCount = 0;
        int parsedCount = 0;

        try (InputStream is = ResourceCsvReader.class.getClassLoader().getResourceAsStream(resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                System.err.println("ResourceCsvReader: CRITICAL: Resource not found: " + resourceName + ". Double-check path and if it's included in JAR.");
                throw new FileNotFoundException("ResourceCsvReader: Resource not found: " + resourceName + ". Please ensure it's in src/main/resources directory or the classpath.");
            }
            System.out.println("ResourceCsvReader: Resource stream opened for: " + resourceName);

            if (skipHeader) {
                String headerLine = reader.readLine(); // Read and discard header
                if (headerLine != null) {
                    System.out.println("ResourceCsvReader: Skipping header: '" + headerLine + "'");
                } else {
                    System.out.println("ResourceCsvReader: No header line found or file is empty.");
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.trim().isEmpty()) {
                    System.out.println("ResourceCsvReader: Skipping empty line at line " + lineCount + ".");
                    continue;
                }
                // Removed verbose logging for every line here, as it's used for initial load.
                // Employee.fromCsvLine itself has verbose logging.
                T record = lineParser.apply(line);
                if (record != null) {
                    records.add(record);
                    parsedCount++;
                } else {
                    System.err.println("ResourceCsvReader: Failed to parse record from line " + lineCount + ". Parser returned null.");
                }
            }
            System.out.println("ResourceCsvReader: Finished reading. Total lines read: " + lineCount + ", Successfully parsed records: " + parsedCount);

        } catch (IOException e) {
            System.err.println("ResourceCsvReader: CRITICAL ERROR reading CSV resource '" + resourceName + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read data from resource: " + resourceName, e);
        }
        return records;
    }

    /**
     * Copies a resource file from the classpath to a target file path.
     * Used for initializing writable data files from static resources.
     * @param resourceName The name of the resource on the classpath.
     * @param targetFilePath The target file path to copy to.
     * @throws IOException if the resource cannot be read or the file cannot be written.
     */
    public static void copyResourceToFile(String resourceName, java.nio.file.Path targetFilePath) throws IOException {
        System.out.println("ResourceCsvReader: Attempting to copy resource '" + resourceName + "' to '" + targetFilePath + "'");
        try (InputStream is = ResourceCsvReader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found on classpath: " + resourceName);
            }
            Files.copy(is, targetFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("ResourceCsvReader: Successfully copied resource '" + resourceName + "' to '" + targetFilePath + "'");
        }
    }
}