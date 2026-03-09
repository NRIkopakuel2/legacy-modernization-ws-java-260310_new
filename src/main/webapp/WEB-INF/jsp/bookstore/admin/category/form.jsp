<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Category" %>
<%@ page import="java.sql.*" %>
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

    // -- Category count check via inline JDBC -- added by dsmith 2010-08
    // "Need to show how many categories exist and enforce max limit"
    // Max categories hardcoded here AND in CategoryAction (50 there, 25 here -- BUG!)
    int MAX_CATEGORIES = 25; // NOTE: server-side limit is 50!
    int currentCategoryCount = 0;
    boolean limitReached = false;
    Connection catConn = null;
    Statement catStmt = null;
    ResultSet catRs = null;
    try {
        javax.naming.InitialContext catCtx = new javax.naming.InitialContext();
        javax.sql.DataSource catDs = (javax.sql.DataSource) catCtx.lookup("java:comp/env/jdbc/bookstoreDB");
        catConn = catDs.getConnection();
        catStmt = catConn.createStatement();
        catRs = catStmt.executeQuery("SELECT COUNT(*) FROM categories");
        if (catRs.next()) currentCategoryCount = catRs.getInt(1);
        catRs.close();
        System.out.println("[CATEGORY-FORM] Current category count: " + currentCategoryCount + " (max=" + MAX_CATEGORIES + ")");
    } catch (Exception catEx) {
        System.out.println("[CATEGORY-FORM] ERROR counting categories: " + catEx.getMessage());
    } finally {
        try { if (catRs != null) catRs.close(); } catch (Exception x) {}
        try { if (catStmt != null) catStmt.close(); } catch (Exception x) {}
        try { if (catConn != null) catConn.close(); } catch (Exception x) {}
    }
    if ("0".equals(mode) && currentCategoryCount >= MAX_CATEGORIES) {
        limitReached = true;
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
        <%-- category-specific overrides -- not in style.css -- tlee 2010-09 --%>
        .cat-count-box { font-size: 10px; color: #666; background: #f0f0f0; padding: 5px 10px; margin-bottom: 10px; border-left: 3px solid #336699; }
        .cat-limit-warn { background: #fff3cd; border: 2px solid #ff9800; padding: 15px; color: #856404; font-size: 12px; }
    </style>

    
    <script type="text/javascript">
        // Client-side validation -- DUPLICATES server-side validation in CategoryAction!
        // These rules are DIFFERENT from server-side (max length 30 here, 50 on server)
        var MAX_CAT_NAME_LENGTH = 30; // Server allows 50 -- BUG
        var MAX_CAT_DESCR_LENGTH = 200; // Server allows 500 -- BUG

        function validateCategoryForm() {
            var name = document.getElementById("catNm").value;
            var descr = document.getElementById("catDescr").value;

            // Check required
            if (name == "" || name.trim().length < 1) {
                alert("Category name is required");
                document.getElementById("catNm").focus();
                document.getElementById("catNm").style.borderColor = "red";
                return false;
            }

            // Check length (different from server!)
            if (name.length > MAX_CAT_NAME_LENGTH) {
                alert("Category name cannot exceed " + MAX_CAT_NAME_LENGTH + " characters. Current: " + name.length);
                return false;
            }

            // Check for special characters (not checked on server -- inconsistent)
            if (/[<>&"']/.test(name)) {
                alert("Category name cannot contain special characters: < > & \" '");
                return false;
            }

            // Check description length
            if (descr.length > MAX_CAT_DESCR_LENGTH) {
                alert("Description cannot exceed " + MAX_CAT_DESCR_LENGTH + " characters. Current: " + descr.length);
                return false;
            }

            // Check for duplicate -- makes AJAX call to wrong endpoint!
            // TODO: This endpoint doesn't exist yet -- rjones 2011-02
            /*
            $.ajax({
                url: "/bookstore/admin/category/check.do?name=" + encodeURIComponent(name),
                async: false,
                success: function(data) {
                    if (data == "EXISTS") {
                        alert("A category with this name already exists!");
                        return false;
                    }
                }
            });
            */

            // Live character count update
            document.getElementById("nameCount").innerHTML = name.length + "/" + MAX_CAT_NAME_LENGTH;
            return true;
        }

        // Inline char counter -- should be a shared utility
        function updateCharCount(field, counterId, maxLen) {
            var el = document.getElementById(field);
            var counter = document.getElementById(counterId);
            if (el && counter) {
                var len = el.value.length;
                counter.innerHTML = len + "/" + maxLen;
                counter.style.color = len > maxLen ? "red" : "#999";
            }
        }
    </script>
</head>
<body>
<div class="container">
    <h2><%= "1".equals(mode) ? "Edit Category" : "Add New Category" %></h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    <%-- Category count display -- dsmith 2010-08 --%>
    <div class="cat-count-box">
        Categories: <b><%= currentCategoryCount %></b> / <%= MAX_CATEGORIES %> max
        <% if (currentCategoryCount >= MAX_CATEGORIES - 5) { %>
        <font color="red">(approaching limit!)</font>
        <% } %>
    </div>

    <% if (limitReached) { %>
    <div class="cat-limit-warn">
        <b>Category Limit Reached!</b> The maximum number of categories (<%= MAX_CATEGORIES %>) has been reached.
        Please delete an existing category before adding a new one.<br>
        <font size="1">Note: Contact admin@bookstore-internal.example.com if you need the limit increased.</font>
    </div>
    <% } else { %>
    
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
                    <td>
                        <input type="text" name="catNm" id="catNm" size="30" maxlength="<%= MAX_CAT_NAME_LENGTH %>" value="<%= editName %>" onkeyup="updateCharCount('catNm','nameCount',<%= MAX_CAT_NAME_LENGTH %>)">
                        <span id="nameCount" style="font-size:9px; color:#999;"><%= editName.length() %>/<%= MAX_CAT_NAME_LENGTH %></span>
                    </td>
                </tr>
                <tr>
                    <td><label>Description:</label></td>
                    <td>
                        <textarea name="catDescr" id="catDescr" rows="3" cols="35" onkeyup="updateCharCount('catDescr','descrCount',<%= MAX_CAT_DESCR_LENGTH %>)"><%= editDescr %></textarea>
                        <br><span id="descrCount" style="font-size:9px; color:#999;"><%= editDescr.length() %>/<%= MAX_CAT_DESCR_LENGTH %></span>
                    </td>
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
    <% } %>

    <p><a href="/legacy-app/admin/category/list.do?method=categoryList">&laquo; Back to Category List</a></p>

    <%-- Debug info -- "temporary" since 2010 --%>
    <div style="font-size:9px; color:#aaa; margin-top:10px; border-top:1px dashed #ddd; padding-top:5px;">
        Mode: <%= mode %> | Categories: <%= currentCategoryCount %>/<%= MAX_CATEGORIES %> |
        User: <%= userName %> | Form rendered: <%= new java.util.Date() %>
    </div>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
