<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.PrintWriter, java.io.StringWriter" %>
<%@ page import="java.util.Enumeration, java.util.Date, java.text.SimpleDateFormat" %>
<%
    String errMsg = (String) request.getAttribute("err");
    if (errMsg == null) errMsg = (String) request.getAttribute("javax.servlet.error.message");
    Exception ex = (Exception) request.getAttribute("exception");
    if (ex == null) ex = (Exception) request.getAttribute("javax.servlet.error.exception");

    // -- Log error to stdout for "monitoring" -- added by dsmith 2008-04
    System.out.println("=== ERROR PAGE RENDERED ===");
    System.out.println("  Time: " + new Date());
    System.out.println("  Error: " + errMsg);
    System.out.println("  User: " + session.getAttribute("user"));
    System.out.println("  Session: " + session.getId());
    System.out.println("  Remote IP: " + request.getRemoteAddr());
    System.out.println("  Request URI: " + request.getAttribute("javax.servlet.error.request_uri"));
    if (ex != null) {
        System.out.println("  Exception: " + ex.getClass().getName() + ": " + ex.getMessage());
        ex.printStackTrace(System.out);
    }
    System.out.println("=== END ERROR LOG ===");

    // Get full stack trace as string for the page -- mchen 2009-07
    String fullStackTrace = "";
    if (ex != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        fullStackTrace = sw.toString();
        pw.close();
        sw.close();
    }

    // Error timestamp
    SimpleDateFormat errDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
    String errorTime = errDateFmt.format(new Date());

    // Get error code if available
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String errorServlet = (String) request.getAttribute("javax.servlet.error.servlet_name");
%>
<html>
<head>
    <title>Error - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">
    <%-- inline error styles -- conflicts with style.css .err class --%>
    <style>
        .err-detail { background: #fff0f0; border: 2px solid #cc0000; padding: 15px; margin: 10px 0; font-family: Courier New, monospace; font-size: 11px; }
        .session-dump { background: #f5f5dc; border: 1px solid #999; padding: 10px; font-size: 10px; font-family: monospace; }
        .env-info { background: #e8f5e9; border: 1px solid #4caf50; padding: 8px; font-size: 10px; margin: 5px 0; }
    </style>
</head>
<body>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td><font size="3" color="white"><b>Bookstore System</b></font></td>
    <td align="right"><a href="/legacy-app/home.do"><font color="white">Home</font></a></td>
</tr>
</table>

<div class="container">
    <h2>System Error</h2>

    <% if (errMsg != null) { %>
        <div class="err">
            <p><%= errMsg %></p>
        </div>
    <% } else { %>
        <div class="err">
            <p>An unexpected error occurred.</p>
        </div>
    <% } %>

    <%-- Error environment info -- should NOT be shown in production! -- tlee 2010-02 --%>
    <div class="env-info">
        <b>Error Time:</b> <%= errorTime %><br>
        <b>Status Code:</b> <%= statusCode != null ? statusCode : "N/A" %><br>
        <b>Servlet:</b> <%= errorServlet != null ? errorServlet : "N/A" %><br>
        <b>Request URI:</b> <%= request.getAttribute("javax.servlet.error.request_uri") %><br>
        <b>Remote Address:</b> <%= request.getRemoteAddr() %><br>
        <b>Server:</b> <%= application.getServerInfo() %><br>
        <b>JVM:</b> <%= System.getProperty("java.version") %><br>
        <b>Contact Admin:</b> <a href="mailto:admin@bookstore-internal.example.com">admin@bookstore-internal.example.com</a>
        (ext. 4502, ask for Dave)
    </div>

    
    <% if (ex != null) { %>
        <h3>Error Details:</h3>
        <div class="err-detail">
            <p><b>Exception:</b> <%= ex.getClass().getName() %>: <%= ex.getMessage() %></p>
            <% if (ex.getCause() != null) { %>
            <p><b>Caused by:</b> <%= ex.getCause().getClass().getName() %>: <%= ex.getCause().getMessage() %></p>
            <% } %>
        </div>
        <h3>Stack Trace:</h3>
        <pre style="background-color: #f0f0f0; padding: 10px; border: 1px solid #ccc; overflow: auto; font-size: 10px;"><%
            ex.printStackTrace(new PrintWriter(out));
        %></pre>
    <% } %>

    <%-- Session attribute dump -- DO NOT DEPLOY TO PROD -- dsmith 2009-06 --%>
    <h3>Session Dump (Debug):</h3>
    <div class="session-dump">
        <table border="1" cellpadding="3" cellspacing="0" width="100%">
            <tr><th bgcolor="#cccccc">Attribute</th><th bgcolor="#cccccc">Value</th><th bgcolor="#cccccc">Type</th></tr>
            <%
                Enumeration attrNames = session.getAttributeNames();
                while (attrNames.hasMoreElements()) {
                    String attrName = (String) attrNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    String typeName = attrValue != null ? attrValue.getClass().getName() : "null";
                    // Truncate long values
                    String displayVal = attrValue != null ? attrValue.toString() : "null";
                    if (displayVal.length() > 200) displayVal = displayVal.substring(0, 200) + "...";
            %>
            <tr>
                <td><b><%= attrName %></b></td>
                <td><%= displayVal %></td>
                <td><font size="1"><%= typeName %></font></td>
            </tr>
            <% } %>
        </table>
        <p><b>Session ID:</b> <%= session.getId() %></p>
        <p><b>Created:</b> <%= new Date(session.getCreationTime()) %></p>
        <p><b>Last Accessed:</b> <%= new Date(session.getLastAccessedTime()) %></p>
    </div>

    <%-- Request header dump -- added for debugging CORS issues -- rjones 2011-11 --%>
    <h3>Request Headers (Debug):</h3>
    <div class="session-dump">
        <table border="1" cellpadding="2" cellspacing="0">
        <%
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String hn = (String) headerNames.nextElement();
        %>
            <tr><td><b><%= hn %></b></td><td><%= request.getHeader(hn) %></td></tr>
        <% } %>
        </table>
    </div>

    <br>
    <p><a href="/legacy-app/home.do">&laquo; Back to Home</a></p>
    <p><a href="/bookstore/login.do">&laquo; Back to Login</a></p>
    <p style="font-size:10px; color:#999;">If this problem persists, contact
        <a href="mailto:admin@bookstore-internal.example.com">admin@bookstore-internal.example.com</a>
        and provide the error time: <b><%= errorTime %></b></p>
</div>

<hr>
<div class="footer">
    <font size="1" color="#999999">Copyright &copy; 2005 Example Bookstore Corp.</font>
</div>

</body>
</html>
