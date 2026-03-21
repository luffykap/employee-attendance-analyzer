package com.attendance;

import java.util.*;
import java.time.LocalTime;

public class AttendanceLogAnalyzer {
    private List<AttendanceLog> attendanceLogs;

    public AttendanceLogAnalyzer() {
        this.attendanceLogs = new ArrayList<>();
    }

    /**
     * Parse a log entry and extract employeeId using substring()
     * Format: "EMP101 | LOGIN | 09:05 AM"
     */
    public void parseAndAddLog(String logEntry) {
        try {
            // Split the log entry by " | "
            String[] parts = logEntry.split(" \\| ");

            if (parts.length != 3) {
                System.err.println("Invalid log format: " + logEntry);
                return;
            }

            // Extract employeeId using substring() - requirement #2
            String employeeId = parts[0].substring(0); // Extract full employee ID
            String action = parts[1];
            String time = parts[2];

            // Create and add AttendanceLog object
            AttendanceLog log = new AttendanceLog(employeeId, action, time);
            attendanceLogs.add(log);

        } catch (Exception e) {
            System.err.println("Error parsing log entry: " + logEntry + " - " + e.getMessage());
        }
    }

    /**
     * Display employees who logged in after 9:00 AM - requirement #4
     */
    public void displayLoginAfter9AM() {
        System.out.println("\\n=== Employees who logged in after 9:00 AM ===");

        List<AttendanceLog> lateLogins = attendanceLogs.stream()
            .filter(AttendanceLog::isLoginAfter9AM)
            .toList();

        if (lateLogins.isEmpty()) {
            System.out.println("No employees logged in after 9:00 AM");
        } else {
            lateLogins.forEach(System.out::println);
        }
    }

    /**
     * Sort logs by employeeId using Comparator - requirement #5
     */
    public void sortLogsByEmployeeId() {
        attendanceLogs.sort(Comparator.comparing(AttendanceLog::getEmployeeId));
        System.out.println("\\nLogs sorted by Employee ID");
    }

    /**
     * Display all logs
     */
    public void displayAllLogs() {
        System.out.println("\\n=== All Attendance Logs ===");
        attendanceLogs.forEach(System.out::println);
    }

    /**
     * Get statistics
     */
    public void displayStatistics() {
        System.out.println("\\n=== Attendance Statistics ===");
        System.out.println("Total log entries: " + attendanceLogs.size());

        long loginCount = attendanceLogs.stream()
            .filter(log -> "LOGIN".equals(log.getAction()))
            .count();

        long logoutCount = attendanceLogs.stream()
            .filter(log -> "LOGOUT".equals(log.getAction()))
            .count();

        System.out.println("LOGIN entries: " + loginCount);
        System.out.println("LOGOUT entries: " + logoutCount);
    }

    /**
     * Get all logs (for GUI access)
     */
    public List<AttendanceLog> getAllLogs() {
        return new ArrayList<>(attendanceLogs);
    }

    /**
     * Get logs for employees who logged in after 9:00 AM
     */
    public List<AttendanceLog> getLogsAfter9AM() {
        return attendanceLogs.stream()
            .filter(AttendanceLog::isLoginAfter9AM)
            .toList();
    }

    /**
     * Save logs to file in specified format (TXT, CSV, JSON)
     */
    public void saveToFile(String filename, String format) throws Exception {
        java.io.FileWriter writer = new java.io.FileWriter(filename);
        java.io.BufferedWriter bw = new java.io.BufferedWriter(writer);

        try {
            if ("JSON".equalsIgnoreCase(format)) {
                // JSON format
                bw.write("[\n");
                for (int i = 0; i < attendanceLogs.size(); i++) {
                    AttendanceLog log = attendanceLogs.get(i);
                    bw.write("  {\n");
                    bw.write("    \"employeeId\": \"" + log.getEmployeeId() + "\",\n");
                    bw.write("    \"action\": \"" + log.getAction() + "\",\n");
                    bw.write("    \"time\": \"" + log.getTime() + "\"\n");
                    bw.write("  }");
                    if (i < attendanceLogs.size() - 1) bw.write(",");
                    bw.write("\n");
                }
                bw.write("]\n");
            } else if ("CSV".equalsIgnoreCase(format)) {
                // CSV format
                bw.write("Employee ID,Action,Time\n");
                for (AttendanceLog log : attendanceLogs) {
                    bw.write(log.getEmployeeId() + "," + log.getAction() + "," + log.getTime() + "\n");
                }
            } else {
                // TXT format (default)
                for (AttendanceLog log : attendanceLogs) {
                    bw.write(log.getEmployeeId() + " | " + log.getAction() + " | " + log.getTime() + "\n");
                }
            }
        } finally {
            bw.close();
            writer.close();
        }
    }

    /**
     * Append logs to existing file
     */
    public void appendToFile(String filename, String format) throws Exception {
        java.io.FileWriter writer = new java.io.FileWriter(filename, true); // append mode
        java.io.BufferedWriter bw = new java.io.BufferedWriter(writer);

        try {
            if ("CSV".equalsIgnoreCase(format)) {
                for (AttendanceLog log : attendanceLogs) {
                    bw.write(log.getEmployeeId() + "," + log.getAction() + "," + log.getTime() + "\n");
                }
            } else {
                // TXT format (default)
                for (AttendanceLog log : attendanceLogs) {
                    bw.write(log.getEmployeeId() + " | " + log.getAction() + " | " + log.getTime() + "\n");
                }
            }
        } finally {
            bw.close();
            writer.close();
        }
    }

    /**
     * Load logs from file (supports TXT, CSV, JSON formats)
     */
    public int loadFromFile(String filename) throws Exception {
        java.io.FileReader reader = new java.io.FileReader(filename);
        java.io.BufferedReader br = new java.io.BufferedReader(reader);
        int count = 0;

        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Skip JSON brackets and braces
                if (line.equals("[") || line.equals("]") || line.equals("{") || line.equals("}") || line.equals("},")) {
                    continue;
                }

                // Skip CSV header
                if (line.startsWith("Employee ID,") || line.startsWith("\"employeeId\"")) {
                    continue;
                }

                // Try to parse as TXT format (EMP | ACTION | TIME)
                if (line.contains(" | ")) {
                    parseAndAddLog(line);
                    count++;
                }
                // Try to parse as CSV format
                else if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String logEntry = parts[0].trim() + " | " + parts[1].trim() + " | " + parts[2].trim();
                        parseAndAddLog(logEntry);
                        count++;
                    }
                }
            }
        } finally {
            br.close();
            reader.close();
        }

        return count;
    }

    /**
     * Clear all logs
     */
    public void clearAllLogs() {
        attendanceLogs.clear();
    }

    public static void main(String[] args) {
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();

        // Sample attendance logs
        String[] sampleLogs = {
            "EMP101 | LOGIN | 09:05 AM",
            "EMP102 | LOGIN | 08:45 AM",
            "EMP103 | LOGIN | 09:15 AM",
            "EMP101 | LOGOUT | 05:30 PM",
            "EMP105 | LOGIN | 08:30 AM",
            "EMP104 | LOGIN | 09:30 AM",
            "EMP102 | LOGOUT | 06:00 PM",
            "EMP103 | LOGOUT | 05:45 PM"
        };

        // Parse and add all logs - demonstrates requirements #1, #2, #3
        System.out.println("Parsing attendance logs...");
        for (String logEntry : sampleLogs) {
            analyzer.parseAndAddLog(logEntry);
        }

        // Display all logs before sorting
        analyzer.displayAllLogs();

        // Requirement #4: Display employees who logged in after 9:00 AM
        analyzer.displayLoginAfter9AM();

        // Requirement #5: Sort logs by employeeId using Comparator
        analyzer.sortLogsByEmployeeId();

        // Display logs after sorting
        System.out.println("\\n=== Logs after sorting by Employee ID ===");
        analyzer.displayAllLogs();

        // Additional statistics
        analyzer.displayStatistics();
    }
}