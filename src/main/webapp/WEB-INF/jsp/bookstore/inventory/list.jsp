<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%
    // Auth check — copy-pasted in every page
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    List books = (List) session.getAttribute("books");
    String lowStockCount = (String) session.getAttribute("lowStockCount");
    String criticalCount = (String) session.getAttribute("criticalCount");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    <title>Inventory List - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .inv-summary { background: #e3f2fd; padding: 10px; border: 1px solid #90caf9; margin-bottom: 10px; }
        .inv-summary span { margin-right: 20px; font-size: 12px; }
        .inv-table { width: 100%; border-collapse: collapse; }
        .inv-table th { background: #336699; color: white; padding: 6px 8px; font-size: 11px; text-align: left; border: 1px solid #224477; }
        .inv-table td { padding: 4px 8px; font-size: 11px; border: 1px solid #ddd; }
        .inv-table tr:hover { background-color: #f0f4f8; }
        .inv-table tr.even { background-color: #f9f9f9; }
        .stock-ok { color: #2e7d32; }
        .stock-low { color: #f57f17; font-weight: bold; }
        .stock-critical { color: #d32f2f; font-weight: bold; }
        .stock-out { color: #b71c1c; font-weight: bold; font-size: 10px; }
        .filter-bar { background: #f5f5f5; padding: 8px; border: 1px solid #ddd; margin-bottom: 10px; }
    </style>
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>
</head>
<body>
<div class="container">
    <h2>Inventory List</h2>

    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    <div class="inv-summary">
        <span>Total Books: <b><%= books != null ? books.size() : 0 %></b></span>
        <span>Low Stock: <b style="color:orange;"><%= lowStockCount != null ? lowStockCount : "?" %></b></span>
        <span>Critical: <b style="color:red;"><%= criticalCount != null ? criticalCount : "?" %></b></span>
        <span><a href="/legacy-app/inventory/lowstock.do?method=lowStock">View Low Stock &raquo;</a></span>
    </div>

    
    <div class="filter-bar">
        <form action="/legacy-app/inventory/list.do" method="get">
            <input type="hidden" name="method" value="list">
            Search: <input type="text" name="keyword" size="20" value="">
            <input type="submit" value="Filter" class="btn">
            <input type="button" value="Show All" class="btn btn-secondary"
                   onclick="window.location.href='/legacy-app/inventory/list.do?method=list'">
        </form>
    </div>

    
    <% if (books != null && books.size() > 0) { %>
    <table class="inv-table">
        <tr>
            <th>ISBN</th>
            <th>Title</th>
            <th>Category</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Status</th>
            <th>Action</th>
        </tr>
        <%
            for (int i = 0; i < books.size(); i++) {
                Object bookObj = books.get(i);
                String bId = "", bIsbn = "", bTitle = "", bPrice = "0.00", bStock = "0", bStatus = "", bCatId = "";

                if (bookObj instanceof Book) {
                    Book book = (Book) bookObj;
                    bId = book.getId() != null ? book.getId().toString() : "";
                    bIsbn = book.getIsbn() != null ? book.getIsbn() : "";
                    bTitle = book.getTitle() != null ? book.getTitle() : "";
                    bPrice = String.valueOf(book.getListPrice());
                    bStock = book.getQtyInStock() != null ? book.getQtyInStock() : "0";
                    bStatus = book.getStatus() != null ? book.getStatus() : "";
                    bCatId = book.getCategoryId() != null ? book.getCategoryId() : "";
                } else if (bookObj instanceof Map) {
                    Map m = (Map) bookObj;
                    bId = (String) m.get("id");
                    bIsbn = (String) m.get("isbn");
                    bTitle = (String) m.get("title");
                    bPrice = (String) m.get("listPrice");
                    bStock = (String) m.get("qtyInStock");
                    bStatus = (String) m.get("status");
                    bCatId = (String) m.get("categoryId");
                }

                // Business logic in JSP: determine stock CSS class
                String stockClass = "stock-ok";
                int stockNum = 0;
                try { stockNum = Integer.parseInt(bStock); } catch (Exception e) {}
                if (stockNum <= 0) stockClass = "stock-out";
                else if (stockNum <= 3) stockClass = "stock-critical";
                else if (stockNum <= 10) stockClass = "stock-low";
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= bIsbn %></td>
            <td><a href="/legacy-app/inventory/detail.do?method=detail&bookId=<%= bId %>"><%= bTitle %></a></td>
            <td><%= bCatId %></td>
            <td align="right">$<%= bPrice %></td>
            <td align="center" class="<%= stockClass %>"><%= bStock %></td>
            <td><%= bStatus %></td>
            <td>
                <a href="/legacy-app/inventory/detail.do?method=detail&bookId=<%= bId %>" style="font-size:10px;">Detail</a>
                <% if ("MANAGER".equals(role) || "ADMIN".equals(role)) { %>
                | <a href="/legacy-app/inventory/adjust.do?method=adjustStock&bookId=<%= bId %>" style="font-size:10px;">Adjust</a>
                <% } %>
                | <a href="/legacy-app/inventory/ledger.do?method=ledger&bookId=<%= bId %>" style="font-size:10px;">Ledger</a>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <p style="font-size:11px; color:#999;">Total: <%= books.size() %> books</p>
    <% } else { %>
        <p>No books found in inventory.</p>
    <% } %>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
