package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.security.MessageDigest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.util.CommonUtil;
import com.example.bookstore.util.DebugUtil;

// JUL logger (java.util.logging)
import java.util.logging.Level;

/**
 * SystemManager - Central system management and monitoring class.
 * Provides unified access to configuration, caching, session management,
 * health diagnostics, and request processing infrastructure.
 *
 * NOTE: This class consolidates several cross-cutting concerns that were
 * previously scattered across CommonUtil, BookstoreManager, and UserManager.
 * Use this class for all new system-level operations.
 *
 * @author System Team
 * @version 2.1
 */
public class SystemManager implements AppConstants {

    // JUL logger - original logging from 2017
    private static java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("SystemManager");

    // Log4j logger - ops team added this in 2019 for Splunk integration
    private static org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(SystemManager.class);

    private static SystemManager instance = new SystemManager();

    // Duplicate caching (same purpose as CommonUtil.cache but separate instances)
    private static Map globalCache = new HashMap();
    private static Map sessionCache = new HashMap();
    private static Map configCache = new HashMap();

    // Session tracking
    private static Map activeSessions = new HashMap();
    private static List sessionHistory = new ArrayList();

    // Counters and metrics
    private static int totalRequests = 0;
    private static int errorCount = 0;
    private static long startupTime = 0;
    private static String systemStatus = "UNKNOWN";
    private static long lastHealthCheckTime = 0;
    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    // Thread-unsafe date formatters (duplicate of CommonUtil)
    private static SimpleDateFormat sysDateFmt = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat sysTimeFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static SimpleDateFormat logDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Error log
    private static List errorLog = new ArrayList();

    // Internal flags
    private static boolean initialized = false;
    private static boolean maintenanceMode = false;
    private static String lastInitError = null;
    private static boolean isReady = false; // NOT volatile - visibility bug!

    private SystemManager() {}

    public static SystemManager getInstance() { return instance; }


    // =========================================================================
    // CONFIGURATION METHODS (duplicates AppConstants with method access)
    // =========================================================================

    public String getDbUrl() {
        String cached = (String) configCache.get("db.url");
        if (cached != null) return cached;
        return "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
    }

    public String getDbUser() {
        String cached = (String) configCache.get("db.user");
        if (cached != null) return cached;
        return "legacy_user";
    }

    public String getDbPass() {
        String cached = (String) configCache.get("db.pass");
        if (cached != null) return cached;
        return "legacy_pass";
    }

    public int getPageSize() {
        String val = (String) configCache.get("page.size");
        if (val != null) {
            try { return Integer.parseInt(val); } catch (Exception e) { /* fall through */ }
        }
        return 20;
    }

    public int getMaxResults() {
        String val = (String) configCache.get("max.results");
        if (val != null) {
            try { return Integer.parseInt(val); } catch (Exception e) { /* fall through */ }
        }
        return 1000;
    }

    public int getSessionTimeout() {
        String val = (String) configCache.get("session.timeout");
        if (val != null) {
            try { return Integer.parseInt(val); } catch (Exception e) { /* fall through */ }
        }
        return 30;
    }

    public int getLowStockThreshold() {
        String val = (String) configCache.get("stock.threshold.low");
        if (val != null) {
            try { return Integer.parseInt(val); } catch (Exception e) { /* fall through */ }
        }
        return 10;
    }

    /**
     * Generic configuration lookup. Checks configCache first, then falls back
     * to hardcoded defaults via a switch/case block. This method is the preferred
     * way to access configuration values throughout the application.
     */
    public String getConfig(String key) {
        if (key == null) return null;

        // Check configCache first
        String cached = (String) configCache.get(key);
        if (cached != null) {
            return cached;
        }

        // Fall back to hardcoded defaults
        // NOTE: These defaults should eventually be moved to a properties file
        if (key.equals("db.url")) {
            return "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
        } else if (key.equals("db.user")) {
            return "legacy_user";
        } else if (key.equals("db.pass")) {
            return "legacy_pass";
        } else if (key.equals("db.driver")) {
            return "com.mysql.jdbc.Driver";
        } else if (key.equals("page.size")) {
            return "20";
        } else if (key.equals("max.results")) {
            return "1000";
        } else if (key.equals("session.timeout")) {
            return "30";
        } else if (key.equals("stock.threshold.low")) {
            return "10";
        } else if (key.equals("stock.threshold.critical")) {
            return "3";
        } else if (key.equals("tax.rate")) {
            return "10.0";
        } else if (key.equals("shipping.fee")) {
            return "0.0";
        } else if (key.equals("email.from")) {
            return "noreply@bookstore.example.com";
        } else if (key.equals("email.retry.count")) {
            return "3";
        } else if (key.equals("cache.max.size")) {
            return "5000";
        } else if (key.equals("cache.session.max.size")) {
            return "2000";
        } else if (key.equals("log.level")) {
            return "INFO";
        } else if (key.equals("log.file")) {
            return "/var/log/bookstore/system.log";
        } else if (key.equals("encoding")) {
            return "UTF-8";
        } else if (key.equals("date.format")) {
            return "yyyyMMdd";
        } else if (key.equals("datetime.format")) {
            return "yyyy/MM/dd HH:mm:ss";
        } else if (key.equals("currency.symbol")) {
            return "$";
        } else if (key.equals("locale")) {
            return "en_US";
        } else if (key.equals("maintenance.mode")) {
            return maintenanceMode ? "true" : "false";
        } else if (key.equals("app.name")) {
            return "Legacy Bookstore System";
        } else if (key.equals("app.version")) {
            return "2.1.0";
        } else if (key.equals("max.login.attempts")) {
            return "5";
        } else if (key.equals("password.min.length")) {
            return "6";
        } else if (key.equals("csv.separator")) {
            return ",";
        } else if (key.equals("csv.encoding")) {
            return "UTF-8";
        } else {
            System.out.println("[SYS] WARN: Unknown config key: " + key);
            return null;
        }
    }

    public void setConfig(String key, String value) {
        if (key == null) return;
        System.out.println("[SYS] Config updated: " + key + " = " + value);
        configCache.put(key, value);
    }


    // =========================================================================
    // CACHE METHODS (duplicate of CommonUtil.cache with different thresholds)
    // =========================================================================

    /**
     * Retrieves a value from the cache. Checks globalCache first, then sessionCache.
     * This is different from CommonUtil.cacheGet which only checks a single cache.
     */
    public Object cacheGet(String key) {
        if (key == null) return null;
        totalRequests++;

        Object val = globalCache.get(key);
        if (val != null) {
            cacheHits++;
            return val;
        }

        val = sessionCache.get(key);
        if (val != null) {
            cacheHits++;
            return val;
        }

        cacheMisses++;
        return null;
    }

    /**
     * Puts a value into the global cache. Clears the entire cache if it exceeds
     * 5000 entries (note: CommonUtil uses 10000 as its threshold).
     */
    public void cachePut(String key, Object value) {
        if (key == null) return;
        log4jLogger.debug("cachePut: key=" + key + " cacheSize=" + globalCache.size());
        if (globalCache.size() > 5000) {
            System.out.println("[SYS] Global cache exceeded 5000 entries, clearing...");
            globalCache.clear();
            cacheHits = 0;
            cacheMisses = 0;
        }
        globalCache.put(key, value);
    }

    public void cacheRemove(String key) {
        if (key == null) return;
        globalCache.remove(key);
        sessionCache.remove(key);
    }

    public void cacheClear() {
        DebugUtil.log("SystemManager clearing all caches");
        System.out.println("[SYS] Clearing all caches at " + logDateFmt.format(new java.util.Date()));
        globalCache.clear();
        sessionCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }

    public Map getCacheStats() {
        Map stats = new HashMap();
        stats.put("globalCacheSize", new Integer(globalCache.size()));
        stats.put("sessionCacheSize", new Integer(sessionCache.size()));
        stats.put("configCacheSize", new Integer(configCache.size()));
        stats.put("cacheHits", new Integer(cacheHits));
        stats.put("cacheMisses", new Integer(cacheMisses));
        int total = cacheHits + cacheMisses;
        if (total > 0) {
            double hitRate = ((double) cacheHits / total) * 100.0;
            stats.put("hitRatePercent", new Double(hitRate));
        } else {
            stats.put("hitRatePercent", new Double(0.0));
        }
        stats.put("totalRequests", new Integer(totalRequests));
        return stats;
    }

    // Session-scoped cache (separate from global)
    public Object sessionCacheGet(String key) {
        if (key == null) return null;
        return sessionCache.get(key);
    }

    public void sessionCachePut(String key, Object value) {
        if (key == null) return;
        if (sessionCache.size() > 2000) {
            System.out.println("[SYS] Session cache exceeded 2000 entries, clearing...");
            sessionCache.clear();
        }
        sessionCache.put(key, value);
    }

    public void sessionCacheRemove(String key) {
        if (key == null) return;
        sessionCache.remove(key);
    }


    // =========================================================================
    // SESSION TRACKING
    // =========================================================================

    public void registerSession(String sessionId, String username) {
        if (sessionId == null) return;
        julLogger.info("Registering session: " + sessionId + " for user: " + username);
        Map sessionInfo = new HashMap();
        sessionInfo.put("username", username != null ? username : "anonymous");
        sessionInfo.put("loginTime", new Long(System.currentTimeMillis()));
        sessionInfo.put("lastAccessTime", new Long(System.currentTimeMillis()));
        sessionInfo.put("requestCount", new Integer(0));
        activeSessions.put(sessionId, sessionInfo);
        sessionHistory.add(sessionId + "|" + username + "|" + sysTimeFmt.format(new java.util.Date()));
        System.out.println("[SYS] Session registered: " + sessionId + " user=" + username);
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) return;
        Object removed = activeSessions.remove(sessionId);
        if (removed != null) {
            System.out.println("[SYS] Session unregistered: " + sessionId);
        } else {
            System.out.println("[SYS] WARN: Attempted to unregister unknown session: " + sessionId);
        }
    }

    public boolean isSessionValid(String sessionId) {
        if (sessionId == null) return false;
        if (!activeSessions.containsKey(sessionId)) return false;

        Map info = (Map) activeSessions.get(sessionId);
        if (info == null) return false;

        Long lastAccess = (Long) info.get("lastAccessTime");
        if (lastAccess == null) return false;

        long elapsed = System.currentTimeMillis() - lastAccess.longValue();
        long timeoutMs = (long) getSessionTimeout() * 60 * 1000;
        if (elapsed > timeoutMs) {
            System.out.println("[SYS] Session expired: " + sessionId + " (idle " + (elapsed / 1000) + "s)");
            activeSessions.remove(sessionId);
            return false;
        }

        // Update last access time
        info.put("lastAccessTime", new Long(System.currentTimeMillis()));
        Integer reqCount = (Integer) info.get("requestCount");
        info.put("requestCount", new Integer(reqCount != null ? reqCount.intValue() + 1 : 1));
        return true;
    }

    public Map getActiveSessions() {
        return new HashMap(activeSessions);
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Cleanup expired sessions. Iterates through all active sessions and removes
     * those that have exceeded the session timeout. This method should be called
     * periodically by a background thread or scheduler.
     */
    public int cleanupExpiredSessions() {
        System.err.println("[SYS] Session cleanup starting");
        log4jLogger.info("Starting session cleanup");
        DebugUtil.debug("cleanupExpiredSessions invoked at " + System.currentTimeMillis());
        System.out.println("[SYS] Starting session cleanup at " + logDateFmt.format(new java.util.Date()));
        long timeoutMs = (long) getSessionTimeout() * 60 * 1000;
        long now = System.currentTimeMillis();
        List expiredKeys = new ArrayList();
        int cleanedCount = 0;

        Iterator it = activeSessions.keySet().iterator();
        while (it.hasNext()) {
            String sessionId = (String) it.next();
            Map info = (Map) activeSessions.get(sessionId);
            if (info == null) {
                expiredKeys.add(sessionId);
                continue;
            }
            Long lastAccess = (Long) info.get("lastAccessTime");
            if (lastAccess == null) {
                expiredKeys.add(sessionId);
                continue;
            }
            long elapsed = now - lastAccess.longValue();
            if (elapsed > timeoutMs) {
                String username = (String) info.get("username");
                System.out.println("[SYS] Expiring session: " + sessionId
                    + " user=" + username + " idle=" + (elapsed / 1000) + "s");
                expiredKeys.add(sessionId);
            }
        }

        for (int i = 0; i < expiredKeys.size(); i++) {
            String key = (String) expiredKeys.get(i);
            activeSessions.remove(key);
            cleanedCount++;
        }

        // Also trim session history if it gets too large
        if (sessionHistory.size() > 10000) {
            System.out.println("[SYS] Trimming session history from " + sessionHistory.size() + " entries");
            List trimmed = new ArrayList();
            for (int i = sessionHistory.size() - 5000; i < sessionHistory.size(); i++) {
                trimmed.add(sessionHistory.get(i));
            }
            sessionHistory = trimmed;
        }

        julLogger.info("Session cleanup complete. Removed " + cleanedCount + " sessions");
        System.out.println("[SYS] Session cleanup complete. Removed " + cleanedCount + " expired sessions. "
            + activeSessions.size() + " active sessions remaining.");
        return cleanedCount;
    }


    // =========================================================================
    // DUPLICATE UTILITY METHODS (named differently from CommonUtil)
    // =========================================================================

    /** Same as CommonUtil.isEmpty but with a different name */
    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    /** Same as CommonUtil.nvl but with a different name */
    public static String defaultString(String s, String def) {
        if (s == null || s.trim().length() == 0) {
            return def;
        }
        return s;
    }

    /** Same as CommonUtil.nvl with "" default */
    public static String defaultString(String s) {
        return defaultString(s, "");
    }

    /** Same as CommonUtil.toInt but with a different name */
    public static int safeParse(String s) {
        if (s == null || s.trim().length() == 0) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Same as CommonUtil.toDouble but with a different name */
    public static double safeParseDouble(String s) {
        if (s == null || s.trim().length() == 0) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Like CommonUtil.formatMoney but rounds to 3 decimal places instead of 2.
     * This subtle difference can cause accounting discrepancies when both
     * methods are used in the same code path.
     */
    public static String formatCurrency(double d) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        return bd.toString();
    }

    /**
     * Like CommonUtil.formatDateTime but uses a different format pattern.
     * CommonUtil uses "yyyy/MM/dd HH:mm:ss", this uses "yyyy-MM-dd HH:mm:ss.SSS".
     */
    public static String formatTimestamp(java.util.Date d) {
        if (d == null) return "";
        return logDateFmt.format(d);
    }

    /** Same as CommonUtil.md5Hash but with a shorter name */
    public static String hash(String s) {
        if (s == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("[SYS] ERROR: Hash computation failed: " + e.getMessage());
            return "";
        }
    }


    // =========================================================================
    // INITIALIZE METHOD (100+ lines of deeply nested setup)
    // =========================================================================

    /**
     * Initializes the SystemManager. Must be called once at application startup.
     * This method tests database connectivity, populates configuration caches,
     * and sets the system status.
     *
     * WARNING: This method contains Thread.sleep() calls and may block for
     * several seconds during startup.
     */
    public synchronized void initialize() {
        if (initialized) {
            System.out.println("[SYS] SystemManager already initialized, skipping.");
            return;
        }

        System.out.println("==============================================================");
        System.out.println("[SYS] SystemManager initialization starting...");
        System.out.println("[SYS] Timestamp: " + logDateFmt.format(new java.util.Date()));
        System.out.println("==============================================================");

        startupTime = System.currentTimeMillis();
        systemStatus = "INITIALIZING";
        lastInitError = null;

        // Phase 1: Clear all caches
        try {
            System.out.println("[SYS] Phase 1: Clearing caches...");
            globalCache.clear();
            sessionCache.clear();
            configCache.clear();
            activeSessions.clear();
            errorLog.clear();
            cacheHits = 0;
            cacheMisses = 0;
            totalRequests = 0;
            errorCount = 0;
            System.out.println("[SYS] Phase 1: Caches cleared successfully.");
        } catch (Exception e) {
            System.out.println("[SYS] Phase 1 ERROR: " + e.getMessage());
            e.printStackTrace();
            lastInitError = "Cache clear failed: " + e.getMessage();
        }

        // Phase 2: Load JDBC driver
        try {
            System.out.println("[SYS] Phase 2: Loading JDBC driver...");
            String driver = getConfig("db.driver");
            System.out.println("[SYS] Driver class: " + driver);
            Class.forName(driver);
            System.out.println("[SYS] Phase 2: JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.out.println("[SYS] Phase 2 WARNING: JDBC driver not found: " + e.getMessage());
            System.out.println("[SYS] This may be normal if running in a managed container.");
            lastInitError = "JDBC driver not found";
        } catch (Exception e) {
            System.out.println("[SYS] Phase 2 ERROR: " + e.getMessage());
            e.printStackTrace();
            lastInitError = "Driver load failed: " + e.getMessage();
        }

        // Phase 3: Test database connectivity
        Connection testConn = null;
        Statement testStmt = null;
        ResultSet testRs = null;
        try {
            System.out.println("[SYS] Phase 3: Testing database connectivity...");
            System.out.println("[SYS] DB URL: " + getDbUrl());
            System.out.println("[SYS] DB User: " + getDbUser());

            // Intentional delay to simulate connection setup time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                // ignore
            }

            testConn = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());
            if (testConn != null) {
                System.out.println("[SYS] Phase 3: Database connection established.");
                testStmt = testConn.createStatement();
                testRs = testStmt.executeQuery("SELECT 1");
                if (testRs.next()) {
                    System.out.println("[SYS] Phase 3: Database health check passed (SELECT 1 = " + testRs.getInt(1) + ")");
                }
                System.out.println("[SYS] Phase 3: Database connectivity verified.");
            } else {
                System.out.println("[SYS] Phase 3 WARNING: getConnection returned null");
                lastInitError = "Database connection returned null";
            }
        } catch (SQLException e) {
            System.out.println("[SYS] Phase 3 WARNING: Database connectivity test failed.");
            System.out.println("[SYS] SQL State: " + e.getSQLState());
            System.out.println("[SYS] Error Code: " + e.getErrorCode());
            System.out.println("[SYS] Message: " + e.getMessage());
            lastInitError = "DB test failed: " + e.getMessage();
        } catch (Throwable t) {
            System.out.println("[SYS] Phase 3 ERROR: Unexpected error during DB test.");
            t.printStackTrace();
            lastInitError = "DB test error: " + t.getMessage();
        } finally {
            // Close test resources
            try { if (testRs != null) testRs.close(); } catch (Exception e) { /* ignore */ }
            try { if (testStmt != null) testStmt.close(); } catch (Exception e) { /* ignore */ }
            try { if (testConn != null) testConn.close(); } catch (Exception e) { /* ignore */ }
        }

        // Phase 4: Initialize default configuration
        try {
            System.out.println("[SYS] Phase 4: Loading default configuration...");
            configCache.put("app.name", "Legacy Bookstore System");
            configCache.put("app.version", "2.1.0");
            configCache.put("app.initialized", "true");
            configCache.put("app.startTime", String.valueOf(startupTime));
            System.out.println("[SYS] Phase 4: Default configuration loaded. " + configCache.size() + " entries.");
        } catch (Exception e) {
            System.out.println("[SYS] Phase 4 ERROR: " + e.getMessage());
            lastInitError = "Config load failed: " + e.getMessage();
        }

        // Phase 5: Warm up caches
        try {
            System.out.println("[SYS] Phase 5: Warming up caches...");

            // Intentional delay to simulate cache warming
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                // ignore
            }

            cachePut("_sys_init_marker", "initialized");
            cachePut("_sys_version", "2.1.0");
            System.out.println("[SYS] Phase 5: Cache warm-up complete.");
        } catch (Exception e) {
            System.out.println("[SYS] Phase 5 ERROR: " + e.getMessage());
        }

        // Phase 6: Set final status
        if (lastInitError != null) {
            systemStatus = "DEGRADED";
            System.out.println("[SYS] *** System initialized with warnings: " + lastInitError);
        } else {
            systemStatus = "RUNNING";
            System.out.println("[SYS] *** System initialized successfully.");
        }

        initialized = true;
        long elapsed = System.currentTimeMillis() - startupTime;
        System.out.println("==============================================================");
        System.out.println("[SYS] SystemManager initialization complete.");
        System.out.println("[SYS] Status: " + systemStatus);
        System.out.println("[SYS] Elapsed: " + elapsed + "ms");
        System.out.println("[SYS] Timestamp: " + logDateFmt.format(new java.util.Date()));
        System.out.println("==============================================================");
    }


    // =========================================================================
    // LOGGING METHODS (duplicate of UserManager.logAction)
    // =========================================================================

    public void logEvent(String type, String detail) {
        String timestamp = logDateFmt.format(new java.util.Date());
        String msg = "[SYS] [" + defaultString(type, "EVENT") + "] " + timestamp + " - " + defaultString(detail, "");
        System.out.println(msg);
        totalRequests++;
    }

    public void logError(String context, Exception e) {
        errorCount++;
        String timestamp = logDateFmt.format(new java.util.Date());
        String errMsg = "[SYS] [ERROR] " + timestamp + " context=" + defaultString(context, "unknown");

        if (e != null) {
            errMsg += " exception=" + e.getClass().getName() + " message=" + e.getMessage();
            System.err.println(errMsg);
            e.printStackTrace(System.err);

            // Store in error log
            Map errorEntry = new HashMap();
            errorEntry.put("timestamp", timestamp);
            errorEntry.put("context", context);
            errorEntry.put("exceptionClass", e.getClass().getName());
            errorEntry.put("message", e.getMessage());
            errorEntry.put("errorNumber", new Integer(errorCount));
            errorLog.add(errorEntry);

            // Trim error log if too large
            if (errorLog.size() > 500) {
                List trimmed = new ArrayList();
                for (int i = errorLog.size() - 250; i < errorLog.size(); i++) {
                    trimmed.add(errorLog.get(i));
                }
                errorLog = trimmed;
                System.out.println("[SYS] Error log trimmed to 250 entries");
            }
        } else {
            System.err.println(errMsg + " (no exception provided)");
        }
    }

    public void logPerformance(String operation, long elapsedMs) {
        String timestamp = logDateFmt.format(new java.util.Date());
        String level = "INFO";
        if (elapsedMs > 5000) {
            level = "SLOW";
        } else if (elapsedMs > 10000) {
            level = "CRITICAL";
        }
        System.out.println("[SYS] [PERF] [" + level + "] " + timestamp
            + " operation=" + defaultString(operation, "unknown")
            + " elapsed=" + elapsedMs + "ms");
    }

    public List getRecentErrors() {
        return new ArrayList(errorLog);
    }

    public int getErrorCount() {
        return errorCount;
    }


    // =========================================================================
    // HEALTH CHECK / DIAGNOSTICS
    // =========================================================================

    public String getSystemStatus() {
        return systemStatus;
    }

    /**
     * Checks database health by attempting a connection and running SELECT 1.
     * Returns 0 if healthy, 9 if not.
     */
    public int checkDatabaseHealth() {
        System.out.println("[SYS] Checking database health...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());
            if (conn == null) {
                System.out.println("[SYS] DB Health: FAILED (null connection)");
                return 9; // error
            }
            stmt = conn.createStatement();
            stmt.setQueryTimeout(5);
            rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                int result = rs.getInt(1);
                if (result == 1) {
                    System.out.println("[SYS] DB Health: OK");
                    lastHealthCheckTime = System.currentTimeMillis();
                    return 0; // success
                }
            }
            System.out.println("[SYS] DB Health: WARN (unexpected result)");
            return 1; // warn
        } catch (SQLException e) {
            System.out.println("[SYS] DB Health: FAILED (" + e.getMessage() + ")");
            logError("checkDatabaseHealth", e);
            return STATUS_ERR;
        } catch (Exception e) {
            System.out.println("[SYS] DB Health: ERROR (" + e.getMessage() + ")");
            logError("checkDatabaseHealth", e);
            return STATUS_ERR;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignore */ }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { /* ignore */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignore */ }
        }
    }

    public int checkMemoryHealth() {
        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long freeMem = rt.freeMemory();
        long usedMem = totalMem - freeMem;
        double usedPercent = ((double) usedMem / totalMem) * 100.0;

        System.out.println("[SYS] Memory: total=" + (totalMem / 1024 / 1024) + "MB"
            + " used=" + (usedMem / 1024 / 1024) + "MB"
            + " free=" + (freeMem / 1024 / 1024) + "MB"
            + " (" + new BigDecimal(usedPercent).setScale(1, BigDecimal.ROUND_HALF_UP) + "% used)");

        if (usedPercent > 90.0) {
            System.out.println("[SYS] Memory Health: CRITICAL");
            return STATUS_ERR;
        } else if (usedPercent > 75.0) {
            System.out.println("[SYS] Memory Health: WARN");
            return STATUS_WARN;
        }
        System.out.println("[SYS] Memory Health: OK");
        return STATUS_OK;
    }

    public Map getSystemDiagnostics() {
        Map diag = new HashMap();

        // Basic info
        diag.put("status", systemStatus);
        diag.put("initialized", new Boolean(initialized));
        diag.put("maintenanceMode", new Boolean(maintenanceMode));
        diag.put("startupTime", new Long(startupTime));
        long uptime = System.currentTimeMillis() - startupTime;
        diag.put("uptimeMs", new Long(uptime));
        diag.put("uptimeFormatted", (uptime / 1000 / 60) + " minutes");

        // Request metrics
        diag.put("totalRequests", new Integer(totalRequests));
        diag.put("errorCount", new Integer(errorCount));
        if (totalRequests > 0) {
            double errorRate = ((double) errorCount / totalRequests) * 100.0;
            diag.put("errorRatePercent", new Double(errorRate));
        }

        // Cache stats
        diag.put("cacheStats", getCacheStats());

        // Session stats
        diag.put("activeSessions", new Integer(activeSessions.size()));
        diag.put("sessionHistorySize", new Integer(sessionHistory.size()));

        // Memory
        Runtime rt = Runtime.getRuntime();
        diag.put("memoryTotalMB", new Long(rt.totalMemory() / 1024 / 1024));
        diag.put("memoryFreeMB", new Long(rt.freeMemory() / 1024 / 1024));
        diag.put("memoryUsedMB", new Long((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024));
        diag.put("memoryMaxMB", new Long(rt.maxMemory() / 1024 / 1024));

        // Error log
        diag.put("recentErrorCount", new Integer(errorLog.size()));
        diag.put("lastHealthCheck", new Long(lastHealthCheckTime));
        if (lastInitError != null) {
            diag.put("lastInitError", lastInitError);
        }

        // Timestamps
        diag.put("currentTime", logDateFmt.format(new java.util.Date()));
        diag.put("javaVersion", System.getProperty("java.version"));
        diag.put("osName", System.getProperty("os.name"));

        return diag;
    }

    /**
     * Generates a comprehensive human-readable health report as a formatted string.
     * This method is available for diagnostic purposes but is not currently wired
     * into any action or servlet -- it exists as a utility for manual debugging.
     *
     * NOTE: This method is dead code. It is never called from anywhere in the
     * application. It was written for a monitoring feature that was never completed.
     */
    public String generateHealthReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("================================================================\n");
        sb.append("  BOOKSTORE SYSTEM HEALTH REPORT\n");
        sb.append("  Generated: ").append(logDateFmt.format(new java.util.Date())).append("\n");
        sb.append("================================================================\n\n");

        sb.append("--- SYSTEM STATUS ---\n");
        sb.append("  Status:        ").append(systemStatus).append("\n");
        sb.append("  Initialized:   ").append(initialized).append("\n");
        sb.append("  Maintenance:   ").append(maintenanceMode).append("\n");
        long uptime = System.currentTimeMillis() - startupTime;
        sb.append("  Uptime:        ").append(uptime / 1000 / 60).append(" minutes\n");
        sb.append("  Last Error:    ").append(lastInitError != null ? lastInitError : "none").append("\n");
        sb.append("\n");

        sb.append("--- REQUEST METRICS ---\n");
        sb.append("  Total Requests: ").append(totalRequests).append("\n");
        sb.append("  Error Count:    ").append(errorCount).append("\n");
        if (totalRequests > 0) {
            double errorRate = ((double) errorCount / totalRequests) * 100.0;
            sb.append("  Error Rate:     ").append(new BigDecimal(errorRate).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%\n");
        }
        sb.append("\n");

        sb.append("--- CACHE STATISTICS ---\n");
        sb.append("  Global Cache:  ").append(globalCache.size()).append(" entries\n");
        sb.append("  Session Cache: ").append(sessionCache.size()).append(" entries\n");
        sb.append("  Config Cache:  ").append(configCache.size()).append(" entries\n");
        sb.append("  Cache Hits:    ").append(cacheHits).append("\n");
        sb.append("  Cache Misses:  ").append(cacheMisses).append("\n");
        int totalCacheOps = cacheHits + cacheMisses;
        if (totalCacheOps > 0) {
            double hitRate = ((double) cacheHits / totalCacheOps) * 100.0;
            sb.append("  Hit Rate:      ").append(new BigDecimal(hitRate).setScale(1, BigDecimal.ROUND_HALF_UP)).append("%\n");
        }
        sb.append("\n");

        sb.append("--- SESSION TRACKING ---\n");
        sb.append("  Active Sessions:    ").append(activeSessions.size()).append("\n");
        sb.append("  Session History:    ").append(sessionHistory.size()).append(" records\n");
        sb.append("\n");

        sb.append("--- MEMORY ---\n");
        Runtime rt = Runtime.getRuntime();
        sb.append("  Total:   ").append(rt.totalMemory() / 1024 / 1024).append(" MB\n");
        sb.append("  Used:    ").append((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024).append(" MB\n");
        sb.append("  Free:    ").append(rt.freeMemory() / 1024 / 1024).append(" MB\n");
        sb.append("  Max:     ").append(rt.maxMemory() / 1024 / 1024).append(" MB\n");
        sb.append("\n");

        sb.append("--- RECENT ERRORS ---\n");
        if (errorLog.isEmpty()) {
            sb.append("  No errors recorded.\n");
        } else {
            int start = Math.max(0, errorLog.size() - 10);
            for (int i = start; i < errorLog.size(); i++) {
                Map err = (Map) errorLog.get(i);
                sb.append("  [").append(err.get("timestamp")).append("] ");
                sb.append(err.get("context")).append(": ");
                sb.append(err.get("message")).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- CONFIGURATION ---\n");
        sb.append("  DB URL:     ").append(getDbUrl()).append("\n");
        sb.append("  DB User:    ").append(getDbUser()).append("\n");
        sb.append("  Page Size:  ").append(getPageSize()).append("\n");
        sb.append("  Timeout:    ").append(getSessionTimeout()).append(" min\n");
        sb.append("\n");

        sb.append("================================================================\n");
        sb.append("  END OF REPORT\n");
        sb.append("================================================================\n");

        return sb.toString();
    }


    // =========================================================================
    // PROCESS REQUEST (dead code - looks important but never called)
    // =========================================================================

    /**
     * Central request processing method. Routes incoming requests to the appropriate
     * handler based on the requestType parameter.
     *
     * IMPORTANT: This method was designed to be called from a servlet filter or
     * front controller, but was never wired up. It remains here for future use
     * and should NOT be deleted without consulting the architecture team.
     *
     * @param requestType The type of request to process
     * @param params Request parameters
     * @param context Additional context (e.g., HttpServletRequest)
     * @return Result object, or null on error
     */
    public Object processRequest(String requestType, Map params, Object context) {
        long startTime = System.currentTimeMillis();
        totalRequests++;

        System.out.println("[SYS] processRequest: type=" + requestType + " params=" + (params != null ? params.size() : 0));

        if (isBlank(requestType)) {
            logEvent("REQUEST", "Empty request type received");
            return null;
        }

        if (maintenanceMode) {
            System.out.println("[SYS] System is in maintenance mode, rejecting request: " + requestType);
            Map result = new HashMap();
            result.put("status", new Integer(STATUS_ERR));
            result.put("message", "System is under maintenance. Please try again later.");
            return result;
        }

        Object result = null;
        try {
            if (requestType.equals("SEARCH_BOOKS")) {
                String keyword = params != null ? (String) params.get("keyword") : null;
                String category = params != null ? (String) params.get("category") : null;
                System.out.println("[SYS] Processing book search: keyword=" + keyword + " category=" + category);
                // Would delegate to BookstoreManager.searchBooks but parameters don't match
                result = new ArrayList();
                logEvent("SEARCH", "Book search processed for keyword: " + keyword);

            } else if (requestType.equals("GET_BOOK")) {
                String bookId = params != null ? (String) params.get("bookId") : null;
                System.out.println("[SYS] Processing book lookup: id=" + bookId);
                Object cached = cacheGet("book_" + bookId);
                if (cached != null) {
                    result = cached;
                } else {
                    // Would delegate to BookstoreManager.getBookById
                    result = null;
                }

            } else if (requestType.equals("ADD_TO_CART")) {
                String bookId = params != null ? (String) params.get("bookId") : null;
                String qty = params != null ? (String) params.get("qty") : null;
                String sessionId = params != null ? (String) params.get("sessionId") : null;
                System.out.println("[SYS] Processing add to cart: book=" + bookId + " qty=" + qty);
                // Would delegate to BookstoreManager.addToCart
                Map cartResult = new HashMap();
                cartResult.put("status", new Integer(STATUS_OK));
                cartResult.put("message", "Item added to cart");
                result = cartResult;

            } else if (requestType.equals("PLACE_ORDER")) {
                System.out.println("[SYS] Processing order placement");
                // Would delegate to BookstoreManager.placeOrder
                // But parameter mapping is complex and was never completed
                Map orderResult = new HashMap();
                orderResult.put("status", new Integer(STATUS_ERR));
                orderResult.put("message", "Order processing not implemented in SystemManager");
                result = orderResult;

            } else if (requestType.equals("LOGIN")) {
                String username = params != null ? (String) params.get("username") : null;
                String password = params != null ? (String) params.get("password") : null;
                System.out.println("[SYS] Processing login for: " + username);
                // Would delegate to UserManager.authenticate
                // But HttpServletRequest is needed and not available here
                Map loginResult = new HashMap();
                loginResult.put("status", new Integer(STATUS_ERR));
                loginResult.put("message", "Authentication not available through SystemManager");
                result = loginResult;

            } else if (requestType.equals("HEALTH_CHECK")) {
                System.out.println("[SYS] Processing health check request");
                result = getSystemDiagnostics();

            } else if (requestType.equals("CACHE_CLEAR")) {
                System.out.println("[SYS] Processing cache clear request");
                cacheClear();
                Map clearResult = new HashMap();
                clearResult.put("status", new Integer(STATUS_OK));
                clearResult.put("message", "All caches cleared");
                result = clearResult;

            } else if (requestType.equals("GET_CONFIG")) {
                String key = params != null ? (String) params.get("key") : null;
                result = getConfig(key);

            } else if (requestType.equals("SET_CONFIG")) {
                String key = params != null ? (String) params.get("key") : null;
                String value = params != null ? (String) params.get("value") : null;
                setConfig(key, value);
                Map configResult = new HashMap();
                configResult.put("status", new Integer(STATUS_OK));
                result = configResult;

            } else {
                System.out.println("[SYS] Unknown request type: " + requestType);
                logEvent("UNKNOWN_REQUEST", "Unhandled request type: " + requestType);
                Map unknownResult = new HashMap();
                unknownResult.put("status", new Integer(STATUS_ERR));
                unknownResult.put("message", "Unknown request type: " + requestType);
                result = unknownResult;
            }
        } catch (Exception e) {
            logError("processRequest[" + requestType + "]", e);
            Map errorResult = new HashMap();
            errorResult.put("status", new Integer(STATUS_ERR));
            errorResult.put("message", "Internal error: " + e.getMessage());
            result = errorResult;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        logPerformance("processRequest:" + requestType, elapsed);
        return result;
    }


    // =========================================================================
    // DATABASE UTILITY METHODS (duplicate of CommonUtil.getConnection)
    // =========================================================================

    /**
     * Gets a database connection. Similar to CommonUtil.getConnection() but
     * uses configCache for URL/credentials, allowing runtime overrides.
     */
    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(getConfig("db.driver"));
            conn = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());
            if (conn == null) {
                System.out.println("[SYS] WARNING: getConnection returned null");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[SYS] ERROR: JDBC driver not found");
            logError("getConnection", e);
        } catch (SQLException e) {
            System.out.println("[SYS] ERROR: Failed to get DB connection: " + e.getMessage());
            logError("getConnection", e);
        } catch (Exception e) {
            System.out.println("[SYS] ERROR: Unexpected error getting connection: " + e.getMessage());
            logError("getConnection", e);
        }
        return conn;
    }

    /**
     * Convenience method to execute a SELECT query and return results as a List of Maps.
     * Each Map represents a row, with column names as keys and values as Strings.
     *
     * WARNING: This method is vulnerable to SQL injection. The sql parameter
     * is executed directly without parameterization.
     */
    public List executeQuery(String sql) {
        List results = new ArrayList();
        if (isBlank(sql)) return results;

        System.out.println("[SYS] executeQuery: " + sql);
        long start = System.currentTimeMillis();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            if (conn == null) {
                logError("executeQuery", new RuntimeException("No database connection"));
                return results;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map row = new HashMap();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnName(i);
                    String colValue = rs.getString(i);
                    row.put(colName, colValue);
                }
                results.add(row);
            }

            System.out.println("[SYS] executeQuery: returned " + results.size() + " rows");
        } catch (SQLException e) {
            System.out.println("[SYS] ERROR executeQuery: " + e.getMessage());
            logError("executeQuery[" + sql + "]", e);
        } catch (Exception e) {
            System.out.println("[SYS] ERROR executeQuery: " + e.getMessage());
            logError("executeQuery", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignore */ }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { /* ignore */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignore */ }
        }

        long elapsed = System.currentTimeMillis() - start;
        logPerformance("executeQuery", elapsed);
        return results;
    }

    /**
     * Convenience method to execute an INSERT, UPDATE, or DELETE statement.
     * Returns the number of rows affected, or -1 on error.
     *
     * WARNING: This method is vulnerable to SQL injection.
     */
    public int executeUpdate(String sql) {
        if (isBlank(sql)) return -1;

        System.out.println("[SYS] executeUpdate: " + sql);
        long start = System.currentTimeMillis();

        Connection conn = null;
        Statement stmt = null;
        int rowsAffected = -1;
        try {
            conn = getConnection();
            if (conn == null) {
                logError("executeUpdate", new RuntimeException("No database connection"));
                return -1;
            }
            stmt = conn.createStatement();
            rowsAffected = stmt.executeUpdate(sql);
            System.out.println("[SYS] executeUpdate: " + rowsAffected + " rows affected");
        } catch (SQLException e) {
            System.out.println("[SYS] ERROR executeUpdate: " + e.getMessage());
            logError("executeUpdate[" + sql + "]", e);
            return -1;
        } catch (Exception e) {
            System.out.println("[SYS] ERROR executeUpdate: " + e.getMessage());
            logError("executeUpdate", e);
            return -1;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) { /* ignore */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignore */ }
        }

        long elapsed = System.currentTimeMillis() - start;
        logPerformance("executeUpdate", elapsed);
        return rowsAffected;
    }


    // =========================================================================
    // MAINTENANCE / ADMIN METHODS
    // =========================================================================

    public void setMaintenanceMode(boolean enabled) {
        maintenanceMode = enabled;
        systemStatus = enabled ? "MAINTENANCE" : (initialized ? "RUNNING" : "UNKNOWN");
        System.out.println("[SYS] Maintenance mode " + (enabled ? "ENABLED" : "DISABLED"));
        logEvent("ADMIN", "Maintenance mode " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long getUptime() {
        if (startupTime == 0) return 0;
        return System.currentTimeMillis() - startupTime;
    }

    /**
     * Resets the SystemManager to its initial state. Clears all caches, sessions,
     * and counters. Primarily used for testing or recovery scenarios.
     */
    public synchronized void reset() {
        System.out.println("[SYS] *** SYSTEM RESET REQUESTED ***");
        globalCache.clear();
        sessionCache.clear();
        configCache.clear();
        activeSessions.clear();
        sessionHistory.clear();
        errorLog.clear();
        totalRequests = 0;
        errorCount = 0;
        cacheHits = 0;
        cacheMisses = 0;
        startupTime = 0;
        lastHealthCheckTime = 0;
        initialized = false;
        maintenanceMode = false;
        lastInitError = null;
        systemStatus = "UNKNOWN";
        System.out.println("[SYS] *** SYSTEM RESET COMPLETE ***");
    }

    /**
     * Shutdown hook. Should be called when the application is stopping.
     * Logs final statistics and clears resources.
     */
    public synchronized void shutdown() {
        System.out.println("[SYS] *** SYSTEM SHUTDOWN INITIATED ***");
        System.out.println("[SYS] Final stats: requests=" + totalRequests + " errors=" + errorCount
            + " sessions=" + activeSessions.size() + " uptime=" + getUptime() + "ms");

        // Log any remaining active sessions
        if (!activeSessions.isEmpty()) {
            System.out.println("[SYS] WARNING: " + activeSessions.size() + " sessions still active at shutdown:");
            Iterator it = activeSessions.keySet().iterator();
            while (it.hasNext()) {
                String sid = (String) it.next();
                Map info = (Map) activeSessions.get(sid);
                System.out.println("[SYS]   session=" + sid + " user=" + info.get("username"));
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }

        systemStatus = "STOPPED";
        initialized = false;
        System.out.println("[SYS] *** SYSTEM SHUTDOWN COMPLETE ***");
    }

    public static Map getEverything() {
        Map all = new HashMap();
        all.put("globalCache", globalCache);
        all.put("sessionCache", sessionCache);
        all.put("activeSessions", activeSessions);
        all.put("errorLog", errorLog);
        all.put("totalRequests", String.valueOf(totalRequests));
        return all; // exposes internal mutable state!
    }

    // Busy-wait loop - broken visibility pattern
    public static void waitForReady() {
        while (!isReady) {
            // Busy wait - TODO: use proper wait/notify
            System.out.println("Waiting for system ready...");
        }
    }

    public static void setReady(boolean ready) {
        isReady = ready;
    }
}
