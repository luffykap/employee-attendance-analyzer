package com.attendance;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AttendanceGUI extends JFrame {

    private AttendanceLogAnalyzer analyzer;
    private JTextField empIdField, actionField, timeField, dateField;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JTextArea outputArea;
    private JLabel statusLabel;

    private static final String[] COLUMN_NAMES = {"Employee ID", "Action", "Date", "Time"};
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);

    public AttendanceGUI() {
        analyzer = new AttendanceLogAnalyzer();
        initializeGUI();
        setLocationRelativeTo(null);
    }

    private void initializeGUI() {
        setTitle("Employee Attendance Log Analyzer - Swing GUI");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(createHeaderPanel());
        mainPanel.add(createInputPanel());

        JPanel centerArea = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePanel(),
                createOutputPanel());
        splitPane.setDividerLocation(180);
        splitPane.setResizeWeight(0.3);
        centerArea.add(splitPane, BorderLayout.CENTER);
        centerArea.setPreferredSize(new Dimension(1200, 550));
        mainPanel.add(centerArea);

        JPanel buttonArea = createButtonPanel();
        buttonArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        mainPanel.add(buttonArea);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        loadSampleData();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Employee Attendance Log Analyzer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

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

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        empIdField = new JTextField(15);
        empIdField.setToolTipText("Format: EMPXXX (e.g., EMP101)");
        inputPanel.add(empIdField, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        inputPanel.add(new JLabel("Action:"), gbc);
        gbc.gridx = 3;
        String[] actions = {"LOGIN", "LOGOUT"};
        JComboBox<String> actionCombo = new JComboBox<>(actions);
        actionCombo.addActionListener(e -> captureCurrentDateTime());
        inputPanel.add(actionCombo, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        inputPanel.add(dateField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        inputPanel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 3;
        timeField = new JTextField(15);
        timeField.setEditable(false);
        timeField.setBackground(Color.WHITE);
        inputPanel.add(timeField, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        JButton captureTimeButton = createStyledButton("Refresh", PRIMARY_COLOR);
        captureTimeButton.setPreferredSize(new Dimension(100, 30));
        captureTimeButton.addActionListener(e -> captureCurrentDateTime());
        inputPanel.add(captureTimeButton, gbc);

        gbc.gridx = 5; gbc.gridy = 1;
        JButton addButton = createStyledButton("Add Log", PRIMARY_COLOR);
        addButton.setPreferredSize(new Dimension(100, 30));
        addButton.addActionListener(e -> addLog((String) actionCombo.getSelectedItem()));
        inputPanel.add(addButton, gbc);

        captureCurrentDateTime();
        return inputPanel;
    }

    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        logTable.setRowHeight(25);
        logTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        logTable.getTableHeader().setBackground(PRIMARY_COLOR);
        logTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Attendance Logs (TreeMap Storage)"));
        return scrollPane;
    }

    private JScrollPane createOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Analysis Output"));
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setPreferredSize(new Dimension(1200, 180));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 15, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JButton displayAllBtn = createStyledButton("Display All Logs", PRIMARY_COLOR);
        JButton lateLoginBtn = createStyledButton("Find Late Logins (>9AM)", PRIMARY_COLOR);
        JButton durationBtn = createStyledButton("Calculate Durations", SUCCESS_COLOR);

        JButton statsBtn = createStyledButton("Show Statistics", PRIMARY_COLOR);
        JButton clearBtn = createStyledButton("Clear All", PRIMARY_COLOR);
        JButton exitBtn = createStyledButton("Exit", PRIMARY_COLOR);

        JButton loadBtn = createStyledButton("Load from File", PRIMARY_COLOR);
        JButton saveBtn = createStyledButton("Save to File", PRIMARY_COLOR);
        JButton saveAsBtn = createStyledButton("Save As...", PRIMARY_COLOR);

        displayAllBtn.addActionListener(e -> displayAllLogs());
        lateLoginBtn.addActionListener(e -> findLateLogins());
        durationBtn.addActionListener(e -> calculateDurations());
        statsBtn.addActionListener(e -> showStatistics());
        clearBtn.addActionListener(e -> clearAllLogs());
        exitBtn.addActionListener(e -> exitApplication());
        
        loadBtn.addActionListener(e -> loadFromFile());
        saveBtn.addActionListener(e -> saveToFile(false));
        saveAsBtn.addActionListener(e -> saveToFile(true));

        buttonPanel.add(displayAllBtn);
        buttonPanel.add(lateLoginBtn);
        buttonPanel.add(durationBtn);
        buttonPanel.add(statsBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exitBtn);
        buttonPanel.add(loadBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(saveAsBtn);

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
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void captureCurrentDateTime() {
        try {
            ZonedDateTime istDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            dateField.setText(istDateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)));
            timeField.setText(istDateTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)).toUpperCase());
            updateStatus("Refreshed current date and time", PRIMARY_COLOR);
        } catch (Exception e) {
            showError("Date Error", e.getMessage());
        }
    }

    private void addLog(String action) {
        try {
            String empId = empIdField.getText().trim();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();

            if (empId.isEmpty()) throw new IllegalArgumentException("Employee ID is required!");
            if (!empId.matches("EMP\\d{3}")) throw new IllegalArgumentException("Format must be EMPXXX");

            String logEntry = String.format("%s | %s | %s", empId, action, time);
            analyzer.parseAndAddLog(logEntry);

            refreshTable();
            empIdField.setText("");
            updateStatus("Log added successfully!", SUCCESS_COLOR);

        } catch (Exception e) {
            showError("Input Error", e.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String currentDate = dateField.getText();
        for (AttendanceLog log : analyzer.getAllLogs()) {
            tableModel.addRow(new Object[]{log.getEmployeeId(), log.getAction(), currentDate, log.getTimeAsString()});
        }
    }

    private void calculateDurations() {
        outputArea.setText("=== TOTAL SESSION DURATIONS ===\n");
        Map<String, String> durations = analyzer.calculateTotalDurations();

        if (durations.isEmpty()) {
            outputArea.append("No session data available.\n");
        } else {
            durations.forEach((empId, timeStr) -> {
                outputArea.append(String.format("Employee ID: %s | Total Time: %s\n", empId, timeStr));
            });
        }
        updateStatus("Durations calculated using TreeMap logic", SUCCESS_COLOR);
    }

    private void displayAllLogs() {
        outputArea.setText("=== ALL ATTENDANCE LOGS ===\n");
        analyzer.getAllLogs().forEach(log -> outputArea.append(log.toString() + "\n"));
        updateStatus("Displayed all logs", PRIMARY_COLOR);
    }

    private void findLateLogins() {
        outputArea.setText("=== LATE LOGINS (>9AM) ===\n");
        List<AttendanceLog> lateLogs = analyzer.getLogsAfter9AM();
        if (lateLogs.isEmpty()) {
            outputArea.append("No late logins found.\n");
        } else {
            lateLogs.forEach(log -> outputArea.append(log.toString() + "\n"));
        }
        updateStatus("Found " + lateLogs.size() + " late logins", new Color(243, 156, 18));
    }

    private void showStatistics() {
        outputArea.setText("=== STATISTICS ===\n");
        List<AttendanceLog> allLogs = analyzer.getAllLogs();
        outputArea.append("Total Logs: " + allLogs.size() + "\n");
        
        long logins = allLogs.stream().filter(l -> l.getAction().equals("LOGIN")).count();
        long logouts = allLogs.stream().filter(l -> l.getAction().equals("LOGOUT")).count();
        
        outputArea.append("Logins: " + logins + "\n");
        outputArea.append("Logouts: " + logouts + "\n");
        updateStatus("Stats generated", PRIMARY_COLOR);
    }

    private void clearAllLogs() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all logs?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            analyzer.clearAllLogs();
            refreshTable();
            outputArea.setText("");
            updateStatus("All logs cleared", ERROR_COLOR);
        }
    }
    
    // --- FILE OPERATIONS ---
    
    private void loadFromFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                analyzer.clearAllLogs(); // Clear existing before load
                int count = analyzer.loadFromFile(fileChooser.getSelectedFile().getAbsolutePath());
                refreshTable();
                updateStatus("Loaded " + count + " logs", SUCCESS_COLOR);
            }
        } catch (Exception e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void saveToFile(boolean saveAs) {
        try {
            String[] formats = {"TXT", "CSV", "JSON"};
            String format = (String) JOptionPane.showInputDialog(this, "Select format:", "Format", JOptionPane.QUESTION_MESSAGE, null, formats, formats[0]);
            if (format == null) return;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("attendance." + format.toLowerCase()));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                analyzer.saveToFile(fileChooser.getSelectedFile().getAbsolutePath(), format);
                updateStatus("Saved successfully", SUCCESS_COLOR);
            }
        } catch (Exception e) {
            showError("Save Error", e.getMessage());
        }
    }

    private void exitApplication() {
        if (JOptionPane.showConfirmDialog(this, "Exit Application?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void loadSampleData() {
        String[] sampleLogs = {
                "EMP101 | LOGIN | 09:05 AM", "EMP102 | LOGIN | 08:45 AM",
                "EMP103 | LOGIN | 09:15 AM", "EMP101 | LOGOUT | 05:30 PM",
                "EMP105 | LOGIN | 08:30 AM", "EMP104 | LOGIN | 09:30 AM",
                "EMP102 | LOGOUT | 06:00 PM", "EMP103 | LOGOUT | 05:45 PM"
        };
        for (String log : sampleLogs) {
            analyzer.parseAndAddLog(log);
        }
        refreshTable();
        updateStatus("Sample data loaded", SUCCESS_COLOR);
    }

    private void updateStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        updateStatus("Error: " + title, ERROR_COLOR);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AttendanceGUI().setVisible(true));
    }
}