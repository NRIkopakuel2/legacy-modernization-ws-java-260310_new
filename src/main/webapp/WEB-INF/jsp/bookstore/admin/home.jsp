<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="java.sql.*, java.util.Date, java.text.SimpleDateFormat" %>
<%!
    // Static cache -- "optimization" per JIRA-6234, never invalidated -- mchen 2010-09
    private static String cachedUserCount = null;
    private static String cachedBookCount = null;
    private static String cachedOrderCount = null;
    private static long cacheTime = 0;
    private static final long CACHE_TTL = 300000; // 5 min in millis
%>
<%
    // Auth + ADMIN role check — copy-pasted
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }

    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");

    // -- Dashboard stats via JDBC -- added by dsmith 2010-03
    // TODO: move this to AdminAction.java (JIRA-7891, assigned to nobody)
    System.out.println("[ADMIN-HOME] Dashboard loaded by: " + userName + " at " + new Date());

    String userCount = "?";
    String bookCount = "?";
    String orderCount = "?";
    String recentOrderAmt = "?";

    long now = System.currentTimeMillis();
    if (cachedUserCount != null && (now - cacheTime) < CACHE_TTL) {
        // Use cached values
        userCount = cachedUserCount;
        bookCount = cachedBookCount;
        orderCount = cachedOrderCount;
        System.out.println("[ADMIN-HOME] Using cached dashboard stats (age=" + (now - cacheTime) + "ms)");
    } else {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // Hardcoded JNDI lookup -- should use DataSource from context -- KL 2011-01
            javax.naming.InitialContext ctx = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/jdbc/bookstoreDB");
            conn = ds.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) userCount = String.valueOf(rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next()) bookCount = String.valueOf(rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM sales_transactions");
            if (rs.next()) orderCount = String.valueOf(rs.getInt(1));
            rs.close();

            // Recent order total -- may be slow on large tables!
            rs = stmt.executeQuery("SELECT SUM(total_amount) FROM sales_transactions WHERE sale_date > CURRENT_DATE - 7");
            if (rs.next()) recentOrderAmt = String.valueOf(rs.getDouble(1));
            rs.close();

            // Update cache
            cachedUserCount = userCount;
            cachedBookCount = bookCount;
            cachedOrderCount = orderCount;
            cacheTime = now;
            System.out.println("[ADMIN-HOME] Refreshed dashboard stats cache");
        } catch (Exception e) {
            System.out.println("[ADMIN-HOME] ERROR fetching stats: " + e.getMessage());
            e.printStackTrace(System.out);
            // swallow and continue -- dashboard should still render
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e2) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e2) {}
            try { if (conn != null) conn.close(); } catch (Exception e2) {}
        }
    }

    // Date formatting (thread-unsafe!) -- dsmith 2010-03
    SimpleDateFormat dashFmt = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm:ss a z");
    String dashboardTime = dashFmt.format(new Date());
    SimpleDateFormat shortFmt = new SimpleDateFormat("MM/dd/yy");
    String shortDate = shortFmt.format(new Date());
%>
<html>
<head>
    <title>Admin Dashboard - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />
    <style>
        .admin-card {
            display: inline-block; width: 220px; padding: 20px; margin: 10px;
            background: white; border: 1px solid #ccc; border-top: 4px solid #9c27b0;
            vertical-align: top;
        }
        .admin-card h4 { color: #7b1fa2; margin: 0 0 10px 0; font-size: 14px; }
        .admin-card p { font-size: 11px; color: #666; margin-bottom: 15px; }
        <%-- Stats card styles -- should be in style.css -- tlee 2010-04 --%>
        .stat-card { display: inline-block; width: 150px; padding: 15px; margin: 5px;
                     background: #f5f5f5; border: 1px solid #ddd; text-align: center; vertical-align: top; }
        .stat-card .stat-number { font-size: 28px; font-weight: bold; color: #7b1fa2; }
        .stat-card .stat-label { font-size: 10px; color: #999; text-transform: uppercase; }
    </style>
</head>
<body>
<div class="container">
    <h2>Admin Dashboard</h2>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>
    <p style="font-size:12px; color:#666;">Welcome, Administrator <b><%= userName %></b>. Today is <%= dashboardTime %>.</p>

    <%-- Dashboard stats cards -- inline JDBC results -- dsmith 2010-03 --%>
    <div style="margin-bottom: 20px; padding: 10px; background: #fafafa; border: 1px solid #eee;">
        <b style="font-size:12px;">Quick Stats</b> <font size="1" color="#999">(cached, refreshes every 5 min | as of <%= shortDate %>)</font>
        <br><br>
        <div class="stat-card">
            <div class="stat-number"><%= userCount %></div>
            <div class="stat-label">Total Users</div>
        </div>
        <div class="stat-card">
            <div class="stat-number"><%= bookCount %></div>
            <div class="stat-label">Total Books</div>
        </div>
        <div class="stat-card">
            <div class="stat-number"><%= orderCount %></div>
            <div class="stat-label">Total Orders</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">$<%= recentOrderAmt %></div>
            <div class="stat-label">Sales (7 days)</div>
        </div>
    </div>

    <div class="admin-card">
        <h4>&#128100; User Management</h4>
        <p>Create, edit, and manage user accounts and roles.</p>
        <a href="/legacy-app/admin/user/list.do?method=userList" class="btn">Manage Users</a>
    </div>

    <div class="admin-card">
        <h4>&#128218; Category Management</h4>
        <p>Create, edit, and delete book categories.</p>
        <a href="/legacy-app/admin/category/list.do?method=categoryList" class="btn">Manage Categories</a>
    </div>

    <div class="admin-card">
        <h4>&#128202; Reports</h4>
        <p>Access sales and inventory reports.</p>
        <a href="/legacy-app/reports.do?method=menu" class="btn">View Reports</a>
    </div>

    <div class="admin-card">
        <h4>&#128220; Audit Log</h4>
        <p>View system activity and user action history.</p>
        <a href="/legacy-app/audit/log.do" class="btn">View Audit Log</a>
    </div>

    <%-- Debug panel -- "temporary" since 2010 --%>
    <div style="margin-top:20px; padding:10px; background:#f3e5f5; border:1px solid #ce93d8; font-size:11px;">
        <b>System Info:</b> Session ID: <%= session.getId() %> | 
        Server Time: <%= new java.util.Date() %><br>
        <b>Cache Age:</b> <%= (System.currentTimeMillis() - cacheTime) / 1000 %>s |
        <b>JVM Free Memory:</b> <%= Runtime.getRuntime().freeMemory() / 1024 / 1024 %>MB |
        <b>Server:</b> <%= application.getServerInfo() %>
    </div>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
