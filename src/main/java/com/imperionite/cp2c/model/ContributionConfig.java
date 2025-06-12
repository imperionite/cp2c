package com.imperionite.cp2c.model;

import java.util.List;

public class ContributionConfig {
    private List<SSSContributionRule> sss;
    private List<PhilHealthContributionRule> philhealth;
    private List<PagIbigContributionRule> pagibig;
    private List<WithholdingTaxRule> withholdingTax; // NEW

    public ContributionConfig() {}

    // Getters and Setters
    public List<SSSContributionRule> getSss() { return sss; }
    public void setSss(List<SSSContributionRule> sss) { this.sss = sss; }

    public List<PhilHealthContributionRule> getPhilhealth() { return philhealth; }
    public void setPhilhealth(List<PhilHealthContributionRule> philhealth) { this.philhealth = philhealth; }

    public List<PagIbigContributionRule> getPagibig() { return pagibig; }
    public void setPagibig(List<PagIbigContributionRule> pagibig) { this.pagibig = pagibig; }

    // NEW
    public List<WithholdingTaxRule> getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(List<WithholdingTaxRule> withholdingTax) { this.withholdingTax = withholdingTax; }
}