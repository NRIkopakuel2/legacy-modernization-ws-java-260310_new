<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Enumeration" %>
<%
    // No auth check — diagnostic page accessible to anyone!
    // COPY-PASTED from db-ping.jsp with additions!
%>
<html>
<head>
    <title>Extended Database Test - Bookstore System</title>
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">
    
    <style>
        .test-container { max-width: 700px; margin: 20px auto; padding: 20px; }
        .test-result { padding: 10px; margin: 10px 0; border: 1px solid #ccc; }
        .test-pass { background: #e8f5e9; border-color: #4CAF50; }
        .test-fail { background: #ffebee; border-color: #f44336; }
        .test-label { font-weight: bold; font-size: 12px; }
        .test-detail { font-size: 11px; color: #666; margin-top: 5px; }
        pre.stacktrace { background: #f5f5f5; padding: 10px; border: 1px solid #ddd; font-size: 10px; overflow: auto; max-height: 200px; }
    </style>
</head>
<body>

<div class="test-container">
    <h2>Extended Database Test</h2>
    <p style="font-size:11px; color:#999;">Server Time: <%= new Date() %></p>

    
    
    
    <h3>1. JDBC Direct Connection</h3>
    <%
        // COPY-PASTED from db-ping.jsp!
        String jdbcUrl = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
        String jdbcUser = "legacy_user";
        String jdbcPass = "legacy_pass";
        Connection conn = null;
        long startTime = System.currentTimeMillis();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
            long elapsed = System.currentTimeMillis() - startTime;
    %>
    <div class="test-result test-pass">
        <div class="test-label">&#10004; JDBC: OK (<%= elapsed %>ms)</div>
        <div class="test-detail">
            Database: <%= conn.getCatalog() %> | User: <%= jdbcUser %>
        </div>
    </div>
    <%
            // COPY-PASTED: Row counts
            Statement stmt = conn.createStatement();
            String[] countTables = {"users", "books", "categories", "orders", "customer", "stock_transaction", "audit_log"};
            for (int ct = 0; ct < countTables.length; ct++) {
                try {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + countTables[ct]);
                    rs.next();
    %>
    <div class="test-result test-pass" style="padding:3px 10px; margin:2px 0;">
        <span style="font-size:11px;"><%= countTables[ct] %>: <b><%= rs.getInt(1) %></b> rows</span>
    </div>
    <%
                    rs.close();
                } catch (Exception e) {
    %>
    <div class="test-result test-fail" style="padding:3px 10px; margin:2px 0;">
        <span style="font-size:11px;"><%= countTables[ct] %>: ERROR</span>
    </div>
    <%
                }
            }
            stmt.close();
        } catch (Exception e) {
    %>
    <div class="test-result test-fail">
        <div class="test-label">&#10008; JDBC: FAILED</div>
        <pre class="stacktrace"><% e.printStackTrace(new java.io.PrintWriter(out)); %></pre>
    </div>
    <%
        } finally {
            if (conn != null) { try { conn.close(); } catch (Exception e) { } }
        }
    %>

    
    
    
    <h3>2. Hibernate Session Test</h3>
    <%
        // Hibernate test — also inline in JSP! (even worse than JDBC!)
        try {
            Class hibUtilClass = Class.forName("com.example.bookstore.util.HibernateUtil");
            java.lang.reflect.Method getSessionFactory = hibUtilClass.getMethod("getSessionFactory", new Class[0]);
            Object sf = getSessionFactory.invoke(null, new Object[0]);
            if (sf != null) {
                java.lang.reflect.Method openSession = sf.getClass().getMethod("openSession", new Class[0]);
                Object hibSession = openSession.invoke(sf, new Object[0]);
    %>
    <div class="test-result test-pass">
        <div class="test-label">&#10004; Hibernate SessionFactory: OK</div>
        <div class="test-detail">Session opened successfully</div>
    </div>
    <%
                // Close session
                java.lang.reflect.Method closeSession = hibSession.getClass().getMethod("close", new Class[0]);
                closeSession.invoke(hibSession, new Object[0]);
            }
        } catch (Exception e) {
    %>
    <div class="test-result test-fail">
        <div class="test-label">&#10008; Hibernate: FAILED</div>
        <div class="test-detail"><%= e.getMessage() %></div>
        <pre class="stacktrace"><% e.printStackTrace(new java.io.PrintWriter(out)); %></pre>
    </div>
    <%
        }
    %>

    
    
    
    <h3>3. HTTP Session Debug</h3>
    <div class="test-result" style="background:#fff3e0; border-color:#ff9800;">
        <div class="test-label">Session Information</div>
        <div class="test-detail">
            <table cellpadding="3" style="font-size:10px;">
                <tr><td style="font-weight:bold;">Session ID:</td><td><%= session.getId() %></td></tr>
                <tr><td style="font-weight:bold;">Created:</td><td><%= new Date(session.getCreationTime()) %></td></tr>
                <tr><td style="font-weight:bold;">Last Accessed:</td><td><%= new Date(session.getLastAccessedTime()) %></td></tr>
                <tr><td style="font-weight:bold;">Max Inactive:</td><td><%= session.getMaxInactiveInterval() %> seconds</td></tr>
            </table>
            <br>
            <b>Session Attributes:</b>
            <table cellpadding="2" style="font-size:10px; border-collapse:collapse;">
                <%
                    // Dump ALL session attributes — SECURITY ISSUE!
                    Enumeration attrNames = session.getAttributeNames();
                    while (attrNames.hasMoreElements()) {
                        String attrName = (String) attrNames.nextElement();
                        Object attrValue = session.getAttribute(attrName);
                        String displayValue = attrValue != null ? attrValue.toString() : "null";
                        // Truncate long values
                        if (displayValue.length() > 100) {
                            displayValue = displayValue.substring(0, 100) + "...";
                        }
                %>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="font-weight:bold; color:#e65100; padding:2px 8px;"><%= attrName %></td>
                    
                    <td style="padding:2px 8px;"><%= displayValue %></td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </div>

    <hr>
    <p style="font-size:11px;">
        <a href="/legacy-app/home.do">Home</a> |
        <a href="/legacy-app/db-ping.jsp">Simple DB Test</a> |
        <a href="/legacy-app/db-admin.jsp">DB Admin</a>
    </p>
</div>

</body>
</html>
