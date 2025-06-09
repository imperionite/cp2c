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

    // Default constructor for Jackson deserialization (important!)
    public EmployeeSummaryDto() {
    }

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
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public String getPhilhealthNumber() {
        return philhealthNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public String getPagibigNumber() {
        return pagibigNumber;
    }

    // Setters (added for Jackson deserialization if this DTO is used in request bodies)
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }


    /**
     * Converts an Employee model object to an EmployeeSummaryDto.
     * * @param employee The Employee object to convert.
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
                employee.getPagibigNumber());
    }
}
