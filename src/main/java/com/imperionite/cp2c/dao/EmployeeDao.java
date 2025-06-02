package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.Employee;
import java.util.List;

/**
 * Interface for employee data access operations.
 */
public interface EmployeeDao {
    /**
     * Retrieves all employee records.
     * 
     * @return A list of all Employee objects.
     */
    List<Employee> getAllEmployees();

    /**
     * Finds an employee by their unique employee number.
     * 
     * @param employeeNumber The employee number to search for.
     * @return The Employee object if found, null otherwise.
     */
    Employee findByEmployeeNumber(String employeeNumber);

    /**
     * Adds a new employee to the data store.
     * 
     * @param employee The Employee object to add.
     */
    void addEmployee(Employee employee);

    /**
     * Updates an existing employee in the data store.
     * 
     * @param updatedEmployee The Employee object with updated details.
     * @return true if the employee was found and updated, false otherwise.
     */
    boolean updateEmployee(Employee updatedEmployee);

    /**
     * Deletes an employee from the data store by their employee number.
     * 
     * @param employeeNumber The employee number to delete.
     * @return true if the employee was found and deleted, false otherwise.
     */
    boolean deleteEmployee(String employeeNumber);
}