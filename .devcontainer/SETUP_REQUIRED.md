# ⚠️ Dev Container Setup Required

## Issue: JDK Binary File Missing

The Dev Container build failed because the required JDK binary file is not present.

### 🔴 Error Details

```
ERROR: "/jdk-1_5_0_22-linux-amd64-rpm.bin": not found
```

The Dockerfile requires the JDK 1.5.0_22 binary file to be placed in the `.devcontainer/` directory before building the container.

---

## ✅ Solution: Download and Place JDK Binary

### Option 1: Automatic Download (Recommended)

Use the provided download script to automatically download from Archive.org with SHA256 verification:

```bash
# From your project root directory
./.devcontainer/scripts/download-jdk.sh .devcontainer
```

The script will:
- Download `jdk-1_5_0_22-linux-amd64-rpm.bin` (~42MB) from Archive.org
- Verify SHA256 checksum for integrity
- Skip download if the file already exists with the correct checksum

Alternatively, the Dockerfile will automatically download the JDK during the Docker build if the binary is not present locally.

### Option 2: Manual Download

#### Step 1: Download the JDK Binary

You need to download the **JDK 1.5.0_22 Linux AMD64 RPM binary** from Oracle:

**File Name:** `jdk-1_5_0_22-linux-amd64-rpm.bin`

**Download Options:**

1. **Oracle Java Archive** (Requires Oracle Account):
   - Visit: https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html
   - Find: Java SE Development Kit 5.0u22
   - Download: `jdk-1_5_0_22-linux-amd64-rpm.bin` (for Linux x64)

2. **Alternative Sources** (if you have the file from a previous download):
   - Check your downloads folder
   - Check any backup or archive locations
   - Contact your system administrator if this is an enterprise environment

### Step 2: Place the File in .devcontainer Directory

```bash
# From your project root directory
cd /home/shinyay/work/java/docker-java5-for-legacy-app/.devcontainer/

# Copy the downloaded JDK binary here
cp /path/to/your/downloads/jdk-1_5_0_22-linux-amd64-rpm.bin .

# Verify the file exists
ls -la jdk-1_5_0_22-linux-amd64-rpm.bin
```

**Expected Result:**
```
-rw-r--r-- 1 user user 51123456 Oct 11 15:30 jdk-1_5_0_22-linux-amd64-rpm.bin
```

### Step 3: Rebuild the Dev Container

After placing the JDK binary file:

**Option A: Using VS Code**
1. Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
2. Type: "Dev Containers: Rebuild Container"
3. Press Enter
4. Wait for the container to build successfully

**Option B: Using Command Line**
```bash
# From project root
docker compose -f .devcontainer/compose.dev.yaml build --no-cache
```

---

## 📋 Quick Checklist

- [ ] Downloaded `jdk-1_5_0_22-linux-amd64-rpm.bin` from Oracle
- [ ] Placed the file in `.devcontainer/` directory
- [ ] Verified the file exists: `ls -la .devcontainer/jdk*.bin`
- [ ] Rebuilt the Dev Container

---

## 🔍 Verification

After successful build, verify the environment:

```bash
# Check Java version (should show 1.5.0_22)
java -version

# Expected output:
java version "1.5.0_22"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_22-b03)
Java HotSpot(TM) 64-Bit Server VM (build 1.5.0_22-b03, mixed mode)
```

---

## ❓ Troubleshooting

### Issue: "Cannot download - Oracle login required"

**Solution:**
1. Create a free Oracle account at https://profile.oracle.com/
2. Log in to Oracle's website
3. Accept the license agreement
4. Download the file

### Issue: "Wrong file downloaded"

**Verify you have the correct file:**
- File name must be exactly: `jdk-1_5_0_22-linux-amd64-rpm.bin`
- File size should be approximately 51 MB
- Platform: Linux x64 (not i586, not Windows, not Solaris)
- Format: Binary installer (not tar.gz, not ZIP)

### Issue: "File exists but build still fails"

**Check file permissions and location:**
```bash
# Verify exact file name and location
ls -la .devcontainer/jdk-1_5_0_22-linux-amd64-rpm.bin

# Ensure file is readable
chmod 644 .devcontainer/jdk-1_5_0_22-linux-amd64-rpm.bin

# Rebuild with clean cache
docker system prune -a
docker compose -f .devcontainer/compose.dev.yaml build --no-cache
```

### Issue: "Build is very slow"

This is normal for the first build:
- The Dockerfile performs automated JDK installation
- Multiple installation methods are attempted for reliability
- Pack200 files are extracted automatically
- Expected build time: 2-5 minutes

---

## 📝 Why This File is Required

Java 1.5.0_22 (J2SE 5.0) is a legacy version from 2009 and is not available via package managers or public Docker images. The binary must be manually obtained from Oracle's Java Archive due to:

1. **License Requirements**: Oracle requires license acceptance
2. **Legacy Status**: No longer distributed through modern channels
3. **Security**: Ensures you have the authentic Oracle JDK

---

## 🚀 Next Steps

Once the Dev Container builds successfully:

1. **Verify Environment**: Run `java -version` and `ant -version`
2. **Build Application**: Run `ant clean build`
3. **Start Development**: Edit code and test your Struts application
4. **Access Services**:
   - Application: http://localhost:8081/legacy-app/
   - phpMyAdmin: http://localhost:8082/
   - MySQL: localhost:3306

---

## 📚 Additional Resources

- [Oracle Java Archive Downloads](https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html)
- [Dev Container Documentation](./.devcontainer/README.md)
- [Project README](../README.md)

---

**Last Updated:** October 11, 2025
**Status:** Waiting for JDK binary file placement
