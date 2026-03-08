#!/bin/bash
# ==============================================================================
# Code Quality Anti-Pattern Assessment Script
# ==============================================================================
# Measures 15 dimensions of anti-patterns in Java/JSP codebases and produces
# a quality score out of 100. Higher score = better quality.
#
# Usage: ./assess-quality.sh [source-directory]
#   source-directory: Root of Java source tree (default: src/)
#
# Dependencies: grep, wc, find, awk, sort (standard Unix tools)
# ==============================================================================

set -uo pipefail
# Note: -e (exit on error) intentionally omitted — grep returns 1 when no matches found

# --- Configuration ---
SRC_DIR="${1:-src}"
if [ ! -d "$SRC_DIR" ]; then
    echo "ERROR: Source directory '$SRC_DIR' not found."
    echo "Usage: $0 [source-directory]"
    exit 1
fi

# --- Helper Functions ---
count_pattern() {
    local pattern="$1"
    local path="$2"
    local flags="${3:--r}"
    grep $flags "$pattern" "$path" 2>/dev/null | wc -l
}

count_files() {
    local ext="$1"
    local path="$2"
    find "$path" -name "$ext" 2>/dev/null | wc -l
}

total_loc() {
    local ext="$1"
    local path="$2"
    find "$path" -name "$ext" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}'
}

files_over_lines() {
    local ext="$1"
    local path="$2"
    local threshold="$3"
    find "$path" -name "$ext" -exec wc -l {} + 2>/dev/null | grep -v "total$" | awk -v t="$threshold" '$1 > t {count++} END {print count+0}'
}

max_file_loc() {
    local ext="$1"
    local path="$2"
    find "$path" -name "$ext" -exec wc -l {} + 2>/dev/null | grep -v "total$" | sort -rn | head -1 | awk '{print $1}'
}

clamp() {
    local val="$1"
    local min="$2"
    local max="$3"
    awk -v v="$val" -v lo="$min" -v hi="$max" 'BEGIN { if (v < lo) print lo; else if (v > hi) print hi; else print v }'
}

deduct() {
    local max_pts="$1"
    local raw_deduction="$2"
    local result
    result=$(awk -v m="$max_pts" -v d="$raw_deduction" 'BEGIN { r = m - d; if (r < 0) r = 0; printf "%.1f", r }')
    echo "$result"
}

# ==============================================================================
# PHASE 1: Gather Raw Metrics
# ==============================================================================
echo "================================================================="
echo "  CODE QUALITY ANTI-PATTERN ASSESSMENT"
echo "  Source: $SRC_DIR"
echo "  Date:   $(date '+%Y-%m-%d %H:%M:%S')"
echo "================================================================="
echo ""

# --- Basic Stats ---
JAVA_FILES=$(count_files "*.java" "$SRC_DIR")
JAVA_LOC=$(total_loc "*.java" "$SRC_DIR")
JAVA_LOC=${JAVA_LOC:-0}
JSP_FILES=$(count_files "*.jsp" "$SRC_DIR")
JSP_LOC=$(total_loc "*.jsp" "$SRC_DIR")
JSP_LOC=${JSP_LOC:-0}
XML_FILES=$(count_files "*.xml" "$SRC_DIR")
PROPS_FILES=$(count_files "*.properties" "$SRC_DIR")
SQL_FILES=$(count_files "*.sql" "$SRC_DIR")
TOTAL_LOC=$((JAVA_LOC + JSP_LOC))
TOTAL_LOC=${TOTAL_LOC:-1}  # Avoid division by zero

# Per-1K-LOC normalization factor
KLOC=$(awk -v t="$TOTAL_LOC" 'BEGIN { k = t / 1000.0; if (k < 1) k = 1; print k }')

echo "--- Codebase Overview ---"
echo "  Java files:       $JAVA_FILES ($JAVA_LOC LOC)"
echo "  JSP files:        $JSP_FILES ($JSP_LOC LOC)"
echo "  XML configs:      $XML_FILES"
echo "  Properties files: $PROPS_FILES"
echo "  SQL files:        $SQL_FILES"
echo "  Total LOC:        $TOTAL_LOC (Java + JSP)"
echo ""

# ==============================================================================
# DIMENSION 1: Security Vulnerabilities (15 pts)
# ==============================================================================
SEC_SQL_INJECTION=$(grep -rn '"SELECT\|"INSERT\|"UPDATE\|"DELETE' "$SRC_DIR" 2>/dev/null | grep -c ' + \|" +\|+ "' || echo 0)
SEC_HARDCODED_CREDS=$(grep -r "legacy_pass\|legacy_user\|password.*=.*\"[a-zA-Z]" "$SRC_DIR" 2>/dev/null | grep -v "\.class$" | wc -l)
SEC_JDBC_IN_VIEWS=$(grep -rl "DriverManager\|getConnection" "$SRC_DIR" 2>/dev/null | grep -c "\.jsp$" || echo 0)
SEC_JDBC_IN_MODELS=$(grep -rl "DriverManager\|java\.sql\.\*" "$SRC_DIR" 2>/dev/null | grep -c "/model/\|/form/\|/action/" || echo 0)

SEC_RAW=$((SEC_SQL_INJECTION + SEC_HARDCODED_CREDS + SEC_JDBC_IN_VIEWS * 5 + SEC_JDBC_IN_MODELS * 3))
SEC_NORM=$(awk -v r="$SEC_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
SEC_DEDUCT=$(awk -v n="$SEC_NORM" 'BEGIN { d = n * 0.3; if (d > 15) d = 15; printf "%.1f", d }')
D1_SCORE=$(deduct 15 "$SEC_DEDUCT")

# ==============================================================================
# DIMENSION 2: Error Handling (12 pts)
# ==============================================================================
EH_GENERIC_CATCH=$(count_pattern "catch (Exception\|catch(Exception" "$SRC_DIR")
EH_CATCH_THROWABLE=$(count_pattern "catch (Throwable\|catch(Throwable" "$SRC_DIR")
EH_CATCH_NPE=$(count_pattern "catch (NullPointerException\|catch(NullPointerException" "$SRC_DIR")
EH_EMPTY_CATCH=$(grep -rP "catch\s*\([^)]+\)\s*\{\s*\}" "$SRC_DIR" 2>/dev/null | wc -l)
EH_PRINTSTACKTRACE=$(count_pattern "printStackTrace" "$SRC_DIR")

EH_RAW=$((EH_GENERIC_CATCH + EH_CATCH_THROWABLE * 3 + EH_CATCH_NPE * 3 + EH_EMPTY_CATCH * 2 + EH_PRINTSTACKTRACE))
EH_NORM=$(awk -v r="$EH_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
EH_DEDUCT=$(awk -v n="$EH_NORM" 'BEGIN { d = n * 0.2; if (d > 12) d = 12; printf "%.1f", d }')
D2_SCORE=$(deduct 12 "$EH_DEDUCT")

# ==============================================================================
# DIMENSION 3: God Classes (10 pts)
# ==============================================================================
GC_OVER_500=$(files_over_lines "*.java" "$SRC_DIR" 500)
GC_OVER_1000=$(files_over_lines "*.java" "$SRC_DIR" 1000)
GC_MAX_LOC=$(max_file_loc "*.java" "$SRC_DIR")
GC_MAX_LOC=${GC_MAX_LOC:-0}
GC_CONCENTRATION=$(awk -v m="$GC_MAX_LOC" -v t="$JAVA_LOC" 'BEGIN { if (t > 0) printf "%.1f", (m / t) * 100; else print 0 }')

GC_DEDUCT=$(awk -v o5="$GC_OVER_500" -v o1="$GC_OVER_1000" -v c="$GC_CONCENTRATION" \
    'BEGIN { d = o5 * 0.5 + o1 * 1.5 + c * 0.2; if (d > 10) d = 10; printf "%.1f", d }')
D3_SCORE=$(deduct 10 "$GC_DEDUCT")

# ==============================================================================
# DIMENSION 4: Memory & Resource Leaks (6 pts)
# ==============================================================================
ML_STATIC_COLLECTIONS=$(grep -rn "static.*List\|static.*Map\|static.*Set\|static.*ArrayList\|static.*HashMap\|static.*HashSet" "$SRC_DIR" 2>/dev/null | grep "\.java:" | grep -v "final.*Collections\|final.*Arrays\|final.*unmodifiable\|final.*empty\|final.*singleton" | wc -l)
ML_CONN_FILES=$(grep -rl "getConnection\|openSession" "$SRC_DIR" 2>/dev/null | wc -l)
ML_FINALLY_FILES=$(grep -rl "finally" "$SRC_DIR" 2>/dev/null | xargs grep -l "getConnection\|openSession" 2>/dev/null | wc -l)
ML_NO_FINALLY=$((ML_CONN_FILES - ML_FINALLY_FILES))
if [ "$ML_NO_FINALLY" -lt 0 ]; then ML_NO_FINALLY=0; fi

ML_RAW=$((ML_STATIC_COLLECTIONS + ML_NO_FINALLY * 2))
ML_NORM=$(awk -v r="$ML_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
ML_DEDUCT=$(awk -v n="$ML_NORM" 'BEGIN { d = n * 1.0; if (d > 6) d = 6; printf "%.1f", d }')
D4_SCORE=$(deduct 6 "$ML_DEDUCT")

# ==============================================================================
# DIMENSION 5: Copy-Paste / Duplication (8 pts)
# ==============================================================================
CP_JDBC_URLS=$(count_pattern "jdbc:mysql://\|jdbc:oracle://\|jdbc:postgresql://" "$SRC_DIR")
CP_GETCONNECTION=$(count_pattern "getConnection\|DriverManager" "$SRC_DIR")
CP_SYSOUT=$(count_pattern "System\.out\.println\|System\.out\.print(" "$SRC_DIR")
CP_SYSERR=$(count_pattern "System\.err" "$SRC_DIR")

CP_RAW=$((CP_JDBC_URLS * 2 + CP_GETCONNECTION + CP_SYSOUT + CP_SYSERR))
CP_NORM=$(awk -v r="$CP_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
CP_DEDUCT=$(awk -v n="$CP_NORM" 'BEGIN { d = n * 0.15; if (d > 8) d = 8; printf "%.1f", d }')
D5_SCORE=$(deduct 8 "$CP_DEDUCT")

# ==============================================================================
# DIMENSION 6: Global Mutable State (7 pts)
# ==============================================================================
GS_MUTABLE_STATIC=$(grep -rn "static " "$SRC_DIR" 2>/dev/null | grep "\.java:" | grep -v "static final\|static void\|static class\|static .*(.*).*{" | wc -l)
GS_THREAD_UNSAFE_FMT=$(grep -rn "static.*SimpleDateFormat\|static.*DateFormat" "$SRC_DIR" 2>/dev/null | grep -v "ThreadLocal" | wc -l)

GS_RAW=$((GS_MUTABLE_STATIC + GS_THREAD_UNSAFE_FMT * 5))
GS_NORM=$(awk -v r="$GS_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
GS_DEDUCT=$(awk -v n="$GS_NORM" 'BEGIN { d = n * 0.5; if (d > 7) d = 7; printf "%.1f", d }')
D6_SCORE=$(deduct 7 "$GS_DEDUCT")

# ==============================================================================
# DIMENSION 7: Naming & Readability (5 pts)
# ==============================================================================
NR_WILDCARD_IMPORTS=$(count_pattern "import.*\.\*;" "$SRC_DIR")
NR_SINGLE_CHAR_VARS=$(grep -rn "private.*\b[a-z];" "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)
NR_MIXED_GETTERS=$(grep -rn "public.*get[a-z][a-zA-Z]*(" "$SRC_DIR" 2>/dev/null | grep -c "get[a-z][a-z]" || echo 0)

NR_RAW=$((NR_WILDCARD_IMPORTS + NR_SINGLE_CHAR_VARS * 2 + NR_MIXED_GETTERS * 2))
NR_NORM=$(awk -v r="$NR_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
NR_DEDUCT=$(awk -v n="$NR_NORM" 'BEGIN { d = n * 0.3; if (d > 5) d = 5; printf "%.1f", d }')
D7_SCORE=$(deduct 5 "$NR_DEDUCT")

# ==============================================================================
# DIMENSION 8: Concurrency Safety (5 pts)
# ==============================================================================
CS_THREAD_SLEEP=$(count_pattern "Thread\.sleep" "$SRC_DIR")
CS_STRING_EQ=$(grep -rn '== "' "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)
CS_BROKEN_DCL=$(grep -rn "synchronized.*if.*null" "$SRC_DIR" 2>/dev/null | grep -v "private static volatile" | wc -l)
CS_BROKEN_DCL=${CS_BROKEN_DCL:-0}

CS_RAW=$((CS_THREAD_SLEEP * 3 + CS_STRING_EQ + CS_BROKEN_DCL * 5))
CS_NORM=$(awk -v r="$CS_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
CS_DEDUCT=$(awk -v n="$CS_NORM" 'BEGIN { d = n * 0.8; if (d > 5) d = 5; printf "%.1f", d }')
D8_SCORE=$(deduct 5 "$CS_DEDUCT")

# ==============================================================================
# DIMENSION 9: Magic Numbers & Strings (5 pts)
# ==============================================================================
MN_STATUS_STRINGS=$(grep -rn '"ACTIVE"\|"PENDING"\|"DRAFT"\|"COMPLETED"\|"CANCELLED"\|"DELETED"\|"INACTIVE"\|"ERROR"\|"SUCCESS"\|"FAILED"' "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)
MN_RETURN_CODES=$(grep -rn "return [2-9];\|return -[0-9];\|return [1-9][0-9]" "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)

MN_RAW=$((MN_STATUS_STRINGS + MN_RETURN_CODES * 2))
MN_NORM=$(awk -v r="$MN_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
MN_DEDUCT=$(awk -v n="$MN_NORM" 'BEGIN { d = n * 0.5; if (d > 5) d = 5; printf "%.1f", d }')
D9_SCORE=$(deduct 5 "$MN_DEDUCT")

# ==============================================================================
# DIMENSION 10: Logging Consistency (5 pts)
# ==============================================================================
LOG_SYSOUT_FILES=$(grep -rl "System\.out\.println" "$SRC_DIR" 2>/dev/null | grep "\.java$" | wc -l)
LOG_JUL_FILES=$(grep -rl "java\.util\.logging" "$SRC_DIR" 2>/dev/null | grep "\.java$" | wc -l)
LOG_COMMONS_FILES=$(grep -rl "org\.apache\.commons\.logging" "$SRC_DIR" 2>/dev/null | grep "\.java$" | wc -l)
LOG_LOG4J_FILES=$(grep -rl "org\.apache\.log4j" "$SRC_DIR" 2>/dev/null | grep "\.java$" | wc -l)
LOG_SLF4J_FILES=$(grep -rl "org\.slf4j" "$SRC_DIR" 2>/dev/null | grep "\.java$" | wc -l)

LOG_FRAMEWORKS=0
[ "$LOG_SYSOUT_FILES" -gt 0 ] && LOG_FRAMEWORKS=$((LOG_FRAMEWORKS + 1))
[ "$LOG_JUL_FILES" -gt 0 ] && LOG_FRAMEWORKS=$((LOG_FRAMEWORKS + 1))
[ "$LOG_COMMONS_FILES" -gt 0 ] && LOG_FRAMEWORKS=$((LOG_FRAMEWORKS + 1))
[ "$LOG_LOG4J_FILES" -gt 0 ] && LOG_FRAMEWORKS=$((LOG_FRAMEWORKS + 1))
[ "$LOG_SLF4J_FILES" -gt 0 ] && LOG_FRAMEWORKS=$((LOG_FRAMEWORKS + 1))

# 1 framework = good, 2+ = increasingly bad
LOG_DEDUCT=$(awk -v f="$LOG_FRAMEWORKS" -v s="$LOG_SYSOUT_FILES" -v k="$KLOC" \
    'BEGIN { d = 0; if (f > 1) d = (f - 1) * 1.5; d = d + (s / k) * 0.3; if (d > 5) d = 5; printf "%.1f", d }')
D10_SCORE=$(deduct 5 "$LOG_DEDUCT")

# ==============================================================================
# DIMENSION 11: Configuration Quality (5 pts)
# ==============================================================================
CFG_COMMENTED_XML=$(find "$SRC_DIR" -name "*.xml" -exec grep -c "<!--" {} + 2>/dev/null | awk -F: '{s+=$NF} END {print s+0}')
CFG_DUPLICATE_PARAMS=$(find "$SRC_DIR" -name "web.xml" -exec grep -c "context-param\|init-param\|filter-mapping" {} + 2>/dev/null | awk -F: '{s+=$NF} END {print s+0}')

CFG_RAW=$((CFG_COMMENTED_XML + CFG_DUPLICATE_PARAMS))
CFG_NORM=$(awk -v r="$CFG_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
CFG_DEDUCT=$(awk -v n="$CFG_NORM" 'BEGIN { d = n * 0.3; if (d > 5) d = 5; printf "%.1f", d }')
D11_SCORE=$(deduct 5 "$CFG_DEDUCT")

# ==============================================================================
# DIMENSION 12: Dead Code (5 pts)
# ==============================================================================
DC_TODO=$(count_pattern "// TODO\|// FIXME\|// HACK\|// XXX\|// BUG" "$SRC_DIR")
DC_COMMENTED_CODE=$(grep -rn "^[[:space:]]*//.*return\|^[[:space:]]*//.*if (\|^[[:space:]]*//.*for (" "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)
DC_FEATURE_FLAGS=$(count_pattern "LEGACY_MODE\|USE_NEW_\|ENABLE_.*=.*false\|FEATURE_.*=\|_V2\b" "$SRC_DIR")

DC_RAW=$((DC_TODO + DC_COMMENTED_CODE + DC_FEATURE_FLAGS * 2))
DC_NORM=$(awk -v r="$DC_RAW" -v k="$KLOC" 'BEGIN { printf "%.1f", r / k }')
DC_DEDUCT=$(awk -v n="$DC_NORM" 'BEGIN { d = n * 0.3; if (d > 5) d = 5; printf "%.1f", d }')
D12_SCORE=$(deduct 5 "$DC_DEDUCT")

# ==============================================================================
# DIMENSION 13: Method Complexity (5 pts)
# ==============================================================================
MC_LONG_METHODS=$(grep -rn "^\s*\(public\|private\|protected\).*(.*).*{" "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)
MC_DEEP_NESTING=$(grep -rP "^\s{24,}" "$SRC_DIR" 2>/dev/null | grep "\.java:" | wc -l)

# Approximate: ratio of methods to LOC (fewer methods in large files = longer methods)
MC_AVG_SIZE=$(awk -v l="$JAVA_LOC" -v m="$MC_LONG_METHODS" 'BEGIN { if (m > 0) printf "%.0f", l / m; else print 0 }')
MC_DEDUCT=$(awk -v a="$MC_AVG_SIZE" -v d="$MC_DEEP_NESTING" -v k="$KLOC" \
    'BEGIN { dd = 0; if (a > 30) dd = (a - 30) * 0.1; dd = dd + (d / k) * 0.1; if (dd > 5) dd = 5; printf "%.1f", dd }')
D13_SCORE=$(deduct 5 "$MC_DEDUCT")

# ==============================================================================
# DIMENSION 14: Dependency Structure (4 pts)
# ==============================================================================
# Check for circular dependencies (A imports B, B imports A)
DS_CIRCULAR=0
if [ "$JAVA_FILES" -gt 0 ]; then
    # Find manager/service files that import each other
    DS_CIRCULAR=$(find "$SRC_DIR" -name "*.java" -path "*/manager/*" -o -name "*.java" -path "*/service/*" 2>/dev/null | while read f; do
        imports=$(grep "^import " "$f" 2>/dev/null | sed 's/import //;s/;//' | grep -o '[^.]*$')
        basename_f=$(basename "$f" .java)
        for imp in $imports; do
            # Check if imported class also imports this class
            find "$SRC_DIR" -name "${imp}.java" -exec grep -l "import.*${basename_f}" {} \; 2>/dev/null
        done
    done | sort -u | wc -l)
fi
DS_TIGHT_COUPLING=$(grep -rl "new.*DAOImpl\|new.*Manager\|new.*Helper" "$SRC_DIR" 2>/dev/null | grep "\.java$" | grep -v "DAOImpl\|Manager\|Helper\|Factory" | wc -l)

DS_DEDUCT=$(awk -v c="$DS_CIRCULAR" -v t="$DS_TIGHT_COUPLING" -v k="$KLOC" \
    'BEGIN { d = c * 2 + (t / k) * 0.5; if (d > 4) d = 4; printf "%.1f", d }')
D14_SCORE=$(deduct 4 "$DS_DEDUCT")

# ==============================================================================
# DIMENSION 15: JSP / View Layer (3 pts)
# ==============================================================================
if [ "$JSP_FILES" -gt 0 ]; then
    JSP_SCRIPTLETS=$(grep -rn "<%[^@%!-]" "$SRC_DIR" 2>/dev/null | grep "\.jsp:" | wc -l)
    JSP_JDBC=$(grep -rl "Connection\|DriverManager\|ResultSet" "$SRC_DIR" 2>/dev/null | grep -c "\.jsp$" || echo 0)
    JSP_REFLECTION=$(grep -rn "getMethod\|invoke(" "$SRC_DIR" 2>/dev/null | grep -c "\.jsp:" || echo 0)

    JSP_DEDUCT=$(awk -v s="$JSP_SCRIPTLETS" -v j="$JSP_JDBC" -v r="$JSP_REFLECTION" -v k="$KLOC" \
        'BEGIN { d = (s / k) * 0.1 + j * 0.5 + r * 0.3; if (d > 3) d = 3; printf "%.1f", d }')
else
    JSP_SCRIPTLETS=0; JSP_JDBC=0; JSP_REFLECTION=0; JSP_DEDUCT="0.0"
fi
D15_SCORE=$(deduct 3 "$JSP_DEDUCT")

# ==============================================================================
# PHASE 2: Calculate Total Score
# ==============================================================================
TOTAL_SCORE=$(awk -v d1="$D1_SCORE" -v d2="$D2_SCORE" -v d3="$D3_SCORE" -v d4="$D4_SCORE" \
    -v d5="$D5_SCORE" -v d6="$D6_SCORE" -v d7="$D7_SCORE" -v d8="$D8_SCORE" \
    -v d9="$D9_SCORE" -v d10="$D10_SCORE" -v d11="$D11_SCORE" -v d12="$D12_SCORE" \
    -v d13="$D13_SCORE" -v d14="$D14_SCORE" -v d15="$D15_SCORE" \
    'BEGIN { printf "%.1f", d1+d2+d3+d4+d5+d6+d7+d8+d9+d10+d11+d12+d13+d14+d15 }')

TOTAL_INT=$(awk -v t="$TOTAL_SCORE" 'BEGIN { printf "%d", t + 0.5 }')

# --- Grade ---
if [ "$TOTAL_INT" -ge 90 ]; then GRADE="A  (Excellent)";
elif [ "$TOTAL_INT" -ge 80 ]; then GRADE="B  (Good)";
elif [ "$TOTAL_INT" -ge 70 ]; then GRADE="C  (Acceptable)";
elif [ "$TOTAL_INT" -ge 50 ]; then GRADE="D  (Poor)";
elif [ "$TOTAL_INT" -ge 30 ]; then GRADE="E  (Very Poor)";
else GRADE="F  (Unmaintainable)"; fi

# ==============================================================================
# PHASE 3: Output Report
# ==============================================================================
echo ""
echo "================================================================="
echo "  SCORE: $TOTAL_SCORE / 100   Grade: $GRADE"
echo "================================================================="
echo ""
echo "--- Dimension Breakdown ---"
echo ""
printf "  %-4s %-30s %6s / %-4s  %s\n" "#" "Dimension" "Score" "Max" "Key Metrics"
printf "  %-4s %-30s %6s   %-4s  %s\n" "---" "------------------------------" "-----" "---" "----------------------------"
printf "  %-4s %-30s %6s / %-4s  %s\n" "1"  "Security Vulnerabilities"      "$D1_SCORE"  "15" "SQLi=$SEC_SQL_INJECTION, Creds=$SEC_HARDCODED_CREDS, JDBC-in-view=$SEC_JDBC_IN_VIEWS"
printf "  %-4s %-30s %6s / %-4s  %s\n" "2"  "Error Handling"               "$D2_SCORE"  "12" "catch(Ex)=$EH_GENERIC_CATCH, empty=$EH_EMPTY_CATCH, stacktrace=$EH_PRINTSTACKTRACE"
printf "  %-4s %-30s %6s / %-4s  %s\n" "3"  "God Classes"                  "$D3_SCORE"  "10" ">500LOC=$GC_OVER_500, >1000LOC=$GC_OVER_1000, max=$GC_MAX_LOC, top%=${GC_CONCENTRATION}%"
printf "  %-4s %-30s %6s / %-4s  %s\n" "4"  "Memory & Resource Leaks"      "$D4_SCORE"  "6"  "static-colls=$ML_STATIC_COLLECTIONS, no-finally=$ML_NO_FINALLY"
printf "  %-4s %-30s %6s / %-4s  %s\n" "5"  "Copy-Paste / Duplication"     "$D5_SCORE"  "8"  "jdbc-urls=$CP_JDBC_URLS, getConn=$CP_GETCONNECTION, sysout=$CP_SYSOUT"
printf "  %-4s %-30s %6s / %-4s  %s\n" "6"  "Global Mutable State"         "$D6_SCORE"  "7"  "mutable-static=$GS_MUTABLE_STATIC, unsafe-dateformat=$GS_THREAD_UNSAFE_FMT"
printf "  %-4s %-30s %6s / %-4s  %s\n" "7"  "Naming & Readability"         "$D7_SCORE"  "5"  "wildcard-imports=$NR_WILDCARD_IMPORTS, mixed-getters=$NR_MIXED_GETTERS"
printf "  %-4s %-30s %6s / %-4s  %s\n" "8"  "Concurrency Safety"           "$D8_SCORE"  "5"  "Thread.sleep=$CS_THREAD_SLEEP, string==$CS_STRING_EQ"
printf "  %-4s %-30s %6s / %-4s  %s\n" "9"  "Magic Numbers & Strings"      "$D9_SCORE"  "5"  "status-strings=$MN_STATUS_STRINGS, return-codes=$MN_RETURN_CODES"
printf "  %-4s %-30s %6s / %-4s  %s\n" "10" "Logging Consistency"          "$D10_SCORE" "5"  "frameworks=$LOG_FRAMEWORKS, sysout-files=$LOG_SYSOUT_FILES"
printf "  %-4s %-30s %6s / %-4s  %s\n" "11" "Configuration Quality"        "$D11_SCORE" "5"  "xml-comments=$CFG_COMMENTED_XML, params=$CFG_DUPLICATE_PARAMS"
printf "  %-4s %-30s %6s / %-4s  %s\n" "12" "Dead Code"                    "$D12_SCORE" "5"  "TODO/FIXME=$DC_TODO, commented-code=$DC_COMMENTED_CODE, flags=$DC_FEATURE_FLAGS"
printf "  %-4s %-30s %6s / %-4s  %s\n" "13" "Method Complexity"            "$D13_SCORE" "5"  "avg-method-size=$MC_AVG_SIZE, deep-nesting=$MC_DEEP_NESTING"
printf "  %-4s %-30s %6s / %-4s  %s\n" "14" "Dependency Structure"         "$D14_SCORE" "4"  "circular=$DS_CIRCULAR, tight-coupling=$DS_TIGHT_COUPLING"
printf "  %-4s %-30s %6s / %-4s  %s\n" "15" "JSP / View Layer"             "$D15_SCORE" "3"  "scriptlets=$JSP_SCRIPTLETS, jdbc-in-jsp=$JSP_JDBC, reflection=$JSP_REFLECTION"
echo ""
printf "  %-4s %-30s %6s / %-4s\n" "" "TOTAL" "$TOTAL_SCORE" "100"
echo ""

# --- Score Interpretation ---
echo "--- Score Interpretation ---"
echo "  90-100  A  Excellent     - Clean, well-structured, production-ready"
echo "  80-89   B  Good          - Minor issues, maintainable with small effort"
echo "  70-79   C  Acceptable    - Notable tech debt, needs planned refactoring"
echo "  50-69   D  Poor          - Significant anti-patterns, hard to maintain"
echo "  30-49   E  Very Poor     - Critical issues, high risk of bugs/outages"
echo "   0-29   F  Unmaintainable - Rewrite recommended, active liability"
echo ""
echo "================================================================="
