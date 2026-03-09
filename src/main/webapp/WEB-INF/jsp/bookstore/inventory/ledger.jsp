<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat, java.text.DecimalFormat" %>
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
    // And ANOTHER format for the summary section -- dsmith 2011-02
    SimpleDateFormat summaryFmt = new SimpleDateFormat("MM/dd/yyyy");

    // Hardcoded number formatting -- not locale-aware -- KL 2010-12
    DecimalFormat numFmt = new DecimalFormat("#,##0");
    DecimalFormat pctFmt = new DecimalFormat("0.0%");

    System.out.println("[LEDGER] Loaded for user=" + userName + " at " + new Date());
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
        <%-- Ledger summary styles -- inline because "it's just one page" -- tlee 2011-03 --%>
        .ledger-summary { background: #e8eaf6; border: 1px solid #7986cb; padding: 10px; margin: 10px 0; font-size: 11px; }
        .ledger-summary td { padding: 3px 10px; }
        .ledger-summary .label { font-weight: bold; color: #283593; width: 180px; }
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
    <%
        // -- Inline calculation of totals -- should be in Action/Service -- dsmith 2011-01
        // "The Action doesn't calculate these so I did it here" -- BUG-7234
        int totalAdded = 0;
        int totalRemoved = 0;
        int txnCount = 0;
        String earliestDate = "";
        String latestDate = "";
        HashMap typeCounts = new HashMap(); // count by transaction type

        for (int c = 0; c < transactions.size(); c++) {
            Object calcObj = transactions.get(c);
            if (calcObj instanceof StockTransaction) {
                StockTransaction calcTxn = (StockTransaction) calcObj;
                txnCount++;
                try {
                    int qty = Integer.parseInt(calcTxn.getQtyChange() != null ? calcTxn.getQtyChange() : "0");
                    if (qty >= 0) totalAdded += qty;
                    else totalRemoved += Math.abs(qty);
                } catch (NumberFormatException nfe) {
                    System.out.println("[LEDGER] WARNING: Invalid qty for txn index " + c + ": " + calcTxn.getQtyChange());
                }
                // Track type counts
                String txType = calcTxn.getTxnType() != null ? calcTxn.getTxnType() : "UNKNOWN";
                Integer tc = (Integer) typeCounts.get(txType);
                if (tc == null) tc = new Integer(0);
                typeCounts.put(txType, new Integer(tc.intValue() + 1));
                // Track date range
                if (calcTxn.getCrtDt() != null) {
                    if (earliestDate.length() == 0 || calcTxn.getCrtDt().compareTo(earliestDate) < 0) earliestDate = calcTxn.getCrtDt();
                    if (latestDate.length() == 0 || calcTxn.getCrtDt().compareTo(latestDate) > 0) latestDate = calcTxn.getCrtDt();
                }
            }
        }
        int netChange = totalAdded - totalRemoved;
        System.out.println("[LEDGER] Summary: added=" + totalAdded + " removed=" + totalRemoved + " net=" + netChange + " txns=" + txnCount);
    %>

    <%-- Inline summary section -- dsmith 2011-01 --%>
    <div class="ledger-summary">
        <b>Ledger Summary</b> (calculated in JSP -- not cached!)
        <table>
            <tr><td class="label">Total Transactions:</td><td><%= numFmt.format(txnCount) %></td></tr>
            <tr><td class="label">Units Added (+):</td><td style="color:#2e7d32;"><b>+<%= numFmt.format(totalAdded) %></b></td></tr>
            <tr><td class="label">Units Removed (-):</td><td style="color:#c62828;"><b>-<%= numFmt.format(totalRemoved) %></b></td></tr>
            <tr><td class="label">Net Change:</td><td><b><%= netChange >= 0 ? "+" : "" %><%= numFmt.format(netChange) %></b></td></tr>
            <% if (txnCount > 0) { %>
            <tr><td class="label">Avg Change/Transaction:</td><td><%= new DecimalFormat("0.00").format((double)netChange / txnCount) %></td></tr>
            <% } %>
            <tr><td class="label">Date Range:</td><td><%= earliestDate %> &mdash; <%= latestDate %></td></tr>
            <tr><td class="label">Types:</td><td>
                <% Iterator typeIt = typeCounts.entrySet().iterator();
                   while (typeIt.hasNext()) {
                       Map.Entry entry = (Map.Entry) typeIt.next(); %>
                    <%= entry.getKey() %>: <%= entry.getValue() %><%= typeIt.hasNext() ? ", " : "" %>
                <% } %>
            </td></tr>
        </table>
    </div>

    <table class="ledger-table">
        <tr>
            <th>#</th><th>Date/Time</th><th>Type</th><th>Change</th><th>After</th><th>User</th><th>Reason</th><th>Notes</th><th>Ref</th><th>Running +/-</th>
        </tr>
        <%
            int runningPlus = 0;
            int runningMinus = 0;
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
                int changeInt = 0;
                try {
                    changeInt = Integer.parseInt(tChange);
                    changeClass = changeInt >= 0 ? "qty-plus" : "qty-minus";
                    if (changeInt >= 0) runningPlus += changeInt;
                    else runningMinus += Math.abs(changeInt);
                } catch (Exception e) {}

                // Debug: print every row to stdout -- "helpful for reconciliation" -- dsmith
                System.out.println("[LEDGER] Row " + (i+1) + ": type=" + tType + " change=" + tChange + " after=" + tAfter + " user=" + tUser + " date=" + tDate);
        %>
        <tr<%= (i % 2 == 1) ? " style=\"background:#fafafa;\"" : "" %>>
            <td><%= i + 1 %></td>
            <td><%= tDate %></td>
            <td><%= tType %></td>
            <td class="<%= changeClass %>"><%= changeInt >= 0 ? "+" : "" %><%= numFmt.format(changeInt) %></td>
            <td><%= tAfter %></td>
            <td><%= tUser %></td>
            <td><%= tReason %></td>
            <td><%= tNotes %></td>
            <td><%= tRef %></td>
            <td style="font-size:9px; color:#999;">+<%= numFmt.format(runningPlus) %>/-<%= numFmt.format(runningMinus) %></td>
        </tr>
        <% } %>
        <%-- Totals row -- calculated inline --%>
        <tr style="background:#37474f; color:white; font-weight:bold;">
            <td colspan="3" align="right">TOTALS:</td>
            <td>+<%= numFmt.format(totalAdded) %> / -<%= numFmt.format(totalRemoved) %></td>
            <td colspan="5">&nbsp;</td>
            <td style="font-size:9px;">Net: <%= netChange >= 0 ? "+" : "" %><%= numFmt.format(netChange) %></td>
        </tr>
    </table>
    <p style="font-size:10px; color:#999;">Total: <%= numFmt.format(transactions.size()) %> transactions | Generated: <%= new Date() %></p>
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
