package com.attendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an employee attendance log entry
 */
public class AttendanceLog {
    private String employeeId;
    private String action;
    private String time;

    // Constructor
    public AttendanceLog(String employeeId, String action, String time) {
        this.employeeId = employeeId;
        this.action = action;
        this.time = time;
    }

    // Getters
    public String getEmployeeId() {
        return employeeId;
    }

    public String getAction() {
        return action;
    }

    public String getTime() {
        return time;
    }

    // Convert time string to LocalTime for comparison
    public LocalTime getTimeAsLocalTime() {
        try {
            // Parse time manually to handle AM/PM format
            String[] parts = time.split(" ");
            String timePart = parts[0];
            String amPm = parts[1];

            String[] hourMinute = timePart.split(":");
            int hour = Integer.parseInt(hourMinute[0]);
            int minute = Integer.parseInt(hourMinute[1]);

            // Convert to 24-hour format
            if (amPm.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            System.err.println("Error parsing time: " + time + " - " + e.getMessage());
            return null;
        }
    }

    // Check if this log represents a login after 9:00 AM
    public boolean isLoginAfter9AM() {
        if (!action.equals("LOGIN")) {
            return false;
        }

        LocalTime logTime = getTimeAsLocalTime();
        LocalTime nineAM = LocalTime.of(9, 0);

        return logTime != null && logTime.isAfter(nineAM);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", employeeId, action, time);
    }
}