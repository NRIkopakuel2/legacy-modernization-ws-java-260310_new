<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="java.util.Enumeration, java.util.Date, java.text.SimpleDateFormat" %>
<%
    // Get session attributes with NO null check (NPE risk!)
    String role = (String) session.getAttribute("role");
    String user = (String) session.getAttribute("user");
    String loginTime = (String) session.getAttribute("loginTime");

    // Business logic in JSP — should be in Action!
    String requiredRole = (String) request.getAttribute("requiredRole");
    if (requiredRole == null) requiredRole = "ADMIN";

    // -- IP logging for security audit -- added by security team 2011-02
    String clientIp = request.getHeader("X-Forwarded-For");
    if (clientIp == null || clientIp.length() == 0) clientIp = request.getRemoteAddr();
    String userAgent = request.getHeader("User-Agent");

    System.out.println("!!! UNAUTHORIZED ACCESS ATTEMPT !!!");
    System.out.println("  User: " + user + " Role: " + role + " Required: " + requiredRole);
    System.out.println("  IP: " + clientIp);
    System.out.println("  User-Agent: " + userAgent);
    System.out.println("  Session ID: " + session.getId());
    System.out.println("  Time: " + new Date());
    System.out.println("  Requested URI: " + request.getAttribute("javax.servlet.forward.request_uri"));

    // Date formatting for display
    SimpleDateFormat unauthFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    String accessTime = unauthFmt.format(new Date());

    // Count consecutive unauthorized attempts from this session -- dsmith 2011-08
    Integer attemptCount = (Integer) session.getAttribute("_unauthAttempts");
    if (attemptCount == null) attemptCount = new Integer(0);
    attemptCount = new Integer(attemptCount.intValue() + 1);
    session.setAttribute("_unauthAttempts", attemptCount);
    System.out.println("  Consecutive unauthorized attempts: " + attemptCount);
%>
<html>
<head>
    <title>Unauthorized Access - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        .unauth-container {
            width: 600px;
            margin: 60px auto;
            padding: 30px;
            background-color: white;
            border: 1px solid #cc0000;
            border-top: 4px solid #cc0000;
        }
        .unauth-icon { font-size: 48px; text-align: center; color: #cc0000; }
        .unauth-title { font-size: 22px; color: #cc0000; font-weight: bold; text-align: center; margin: 15px 0; }
        .info-box { background-color: #fff3cd; border: 1px solid #ffc107; padding: 12px; margin: 15px 0; font-size: 12px; }
        .role-info { font-weight: bold; }
        .debug-dump { background: #f5f5dc; border: 1px solid #999; padding: 8px; font-size: 9px; font-family: monospace; margin: 10px 0; max-height: 200px; overflow: auto; }
    </style>

    
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    <script type="text/javascript">

        var secondsLeft = 15; // increased from 10 to give users time to read -- tlee 2012-01

        function startCountdown() {
            var timer = setInterval(function() {
                secondsLeft--;
                document.getElementById("countdown").innerHTML = secondsLeft;
                document.title = "(" + secondsLeft + ") Unauthorized - Bookstore";
                if (secondsLeft <= 0) {
                    clearInterval(timer);
                    // BUG: hardcoded URL, should use APP_CONTEXT -- dsmith 2011-06
                    // Sometimes redirects to /bookstore/ instead of /legacy-app/
                    window.location.href = "http://localhost:8080/legacy-app/home.do";
                }
            }, 1000);
        }

        $(document).ready(function() {
            startCountdown();
            // Log to browser console for debugging
            console.log("=== UNAUTHORIZED ACCESS DEBUG ===");
            console.log("User: <%= user %>, Role: <%= role %>, Required: <%= requiredRole %>");
            console.log("IP: <%= clientIp %>");
            console.log("Attempt #<%= attemptCount %>");
            console.log("Session: <%= session.getId() %>");
        });
    </script>
</head>
<body>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td><font size="3"><b>Bookstore System</b></font></td>
    <td align="right">
        <font color="white">User: <%= user %> [<%= role %>]</font>&nbsp;
        <a href="/legacy-app/logout.do?method=logout"><font color="white">Logout</font></a>
    </td>
</tr>
</table>

<div class="unauth-container">
    <div class="unauth-icon">&#9888;</div>
    <div class="unauth-title">Access Denied</div>

    <p>You do not have permission to access this resource.</p>

    <% if (attemptCount.intValue() >= 3) { %>
    <div style="background:#ffcccc; border:2px solid #cc0000; padding:10px; margin:10px 0; font-size:12px;">
        <b>WARNING:</b> You have made <%= attemptCount %> unauthorized access attempts in this session.
        Further attempts may be logged for security review.
    </div>
    <% } %>

    
    <div class="info-box">
        <table width="100%" cellpadding="3">
            <tr>
                <td width="40%"><span class="role-info">Current User:</span></td>
                <td><%= user %></td>
            </tr>
            <tr>
                <td><span class="role-info">Your Role:</span></td>
                <td><%= role %></td>
            </tr>
            <tr>
                <td><span class="role-info">Required Role:</span></td>
                <td><%= requiredRole %></td>
            </tr>
            <tr>
                <td><span class="role-info">Login Time:</span></td>
                <td><%= loginTime %></td>
            </tr>
            <tr>
                <td><span class="role-info">Access Time:</span></td>
                <td><%= accessTime %></td>
            </tr>
            <tr>
                <td><span class="role-info">Client IP:</span></td>
                <td><%= clientIp %></td>
            </tr>
            <tr>
                <td><span class="role-info">Attempt #:</span></td>
                <td><%= attemptCount %></td>
            </tr>
            <tr>
                <td><span class="role-info">Session ID:</span></td>
                
                <td><font size="1"><%= session.getId() %></font></td>
            </tr>
        </table>
    </div>

    <%-- Session attribute dump -- "helps with debugging auth issues" -- rjones 2011-05 --%>
    <h4 style="font-size:11px; color:#666;">Session Debug Info:</h4>
    <div class="debug-dump">
        <%
            Enumeration unauthAttrNames = session.getAttributeNames();
            while (unauthAttrNames.hasMoreElements()) {
                String an = (String) unauthAttrNames.nextElement();
                Object av = session.getAttribute(an);
                String avStr = av != null ? av.toString() : "null";
                if (avStr.length() > 100) avStr = avStr.substring(0, 100) + "...";
        %>
        <b><%= an %></b> = <%= avStr %> [<%= av != null ? av.getClass().getSimpleName() : "null" %>]<br>
        <% } %>
        <hr size="1">
        Session Created: <%= new Date(session.getCreationTime()) %><br>
        Last Accessed: <%= new Date(session.getLastAccessedTime()) %><br>
        Max Inactive: <%= session.getMaxInactiveInterval() %>s<br>
        Request Method: <%= request.getMethod() %><br>
        Request URI: <%= request.getRequestURI() %><br>
        Query String: <%= request.getQueryString() %><br>
        Referer: <%= request.getHeader("Referer") %><br>
        User-Agent: <%= userAgent != null && userAgent.length() > 80 ? userAgent.substring(0, 80) + "..." : userAgent %><br>
    </div>

    
    <%
        if ("CLERK".equals(role)) {
    %>
        <p style="color: #856404; background-color: #fff3cd; padding: 10px; border-left: 3px solid #ffc107;">
            <b>Note for Clerks:</b> This function is only available to Managers and
            Administrators. Please contact your manager if you need access.
        </p>
    <%
        } else if ("MANAGER".equals(role)) {
    %>
        <p style="color: #856404; background-color: #fff3cd; padding: 10px; border-left: 3px solid #ffc107;">
            <b>Note for Managers:</b> This admin function is restricted to system
            administrators only. Contact IT department for access.
        </p>
    <%
        }
    %>

    <div style="text-align: center; margin-top: 20px;">
        
        <a href="/legacy-app/home.do" class="btn">Back to Home</a>
        &nbsp;
        <%-- old link that points to wrong context -- never removed --%>
        <a href="/bookstore/home.do" class="btn" style="background:#999;">Home (Old)</a>
    </div>

    <p style="text-align:center; margin-top:15px; font-size:12px; color:#999;">
        Redirecting in <span id="countdown" style="font-weight:bold; color:#cc0000;">15</span> seconds...
    </p>
</div>

<hr>
<div style="text-align:center; font-size:10px; color:#999; padding:10px;">
    Copyright &copy; 2005 Example Bookstore Corp. All rights reserved.
</div>

</body>
</html>
