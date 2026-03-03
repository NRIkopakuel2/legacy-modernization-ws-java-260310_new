<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.PrintWriter" %>
<%
    String errMsg = (String) request.getAttribute("err");
    if (errMsg == null) errMsg = (String) request.getAttribute("javax.servlet.error.message");
    Exception ex = (Exception) request.getAttribute("exception");
    if (ex == null) ex = (Exception) request.getAttribute("javax.servlet.error.exception");
%>
<html>
<head>
    <title>Error - Bookstore System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">
</head>
<body>

<table width="100%" class="header-bar" cellpadding="0" cellspacing="0">
<tr>
    <td><font size="3" color="white"><b>Bookstore System</b></font></td>
    <td align="right"><a href="/legacy-app/home.do"><font color="white">Home</font></a></td>
</tr>
</table>

<div class="container">
    <h2>System Error</h2>

    <% if (errMsg != null) { %>
        <div class="err">
            <p><%= errMsg %></p>
        </div>
    <% } else { %>
        <div class="err">
            <p>An unexpected error occurred.</p>
        </div>
    <% } %>

    
    <% if (ex != null) { %>
        <h3>Error Details:</h3>
        <p><b>Exception:</b> <%= ex.getClass().getName() %>: <%= ex.getMessage() %></p>
        <h3>Stack Trace:</h3>
        <pre style="background-color: #f0f0f0; padding: 10px; border: 1px solid #ccc; overflow: auto; font-size: 10px;"><%
            ex.printStackTrace(new PrintWriter(out));
        %></pre>
    <% } %>

    <br>
    <p><a href="/legacy-app/home.do">&laquo; Back to Home</a></p>
    <p><a href="/legacy-app/login.do">&laquo; Back to Login</a></p>
</div>

<hr>
<div class="footer">
    <font size="1" color="#999999">Copyright &copy; 2005 Example Bookstore Corp.</font>
</div>

</body>
</html>
