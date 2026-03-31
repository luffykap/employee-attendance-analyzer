#!/bin/bash

# Employee Attendance Analyzer - Run Script

PROJECT_DIR="$(dirname "$0")"
CLASS_DIR="$PROJECT_DIR/target/classes"

if [ ! -d "$CLASS_DIR" ]; then
    echo "Classes not found. Run ./build.sh first."
    exit 1
fi

case "$1" in
    "gui")
        echo "Launching Swing GUI..."
        echo ""
        java -cp "$CLASS_DIR" com.attendance.AttendanceGUI
        ;;
    "analyzer")
        echo "Running AttendanceLogAnalyzer..."
        echo ""
        java -cp "$CLASS_DIR" com.attendance.AttendanceLogAnalyzer
        ;;
    *)
        echo "Usage: $0 {gui|analyzer}"
        echo ""
        echo "  gui       - Launch the Java Swing GUI interface (RECOMMENDED)"
        echo "  analyzer  - Run the console-based attendance log analyzer"
        exit 1
        ;;
esac