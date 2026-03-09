<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Supplier" %>
<%
    // Auth check — copy-pasted
    String userName = (String) session.getAttribute("user");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    List suppliers = (List) session.getAttribute("suppliers");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");
%>
<html>
<head>
    <title>Suppliers - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    
    <style>
        /* Supplier page overrides — duplicated from style.css with drift */
        body { font-family: Verdana, Geneva, sans-serif; font-size: 12px; }
        .container { max-width: 960px; margin: 0 auto; }
        h2 { color: #00695c; font-size: 18px; border-bottom: 2px solid #00695c; }
        a { color: #00796b; text-decoration: none; }
        a:hover { text-decoration: underline; color: #004d40; }
        .btn { background: #00796b; color: white; border: none; padding: 4px 10px;
               font-size: 11px; cursor: pointer; }
        .btn:hover { background: #00695c; }
        .err { background: #ffebee; color: #c62828; padding: 8px; border: 1px solid #ef9a9a; }
        .msg { background: #e8f5e9; color: #2e7d32; padding: 8px; border: 1px solid #a5d6a7; }
        input[type=text] { padding: 3px; border: 1px solid #b0bec5; font-size: 11px; }
        select { padding: 3px; border: 1px solid #b0bec5; font-size: 11px; }
        .supplier-contact { font-size: 10px; color: #546e7a; font-style: italic; }
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
    </style>
</head>
<body>
<div class="container">
    <h2>Supplier List</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    <div class="proc-toolbar">
        <a href="/legacy-app/home.do" class="btn btn-secondary">Home</a>
    </div>

    <% if (suppliers != null && suppliers.size() > 0) { %>
    <table class="proc-table">
        <tr>
            <th>#</th>
            <th>Name</th>
            <th>Contact</th>
            <th>Email</th>
            <th>Phone</th>
            <th>City</th>
            <th>Status</th>
            <th>Lead Time</th>
        </tr>
        <%
            for (int i = 0; i < suppliers.size(); i++) {
                Object suppObj = suppliers.get(i);
                String sId = "", sName = "", sContact = "", sEmail = "", sPhone = "", sCity = "", sStatus = "", sLeadTime = "";
                if (suppObj instanceof Supplier) {
                    Supplier sup = (Supplier) suppObj;
                    sId = sup.getId() != null ? sup.getId().toString() : "";
                    sName = sup.getNm() != null ? sup.getNm() : "";
                    sContact = sup.getContactPerson() != null ? sup.getContactPerson() : "";
                    sEmail = sup.getEmail() != null ? sup.getEmail() : "";
                    sPhone = sup.getPhone() != null ? sup.getPhone() : "";
                    sCity = sup.getCity() != null ? sup.getCity() : "";
                    sStatus = sup.getStatus() != null ? sup.getStatus() : "";
                    sLeadTime = sup.getLeadTimeDays() != null ? sup.getLeadTimeDays() + " days" : "";
                }
                String badgeClass = "ACTIVE".equals(sStatus) ? "badge-active" : "badge-inactive";
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            
            <td><b><%= sName %></b></td>
            <td class="contact-info"><%= sContact %></td>
            <td class="contact-info"><%= sEmail %></td>
            <td class="contact-info"><%= sPhone %></td>
            <td><%= sCity %></td>
            <td><span class="status-badge <%= badgeClass %>"><%= sStatus %></span></td>
            <td><%= sLeadTime %></td>
        </tr>
        <%
            }
        %>
    </table>
    <p style="font-size:10px; color:#999;">Total: <%= suppliers.size() %> suppliers</p>
    <% } else { %>
        <p>No suppliers found.</p>
    <% } %>

    <p><a href="/legacy-app/home.do">&laquo; Home</a>
       | <a href="/legacy-app/purchaseorder/list.do?method=poList">Purchase Orders</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
