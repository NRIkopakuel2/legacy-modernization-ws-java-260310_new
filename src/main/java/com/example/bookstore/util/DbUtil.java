package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;

public class DbUtil {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    private static final String DB_URL_PROD = "jdbc:mysql://db-server:3306/bookstore_prod";

    
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DbUtil: Failed to connect to database");
        }
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
        return conn;
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
}
