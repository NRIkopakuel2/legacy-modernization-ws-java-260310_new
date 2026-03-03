<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%
    // Auth + role check — COPY-PASTED from menu.jsp!
    String userName = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }
    if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
        response.sendRedirect("/legacy-app/home.do"); return;
    }

    // Report data from Action
    List reportData = (List) request.getAttribute("reportData");
    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    String errMsg = (String) request.getAttribute("err");

    // Inline date formatting for display (yet ANOTHER format pattern!)
    SimpleDateFormat reportDisplayFmt = new SimpleDateFormat("MM/dd/yyyy");
%>
<html>
<head>
    <title>Daily Sales Report - Bookstore System</title>
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
    <h2>Daily Sales Report</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    
    <div class="report-header">
        <form action="/legacy-app/reports/daily.do" method="get">
            <input type="hidden" name="method" value="dailySales">
            <label>From:</label>
            <input type="text" name="startDate" value="<%= startDate != null ? startDate : "" %>" placeholder="yyyy-MM-dd">
            &nbsp;
            <label>To:</label>
            <input type="text" name="endDate" value="<%= endDate != null ? endDate : "" %>" placeholder="yyyy-MM-dd">
            &nbsp;
            <input type="submit" value="Generate" class="btn">
        </form>
    </div>

    
    <div class="export-bar">
        <a href="/legacy-app/export/csv.do?method=exportCsv&reportType=daily&startDate=<%= startDate != null ? startDate : "" %>&endDate=<%= endDate != null ? endDate : "" %>"
           class="btn btn-secondary" style="font-size:10px;">Export CSV</a>
    </div>

    
    <% if (reportData != null && reportData.size() > 0) { %>
    <table class="report-table">
        <tr>
            
            <th>#</th>
            <th>Date</th>
            <th>Orders</th>
            <th>Items Sold</th>
            <th>Gross Sales</th>
            <th>Tax</th>
            <th>Net Sales</th>
        </tr>
        <%
            double grandTotal = 0.0;
            double grandTax = 0.0;
            for (int i = 0; i < reportData.size(); i++) {
                String[] row = (String[]) reportData.get(i);
                // row[0]=date, row[1]=orderCount, row[2]=itemsSold, row[3]=grossSales, row[4]=tax, row[5]=netSales
                // Money formatting with double (rounding bugs!)
                double gross = 0.0, tax = 0.0, net = 0.0;
                try { gross = Double.parseDouble(row[3]); } catch (Exception e) {}
                try { tax = Double.parseDouble(row[4]); } catch (Exception e) {}
                try { net = Double.parseDouble(row[5]); } catch (Exception e) {}
                grandTotal = grandTotal + gross;
                grandTax = grandTax + tax;
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            <td><%= row[0] != null ? row[0] : "" %></td>
            <td class="number"><%= row[1] != null ? row[1] : "0" %></td>
            <td class="number"><%= row[2] != null ? row[2] : "0" %></td>
            
            <td class="money">$<%= String.format("%.2f", gross) %></td>
            <td class="money">$<%= String.format("%.2f", tax) %></td>
            <td class="money">$<%= String.format("%.2f", net) %></td>
        </tr>
        <%
            }
        %>
        
        <tr class="report-total">
            <td colspan="4" align="right"><b>Grand Total:</b></td>
            <td class="money"><b>$<%= String.format("%.2f", grandTotal) %></b></td>
            <td class="money"><b>$<%= String.format("%.2f", grandTax) %></b></td>
            <td class="money"><b>$<%= String.format("%.2f", grandTotal - grandTax) %></b></td>
        </tr>
    </table>
    <p style="font-size:10px; color:#999;"><%= reportData.size() %> rows | Period: <%= startDate %> to <%= endDate %></p>
    <% } else { %>
        <div class="no-data">No sales data for the selected period. Try a different date range.</div>
    <% } %>

    <p><a href="/legacy-app/reports.do?method=menu">&laquo; Back to Reports</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
