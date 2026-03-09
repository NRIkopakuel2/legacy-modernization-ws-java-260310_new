<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.User" %>
<%
    // Auth + ADMIN role check — COPY-PASTED
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    String mode = (String) request.getAttribute("mode");
    if (mode == null) mode = "0"; // 0=add, 1=edit
    Object editUser = request.getAttribute("editUser");
    String errMsg = (String) request.getAttribute("err");

    // Extract user fields for edit mode
    String editId = "", editName = "", editRole = "", editActive = "1";
    if (editUser != null && editUser instanceof User) {
        User eu = (User) editUser;
        editId = eu.getId() != null ? eu.getId().toString() : "";
        editName = eu.getUsrNm() != null ? eu.getUsrNm() : "";
        editRole = eu.getRole() != null ? eu.getRole() : "";
        editActive = eu.getActiveFlg() != null ? eu.getActiveFlg() : "1";
    }
%>
<html>
<head>
    <title><%= "1".equals(mode) ? "Edit" : "Add" %> User - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .admin-form { background: #fafafa; border: 1px solid #ccc; padding: 20px; max-width: 500px; }
        .admin-form td { padding: 6px; }
        .admin-form label { font-weight: bold; font-size: 12px; display: inline-block; width: 120px; }
        .admin-form input[type=text], .admin-form input[type=password], .admin-form select { padding: 5px; border: 1px solid #999; font-size: 11px; }
        .admin-form textarea { padding: 5px; border: 1px solid #999; font-size: 11px; }
    </style>

    <script type="text/javascript">
        function validateUserForm() {
            var name = document.getElementById("usrNm").value;
            if (name == "" || name.trim().length < 2) {
                alert("Username is required (at least 2 characters)");
                return false;
            }

            var roleSelect = document.forms[0].role;
            if (roleSelect && roleSelect.value == "GUEST") {
                alert("Role 'GUEST' is not allowed for staff accounts");
                return false;
            }
            <% if ("0".equals(mode)) { %>
            var pwd = document.getElementById("password").value;
            var cpwd = document.getElementById("confirmPwd").value;
            if (pwd == "" || pwd.length < 4) {
                alert("Password is required (at least 4 characters)");
                return false;
            }
            if (pwd != cpwd) {
                alert("Passwords do not match");
                return false;
            }
            <% } %>
            return true;
        }
    </script>
</head>
<body>
<div class="container">
    <h2><%= "1".equals(mode) ? "Edit User" : "Add New User" %></h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    <div class="admin-form">
        <form action="/legacy-app/admin/user/save.do" method="post" onsubmit="return validateUserForm();">
            <input type="hidden" name="method" value="userSave">
            <input type="hidden" name="entityType" value="user">
            <input type="hidden" name="mode" value="<%= mode %>">
            <% if ("1".equals(mode)) { %>
                <input type="hidden" name="userId" value="<%= editId %>">
            <% } %>

            <table>
                <tr>
                    <td><label>Username *:</label></td>
                    <td><input type="text" name="usrNm" id="usrNm" size="20" value="<%= editName %>"></td>
                </tr>
                <tr>
                    <td><label>Password<%= "0".equals(mode) ? " *" : "" %>:</label></td>
                    <td><input type="password" name="password" id="password" size="20"></td>
                </tr>
                <% if ("0".equals(mode)) { %>
                <tr>
                    <td><label>Confirm Pwd *:</label></td>
                    <td><input type="password" name="confirmPwd" id="confirmPwd" size="20"></td>
                </tr>
                <% } %>
                <tr>
                    <td><label>Role *:</label></td>
                    <td>
                        <select name="role">
                            <option value="CLERK" <%= "CLERK".equals(editRole) ? "selected" : "" %>>Clerk</option>
                            <option value="MANAGER" <%= "MANAGER".equals(editRole) ? "selected" : "" %>>Manager</option>
                            <option value="ADMIN" <%= "ADMIN".equals(editRole) ? "selected" : "" %>>Admin</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label>Active:</label></td>
                    <td>
                        <select name="activeFlg">
                            <option value="1" <%= "1".equals(editActive) ? "selected" : "" %>>Active</option>
                            <option value="0" <%= "0".equals(editActive) ? "selected" : "" %>>Inactive</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="right" style="padding-top:10px;">
                        <a href="/legacy-app/admin/user/list.do?method=userList" class="btn btn-secondary">Cancel</a>
                        &nbsp;
                        <input type="submit" value="Save User" class="btn">
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <p><a href="/legacy-app/admin/user/list.do?method=userList">&laquo; Back to User List</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
