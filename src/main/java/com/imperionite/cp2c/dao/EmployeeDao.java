package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.Employee;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
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
    // Delimiter for CSV file
    private static final String CSV_DELIMITER = ",";

    public EmployeeDao(String filePath) {
        this.filePath = filePath;
        // Load existing employees from CSV on initialization
        this.employees = loadEmployeesFromCsv();
        System.out.println("EmployeeDao: Initialized with " + employees.size() + " employees loaded from " + filePath);
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
            String[] parts = line.split(Pattern.quote(CSV_DELIMITER), -1); // -1 to keep trailing empty strings
            // Ensure all expected parts are present (19 fields)
            if (parts.length < 19) {
                System.err.println("EmployeeDao: Skipping malformed CSV line (expected 19 parts, got " + parts.length + "): " + line);
                return null;
            }

            // Parse BigDecimal values safely - FIX: Remove commas AND quotes
            BigDecimal basicSalary = parseBigDecimal(parts[13], "basicSalary");
            BigDecimal riceSubsidy = parseBigDecimal(parts[14], "riceSubsidy");
            BigDecimal phoneAllowance = parseBigDecimal(parts[15], "phoneAllowance");
            BigDecimal clothingAllowance = parseBigDecimal(parts[16], "clothingAllowance");
            BigDecimal grossSemiMonthlyRate = parseBigDecimal(parts[17], "grossSemiMonthlyRate");
            BigDecimal hourlyRate = parseBigDecimal(parts[18], "hourlyRate");

            return new Employee(
                    parts[0], // employeeNumber
                    parts[1], // lastName
                    parts[2], // firstName
                    parts[3], // birthday
                    parts[4], // address
                    parts[5], // phoneNumber
                    parts[6], // sssNumber
                    parts[7], // philhealthNumber
                    parts[8], // tinNumber
                    parts[9], // pagibigNumber
                    parts[10], // status
                    parts[11], // position
                    parts[12], // immediateSupervisor
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
     * FIX: Added removal of commas and quotation marks.
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            // System.out.println("EmployeeDao: " + fieldName + " is empty or null, using BigDecimal.ZERO.");
            return BigDecimal.ZERO;
        }
        try {
            // Remove commas and quotation marks before parsing
            String cleanValue = value.trim().replace(",", "").replace("\"", "");
            System.out.println("DEBUG: Parsing " + fieldName + ": '" + value + "' -> cleaned to '" + cleanValue + "'"); // Added debug
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
        // Use Optional.ofNullable to handle potential null BigDecimal values, converting them to "0" for CSV.
        // It's also possible to convert null BigDecimals to empty strings for CSV, but "0" is often safer for numeric fields.
        return String.join(CSV_DELIMITER,
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
