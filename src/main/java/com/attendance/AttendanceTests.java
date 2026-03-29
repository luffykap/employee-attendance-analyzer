package com.attendance;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Self-contained unit tests for AttendanceLog and AttendanceLogAnalyzer.
 * No external test framework needed — run with: java -cp target/classes com.attendance.AttendanceTests
 */
public class AttendanceTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Running Attendance Tests ===\n");

        // AttendanceLog tests
        testLogCreation();
        testLogLateDetection();
        testLogToString();

        // AttendanceLogAnalyzer tests
        testParseAndAddLog();
        testParseAndAddLogLegacyFormat();
        testParseAndAddLogInvalidFormat();
        testValidateAndAddLogSuccess();
        testValidateAndAddLogDuplicateLogin();
        testValidateAndAddLogLogoutWithoutLogin();
        testValidateAndAddLogInvalidId();
        testValidateAndAddLogInvalidAction();
        testValidateAndAddLogUnsafeChars();
        testGetLastAction();
        testGetAllLogs();
        testGetLogsAfter9AM();
        testCalculateDurations();
        testGetLogsByEmployeeId();
        testEmployeeExists();
        testClearAllLogs();
        testSaveAndLoadTxt();
        testSaveAndLoadCsv();
        testSaveAndLoadJson();

        // Summary
        System.out.println("\n=== Test Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.out.println("\n❌ SOME TESTS FAILED");
            System.exit(1);
        } else {
            System.out.println("\n✅ ALL TESTS PASSED");
        }
    }

    // --- Helper methods ---

    private static void assertTest(String name, boolean condition) {
        if (condition) {
            System.out.println("  ✅ " + name);
            passed++;
        } else {
            System.out.println("  ❌ " + name);
            failed++;
        }
    }

    private static void assertThrows(String name, Class<? extends Exception> expected, Runnable code) {
        try {
            code.run();
            System.out.println("  ❌ " + name + " (no exception thrown)");
            failed++;
        } catch (Exception e) {
            if (expected.isInstance(e)) {
                System.out.println("  ✅ " + name);
                passed++;
            } else {
                System.out.println("  ❌ " + name + " (wrong exception: " + e.getClass().getSimpleName() + ")");
                failed++;
            }
        }
    }

    // --- AttendanceLog tests ---

    private static void testLogCreation() {
        System.out.println("\n[AttendanceLog - Creation]");
        AttendanceLog log = new AttendanceLog("EMP101", "LOGIN", "29-Mar-2026", "09:05 AM");
        assertTest("Employee ID is stored", "EMP101".equals(log.getEmployeeId()));
        assertTest("Action is stored", "LOGIN".equals(log.getAction()));
        assertTest("Date is stored", "29-Mar-2026".equals(log.getDate()));
        assertTest("Time is parsed correctly", log.getTime() != null);
        assertTest("Time string format", log.getTimeAsString().contains("9:05"));
    }

    private static void testLogLateDetection() {
        System.out.println("\n[AttendanceLog - Late Detection]");
        AttendanceLog late = new AttendanceLog("EMP101", "LOGIN", "29-Mar-2026", "09:15 AM");
        AttendanceLog onTime = new AttendanceLog("EMP102", "LOGIN", "29-Mar-2026", "08:45 AM");
        AttendanceLog exact = new AttendanceLog("EMP103", "LOGIN", "29-Mar-2026", "09:00 AM");
        AttendanceLog logout = new AttendanceLog("EMP104", "LOGOUT", "29-Mar-2026", "09:30 AM");

        assertTest("9:15 AM is late", late.isLoginAfter9AM());
        assertTest("8:45 AM is not late", !onTime.isLoginAfter9AM());
        assertTest("Exactly 9:00 AM is not late (boundary)", !exact.isLoginAfter9AM());
        assertTest("LOGOUT is never counted as late", !logout.isLoginAfter9AM());
    }

    private static void testLogToString() {
        System.out.println("\n[AttendanceLog - toString]");
        AttendanceLog log = new AttendanceLog("EMP101", "LOGIN", "29-Mar-2026", "09:05 AM");
        String str = log.toString();
        assertTest("toString contains employee ID", str.contains("EMP101"));
        assertTest("toString contains action", str.contains("LOGIN"));
        assertTest("toString contains date", str.contains("29-Mar-2026"));
    }

    // --- AttendanceLogAnalyzer tests ---

    private static void testParseAndAddLog() {
        System.out.println("\n[Analyzer - Parse and Add (4-part)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        assertTest("Log count is 1", analyzer.getAllLogs().size() == 1);
        assertTest("Employee ID matches", "EMP101".equals(analyzer.getAllLogs().get(0).getEmployeeId()));
    }

    private static void testParseAndAddLogLegacyFormat() {
        System.out.println("\n[Analyzer - Parse and Add (3-part legacy)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP102 | LOGIN | 08:45 AM");
        assertTest("Log count is 1", analyzer.getAllLogs().size() == 1);
        assertTest("Date defaults to N/A", "N/A".equals(analyzer.getAllLogs().get(0).getDate()));
    }

    private static void testParseAndAddLogInvalidFormat() {
        System.out.println("\n[Analyzer - Parse and Add (invalid)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("INVALID DATA");
        assertTest("Invalid format is skipped", analyzer.getAllLogs().size() == 0);
    }

    private static void testValidateAndAddLogSuccess() {
        System.out.println("\n[Analyzer - Validate and Add (success)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.validateAndAddLog("EMP101", "LOGIN", "29-Mar-2026", "09:05 AM");
        assertTest("Valid login added", analyzer.getAllLogs().size() == 1);

        analyzer.validateAndAddLog("EMP101", "LOGOUT", "29-Mar-2026", "05:30 PM");
        assertTest("Valid logout added", analyzer.getAllLogs().size() == 2);
    }

    private static void testValidateAndAddLogDuplicateLogin() {
        System.out.println("\n[Analyzer - Validate (duplicate login)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.validateAndAddLog("EMP101", "LOGIN", "29-Mar-2026", "09:05 AM");
        assertThrows("Duplicate LOGIN throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("EMP101", "LOGIN", "29-Mar-2026", "10:00 AM"));
    }

    private static void testValidateAndAddLogLogoutWithoutLogin() {
        System.out.println("\n[Analyzer - Validate (logout without login)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        assertThrows("LOGOUT without LOGIN throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("EMP101", "LOGOUT", "29-Mar-2026", "05:00 PM"));
    }

    private static void testValidateAndAddLogInvalidId() {
        System.out.println("\n[Analyzer - Validate (invalid ID)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        assertThrows("Empty ID throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("", "LOGIN", "29-Mar-2026", "09:00 AM"));
        assertThrows("Bad format throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("ABC123", "LOGIN", "29-Mar-2026", "09:00 AM"));
    }

    private static void testValidateAndAddLogInvalidAction() {
        System.out.println("\n[Analyzer - Validate (invalid action)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        assertThrows("Invalid action throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("EMP101", "BREAK", "29-Mar-2026", "09:00 AM"));
    }

    private static void testValidateAndAddLogUnsafeChars() {
        System.out.println("\n[Analyzer - Validate (unsafe chars)]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        assertThrows("Pipe in date throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("EMP101", "LOGIN", "29|Mar|2026", "09:00 AM"));
        assertThrows("Quote in time throws",
            IllegalArgumentException.class,
            () -> analyzer.validateAndAddLog("EMP101", "LOGIN", "29-Mar-2026", "09:00\"AM"));
    }

    private static void testGetLastAction() {
        System.out.println("\n[Analyzer - getLastAction]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        assertTest("No records returns null", analyzer.getLastAction("EMP101") == null);

        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        assertTest("After LOGIN returns LOGIN", "LOGIN".equals(analyzer.getLastAction("EMP101")));

        analyzer.parseAndAddLog("EMP101 | LOGOUT | 29-Mar-2026 | 05:30 PM");
        assertTest("After LOGOUT returns LOGOUT", "LOGOUT".equals(analyzer.getLastAction("EMP101")));
    }

    private static void testGetAllLogs() {
        System.out.println("\n[Analyzer - getAllLogs]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        analyzer.parseAndAddLog("EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM");
        assertTest("Returns all logs", analyzer.getAllLogs().size() == 2);
    }

    private static void testGetLogsAfter9AM() {
        System.out.println("\n[Analyzer - getLogsAfter9AM]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:15 AM");
        analyzer.parseAndAddLog("EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM");
        analyzer.parseAndAddLog("EMP103 | LOGIN | 29-Mar-2026 | 09:30 AM");
        List<AttendanceLog> late = analyzer.getLogsAfter9AM();
        assertTest("Finds 2 late logins", late.size() == 2);
    }

    private static void testCalculateDurations() {
        System.out.println("\n[Analyzer - calculateTotalDurations]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:00 AM");
        analyzer.parseAndAddLog("EMP101 | LOGOUT | 29-Mar-2026 | 05:00 PM");
        Map<String, String> durations = analyzer.calculateTotalDurations();
        assertTest("EMP101 has duration", durations.containsKey("EMP101"));
        assertTest("Duration is 8 hours", durations.get("EMP101").contains("8 hours"));
    }

    private static void testGetLogsByEmployeeId() {
        System.out.println("\n[Analyzer - getLogsByEmployeeId]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        analyzer.parseAndAddLog("EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM");
        analyzer.parseAndAddLog("EMP101 | LOGOUT | 29-Mar-2026 | 05:30 PM");
        List<AttendanceLog> logs = analyzer.getLogsByEmployeeId("EMP101");
        assertTest("Returns 2 logs for EMP101", logs.size() == 2);
        assertTest("Unknown employee returns empty", analyzer.getLogsByEmployeeId("EMP999").isEmpty());
    }

    private static void testEmployeeExists() {
        System.out.println("\n[Analyzer - employeeExists]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        assertTest("Existing employee found", analyzer.employeeExists("EMP101"));
        assertTest("Non-existing employee not found", !analyzer.employeeExists("EMP999"));
    }

    private static void testClearAllLogs() {
        System.out.println("\n[Analyzer - clearAllLogs]");
        AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
        analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
        analyzer.clearAllLogs();
        assertTest("Logs cleared", analyzer.getAllLogs().isEmpty());
    }

    private static void testSaveAndLoadTxt() {
        System.out.println("\n[Analyzer - Save/Load TXT]");
        try {
            AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
            analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
            analyzer.parseAndAddLog("EMP101 | LOGOUT | 29-Mar-2026 | 05:30 PM");

            String file = "target/test_roundtrip.txt";
            analyzer.saveToFile(file, "TXT");

            AttendanceLogAnalyzer loaded = new AttendanceLogAnalyzer();
            int count = loaded.loadFromFile(file);
            assertTest("TXT roundtrip count matches", count == 2);
            assertTest("TXT data matches", loaded.getAllLogs().size() == 2);
            new File(file).delete();
        } catch (Exception e) {
            System.out.println("  ❌ TXT roundtrip failed: " + e.getMessage());
            failed++;
        }
    }

    private static void testSaveAndLoadCsv() {
        System.out.println("\n[Analyzer - Save/Load CSV]");
        try {
            AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
            analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
            analyzer.parseAndAddLog("EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM");

            String file = "target/test_roundtrip.csv";
            analyzer.saveToFile(file, "CSV");

            AttendanceLogAnalyzer loaded = new AttendanceLogAnalyzer();
            int count = loaded.loadFromFile(file);
            assertTest("CSV roundtrip count matches", count == 2);
            assertTest("CSV data matches", loaded.getAllLogs().size() == 2);
            new File(file).delete();
        } catch (Exception e) {
            System.out.println("  ❌ CSV roundtrip failed: " + e.getMessage());
            failed++;
        }
    }

    private static void testSaveAndLoadJson() {
        System.out.println("\n[Analyzer - Save/Load JSON]");
        try {
            AttendanceLogAnalyzer analyzer = new AttendanceLogAnalyzer();
            analyzer.parseAndAddLog("EMP101 | LOGIN | 29-Mar-2026 | 09:05 AM");
            analyzer.parseAndAddLog("EMP101 | LOGOUT | 29-Mar-2026 | 05:30 PM");
            analyzer.parseAndAddLog("EMP102 | LOGIN | 29-Mar-2026 | 08:45 AM");

            String file = "target/test_roundtrip.json";
            analyzer.saveToFile(file, "JSON");

            AttendanceLogAnalyzer loaded = new AttendanceLogAnalyzer();
            int count = loaded.loadFromFile(file);
            assertTest("JSON roundtrip count matches", count == 3);
            assertTest("JSON data matches", loaded.getAllLogs().size() == 3);
            assertTest("JSON preserves employee ID",
                loaded.getAllLogs().stream().anyMatch(l -> "EMP101".equals(l.getEmployeeId())));
            new File(file).delete();
        } catch (Exception e) {
            System.out.println("  ❌ JSON roundtrip failed: " + e.getMessage());
            failed++;
        }
    }
}
