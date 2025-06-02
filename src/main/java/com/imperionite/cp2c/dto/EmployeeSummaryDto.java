package com.imperionite.cp2c.dto;

import com.imperionite.cp2c.model.Employee;

/**
 * DTO for summarizing employee information for list views.
 */
public class EmployeeSummaryDto {
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;

    public EmployeeSummaryDto(String employeeNumber, String firstName, String lastName,
                              String sssNumber, String philhealthNumber, String tinNumber,
                              String pagibigNumber) {
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sssNumber = sssNumber;
        this.philhealthNumber = philhealthNumber;
        this.tinNumber = tinNumber;
        this.pagibigNumber = pagibigNumber;
    }

    // Getters
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagibigNumber() { return pagibigNumber; }

    /**
     * Converts an Employee model object to an EmployeeSummaryDto.
     * Note: This DTO does not directly expose the username from the Employee model,
     * as the Employee model itself no longer stores it directly from the static CSV.
     * If username is needed in the summary, it would be added here from an associated User object
     * in the service layer, but for now, it's excluded from the summary DTO as per previous request.
     * @param employee The Employee object to convert.
     * @return A new EmployeeSummaryDto.
     */
    public static EmployeeSummaryDto fromEmployee(Employee employee) {
        return new EmployeeSummaryDto(
            employee.getEmployeeNumber(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getSssNumber(),
            employee.getPhilhealthNumber(),
            employee.getTinNumber(),
            employee.getPagibigNumber()
        );
    }
}