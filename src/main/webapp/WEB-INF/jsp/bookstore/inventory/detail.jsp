<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%@ page import="com.example.bookstore.model.StockTransaction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    Object bookObj = session.getAttribute("book");
    List transactions = (List) session.getAttribute("transactions");
    String errMsg = (String) request.getAttribute("err");

    // Inline date formatting — yet ANOTHER format (not CommonUtil, not DateUtil)
    SimpleDateFormat detailDateFmt = new SimpleDateFormat("MM/dd HH:mm");
%>
<html>
<head>
    <title>Stock Detail - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .detail-box { background: #fafafa; border: 1px solid #ccc; padding: 15px; margin-bottom: 15px; }
        .detail-table td { padding: 5px; font-size: 12px; }
        .detail-table td:first-child { font-weight: bold; color: #336699; width: 150px; }
        .txn-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .txn-table th { background: #455a64; color: white; padding: 5px; font-size: 10px; }
        .txn-table td { padding: 4px; font-size: 10px; border-bottom: 1px solid #eee; }
        .txn-increase { color: green; font-weight: bold; }
        .txn-decrease { color: red; font-weight: bold; }
    </style>
</head>
<body>
<div class="container">
    <h2>Stock Detail</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    <% if (bookObj != null) {
        // MIXED: use bean:write for some fields, expression for others!
        Book book = (Book) bookObj;
        String stockClass = "stock-ok";
        int stockNum = 0;
        try { stockNum = Integer.parseInt(book.getQtyInStock()); } catch (Exception e) {}
        if (stockNum <= 0) stockClass = "stock-out";
        else if (stockNum <= 10) stockClass = "stock-low";
    %>
    <div class="detail-box">
        <table class="detail-table" width="100%">
            <tr><td>ISBN:</td><td><bean:write name="book" property="isbn" scope="session"/></td></tr>
            <tr><td>Title:</td><td><%= book.getTitle() %></td></tr>
            <tr><td>Publisher:</td><td><bean:write name="book" property="publisher" scope="session"/></td></tr>
            <tr><td>Price:</td><td>$<%= book.getListPrice() %></td></tr>
            <tr><td>Stock:</td><td class="<%= stockClass %>" style="font-size:18px;"><%= book.getQtyInStock() %></td></tr>
            <tr><td>Status:</td><td><%= book.getStatus() %></td></tr>
        </table>

        <% if ("MANAGER".equals(role) || "ADMIN".equals(role)) { %>
            <div style="margin-top:10px;">
                <a href="/legacy-app/inventory/adjust.do?method=adjustStock&bookId=<%= book.getId() %>" class="btn">Adjust Stock</a>
            </div>
        <% } %>
    </div>

    
    <h3>Recent Stock Transactions</h3>
    <% if (transactions != null && transactions.size() > 0) { %>
    <table class="txn-table">
        <tr><th>Date</th><th>Type</th><th>Change</th><th>After</th><th>User</th><th>Reason</th></tr>
        <%
            for (int t = 0; t < transactions.size(); t++) {
                Object txnObj = transactions.get(t);
                String tType = "", tChange = "", tAfter = "", tUser = "", tReason = "", tDate = "";
                if (txnObj instanceof StockTransaction) {
                    StockTransaction txn = (StockTransaction) txnObj;
                    tType = txn.getTxnType() != null ? txn.getTxnType() : "";
                    tChange = txn.getQtyChange() != null ? txn.getQtyChange() : "0";
                    tAfter = txn.getQtyAfter() != null ? txn.getQtyAfter() : "";
                    tUser = txn.getUserId() != null ? txn.getUserId() : "";
                    tReason = txn.getReason() != null ? txn.getReason() : "";
                    tDate = txn.getCrtDt() != null ? txn.getCrtDt() : "";
                }
                String changeClass = "";
                try {
                    int c = Integer.parseInt(tChange);
                    changeClass = c >= 0 ? "txn-increase" : "txn-decrease";
                } catch (Exception e) {}
        %>
        <tr>
            <td><%= tDate %></td>
            <td><%= tType %></td>
            <td class="<%= changeClass %>"><%= tChange %></td>
            <td><%= tAfter %></td>
            <td><%= tUser %></td>
            <td><%= tReason %></td>
        </tr>
        <% } %>
    </table>
    <% } else { %>
        <p style="color:#999; font-size:12px;">No stock transactions found.</p>
    <% } %>
    <% } else { %>
        <p class="err">Book not found.</p>
    <% } %>

    <p><a href="/legacy-app/inventory/list.do?method=list">&laquo; Back to Inventory</a>
       | <a href="/legacy-app/inventory/ledger.do?method=ledger&bookId=<%= bookObj instanceof Book ? ((Book)bookObj).getId() : "" %>">Full Ledger</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
