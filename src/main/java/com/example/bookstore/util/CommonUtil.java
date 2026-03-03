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

import com.example.bookstore.constant.AppConstants;

public class CommonUtil implements AppConstants {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static Map cache = new HashMap();

    private static int counter = 0;

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    
    public static String formatDate(Date d) {
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
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;

                default: sb.append(c);
            }
        }
        return sb.toString();
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
        boolean first = true;
        while (it.hasNext()) {
            String key = (String) it.next();
            Object val = map.get(key);
            if (!first) sb.append(",");
            sb.append("\"").append(key).append("\":\"").append(escapeJson(cnvNull(val))).append("\"");
            first = false;
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
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return new String(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new String("");
        }
    }

    
    public static String generateId() {
        counter++;
        return String.valueOf(System.currentTimeMillis()) + String.valueOf(counter);
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
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
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
            String key = (String) it.next();
            String val = (String) params.get(key);
            if (val != null && val.trim().length() > 0) {
                sb.append(" AND ").append(key).append(" = '").append(val).append("'");
            }
        }
        return sb.toString();
    }

    
    public static String buildLikeClause(String column, String keyword) {
        if (isEmpty(keyword)) return "";
        return " AND " + column + " LIKE '%" + keyword + "%'";
    }

    
    public static void cachePut(String key, Object value) {
        if (cache.size() > 10000) {
            cache.clear();
        }
        key = key.intern();
        cache.put(key, value);
    }

    
    public static Object cacheGet(String key) {
        return cache.get(key);
    }

    
    public static boolean cacheContains(String key) {
        return cache.containsKey(key);
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
}
