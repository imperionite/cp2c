package com.imperionite.cp2c.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {
    private String employeeNumber;
    private LocalDate date;
    private LocalTime loginTime;
    private LocalTime logoutTime;

    // Default constructor for CSV parsing
    public AttendanceRecord() {}

    // Getters and Setters
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalTime loginTime) { this.loginTime = loginTime; }

    public LocalTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalTime logoutTime) { this.logoutTime = logoutTime; }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
               "employeeNumber='" + employeeNumber + '\'' +
               ", date=" + date +
               ", loginTime=" + loginTime +
               ", logoutTime=" + logoutTime +
               '}';
    }
}