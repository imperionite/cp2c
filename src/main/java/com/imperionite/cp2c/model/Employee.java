package com.imperionite.cp2c.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Employee entity with all attributes from the employees.csv,
 * with appropriate data types for computations and dates.
 * The association with User will be handled in the service layer.
 */
public class Employee {
    private String employeeNumber;
    private String lastName;
    private String firstName;
    private String birthday;
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

    // Regex for robust CSV parsing
    private static final Pattern CSV_FIELD_MATCHER = Pattern.compile(
            "\"([^\"]*+(?:\"\"[^\"]*+)*)\"|([^,]*)");

    /**
     * NO-ARGUMENT CONSTRUCTOR IS REQUIRED BY JACKSON FOR JSON DESERIALIZATION.
     * 
     */
    public Employee() {

    }

    // ALL-ARGUMENTS CONSTRUCTOR
    public Employee(String employeeNumber, String lastName, String firstName, String birthday, String address,
            String phoneNumber, String sssNumber, String philhealthNumber, String tinNumber,
            String pagibigNumber, String status, String position, String immediateSupervisor,
            BigDecimal basicSalary, BigDecimal riceSubsidy, BigDecimal phoneAllowance, BigDecimal clothingAllowance,
            BigDecimal grossSemiMonthlyRate, BigDecimal hourlyRate) {
        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philhealthNumber = philhealthNumber;
        this.tinNumber = tinNumber;
        this.pagibigNumber = pagibigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;
    }

    // --- Getters for all fields ---
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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

    public String getStatus() {
        return status;
    }

    public String getPosition() {
        return position;
    }

    public String getImmediateSupervisor() {
        return immediateSupervisor;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public BigDecimal getRiceSubsidy() {
        return riceSubsidy;
    }

    public BigDecimal getPhoneAllowance() {
        return phoneAllowance;
    }

    public BigDecimal getClothingAllowance() {
        return clothingAllowance;
    }

    public BigDecimal getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    // --- Setters for all fields ---
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSssNumber(String sssNumber) {
        this.sssNumber = sssNumber;
    }

    public void setPhilhealthNumber(String philhealthNumber) {
        this.philhealthNumber = philhealthNumber;
    }

    public void setTinNumber(String tinNumber) {
        this.tinNumber = tinNumber;
    }

    public void setPagibigNumber(String pagibigNumber) {
        this.pagibigNumber = pagibigNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setImmediateSupervisor(String immediateSupervisor) {
        this.immediateSupervisor = immediateSupervisor;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public void setRiceSubsidy(BigDecimal riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public void setPhoneAllowance(BigDecimal phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public void setClothingAllowance(BigDecimal clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
    }

    public void setGrossSemiMonthlyRate(BigDecimal grossSemiMonthlyRate) {
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    // --- Static factory method to parse a CSV line ---
    public static Employee fromCsvLine(String line) {
        System.out.println("Employee.fromCsvLine: Attempting to parse line: '" + line + "'");

        List<String> extractedFields = new ArrayList<>();
        Matcher matcher = CSV_FIELD_MATCHER.matcher(line);
        int currentPosition = 0;

        while (currentPosition < line.length()) {
            matcher.region(currentPosition, line.length());
            if (matcher.lookingAt()) {
                String field;
                if (matcher.group(1) != null) {
                    field = matcher.group(1).replace("\"\"", "\"");
                } else {
                    field = matcher.group(2);
                }
                extractedFields.add(field.trim());

                currentPosition = matcher.end();

                if (currentPosition < line.length() && line.charAt(currentPosition) == ',') {
                    currentPosition++;
                }
            } else {
                // If no field matched at currentPosition, it implies an empty field
                // (e.g., "a,,b" or "a,b,"). Add an empty string for it.
                extractedFields.add("");
                // Advance past the comma that would imply this empty field
                if (currentPosition < line.length() && line.charAt(currentPosition) == ',') {
                    currentPosition++;
                } else {
                    // If no field matched and no comma, it's the end of the line or malformed
                    break;
                }
            }
        }

        // After the loop, if the line ends with a comma, or has trailing empty fields,
        // the loop might not have added them. Ensure we have exactly 19 fields.
        final int EXPECTED_COLUMNS = 19;
        while (extractedFields.size() < EXPECTED_COLUMNS) { // Use the constant
            extractedFields.add("");
        }
        // If there are more fields than expected, truncate them.
        while (extractedFields.size() > EXPECTED_COLUMNS) {
            extractedFields.remove(extractedFields.size() - 1);
        }

        String[] tokens = extractedFields.toArray(new String[0]);

        if (tokens.length != EXPECTED_COLUMNS) {
            System.err.println("Employee.fromCsvLine: Skipping malformed employee CSV line (expected "
                    + EXPECTED_COLUMNS + " columns, but got " + tokens.length + "). Line: '" + line + "'");
            System.err.println("Employee.fromCsvLine: Processed Tokens (malformed): " + Arrays.toString(tokens));
            return null;
        }
        System.out.println("Employee.fromCsvLine: Processed Tokens: " + Arrays.toString(tokens));

        try {
            // Parse BigDecimal values, handling commas within numbers (e.g., "90,000" ->
            // 90000)
            BigDecimal basicSalary = parseBigDecimal(tokens[13], "Basic Salary", tokens[0]);
            BigDecimal riceSubsidy = parseBigDecimal(tokens[14], "Rice Subsidy", tokens[0]);
            BigDecimal phoneAllowance = parseBigDecimal(tokens[15], "Phone Allowance", tokens[0]);
            BigDecimal clothingAllowance = parseBigDecimal(tokens[16], "Clothing Allowance", tokens[0]);
            BigDecimal grossSemiMonthlyRate = parseBigDecimal(tokens[17], "Gross Semi-monthly Rate", tokens[0]);
            BigDecimal hourlyRate = parseBigDecimal(tokens[18], "Hourly Rate", tokens[0]);

            String birthdayString = tokens[3];

            Employee employee = new Employee(
                    tokens[0], // Employee #
                    tokens[1], // Last Name
                    tokens[2], // First Name
                    birthdayString, // Birthday (String)
                    tokens[4], // Address
                    tokens[5], // Phone Number
                    tokens[6], // SSS #
                    tokens[7], // Philhealth #
                    tokens[8], // TIN #
                    tokens[9], // Pag-ibig #
                    tokens[10], // Status
                    tokens[11], // Position
                    tokens[12], // Immediate Supervisor
                    basicSalary,
                    riceSubsidy,
                    phoneAllowance,
                    clothingAllowance,
                    grossSemiMonthlyRate,
                    hourlyRate);
            System.out.println("Employee.fromCsvLine: Successfully parsed employee: " + employee.getEmployeeNumber());
            return employee;
        } catch (NumberFormatException e) {
            System.err.println("Employee.fromCsvLine: Error parsing numeric value in employee CSV line for employee "
                    + tokens[0] + ": " + e.getMessage() + ". Line: '" + line + "'");
            return null;
        } catch (Exception e) {
            System.err.println("Employee.fromCsvLine: Unexpected error parsing employee CSV line for employee "
                    + tokens[0] + ": " + e.getMessage() + ". Line: '" + line + "'");
            e.printStackTrace();
            return null;
        }
    }

    // --- Instance method to serialize an Employee object into a CSV line string
    // ---
    public String toCsvLine() {
        return String.join(",",
                quoteAndEscape(employeeNumber),
                quoteAndEscape(lastName),
                quoteAndEscape(firstName),
                quoteAndEscape(birthday),
                quoteAndEscape(address),
                quoteAndEscape(phoneNumber),
                quoteAndEscape(sssNumber),
                quoteAndEscape(philhealthNumber),
                quoteAndEscape(tinNumber),
                quoteAndEscape(pagibigNumber),
                quoteAndEscape(status),
                quoteAndEscape(position),
                quoteAndEscape(immediateSupervisor),
                quoteAndEscape(basicSalary != null ? basicSalary.toPlainString() : ""),
                quoteAndEscape(riceSubsidy != null ? riceSubsidy.toPlainString() : ""),
                quoteAndEscape(phoneAllowance != null ? phoneAllowance.toPlainString() : ""),
                quoteAndEscape(clothingAllowance != null ? clothingAllowance.toPlainString() : ""),
                quoteAndEscape(grossSemiMonthlyRate != null ? grossSemiMonthlyRate.toPlainString() : ""),
                quoteAndEscape(hourlyRate != null ? hourlyRate.toPlainString() : ""));
    }

    // --- Helper methods ---
    private static String quoteAndEscape(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static BigDecimal parseBigDecimal(String value, String fieldName, String employeeNumber)
            throws NumberFormatException {
        try {
            if (value == null || value.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format for " + fieldName + " ('" + value
                    + "') for employee " + employeeNumber + ": " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeNumber, employee.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeNumber);
    }

    // And a toString() for better logging
    @Override
    public String toString() {
        return "Employee{" +
                "employeeNumber='" + employeeNumber + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                // ... include other relevant fields for good logging ...
                '}';
    }
}