package com.attendance;

/**
 * Demonstration of substring() usage for extracting employee ID
 * This shows different ways to use substring() as required in the task
 */
public class SubstringDemo {
    public static void main(String[] args) {
        // Sample log entries
        String[] logEntries = {
            "EMP101 | LOGIN | 09:05 AM",
            "EMP102 | LOGOUT | 05:30 PM",
            "TEMP_EMP999 | LOGIN | 08:00 AM"
        };

        System.out.println("=== Demonstrating substring() usage for Employee ID extraction ===");

        for (String log : logEntries) {
            System.out.println("\\nOriginal log: " + log);

            // Method 1: Find the first "|" and extract substring before it
            int pipeIndex = log.indexOf(" |");
            if (pipeIndex > 0) {
                String empId1 = log.substring(0, pipeIndex);
                System.out.println("Method 1 - substring(0, " + pipeIndex + "): " + empId1);
            }

            // Method 2: Split and then use substring on the first part
            String[] parts = log.split(" \\| ");
            if (parts.length > 0) {
                String empId2 = parts[0].substring(0); // Extract from beginning
                System.out.println("Method 2 - after split, substring(0): " + empId2);

                // Method 3: Extract only numeric part using substring
                if (parts[0].startsWith("EMP")) {
                    String numericPart = parts[0].substring(3); // Skip "EMP"
                    System.out.println("Method 3 - numeric part, substring(3): " + numericPart);
                }

                // Method 4: Extract prefix using substring
                if (parts[0].length() >= 3) {
                    String prefix = parts[0].substring(0, 3);
                    System.out.println("Method 4 - prefix, substring(0, 3): " + prefix);
                }
            }
        }
    }
}