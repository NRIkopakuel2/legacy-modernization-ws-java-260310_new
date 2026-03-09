<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%@ page import="com.example.bookstore.model.ShoppingCart" %>
<%
    // Auth check — copy-pasted from Action (should only be in Action!)
    String userName = (String) session.getAttribute("user");
    String userRole = (String) session.getAttribute("role");
    if (userName == null) {
        response.sendRedirect("/legacy-app/login.do");
        return;
    }

    // Get data from session (set by SalesAction)
    List bookList = (List) session.getAttribute("books");
    List cartItems = (List) session.getAttribute("cart");
    String cartTotal = (String) session.getAttribute("cartTotal");
    String cartItemCount = (String) session.getAttribute("cartItemCount");
    List categories = (List) session.getAttribute("categories");
    String errMsg = (String) request.getAttribute("err");
    String msg = (String) session.getAttribute("msg");
    if (msg != null) session.removeAttribute("msg");

    // Inline date formatting (yet another approach — not CommonUtil or DateUtil!)
    SimpleDateFormat entryDateFmt = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    String currentTime = entryDateFmt.format(new java.util.Date());
%>
<html>
<head>
    <title>Sales Entry - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        .search-panel {
            background-color: #f0f4f8;
            border: 1px solid #ccc;
            padding: 12px;
            margin-bottom: 15px;
        }
        .search-panel label { font-weight: bold; font-size: 11px; margin-right: 5px; }
        .search-panel input[type=text] { padding: 4px; border: 1px solid #999; font-size: 11px; }
        .search-panel select { padding: 3px; font-size: 11px; }
        .book-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .book-table th { background-color: #336699; color: white; padding: 5px 8px; font-size: 11px; text-align: left; border: 1px solid #224477; }
        .book-table td { padding: 4px 8px; font-size: 11px; border: 1px solid #ccc; }
        .book-table tr:hover { background-color: #e8f0fe; }
        .book-table tr.even { background-color: #f5f5f5; }
        .cart-panel {
            background-color: #fffde7;
            border: 2px solid #ffc107;
            padding: 15px;
            margin-top: 15px;
        }
        .cart-panel h3 { color: #f57f17; margin: 0 0 10px 0; font-size: 14px; }
        .cart-table { width: 100%; border-collapse: collapse; }
        .cart-table th { background-color: #fff8e1; padding: 5px; font-size: 11px; border-bottom: 2px solid #ffc107; }
        .cart-table td { padding: 4px 8px; font-size: 11px; border-bottom: 1px solid #ffe082; }
        .cart-total { font-size: 16px; font-weight: bold; color: #e65100; text-align: right; margin-top: 10px; }
        .qty-input { width: 40px; padding: 2px; text-align: center; border: 1px solid #999; }
        .btn-add { background-color: #4CAF50; color: white; border: none; padding: 3px 8px; cursor: pointer; font-size: 10px; }
        .btn-add:hover { background-color: #388E3C; }
        .btn-remove { background-color: #f44336; color: white; border: none; padding: 2px 6px; cursor: pointer; font-size: 10px; }
        .btn-checkout { background-color: #ff9800; color: white; border: none; padding: 8px 20px; cursor: pointer; font-size: 13px; font-weight: bold; }
        .btn-checkout:hover { background-color: #f57c00; }
        .stock-ok { color: green; }
        .stock-low { color: orange; font-weight: bold; }
        .stock-out { color: red; font-weight: bold; }
        .page-info { font-size: 10px; color: #999; text-align: right; margin-bottom: 5px; }
    </style>

    
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/legacy-app/js/common.js"></script>

    
    <script type="text/javascript">

        var APP_CTX = "/legacy-app";
        var currentBookId = 0;
        var cartCount = <%= cartItemCount != null ? cartItemCount : "0" %>;
        var isProcessing = false;

        function doSearch() {
            var isbn = document.getElementById("searchIsbn").value;
            var title = document.getElementById("searchTitle").value;
            var catId = document.getElementById("searchCatId").value;

            if (isbn == "" && title == "" && catId == "") {

            }

            document.getElementById("searchForm").submit();
        }

        function addToCart(bookId) {
            if (isProcessing) return;
            isProcessing = true;

            var qty = document.getElementById("qty_" + bookId).value;

            var isbnCell = $("#qty_" + bookId).closest("tr").find("td:first").text();
            if (isbnCell != "" && !/^\d{13}$/.test(isbnCell.trim())) {
                alert("ISBN must be exactly 13 digits");
                isProcessing = false;
                return;
            }

            if (qty == "" || parseInt(qty) <= 0 || parseInt(qty) > 9999) {
                alert("Quantity must be between 1 and 9999");
                isProcessing = false;
                return;
            }

            $.ajax({
                url: APP_CTX + "/sales/cart/add.do",
                type: "POST",
                data: { bookId: bookId, qty: qty, method: "addToCart" },
                success: function(response) {

                    $.ajax({
                        url: APP_CTX + "/sales/entry.do",
                        type: "GET",
                        data: { method: "entry" },
                        success: function(data) {

                            location.reload();
                        },
                        error: function() {
                            alert("Error refreshing page");
                            isProcessing = false;
                        }
                    });
                },
                error: function() {
                    alert("Error adding to cart. Please try again.");
                    isProcessing = false;
                }
            });
        }

        function updateQty(cartItemId) {
            var newQty = document.getElementById("cartQty_" + cartItemId).value;
            if (newQty == "" || parseInt(newQty) <= 0) {
                alert("Invalid quantity");
                return;
            }

            $.post(APP_CTX + "/sales/cart/update.do",
                { cartId: cartItemId, qty: newQty, method: "updateCart" },
                function() { location.reload(); }
            );
        }

        function removeItem(cartItemId) {
            if (!confirm("Remove this item from cart?")) return;
            $.post(APP_CTX + "/sales/cart/remove.do",
                { cartId: cartItemId, method: "removeFromCart" },
                function(result) { location.reload(); }
            );
        }

        $(document).ready(function() {

            $(".book-table tr").hover(
                function() { $(".book-table tr").css("background", ""); $(this).css("background", "#e8f0fe"); },
                function() { $(this).css("background", ""); }
            );
        });
    </script>
</head>
<body>

<jsp:include page="/includes/header.jsp" />

<div class="container">
    <div class="page-info">Sales Entry | <%= currentTime %> | User: <%= userName %></div>

    <h2>Sales Entry</h2>

    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>
    <% if (msg != null) { %><div class="msg"><%= msg %></div><% } %>

    
    
    
    <div class="search-panel">
        <form id="searchForm" action="/legacy-app/sales/entry.do" method="get">
            <input type="hidden" name="method" value="entry">
            <label>ISBN:</label>
            <input type="text" id="searchIsbn" name="isbn" size="13" value="">
            &nbsp;
            <label>Title:</label>
            <input type="text" id="searchTitle" name="title" size="20" value="">
            &nbsp;
            <label>Category:</label>
            <select id="searchCatId" name="catId">
                <option value="">-- All --</option>
                <%
                    // Scriptlet loop for category dropdown
                    if (categories != null) {
                        for (int c = 0; c < categories.size(); c++) {
                            Object catObj = categories.get(c);
                %>
                <option value="<%= c + 1 %>"><%= catObj %></option>
                <%
                        }
                    }
                %>
            </select>
            &nbsp;
            <input type="button" value="Search" class="btn" onclick="doSearch();">
            <input type="button" value="Show All" class="btn btn-secondary" onclick="document.getElementById('searchIsbn').value='';document.getElementById('searchTitle').value='';document.getElementById('searchCatId').value='';doSearch();">
        </form>
    </div>

    
    
    
    <% if (bookList != null && bookList.size() > 0) { %>
    <p style="font-size:11px; color:#666;">Found <%= bookList.size() %> books</p>
    <table class="book-table">
        <tr>
            <th>ISBN</th>
            <th>Title</th>
            <th>Publisher</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Status</th>
            <th>Qty</th>
            <th>Action</th>
        </tr>
        <%
            for (int i = 0; i < bookList.size(); i++) {
                Object bookObj = bookList.get(i);
                // Cast to Book or Map — messy type handling
                String bId = "", bIsbn = "", bTitle = "", bPublisher = "";
                String bPrice = "0.00", bStock = "0", bStatus = "";
                if (bookObj instanceof Book) {
                    Book book = (Book) bookObj;
                    bId = book.getId() != null ? book.getId().toString() : "";
                    bIsbn = book.getIsbn() != null ? book.getIsbn() : "";
                    bTitle = book.getTitle() != null ? book.getTitle() : "";
                    bPublisher = book.getPublisher() != null ? book.getPublisher() : "";
                    bPrice = String.valueOf(book.getListPrice());
                    bStock = book.getQtyInStock() != null ? book.getQtyInStock() : "0";
                    bStatus = book.getStatus() != null ? book.getStatus() : "";
                } else if (bookObj instanceof Map) {
                    Map bookMap = (Map) bookObj;
                    bId = (String) bookMap.get("id");
                    bIsbn = (String) bookMap.get("isbn");
                    bTitle = (String) bookMap.get("title");
                    bPublisher = (String) bookMap.get("publisher");
                    bPrice = (String) bookMap.get("listPrice");
                    bStock = (String) bookMap.get("qtyInStock");
                    bStatus = (String) bookMap.get("status");
                }
                // Determine stock CSS class (business logic in JSP!)
                String stockClass = "stock-ok";
                int stockNum = 0;
                try { stockNum = Integer.parseInt(bStock); } catch (Exception e) {}
                if (stockNum <= 0) stockClass = "stock-out";
                else if (stockNum <= 10) stockClass = "stock-low";
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            
            <td><%= bIsbn %></td>
            <td><%= bTitle %></td>
            <td><%= bPublisher %></td>
            <td align="right">$<%= bPrice %></td>
            <td align="center" class="<%= stockClass %>"><%= bStock %></td>
            <td><%= bStatus %></td>
            <td>
                
                <input type="hidden" id="bookId_<%= i %>" value="<%= bId %>">
                <input type="text" id="qty_<%= bId %>" class="qty-input" value="1" size="3">
            </td>
            <td>
                <% if (stockNum > 0) { %>
                    <input type="button" value="Add" class="btn-add" onclick="addToCart('<%= bId %>')">
                <% } else { %>
                    <font color="red" size="1">Out of Stock</font>
                <% } %>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <% } else { %>
        <p>No books found. Try a different search.</p>
    <% } %>

    
    
    
    <div class="cart-panel">
        <h3>Shopping Cart (<%= cartItemCount != null ? cartItemCount : "0" %> items)</h3>

        
        <input type="hidden" id="hdnCartTotal" value="<%= cartTotal != null ? cartTotal : "0" %>">
        <input type="hidden" id="hdnCartCount" value="<%= cartItemCount != null ? cartItemCount : "0" %>">
        <input type="hidden" id="hdnSessionId" value="<%= session.getId() %>">

        <% if (cartItems != null && cartItems.size() > 0) { %>
        <table class="cart-table">
            <tr>
                <th>#</th>
                <th>Book</th>
                <th>Qty</th>
                <th>Action</th>
            </tr>
            <%
                for (int ci = 0; ci < cartItems.size(); ci++) {
                    Object cartObj = cartItems.get(ci);
                    String ciId = "", ciBookId = "", ciQty = "1";
                    if (cartObj instanceof ShoppingCart) {
                        ShoppingCart sc = (ShoppingCart) cartObj;
                        ciId = sc.getId() != null ? sc.getId().toString() : "";
                        ciBookId = sc.getBookId() != null ? sc.getBookId() : "";
                        ciQty = sc.getQty() != null ? sc.getQty() : "1";
                    }
            %>
            <tr>
                <td><%= ci + 1 %></td>
                <td>Book #<%= ciBookId %></td>
                <td>
                    <input type="text" id="cartQty_<%= ciId %>" value="<%= ciQty %>" class="qty-input" size="3">
                    <input type="button" value="Update" class="btn" style="font-size:9px;padding:2px 5px;" onclick="updateQty('<%= ciId %>')">
                </td>
                <td>
                    <input type="button" value="Remove" class="btn-remove" onclick="removeItem('<%= ciId %>')">
                </td>
            </tr>
            <%
                }
            %>
        </table>

        <div class="cart-total">
            Total: $<%= cartTotal != null ? cartTotal : "0.00" %>
        </div>

        <div style="text-align:right; margin-top:10px;">
            <a href="/legacy-app/sales/checkout.do?method=checkout">
                <input type="button" value="Proceed to Checkout &raquo;" class="btn-checkout">
            </a>
        </div>
        <% } else { %>
            <p style="color:#999; font-size:12px;">Cart is empty. Search for books above and add items.</p>
        <% } %>
    </div>
</div>

<jsp:include page="/includes/footer.jsp" />

</body>
</html>
