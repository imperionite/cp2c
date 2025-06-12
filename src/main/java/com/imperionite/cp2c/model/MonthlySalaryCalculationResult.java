package com.imperionite.cp2c.model;

import java.math.BigDecimal;

public class MonthlySalaryCalculationResult {
    private String employeeNumber;
    private String yearMonth;
    private double monthlyWorkedHours; // Using double for hours, will be converted to BigDecimal for money
    private BigDecimal grossMonthlySalary;
    private BigDecimal monthlySssDeduction;
    private BigDecimal monthlyPhilhealthDeduction;
    private BigDecimal monthlyPagibigDeduction;
    private BigDecimal monthlyWithholdingTax;
    private BigDecimal totalDeductions;
    private BigDecimal netMonthlySalary;

    // Default constructor for Jackson serialization
    public MonthlySalaryCalculationResult() {}

    public MonthlySalaryCalculationResult(String employeeNumber, String yearMonth, double monthlyWorkedHours, BigDecimal grossMonthlySalary, BigDecimal monthlySssDeduction, BigDecimal monthlyPhilhealthDeduction, BigDecimal monthlyPagibigDeduction, BigDecimal monthlyWithholdingTax, BigDecimal totalDeductions, BigDecimal netMonthlySalary) {
        this.employeeNumber = employeeNumber;
        this.yearMonth = yearMonth;
        this.monthlyWorkedHours = monthlyWorkedHours;
        this.grossMonthlySalary = grossMonthlySalary;
        this.monthlySssDeduction = monthlySssDeduction;
        this.monthlyPhilhealthDeduction = monthlyPhilhealthDeduction;
        this.monthlyPagibigDeduction = monthlyPagibigDeduction;
        this.monthlyWithholdingTax = monthlyWithholdingTax;
        this.totalDeductions = totalDeductions;
        this.netMonthlySalary = netMonthlySalary;
    }

    // Getters
    public String getEmployeeNumber() { return employeeNumber; }
    public String getYearMonth() { return yearMonth; }
    public double getMonthlyWorkedHours() { return monthlyWorkedHours; }
    public BigDecimal getGrossMonthlySalary() { return grossMonthlySalary; }
    public BigDecimal getMonthlySssDeduction() { return monthlySssDeduction; }
    public BigDecimal getMonthlyPhilhealthDeduction() { return monthlyPhilhealthDeduction; }
    public BigDecimal getMonthlyPagibigDeduction() { return monthlyPagibigDeduction; }
    public BigDecimal getMonthlyWithholdingTax() { return monthlyWithholdingTax; }
    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public BigDecimal getNetMonthlySalary() { return netMonthlySalary; }
}

