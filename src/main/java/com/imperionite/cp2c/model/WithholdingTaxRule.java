package com.imperionite.cp2c.model;

import java.math.BigDecimal;

public class WithholdingTaxRule {
    private BigDecimal minTaxableIncome;
    private BigDecimal maxTaxableIncome;
    private BigDecimal fixedTax;
    private BigDecimal percentageOver; // As a decimal (e.g., 0.20 for 20%)
    private BigDecimal excessOver;

    public WithholdingTaxRule() {}

    // Getters and Setters
    public BigDecimal getMinTaxableIncome() { return minTaxableIncome; }
    public void setMinTaxableIncome(BigDecimal minTaxableIncome) { this.minTaxableIncome = minTaxableIncome; }

    public BigDecimal getMaxTaxableIncome() { return maxTaxableIncome; }
    public void setMaxTaxableIncome(BigDecimal maxTaxableIncome) { this.maxTaxableIncome = maxTaxableIncome; }

    public BigDecimal getFixedTax() { return fixedTax; }
    public void setFixedTax(BigDecimal fixedTax) { this.fixedTax = fixedTax; }

    public BigDecimal getPercentageOver() { return percentageOver; }
    public void setPercentageOver(BigDecimal percentageOver) { this.percentageOver = percentageOver; }

    public BigDecimal getExcessOver() { return excessOver; }
    public void setExcessOver(BigDecimal excessOver) { this.excessOver = excessOver; }
}