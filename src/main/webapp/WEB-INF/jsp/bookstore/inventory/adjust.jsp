<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    // Hard-coded role check in JSP (should be in Action!)
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
        response.sendRedirect("/legacy-app/home.do");
        return;
    }

    Object bookObj = request.getAttribute("book");
    String maxAdj = (String) request.getAttribute("maxAdjustment");
    if (maxAdj == null) maxAdj = "100";
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    <title>Stock Adjustment - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .adjust-form { background: #fff3e0; border: 1px solid #ff9800; padding: 20px; max-width: 500px; }
        .adjust-form td { padding: 6px; }
        .adjust-form label { font-weight: bold; font-size: 12px; display: inline-block; width: 100px; }
        .field-error { color: red; font-size: 10px; }
    </style>
    
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/common.js"></script>

    
    <script type="text/javascript">
        function validateAdjustment() {
            var bookId = document.getElementById("bookId").value;
            var qty = document.getElementById("qty").value;
            var adjType = document.getElementById("adjType").value;
            var reason = document.getElementById("reason").value;

            if (bookId == "") { alert("Book ID is required"); return false; }
            if (adjType == "") { alert("Select adjustment type"); return false; }

            if (qty == "" || parseInt(qty) <= 0 || parseInt(qty) > 9999) {
                alert("Quantity must be between 1 and 9999");
                return false;
            }

            if (reason == "") { alert("Reason is required"); return false; }
            if (reason == "OTHER") {
                var notes = document.getElementById("notes").value;
                if (notes == "") { alert("Notes required for 'Other' reason"); return false; }
            }

            return confirm("Are you sure you want to adjust stock?");
        }
    </script>
</head>
<body>
<div class="container">
    <h2>Stock Adjustment</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    <% if (bookObj != null && bookObj instanceof Book) {
        Book book = (Book) bookObj;
    %>
    <p>Book: <b><%= book.getTitle() %></b> (ISBN: <%= book.getIsbn() %>) — Current Stock: <b><%= book.getQtyInStock() %></b></p>
    <% } %>

    <div class="adjust-form">
        <form action="/legacy-app/inventory/adjust.do" method="post" onsubmit="return validateAdjustment();">
            <input type="hidden" name="method" value="adjustStock">
            <input type="hidden" name="_method" value="POST">
            <table>
                <tr>
                    <td><label>Book ID *:</label></td>
                    <td><input type="text" name="bookId" id="bookId" size="10"
                               value="<%= bookObj instanceof Book ? ((Book)bookObj).getId() : "" %>"></td>
                </tr>
                <tr>
                    <td><label>Type *:</label></td>
                    <td>
                        <select name="adjType" id="adjType">
                            <option value="">-- Select --</option>
                            <option value="INCREASE">Increase</option>
                            <option value="DECREASE">Decrease</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label>Quantity *:</label></td>
                    <td>
                        <input type="text" name="qty" id="qty" size="5">
                        <font size="1" color="#999">(max: <%= maxAdj %>)</font>
                    </td>
                </tr>
                <tr>
                    <td><label>Reason *:</label></td>
                    <td>
                        <select name="reason" id="reason">
                            <option value="">-- Select --</option>
                            <option value="CORRECTION">Correction</option>
                            <option value="DAMAGE">Damage</option>
                            <option value="THEFT">Theft</option>
                            <option value="LOSS">Loss</option>
                            <option value="FOUND">Found</option>
                            <option value="SAMPLE">Sample</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label>Notes:</label></td>
                    <td><textarea name="notes" id="notes" rows="3" cols="30"></textarea></td>
                </tr>
                <tr>
                    <td colspan="2" align="right">
                        <input type="submit" value="Adjust Stock" class="btn">
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <p><a href="/legacy-app/inventory/list.do?method=list">&laquo; Back to Inventory</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
