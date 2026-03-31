# Employee Attendance Log Analyzer

A **Java Swing GUI application** for managing and analyzing employee attendance records with real-time validation, automatic date/time capture, and file operations.

---

## 📋 About the Project

This application provides a modern graphical interface for tracking employee login and logout times. It enforces proper login/logout sequences, automatically captures timestamps in Indian Standard Time (IST), and provides analytics features like late login detection and attendance statistics.

---

## ✨ Features

- **Smart Login/Logout Validation** - Prevents duplicate logins and invalid logouts
- **Auto Date/Time Capture** - Automatically fills current date and time in IST timezone
- **Late Login Detection** - Identifies employees who logged in after 9:00 AM
- **Statistics Dashboard** - Shows total logins, logouts, and unique employee count
- **Sort by Employee ID** - Organizes attendance logs alphabetically
- **File Operations** - Load and save attendance data in TXT, CSV, and JSON formats
- **Data Validation** - Ensures proper format for Employee ID (EMPXXX) and time (HH:mm AM/PM)
- **User-Friendly Interface** - Color-coded buttons, status indicators, and error dialogs

---

## 🛠️ Technologies Used

- **Java 11+** - Core programming language
- **Java Swing** - GUI framework for desktop application
- **ArrayList** - Dynamic storage for attendance logs
- **HashSet** - Tracking unique employees
- **Stream API** - Filtering and data processing
- **LocalTime/ZonedDateTime** - Time comparison and timezone handling
- **File I/O** - Reading and writing attendance data

---

## 📁 Project Structure

```
employee-attendance-analyzer/
├── src/main/java/com/attendance/
│   ├── AttendanceGUI.java           # Main GUI application
│   ├── AttendanceLog.java           # Data model class
│   ├── AttendanceLogAnalyzer.java   # Business logic & file operations

├── .gitignore                       # Git ignore rules
├── build.sh                         # Build script
├── run.sh                          # Run script
└── README.md                       # This file
```

---

## 🚀 How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or higher installed
- Terminal/Command Prompt

### Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/<your-username>/employee-attendance-analyzer.git
cd employee-attendance-analyzer
```

#### 2. Build the Project
```bash
chmod +x build.sh
./build.sh
```

#### 3. Run the Application
```bash
chmod +x run.sh
./run.sh
```

### For Windows Users

**Build:**
```cmd
javac -d target/classes src/main/java/com/attendance/*.java
```

**Run:**
```cmd
java -cp target/classes com.attendance.AttendanceGUI
```

---

## 📖 Usage

### Adding a Log Entry

1. Enter **Employee ID** (format: EMPXXX, e.g., EMP101)
2. Select **Action** (LOGIN or LOGOUT)
3. Date and Time are **auto-filled** with current IST time
4. Click **"Add Log"** button

### Validation Rules

✅ **Allowed:**
- First LOGIN for an employee
- LOGIN after LOGOUT
- LOGOUT after LOGIN

❌ **Blocked:**
- LOGIN → LOGIN (error: "Employee already logged in")
- LOGOUT without LOGIN (error: "Employee is not logged in")

### Features

- **Display All Logs** - View complete attendance history
- **Find Late Logins** - Filter employees who arrived after 9:00 AM
- **Sort by Employee ID** - Organize logs alphabetically
- **Show Statistics** - View total logins, logouts, and unique employees
- **Load from File** - Import logs from TXT, CSV, or JSON
- **Save to File** - Export logs in TXT, CSV, or JSON format
- **Clear All** - Reset all data (with confirmation)

---

## 🔧 Technical Highlights

### Data Structures Used

- **ArrayList<AttendanceLog>** - Stores all attendance logs with O(1) access
- **HashSet<String>** - Tracks unique employees efficiently
- **DefaultTableModel** - Manages GUI table data with automatic updates
- **Stream API** - Functional filtering and counting operations

### Exception Handling

- **IllegalArgumentException** - Invalid input formats
- **NumberFormatException** - Invalid time values
- **FileNotFoundException** - Missing files during load operation
- **IOException** - File read/write errors
- **Generic Exception** - Unexpected runtime errors

All exceptions display user-friendly error dialogs with clear messages.

---

## 📝 Sample Data

The application loads sample attendance data on startup:

```
EMP101 | LOGIN  | 09:05 AM
EMP102 | LOGIN  | 08:45 AM
EMP103 | LOGIN  | 09:15 AM
EMP101 | LOGOUT | 05:30 PM
EMP105 | LOGIN  | 08:30 AM
EMP104 | LOGIN  | 09:30 AM
EMP102 | LOGOUT | 06:00 PM
EMP103 | LOGOUT | 05:45 PM
```

---

## 📄 License

This project is created for educational purposes.

---

## 👨‍💻 Author

Created as a Java programming assignment demonstrating GUI development, data structures, and exception handling.

---

