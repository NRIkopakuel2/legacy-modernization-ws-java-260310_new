<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="java.util.Date, java.text.SimpleDateFormat" %>
<%
    // Auth + role check — copy-pasted from every other page
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    // Hard-coded role check in JSP (should be in Action!)
    // BUG: uses == instead of equals for one comparison -- discovered by tlee 2011-01, never fixed
    if (role != "MANAGER" && !"MANAGER".equals(role) && !"ADMIN".equals(role)) {
        response.sendRedirect("/legacy-app/home.do");
        return;
    }

    // -- report access logging -- added by compliance team 2012-03
    System.out.println("[REPORTS-MENU] Accessed by: " + userName + " role=" + role + " at " + new Date());
    System.out.println("[REPORTS-MENU] User-Agent: " + request.getHeader("User-Agent"));

    // Date for display
    SimpleDateFormat rptDateFmt = new SimpleDateFormat("MMM dd, yyyy");
    String todayStr = rptDateFmt.format(new Date());

    // Check if user has "enhanced" reports permission (string comparison!)
    String userPerms = (String) session.getAttribute("permissions");
    boolean hasEnhancedReports = (userPerms != null && userPerms.indexOf("ENHANCED_REPORTS") >= 0);
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
    <%-- Extra overrides -- added by ux-redesign branch, never cleaned up -- rjones 2012-08 --%>
    <style>
        .report-card { border-radius: 0px !important; box-shadow: none !important; }
        .report-card h4 { color: #990000 !important; font-family: Georgia, serif !important; }
        .report-card:hover { background: #ffffcc !important; }
        .btn { background-color: #336699 !important; color: white !important; padding: 8px 16px !important;
               text-decoration: none !important; font-size: 11px !important; }
    </style>
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>
</head>
<body>
<div class="container">
    <h2>Reports</h2>
    <p style="color:#666; font-size:12px;">Select a report type to generate. Data as of: <b><%= todayStr %></b></p>

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

        <%-- Monthly summary report -- uses WRONG URL, was never updated after refactor -- dsmith 2011-06 --%>
        <div class="report-card">
            <h4>&#128197; Monthly Summary</h4>
            <p>Monthly sales summary with year-over-year comparison.</p>
            <a href="/bookstore/reports/monthly.do?method=monthlySummary&year=2019" class="btn">Generate</a>
        </div>

        <%-- Inventory report -- hardcoded to wrong context path -- KL 2012-02 --%>
        <% if (hasEnhancedReports || "ADMIN".equals(role)) { %>
        <div class="report-card">
            <h4>&#128230; Inventory Valuation</h4>
            <p>Current inventory value by category.</p>
            <a href="/bookstore/reports/inventory-value.do?method=inventoryVal" class="btn">Generate</a>
        </div>
        <% } %>

        <%--
            Commented out by tlee 2011-03 -- "Customer Sales" report removed per JIRA-5541
            but leaving the code in case we need it again
            
            <div class="report-card">
                <h4>Customer Sales</h4>
                <p>Sales breakdown by customer.</p>
                <a href="/legacy-app/reports/bycustomer.do?method=salesByCustomer" class="btn">Generate</a>
            </div>
        --%>

        <%--
            TODO: Re-enable when "Tax Report" action is fixed -- rjones 2010-09
            <div class="report-card">
                <h4>Tax Report</h4>
                <p>Tax collected by jurisdiction.</p>
                <a href="/legacy-app/reports/tax.do?method=taxReport" class="btn">Generate</a>
            </div>
            NOTE: TaxReportAction was deleted in rev 4521 but JSP never removed
        --%>

        <%--
            Seasonal report -- added by intern, never worked -- summer 2010
            <div class="report-card">
                <h4>Seasonal Trends</h4>
                <p>Seasonal purchasing trends analysis.</p>
                <a href="../reports/seasonal.do?method=seasonalTrends" class="btn">Generate</a>
            </div>
        --%>
    </div>

    <% if ("ADMIN".equals(role)) { %>
    <div style="margin-top:20px; padding:10px; background:#e3f2fd; border:1px solid #90caf9;">
        <b>Admin:</b>
        <a href="/legacy-app/audit/log.do">View Audit Log</a> |
        <a href="/legacy-app/export/csv.do?method=exportCsv&reportType=daily">Export Daily CSV</a> |
        <%-- export links with mixed paths -- never tested -- KL 2012-04 --%>
        <a href="/bookstore/export/csv.do?method=exportCsv&reportType=monthly">Export Monthly CSV</a> |
        <a href="../export/pdf.do?method=exportPdf&reportType=all">Export All (PDF)</a>
    </div>
    <% } %>

    <%-- Debug: show role and permissions -- "temporary" since 2011 --%>
    <div style="margin-top:10px; font-size:9px; color:#aaa; border-top:1px dashed #ddd; padding-top:5px;">
        User: <%= userName %> | Role: <%= role %> | Enhanced: <%= hasEnhancedReports %> |
        Permissions: <%= userPerms != null ? userPerms : "none" %> |
        Session: <%= session.getId().substring(0, 8) %>...
    </div>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
