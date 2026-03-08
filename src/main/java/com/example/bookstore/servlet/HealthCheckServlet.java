// Health check endpoint for monitoring
// Added by HN 2019/08 for Nagios integration
// Mapped to /health in web.xml but nobody configured Nagios to call it
// TODO: add memory/thread pool checks - BOOK-389
package com.example.bookstore.servlet;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;

public class HealthCheckServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        boolean dbOk = false;
        String dbMessage = "";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName(DB_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement();
            // Simple connectivity test
            rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                dbOk = true;
                dbMessage = "connected";
            }
        } catch (Exception e) {
            dbOk = false;
            dbMessage = e.getMessage();
            System.out.println("[HealthCheck] DB check failed: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }

        if (dbOk) {
            response.setStatus(200);
            out.println("OK");
            out.println("database: " + dbMessage);
            out.println("uptime: " + getUptimeStr());
            out.println("memory_free: " + Runtime.getRuntime().freeMemory());
            out.println("memory_total: " + Runtime.getRuntime().totalMemory());
        } else {
            response.setStatus(503);
            out.println("ERROR");
            out.println("database: " + dbMessage);
        }
        out.flush();
    }

    private String getUptimeStr() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
    }
}
