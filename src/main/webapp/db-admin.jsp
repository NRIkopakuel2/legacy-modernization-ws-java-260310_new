<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Enumeration" %>
<%
    // No auth check! Diagnostic page accessible to anyone!
%>
<html>
<head>
    <title>Database Admin - Bookstore System</title>
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">
    <style>
        .admin-links { max-width: 600px; margin: 20px auto; padding: 20px; }
        .link-card { display: block; padding: 15px; margin: 10px 0; background: white; border: 1px solid #ccc; border-left: 4px solid #336699; text-decoration: none; color: #333; }
        .link-card:hover { background: #f5f5f5; }
        .link-card h4 { color: #336699; margin: 0 0 5px 0; }
        .link-card p { font-size: 11px; color: #666; margin: 0; }
        .env-info { background: #f5f5f5; padding: 15px; border: 1px solid #ddd; margin-top: 20px; font-size: 10px; }
    </style>
</head>
<body>

<div class="admin-links">
    <h2>Database Administration</h2>

    <a href="http://localhost:8082/" target="_blank" class="link-card">
        <h4>&#128187; phpMyAdmin</h4>
        <p>Open phpMyAdmin web interface (port 8082). Login: root / root</p>
    </a>

    <a href="/legacy-app/db-ping.jsp" class="link-card">
        <h4>&#128268; Database Ping</h4>
        <p>Quick JDBC connection test and table existence check.</p>
    </a>

    <a href="/legacy-app/test-db.jsp" class="link-card">
        <h4>&#128295; Extended DB Test</h4>
        <p>JDBC + Hibernate + session debug output.</p>
    </a>

    
    <div class="env-info">
        <b>Server Environment:</b>
        <table cellpadding="2">
            <tr><td style="font-weight:bold;">Server Info:</td><td><%= application.getServerInfo() %></td></tr>
            <tr><td style="font-weight:bold;">Servlet API:</td><td><%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></td></tr>
            <tr><td style="font-weight:bold;">Java Version:</td><td><%= System.getProperty("java.version") %></td></tr>
            <tr><td style="font-weight:bold;">Java Home:</td><td><%= System.getProperty("java.home") %></td></tr>
            <tr><td style="font-weight:bold;">OS:</td><td><%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %></td></tr>
            <tr><td style="font-weight:bold;">User:</td><td><%= System.getProperty("user.name") %></td></tr>
            <tr><td style="font-weight:bold;">Free Memory:</td><td><%= Runtime.getRuntime().freeMemory() / 1024 / 1024 %> MB</td></tr>
            <tr><td style="font-weight:bold;">Total Memory:</td><td><%= Runtime.getRuntime().totalMemory() / 1024 / 1024 %> MB</td></tr>
            <tr><td style="font-weight:bold;">Session ID:</td><td><%= session.getId() %></td></tr>
        </table>
    </div>

    
    <div class="env-info" style="border-left: 4px solid #ff9800;">
        <b>Database Connection Info:</b>
        <table cellpadding="2">
            <tr><td style="font-weight:bold;">Host:</td><td>legacy-mysql:3306</td></tr>
            <tr><td style="font-weight:bold;">Database:</td><td>legacy_db</td></tr>
            <tr><td style="font-weight:bold;">User:</td><td>legacy_user</td></tr>
            <tr><td style="font-weight:bold;">Password:</td><td>legacy_pass</td></tr>
            <tr><td style="font-weight:bold;">Root Password:</td><td>root</td></tr>
        </table>
    </div>

    <hr>
    <p style="font-size:11px;">
        <a href="/legacy-app/home.do">Home</a>
    </p>
</div>

</body>
</html>
