package com.attendance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AttendanceLogAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(AttendanceLogAnalyzer.class.getName());

    private TreeMap<String, List<AttendanceLog>> attendanceMap;

    public AttendanceLogAnalyzer() {
        this.attendanceMap = new TreeMap<>();
    }

    public void parseAndAddLog(String logEntry) {
        try {
            String[] parts = logEntry.split(" \\| ");
            String employeeId, action, date, timeStr;

            if (parts.length == 4) {
                // New format: EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM
                employeeId = parts[0].trim();
                action = parts[1].trim();
                date = parts[2].trim();
                timeStr = parts[3].trim();
            } else if (parts.length == 3) {
                // Legacy format: EMP101 | LOGIN | 09:05 AM (no date)
                employeeId = parts[0].trim();
                action = parts[1].trim();
                date = "N/A";
                timeStr = parts[2].trim();
            } else {
                LOGGER.warning("Invalid log format: " + logEntry);
                return;
            }

            AttendanceLog log = new AttendanceLog(employeeId, action, date, timeStr);

            // Add to TreeMap, creating a new list if the employee doesn't exist yet
            attendanceMap.putIfAbsent(employeeId, new ArrayList<>());
            attendanceMap.get(employeeId).add(log);

        } catch (DateTimeParseException e) {
            LOGGER.log(Level.WARNING, "Invalid time format in log entry: " + logEntry, e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid argument in log entry: " + logEntry, e);
        }
    }

    /**
     * Validates business rules and adds a log entry.
     * Throws IllegalArgumentException if validation fails.
     * This is the primary method for interactive (GUI/console) entry.
     * 
     * Validation rules:
     *   - Employee ID must match EMPXXX format
     *   - Cannot LOGIN if already logged in
     *   - Cannot LOGOUT if not logged in
     */
    public void validateAndAddLog(String employeeId, String action, String date, String timeStr) {
        // Validate Employee ID format
        if (employeeId == null || employeeId.isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required!");
        }
        if (!employeeId.matches("EMP\\d{3}")) {
            throw new IllegalArgumentException("Employee ID format must be EMPXXX (e.g., EMP101)");
        }

        // Sanitize inputs — reject special characters that could break file I/O
        if (containsUnsafeChars(action) || containsUnsafeChars(date) || containsUnsafeChars(timeStr)) {
            throw new IllegalArgumentException("Input contains invalid special characters.");
        }

        // Validate action
        if (!"LOGIN".equals(action) && !"LOGOUT".equals(action)) {
            throw new IllegalArgumentException("Action must be LOGIN or LOGOUT.");
        }

        // Validate login/logout sequencing
        String lastAction = getLastAction(employeeId);

        if ("LOGIN".equals(action) && "LOGIN".equals(lastAction)) {
            throw new IllegalArgumentException(
                "Employee " + employeeId + " is already logged in. Please logout first.");
        }

        if ("LOGOUT".equals(action) && !"LOGIN".equals(lastAction)) {
            String reason = lastAction == null
                ? "Employee " + employeeId + " is not logged in. Cannot logout."
                : "Employee " + employeeId + " is already logged out. Please login first.";
            throw new IllegalArgumentException(reason);
        }

        // Validation passed — add the log
        String logEntry = String.format("%s | %s | %s | %s", employeeId, action, date, timeStr);
        parseAndAddLog(logEntry);
    }

    /**
     * Checks if a string contains characters that could break file I/O or parsing.
     */
    private boolean containsUnsafeChars(String input) {
        if (input == null) return false;
        // Reject pipe (breaks TXT parsing), quotes (breaks JSON), newlines, tabs
        return input.contains("|") || input.contains("\"") ||
               input.contains("\n") || input.contains("\r") || input.contains("\t");
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


    public void clearAllLogs() {
        attendanceMap.clear();
    }

    /**
     * NEW: Filter logs by specific employee ID
     * Returns all attendance records for a given employee
     * Efficient O(log n) lookup using TreeMap
     */
    public List<AttendanceLog> getLogsByEmployeeId(String employeeId) {
        List<AttendanceLog> logs = attendanceMap.get(employeeId);
        if (logs == null) {
            return Collections.emptyList();
        }
        // Return a sorted copy (by time)
        List<AttendanceLog> sortedLogs = new ArrayList<>(logs);
        sortedLogs.sort(Comparator.comparing(AttendanceLog::getTime));
        return sortedLogs;
    }

    /**
     * NEW: Get total working hours for a specific employee
     * Returns a formatted string with hours and minutes
     */
    public String getTotalDurationForEmployee(String employeeId) {
        List<AttendanceLog> logs = attendanceMap.get(employeeId);
        if (logs == null || logs.isEmpty()) {
            return "No data available";
        }

        long totalMinutes = 0;
        AttendanceLog lastLogin = null;

        // Sort logs chronologically
        List<AttendanceLog> sortedLogs = new ArrayList<>(logs);
        sortedLogs.sort(Comparator.comparing(AttendanceLog::getTime));

        for (AttendanceLog log : sortedLogs) {
            if ("LOGIN".equals(log.getAction())) {
                lastLogin = log;
            } else if ("LOGOUT".equals(log.getAction()) && lastLogin != null) {
                totalMinutes += Duration.between(lastLogin.getTime(), log.getTime()).toMinutes();
                lastLogin = null;
            }
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d hours, %d minutes", hours, minutes);
    }

    /**
     * Get the last action (LOGIN/LOGOUT) for a specific employee.
     * Efficient O(log n) TreeMap lookup + O(1) list tail access.
     * Returns null if the employee has no records.
     */
    public String getLastAction(String employeeId) {
        List<AttendanceLog> logs = attendanceMap.get(employeeId);
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        return logs.get(logs.size() - 1).getAction();
    }

    /**
     * NEW: Check if employee exists in the system
     */
    public boolean employeeExists(String employeeId) {
        return attendanceMap.containsKey(employeeId);
    }

    // --- FILE I/O OPERATIONS ---

    public void saveToFile(String filename, String format) throws IOException {
        List<AttendanceLog> flatLogs = getAllLogs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            if ("JSON".equalsIgnoreCase(format)) {
                bw.write("[\n");
                for (int i = 0; i < flatLogs.size(); i++) {
                    AttendanceLog log = flatLogs.get(i);
                    bw.write("  {\n");
                    bw.write("    \"employeeId\": \"" + escapeJsonString(log.getEmployeeId()) + "\",\n");
                    bw.write("    \"action\": \"" + escapeJsonString(log.getAction()) + "\",\n");
                    bw.write("    \"date\": \"" + escapeJsonString(log.getDate()) + "\",\n");
                    bw.write("    \"time\": \"" + escapeJsonString(log.getTimeAsString()) + "\"\n");
                    bw.write("  }");
                    if (i < flatLogs.size() - 1) bw.write(",");
                    bw.write("\n");
                }
                bw.write("]\n");
            } else if ("CSV".equalsIgnoreCase(format)) {
                bw.write("Employee ID,Action,Date,Time\n");
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + "," + log.getAction() + "," + log.getDate() + "," + log.getTimeAsString() + "\n");
                }
            } else {
                for (AttendanceLog log : flatLogs) {
                    bw.write(log.getEmployeeId() + " | " + log.getAction() + " | " + log.getDate() + " | " + log.getTimeAsString() + "\n");
                }
            }
        }
    }

    /**
     * Escapes special characters for safe JSON string output.
     */
    private String escapeJsonString(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }


    public int loadFromFile(String filename) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Peek at the first non-empty line to detect format
            br.mark(8192);
            String firstLine = "";
            String peek;
            while ((peek = br.readLine()) != null) {
                peek = peek.trim();
                if (!peek.isEmpty()) { firstLine = peek; break; }
            }
            br.reset();

            if (firstLine.startsWith("[") || firstLine.startsWith("{")) {
                // --- JSON FORMAT ---
                // Read the entire file content and parse JSON objects manually
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String content = sb.toString();

                // Extract each JSON object's fields using simple string parsing
                String employeeId = null, action = null, date = null, time = null;
                for (String jsonLine : content.split("\n")) {
                    jsonLine = jsonLine.trim();

                    if (jsonLine.startsWith("\"employeeId\"")) {
                        employeeId = extractJsonValue(jsonLine);
                    } else if (jsonLine.startsWith("\"action\"")) {
                        action = extractJsonValue(jsonLine);
                    } else if (jsonLine.startsWith("\"date\"")) {
                        date = extractJsonValue(jsonLine);
                    } else if (jsonLine.startsWith("\"time\"")) {
                        time = extractJsonValue(jsonLine);
                    }

                    // When we hit a closing brace, we have a complete object
                    if (jsonLine.startsWith("}") && employeeId != null && action != null && time != null) {
                        String logEntry;
                        if (date != null) {
                            logEntry = employeeId + " | " + action + " | " + date + " | " + time;
                        } else {
                            logEntry = employeeId + " | " + action + " | " + time;
                        }
                        parseAndAddLog(logEntry);
                        count++;
                        // Reset for the next object
                        employeeId = null; action = null; date = null; time = null;
                    }
                }
            } else {
                // --- TXT or CSV FORMAT ---
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("Employee ID,")) {
                        continue;
                    }

                    if (line.contains(" | ")) {
                        // TXT format: EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM
                        parseAndAddLog(line);
                        count++;
                    } else if (line.contains(",")) {
                        // CSV format: EMP101,LOGIN,29-Mar-2026,09:05 AM
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            String logEntry = parts[0].trim() + " | " + parts[1].trim() + " | " + parts[2].trim() + " | " + parts[3].trim();
                            parseAndAddLog(logEntry);
                            count++;
                        } else if (parts.length == 3) {
                            // Legacy CSV without date
                            String logEntry = parts[0].trim() + " | " + parts[1].trim() + " | " + parts[2].trim();
                            parseAndAddLog(logEntry);
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Extracts the string value from a JSON line like:  "key": "value",
     */
    private String extractJsonValue(String jsonLine) {
        // Find the value after the colon: "key": "value" or "key": "value",
        int colonIndex = jsonLine.indexOf(':');
        if (colonIndex < 0) return null;
        String valuePart = jsonLine.substring(colonIndex + 1).trim();
        // Remove surrounding quotes and trailing comma
        valuePart = valuePart.replace(",", "").replace("\"", "").trim();
        return valuePart;
    }

    // --- CONSOLE ENTRY POINT ---

    public static void main(String[] args) {
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        Scanner scanner = new Scanner(System.in);

        // Load sample data
        String[] sampleLogs = {
            "EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM",
            "EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM",
            "EMP103 | LOGIN | 29-Mar-2026 | 09:15 AM",
            "EMP101 | LOGOUT | 29-Mar-2026 | 05:30 PM",
            "EMP105 | LOGIN | 29-Mar-2026 | 08:30 AM",
            "EMP104 | LOGIN | 29-Mar-2026 | 09:30 AM",
            "EMP102 | LOGOUT | 29-Mar-2026 | 06:00 PM",
            "EMP103 | LOGOUT | 29-Mar-2026 | 05:45 PM"
        };
        for (String log : sampleLogs) {
            analyzer.parseAndAddLog(log);
        }
        System.out.println("Sample data loaded (" + sampleLogs.length + " entries).\n");

        boolean running = true;
        while (running) {
            System.out.println("=== Employee Attendance Log Analyzer (Console) ===");
            System.out.println("1. Display All Logs");
            System.out.println("2. Add New Log Entry");
            System.out.println("3. Find Late Logins (>9AM)");
            System.out.println("4. Calculate Durations");
            System.out.println("5. Show Statistics");
            System.out.println("6. Search Employee");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1":
                    System.out.println("--- All Attendance Logs ---");
                    analyzer.getAllLogs().forEach(System.out::println);
                    break;

                case "2":
                    try {
                        System.out.print("Employee ID (EMPXXX): ");
                        String empId = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Action (LOGIN/LOGOUT): ");
                        String action = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Date (e.g. 29-Mar-2026): ");
                        String date = scanner.nextLine().trim();
                        System.out.print("Time (e.g. 09:05 AM): ");
                        String time = scanner.nextLine().trim().toUpperCase();
                        analyzer.validateAndAddLog(empId, action, date, time);
                        System.out.println("Log added successfully!");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Validation Error: " + e.getMessage());
                    }
                    break;

                case "3":
                    System.out.println("--- Late Logins (after 9:00 AM) ---");
                    List<AttendanceLog> lateLogs = analyzer.getLogsAfter9AM();
                    if (lateLogs.isEmpty()) {
                        System.out.println("No late logins found.");
                    } else {
                        lateLogs.forEach(System.out::println);
                    }
                    break;

                case "4":
                    System.out.println("--- Total Session Durations ---");
                    Map<String, String> durations = analyzer.calculateTotalDurations();
                    durations.forEach((id, dur) ->
                        System.out.printf("Employee ID: %s | Total Time: %s%n", id, dur));
                    break;

                case "5":
                    System.out.println("--- Statistics ---");
                    List<AttendanceLog> all = analyzer.getAllLogs();
                    long logins = all.stream().filter(l -> "LOGIN".equals(l.getAction())).count();
                    long logouts = all.stream().filter(l -> "LOGOUT".equals(l.getAction())).count();
                    long unique = all.stream().map(AttendanceLog::getEmployeeId).distinct().count();
                    System.out.println("Total Logs: " + all.size());
                    System.out.println("Logins: " + logins);
                    System.out.println("Logouts: " + logouts);
                    System.out.println("Unique Employees: " + unique);
                    break;

                case "6":
                    System.out.print("Enter Employee ID: ");
                    String searchId = scanner.nextLine().trim().toUpperCase();
                    if (!analyzer.employeeExists(searchId)) {
                        System.out.println("No records found for " + searchId);
                    } else {
                        List<AttendanceLog> empLogs = analyzer.getLogsByEmployeeId(searchId);
                        empLogs.forEach(System.out::println);
                        System.out.println("Total Working Time: " + analyzer.getTotalDurationForEmployee(searchId));
                    }
                    break;

                case "7":
                    running = false;
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-7.");
            }
            System.out.println();
        }
        scanner.close();
    }
}