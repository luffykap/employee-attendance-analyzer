package com.attendance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceLogAnalyzer {
    // REPLACED: ArrayList replaced with TreeMap. Employee ID is the Key.
    private TreeMap<String, List<AttendanceLog>> attendanceMap;

    public AttendanceLogAnalyzer() {
        this.attendanceMap = new TreeMap<>();
    }

    public void parseAndAddLog(String logEntry) {
        try {
            String[] parts = logEntry.split(" \\| ");
            if (parts.length != 3) {
                System.err.println("Invalid log format: " + logEntry);
                return;
            }

            String employeeId = parts[0].trim();
            String action = parts[1].trim();
            String timeStr = parts[2].trim();

            AttendanceLog log = new AttendanceLog(employeeId, action, timeStr);

            // Add to TreeMap, creating a new list if the employee doesn't exist yet
            attendanceMap.putIfAbsent(employeeId, new ArrayList<>());
            attendanceMap.get(employeeId).add(log);

        } catch (Exception e) {
            System.err.println("Error parsing log entry: " + logEntry + " - " + e.getMessage());
        }
    }

    /**
     * NEW: Calculate total session durations using LocalTime math
     */
    public Map<String, String> calculateTotalDurations() {
        Map<String, String> durationReport = new LinkedHashMap<>();

        attendanceMap.forEach((empId, logs) -> {
            long totalMinutes = 0;
            AttendanceLog lastLogin = null;

            // Ensure logs are sorted chronologically before calculating
            logs.sort(Comparator.comparing(AttendanceLog::getTime));

            for (AttendanceLog log : logs) {
                if ("LOGIN".equals(log.getAction())) {
                    lastLogin = log;
                } else if ("LOGOUT".equals(log.getAction()) && lastLogin != null) {
                    totalMinutes += Duration.between(lastLogin.getTime(), log.getTime()).toMinutes();
                    lastLogin = null; // Reset for next session
                }
            }

            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            durationReport.put(empId, String.format("%d hours, %d minutes", hours, minutes));
        });

        return durationReport;
    }

    public List<AttendanceLog> getAllLogs() {
        // Flatten the TreeMap into a single List for the GUI Table and File Saving
        return attendanceMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<AttendanceLog> getLogsAfter9AM() {
        return getAllLogs().stream()
                .filter(AttendanceLog::isLoginAfter9AM)
                .collect(Collectors.toList());
    }

    public void sortLogsByEmployeeId() {
        // TreeMap naturally sorts by the String Key (Employee ID), so we don't need to do anything!
        System.out.println("\nLogs are automatically sorted by Employee ID via TreeMap.");
    }

    public void clearAllLogs() {
        attendanceMap.clear();
    }

    // --- FILE I/O OPERATIONS ---

    public void saveToFile(String filename, String format) throws Exception {
        List<AttendanceLog> flatLogs = getAllLogs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            if ("JSON".equalsIgnoreCase(format)) {
                bw.write("[\n");
                for (int i = 0; i < flatLogs.size(); i++) {
                    AttendanceLog log = flatLogs.get(i);
                    bw.write("  {\n");
                    bw.write("    \"employeeId\": \"" + log.getEmployeeId() + "\",\n");
                    bw.write("    \"action\": \"" + log.getAction() + "\",\n");
                    bw.write("    \"time\": \"" + log.getTimeAsString() + "\"\n");
                    bw.write("  }");
                    if (i < flatLogs.size() - 1) bw.write(",");
                    bw.write("\n");
                }
                bw.write("]\n");
            } else if ("CSV".equalsIgnoreCase(format)) {
                bw.write("Employee ID,Action,Time\n");
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + "," + log.getAction() + "," + log.getTimeAsString() + "\n");
                }
            } else {
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + " | " + log.getAction() + " | " + log.getTimeAsString() + "\n");
                }
            }
        }
    }

    public void appendToFile(String filename, String format) throws Exception {
        List<AttendanceLog> flatLogs = getAllLogs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            if ("CSV".equalsIgnoreCase(format)) {
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + "," + log.getAction() + "," + log.getTimeAsString() + "\n");
                }
            } else {
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + " | " + log.getAction() + " | " + log.getTimeAsString() + "\n");
                }
            }
        }
    }

    public int loadFromFile(String filename) throws Exception {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.equals("[") || line.equals("]") || 
                    line.equals("{") || line.equals("}") || line.equals("},") || 
                    line.startsWith("Employee ID,") || line.startsWith("\"employeeId\"")) {
                    continue;
                }

                if (line.contains(" | ")) {
                    parseAndAddLog(line);
                    count++;
                } else if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String logEntry = parts[0].trim() + " | " + parts[1].trim() + " | " + parts[2].trim();
                        parseAndAddLog(logEntry);
                        count++;
                    }
                }
            }
        }
        return count;
    }
}