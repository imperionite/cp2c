package com.imperionite.cp2c.model;

import java.math.BigDecimal;

public class SSSContributionRule {
    private BigDecimal salaryCap;
    private BigDecimal contribution; // Employee share

    public SSSContributionRule() {}

    public BigDecimal getSalaryCap() { return salaryCap; }
    public void setSalaryCap(BigDecimal salaryCap) { this.salaryCap = salaryCap; }

    public BigDecimal getContribution() { return contribution; }
    public void setContribution(BigDecimal contribution) { this.contribution = contribution; }
}