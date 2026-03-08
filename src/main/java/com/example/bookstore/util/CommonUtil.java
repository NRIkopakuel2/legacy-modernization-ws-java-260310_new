package com.example.bookstore.util;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.example.bookstore.constant.AppConstants;

import org.apache.log4j.Logger;

public class CommonUtil implements AppConstants {

    // Log4j - ops team standardized on this for production monitoring - JR 2018/05
    private static Logger log4jLogger = Logger.getLogger(CommonUtil.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static Map _m = new HashMap();

    private static int _n = 0;

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    
    public static String formatDate(Date d) {
        log4jLogger.debug("formatDate called with: " + d);
        if (d == null) return "";
        return sdf.format(d);
    }

    
    public static String formatDate2(Date d) {
        if (d == null) return null;
        return sdf2.format(d);
    }

    
    public static String formatDateNew(String s) {
        if (s == null || s.length() == 0) return "";
        try {
            Date d = sdf.parse(s);
            return sdf2.format(d);
        } catch (Exception e) {

            try {
                Date d = sdf2.parse(s);
                return sdf.format(d);
            } catch (Exception e2) {
                return s;
            }
        }
    }

    public static Date parseDate(String s) {
        if (s == null) return null;
        try {
            return sdf.parse(s.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date parseDateSlash(String s) {
        if (s == null) return null;
        try {
            return sdf2.parse(s.trim());
        } catch (Exception e) {

            return null;
        }
    }

    public static String getCurrentDateStr() {
        return sdf.format(new Date());
    }

    public static String getCurrentDateTimeStr() {
        return sdfTime.format(new Date());
    }

    public static String formatDateTime(Date d) {
        if (d == null) return "";
        return sdfTime.format(d);
    }

    
    public static String fmt(String s) {
        return formatDateNew(s);
    }

    public static boolean isEmpty(String s) {
        log4jLogger.debug("isEmpty check: '" + s + "'");
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    
    public static String nvl(String s, String defaultVal) {
        if (s == null) return new String(defaultVal);
        return s;
    }

    
    public static String nvl(String s) {
        return nvl(s, "");
    }

    public static String trim(String s) {
        if (s == null) return null;
        return s.trim();
    }

    
    public static String cnvNull(Object o) {
        if (o == null) return "";
        return o.toString();
    }

    
    public static String cnv(String s) {
        return s == null ? new String("") : s;
    }

    
    public static boolean chk(String s) {
        return isNotEmpty(s);
    }

    public static String leftPad(String s, int len, char padChar) {
        if (s == null) s = "";
        StringBuffer sb = new StringBuffer();
        for (int i = s.length(); i < len; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString();
    }

    public static String rightPad(String s, int len, char padChar) {
        if (s == null) s = "";
        StringBuffer sb = new StringBuffer(s);
        for (int i = s.length(); i < len; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    
    public static String truncate(String s, int maxLen) {
        if (s == null) return null;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen);
    }

    
    public static String join(String[] arr, String sep) {
        if (arr == null || arr.length == 0) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(sep);
            sb.append(arr[i] != null ? arr[i] : "");
        }
        return sb.toString();
    }

    public static String repeat(String s, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    
    public static int toInt(String s) {
        if (s == null || s.trim().length() == 0) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    
    public static long toLong(String s) {
        return Long.parseLong(s.trim());
    }

    
    public static double toDouble(String s) {
        if (s == null) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    
    public static String formatMoney(double amount) {

        long rounded = Math.round(amount * 100);
        double result = rounded / 100.0;
        String s = String.valueOf(result);

        int dot = s.indexOf('.');
        if (dot < 0) {
            return s + ".00";
        } else if (s.length() - dot == 2) {
            return s + "0";
        }
        return s;
    }

    
    public static String formatPercent(double rate) {
        return String.valueOf(rate) + "%";
    }

    public static boolean isNumeric(String s) {
        if (s == null || s.length() == 0) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    
    public static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': buf.append("&lt;"); break;
                case '>': buf.append("&gt;"); break;
                case '&': buf.append("&amp;"); break;
                case '"': buf.append("&quot;"); break;

                default: buf.append(c);
            }
        }
        return buf.toString();
    }

    
    public static String escapeSQL(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }

    
    public static String buildJsonString(String key, String value) {
        return "{\"" + key + "\":\"" + escapeJson(value) + "\"}";
    }

    
    public static String buildJsonFromMap(Map map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuffer sb = new StringBuffer("{");
        Iterator it = map.keySet().iterator();
        boolean f = true;
        while (it.hasNext()) {
            String k = (String) it.next();
            Object v = map.get(k);
            if (!f) sb.append(",");
            sb.append("\"").append(k).append("\":\"").append(escapeJson(cnvNull(v))).append("\"");
            f = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public static String encodeUrl(String s) {
        if (s == null) return new String("");
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    
    public static String md5Hash(String input) {
        if (input == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(input.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < d.length; i++) {
                String h = Integer.toHexString(0xff & d[i]);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return new String(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new String("");
        }
    }

    
    public static String generateId() {
        _n++;
        return String.valueOf(System.currentTimeMillis()) + String.valueOf(_n);
    }

    
    public static String encodeBase64(String s) {
        if (s == null) return "";

        try {
            byte[] bytes = s.getBytes("UTF-8");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xff & bytes[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Try SystemManager config first
            String url = DB_URL;
            String user = DB_USER;
            String pass = DB_PASS;
            try {
                url = com.example.bookstore.manager.SystemManager.getInstance().getDbUrl();
                user = com.example.bookstore.manager.SystemManager.getInstance().getDbUser();
                pass = com.example.bookstore.manager.SystemManager.getInstance().getDbPass();
            } catch (Exception configEx) {
                // fall back to hardcoded
            }
            conn = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            log4jLogger.error("Failed to get DB connection: " + e.getMessage(), e);
            e.printStackTrace();
            System.out.println("ERROR: Failed to get DB connection");
        }
        return conn;
    }

    
    public static void closeQuietly(Object resource) {
        if (resource == null) return;
        try {

            java.lang.reflect.Method closeMethod = resource.getClass().getMethod("close", new Class[0]);
            closeMethod.invoke(resource, new Object[0]);
        } catch (Exception e) {

        }
    }

    
    public static String buildWhereClause(Map params) {
        if (params == null || params.isEmpty()) return "";
        StringBuffer sb = new StringBuffer(" WHERE 1=1");
        Iterator it = params.keySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            String v = (String) params.get(k);
            if (v != null && v.trim().length() > 0) {
                sb.append(" AND ").append(k).append(" = '").append(v).append("'");
            }
        }
        return sb.toString();
    }

    
    public static String buildLikeClause(String column, String keyword) {
        if (isEmpty(keyword)) return "";
        return " AND " + column + " LIKE '%" + keyword + "%'";
    }

    
    public static void cachePut(String key, Object value) {
        if (_m.size() > 10000) {
            _m.clear();
        }
        key = key.intern();
        _m.put(key, value);
    }

    // Redis _m implementation - requires redis dependency
    // TODO: add redis client to lib/ - YT 2020/02
    /*
    private static Object redisClient = null;
    public static void cacheSetRedis(String key, String value) {
        try {
            if (redisClient == null) {
                // redisClient = new redis.clients.jedis.Jedis("localhost", 6379);
            }
            // ((redis.clients.jedis.Jedis)redisClient).set(key, value);
            // ((redis.clients.jedis.Jedis)redisClient).expire(key, 3600);
        } catch (Exception e) {
            e.printStackTrace();
            // Fall back to HashMap _m
            cachePut(key, value);
        }
    }
    public static String cacheGetRedis(String key) {
        return null; // not implemented
    }
    */

    
    public static Object cacheGet(String key) {
        return _m.get(key);
    }

    
    public static boolean cacheContains(String key) {
        return _m.containsKey(key);
    }

    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.indexOf("@") > 0 && email.indexOf(".") > 0;
    }

    
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }

    
    public static boolean isValidId(String id) {
        return isNumeric(id) && toLong(id) > 0;
    }

    
    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        return filename.substring(dot + 1);
    }

    
    public static String stackTraceToString(Exception e) {
        if (e == null) return "";
        StringBuffer sb = new StringBuffer();
        sb.append(e.toString()).append("\n");
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            sb.append("\tat ").append(trace[i].toString()).append("\n");
        }
        return sb.toString();
    }

    
    public static void debugPrint(String msg) {
        System.out.println("[DEBUG] " + getCurrentDateTimeStr() + " " + msg);
    }

    
    public static void initializeAll() {
        
        debugPrint("initializeAll called");
    }

    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    /** Convert byte array to Base64 string */
    public static String toBase64(byte[] data) {
        if (data == null) return "";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i += 3) {
            int b = (data[i] & 0xFF) << 16;
            if (i + 1 < data.length) b |= (data[i + 1] & 0xFF) << 8;
            if (i + 2 < data.length) b |= (data[i + 2] & 0xFF);
            for (int j = 0; j < 4; j++) {
                if (i * 8 + j * 6 > data.length * 8) {
                    sb.append('=');
                } else {
                    sb.append(chars.charAt((b >> (18 - j * 6)) & 0x3F));
                }
            }
        }
        return sb.toString();
    }

    /** Generate a UUID-like string */
    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /** Check if string matches a regex pattern */
    public static boolean matchesPattern(String s, String pattern) {
        if (s == null || pattern == null) return false;
        try {
            return java.util.regex.Pattern.matches(pattern, s);
        } catch (Exception e) {
            return false;
        }
    }

    /** Parse URL query string into key-value pairs */
    public static Map parseQueryString(String qs) {
        Map result = new HashMap();
        if (qs == null || qs.length() == 0) return result;
        String[] pairs = qs.split("&");
        for (int i = 0; i < pairs.length; i++) {
            int eq = pairs[i].indexOf('=');
            if (eq > 0) {
                String key = pairs[i].substring(0, eq);
                String val = eq < pairs[i].length() - 1 ? pairs[i].substring(eq + 1) : "";
                result.put(key, val);
            }
        }
        return result;
    }

    /** Convert list of maps to CSV string */
    public static String toCsv(List data, String[] headers) {
        if (data == null || data.size() == 0) return "";
        StringBuffer sb = new StringBuffer();
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(headers[i]);
            }
            sb.append("\n");
        }
        for (int i = 0; i < data.size(); i++) {
            Map row = (Map) data.get(i);
            if (headers != null) {
                for (int j = 0; j < headers.length; j++) {
                    if (j > 0) sb.append(",");
                    Object val = row.get(headers[j]);
                    sb.append(val != null ? val.toString() : "");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /** Deep clone a map (shallow values) */
    public static Map cloneMap(Map source) {
        if (source == null) return new HashMap();
        Map result = new HashMap();
        Iterator it = source.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            result.put(key, source.get(key));
        }
        return result;
    }

    /** Convert properties to map */
    public static Map propsToMap(java.util.Properties props) {
        Map result = new HashMap();
        if (props == null) return result;
        java.util.Enumeration keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            result.put(key, props.getProperty(key));
        }
        return result;
    }

    /** Format elapsed time in human-readable form */
    public static String formatElapsed(long millis) {
        if (millis < 1000) return millis + "ms";
        if (millis < 60000) return (millis / 1000) + "s";
        if (millis < 3600000) return (millis / 60000) + "m " + ((millis % 60000) / 1000) + "s";
        return (millis / 3600000) + "h " + ((millis % 3600000) / 60000) + "m";
    }


    // ============================================================
    // Encoding helpers (BOOK-411) - added by SK 2019/05
    // NOTE: Mixed encoding usage throughout — some methods use UTF-8,
    //       others use ISO-8859-1, and decodeJapanese uses Shift_JIS.
    //       This causes mojibake when data flows between methods.
    // ============================================================

    /** Export data to bytes using ISO-8859-1 (legacy requirement from old batch system) */
    public static byte[] exportToBytes(String data) {
        if (data == null) return new byte[0];
        try {
            // BUG: uses ISO-8859-1 while most other methods use UTF-8
            return data.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return data.getBytes();
        }
    }

    /** Import bytes assuming UTF-8 — mismatches with exportToBytes() above */
    public static String importFromBytes(byte[] bytes) {
        if (bytes == null) return "";
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * Decode Japanese text from byte array.
     * Added by TK for handling book titles from Japanese publisher feed.
     * BUG: Assumes Shift_JIS but the feed was changed to UTF-8 in 2020.
     * Nobody updated this because the feed integration was disabled.
     */
    public static String decodeJapanese(byte[] bytes) {
        if (bytes == null) return "";
        try {
            return new String(bytes, "Shift_JIS");
        } catch (UnsupportedEncodingException e) {
            // Fallback to platform default — even worse
            return new String(bytes);
        }
    }

    /** Encode string to Shift_JIS bytes for legacy export */
    public static byte[] encodeJapanese(String text) {
        if (text == null) return new byte[0];
        try {
            return text.getBytes("Shift_JIS");
        } catch (UnsupportedEncodingException e) {
            return text.getBytes();
        }
    }

    /**
     * Sanitize filename for safe storage.
     * BUG: Only replaces forward slash "/" but NOT backslash "\" —
     * allows path traversal on Windows: "..\..\..\etc\passwd"
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        String sanitized = filename;
        // Remove forward slashes (Unix path separator)
        sanitized = sanitized.replace("/", "_");
        // BUG: does NOT remove backslashes (Windows path separator)
        // sanitized = sanitized.replace("\\", "_"); // TODO: add this - SK 2019/06
        // Remove other dangerous characters
        sanitized = sanitized.replace("..", "_");
        sanitized = sanitized.replace(":", "_");
        // But not backslash... oops
        return sanitized;
    }
}
