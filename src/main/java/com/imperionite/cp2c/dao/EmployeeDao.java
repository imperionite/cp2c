package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.Employee;
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

/**
 * Data Access Object for Employee entities, managing persistence to a CSV file.
 * This class handles reading Employee data from and writing to `employees.csv`.
 * It includes basic in-memory caching and thread-safety for concurrent access.
 */
public class EmployeeDao {
    private final String filePath;
    private final List<Employee> employees; // In-memory cache of employees
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // For thread-safe access

    // CSV header for the employees file
    private static final String CSV_HEADER = "employeeNumber,lastName,firstName,birthday,address,phoneNumber,sssNumber,philhealthNumber,tinNumber,pagibigNumber,status,position,immediateSupervisor,basicSalary,riceSubsidy,phoneAllowance,clothingAllowance,grossSemiMonthlyRate,hourlyRate";
    // Regex for splitting CSV lines, handling commas within double quotes.
    // This regex splits on a comma that is NOT followed by an even number of quotation marks until the end of the line.
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";


    public EmployeeDao(String filePath) {
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
                Files.writeString(path, CSV_HEADER + System.lineSeparator());
                System.out.println("EmployeeDao: Created new employees CSV file with header: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("EmployeeDao: Error creating employees CSV file '" + filePath + "': " + e.getMessage());
                // Rethrow as runtime exception since persistence won't work without the file
                throw new RuntimeException("Failed to initialize employees CSV file.", e);
            }
        }
    }

    /**
     * Loads employees from the CSV file.
     *
     * @return A list of Employee objects.
     */
    private List<Employee> loadEmployeesFromCsv() {
        // Use CSVUtils to read lines and map them to Employee objects
        return CSVUtils.loadFromCsv(filePath, this::mapCsvLineToEmployee, true); // true to skip header
    }

    /**
     * Maps a single CSV line string to an Employee object.
     * Handles potential parsing errors, especially for BigDecimal fields.
     *
     * @param line The CSV line string.
     * @return An Employee object, or null if parsing fails.
     */
    private Employee mapCsvLineToEmployee(String line) {
        try {
            // Use the more robust regex to split the line, handling commas within quotes
            String[] parts = line.split(CSV_SPLIT_REGEX, -1);
            // Ensure all expected parts are present (19 fields based on CSV_HEADER)
            if (parts.length < 19) {
                System.err.println("EmployeeDao: Skipping malformed CSV line (expected 19 parts, got " + parts.length + "): " + line);
                return null;
            }

            // Trim parts and remove surrounding quotes for string fields that might have them
            String employeeNumber = parts[0].trim();
            String lastName = parts[1].trim();
            String firstName = parts[2].trim();
            String birthday = parts[3].trim();
            String address = parts[4].trim().replace("\"", ""); // Remove outer quotes for address
            String phoneNumber = parts[5].trim();
            String sssNumber = parts[6].trim();
            String philhealthNumber = parts[7].trim();
            String tinNumber = parts[8].trim();
            String pagibigNumber = parts[9].trim();
            String status = parts[10].trim();
            String position = parts[11].trim();
            String immediateSupervisor = parts[12].trim().replace("\"", ""); // Immediate supervisor might also be quoted

            // Parse BigDecimal values safely - ENSURE COMMAS AND QUOTES ARE REMOVED
            BigDecimal basicSalary = parseBigDecimal(parts[13], "basicSalary");
            BigDecimal riceSubsidy = parseBigDecimal(parts[14], "riceSubsidy");
            BigDecimal phoneAllowance = parseBigDecimal(parts[15], "phoneAllowance");
            BigDecimal clothingAllowance = parseBigDecimal(parts[16], "clothingAllowance");
            BigDecimal grossSemiMonthlyRate = parseBigDecimal(parts[17], "grossSemiMonthlyRate");
            BigDecimal hourlyRate = parseBigDecimal(parts[18], "hourlyRate");

            return new Employee(
                    employeeNumber,
                    lastName,
                    firstName,
                    birthday,
                    address,
                    phoneNumber,
                    sssNumber,
                    philhealthNumber,
                    tinNumber,
                    pagibigNumber,
                    status,
                    position,
                    immediateSupervisor,
                    basicSalary,
                    riceSubsidy,
                    phoneAllowance,
                    clothingAllowance,
                    grossSemiMonthlyRate,
                    hourlyRate
            );
        } catch (Exception e) {
            System.err.println("EmployeeDao: Error parsing CSV line to Employee: '" + line + "'. Error: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null for unparseable lines
        }
    }

    /**
     * Helper method to safely parse BigDecimal values from string parts.
     * Returns BigDecimal.ZERO if the part is empty or cannot be parsed.
     * IMPORTANT: This must remove commas and quotation marks before parsing.
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Remove commas and quotation marks before parsing
            String cleanValue = value.trim().replace(",", "").replace("\"", "");
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("EmployeeDao: Warning: Could not parse " + fieldName + " value '" + value + "'. Using BigDecimal.ZERO. Error: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Saves the current list of employees to the CSV file.
     * This method is called after any modification (add, update, delete).
     */
    private void saveEmployeesToCsv() {
        // Use CSVUtils to write Employee objects to CSV
        CSVUtils.saveToCsv(filePath, employees, this::mapEmployeeToCsvLine, CSV_HEADER);
    }

    /**
     * Maps an Employee object to a single CSV line string.
     *
     * @param employee The Employee object.
     * @return A CSV formatted string.
     */
    private String mapEmployeeToCsvLine(Employee employee) {
        // The Optional.ofNullable to handle potential null BigDecimal values, converting them to "0" for CSV was used.
        // Also ensure that string fields that might contain commas are quoted before writing to CSV.
        return String.join(",",
                quoteCsvField(employee.getEmployeeNumber()),
                quoteCsvField(employee.getLastName()),
                quoteCsvField(employee.getFirstName()),
                quoteCsvField(employee.getBirthday()),
                quoteCsvField(employee.getAddress()),
                quoteCsvField(employee.getPhoneNumber()),
                quoteCsvField(employee.getSssNumber()),
                quoteCsvField(employee.getPhilhealthNumber()),
                quoteCsvField(employee.getTinNumber()),
                quoteCsvField(employee.getPagibigNumber()),
                quoteCsvField(employee.getStatus()),
                quoteCsvField(employee.getPosition()),
                quoteCsvField(employee.getImmediateSupervisor()),
                Optional.ofNullable(employee.getBasicSalary()).orElse(BigDecimal.ZERO).toPlainString(),
                Optional.ofNullable(employee.getRiceSubsidy()).orElse(BigDecimal.ZERO).toPlainString(),
                Optional.ofNullable(employee.getPhoneAllowance()).orElse(BigDecimal.ZERO).toPlainString(),
                Optional.ofNullable(employee.getClothingAllowance()).orElse(BigDecimal.ZERO).toPlainString(),
                Optional.ofNullable(employee.getGrossSemiMonthlyRate()).orElse(BigDecimal.ZERO).toPlainString(),
                Optional.ofNullable(employee.getHourlyRate()).orElse(BigDecimal.ZERO).toPlainString()
        );
    }

    /**
     * Helper to quote a CSV field if it contains commas or double quotes.
     * Double quotes within the field are escaped by doubling them.
     */
    private String quoteCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Check if field contains comma, double quote, or newline
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // Escape double quotes by doubling them, then enclose in double quotes
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
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
