<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.PurchaseOrder" %>
<%
    // Auth check — COPY-PASTED from supplier/list.jsp!
    String userName = (String) session.getAttribute("user");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    // DIFFERENT from supplier/list.jsp: "purchaseOrders" instead of "suppliers"
    List purchaseOrders = (List) session.getAttribute("purchaseOrders");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    
    <title>Purchase Orders - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    
    <style>
        /* PO page inline overrides — duplicated and divergent from style.css */
        body { font-family: Verdana, Geneva, sans-serif; }
        .container { max-width: 980px; }
        h2 { color: #00695c; border-bottom: 2px solid #00695c; padding-bottom: 6px; }
        a { color: #00695c; }
        a:hover { color: #004d40; text-decoration: underline; }
        .btn { background-color: #00695c; color: white; border: 1px solid #004d40;
               padding: 4px 10px; font-size: 11px; cursor: pointer; }
        .btn:hover { background-color: #004d40; }
        .btn-secondary { background-color: #78909c; border-color: #546e7a; }
        .err { color: #c62828; background: #ffebee; border: 1px solid #ef9a9a;
               padding: 8px; margin-bottom: 10px; font-size: 11px; }
        .msg { color: #2e7d32; background: #e8f5e9; border: 1px solid #a5d6a7;
               padding: 8px; margin-bottom: 10px; font-size: 11px; }
        .page-total { font-size: 10px; color: #78909c; text-align: right; margin-top: 5px; }
        input[type=text], select { padding: 3px 5px; border: 1px solid #b0bec5; font-size: 11px; }
        input[type=text]:focus, select:focus { border-color: #00695c; outline: none; }
        .filter-row { background: #e0f2f1; padding: 8px; border: 1px solid #b2dfdb; margin-bottom: 10px; }
        .po-amount { font-family: 'Courier New', monospace; text-align: right; }
        .po-overdue { color: #c62828; font-weight: bold; }
    </style>
    <style>
        .proc-table { width: 100%; border-collapse: collapse; }
        .proc-table th { background: #00695c; color: white; padding: 6px 8px; font-size: 11px; text-align: left; }
        .proc-table td { padding: 5px 8px; font-size: 11px; border-bottom: 1px solid #e0e0e0; }
        .proc-table tr:hover { background-color: #e0f2f1; }
        .proc-table tr.even { background-color: #f5f5f5; }
        .status-badge { padding: 2px 6px; font-size: 10px; font-weight: bold; border-radius: 3px; }
        .badge-active { background: #c8e6c9; color: #2e7d32; }
        .badge-inactive { background: #ffcdd2; color: #c62828; }
        .proc-toolbar { margin-bottom: 10px; }
        .contact-info { font-size: 10px; color: #666; }
        /* Additional: PO-specific status badges */
        .badge-draft { background: #e0e0e0; color: #616161; }
        .badge-submitted { background: #bbdefb; color: #1565c0; }
        .badge-received { background: #c8e6c9; color: #2e7d32; }
        .badge-cancelled { background: #ffcdd2; color: #c62828; }
    </style>
</head>
<body>
<div class="container">
    
    <h2>Purchase Orders</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    <div class="proc-toolbar">
        
        <form action="/legacy-app/purchaseorder/list.do" method="get" style="display:inline;">
            <input type="hidden" name="method" value="poList">
            Status: <select name="status">
                <option value="">All</option>
                <option value="DRAFT">Draft</option>
                <option value="SUBMITTED">Submitted</option>
                <option value="PARTIALLY_RECEIVED">Partially Received</option>
                <option value="RECEIVED">Received</option>
                <option value="CLOSED">Closed</option>
                <option value="CANCELLED">Cancelled</option>
            </select>
            <input type="submit" value="Filter" class="btn" style="font-size:10px;">
        </form>
        &nbsp;
        <a href="/legacy-app/home.do" class="btn btn-secondary">Home</a>
    </div>

    
    <% if (purchaseOrders != null && purchaseOrders.size() > 0) { %>
    <table class="proc-table">
        <tr>
            <th>#</th>
            
            <th>PO Number</th>
            <th>Order Date</th>
            <th>Status</th>
            <th>Total</th>
            <th>Created By</th>
        </tr>
        <%
            // COPY-PASTED loop structure from supplier/list.jsp!
            for (int i = 0; i < purchaseOrders.size(); i++) {
                Object poObj = purchaseOrders.get(i);
                String pId = "", pPoNum = "", pOrderDt = "", pStatus = "", pTotal = "0.00", pCreatedBy = "";
                if (poObj instanceof PurchaseOrder) {
                    PurchaseOrder po = (PurchaseOrder) poObj;
                    pId = po.getId() != null ? po.getId().toString() : "";
                    pPoNum = po.getPoNumber() != null ? po.getPoNumber() : "";
                    pOrderDt = po.getOrderDt() != null ? po.getOrderDt() : "";
                    pStatus = po.getStatus() != null ? po.getStatus() : "";
                    pTotal = String.format("%.2f", po.getTotal());
                    pCreatedBy = po.getCreatedBy() != null ? po.getCreatedBy() : "";
                }
                String badgeClass = "badge-active";
                if ("DRAFT".equals(pStatus)) badgeClass = "badge-draft";
                else if ("SUBMITTED".equals(pStatus)) badgeClass = "badge-submitted";
                else if ("RECEIVED".equals(pStatus) || "PARTIALLY_RECEIVED".equals(pStatus)) badgeClass = "badge-received";
                else if ("CANCELLED".equals(pStatus)) badgeClass = "badge-cancelled";
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            
            <td><b><%= pPoNum %></b></td>
            <td><%= pOrderDt %></td>
            <td><span class="status-badge <%= badgeClass %>"><%= pStatus %></span></td>
            <td align="right">$<%= pTotal %></td>
            <td class="contact-info"><%= pCreatedBy %></td>
        </tr>
        <%
            }
        %>
    </table>
    
    <p style="font-size:10px; color:#999;">Total: <%= purchaseOrders.size() %> purchase orders</p>
    <% } else { %>
        <p>No purchase orders found.</p>
    <% } %>

    <p><a href="/legacy-app/home.do">&laquo; Home</a>
       | <a href="/legacy-app/supplier/list.do?method=supplierList">Suppliers</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
