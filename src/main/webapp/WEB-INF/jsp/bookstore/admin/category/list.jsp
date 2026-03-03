<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Category" %>
<%
    // Auth + ADMIN role check — COPY-PASTED from user/list.jsp!
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    // DIFFERENT from user/list.jsp: "categoryList" instead of "userList"
    List categoryList = (List) session.getAttribute("categoryList");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    
    <title>Category Management - Bookstore System</title>
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
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>
    <script type="text/javascript">
        function confirmDelete(catId) {
            if (confirm("Are you sure you want to delete this category?")) {
                window.location.href = "/legacy-app/admin/category/delete.do?method=categoryDelete&id=" + catId;
            }
        }
    </script>
</head>
<body>
<div class="container">
    
    <h2>Category Management</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    <div class="admin-toolbar">
        <a href="/legacy-app/admin/category/form.do?method=categoryForm" class="btn">+ Add New Category</a>
    </div>

    
    <% if (categoryList != null && categoryList.size() > 0) { %>
    <table class="admin-table">
        <tr>
            <th>#</th>
            
            <th>Name</th>
            <th>Description</th>
            <th>Created</th>
            <th>Actions</th>
        </tr>
        <%
            // COPY-PASTED loop structure from user/list.jsp!
            for (int i = 0; i < categoryList.size(); i++) {
                Object catObj = categoryList.get(i);
                String cId = "", cName = "", cDescr = "", cCrtDt = "";
                if (catObj instanceof Category) {
                    Category cat = (Category) catObj;
                    cId = cat.getId() != null ? cat.getId().toString() : "";
                    cName = cat.getCatNm() != null ? cat.getCatNm() : "";
                    cDescr = cat.getDescr() != null ? cat.getDescr() : "";
                    cCrtDt = cat.getCrtDt() != null ? cat.getCrtDt() : "";
                }
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            <td><%= cName %></td>
            <td><%= cDescr %></td>
            <td><%= cCrtDt %></td>
            <td>
                
                <a href="/legacy-app/admin/category/form.do?method=categoryForm&id=<%= cId %>" class="action-link">Edit</a>
                <a href="javascript:confirmDelete('<%= cId %>')" class="action-link" style="color:red;">Delete</a>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    
    <p style="font-size:10px; color:#999;">Total: <%= categoryList.size() %> categories</p>
    <% } else { %>
        <p>No categories found.</p>
    <% } %>

    <p><a href="/legacy-app/admin/home.do?method=home">&laquo; Back to Admin</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
