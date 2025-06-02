package com.imperionite.cp2c.dao;

import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.util.CsvFileHandler;
import java.util.List;
import java.util.Optional;

/**
 * CSV-based implementation of EmployeeDao, now using CsvFileHandler for
 * read/write
 * operations on employees.csv in the data directory.
 */
public class CSVEmployeeDao implements EmployeeDao {

    private final CsvFileHandler<Employee> csvFileHandler;
    private static final String CSV_FILENAME = "employees.csv";
    private static final String EMPLOYEE_CSV_HEADER = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";

    public CSVEmployeeDao() { // Constructor now takes no resourceName, as it uses CsvFileHandler
        this.csvFileHandler = new CsvFileHandler<>(
                CSV_FILENAME,
                Employee::fromCsvLine,
                Employee::toCsvLine, // Use the new toCsvLine method in Employee
                true // employees.csv WILL have a header
        );
        // Initial load is handled by CsvFileHandler's readAll, which is called by
        // getAllEmployees
        System.out.println("CSVEmployeeDao: Initialized with CsvFileHandler for " + CSV_FILENAME);
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = csvFileHandler.readAll();
        System.out.println("CSVEmployeeDao: Retrieved " + employees.size() + " employees from " + CSV_FILENAME);
        return employees;
    }

    @Override
    public Employee findByEmployeeNumber(String employeeNumber) {
        System.out.println("CSVEmployeeDao: Searching for employee number: '" + employeeNumber + "'");
        Optional<Employee> found = getAllEmployees().stream() // Read fresh data to ensure latest state
                .filter(e -> e != null && e.getEmployeeNumber().equals(employeeNumber))
                .findFirst();

        if (found.isPresent()) {
            System.out.println("CSVEmployeeDao: Found employee " + employeeNumber + ".");
            return found.get();
        } else {
            System.out.println("CSVEmployeeDao: Employee " + employeeNumber + " not found.");
            return null;
        }
    }

    @Override
    public void addEmployee(Employee employee) {
        List<Employee> employees = getAllEmployees();
        employees.add(employee);
        csvFileHandler.writeHeaderAndAll(EMPLOYEE_CSV_HEADER, employees);
        System.out.println("CSVEmployeeDao: Added employee " + employee.getEmployeeNumber() + " to " + CSV_FILENAME);
    }

    @Override
    public boolean updateEmployee(Employee updatedEmployee) {
        List<Employee> employees = getAllEmployees();
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeNumber().equals(updatedEmployee.getEmployeeNumber())) {
                employees.set(i, updatedEmployee);
                found = true;
                break;
            }
        }
        if (found) {
            csvFileHandler.writeHeaderAndAll(EMPLOYEE_CSV_HEADER, employees);
            System.out.println(
                    "CSVEmployeeDao: Updated employee " + updatedEmployee.getEmployeeNumber() + " in " + CSV_FILENAME);
        } else {
            System.out.println(
                    "CSVEmployeeDao: Employee " + updatedEmployee.getEmployeeNumber() + " not found for update.");
        }
        return found;
    }

    @Override
    public boolean deleteEmployee(String employeeNumber) {
        List<Employee> employees = getAllEmployees();
        boolean removed = employees.removeIf(e -> e.getEmployeeNumber().equals(employeeNumber));
        if (removed) {
            csvFileHandler.writeHeaderAndAll(EMPLOYEE_CSV_HEADER, employees);
            System.out.println("CSVEmployeeDao: Deleted employee " + employeeNumber + " from " + CSV_FILENAME);
        } else {
            System.out.println("CSVEmployeeDao: Employee " + employeeNumber + " not found for deletion.");
        }
        return removed;
    }
}