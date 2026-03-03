<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%@ page import="com.example.bookstore.model.StockTransaction" %>
<%
    String userName = (String) session.getAttribute("user");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    Object bookObj = request.getAttribute("book");
    List transactions = (List) session.getAttribute("transactions");
    String errMsg = (String) request.getAttribute("err");

    // Yet ANOTHER date format pattern (not CommonUtil, not DateUtil, not detail.jsp's format!)
    SimpleDateFormat ledgerFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>
<html>
<head>
    <title>Stock Ledger - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .ledger-table { width: 100%; border-collapse: collapse; }
        .ledger-table th { background: #37474f; color: white; padding: 5px 8px; font-size: 10px; }
        .ledger-table td { padding: 4px 8px; font-size: 10px; border-bottom: 1px solid #eee; }
        .qty-plus { color: #2e7d32; font-weight: bold; }
        .qty-minus { color: #c62828; font-weight: bold; }
    </style>
</head>
<body>
<div class="container">
    <h2>Stock Ledger</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    <% if (bookObj != null && bookObj instanceof Book) {
        Book book = (Book) bookObj; %>
    <p>Book: <b><%= book.getTitle() %></b> | ISBN: <%= book.getIsbn() %> | Current Stock: <b><%= book.getQtyInStock() %></b></p>
    <% } %>

    <% if (transactions != null && transactions.size() > 0) { %>
    <table class="ledger-table">
        <tr>
            <th>#</th><th>Date/Time</th><th>Type</th><th>Change</th><th>After</th><th>User</th><th>Reason</th><th>Notes</th><th>Ref</th>
        </tr>
        <%
            for (int i = 0; i < transactions.size(); i++) {
                Object txnObj = transactions.get(i);
                String tDate = "", tType = "", tChange = "0", tAfter = "", tUser = "", tReason = "", tNotes = "", tRef = "";
                if (txnObj instanceof StockTransaction) {
                    StockTransaction txn = (StockTransaction) txnObj;
                    // Inline date formatting with DIFFERENT pattern than detail.jsp!
                    if (txn.getCrtDt() != null) {
                        try {
                            // Try to reformat — but CrtDt is stored as String, not Date!
                            tDate = txn.getCrtDt();
                        } catch (Exception e) {
                            tDate = txn.getCrtDt();
                        }
                    }
                    tType = txn.getTxnType() != null ? txn.getTxnType() : "";
                    tChange = txn.getQtyChange() != null ? txn.getQtyChange() : "0";
                    tAfter = txn.getQtyAfter() != null ? txn.getQtyAfter() : "";
                    tUser = txn.getUserId() != null ? txn.getUserId() : "";
                    tReason = txn.getReason() != null ? txn.getReason() : "";
                    tNotes = txn.getNotes() != null ? txn.getNotes() : "";
                    tRef = (txn.getRefType() != null ? txn.getRefType() : "") + (txn.getRefId() != null ? "#" + txn.getRefId() : "");
                }
                String changeClass = "";
                try { changeClass = Integer.parseInt(tChange) >= 0 ? "qty-plus" : "qty-minus"; } catch (Exception e) {}
        %>
        <tr>
            <td><%= i + 1 %></td>
            <td><%= tDate %></td>
            <td><%= tType %></td>
            <td class="<%= changeClass %>"><%= Integer.parseInt(tChange) >= 0 ? "+" : "" %><%= tChange %></td>
            <td><%= tAfter %></td>
            <td><%= tUser %></td>
            <td><%= tReason %></td>
            <td><%= tNotes %></td>
            <td><%= tRef %></td>
        </tr>
        <% } %>
    </table>
    <p style="font-size:10px; color:#999;">Total: <%= transactions.size() %> transactions</p>
    <% } else { %>
        <p style="color:#999;">No stock transactions found for this book.</p>
    <% } %>

    <p><a href="/legacy-app/inventory/list.do?method=list">&laquo; Back to Inventory</a>
       <% if (bookObj != null && bookObj instanceof Book) { %>
       | <a href="/legacy-app/inventory/detail.do?method=detail&bookId=<%= ((Book)bookObj).getId() %>">Back to Detail</a>
       <% } %>
    </p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
