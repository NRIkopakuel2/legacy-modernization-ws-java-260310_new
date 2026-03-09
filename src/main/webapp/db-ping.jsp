<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%
    // No auth check — diagnostic page accessible to anyone!
    // SECURITY: exposes database connection details
%>
<html>
<head>
    <title>Database Connection Test - Bookstore System</title>
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
    <h2>Database Connection Test</h2>
    <p style="font-size:11px; color:#999;">Server Time: <%= new Date() %></p>

    
    
    
    <h3>1. JDBC Direct Connection</h3>
    <%
        // INLINE JDBC IN JSP — severe anti-pattern!
        // Hard-coded credentials exposed in view layer!
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
        <div class="test-label">&#10004; JDBC Connection: SUCCESS</div>
        <div class="test-detail">
            
            URL: <%= jdbcUrl %><br>
            User: <%= jdbcUser %><br>
            Database: <%= conn.getCatalog() %><br>
            Connection Time: <%= elapsed %>ms<br>
            Auto-Commit: <%= conn.getAutoCommit() %><br>
            Read-Only: <%= conn.isReadOnly() %>
        </div>
    </div>
    <%
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
    %>
    <div class="test-result test-fail">
        <div class="test-label">&#10008; JDBC Connection: FAILED</div>
        <div class="test-detail">
            URL: <%= jdbcUrl %><br>
            Error: <%= e.getMessage() %><br>
            Time: <%= elapsed %>ms
        </div>
        
        <pre class="stacktrace"><%
            e.printStackTrace(new java.io.PrintWriter(out));
        %></pre>
    </div>
    <%
        } finally {
            if (conn != null) { try { conn.close(); } catch (Exception e) { } }
        }
    %>

    
    
    
    <h3>2. Table Existence Check</h3>
    <%
        Connection conn2 = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn2 = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
            DatabaseMetaData meta = conn2.getMetaData();

            // Check each table — hard-coded table names!
            String[] tables = {"users", "books", "categories", "authors", "orders",
                               "order_items", "customer", "shopping_cart",
                               "stock_transaction", "supplier", "purchase_order", "audit_log"};
            for (int t = 0; t < tables.length; t++) {
                ResultSet rs = meta.getTables(null, null, tables[t], new String[]{"TABLE"});
                boolean exists = rs.next();
                rs.close();
    %>
    <div class="test-result <%= exists ? "test-pass" : "test-fail" %>" style="padding:5px; margin:3px 0;">
        <span class="test-label" style="font-size:11px;"><%= exists ? "&#10004;" : "&#10008;" %> <%= tables[t] %></span>
    </div>
    <%
            }
        } catch (Exception e) {
    %>
    <div class="test-result test-fail">
        <div class="test-label">&#10008; Table check failed: <%= e.getMessage() %></div>
    </div>
    <%
        } finally {
            if (conn2 != null) { try { conn2.close(); } catch (Exception e) { } }
        }
    %>

    
    
    
    <h3>3. Row Counts</h3>
    <%
        Connection conn3 = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn3 = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
            stmt = conn3.createStatement();

            String[] countTables = {"users", "books", "categories", "orders", "customer"};
            for (int ct = 0; ct < countTables.length; ct++) {
                try {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + countTables[ct]);
                    rs.next();
                    int count = rs.getInt(1);
                    rs.close();
    %>
    <div class="test-result test-pass" style="padding:3px 10px; margin:2px 0;">
        <span style="font-size:11px;"><%= countTables[ct] %>: <b><%= count %></b> rows</span>
    </div>
    <%
                } catch (Exception e) {
    %>
    <div class="test-result test-fail" style="padding:3px 10px; margin:2px 0;">
        <span style="font-size:11px;"><%= countTables[ct] %>: ERROR — <%= e.getMessage() %></span>
    </div>
    <%
                }
            }
        } catch (Exception e) {
            out.println("<div class='test-result test-fail'>Row count failed: " + e.getMessage() + "</div>");
        } finally {
            if (stmt != null) { try { stmt.close(); } catch (Exception e) { } }
            if (conn3 != null) { try { conn3.close(); } catch (Exception e) { } }
        }
    %>

    <hr>
    <p style="font-size:11px;">
        <a href="/legacy-app/home.do">Home</a> |
        <a href="/legacy-app/test-db.jsp">Extended DB Test</a> |
        <a href="/legacy-app/db-admin.jsp">DB Admin</a>
    </p>
</div>

</body>
</html>
