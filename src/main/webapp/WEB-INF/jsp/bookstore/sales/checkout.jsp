<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.ShoppingCart" %>
<%
    // Auth check — COPY-PASTE from entry.jsp!
    String userName = (String) session.getAttribute("user");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    List cartItems = (List) session.getAttribute("cart");
    String checkoutTotal = (String) session.getAttribute("checkoutTotal");
    String cartItemCount = (String) session.getAttribute("cartItemCount");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");

    // Redirect if cart is empty (business logic in JSP!)
    if (cartItems == null || cartItems.size() == 0) {
        response.sendRedirect("/legacy-app/sales/entry.do?method=entry");
        return;
    }
%>
<html>
<head>
    <title>Checkout - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        /* Copy-pasted from entry.jsp (should be shared CSS but isn't!) */
        .cart-table { width: 100%; border-collapse: collapse; }
        .cart-table th { background-color: #fff8e1; padding: 5px; font-size: 11px; border-bottom: 2px solid #ffc107; }
        .cart-table td { padding: 4px 8px; font-size: 11px; border-bottom: 1px solid #ffe082; }
        .cart-total { font-size: 16px; font-weight: bold; color: #e65100; text-align: right; margin-top: 10px; }

        /* Checkout-specific styles */
        .checkout-section { margin: 15px 0; padding: 15px; border: 1px solid #ccc; background: #fafafa; }
        .checkout-section h3 { color: #336699; margin: 0 0 10px 0; font-size: 14px; border-bottom: 1px solid #ccc; padding-bottom: 5px; }
        .checkout-form td { padding: 5px; }
        .checkout-form label { font-weight: bold; font-size: 11px; display: inline-block; width: 100px; }
        .checkout-form input[type=text], .checkout-form select { padding: 4px; border: 1px solid #999; font-size: 11px; }
        .order-summary { background: #e8f5e9; border: 1px solid #4CAF50; padding: 15px; margin: 15px 0; }
        .btn-place-order { background-color: #4CAF50; color: white; border: none; padding: 12px 30px; cursor: pointer; font-size: 14px; font-weight: bold; }
        .btn-place-order:hover { background-color: #388E3C; }
        .field-error { color: red; font-size: 10px; display: none; }
    </style>

    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    
    <script type="text/javascript" src="../js/common.js"></script>

    
    <script type="text/javascript">

        var formSubmitted = false;

        function validateCheckout() {
            if (formSubmitted) return false;
            var hasError = false;

            $(".field-error").hide();

            var email = document.getElementById("customerEmail").value;
            if (email == "" || email.trim() == "") {
                document.getElementById("emailError").innerHTML = "Email address is required";
                document.getElementById("emailError").style.display = "block";
                hasError = true;
            } else if (!/^[a-zA-Z0-9@._-]+$/.test(email)) {

                document.getElementById("emailError").innerHTML = "Please enter a valid email address";
                document.getElementById("emailError").style.display = "block";
                hasError = true;
            }

            var shipName = document.getElementById("shipName").value;
            if (shipName == "" || shipName.trim() == "") {
                document.getElementById("shipNameError").innerHTML = "Shipping name is required";
                document.getElementById("shipNameError").style.display = "block";
                hasError = true;
            }

            if (hasError) return false;

            formSubmitted = true;
            document.getElementById("submitBtn").disabled = true;
            document.getElementById("submitBtn").value = "Processing...";
            return true;
        }

        function getCartTotal() {
            return document.getElementById("hdnTotal").value;
        }

        $(document).ready(function() {

            if (document.getElementById("shipCountry").value == "") {
                document.getElementById("shipCountry").value = "USA";
            }
        });
    </script>
</head>
<body>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td width="30%"><font size="3"><b>Bookstore System</b></font></td>
    <td align="center">
        <a href="/legacy-app/home.do">Home</a>
        <a href="/legacy-app/sales/entry.do?method=entry">Sales</a>
    </td>
    <td width="25%" align="right">
        <font color="white">User: <%= userName %></font>&nbsp;
        <a href="/legacy-app/logout.do?method=logout"><font color="white">Logout</font></a>
    </td>
</tr>
</table>
<br>

<div class="container">
    <h2>Checkout</h2>

    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    <div class="order-summary">
        <h3 style="color:#2e7d32;">Order Summary</h3>
        <table class="cart-table">
            <tr><th>#</th><th>Book</th><th>Qty</th></tr>
            <%
                for (int i = 0; i < cartItems.size(); i++) {
                    Object cartObj = cartItems.get(i);
                    String ciBookId = "", ciQty = "1";
                    if (cartObj instanceof ShoppingCart) {
                        ShoppingCart sc = (ShoppingCart) cartObj;
                        ciBookId = sc.getBookId() != null ? sc.getBookId() : "";
                        ciQty = sc.getQty() != null ? sc.getQty() : "1";
                    }
            %>
            <tr>
                <td><%= i + 1 %></td>
                <td>Book #<%= ciBookId %></td>
                <td><%= ciQty %></td>
            </tr>
            <% } %>
        </table>
        <div class="cart-total">
            Total: $<%= checkoutTotal != null ? checkoutTotal : "0.00" %>
        </div>
    </div>

    
    <input type="hidden" id="hdnTotal" value="<%= checkoutTotal %>">
    <input type="hidden" id="hdnItemCount" value="<%= cartItemCount %>">

    
    <div class="checkout-section">
        <h3>Shipping & Payment</h3>
        <form action="/legacy-app/sales/checkout/submit.do" method="post" onsubmit="return validateCheckout();">
            <input type="hidden" name="method" value="submitCheckout">
            <input type="hidden" name="step" value="3">

            <table class="checkout-form">
                <tr>
                    <td><label>Email *:</label></td>
                    <td>
                        <input type="text" name="customerEmail" id="customerEmail" size="30">
                        <div id="emailError" class="field-error"></div>
                    </td>
                </tr>
                <tr>
                    <td><label>Payment:</label></td>
                    <td>
                        <select name="payMethod">
                            <option value="CASH">Cash</option>
                            <option value="CARD">Credit Card</option>
                            <option value="CHECK">Check</option>
                        </select>
                    </td>
                </tr>
                <tr><td colspan="2"><hr></td></tr>
                <tr>
                    <td><label>Ship Name *:</label></td>
                    <td>
                        <input type="text" name="shipName" id="shipName" size="30">
                        <div id="shipNameError" class="field-error"></div>
                    </td>
                </tr>
                <tr>
                    <td><label>Address:</label></td>
                    <td><input type="text" name="shipAddr" size="40"></td>
                </tr>
                <tr>
                    <td><label>City:</label></td>
                    <td><input type="text" name="shipCity" size="20"></td>
                </tr>
                <tr>
                    <td><label>State:</label></td>
                    <td><input type="text" name="shipState" size="10"></td>
                </tr>
                <tr>
                    <td><label>Zip:</label></td>
                    <td><input type="text" name="shipZip" size="10"></td>
                </tr>
                <tr>
                    <td><label>Country:</label></td>
                    <td><input type="text" name="shipCountry" id="shipCountry" size="10" value="USA"></td>
                </tr>
                <tr>
                    <td><label>Phone:</label></td>
                    <td><input type="text" name="shipPhone" size="15"></td>
                </tr>
                <tr>
                    <td><label>Notes:</label></td>
                    <td><textarea name="notes" rows="3" cols="40"></textarea></td>
                </tr>
                <tr>
                    <td colspan="2" align="right" style="padding-top:15px;">
                        <a href="/legacy-app/sales/entry.do?method=entry" class="btn btn-secondary">&laquo; Back to Cart</a>
                        &nbsp;
                        <input type="submit" id="submitBtn" value="Place Order" class="btn-place-order">
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>

<hr>
<div style="text-align:center; font-size:10px; color:#999; padding:10px;">
    Copyright &copy; 2005 Example Bookstore Corp.
</div>

</body>
</html>
