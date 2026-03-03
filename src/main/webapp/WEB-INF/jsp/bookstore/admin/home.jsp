<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Auth + ADMIN role check — copy-pasted
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    <title>Admin Dashboard - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .admin-card {
            display: inline-block; width: 220px; padding: 20px; margin: 10px;
            background: white; border: 1px solid #ccc; border-top: 4px solid #9c27b0;
            vertical-align: top;
        }
        .admin-card h4 { color: #7b1fa2; margin: 0 0 10px 0; font-size: 14px; }
        .admin-card p { font-size: 11px; color: #666; margin-bottom: 15px; }
    </style>
</head>
<body>
<div class="container">
    <h2>Admin Dashboard</h2>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>
    <p style="font-size:12px; color:#666;">Welcome, Administrator <b><%= userName %></b>.</p>

    <div class="admin-card">
        <h4>&#128100; User Management</h4>
        <p>Create, edit, and manage user accounts and roles.</p>
        <a href="/legacy-app/admin/user/list.do?method=userList" class="btn">Manage Users</a>
    </div>

    <div class="admin-card">
        <h4>&#128218; Category Management</h4>
        <p>Create, edit, and delete book categories.</p>
        <a href="/legacy-app/admin/category/list.do?method=categoryList" class="btn">Manage Categories</a>
    </div>

    <div class="admin-card">
        <h4>&#128202; Reports</h4>
        <p>Access sales and inventory reports.</p>
        <a href="/legacy-app/reports.do?method=menu" class="btn">View Reports</a>
    </div>

    <div class="admin-card">
        <h4>&#128220; Audit Log</h4>
        <p>View system activity and user action history.</p>
        <a href="/legacy-app/audit/log.do" class="btn">View Audit Log</a>
    </div>

    <div style="margin-top:20px; padding:10px; background:#f3e5f5; border:1px solid #ce93d8; font-size:11px;">
        <b>System Info:</b> Session ID: <%= session.getId() %> | 
        Server Time: <%= new java.util.Date() %>
    </div>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
