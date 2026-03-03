<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
    String role = (String) session.getAttribute("role");
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) { response.sendRedirect("/legacy-app/home.do"); return; }
    List auditLogs = (List) session.getAttribute("auditLogs");
    String totalCount = (String) session.getAttribute("auditTotalCount");
%>
<html>
<head><title>Audit Log - Bookstore</title><jsp:include page="/includes/header.jsp" /></head>
<body>
<div class="container">
    <h2>Audit Log</h2>
    <form action="/legacy-app/audit/log.do" method="get">
        From: <input type="text" name="startDate" size="10">
        To: <input type="text" name="endDate" size="10">
        Action: <input type="text" name="actionType" size="15">
        <input type="submit" value="Filter" class="btn">
    </form>
    <% if (auditLogs != null && auditLogs.size() > 0) { %>
    <p>Total: <%= totalCount %></p>
    <table class="tbl"><tr><th>#</th><th>Log</th></tr>
    <% for (int i = 0; i < auditLogs.size(); i++) { %>
    <tr><td><%= i+1 %></td><td><%= auditLogs.get(i) %></td></tr>
    <% } %></table>
    <% } else { %><p>No audit logs found.</p><% } %>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
