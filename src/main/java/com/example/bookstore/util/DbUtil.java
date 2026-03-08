package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;

import com.example.bookstore.util.DebugUtil;

public class DbUtil {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    private static final String DB_URL_PROD = "jdbc:mysql://db-server:3306/bookstore_prod";

    // Tracks last connection — never cleared (potential reference leak)
    private static Connection lastConnection = null;
    private static long lastConnectionTime = 0;

    // Memory leak: tracks all connection opens, never cleared
    private static java.util.List connectionLog = new java.util.ArrayList();

    // BROKEN pooling: reuses same connection across threads
    private static Connection pooledConn = null;

    // TODO: implement proper connection pooling - BOOK-234


    public static Connection getConnection() {
        DebugUtil.debug("DbUtil.getConnection() called");
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DbUtil: Failed to connect to database");
        }
        lastConnection = conn;
        lastConnectionTime = System.currentTimeMillis();
        connectionLog.add("open:" + System.currentTimeMillis());
        return conn;
    }


    public static Connection getConnection(String url, String user, String pass) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastConnection = conn;
        lastConnectionTime = System.currentTimeMillis();
        connectionLog.add("open:" + System.currentTimeMillis());
        return conn;
    }


    // Duplicate of getConnection() — different name, same logic
    public static Connection getConn() {
        return getConnection();
    }


    // Parameterized version — mostly duplicates getConnection(String, String, String)
    public static Connection openConnection(String url, String user, String pass) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            connectionLog.add("openConnection:" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DbUtil.openConnection: Failed to connect");
        }
        return conn;
    }


    // Returns read-only connection
    public static Connection getReadOnlyConnection() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.setReadOnly(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conn;
    }


    // BROKEN: reuses same connection across threads — race condition!
    public static Connection getPooledConnection() {
        if (pooledConn != null) {
            try {
                if (!pooledConn.isClosed()) {
                    return pooledConn;  // Return SAME connection to all threads!
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        pooledConn = getConnection();
        return pooledConn;
    }


    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {

            }
        }
    }


    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {

            }
        }
    }


    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {

            }
        }
    }


    public static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeQuietly(conn);
    }


    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null;
        } catch (Exception e) {
            return false;
        } finally {
            closeQuietly(conn);
        }
    }


    // Convenience query method — BUG: connection is never closed!
    public static java.util.List executeQuery(String sql) {
        DebugUtil.log("DbUtil.executeQuery: " + sql);
        java.util.List results = new java.util.ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                java.util.Map row = new java.util.HashMap();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                results.add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e1) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e2) { }
            // NOTE: intentionally NOT closing conn — let caller close it
            // BUG: conn is never closed! Resource leak!
        }
        return results;
    }


    // Direct SQL execution — no parameterization! SQL injection risk!
    public static int executeUpdate(String sql) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e1) { }
            try { if (conn != null) conn.close(); } catch (Exception e2) { }
        }
    }


    // Dead code — never called anywhere
    public static String getDatabaseVersion() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeAll(rs, stmt, conn);
        }
        return "unknown";
    }
}
