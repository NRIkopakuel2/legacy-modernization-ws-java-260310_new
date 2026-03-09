<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Auth check — copy-pasted in every page
    String userName = (String) session.getAttribute("user");
    String userRole = (String) session.getAttribute("role");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    // Dashboard data from session (set by HomeAction — stale on back-navigation!)
    String bookCount = (String) session.getAttribute("bookCount");
    String orderCount = (String) session.getAttribute("orderCount");
    String lowStockCount = (String) session.getAttribute("lowStockCount");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");

    // Inline date formatting (yet another approach — not CommonUtil or DateUtil!)
    SimpleDateFormat homeDateFmt = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm");
    String currentTime = homeDateFmt.format(new java.util.Date());
%>
<html>
<head>
    <title>Dashboard - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .dashboard-header {
            background: linear-gradient(135deg, #336699, #224477);
            color: white;
            padding: 20px;
            margin-bottom: 20px;
        }
        .dashboard-header h2 { margin: 0; color: white; border: none; }
        .dashboard-header p { margin: 5px 0 0 0; font-size: 12px; opacity: 0.8; }
        .card-grid { display: block; }
        .dash-card {
            display: inline-block;
            width: 180px;
            padding: 15px;
            margin: 8px;
            background: white;
            border: 1px solid #ddd;
            border-left: 4px solid #336699;
            vertical-align: top;
        }
        .dash-card h4 { color: #336699; margin: 0 0 5px 0; font-size: 13px; }
        .dash-card .count { font-size: 28px; font-weight: bold; color: #333; }
        .dash-card p { font-size: 11px; color: #666; margin: 5px 0; }
        .dash-card a { font-size: 11px; }
        .card-sales { border-left-color: #4CAF50; }
        .card-sales h4 { color: #4CAF50; }
        .card-inventory { border-left-color: #ff9800; }
        .card-inventory h4 { color: #ff9800; }
        .card-reports { border-left-color: #9c27b0; }
        .card-reports h4 { color: #9c27b0; }
        .card-admin { border-left-color: #f44336; }
        .card-admin h4 { color: #f44336; }
        .quick-actions { margin-top: 20px; padding: 15px; background: #f5f5f5; border: 1px solid #ddd; }
    </style>
    
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>
</head>
<body>
<div class="container">
    
    <div class="dashboard-header">
        <h2>Dashboard</h2>
        <p>Welcome, <b><%= userName %></b> (<%= userRole %>) | <%= currentTime %></p>
    </div>

    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    <div class="card-grid">
        
        <div class="dash-card">
            <h4>&#128218; Books</h4>
            <div class="count"><%= bookCount != null ? bookCount : "?" %></div>
            <p>Total books in catalog</p>
            <a href="/legacy-app/book/search.do">Search Books</a>
        </div>

        
        <div class="dash-card card-sales">
            <h4>&#128176; Sales</h4>
            <div class="count"><%= orderCount != null ? orderCount : "?" %></div>
            <p>Total orders</p>
            <a href="/legacy-app/sales/entry.do?method=entry">New Sale</a>
        </div>

        
        <div class="dash-card card-inventory">
            <h4>&#128230; Inventory</h4>
            <div class="count" style="color: <%= lowStockCount != null && Integer.parseInt(lowStockCount) > 0 ? "orange" : "green" %>">
                <%= lowStockCount != null ? lowStockCount : "?" %>
            </div>
            <p>Low stock items</p>
            <a href="/legacy-app/inventory/list.do?method=list">View Inventory</a>
            | <a href="/legacy-app/inventory/lowstock.do?method=lowStock">Low Stock</a>
        </div>

        
        <div class="dash-card">
            <h4>&#127970; Suppliers</h4>
            <a href="/legacy-app/supplier/list.do?method=supplierList">View Suppliers</a>
            <br><a href="/legacy-app/purchaseorder/list.do?method=poList">Purchase Orders</a>
        </div>

        
        <%
            if ("MANAGER".equals(userRole) || "ADMIN".equals(userRole)) {
        %>
        <div class="dash-card card-reports">
            <h4>&#128202; Reports</h4>
            <p>Sales and inventory reports</p>
            <a href="/legacy-app/reports.do?method=menu">View Reports</a>
            <br><a href="/legacy-app/export/csv.do?method=exportCsv&reportType=daily">Quick CSV Export</a>
        </div>

        <div class="dash-card">
            <h4>&#128220; Audit Log</h4>
            <p>System activity history</p>
            <a href="/legacy-app/audit/log.do">View Audit Log</a>
        </div>
        <%
            }
        %>

        
        <%
            if ("ADMIN".equals(userRole)) {
        %>
        <div class="dash-card card-admin">
            <h4>&#9881; Admin</h4>
            <p>System administration</p>
            <a href="/legacy-app/admin/home.do?method=home">Admin Panel</a>
            <br><a href="/legacy-app/admin/user/list.do?method=userList">Users</a>
            | <a href="/legacy-app/admin/category/list.do?method=categoryList">Categories</a>
        </div>
        <%
            }
        %>
    </div>

    
    <div class="quick-actions">
        <b style="font-size:12px;">Quick Actions:</b>&nbsp;
        <a href="/legacy-app/sales/entry.do?method=entry" class="btn" style="font-size:10px;">New Sale</a>
        <a href="/legacy-app/book/search.do" class="btn" style="font-size:10px;">Search Books</a>
        <a href="/legacy-app/inventory/list.do?method=list" class="btn" style="font-size:10px;">Inventory</a>
        <% if ("ADMIN".equals(userRole)) { %>
            <a href="/legacy-app/db-ping.jsp" class="btn btn-secondary" style="font-size:10px;">DB Test</a>
        <% } %>
    </div>
</div>
<script>document.write("<p style='color:gray;font-size:9px'>Page rendered: " + new Date() + "</p>");</script>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
