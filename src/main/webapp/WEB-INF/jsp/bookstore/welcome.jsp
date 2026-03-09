<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%
    // Get session data — business logic in JSP!
    String userName = (String) session.getAttribute("user");
    String userRole = (String) session.getAttribute("role");
    String loginTime = (String) session.getAttribute("loginTime");

    // Redirect if not logged in
    if (userName == null || userName.trim().length() == 0) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    // Inline date formatting (not using CommonUtil or DateUtil — yet another approach!)
    SimpleDateFormat welcomeSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    String currentTime = welcomeSdf.format(new Date());

    // Business logic: determine default page based on role (should be in Action!)
    String defaultUrl = "/legacy-app/home.do";
    String roleDescription = "User";
    if ("ADMIN".equals(userRole)) {
        defaultUrl = "/legacy-app/admin/home.do?method=home";
        roleDescription = "Administrator";
    } else if ("MANAGER".equals(userRole)) {
        defaultUrl = "/legacy-app/reports.do?method=menu";
        roleDescription = "Store Manager";
    } else if ("CLERK".equals(userRole)) {
        defaultUrl = "/legacy-app/sales/entry.do?method=entry";
        roleDescription = "Sales Clerk";
    }
%>
<html>
<head>
    <title>Welcome - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        .welcome-box {
            width: 650px;
            margin: 50px auto;
            padding: 30px;
            background-color: #f0f8ff;
            border: 2px solid #336699;
        }
        .welcome-title { font-size: 24px; color: #336699; font-weight: bold; text-align: center; margin-bottom: 20px; }
        .user-info { background: white; padding: 15px; border: 1px solid #ccc; margin: 15px 0; }
        .user-info td { padding: 5px; font-size: 12px; }
        .quick-link { display: inline-block; padding: 10px 20px; margin: 5px; background: #336699; color: white; text-decoration: none; font-weight: bold; font-size: 12px; }
        .quick-link:hover { background: #224477; }
        .redirect-msg { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
    </style>

    
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>

    
    <script type="text/javascript">

        var redirectUrl = "<%= defaultUrl %>";
        var secondsLeft = 5;

        function doCountdown() {
            document.getElementById("seconds").innerHTML = secondsLeft;
            secondsLeft--;
            if (secondsLeft < 0) {
                window.location.href = redirectUrl;
            } else {
                setTimeout(doCountdown, 1000);
            }
        }

        function cancelRedirect() {
            secondsLeft = -999;
            document.getElementById("redirectMsg").innerHTML =
                "<font color='green'>Auto-redirect cancelled.</font>";
        }

        $(document).ready(function() {
            doCountdown();
        });
    </script>
</head>
<body>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td width="30%"><font size="3"><b>Bookstore System</b></font></td>
    <td align="right">
        <font color="white">Welcome, <%= userName %>!</font>&nbsp;
        <a href="/legacy-app/logout.do?method=logout"><font color="white">Logout</font></a>
    </td>
</tr>
</table>

<div class="welcome-box">
    <div class="welcome-title">
        Welcome to Bookstore System!
    </div>

    
    <p style="font-size:14px;">Hello, <b><%= userName %></b>! You have logged in as <b><%= roleDescription %></b>.</p>

    
    <div class="user-info">
        <table width="100%" cellpadding="3">
            <tr>
                <td width="30%" style="font-weight:bold; color:#336699;">Username:</td>
                <td><%= userName %></td>
            </tr>
            <tr>
                <td style="font-weight:bold; color:#336699;">Role:</td>
                <td><%= roleDescription %> (<%= userRole %>)</td>
            </tr>
            <tr>
                <td style="font-weight:bold; color:#336699;">Login Time:</td>
                <td><%= loginTime != null ? loginTime : currentTime %></td>
            </tr>
            <tr>
                <td style="font-weight:bold; color:#336699;">Session ID:</td>
                
                <td><font size="1" color="#999"><%= session.getId() %></font></td>
            </tr>
        </table>
    </div>

    
    <%
        if ("ADMIN".equals(userRole)) {
    %>
        <p style="background:#e3f2fd; padding:12px; border-left:4px solid #2196F3; font-size:12px;">
            <b>Administrator Access:</b> Full system access including user management,
            system configuration, and all reporting functions.
        </p>
    <%
        } else if ("MANAGER".equals(userRole)) {
    %>
        <p style="background:#fff3e0; padding:12px; border-left:4px solid #ff9800; font-size:12px;">
            <b>Manager Access:</b> Sales reports, inventory management, purchase orders,
            and stock adjustments. Contact admin for user management access.
        </p>
    <%
        } else if ("CLERK".equals(userRole)) {
    %>
        <p style="background:#e8f5e9; padding:12px; border-left:4px solid #4caf50; font-size:12px;">
            <b>Clerk Access:</b> Sales transactions, book search, and basic inventory viewing.
            Contact your manager for report access.
        </p>
    <%
        }
    %>

    
    <div style="margin-top:20px;">
        <b style="font-size:12px;">Quick Links:</b><br>
        <a href="/legacy-app/home.do" class="quick-link">Dashboard</a>
        <%
            if ("ADMIN".equals(userRole)) {
        %>
            <a href="/legacy-app/admin/home.do?method=home" class="quick-link">Admin Panel</a>
            <a href="/legacy-app/admin/user/list.do?method=userList" class="quick-link">Users</a>
            <a href="/legacy-app/reports.do?method=menu" class="quick-link">Reports</a>
        <%
            } else if ("MANAGER".equals(userRole)) {
        %>
            <a href="/legacy-app/reports.do?method=menu" class="quick-link">Reports</a>
            <a href="/legacy-app/inventory/list.do?method=list" class="quick-link">Inventory</a>
        <%
            }
        %>
        <a href="/legacy-app/sales/entry.do?method=entry" class="quick-link">Sales</a>
        <a href="/legacy-app/book/search.do" class="quick-link">Book Search</a>
    </div>

    
    <div class="redirect-msg" id="redirectMsg">
        Redirecting to your default page in <span id="seconds" style="font-weight:bold; color:#336699;">5</span> seconds...
        <br>
        <a href="javascript:cancelRedirect();" style="font-size:11px;">Cancel auto-redirect</a>
    </div>
</div>

<hr>
<div style="text-align:center; font-size:10px; color:#999; padding:10px;">
    Copyright &copy; 2005-<%= new java.util.Date().getYear() + 1900 %> Example Bookstore Corp.
</div>

</body>
</html>
