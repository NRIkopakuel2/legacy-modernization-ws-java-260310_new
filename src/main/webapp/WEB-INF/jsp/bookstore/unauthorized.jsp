<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Get session attributes with NO null check (NPE risk!)
    String role = (String) session.getAttribute("role");
    String user = (String) session.getAttribute("user");
    String loginTime = (String) session.getAttribute("loginTime");

    // Business logic in JSP — should be in Action!
    String requiredRole = (String) request.getAttribute("requiredRole");
    if (requiredRole == null) requiredRole = "ADMIN";
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
    </style>

    
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    <script type="text/javascript">

        var secondsLeft = 10;

        function startCountdown() {
            var timer = setInterval(function() {
                secondsLeft--;
                document.getElementById("countdown").innerHTML = secondsLeft;
                if (secondsLeft <= 0) {
                    clearInterval(timer);

                    window.location.href = "/legacy-app/home.do";
                }
            }, 1000);
        }

        $(document).ready(function() {
            startCountdown();
        });

        console.log("Unauthorized access: user=<%= user %>, role=<%= role %>");
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
                <td><span class="role-info">Session ID:</span></td>
                
                <td><font size="1"><%= session.getId() %></font></td>
            </tr>
        </table>
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
    </div>

    <p style="text-align:center; margin-top:15px; font-size:12px; color:#999;">
        Redirecting in <span id="countdown" style="font-weight:bold; color:#cc0000;">10</span> seconds...
    </p>
</div>

<hr>
<div style="text-align:center; font-size:10px; color:#999; padding:10px;">
    Copyright &copy; 2005 Example Bookstore Corp. All rights reserved.
</div>

</body>
</html>
