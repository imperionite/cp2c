package com.imperionite.cp2c.service;

import com.imperionite.cp2c.dao.EmployeeDao;
import com.imperionite.cp2c.dao.UserDao;
import com.imperionite.cp2c.dto.EmployeeSummaryDto;
import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class handling business logic for employee data.
 * Associates Employee data with User data in memory.
 */
public class EmployeeService {
    private final EmployeeDao employeeDao;
    private final UserDao userDao;
    private final AuthService authService; // Inject AuthService for cascading user deletion

    public EmployeeService(EmployeeDao employeeDao, UserDao userDao, AuthService authService) {
        this.employeeDao = employeeDao;
        this.userDao = userDao;
        this.authService = authService; // Initialize AuthService
    }

    /**
     * Retrieves a list of all employees with summarized key attributes.
     * Associates each employee with their corresponding user account in memory.
     * @return A list of EmployeeSummaryDto objects.
     */
    public List<EmployeeSummaryDto> getAllEmployeeSummaries() {
        System.out.println("EmployeeService: Starting getAllEmployeeSummaries.");
        List<Employee> employees = employeeDao.getAllEmployees();
        System.out.println("EmployeeService: Retrieved " + employees.size() + " employees from EmployeeDao.");

        List<User> users = userDao.getAllUsers();
        System.out.println("EmployeeService: Retrieved " + users.size() + " users from UserDao.");


        // Create a map of usernames to User objects for efficient lookup
        Map<String, User> userMap = users.stream()
            .filter(u -> u.getUsername().startsWith("user-")) // Only consider employee users
            .collect(Collectors.toMap(User::getUsername, user -> user));
        System.out.println("EmployeeService: Mapped " + userMap.size() + " employee-associated users for potential future use in DTOs.");

        List<EmployeeSummaryDto> summaries = employees.stream()
                .map(employee -> {
                    // This DTO does not directly expose the username, but the mapping logic is here.
                    return EmployeeSummaryDto.fromEmployee(employee);
                })
                .collect(Collectors.toList());
        System.out.println("EmployeeService: Prepared " + summaries.size() + " employee summaries.");
        return summaries;
    }

    /**
     * Retrieves full details for a specific employee by their employee number.
     * Associates the employee with their corresponding user account in memory.
     * @param employeeNumber The unique employee number.
     * @return The Employee object with all details and associated user (if found), or null if not found.
     */
    public Employee getEmployeeDetails(String employeeNumber) {
        System.out.println("EmployeeService: Starting getEmployeeDetails for employee number: " + employeeNumber);
        Employee employee = employeeDao.findByEmployeeNumber(employeeNumber);

        if (employee != null) {
            System.out.println("EmployeeService: Found employee " + employee.getEmployeeNumber() + " in DAO.");
            // Attempt to associate user in memory
            String expectedUsername = "user-" + employee.getEmployeeNumber();
            userDao.findByUsername(expectedUsername).ifPresent(user -> {
                System.out.println("EmployeeService: Found associated user " + user.getUsername() + " for employee " + employee.getEmployeeNumber());
            });
        } else {
            System.out.println("EmployeeService: Employee with number " + employeeNumber + " NOT found in DAO.");
        }
        return employee;
    }

    /**
     * Creates a new employee after validating uniqueness of key fields.
     * @param newEmployee The employee object to create.
     * @return The created Employee object if successful, null otherwise.
     */
    public Employee createEmployee(Employee newEmployee) {
        System.out.println("EmployeeService: Attempting to create employee: " + newEmployee.getEmployeeNumber());
        String validationError = validateEmployeeUniqueness(newEmployee, false); // false for creation
        if (validationError != null) {
            System.err.println("EmployeeService: Create failed due to validation error: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        try {
            employeeDao.addEmployee(newEmployee);
            System.out.println("EmployeeService: Successfully created employee: " + newEmployee.getEmployeeNumber());
            return newEmployee;
        } catch (Exception e) {
            System.err.println("EmployeeService: Error creating employee " + newEmployee.getEmployeeNumber() + ": " + e.getMessage());
            throw new RuntimeException("Failed to create employee.", e);
        }
    }

    /**
     * Updates an existing employee with partial data (PATCH operation).
     * Only fields present (non-null/non-empty strings, non-null BigDecimals) in the partialEmployee
     * will overwrite the existing employee's data.
     *
     * @param employeeNumber The employee number of the employee to update.
     * @param partialEmployee The employee object containing only the fields to be updated.
     * @return The updated Employee object if successful, null otherwise.
     */
    public Employee updateEmployee(String employeeNumber, Employee partialEmployee) {
        System.out.println("EmployeeService: Attempting to update employee: " + employeeNumber);

        // Fetch the existing employee
        Employee existingEmployee = employeeDao.findByEmployeeNumber(employeeNumber);
        if (existingEmployee == null) {
            String error = "Employee " + employeeNumber + " not found for update.";
            System.err.println("EmployeeService: Update failed: " + error);
            return null;
        }

        // Apply partial updates to the existing employee object
        // Only update fields if they are provided (not null and, for strings, not empty)
        if (partialEmployee.getLastName() != null && !partialEmployee.getLastName().isEmpty()) {
            existingEmployee.setLastName(partialEmployee.getLastName());
        }
        if (partialEmployee.getFirstName() != null && !partialEmployee.getFirstName().isEmpty()) {
            existingEmployee.setFirstName(partialEmployee.getFirstName());
        }
        if (partialEmployee.getBirthday() != null && !partialEmployee.getBirthday().isEmpty()) {
            existingEmployee.setBirthday(partialEmployee.getBirthday());
        }
        if (partialEmployee.getAddress() != null && !partialEmployee.getAddress().isEmpty()) {
            existingEmployee.setAddress(partialEmployee.getAddress());
        }
        if (partialEmployee.getPhoneNumber() != null && !partialEmployee.getPhoneNumber().isEmpty()) {
            existingEmployee.setPhoneNumber(partialEmployee.getPhoneNumber());
        }
        if (partialEmployee.getSssNumber() != null && !partialEmployee.getSssNumber().isEmpty()) {
            existingEmployee.setSssNumber(partialEmployee.getSssNumber());
        }
        if (partialEmployee.getPhilhealthNumber() != null && !partialEmployee.getPhilhealthNumber().isEmpty()) {
            existingEmployee.setPhilhealthNumber(partialEmployee.getPhilhealthNumber());
        }
        if (partialEmployee.getTinNumber() != null && !partialEmployee.getTinNumber().isEmpty()) {
            existingEmployee.setTinNumber(partialEmployee.getTinNumber());
        }
        if (partialEmployee.getPagibigNumber() != null && !partialEmployee.getPagibigNumber().isEmpty()) {
            existingEmployee.setPagibigNumber(partialEmployee.getPagibigNumber());
        }
        if (partialEmployee.getStatus() != null && !partialEmployee.getStatus().isEmpty()) {
            existingEmployee.setStatus(partialEmployee.getStatus());
        }
        if (partialEmployee.getPosition() != null && !partialEmployee.getPosition().isEmpty()) {
            existingEmployee.setPosition(partialEmployee.getPosition());
        }
        if (partialEmployee.getImmediateSupervisor() != null && !partialEmployee.getImmediateSupervisor().isEmpty()) {
            existingEmployee.setImmediateSupervisor(partialEmployee.getImmediateSupervisor());
        }
        if (partialEmployee.getBasicSalary() != null) { // BigDecimal can be 0, so check for null
            existingEmployee.setBasicSalary(partialEmployee.getBasicSalary());
        }
        if (partialEmployee.getRiceSubsidy() != null) {
            existingEmployee.setRiceSubsidy(partialEmployee.getRiceSubsidy());
        }
        if (partialEmployee.getPhoneAllowance() != null) {
            existingEmployee.setPhoneAllowance(partialEmployee.getPhoneAllowance());
        }
        if (partialEmployee.getClothingAllowance() != null) {
            existingEmployee.setClothingAllowance(partialEmployee.getClothingAllowance());
        }
        if (partialEmployee.getGrossSemiMonthlyRate() != null) {
            existingEmployee.setGrossSemiMonthlyRate(partialEmployee.getGrossSemiMonthlyRate());
        }
        if (partialEmployee.getHourlyRate() != null) {
            existingEmployee.setHourlyRate(partialEmployee.getHourlyRate());
        }

        // Validate the *merged* employee object for uniqueness
        String validationError = validateEmployeeUniqueness(existingEmployee, true); // true for update
        if (validationError != null) {
            System.err.println("EmployeeService: Update failed due to validation error: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        try {
            boolean success = employeeDao.updateEmployee(existingEmployee);
            if (success) {
                System.out.println("EmployeeService: Successfully updated employee: " + existingEmployee.getEmployeeNumber());
                return existingEmployee;
            } else {
                System.err.println("EmployeeService: Failed to update employee " + existingEmployee.getEmployeeNumber() + " (DAO reported not found).");
                return null;
            }
        } catch (Exception e) {
            System.err.println("EmployeeService: Error updating employee " + existingEmployee.getEmployeeNumber() + ": " + e.getMessage());
            throw new RuntimeException("Failed to update employee.", e);
        }
    }

    /**
     * Deletes an employee by their employee number and also deletes the associated user.
     * @param employeeNumber The employee number of the employee to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteEmployee(String employeeNumber) {
        System.out.println("EmployeeService: Attempting to delete employee: " + employeeNumber);
        try {
            // First, delete the employee from the employee data store
            boolean employeeDeleted = employeeDao.deleteEmployee(employeeNumber);

            if (employeeDeleted) {
                System.out.println("EmployeeService: Employee " + employeeNumber + " deleted successfully. Now attempting to delete associated user.");
                // Construct the username for the associated user
                String associatedUsername = "user-" + employeeNumber;
                Optional<User> userOptional = userDao.findByUsername(associatedUsername);

                if (userOptional.isPresent()) {
                    String userIdToDelete = userOptional.get().getId();
                    boolean userDeleted = authService.deleteUser(userIdToDelete); // Call AuthService to delete the user
                    if (userDeleted) {
                        System.out.println("EmployeeService: Successfully deleted associated user '" + associatedUsername + "'.");
                    } else {
                        System.err.println("EmployeeService: Failed to delete associated user '" + associatedUsername + "' (ID: " + userIdToDelete + "). This might require manual cleanup.");
                    }
                } else {
                    System.out.println("EmployeeService: No associated user found for employee " + employeeNumber + ". Skipping user deletion.");
                }
                return true; // Employee was deleted, even if user deletion failed
            } else {
                System.out.println("EmployeeService: Employee " + employeeNumber + " not found for deletion.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("EmployeeService: Error deleting employee " + employeeNumber + " or associated user: " + e.getMessage());
            throw new RuntimeException("Failed to delete employee and/or associated user.", e);
        }
    }

    /**
     * Validates the uniqueness of key employee fields.
     *
     * @param employee The employee object to validate (can be a merged object for updates).
     * @param isUpdate True if this is an update operation, false for creation.
     * @return A String containing an error message if validation fails, or null if successful.
     */
    private String validateEmployeeUniqueness(Employee employee, boolean isUpdate) {
        List<Employee> allExistingEmployees = employeeDao.getAllEmployees();

        List<Employee> otherEmployees;
        if (isUpdate) {
            // For update, filter out the employee being updated based on its employeeNumber
            otherEmployees = allExistingEmployees.stream()
                .filter(e -> !e.getEmployeeNumber().equals(employee.getEmployeeNumber()))
                .collect(Collectors.toList());
        } else {
            // For creation, all existing employees are "other employees"
            otherEmployees = allExistingEmployees;
        }

        // Validate Employee Number uniqueness against other employees
        // This correctly handles both creation (new employee number must not exist)
        // and update (if employee number is changed, new number must not exist among others)
        if (otherEmployees.stream().anyMatch(e -> e.getEmployeeNumber().equals(employee.getEmployeeNumber()))) {
             return "Employee Number " + employee.getEmployeeNumber() + " already exists.";
        }


        // Validate uniqueness for other fields (SSS, PhilHealth, TIN, Pag-ibig, Phone Number)
        // These checks should ignore the employee being updated if it's an update operation.
        // We compare against all other employees.
        if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isEmpty() &&
            otherEmployees.stream().anyMatch(e -> e.getPhoneNumber().equals(employee.getPhoneNumber()))) {
            return "Phone Number " + employee.getPhoneNumber() + " already exists.";
        }
        if (employee.getSssNumber() != null && !employee.getSssNumber().isEmpty() &&
            otherEmployees.stream().anyMatch(e -> e.getSssNumber().equals(employee.getSssNumber()))) {
            return "SSS Number " + employee.getSssNumber() + " already exists.";
        }
        if (employee.getPhilhealthNumber() != null && !employee.getPhilhealthNumber().isEmpty() &&
            otherEmployees.stream().anyMatch(e -> e.getPhilhealthNumber().equals(employee.getPhilhealthNumber()))) {
            return "PhilHealth Number " + employee.getPhilhealthNumber() + " already exists.";
        }
        if (employee.getTinNumber() != null && !employee.getTinNumber().isEmpty() &&
            otherEmployees.stream().anyMatch(e -> e.getTinNumber().equals(employee.getTinNumber()))) {
            return "TIN Number " + employee.getTinNumber() + " already exists.";
        }
        if (employee.getPagibigNumber() != null && !employee.getPagibigNumber().isEmpty() &&
            otherEmployees.stream().anyMatch(e -> e.getPagibigNumber().equals(employee.getPagibigNumber()))) {
            return "Pag-ibig Number " + employee.getPagibigNumber() + " already exists.";
        }

        return null; // All uniqueness checks passed
    }
}