<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Category" %>
<%
    // Auth + ADMIN role check — COPY-PASTED from user/form.jsp!
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    // COPY-PASTED mode handling from user/form.jsp
    String mode = (String) request.getAttribute("mode");
    if (mode == null) mode = "0";
    Object editCategory = request.getAttribute("editCategory");
    String errMsg = (String) request.getAttribute("err");

    // DIFFERENT: Category fields instead of User fields
    String editId = "", editName = "", editDescr = "";
    if (editCategory != null && editCategory instanceof Category) {
        Category ec = (Category) editCategory;
        editId = ec.getId() != null ? ec.getId().toString() : "";
        editName = ec.getCatNm() != null ? ec.getCatNm() : "";
        editDescr = ec.getDescr() != null ? ec.getDescr() : "";
    }
%>
<html>
<head>
    
    <title><%= "1".equals(mode) ? "Edit" : "Add" %> Category - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .admin-form { background: #fafafa; border: 1px solid #ccc; padding: 20px; max-width: 500px; }
        .admin-form td { padding: 6px; }
        .admin-form label { font-weight: bold; font-size: 12px; display: inline-block; width: 120px; }
        .admin-form input[type=text], .admin-form input[type=password], .admin-form select { padding: 5px; border: 1px solid #999; font-size: 11px; }
        .admin-form textarea { padding: 5px; border: 1px solid #999; font-size: 11px; }
    </style>

    
    <script type="text/javascript">
        function validateCategoryForm() {
            var name = document.getElementById("catNm").value;
            if (name == "" || name.trim().length < 1) {
                alert("Category name is required");
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
<div class="container">
    <h2><%= "1".equals(mode) ? "Edit Category" : "Add New Category" %></h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    
    <div class="admin-form">
        <form action="/legacy-app/admin/category/save.do" method="post" onsubmit="return validateCategoryForm();">
            <input type="hidden" name="method" value="categorySave">
            <input type="hidden" name="entityType" value="category">
            <input type="hidden" name="mode" value="<%= mode %>">
            <% if ("1".equals(mode)) { %>
                <input type="hidden" name="categoryId" value="<%= editId %>">
            <% } %>

            <table>
                
                <tr>
                    <td><label>Name *:</label></td>
                    <td><input type="text" name="catNm" id="catNm" size="30" value="<%= editName %>"></td>
                </tr>
                <tr>
                    <td><label>Description:</label></td>
                    <td><textarea name="catDescr" rows="3" cols="35"><%= editDescr %></textarea></td>
                </tr>
                
                <tr>
                    <td colspan="2" align="right" style="padding-top:10px;">
                        <a href="/legacy-app/admin/category/list.do?method=categoryList" class="btn btn-secondary">Cancel</a>
                        &nbsp;
                        <input type="submit" value="Save Category" class="btn">
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <p><a href="/legacy-app/admin/category/list.do?method=categoryList">&laquo; Back to Category List</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
