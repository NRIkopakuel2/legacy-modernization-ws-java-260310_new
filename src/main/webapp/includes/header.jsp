<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Get user info from session (no null check - NPE risk)
    String userName = (String) session.getAttribute("user");
    String userRole = (String) session.getAttribute("role");
%>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

<script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>

<script type="text/javascript">

    var APP_CONTEXT = "/legacy-app";
    var currentUser = '<%= userName %>';
    var currentRole = '<%= userRole %>';

    $(document).ready(function() {
        $(document).ajaxError(function() {
            alert("System error occurred. Please try again.");
        });
    });
</script>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td width="30%">
        <font size="3"><b>Bookstore System</b></font>
    </td>
    <td align="center">
        <a href="/legacy-app/home.do">Home</a>
        <a href="/legacy-app/sales/entry.do">Sales</a>
        <a href="/legacy-app/book/search.do">Books</a>
        <a href="/legacy-app/inventory/list.do">Inventory</a>
<% if ("MANAGER".equals(userRole) || "ADMIN".equals(userRole)) { %>
        <a href="/legacy-app/reports.do">Reports</a>
<% } %>
<% if ("ADMIN".equals(userRole)) { %>
        <a href="/legacy-app/admin/home.do">Admin</a>
<% } %>
    </td>
    <td width="25%" align="right">
        <font color="white">User: <%= userName %> [<%= userRole %>]</font>
        &nbsp;
        <a href="/legacy-app/logout.do?method=logout">Logout</a>
    </td>
</tr>
</table>
<br>
