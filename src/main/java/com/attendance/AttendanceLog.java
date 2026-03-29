package com.attendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents an employee attendance log entry
 */
public class AttendanceLog {
    private String employeeId;
    private String action;
    private String date;
    private LocalTime time;

    // Formatter handles both single-digit (9:05 AM) and double-digit (10:05 AM) hours
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public AttendanceLog(String employeeId, String action, String date, String timeStr) {
        this.employeeId = employeeId;
        this.action = action;
        this.date = date;
        // Parse string to LocalTime immediately upon creation
        this.time = LocalTime.parse(timeStr.toUpperCase(), FORMATTER);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getAction() {
        return action;
    }

    public String getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    // Convert back to string for the GUI table and file saving
    public String getTimeAsString() {
        return time.format(FORMATTER).toUpperCase();
    }

    public boolean isLoginAfter9AM() {
        return "LOGIN".equals(action) && time.isAfter(LocalTime.of(9, 0));
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", employeeId, action, date, getTimeAsString());
    }
}