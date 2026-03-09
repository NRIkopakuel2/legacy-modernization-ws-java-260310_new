#!/bin/bash

# JDK 1.5.0_22 Automatic Download Script
# Downloads the JDK binary from Archive.org with SHA256 verification
#
# Usage: ./download-jdk.sh [target_dir]
#   target_dir: Directory to save the JDK binary (default: .devcontainer/)

set -e

# Configuration
JDK_FILENAME="jdk-1_5_0_22-linux-amd64-rpm.bin"
JDK_URL="https://archive.org/download/Java_5_update_22/${JDK_FILENAME}"
JDK_SHA256="fe2005d11c60b093b86461bc82f2b3590b7298c077195657b1973afae340127b"
JDK_SIZE_HUMAN="42MB"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Determine target directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${1:-$(dirname "$SCRIPT_DIR")}"
JDK_FILE="${TARGET_DIR}/${JDK_FILENAME}"

echo "════════════════════════════════════════════════════════════"
echo "  JDK 1.5.0_22 Automatic Download"
echo "════════════════════════════════════════════════════════════"
echo ""

# Check if file already exists with correct checksum
if [ -f "$JDK_FILE" ]; then
    echo "Found existing file: ${JDK_FILE}"
    echo -n "Verifying checksum... "
    ACTUAL_SHA256=$(sha256sum "$JDK_FILE" | awk '{print $1}')
    if [ "$ACTUAL_SHA256" = "$JDK_SHA256" ]; then
        echo -e "${GREEN}OK${NC}"
        echo ""
        echo -e "${GREEN}✅ JDK binary already exists with correct checksum. Skipping download.${NC}"
        exit 0
    else
        echo -e "${YELLOW}MISMATCH${NC}"
        echo "  Expected: ${JDK_SHA256}"
        echo "  Actual:   ${ACTUAL_SHA256}"
        echo "Removing corrupted file and re-downloading..."
        rm -f "$JDK_FILE"
    fi
fi

# Check for required tools
if command -v wget &> /dev/null; then
    DOWNLOAD_CMD="wget"
elif command -v curl &> /dev/null; then
    DOWNLOAD_CMD="curl"
else
    echo -e "${RED}ERROR: Neither wget nor curl found. Please install one of them.${NC}"
    exit 1
fi

# Download
echo "Downloading JDK 1.5.0_22 (~${JDK_SIZE_HUMAN}) from Archive.org..."
echo "  URL: ${JDK_URL}"
echo "  Target: ${JDK_FILE}"
echo ""

mkdir -p "$TARGET_DIR"

if [ "$DOWNLOAD_CMD" = "wget" ]; then
    wget -q --show-progress -O "$JDK_FILE" "$JDK_URL"
elif [ "$DOWNLOAD_CMD" = "curl" ]; then
    curl -L --progress-bar -o "$JDK_FILE" "$JDK_URL"
fi

# Verify download
if [ ! -f "$JDK_FILE" ]; then
    echo -e "${RED}ERROR: Download failed. File not found at ${JDK_FILE}${NC}"
    exit 1
fi

echo ""
echo -n "Verifying SHA256 checksum... "
ACTUAL_SHA256=$(sha256sum "$JDK_FILE" | awk '{print $1}')
if [ "$ACTUAL_SHA256" = "$JDK_SHA256" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAILED${NC}"
    echo "  Expected: ${JDK_SHA256}"
    echo "  Actual:   ${ACTUAL_SHA256}"
    echo ""
    echo -e "${RED}ERROR: Checksum verification failed. The downloaded file may be corrupted.${NC}"
    rm -f "$JDK_FILE"
    exit 1
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo -e "  ${GREEN}✅ JDK 1.5.0_22 downloaded and verified successfully!${NC}"
echo "  Location: ${JDK_FILE}"
echo "  SHA256: ${JDK_SHA256}"
echo "════════════════════════════════════════════════════════════"
