package com.imperionite.cp2c.model;

import java.math.BigDecimal;

public class PhilHealthContributionRule {
    private BigDecimal minSalary; // NEW
    private BigDecimal maxSalary; // NEW
    private BigDecimal rate; // NEW (for percentage-based calculation)
    private BigDecimal fixedEmployeeContribution; // NEW (for fixed amounts at floor/ceiling)

    public PhilHealthContributionRule() {}

    // Getters and Setters
    public BigDecimal getMinSalary() { return minSalary; }
    public void setMinSalary(BigDecimal minSalary) { this.minSalary = minSalary; }

    public BigDecimal getMaxSalary() { return maxSalary; }
    public void setMaxSalary(BigDecimal maxSalary) { this.maxSalary = maxSalary; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getFixedEmployeeContribution() { return fixedEmployeeContribution; }
    public void setFixedEmployeeContribution(BigDecimal fixedEmployeeContribution) { this.fixedEmployeeContribution = fixedEmployeeContribution; }
}