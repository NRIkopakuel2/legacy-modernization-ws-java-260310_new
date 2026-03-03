#!/bin/bash

# JDK Binary Check Script
# Validates that the required JDK binary file exists before Dev Container build

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JDK_FILE="${SCRIPT_DIR}/jdk-1_5_0_22-linux-amd64-rpm.bin"

echo "════════════════════════════════════════════════════════════"
echo "  Dev Container Setup Validation"
echo "════════════════════════════════════════════════════════════"
echo ""

# Check if JDK binary exists
if [ -f "$JDK_FILE" ]; then
    FILE_SIZE=$(stat -f%z "$JDK_FILE" 2>/dev/null || stat -c%s "$JDK_FILE" 2>/dev/null)
    echo "✅ JDK binary file found"
    echo "   Location: $JDK_FILE"
    echo "   Size: $(numfmt --to=iec-i --suffix=B $FILE_SIZE 2>/dev/null || echo "$FILE_SIZE bytes")"
    echo ""

    # Check file permissions
    if [ -r "$JDK_FILE" ]; then
        echo "✅ File is readable"
    else
        echo "⚠️  File exists but is not readable"
        echo "   Run: chmod 644 $JDK_FILE"
    fi

    echo ""
    echo "════════════════════════════════════════════════════════════"
    echo "  ✅ Setup validation PASSED"
    echo "  You can now start the Dev Container"
    echo "════════════════════════════════════════════════════════════"
    exit 0
else
    echo "❌ JDK binary file NOT FOUND"
    echo ""
    echo "Required file: jdk-1_5_0_22-linux-amd64-rpm.bin"
    echo "Expected location: $JDK_FILE"
    echo ""
    echo "📋 Option 1: Automatic Download (Recommended)"
    echo ""
    echo "   Run the download script:"
    echo "   ${SCRIPT_DIR}/scripts/download-jdk.sh ${SCRIPT_DIR}"
    echo ""

    # Attempt automatic download if the script exists
    DOWNLOAD_SCRIPT="${SCRIPT_DIR}/scripts/download-jdk.sh"
    if [ -f "$DOWNLOAD_SCRIPT" ]; then
        echo "🔄 Attempting automatic download..."
        echo ""
        if bash "$DOWNLOAD_SCRIPT" "$SCRIPT_DIR"; then
            echo ""
            echo "════════════════════════════════════════════════════════════"
            echo "  ✅ Setup validation PASSED (auto-downloaded)"
            echo "  You can now start the Dev Container"
            echo "════════════════════════════════════════════════════════════"
            exit 0
        else
            echo ""
            echo "⚠️  Automatic download failed. Please try manual download."
            echo ""
        fi
    fi

    echo "📋 Option 2: Manual Download"
    echo ""
    echo "1. Download the JDK binary from Oracle:"
    echo "   https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html"
    echo ""
    echo "2. Place it in the .devcontainer directory:"
    echo "   cp /path/to/jdk-1_5_0_22-linux-amd64-rpm.bin $SCRIPT_DIR/"
    echo ""
    echo "3. Run this script again to validate:"
    echo "   $0"
    echo ""
    echo "📖 For detailed instructions, see:"
    echo "   ${SCRIPT_DIR}/SETUP_REQUIRED.md"
    echo ""
    echo "════════════════════════════════════════════════════════════"
    echo "  ❌ Setup validation FAILED"
    echo "  Cannot proceed without JDK binary file"
    echo "════════════════════════════════════════════════════════════"
    exit 1
fi
