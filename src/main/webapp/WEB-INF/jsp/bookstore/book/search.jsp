<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.bookstore.constant.AppConstants" %>
<%@ page import="com.example.bookstore.model.Book" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%
    // Auth check — copy-pasted
    String userName = (String) session.getAttribute("user");
    if (userName == null) { response.sendRedirect("/legacy-app/login.do"); return; }

    List books = (List) session.getAttribute("books");
    List categories = (List) session.getAttribute("categories");
    String errMsg = (String) request.getAttribute("err");
%>
<html>
<head>
    <title>Book Search - Bookstore System</title>
    <jsp:include page="/includes/header.jsp" />

    
    <style>
        .search-form-panel { background: #e3f2fd; padding: 15px; border: 1px solid #90caf9; margin-bottom: 15px; }
        .search-form-panel label { font-weight: bold; font-size: 11px; display: inline-block; width: 80px; }
        .search-form-panel input[type=text] { padding: 4px; border: 1px solid #999; font-size: 11px; }
        .search-form-panel select { padding: 3px; font-size: 11px; }
        .result-table { width: 100%; border-collapse: collapse; }
        .result-table th { background: #1565c0; color: white; padding: 6px 8px; font-size: 11px; }
        .result-table td { padding: 5px 8px; font-size: 11px; border-bottom: 1px solid #e0e0e0; }
        .result-table tr:hover { background-color: #e3f2fd; }
        .result-table tr.even { background-color: #f5f5f5; }
    </style>
    
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/common.js"></script>
</head>
<body>
<div class="container">
    <h2>Book Search</h2>
    <% if (errMsg != null) { %><div class="err"><%= errMsg %></div><% } %>

    
    <div class="search-form-panel">
        <html:form action="/book/search" method="get">
            <table>
                <tr>
                    <td><label>ISBN:</label></td>
                    <td><html:text property="isbn" size="13"/></td>
                    <td><label>Title:</label></td>
                    <td><html:text property="title" size="20"/></td>
                </tr>
                <tr>
                    <td><label>Author:</label></td>
                    <td><html:text property="authorName" size="20"/></td>
                    <td><label>Category:</label></td>
                    <td>
                        
                        <select name="catId">
                            <option value="">-- All Categories --</option>
                            <%
                                // Build dropdown with scriptlet loop (should use html:options or JSTL)
                                if (categories != null) {
                                    for (int c = 0; c < categories.size(); c++) {
                                        Object catObj = categories.get(c);
                                        String catId = String.valueOf(c + 1);
                                        String catName = "";
                                        try {
                                            java.lang.reflect.Method getNm = catObj.getClass().getMethod("getCatNm", new Class[0]);
                                            catName = (String) getNm.invoke(catObj, new Object[0]);
                                        } catch (Exception e) {
                                            catName = catObj.toString();
                                        }
                            %>
                            <option value="<%= catId %>"><%= catName %></option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="4" align="right" style="padding-top:10px;">
                        <html:submit styleClass="btn" value="Search"/>
                        <html:reset styleClass="btn btn-secondary" value="Clear"/>
                    </td>
                </tr>
            </table>
        </html:form>
    </div>

    
    <% if (books != null && books.size() > 0) { %>
    <p style="font-size:11px; color:#666;">Found <%= books.size() %> books</p>
    <table class="result-table">
        <tr>
            <th>#</th>
            <th>ISBN</th>
            <th>Title</th>
            <th>Publisher</th>
            <th>Price</th>
            <th>Status</th>
            <th>Stock</th>
        </tr>
        <%
            for (int i = 0; i < books.size(); i++) {
                Object bookObj = books.get(i);
                String bIsbn = "", bTitle = "", bPublisher = "", bPrice = "0.00", bStatus = "", bStock = "0";

                if (bookObj instanceof Book) {
                    Book book = (Book) bookObj;
                    bIsbn = book.getIsbn() != null ? book.getIsbn() : "";
                    bTitle = book.getTitle() != null ? book.getTitle() : "";
                    bPublisher = book.getPublisher() != null ? book.getPublisher() : "";
                    bPrice = String.valueOf(book.getListPrice());
                    bStatus = book.getStatus() != null ? book.getStatus() : "";
                    bStock = book.getQtyInStock() != null ? book.getQtyInStock() : "0";
                } else if (bookObj instanceof Map) {
                    Map m = (Map) bookObj;
                    bIsbn = (String) m.get("isbn");
                    bTitle = (String) m.get("title");
                    bPublisher = (String) m.get("publisher");
                    bPrice = (String) m.get("listPrice");
                    bStatus = (String) m.get("status");
                    bStock = (String) m.get("qtyInStock");
                }
        %>
        <tr class="<%= i % 2 == 0 ? "" : "even" %>">
            <td><%= i + 1 %></td>
            
            <td><%= bIsbn %></td>
            <td><%= bTitle %></td>
            <td><%= bPublisher %></td>
            <td align="right">$<%= bPrice %></td>
            <td><%= bStatus %></td>
            <td align="center"><%= bStock %></td>
        </tr>
        <%
            }
        %>
    </table>
    <% } else if (books != null) { %>
        <p>No books found matching your criteria.</p>
    <% } %>

    <p><a href="/legacy-app/home.do">&laquo; Home</a></p>
</div>
<jsp:include page="/includes/footer.jsp" />
</body>
</html>
