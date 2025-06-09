package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.Employee;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.csv.CSVFormat; // Import Apache Commons CSV
import org.apache.commons.csv.CSVParser; // Import Apache Commons CSV
import org.apache.commons.csv.CSVPrinter; // Import Apache Commons CSV
import org.apache.commons.csv.CSVRecord; // Import Apache Commons CSV

/**
 * Data Access Object for Employee entities, managing persistence to a CSV file.
 * This class handles reading Employee data from and writing to `employees.csv`
 * using the Apache Commons CSV library for robust parsing and writing.
 * It includes basic in-memory caching and thread-safety for concurrent access.
 */
public class EmployeeDao {
    private final String filePath;
    private final List<Employee> employees; // In-memory cache of employees
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // For thread-safe access

    // CSV header for the employees file
    private static final String[] CSV_HEADERS_ARRAY = {
            "employeeNumber", "lastName", "firstName", "birthday", "address",
            "phoneNumber", "sssNumber", "philhealthNumber", "tinNumber", "pagibigNumber",
            "status", "position", "immediateSupervisor", "basicSalary", "riceSubsidy",
            "phoneAllowance", "clothingAllowance", "grossSemiMonthlyRate", "hourlyRate"
    };
    // CSVFormat for reading and writing, configured with headers and quoting
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(CSV_HEADERS_ARRAY) // Specify headers for reading and writing
            .setSkipHeaderRecord(true)    // Skip header when reading existing files
            .setQuoteMode(org.apache.commons.csv.QuoteMode.MINIMAL) // Quote fields only when necessary (e.g., if they contain commas)
            .setTrim(true)                // Trim leading/trailing whitespace from fields
            .build();


    public EmployeeDao(String filePath) throws IOException {
        this.filePath = filePath;
        // Ensure the CSV file exists with its header when the DAO is initialized
        initializeCsvFile();
        // Load existing employees from CSV on initialization
        this.employees = loadEmployeesFromCsv();
        System.out.println("EmployeeDao: Initialized with " + employees.size() + " employees loaded from " + filePath);
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
                    System.out.println("EmployeeDao: Created parent directory for employees CSV: " + parentDir.toAbsolutePath());
                }

                // Write only the header using CSVPrinter, then close
                try (FileWriter fileWriter = new FileWriter(path.toFile());
                     CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder().setHeader(CSV_HEADERS_ARRAY).build())) {
                    // No need to print records, just the header is implicitly written by setHeader()
                    System.out.println("EmployeeDao: Created new employees CSV file with header: " + path.toAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("EmployeeDao: Error creating employees CSV file '" + filePath + "': " + e.getMessage());
                // Rethrow as runtime exception since persistence won't work without the file
                throw new RuntimeException("Failed to initialize employees CSV file.", e);
            }
        }
    }

    /**
     * Loads employees from the CSV file using Apache Commons CSV.
     *
     * @return A list of Employee objects.
     * @throws IOException 
     */
    private List<Employee> loadEmployeesFromCsv() throws IOException {
        List<Employee> data = new ArrayList<>();
        Path path = Paths.get(filePath);
        System.out.println("EmployeeDao: Attempting to load from CSV: " + path.toAbsolutePath());

        if (!Files.exists(path) || !Files.isReadable(path) || Files.size(path) == 0) {
            System.out.println("EmployeeDao: CSV file not found, not readable, or empty: " + path.toAbsolutePath() + ". Returning empty list.");
            return data;
        }

        try (FileReader fileReader = new FileReader(path.toFile());
             CSVParser csvParser = new CSVParser(fileReader, CSV_FORMAT)) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    data.add(mapCsvRecordToEmployee(csvRecord));
                } catch (Exception e) {
                    System.err.println("EmployeeDao: Error mapping CSV record: '" + csvRecord + "'. Skipping record. Error: " + e.getMessage());
                }
            }
            System.out.println("EmployeeDao: Loaded " + data.size() + " records from " + filePath);
        } catch (IOException e) {
            System.err.println("EmployeeDao: Error reading CSV file '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Maps a single CSVRecord to an Employee object.
     *
     * @param record The CSVRecord.
     * @return An Employee object.
     * @throws IllegalArgumentException if required fields are missing or parsing errors occur.
     */
    private Employee mapCsvRecordToEmployee(CSVRecord record) {
        // Access fields by header name for robustness (less error-prone than by index)
        String employeeNumber = record.get("employeeNumber");
        String lastName = record.get("lastName");
        String firstName = record.get("firstName");
        String birthday = record.get("birthday");
        String address = record.get("address");
        String phoneNumber = record.get("phoneNumber");
        String sssNumber = record.get("sssNumber");
        String philhealthNumber = record.get("philhealthNumber");
        String tinNumber = record.get("tinNumber");
        String pagibigNumber = record.get("pagibigNumber");
        String status = record.get("status");
        String position = record.get("position");
        String immediateSupervisor = record.get("immediateSupervisor");

        // Parse BigDecimal values safely
        BigDecimal basicSalary = parseBigDecimal(record.get("basicSalary"), "basicSalary");
        BigDecimal riceSubsidy = parseBigDecimal(record.get("riceSubsidy"), "riceSubsidy");
        BigDecimal phoneAllowance = parseBigDecimal(record.get("phoneAllowance"), "phoneAllowance");
        BigDecimal clothingAllowance = parseBigDecimal(record.get("clothingAllowance"), "clothingAllowance");
        BigDecimal grossSemiMonthlyRate = parseBigDecimal(record.get("grossSemiMonthlyRate"), "grossSemiMonthlyRate");
        BigDecimal hourlyRate = parseBigDecimal(record.get("hourlyRate"), "hourlyRate");

        return new Employee(
                employeeNumber, lastName, firstName, birthday, address,
                phoneNumber, sssNumber, philhealthNumber, tinNumber, pagibigNumber,
                status, position, immediateSupervisor, basicSalary, riceSubsidy,
                phoneAllowance, clothingAllowance, grossSemiMonthlyRate, hourlyRate
        );
    }

    /**
     * Helper method to safely parse BigDecimal values from string parts.
     * Returns BigDecimal.ZERO if the part is empty or cannot be parsed.
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("EmployeeDao: Warning: Could not parse " + fieldName + " value '" + value + "'. Using BigDecimal.ZERO. Error: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Saves the current list of employees to the CSV file using Apache Commons CSV.
     * This method is called after any modification (add, update, delete).
     */
    private void saveEmployeesToCsv() {
        Path path = Paths.get(filePath);
        System.out.println("EmployeeDao: Attempting to save " + employees.size() + " records to CSV: " + path.toAbsolutePath());

        // Ensure the parent directory exists
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
                System.out.println("EmployeeDao: Created parent directory for CSV: " + parentDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("EmployeeDao: Error creating parent directory for CSV '" + filePath + "': " + e.getMessage());
            }
        }

        try (FileWriter fileWriter = new FileWriter(path.toFile());
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSV_FORMAT)) {

            for (Employee employee : employees) {
                csvPrinter.printRecord(
                        employee.getEmployeeNumber(),
                        employee.getLastName(),
                        employee.getFirstName(),
                        employee.getBirthday(),
                        employee.getAddress(),
                        employee.getPhoneNumber(),
                        employee.getSssNumber(),
                        employee.getPhilhealthNumber(),
                        employee.getTinNumber(),
                        employee.getPagibigNumber(),
                        employee.getStatus(),
                        employee.getPosition(),
                        employee.getImmediateSupervisor(),
                        Optional.ofNullable(employee.getBasicSalary()).orElse(BigDecimal.ZERO).toPlainString(),
                        Optional.ofNullable(employee.getRiceSubsidy()).orElse(BigDecimal.ZERO).toPlainString(),
                        Optional.ofNullable(employee.getPhoneAllowance()).orElse(BigDecimal.ZERO).toPlainString(),
                        Optional.ofNullable(employee.getClothingAllowance()).orElse(BigDecimal.ZERO).toPlainString(),
                        Optional.ofNullable(employee.getGrossSemiMonthlyRate()).orElse(BigDecimal.ZERO).toPlainString(),
                        Optional.ofNullable(employee.getHourlyRate()).orElse(BigDecimal.ZERO).toPlainString()
                );
            }
            csvPrinter.flush(); // Ensure all data is written to the file
            System.out.println("EmployeeDao: Successfully saved " + employees.size() + " records to " + filePath);
        } catch (IOException e) {
            System.err.println("EmployeeDao: Error writing to CSV file '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save data to CSV file.", e);
        }
    }


    /**
     * Adds a new employee to the in-memory list and persists to CSV.
     *
     * @param employee The Employee object to add.
     */
    public void addEmployee(Employee employee) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            employees.add(employee);
            saveEmployeesToCsv(); // Persist changes
            System.out.println("EmployeeDao: Added new employee: " + employee.getEmployeeNumber());
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Finds an employee by their employee number.
     *
     * @param employeeNumber The employee number to search for.
     * @return The Employee object if found, null otherwise.
     */
    public Employee findByEmployeeNumber(String employeeNumber) {
        lock.readLock().lock(); // Acquire read lock
        try {
            return employees.stream()
                    .filter(e -> e.getEmployeeNumber().equals(employeeNumber))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Updates an existing employee in the in-memory list and persists to CSV.
     *
     * @param updatedEmployee The Employee object with updated details.
     * @return true if the employee was found and updated, false otherwise.
     */
    public boolean updateEmployee(Employee updatedEmployee) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            for (int i = 0; i < employees.size(); i++) {
                if (employees.get(i).getEmployeeNumber().equals(updatedEmployee.getEmployeeNumber())) {
                    employees.set(i, updatedEmployee);
                    saveEmployeesToCsv(); // Persist changes
                    System.out.println("EmployeeDao: Updated employee: " + updatedEmployee.getEmployeeNumber());
                    return true;
                }
            }
            System.out.println("EmployeeDao: Employee " + updatedEmployee.getEmployeeNumber() + " not found for update.");
            return false;
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Deletes an employee by their employee number from the in-memory list and persists to CSV.
     *
     * @param employeeNumber The employee number of the employee to delete.
     * @return true if the employee was deleted, false otherwise.
     */
    public boolean deleteEmployee(String employeeNumber) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            boolean removed = employees.removeIf(employee -> employee.getEmployeeNumber().equals(employeeNumber));
            if (removed) {
                saveEmployeesToCsv(); // Persist changes
                System.out.println("EmployeeDao: Deleted employee: " + employeeNumber);
            } else {
                System.out.println("EmployeeDao: Employee " + employeeNumber + " not found for deletion.");
            }
            return removed;
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Retrieves an unmodifiable list of all employees.
     *
     * @return A list of all Employee objects.
     */
    public List<Employee> getAllEmployees() {
        lock.readLock().lock(); // Acquire read lock
        try {
            // Return an unmodifiable list to prevent external modification of the internal cache
            return Collections.unmodifiableList(new ArrayList<>(employees));
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }
}
