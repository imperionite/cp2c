package com.imperionite.cp2c.model;

public class MonthlyCutoff {
    private String yearMonth; // e.g., "2024-01"
    private String startDate; // e.g., "Jan 1"
    private String endDate;   // e.g., "Jan 31"

    public MonthlyCutoff(String yearMonth, String startDate, String endDate) {
        this.yearMonth = yearMonth;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}