#!/bin/bash

# Employee Attendance Analyzer - Build Script

PROJECT_DIR="$(dirname "$0")"
SRC_DIR="$PROJECT_DIR/src/main/java"
CLASS_DIR="$PROJECT_DIR/target/classes"

echo "=== Employee Attendance Analyzer Build Script ==="

# Create target directory
mkdir -p "$CLASS_DIR"

# Compile all Java files
echo "Compiling Java source files..."
javac -d "$CLASS_DIR" "$SRC_DIR/com/attendance/"*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "Available commands:"
    echo "  ./run.sh gui       - Launch the Java Swing GUI interface (RECOMMENDED)"
    echo "  ./run.sh analyzer  - Run the console-based attendance log analyzer"
    echo ""
else
    echo "❌ Compilation failed!"
    exit 1
fi