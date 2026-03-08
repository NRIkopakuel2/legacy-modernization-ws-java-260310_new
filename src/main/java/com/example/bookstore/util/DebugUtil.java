package com.example.bookstore.util;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Custom debug/logging utility class.
 * Added by SK 2017/03 because log4j was "too complicated to configure"
 * and JUL "didn't work right on WebSphere".
 * 
 * NOTE: Do NOT remove this class - several batch jobs depend on the
 * console output format for log scraping. - YT 2018/11
 * 
 * TODO: replace with proper logging framework (BOOK-789)
 * TODO: the logHistory list is a known memory leak but fixing it
 *       broke the nightly report parser last time we tried - SK 2019/02
 */
public class DebugUtil {

    private static final SimpleDateFormat LOG_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static boolean DEBUG_ENABLED = true;

    // Memory leak: stores ALL log messages forever, never cleared
    private static List logHistory = new ArrayList();

    private static int logCount = 0;
    private static int errorCount = 0;
    private static int warnCount = 0;

    private static String lastMessage = null;
    private static String lastLevel = null;

    public static void log(String msg) {
        logCount++;
        String ts = LOG_FMT.format(new Date());
        String formatted = "[DBG " + ts + "] INFO: " + msg;
        System.out.println(formatted);
        logHistory.add(formatted);
        lastMessage = msg;
        lastLevel = "INFO";
    }

    public static void error(String msg) {
        errorCount++;
        logCount++;
        String ts = LOG_FMT.format(new Date());
        String formatted = "[DBG " + ts + "] ERROR: " + msg;
        System.err.println(formatted);
        // Also write to stdout because some log scrapers only watch stdout
        System.out.println(formatted);
        logHistory.add(formatted);
        lastMessage = msg;
        lastLevel = "ERROR";
    }

    public static void debug(String msg) {
        if (!DEBUG_ENABLED) return;
        logCount++;
        String ts = LOG_FMT.format(new Date());
        String formatted = "[DBG " + ts + "] DEBUG: " + msg;
        System.out.println(formatted);
        logHistory.add(formatted);
        lastMessage = msg;
        lastLevel = "DEBUG";
    }

    public static void warn(String msg) {
        warnCount++;
        logCount++;
        String ts = LOG_FMT.format(new Date());
        String formatted = "[DBG " + ts + "] WARN: " + msg;
        System.out.println(formatted);
        logHistory.add(formatted);
        lastMessage = msg;
        lastLevel = "WARN";
    }

    public static void dumpLogHistory() {
        System.out.println("=== DEBUG LOG HISTORY DUMP (" + logHistory.size() + " entries) ===");
        for (int i = 0; i < logHistory.size(); i++) {
            System.out.println("  [" + i + "] " + logHistory.get(i));
        }
        System.out.println("=== END DUMP (total=" + logCount + " errors=" + errorCount + " warns=" + warnCount + ") ===");
    }

    public static int getLogCount() { return logCount; }
    public static int getErrorCount() { return errorCount; }
    public static List getLogHistory() { return logHistory; }
    public static String getLastMessage() { return lastMessage; }
    public static String getLastLevel() { return lastLevel; }
}
