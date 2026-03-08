<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.User" %>
<%
    // Auth + ADMIN role check — copy-pasted in every admin page
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    List userList = (List) session.getAttribute("userList");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    <title>User Management - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .admin-table { width: 100%; border-collapse: collapse; }
        .admin-table th { background: #7b1fa2; color: white; padding: 6px 8px; font-size: 11px; text-align: left; }
        .admin-table td { padding: 5px 8px; font-size: 11px; border-bottom: 1px solid #e0e0e0; }
        .admin-table tr:hover { background-color: #f3e5f5; }
        .admin-table tr.even { background-color: #fafafa; }
        .status-active { color: green; font-weight: bold; }
        .status-inactive { color: red; font-weight: bold; }
        .action-link { font-size: 10px; margin-right: 5px; }
        .admin-toolbar { margin-bottom: 10px; }
    </style>

    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/common.js"></script>
    <script type="text/javascript">
        function confirmToggle(userId, currentStatus) {
            var action = currentStatus == "1" ? "deactivate" : "activate";
            if (confirm("Are you sure you want to " + action + " this user?")) {
                window.location.href = "/legacy-app/admin/user/toggleActive.do?method=userToggleActive&id=" + userId;
            }
        }
    </script>
</head>
<body>
<div class="container">
    <h2>User Management</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    <div class="admin-toolbar">
        <a href="/legacy-app/admin/user/form.do?method=userForm" class="btn">+ Add New User</a>
    </div>

    <% if (userList != null && userList.size() > 0) { %>
    <table class="admin-table">
        <tr>
            <th>#</th>
            <th>Username</th>
            <th>Role</th>
            <th>Active</th>
            <th>Created</th>
            <th>Actions</th>
        </tr>
        <%
            for (int i = 0; i < userList.size(); i++) {
                Object userObj = userList.get(i);
                String uId = "", uName = "", uRole = "", uActive = "", uCrtDt = "";
                if (userObj instanceof User) {
                    User u = (User) userObj;
                    uId = u.getId() != null ? u.getId().toString() : "";
                    uName = u.getUsrNm() != null ? u.getUsrNm() : "";
                    uRole = u.getRole() != null ? u.getRole() : "";
                    uActive = u.getActiveFlg() != null ? u.getActiveFlg() : "0";
                    uCrtDt = u.getCrtDt() != null ? u.getCrtDt() : "";
                }
                String statusClass = "1".equals(uActive) ? "status-active" : "status-inactive";
                String statusText = "1".equals(uActive) ? "Active" : "Inactive";
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            
            <td><%= uName %></td>
            <td><%= uRole %></td>
            <td class="<%= statusClass %>"><%= statusText %></td>
            <td><%= uCrtDt %></td>
            <td>
                <a href="/legacy-app/admin/user/form.do?method=userForm&id=<%= uId %>" class="action-link">Edit</a>
                <a href="javascript:confirmToggle('<%= uId %>', '<%= uActive %>')" class="action-link">
                    <%= "1".equals(uActive) ? "Deactivate" : "Activate" %>
                </a>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <p style="font-size:10px; color:#999;">Total: <%= userList.size() %> users</p>
    <% } else { %>
        <p>No users found.</p>
    <% } %>

    <p><a href="/legacy-app/admin/home.do?method=home">&laquo; Back to Admin</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
