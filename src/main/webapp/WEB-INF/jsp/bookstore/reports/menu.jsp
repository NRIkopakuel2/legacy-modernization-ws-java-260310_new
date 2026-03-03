<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Auth + role check — copy-pasted from every other page
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }
    // Hard-coded role check in JSP (should be in Action!)
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
        response.sendRedirect("/legacy-app/home.do");
        return;
    }
%>
<html>
<head>
    <title>Reports - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    
    <style>
        /* Table styles — conflicts with style.css definitions! */
        table { border-collapse: collapse; width: 100%; margin: 10px 0; }
        table th { background-color: #2c3e50; color: #ecf0f1; padding: 8px 12px;
                   font-size: 12px; text-align: left; border: 1px solid #1a252f; }
        table td { padding: 6px 12px; font-size: 11px; border: 1px solid #bdc3c7;
                   color: #2c3e50; }
        table tr:nth-child(even) { background-color: #ecf0f1; }
        table tr:hover { background-color: #d5dbdb; }
        table caption { font-size: 14px; font-weight: bold; margin-bottom: 8px;
                        color: #2c3e50; text-align: left; }
        .data-grid { border: 2px solid #2c3e50; }
        .data-grid th { text-transform: uppercase; letter-spacing: 1px; }
        .data-grid td { vertical-align: middle; }
        .data-grid .number { text-align: right; font-family: monospace; }
        .data-grid .total-row { background-color: #f39c12; color: white; font-weight: bold; }
        .data-grid .total-row td { border-top: 2px solid #2c3e50; }
        .data-grid .highlight { background-color: #ffeaa7; }
        .sort-header { cursor: pointer; }
        .sort-header:hover { text-decoration: underline; }
        .no-data { text-align: center; padding: 20px; color: #999; font-style: italic; }
        .export-link { float: right; font-size: 10px; margin-top: -5px; }
    </style>
    <style>
        .report-menu { max-width: 700px; margin: 0 auto; }
        .report-card {
            display: inline-block;
            width: 200px;
            padding: 20px;
            margin: 10px;
            background: white;
            border: 1px solid #ccc;
            border-top: 4px solid #336699;
            vertical-align: top;
            text-align: center;
        }
        .report-card h4 { color: #336699; margin: 0 0 10px 0; font-size: 14px; }
        .report-card p { font-size: 11px; color: #666; margin-bottom: 15px; }
        .report-card .btn { display: block; }
    </style>
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>
</head>
<body>
<div class="container">
    <h2>Reports</h2>
    <p style="color:#666; font-size:12px;">Select a report type to generate.</p>

    <div class="report-menu">
        <div class="report-card">
            <h4>&#128200; Daily Sales</h4>
            <p>Sales aggregated by date with totals and tax breakdowns.</p>
            
            <a href="/legacy-app/reports/daily.do?method=dailySales" class="btn">Generate</a>
        </div>

        <div class="report-card">
            <h4>&#128214; Sales by Book</h4>
            <p>Per-book sales volume, revenue, and average prices.</p>
            <a href="/legacy-app/reports/bybook.do?method=salesByBook" class="btn">Generate</a>
        </div>

        <div class="report-card">
            <h4>&#127942; Top Books</h4>
            <p>Best-selling books ranked by quantity or revenue.</p>
            <a href="/legacy-app/reports/topbooks.do?method=topBooks" class="btn">Generate</a>
        </div>
    </div>

    <% if ("ADMIN".equals(role)) { %>
    <div style="margin-top:20px; padding:10px; background:#e3f2fd; border:1px solid #90caf9;">
        <b>Admin:</b>
        <a href="/legacy-app/audit/log.do">View Audit Log</a> |
        <a href="/legacy-app/export/csv.do?method=exportCsv&reportType=daily">Export Daily CSV</a>
    </div>
    <% } %>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
