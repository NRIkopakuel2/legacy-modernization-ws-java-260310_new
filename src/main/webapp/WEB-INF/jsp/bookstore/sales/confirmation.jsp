<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Order" %>
<%@ page import="com.example.bookstore.util.CommonUtil" %>
<%
    // Auth check — COPY-PASTE from entry.jsp and checkout.jsp!
    String userName = (String) session.getAttribute("user");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    // Get order from request/session
    Object orderObj = request.getAttribute("order");
    if (orderObj == null) orderObj = session.getAttribute("lastOrder");

    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");

    // Extract order fields — messy type handling (business logic in JSP!)
    String orderNo = "";
    String orderDt = "";
    String orderTotal = "0.00";
    String orderStatus = "";
    String customerEmail = "";

    if (orderObj != null && orderObj instanceof Order) {
        Order order = (Order) orderObj;
        orderNo = order.getOrderNo() != null ? order.getOrderNo() : "";
        orderDt = order.getOrderDt() != null ? order.getOrderDt() : "";
        orderTotal = CommonUtil.formatMoney(order.getTotal());
        orderStatus = order.getStatus() != null ? order.getStatus() : "";
        customerEmail = order.getGuestEmail() != null ? order.getGuestEmail() : "";
    }
%>
<html>
<head>
    <title>Order Confirmation - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        .confirmation-box {
            max-width: 650px;
            margin: 20px auto;
            padding: 30px;
            background: #e8f5e9;
            border: 2px solid #4CAF50;
        }
        .confirm-icon { font-size: 48px; text-align: center; color: #4CAF50; }
        .confirm-title { font-size: 22px; color: #2e7d32; font-weight: bold; text-align: center; margin: 15px 0; }
        .order-detail { background: white; padding: 15px; border: 1px solid #ccc; margin: 15px 0; }
        .order-detail td { padding: 5px; font-size: 12px; }
        .order-detail td:first-child { font-weight: bold; color: #336699; width: 150px; }
    </style>

    <script type="text/javascript">
        function printReceipt() {
            window.print();
        }
    </script>
</head>
<body>

<jsp:include page="/includes/header.jsp" />

<div class="container">
    <div class="confirmation-box">
        <div class="confirm-icon">&#10004;</div>
        <div class="confirm-title">Order Placed Successfully!</div>

        <% if (msg != null) { %><div class="msg" style="text-align:center;"><%= msg %></div><% } %>

        <div class="order-detail">
            <table width="100%" cellpadding="3">
                <tr>
                    <td>Order Number:</td>
                    
                    <td><b><%= orderNo %></b></td>
                </tr>
                <tr>
                    <td>Order Date:</td>
                    <td><%= orderDt %></td>
                </tr>
                <tr>
                    <td>Status:</td>
                    <td><span style="color:green; font-weight:bold;"><%= orderStatus %></span></td>
                </tr>
                <tr>
                    <td>Email:</td>
                    <td><%= customerEmail %></td>
                </tr>
                <tr>
                    <td>Total:</td>
                    <td style="font-size:16px; font-weight:bold; color:#e65100;">$<%= orderTotal %></td>
                </tr>
            </table>
        </div>

        <p style="font-size:12px; color:#666; text-align:center;">
            A confirmation email will be sent to <b><%= customerEmail %></b>.
            <br>Please save your order number <b><%= orderNo %></b> for reference.
        </p>

        <div style="text-align:center; margin-top:20px;">
            <input type="button" value="Print Receipt" class="btn" onclick="printReceipt();">
            &nbsp;
            <a href="/legacy-app/sales/entry.do?method=entry" class="btn" style="text-decoration:none; color:white; display:inline-block; padding:6px 15px;">New Sale</a>
            &nbsp;
            <a href="/legacy-app/home.do" class="btn btn-secondary" style="text-decoration:none; color:white; display:inline-block; padding:6px 15px;">Home</a>
        </div>
    </div>
</div>

<jsp:include page="/includes/footer.jsp" />

</body>
</html>
