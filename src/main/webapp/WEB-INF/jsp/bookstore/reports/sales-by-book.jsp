<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Auth + role check — COPY-PASTED from daily-sales.jsp!
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
        response.sendRedirect("/legacy-app/home.do"); return;
    }

    // Report data — COPY-PASTED variable names from daily-sales.jsp!
    List reportData = (List) request.getAttribute("reportData");
    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    String errMsg = (String) request.getAttribute("err");

    // COPY-PASTED date formatter from daily-sales.jsp!
    SimpleDateFormat reportDisplayFmt = new SimpleDateFormat("MM/dd/yyyy");
%>
<html>
<head>
    
    <title>Sales by Book Report - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .report-header { background: #e8eaf6; padding: 12px; border: 1px solid #c5cae9; margin-bottom: 15px; }
        .report-header label { font-weight: bold; font-size: 11px; margin-right: 5px; }
        .report-header input[type=text] { padding: 4px; border: 1px solid #999; font-size: 11px; width: 90px; }
        .report-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .report-table th { background: #3f51b5; color: white; padding: 6px 8px; font-size: 10px; text-align: left; }
        .report-table td { padding: 5px 8px; font-size: 11px; border-bottom: 1px solid #e0e0e0; }
        .report-table tr:hover { background-color: #e8eaf6; }
        .report-table tr.even { background-color: #f5f5f5; }
        .report-table td.money { text-align: right; font-family: monospace; }
        .report-table td.number { text-align: center; }
        .report-total { font-weight: bold; background: #e8eaf6; }
        .export-bar { margin-top: 10px; text-align: right; }
        .no-data { color: #999; font-size: 12px; padding: 20px; text-align: center; }
    </style>

    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
</head>
<body>
<div class="container">
    
    <h2>Sales by Book Report</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    
    <div class="report-header">
        <form action="/legacy-app/reports/bybook.do" method="get">
            <input type="hidden" name="method" value="salesByBook">
            <label>From:</label>
            <input type="text" name="startDate" value="<%= startDate != null ? startDate : "" %>" placeholder="yyyy-MM-dd">
            &nbsp;
            <label>To:</label>
            <input type="text" name="endDate" value="<%= endDate != null ? endDate : "" %>" placeholder="yyyy-MM-dd">
            &nbsp;
            
            <label>Category:</label>
            <select name="categoryId">
                <option value="">All</option>
            </select>
            &nbsp;
            <label>Sort:</label>
            <select name="sortBy">
                <option value="quantity">By Quantity</option>
                <option value="revenue">By Revenue</option>
            </select>
            &nbsp;
            <input type="submit" value="Generate" class="btn">
        </form>
    </div>

    
    <div class="export-bar">
        <a href="/legacy-app/export/csv.do?method=exportCsv&reportType=bybook&startDate=<%= startDate != null ? startDate : "" %>&endDate=<%= endDate != null ? endDate : "" %>"
           class="btn btn-secondary" style="font-size:10px;">Export CSV</a>
    </div>

    
    <% if (reportData != null && reportData.size() > 0) { %>
    <table class="report-table">
        <tr>
            
            <th>#</th>
            <th>ISBN</th>
            <th>Title</th>
            <th>Category</th>
            <th>Qty Sold</th>
            <th>Revenue</th>
            <th>Avg Price</th>
            <th>Stock</th>
        </tr>
        <%
            // COPY-PASTED loop structure from daily-sales.jsp!
            double totalRevenue = 0.0;
            for (int i = 0; i < reportData.size(); i++) {
                String[] row = (String[]) reportData.get(i);
                // row[0]=isbn, row[1]=title, row[2]=category, row[3]=qtySold, row[4]=revenue, row[5]=avgPrice, row[6]=stock
                double revenue = 0.0, avgPrice = 0.0;
                try { revenue = Double.parseDouble(row[4]); } catch (Exception e) {}
                try { avgPrice = Double.parseDouble(row[5]); } catch (Exception e) {}
                totalRevenue = totalRevenue + revenue;
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            <td><%= row[0] != null ? row[0] : "" %></td>
            <td><%= row[1] != null ? row[1] : "" %></td>
            <td><%= row[2] != null ? row[2] : "" %></td>
            <td class="number"><%= row[3] != null ? row[3] : "0" %></td>
            <td class="money">$<%= String.format("%.2f", revenue) %></td>
            <td class="money">$<%= String.format("%.2f", avgPrice) %></td>
            <td class="number"><%= row[6] != null ? row[6] : "0" %></td>
        </tr>
        <%
            }
        %>
        <tr class="report-total">
            <td colspan="5" align="right"><b>Total Revenue:</b></td>
            <td class="money"><b>$<%= String.format("%.2f", totalRevenue) %></b></td>
            <td colspan="2"></td>
        </tr>
    </table>
    <p style="font-size:10px; color:#999;"><%= reportData.size() %> books | Period: <%= startDate %> to <%= endDate %></p>
    <% } else { %>
        <div class="no-data">No sales data for the selected period.</div>
    <% } %>

    <p><a href="/legacy-app/reports.do?method=menu">&laquo; Back to Reports</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
