package com.imperionite.cp2c.dto;

import com.imperionite.cp2c.model.Employee;
import java.math.BigDecimal;

/**
 * DTO representing the full details of an Employee, used for requests and responses.
 * It's structured similarly to the Employee model but explicitly for API interaction.
 * Can be used for creating (POST) or partially updating (PATCH) an employee.
 */
public class EmployeeDto {
    // Note: EmployeeNumber is often required for creation, but can be null for partial updates (PATCH)
    private String employeeNumber;
    private String lastName;
    private String firstName;
    private String birthday; // Format: YYYY-MM-DD
    private String address;
    private String phoneNumber;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;
    private String status;
    private String position;
    private String immediateSupervisor;
    private BigDecimal basicSalary;
    private BigDecimal riceSubsidy;
    private BigDecimal phoneAllowance;
    private BigDecimal clothingAllowance;
    private BigDecimal grossSemiMonthlyRate;
    private BigDecimal hourlyRate;

    public EmployeeDto() {
    }

    // Constructor to convert from Employee model to DTO
    public EmployeeDto(Employee employee) {
        this.employeeNumber = employee.getEmployeeNumber();
        this.lastName = employee.getLastName();
        this.firstName = employee.getFirstName();
        this.birthday = employee.getBirthday();
        this.address = employee.getAddress();
        this.phoneNumber = employee.getPhoneNumber();
        this.sssNumber = employee.getSssNumber();
        this.philhealthNumber = employee.getPhilhealthNumber();
        this.tinNumber = employee.getTinNumber();
        this.pagibigNumber = employee.getPagibigNumber();
        this.status = employee.getStatus();
        this.position = employee.getPosition();
        this.immediateSupervisor = employee.getImmediateSupervisor();
        this.basicSalary = employee.getBasicSalary();
        this.riceSubsidy = employee.getRiceSubsidy();
        this.phoneAllowance = employee.getPhoneAllowance();
        this.clothingAllowance = employee.getClothingAllowance();
        this.grossSemiMonthlyRate = employee.getGrossSemiMonthlyRate();
        this.hourlyRate = employee.getHourlyRate();
    }

    // Static factory method to convert DTO to Employee model
    public Employee toEmployee() {
        return new Employee(
            this.employeeNumber,
            this.lastName,
            this.firstName,
            this.birthday,
            this.address,
            this.phoneNumber,
            this.sssNumber,
            this.philhealthNumber,
            this.tinNumber,
            this.pagibigNumber,
            this.status,
            this.position,
            this.immediateSupervisor,
            this.basicSalary,
            this.riceSubsidy,
            this.phoneAllowance,
            this.clothingAllowance,
            this.grossSemiMonthlyRate,
            this.hourlyRate
        );
    }

    // --- Getters and Setters ---
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(BigDecimal riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public BigDecimal getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public BigDecimal getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(BigDecimal clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public BigDecimal getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(BigDecimal grossSemiMonthlyRate) { this.grossSemiMonthlyRate = grossSemiMonthlyRate; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}
