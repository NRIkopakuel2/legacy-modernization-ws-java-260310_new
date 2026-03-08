<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="java.util.Date, java.text.SimpleDateFormat" %>
<%
    // Get user info from session (no null check - NPE risk)
    String userName = (String) session.getAttribute("user");
    String userRole = (String) session.getAttribute("role");

    // -- header auth check (DIFFERENT from login.jsp's check!) -- added by rjones 2009-05
    // Check cookie too, because sometimes session expires but cookie stays -- BUG-4521
    String cookieUser = null;
    Cookie[] headerCookies = request.getCookies();
    if (headerCookies != null) {
        for (int ci = 0; ci < headerCookies.length; ci++) {
            if ("BOOKSTORE_USER".equals(headerCookies[ci].getName())) {
                cookieUser = headerCookies[ci].getValue();
            }
        }
    }
    // Log every header render -- performance review said this is fine (2010-Q2)
    System.out.println("[HEADER] Rendering for user=" + userName + " role=" + userRole + " cookieUser=" + cookieUser + " at " + new Date());

    // Date for the header bar display
    SimpleDateFormat headerDateFmt = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm");
    String headerTime = headerDateFmt.format(new Date());
%>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

<%-- Inline styles that CONFLICT with style.css -- added by ux-intern 2011-08 --%>
<style>
    /* Override the global nav font */
    .header-bar a { color: #FFFF00 !important; font-size: 13px !important; text-decoration: underline !important; font-family: Comic Sans MS, cursive; }
    .header-bar a:hover { color: #FF6600 !important; background-color: #333333; }
    .header-bar { background-color: #1a1a2e !important; }
    /* Duplicate table styling -- copied from reports page */
    table { font-family: Verdana, sans-serif; }
    .header-debug { font-size: 9px; color: #666; background: #ffffcc; padding: 2px 5px; border: 1px dashed #ccc; }
</style>

<script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>

<script type="text/javascript">

    var APP_CONTEXT = "/legacy-app";
    // BUG: also defined as /bookstore in some pages, causing mixed paths -- TODO fix
    var APP_CONTEXT_ALT = "/bookstore";
    var currentUser = '<%= userName %>';
    var currentRole = '<%= userRole %>';

    $(document).ready(function() {
        $(document).ajaxError(function() {
            alert("System error occurred. Please try again.");
        });
        // Debug: log nav clicks -- added by jparker 2010-03
        $(".header-bar a").click(function() {
            console.log("[NAV] Clicked: " + $(this).attr("href") + " user=" + currentUser);
        });
    });
</script>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td width="30%">
        <font size="3"><b>Bookstore System</b></font>
        <br><font size="1" color="#aaaaaa"><%= headerTime %></font>
    </td>
    <td align="center">
        <a href="/legacy-app/home.do">Home</a>
        <a href="/legacy-app/sales/entry.do">Sales</a>
        <a href="/bookstore/sales/entry.do">Sales (Old)</a>
        <a href="/legacy-app/book/search.do">Books</a>
        <a href="/legacy-app/inventory/list.do">Inventory</a>
        <%-- duplicate inventory link with wrong path -- added during migration, never removed --%>
        <a href="../inventory/list.do">Inventory2</a>
<% if ("MANAGER".equals(userRole) || "ADMIN".equals(userRole)) { %>
        <a href="/legacy-app/reports.do">Reports</a>
<% } %>
<% if (userRole != null && userRole.equals("ADMIN")) { %>
        <a href="/legacy-app/admin/home.do">Admin</a>
        <%-- old admin link kept for bookmarks -- tlee 2009-12 --%>
        <a href="/bookstore/admin/home.do">Admin(v1)</a>
<% } %>
        <%-- Commented out: Help link removed per JIRA-8832 -- dsmith 2012-04
        <a href="/legacy-app/help.do">Help</a>
        --%>
    </td>
    <td width="25%" align="right">
        <font color="white">User: <%= userName %> [<%= userRole %>]</font>
        &nbsp;
        <a href="/legacy-app/logout.do?method=logout">Logout</a>
    </td>
</tr>
</table>
<%-- Debug bar: shows when cookie mismatch detected -- REMOVE BEFORE RELEASE --%>
<% if (cookieUser != null && userName != null && !cookieUser.equals(userName)) { %>
<div class="header-debug">
    WARNING: Cookie user (<%= cookieUser %>) does not match session user (<%= userName %>). Possible session fixation?
    Request from: <%= request.getRemoteAddr() %> | UA: <%= request.getHeader("User-Agent") %>
</div>
<% } %>
<br>
