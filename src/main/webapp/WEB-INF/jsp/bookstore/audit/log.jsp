<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat, java.sql.*" %>
<%
    String role = (String) session.getAttribute("role");
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }
    List auditLogs = (List) session.getAttribute("auditLogs");
    String totalCount = (String) session.getAttribute("auditTotalCount");

    // -- Inline JDBC query to ALSO fetch audit logs directly -- added by dsmith 2010-11
    // "The Action wasn't returning all fields so I query them here too" -- dsmith
    // TODO: remove this once AuditAction is fixed (JIRA-6102)
    String filterStartDate = request.getParameter("startDate");
    String filterEndDate = request.getParameter("endDate");
    String filterAction = request.getParameter("actionType");

    // Pagination in JSP -- because "it was faster to add here" -- mchen 2011-04
    String pageParam = request.getParameter("p");
    int currentPage = 1;
    int pageSize = 25; // hardcoded, should be configurable
    try { if (pageParam != null) currentPage = Integer.parseInt(pageParam); } catch (Exception e) { currentPage = 1; }
    if (currentPage < 1) currentPage = 1;
    int offset = (currentPage - 1) * pageSize;
    int totalRows = 0;

    // Build query dynamically -- SQL injection risk! -- never fixed
    String baseQuery = "SELECT audit_id, action_type, user_id, action_detail, created_date FROM audit_log WHERE 1=1";
    String countQuery = "SELECT COUNT(*) FROM audit_log WHERE 1=1";
    if (filterStartDate != null && filterStartDate.length() > 0) {
        baseQuery += " AND created_date >= '" + filterStartDate + "'";
        countQuery += " AND created_date >= '" + filterStartDate + "'";
    }
    if (filterEndDate != null && filterEndDate.length() > 0) {
        baseQuery += " AND created_date <= '" + filterEndDate + " 23:59:59'";
        countQuery += " AND created_date <= '" + filterEndDate + " 23:59:59'";
    }
    if (filterAction != null && filterAction.length() > 0) {
        baseQuery += " AND action_type = '" + filterAction + "'";
        countQuery += " AND action_type = '" + filterAction + "'";
    }
    baseQuery += " ORDER BY created_date DESC LIMIT " + pageSize + " OFFSET " + offset;

    System.out.println("[AUDIT-LOG] Query: " + baseQuery);
    System.out.println("[AUDIT-LOG] Count query: " + countQuery);
    System.out.println("[AUDIT-LOG] Page=" + currentPage + " offset=" + offset + " user=" + session.getAttribute("user"));

    // Date formatter for display -- not thread-safe, created per request
    SimpleDateFormat auditDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat displayFmt = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

    List jdbcAuditRows = new ArrayList();
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
        javax.naming.InitialContext ctx = new javax.naming.InitialContext();
        javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/jdbc/bookstoreDB");
        conn = ds.getConnection();
        stmt = conn.createStatement();

        // Get total count for pagination
        rs = stmt.executeQuery(countQuery);
        if (rs.next()) totalRows = rs.getInt(1);
        rs.close();

        // Get page of results
        rs = stmt.executeQuery(baseQuery);
        while (rs.next()) {
            HashMap row = new HashMap();
            row.put("id", String.valueOf(rs.getInt("audit_id")));
            row.put("action", rs.getString("action_type"));
            row.put("userId", rs.getString("user_id"));
            row.put("detail", rs.getString("action_detail"));
            // Format the date inline
            String rawDate = rs.getString("created_date");
            String fmtDate = rawDate;
            try {
                java.util.Date parsed = auditDateFmt.parse(rawDate);
                fmtDate = displayFmt.format(parsed);
            } catch (Exception de) {
                System.out.println("[AUDIT-LOG] Date parse error: " + rawDate);
            }
            row.put("date", fmtDate);
            jdbcAuditRows.add(row);
        }
        rs.close();
        System.out.println("[AUDIT-LOG] Fetched " + jdbcAuditRows.size() + " rows (total=" + totalRows + ")");
    } catch (Exception e) {
        System.out.println("[AUDIT-LOG] JDBC ERROR: " + e.getMessage());
        e.printStackTrace(System.out);
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception x) {}
        try { if (stmt != null) stmt.close(); } catch (Exception x) {}
        try { if (conn != null) conn.close(); } catch (Exception x) {}
    }

    int totalPages = (totalRows + pageSize - 1) / pageSize;
    if (totalPages < 1) totalPages = 1;
%>
<html>
<head>
    <title>Audit Log - Bookstore</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .audit-tbl { width: 100%; border-collapse: collapse; font-size: 10px; }
        .audit-tbl th { background: #2c3e50; color: white; padding: 5px 8px; text-align: left; }
        .audit-tbl td { padding: 4px 8px; border-bottom: 1px solid #ddd; }
        .audit-tbl tr:hover { background: #f5f5f5; }
        .page-nav { margin: 10px 0; font-size: 11px; }
        .page-nav a { margin: 0 3px; padding: 3px 8px; border: 1px solid #ccc; text-decoration: none; }
        .page-nav a.active { background: #336699; color: white; }
    </style>
</head>
<body>
<div class="container">
    <h2>Audit Log</h2>
    <form action="/legacy-app/audit/log.do" method="get">
        From: <input type="text" name="startDate" size="10" value="<%= filterStartDate != null ? filterStartDate : "" %>">
        To: <input type="text" name="endDate" size="10" value="<%= filterEndDate != null ? filterEndDate : "" %>">
        Action: <input type="text" name="actionType" size="15" value="<%= filterAction != null ? filterAction : "" %>">
        <input type="submit" value="Filter" class="btn">
    </form>

    <%-- Debug query display -- DO NOT show in production!! -- dsmith 2010-11 --%>
    <div style="background:#ffffcc; border:1px dashed #cc9900; padding:5px; margin:5px 0; font-size:9px; font-family:monospace; color:#666;">
        DEBUG SQL: <%= baseQuery %><br>
        Page: <%= currentPage %>/<%= totalPages %> | Total rows: <%= totalRows %> | Offset: <%= offset %>
    </div>

    <%-- Use JDBC results instead of session attribute if available --%>
    <% if (jdbcAuditRows.size() > 0) { %>
    <p>Showing <%= offset + 1 %>-<%= Math.min(offset + pageSize, totalRows) %> of <%= totalRows %> entries</p>
    <table class="audit-tbl">
        <tr><th>#</th><th>ID</th><th>Date</th><th>Action</th><th>User</th><th>Detail</th></tr>
        <% for (int i = 0; i < jdbcAuditRows.size(); i++) {
            HashMap row = (HashMap) jdbcAuditRows.get(i);
            System.out.println("[AUDIT-LOG] Row " + (offset + i + 1) + ": " + row.get("action") + " by " + row.get("userId"));
        %>
        <tr>
            <td><%= offset + i + 1 %></td>
            <td><%= row.get("id") %></td>
            <td><%= row.get("date") %></td>
            <td><%= row.get("action") %></td>
            <td><%= row.get("userId") %></td>
            <td><%= row.get("detail") %></td>
        </tr>
        <% } %>
    </table>

    <%-- Pagination nav -- built in JSP because "it's just a few links" -- mchen 2011-04 --%>
    <div class="page-nav">
        <% if (currentPage > 1) { %>
            <a href="/legacy-app/audit/log.do?p=<%= currentPage - 1 %>&startDate=<%= filterStartDate != null ? filterStartDate : "" %>&endDate=<%= filterEndDate != null ? filterEndDate : "" %>&actionType=<%= filterAction != null ? filterAction : "" %>">&laquo; Prev</a>
        <% } %>
        <% for (int p = 1; p <= totalPages && p <= 10; p++) { %>
            <a href="/legacy-app/audit/log.do?p=<%= p %>&startDate=<%= filterStartDate != null ? filterStartDate : "" %>&endDate=<%= filterEndDate != null ? filterEndDate : "" %>&actionType=<%= filterAction != null ? filterAction : "" %>" class="<%= p == currentPage ? "active" : "" %>"><%= p %></a>
        <% } %>
        <% if (totalPages > 10) { %>
            ... <a href="/legacy-app/audit/log.do?p=<%= totalPages %>&startDate=<%= filterStartDate != null ? filterStartDate : "" %>&endDate=<%= filterEndDate != null ? filterEndDate : "" %>&actionType=<%= filterAction != null ? filterAction : "" %>"><%= totalPages %></a>
        <% } %>
        <% if (currentPage < totalPages) { %>
            <a href="/legacy-app/audit/log.do?p=<%= currentPage + 1 %>&startDate=<%= filterStartDate != null ? filterStartDate : "" %>&endDate=<%= filterEndDate != null ? filterEndDate : "" %>&actionType=<%= filterAction != null ? filterAction : "" %>">Next &raquo;</a>
        <% } %>
    </div>

    <% } else if (auditLogs != null && auditLogs.size() > 0) { %>
    <%-- Fallback to session-based data if JDBC failed --%>
    <p>Total: <%= totalCount %> (from session cache)</p>
    <table class="tbl"><tr><th>#</th><th>Log</th></tr>
    <% for (int i = 0; i < auditLogs.size(); i++) { %>
    <tr><td><%= i+1 %></td><td><%= auditLogs.get(i) %></td></tr>
    <% } %></table>
    <% } else { %><p>No audit logs found.</p><% } %>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
