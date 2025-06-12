package com.imperionite.cp2c.model;

import java.math.BigDecimal;

public class PagIbigContributionRule {
    private BigDecimal salaryCap;
    private BigDecimal contributionRate; // Employee share rate

    public PagIbigContributionRule() {}

    public BigDecimal getSalaryCap() { return salaryCap; }
    public void setSalaryCap(BigDecimal salaryCap) { this.salaryCap = salaryCap; }

    public BigDecimal getContributionRate() { return contributionRate; }
    public void setContributionRate(BigDecimal contributionRate) { this.contributionRate = contributionRate; }
}