package com.attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Java Swing GUI for Employee Attendance Log Analyzer
 *
 * DATA STRUCTURES USED:
 * 1. ArrayList<AttendanceLog> - Dynamic array for storing attendance logs
 *    Significance: O(1) access, dynamic resizing, efficient iteration
 *
 * 2. DefaultTableModel - Swing's table data structure
 *    Significance: Automatic UI updates, easy data manipulation
 *
 * 3. HashMap<String, Integer> - For employee statistics (future enhancement)
 *    Significance: O(1) lookup for employee data
 *
 * EXCEPTION HANDLING:
 * - NumberFormatException: Invalid time format
 * - ArrayIndexOutOfBoundsException: Malformed log entries
 * - NullPointerException: Missing data validation
 * - IllegalArgumentException: Invalid input validation
 */
public class AttendanceGUI extends JFrame {

    // Core data structure - ArrayList for dynamic storage
    private AttendanceLogAnalyzer analyzer;

    // GUI Components
    private JTextField empIdField, actionField, timeField, dateField;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JTextArea outputArea;
    private JLabel statusLabel;

    // Constants
    private static final String[] COLUMN_NAMES = {"Employee ID", "Action", "Date", "Time"};
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Light blue theme
    private static final Color SUCCESS_COLOR = new Color(70, 130, 180); // Light blue theme
    private static final Color ERROR_COLOR = new Color(231, 76, 60);

    public AttendanceGUI() {
        analyzer = new AttendanceLogAnalyzer();
        initializeGUI();
        setLocationRelativeTo(null); // Center the window
    }

    /**
     * Initialize the complete GUI interface
     */
    private void initializeGUI() {
        setTitle("Employee Attendance Log Analyzer - Swing GUI");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Use BoxLayout for better control
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add components
        mainPanel.add(createHeaderPanel());
        mainPanel.add(createInputPanel());

        // Create scrollable center area
        JPanel centerArea = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePanel(),
                createOutputPanel());
        splitPane.setDividerLocation(180);
        splitPane.setResizeWeight(0.3);
        centerArea.add(splitPane, BorderLayout.CENTER);
        centerArea.setPreferredSize(new Dimension(1200, 550));
        mainPanel.add(centerArea);

        // Add button panel with fixed height
        JPanel buttonArea = createButtonPanel();
        buttonArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180)); // More height for 3 rows
        mainPanel.add(buttonArea);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Load sample data
        loadSampleData();
    }

    /**
     * Create header panel with title and info
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Employee Attendance Log Analyzer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // Instructional text removed for cleaner UI
        // JLabel infoLabel = new JLabel("Data Structure: ArrayList<AttendanceLog> | Auto-fills Date & Time on LOGIN/LOGOUT selection | Format: EMPXXX | LOGIN/LOGOUT | HH:mm AM/PM", SwingConstants.CENTER);
        // infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        // infoLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        // headerPanel.add(infoLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    /**
     * Create center panel with input form and table
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input panel
        centerPanel.add(createInputPanel(), BorderLayout.NORTH);

        // Split pane for table and output
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePanel(),
                createOutputPanel());
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3); // Give 30% to top, 70% to bottom
        centerPanel.add(splitPane, BorderLayout.CENTER);

        return centerPanel;
    }

    /**
     * Create input form panel
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Add New Attendance Log",
            0, 0, new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Employee ID and Action
        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        empIdField = new JTextField(15);
        empIdField.setToolTipText("Format: EMPXXX (e.g., EMP101)");
        inputPanel.add(empIdField, gbc);

        // Action
        gbc.gridx = 2; gbc.gridy = 0;
        inputPanel.add(new JLabel("Action:"), gbc);
        gbc.gridx = 3;
        String[] actions = {"LOGIN", "LOGOUT"};
        JComboBox<String> actionCombo = new JComboBox<>(actions);
        actionField = new JTextField(10);
        actionField.setText("LOGIN");
        // Auto-fill date and time when action is selected
        actionCombo.addActionListener(e -> captureCurrentDateTime());
        inputPanel.add(actionCombo, gbc);

        // Row 2: Date and Time
        // Date
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        dateField.setToolTipText("Auto-filled when LOGIN/LOGOUT is selected");
        inputPanel.add(dateField, gbc);

        // Time
        gbc.gridx = 2; gbc.gridy = 1;
        inputPanel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 3;
        timeField = new JTextField(15);
        timeField.setEditable(false); // Make field non-editable
        timeField.setBackground(Color.WHITE); // Keep white background
        timeField.setToolTipText("Auto-filled when LOGIN/LOGOUT is selected");
        inputPanel.add(timeField, gbc);

        // Capture Time button (now labeled "Refresh")
        gbc.gridx = 4; gbc.gridy = 1;
        JButton captureTimeButton = new JButton("Refresh");
        captureTimeButton.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Remove macOS styling
        captureTimeButton.setBackground(PRIMARY_COLOR);
        captureTimeButton.setForeground(Color.WHITE);
        captureTimeButton.setOpaque(true);
        captureTimeButton.setContentAreaFilled(true);
        captureTimeButton.setBorderPainted(false);
        captureTimeButton.setFocusPainted(false);
        captureTimeButton.setFont(new Font("Arial", Font.BOLD, 12));
        captureTimeButton.setPreferredSize(new Dimension(100, 30));
        captureTimeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        captureTimeButton.addActionListener(e -> captureCurrentDateTime());
        captureTimeButton.setToolTipText("Click to refresh date & time to current values");
        inputPanel.add(captureTimeButton, gbc);

        // Add button
        gbc.gridx = 5; gbc.gridy = 1;
        JButton addButton = new JButton("Add Log");
        addButton.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Remove macOS styling
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setContentAreaFilled(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setPreferredSize(new Dimension(100, 30));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addLog((String) actionCombo.getSelectedItem()));
        inputPanel.add(addButton, gbc);

        // Auto-fill on initialization
        captureCurrentDateTime();

        return inputPanel;
    }

    /**
     * Create table panel to display logs
     */
    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        logTable.setRowHeight(25);
        logTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        logTable.getTableHeader().setBackground(PRIMARY_COLOR);
        logTable.getTableHeader().setForeground(Color.WHITE);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Attendance Logs (ArrayList<AttendanceLog>)"));

        return scrollPane;
    }

    /**
     * Create output text area panel
     */
    private JScrollPane createOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Analysis Output"));

        return scrollPane;
    }

    /**
     * Create button panel for operations
     */
    private JPanel createButtonPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setPreferredSize(new Dimension(1200, 180)); // More space for 3rd row
        outerPanel.setMinimumSize(new Dimension(800, 160));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 15, 10)); // 3 rows, 3 columns
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        buttonPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        // Row 1: Analysis buttons
        JButton displayAllBtn = createStyledButton("Display All Logs", PRIMARY_COLOR);
        JButton lateLoginBtn = createStyledButton("Find Late Logins (>9AM)", PRIMARY_COLOR);
        JButton sortBtn = createStyledButton("Sort by Employee ID", PRIMARY_COLOR);

        // Row 2: Statistics and Clear
        JButton statsBtn = createStyledButton("Show Statistics", PRIMARY_COLOR);
        JButton clearBtn = createStyledButton("Clear All", PRIMARY_COLOR);
        JButton exitBtn = createStyledButton("Exit", PRIMARY_COLOR);

        // Row 3: File Operations (NEW!)
        JButton loadBtn = createStyledButton("Load from File", PRIMARY_COLOR);
        JButton saveBtn = createStyledButton("Save to File", PRIMARY_COLOR);
        JButton saveAsBtn = createStyledButton("Save As...", PRIMARY_COLOR);

        // Add action listeners - Analysis
        displayAllBtn.addActionListener(e -> displayAllLogs());
        lateLoginBtn.addActionListener(e -> findLateLogins());
        sortBtn.addActionListener(e -> sortLogs());

        // Statistics and control
        statsBtn.addActionListener(e -> showStatistics());
        clearBtn.addActionListener(e -> clearAllLogs());
        exitBtn.addActionListener(e -> exitApplication());

        // File operations (NEW!)
        loadBtn.addActionListener(e -> loadFromFile());
        saveBtn.addActionListener(e -> saveToFile(false));
        saveAsBtn.addActionListener(e -> saveToFile(true));

        // Add buttons to panel (3x3 grid)
        buttonPanel.add(displayAllBtn);
        buttonPanel.add(lateLoginBtn);
        buttonPanel.add(sortBtn);
        buttonPanel.add(statsBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exitBtn);
        buttonPanel.add(loadBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(saveAsBtn);

        // Status label
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        outerPanel.add(buttonPanel, BorderLayout.NORTH);
        outerPanel.add(statusLabel, BorderLayout.SOUTH);

        return outerPanel;
    }

   private JButton createStyledButton(String text, Color bgColor) {
    JButton button = new JButton(text);

    button.setFont(new Font("Arial", Font.BOLD, 14));
    button.setForeground(Color.WHITE);

    // 🔥 CRITICAL FIXES
    button.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // REMOVE macOS styling
    button.setBackground(bgColor);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(false);

    button.setFocusPainted(false);
    button.setPreferredSize(new Dimension(200, 45));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    return button;
}
    /**
     * Capture current date and time in Indian Standard Time (IST) and fill both fields
     */
    private void captureCurrentDateTime() {
        try {
            // Get current date and time in IST timezone
            ZonedDateTime istDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            // Format date as dd-MMM-yyyy (e.g., 21-Mar-2026)
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            String formattedDate = istDateTime.format(dateFormatter);

            // Format time as hh:mm a (e.g., 09:45 AM) with Locale.ENGLISH
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            String formattedTime = istDateTime.format(timeFormatter).toUpperCase(); // Ensure uppercase AM/PM

            // Set the date and time in the fields
            dateField.setText(formattedDate);
            timeField.setText(formattedTime);

            // Only update status if statusLabel has been initialized
            if (statusLabel != null) {
                updateStatus("Date & Time captured: " + formattedDate + " " + formattedTime + " IST", SUCCESS_COLOR);
            }

        } catch (Exception e) {
            // Only show error dialog if GUI is fully initialized
            if (statusLabel != null) {
                showError("Date/Time Capture Error", "Failed to capture current date/time: " + e.getMessage());
            } else {
                System.err.println("Date/Time Capture Error: " + e.getMessage());
            }
        }
    }

    /**
     * Add new attendance log with exception handling
     */
    private void addLog(String action) {
        try {
            // Validate inputs
            String empId = empIdField.getText().trim();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();

            // Exception handling: Validate required fields
            if (empId.isEmpty() || date.isEmpty() || time.isEmpty()) {
                throw new IllegalArgumentException("All fields are required!");
            }

            // Exception handling: Validate employee ID format
            if (!empId.matches("EMP\\d{3}")) {
                throw new IllegalArgumentException("Employee ID must be in format EMPXXX (e.g., EMP101)");
            }

            // Exception handling: Validate time format
            if (!time.matches("\\d{1,2}:\\d{2}\\s(AM|PM)")) {
                throw new IllegalArgumentException("Time must be in format HH:mm AM/PM (e.g., 09:15 AM)");
            }

            // Validate time values
            String[] parts = time.split("[:\\s]+");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Invalid time values! Hour: 1-12, Minute: 0-59");
            }

            // VALIDATION: Check employee's last action to enforce login/logout rules
            String lastAction = null;
            List<AttendanceLog> allLogs = analyzer.getAllLogs();

            // Find the last action for this employee
            for (int i = allLogs.size() - 1; i >= 0; i--) {
                if (allLogs.get(i).getEmployeeId().equals(empId)) {
                    lastAction = allLogs.get(i).getAction();
                    break;
                }
            }

            // Rule 1: Cannot LOGIN if already logged in
            if (action.equals("LOGIN") && "LOGIN".equals(lastAction)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Employee " + empId + " is already logged in. Please logout first.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                updateStatus("Cannot login - Employee already logged in", ERROR_COLOR);
                return; // Stop execution, don't add log
            }

            // Rule 2: Cannot LOGOUT if not logged in
            if (action.equals("LOGOUT") && !"LOGIN".equals(lastAction)) {
                String message = lastAction == null ?
                    "Employee " + empId + " is not logged in. Cannot logout." :
                    "Employee " + empId + " is already logged out. Please login first.";

                JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                updateStatus("Cannot logout - Employee not logged in", ERROR_COLOR);
                return; // Stop execution, don't add log
            }

            // Validation passed - create log entry
            String logEntry = String.format("%s | %s | %s", empId, action, time);
            analyzer.parseAndAddLog(logEntry);

            // Update table with date included
            tableModel.addRow(new Object[]{empId, action, date, time});

            // Clear employee ID field only (date and time will auto-refresh)
            empIdField.setText("");

            updateStatus("Log added successfully! " + empId + " " + action, SUCCESS_COLOR);

        } catch (NumberFormatException e) {
            showError("Number Format Error", "Invalid time format! Use HH:mm AM/PM");
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            showError("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display all logs in the output area
     */
    private void displayAllLogs() {
        try {
            outputArea.setText("");
            outputArea.append("=== ALL ATTENDANCE LOGS ===\n");
            outputArea.append("Data Structure: ArrayList<AttendanceLog>\n");
            outputArea.append("Size: " + tableModel.getRowCount() + " entries\n");
            outputArea.append("=" .repeat(50) + "\n\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String empId = (String) tableModel.getValueAt(i, 0);
                String action = (String) tableModel.getValueAt(i, 1);
                String date = (String) tableModel.getValueAt(i, 2);
                String time = (String) tableModel.getValueAt(i, 3);
                outputArea.append(String.format("%s | %s | %s | %s\n", empId, action, date, time));
            }

            updateStatus("Displayed all logs", PRIMARY_COLOR);

        } catch (Exception e) {
            showError("Display Error", "Error displaying logs: " + e.getMessage());
        }
    }

    /**
     * Find and display employees who logged in after 9:00 AM
     */
    private void findLateLogins() {
        try {
            outputArea.setText("");
            outputArea.append("=== LATE LOGINS (After 9:00 AM) ===\n");
            outputArea.append("Using Stream API with filter() method\n");
            outputArea.append("=" .repeat(50) + "\n\n");

            List<AttendanceLog> lateLogs = analyzer.getLogsAfter9AM();

            if (lateLogs.isEmpty()) {
                outputArea.append("No employees logged in after 9:00 AM\n");
            } else {
                for (AttendanceLog log : lateLogs) {
                    outputArea.append(log.toString() + "\n");
                }
                outputArea.append("\nTotal late logins: " + lateLogs.size() + "\n");
            }

            updateStatus("Found " + lateLogs.size() + " late login(s)", new Color(243, 156, 18));

        } catch (Exception e) {
            showError("Analysis Error", "Error finding late logins: " + e.getMessage());
        }
    }

    /**
     * Sort logs by Employee ID using Comparator
     */
    private void sortLogs() {
        try {
            analyzer.sortLogsByEmployeeId();

            // Get current date for sorted entries (since AttendanceLog doesn't store date)
            ZonedDateTime istDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            String currentDate = istDateTime.format(dateFormatter);

            // Clear and reload table
            tableModel.setRowCount(0);
            List<AttendanceLog> sortedLogs = analyzer.getAllLogs();

            for (AttendanceLog log : sortedLogs) {
                tableModel.addRow(new Object[]{
                    log.getEmployeeId(),
                    log.getAction(),
                    currentDate,  // Use current date as placeholder
                    log.getTime()
                });
            }

            outputArea.setText("");
            outputArea.append("=== LOGS SORTED BY EMPLOYEE ID ===\n");
            outputArea.append("Using Comparator.comparing() method\n");
            outputArea.append("=" + "=".repeat(50) + "\n\n");
            outputArea.append("Logs have been sorted in the table above.\n");
            outputArea.append("Data structure maintains sorted order in ArrayList.\n");

            updateStatus("Logs sorted by Employee ID", new Color(155, 89, 182));

        } catch (Exception e) {
            showError("Sort Error", "Error sorting logs: " + e.getMessage());
        }
    }

    /**
     * Display attendance statistics
     */
    private void showStatistics() {
        try {
            outputArea.setText("");
            outputArea.append("=== ATTENDANCE STATISTICS ===\n");
            outputArea.append("Computed using Stream API\n");
            outputArea.append("=".repeat(50) + "\n\n");

            List<AttendanceLog> allLogs = analyzer.getAllLogs();
            int totalLogs = allLogs.size();

            long loginCount = allLogs.stream()
                .filter(log -> "LOGIN".equals(log.getAction()))
                .count();

            long logoutCount = allLogs.stream()
                .filter(log -> "LOGOUT".equals(log.getAction()))
                .count();

            // Get unique employees using HashSet
            Set<String> uniqueEmployees = new HashSet<>();
            allLogs.forEach(log -> uniqueEmployees.add(log.getEmployeeId()));

            outputArea.append(String.format("Total Log Entries: %d\n", totalLogs));
            outputArea.append(String.format("LOGIN Entries: %d\n", loginCount));
            outputArea.append(String.format("LOGOUT Entries: %d\n", logoutCount));
            outputArea.append(String.format("Unique Employees: %d\n", uniqueEmployees.size()));
            outputArea.append("\n");
            outputArea.append("Data Structures Used:\n");
            outputArea.append("- ArrayList<AttendanceLog> for storage\n");
            outputArea.append("- HashSet<String> for unique employee count\n");
            outputArea.append("- Stream API for filtering and counting\n");

            updateStatus("Statistics calculated", new Color(52, 73, 94));

        } catch (Exception e) {
            showError("Statistics Error", "Error calculating statistics: " + e.getMessage());
        }
    }

    /**
     * Clear all logs with confirmation
     */
    private void clearAllLogs() {
        try {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all logs?\nThis action cannot be undone.",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                analyzer.clearAllLogs();
                tableModel.setRowCount(0);
                outputArea.setText("");
                loadSampleData(); // Reload sample data
                updateStatus("All logs cleared and sample data reloaded", ERROR_COLOR);
            }

        } catch (Exception e) {
            showError("Clear Error", "Error clearing logs: " + e.getMessage());
        }
    }

    /**
     * Load logs from file
     */
    private void loadFromFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Attendance Logs");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Attendance Files (*.txt, *.csv, *.json)", "txt", "csv", "json"));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();

                // Ask if user wants to append or replace
                String[] options = {"Replace All", "Append to Existing", "Cancel"};
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Do you want to replace all logs or append to existing logs?",
                    "Load Mode",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );

                if (choice == 0) {
                    // Replace all
                    analyzer.clearAllLogs();
                    tableModel.setRowCount(0);
                }

                if (choice != 2) { // Not cancelled
                    int count = analyzer.loadFromFile(filename);

                    // Get current date for loaded entries
                    ZonedDateTime istDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
                    String currentDate = istDateTime.format(dateFormatter);

                    // Refresh table
                    tableModel.setRowCount(0);
                    List<AttendanceLog> logs = analyzer.getAllLogs();
                    for (AttendanceLog log : logs) {
                        tableModel.addRow(new Object[]{
                            log.getEmployeeId(),
                            log.getAction(),
                            currentDate,  // Use current date as placeholder
                            log.getTime()
                        });
                    }

                    updateStatus("Loaded " + count + " logs from " + filename, SUCCESS_COLOR);
                    JOptionPane.showMessageDialog(
                        this,
                        "Successfully loaded " + count + " attendance logs!",
                        "Load Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (java.io.FileNotFoundException e) {
            showError("File Not Found", "The selected file could not be found:\n" + e.getMessage());
        } catch (Exception e) {
            showError("Load Error", "Error loading file:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save logs to file
     */
    private void saveToFile(boolean saveAs) {
        try {
            // Select file format
            String[] formats = {"TXT", "CSV", "JSON"};
            String format = (String) JOptionPane.showInputDialog(
                this,
                "Select file format:",
                "File Format",
                JOptionPane.QUESTION_MESSAGE,
                null,
                formats,
                formats[0]
            );

            if (format == null) return; // User cancelled

            // Select file location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Attendance Logs");

            // Set default extension
            String extension = format.toLowerCase();
            fileChooser.setSelectedFile(new java.io.File("attendance_logs." + extension));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                format + " Files (*." + extension + ")", extension));

            int result = fileChooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();

                // Add extension if missing
                if (!filename.toLowerCase().endsWith("." + extension)) {
                    filename += "." + extension;
                }

                // Check if file exists
                java.io.File file = new java.io.File(filename);
                if (file.exists() && !saveAs) {
                    String[] options = {"Overwrite", "Append", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        "File already exists. Do you want to overwrite or append?",
                        "File Exists",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice == 2) return; // Cancel

                    if (choice == 1) { // Append
                        analyzer.appendToFile(filename, format);
                        updateStatus("Appended logs to " + filename, SUCCESS_COLOR);
                        JOptionPane.showMessageDialog(
                            this,
                            "Successfully appended logs to file!",
                            "Save Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                }

                // Save/Overwrite
                analyzer.saveToFile(filename, format);
                updateStatus("Saved " + tableModel.getRowCount() + " logs to " + filename, SUCCESS_COLOR);
                JOptionPane.showMessageDialog(
                    this,
                    "Successfully saved " + tableModel.getRowCount() + " logs to:\n" + filename,
                    "Save Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception e) {
            showError("Save Error", "Error saving file:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exit application with confirmation
     */
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("Application closed successfully.");
            System.exit(0);
        }
    }

    /**
     * Load sample data for demonstration
     */
    private void loadSampleData() {
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

        // Get current date for sample data
        ZonedDateTime istDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String currentDate = istDateTime.format(dateFormatter);

        for (String log : sampleLogs) {
            analyzer.parseAndAddLog(log);
            String[] parts = log.split(" \\| ");
            tableModel.addRow(new Object[]{parts[0], parts[1], currentDate, parts[2]});
        }

        updateStatus("Sample data loaded: " + sampleLogs.length + " logs", SUCCESS_COLOR);
    }

    /**
     * Update status label
     */
    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
        updateStatus("Error: " + title, ERROR_COLOR);
    }

    /**
     * Main method to launch the GUI
     */
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AttendanceGUI gui = new AttendanceGUI();
            gui.setVisible(true);
        });
    }
}
