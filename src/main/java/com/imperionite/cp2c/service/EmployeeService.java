package com.imperionite.cp2c.service;

import com.imperionite.cp2c.dao.EmployeeDao;
import com.imperionite.cp2c.dao.UserDao; // Needed for user lookup, though not directly used in employee creation/update
import com.imperionite.cp2c.dto.EmployeeSummaryDto;
import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class handling business logic for employee data.
 * Interacts with EmployeeDao for employee persistence and AuthService for cascading user deletion.
 * Includes comprehensive validation logic.
 */
public class EmployeeService {
    private final EmployeeDao employeeDao;
    private final UserDao userDao; // Injected for consistency and potential future direct user lookup
    private final AuthService authService; // Injected for cascading user deletion

    public EmployeeService(EmployeeDao employeeDao, UserDao userDao, AuthService authService) {
        this.employeeDao = employeeDao;
        this.userDao = userDao;
        this.authService = authService;
    }

    /**
     * Retrieves a list of all employees with summarized key attributes.
     * Currently, user association for summary is not directly in DTO, but the service has access to both.
     *
     * @return A list of EmployeeSummaryDto objects.
     */
    public List<EmployeeSummaryDto> getAllEmployeeSummaries() {
        System.out.println("EmployeeService: Starting getAllEmployeeSummaries.");
        List<Employee> employees = employeeDao.getAllEmployees();
        System.out.println("EmployeeService: Retrieved " + employees.size() + " employees from EmployeeDao.");

        // If you needed to include user-specific info in the summary DTO,
        // you would fetch users here and combine them, e.g.:
        // List<User> users = userDao.getAllUsers();
        // Map<String, User> userMap = users.stream()
        //         .filter(u -> u.getUsername().startsWith("user-"))
        //         .collect(Collectors.toMap(User::getUsername, user -> user));

        List<EmployeeSummaryDto> summaries = employees.stream()
                .map(EmployeeSummaryDto::fromEmployee) // Use the static factory method
                .collect(Collectors.toList());
        System.out.println("EmployeeService: Prepared " + summaries.size() + " employee summaries.");
        return summaries;
    }

    /**
     * Retrieves a list of all raw Employee objects from the DAO.
     * This method is added specifically for the seeding logic in Main.java.
     *
     * @return A list of all Employee objects.
     */
    public List<Employee> getAllEmployees() {
        System.out.println("EmployeeService: Starting getAllEmployees (raw data for seeding).");
        return employeeDao.getAllEmployees();
    }


    /**
     * Retrieves full details for a specific employee by their employee number.
     *
     * @param employeeNumber The unique employee number.
     * @return The Employee object with all details, or null if not found.
     */
    public Employee getEmployeeDetails(String employeeNumber) {
        System.out.println("EmployeeService: Starting getEmployeeDetails for employee number: " + employeeNumber);
        Employee employee = employeeDao.findByEmployeeNumber(employeeNumber);

        if (employee != null) {
            System.out.println("EmployeeService: Found employee " + employee.getEmployeeNumber() + " in DAO.");
            // Example of associating user data (though not returned in this method's signature)
            String expectedUsername = "user-" + employee.getEmployeeNumber();
            userDao.findByUsername(expectedUsername).ifPresent(user -> {
                System.out.println("EmployeeService: Found associated user " + user.getUsername() + " for employee "
                        + employee.getEmployeeNumber());
            });
        } else {
            System.out.println("EmployeeService: Employee with number " + employeeNumber + " NOT found in DAO.");
        }
        return employee;
    }

    /**
     * Creates a new employee after validating uniqueness of key fields.
     * Also creates an associated user for this employee in the authentication system.
     *
     * @param newEmployee The employee object to create.
     * @return The created Employee object if successful.
     * @throws IllegalArgumentException if validation fails.
     * @throws RuntimeException if creation fails.
     */
    public Employee createEmployee(Employee newEmployee) {
        System.out.println("EmployeeService: Attempting to create employee: " + newEmployee.getEmployeeNumber());

        // Basic validation for critical fields on creation
        if (newEmployee.getEmployeeNumber() == null || newEmployee.getEmployeeNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee Number is required.");
        }
        if (newEmployee.getFirstName() == null || newEmployee.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First Name is required.");
        }
        if (newEmployee.getLastName() == null || newEmployee.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last Name is required.");
        }

        // Validate uniqueness against existing employees (false for creation)
        String validationError = validateEmployeeUniqueness(newEmployee, false);
        if (validationError != null) {
            System.err.println("EmployeeService: Create failed due to validation error: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        try {
            employeeDao.addEmployee(newEmployee);
            System.out.println("EmployeeService: Successfully created employee: " + newEmployee.getEmployeeNumber());

            // Automatically create a user for this employee with a default password.
            // You might want a more sophisticated way to handle initial passwords in a real app.
            String associatedUsername = "user-" + newEmployee.getEmployeeNumber();
            String defaultPassword = "userPassword"; // Consider generating a strong random password or prompting admin
            authService.registerUser(associatedUsername, defaultPassword); // This call also handles username format/uniqueness

            System.out.println("EmployeeService: Successfully created associated user '" + associatedUsername + "' for employee " + newEmployee.getEmployeeNumber());
            return newEmployee;
        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException from authService.registerUser (e.g., if username format fails unexpectedly)
            System.err.println("EmployeeService: Error creating associated user for employee " + newEmployee.getEmployeeNumber() + ": " + e.getMessage());
            throw new IllegalArgumentException("Failed to create employee due to user registration error: " + e.getMessage());
        }
        catch (Exception e) {
            System.err.println("EmployeeService: Error creating employee " + newEmployee.getEmployeeNumber() + ": "
                    + e.getMessage());
            throw new RuntimeException("Failed to create employee and/or associated user.", e);
        }
    }

    /**
     * Updates an existing employee with partial data (PATCH operation).
     * Only fields present (non-null/non-empty strings, non-null BigDecimals) in the
     * partialEmployee will overwrite the existing employee's data.
     *
     * @param employeeNumber  The employee number of the employee to update.
     * @param partialEmployee The employee object containing only the fields to be updated.
     * @return The updated Employee object if successful, null otherwise.
     * @throws IllegalArgumentException if validation fails.
     * @throws RuntimeException if update fails.
     */
    public Employee updateEmployee(String employeeNumber, Employee partialEmployee) {
        System.out.println("EmployeeService: Attempting to update employee: " + employeeNumber);

        // Fetch the existing employee
        Employee existingEmployee = employeeDao.findByEmployeeNumber(employeeNumber);
        if (existingEmployee == null) {
            String error = "Employee " + employeeNumber + " not found for update.";
            System.err.println("EmployeeService: Update failed: " + error);
            return null; // Return null if employee not found
        }

        // Apply partial updates to the existing employee object
        // Use Optional.ofNullable for cleaner null checks and default values
        existingEmployee.setLastName(Optional.ofNullable(partialEmployee.getLastName())
                                            .filter(s -> !s.isEmpty())
                                            .orElse(existingEmployee.getLastName()));
        existingEmployee.setFirstName(Optional.ofNullable(partialEmployee.getFirstName())
                                             .filter(s -> !s.isEmpty())
                                             .orElse(existingEmployee.getFirstName()));
        existingEmployee.setBirthday(Optional.ofNullable(partialEmployee.getBirthday())
                                            .filter(s -> !s.isEmpty())
                                            .orElse(existingEmployee.getBirthday()));
        existingEmployee.setAddress(Optional.ofNullable(partialEmployee.getAddress())
                                           .filter(s -> !s.isEmpty())
                                           .orElse(existingEmployee.getAddress()));
        existingEmployee.setPhoneNumber(Optional.ofNullable(partialEmployee.getPhoneNumber())
                                               .filter(s -> !s.isEmpty())
                                               .orElse(existingEmployee.getPhoneNumber()));
        existingEmployee.setSssNumber(Optional.ofNullable(partialEmployee.getSssNumber())
                                             .filter(s -> !s.isEmpty())
                                             .orElse(existingEmployee.getSssNumber()));
        existingEmployee.setPhilhealthNumber(Optional.ofNullable(partialEmployee.getPhilhealthNumber())
                                                   .filter(s -> !s.isEmpty())
                                                   .orElse(existingEmployee.getPhilhealthNumber()));
        existingEmployee.setTinNumber(Optional.ofNullable(partialEmployee.getTinNumber())
                                            .filter(s -> !s.isEmpty())
                                            .orElse(existingEmployee.getTinNumber()));
        existingEmployee.setPagibigNumber(Optional.ofNullable(partialEmployee.getPagibigNumber())
                                                 .filter(s -> !s.isEmpty())
                                                 .orElse(existingEmployee.getPagibigNumber()));
        existingEmployee.setStatus(Optional.ofNullable(partialEmployee.getStatus())
                                          .filter(s -> !s.isEmpty())
                                          .orElse(existingEmployee.getStatus()));
        existingEmployee.setPosition(Optional.ofNullable(partialEmployee.getPosition())
                                            .filter(s -> !s.isEmpty())
                                            .orElse(existingEmployee.getPosition()));
        existingEmployee.setImmediateSupervisor(Optional.ofNullable(partialEmployee.getImmediateSupervisor())
                                                       .filter(s -> !s.isEmpty())
                                                       .orElse(existingEmployee.getImmediateSupervisor()));

        // For BigDecimals, check for null explicitly as BigDecimal.ZERO is a valid value
        existingEmployee.setBasicSalary(Optional.ofNullable(partialEmployee.getBasicSalary())
                                                .orElse(existingEmployee.getBasicSalary()));
        existingEmployee.setRiceSubsidy(Optional.ofNullable(partialEmployee.getRiceSubsidy())
                                               .orElse(existingEmployee.getRiceSubsidy()));
        existingEmployee.setPhoneAllowance(Optional.ofNullable(partialEmployee.getPhoneAllowance())
                                                  .orElse(existingEmployee.getPhoneAllowance()));
        existingEmployee.setClothingAllowance(Optional.ofNullable(partialEmployee.getClothingAllowance())
                                                    .orElse(existingEmployee.getClothingAllowance()));
        existingEmployee.setGrossSemiMonthlyRate(Optional.ofNullable(partialEmployee.getGrossSemiMonthlyRate())
                                                        .orElse(existingEmployee.getGrossSemiMonthlyRate()));
        existingEmployee.setHourlyRate(Optional.ofNullable(partialEmployee.getHourlyRate())
                                              .orElse(existingEmployee.getHourlyRate()));

        // Validate the *merged* employee object for uniqueness (true for update)
        String validationError = validateEmployeeUniqueness(existingEmployee, true);
        if (validationError != null) {
            System.err.println("EmployeeService: Update failed due to validation error: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        try {
            boolean success = employeeDao.updateEmployee(existingEmployee);
            if (success) {
                System.out.println(
                        "EmployeeService: Successfully updated employee: " + existingEmployee.getEmployeeNumber());
                return existingEmployee;
            } else {
                // This case should ideally not be hit if existingEmployee was found initially
                System.err.println("EmployeeService: Failed to update employee " + existingEmployee.getEmployeeNumber()
                        + " (DAO reported not found during update attempt).");
                return null;
            }
        } catch (Exception e) {
            System.err.println("EmployeeService: Error updating employee " + existingEmployee.getEmployeeNumber() + ": "
                    + e.getMessage());
            throw new RuntimeException("Failed to update employee.", e);
        }
    }

    /**
     * Deletes an employee by their employee number and also deletes the associated user.
     *
     * @param employeeNumber The employee number of the employee to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws RuntimeException if deletion fails.
     */
    public boolean deleteEmployee(String employeeNumber) {
        System.out.println("EmployeeService: Attempting to delete employee: " + employeeNumber);
        try {
            // First, delete the employee from the employee data store
            boolean employeeDeleted = employeeDao.deleteEmployee(employeeNumber);

            if (employeeDeleted) {
                System.out.println("EmployeeService: Employee " + employeeNumber
                        + " deleted successfully. Now attempting to delete associated user.");

                String associatedUsername = "user-" + employeeNumber;
                Optional<User> userOptional = userDao.findByUsername(associatedUsername);

                if (userOptional.isPresent()) {
                    String userIdToDelete = userOptional.get().getId();
                    // Call AuthService to delete the user, relying on its internal logging
                    boolean userDeleted = authService.deleteUser(userIdToDelete);
                    if (userDeleted) {
                        System.out.println(
                                "EmployeeService: Successfully deleted associated user '" + associatedUsername + "'.");
                    } else {
                        System.err.println("EmployeeService: Failed to delete associated user '" + associatedUsername
                                + "' (ID: " + userIdToDelete + "). This might require manual cleanup.");
                    }
                } else {
                    System.out.println("EmployeeService: No associated user found for employee " + employeeNumber
                            + ". Skipping user deletion.");
                }
                return true; // Employee was deleted, even if user deletion failed
            } else {
                System.out.println("EmployeeService: Employee " + employeeNumber + " not found for deletion.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("EmployeeService: Error deleting employee " + employeeNumber + " or associated user: "
                    + e.getMessage());
            throw new RuntimeException("Failed to delete employee and/or associated user.", e);
        }
    }

    /**
     * Validates the uniqueness of key employee fields (employee number, SSS, PhilHealth, TIN, Pag-ibig, Phone Number).
     *
     * @param employee The employee object to validate.
     * @param isUpdate A flag indicating if this is an update operation (true) or creation (false).
     * For updates, the current employee's own record is excluded from uniqueness checks.
     * @return A String containing an error message if a uniqueness violation is found, otherwise null.
     */
    private String validateEmployeeUniqueness(Employee employee, boolean isUpdate) {
        List<Employee> allExistingEmployees = employeeDao.getAllEmployees();

        // Filter out the employee being updated if it's an update operation
        List<Employee> otherEmployees;
        if (isUpdate) {
            otherEmployees = allExistingEmployees.stream()
                    .filter(e -> !e.getEmployeeNumber().equals(employee.getEmployeeNumber()))
                    .collect(Collectors.toList());
        } else {
            // For creation, all existing employees are "other" employees
            otherEmployees = allExistingEmployees;
        }

        // Validate Employee Number uniqueness
        if (otherEmployees.stream().anyMatch(e -> e.getEmployeeNumber().equals(employee.getEmployeeNumber()))) {
            return "Employee Number " + employee.getEmployeeNumber() + " already exists.";
        }

        // Validate uniqueness for other fields (only if they are provided/non-empty in the current employee)
        if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isEmpty() &&
                otherEmployees.stream().anyMatch(e -> employee.getPhoneNumber().equals(e.getPhoneNumber()))) {
            return "Phone Number " + employee.getPhoneNumber() + " already exists.";
        }
        if (employee.getSssNumber() != null && !employee.getSssNumber().isEmpty() &&
                otherEmployees.stream().anyMatch(e -> employee.getSssNumber().equals(e.getSssNumber()))) {
            return "SSS Number " + employee.getSssNumber() + " already exists.";
        }
        if (employee.getPhilhealthNumber() != null && !employee.getPhilhealthNumber().isEmpty() &&
                otherEmployees.stream().anyMatch(e -> employee.getPhilhealthNumber().equals(e.getPhilhealthNumber()))) {
            return "PhilHealth Number " + employee.getPhilhealthNumber() + " already exists.";
        }
        if (employee.getTinNumber() != null && !employee.getTinNumber().isEmpty() &&
                otherEmployees.stream().anyMatch(e -> employee.getTinNumber().equals(e.getTinNumber()))) {
            return "TIN Number " + employee.getTinNumber() + " already exists.";
        }
        if (employee.getPagibigNumber() != null && !employee.getPagibigNumber().isEmpty() &&
                otherEmployees.stream().anyMatch(e -> employee.getPagibigNumber().equals(e.getPagibigNumber()))) {
            return "Pag-ibig Number " + employee.getPagibigNumber() + " already exists.";
        }

        return null; // No validation errors found
    }
}
