<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.bookstore.constant.AppConstants" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%
    // Scriptlet: check for error and previous username
    String errMsg = (String) request.getAttribute("err");
    String prevUser = request.getParameter("usrNm");
    if (prevUser == null) prevUser = "";

    // Check if already logged in — redirect (business logic in JSP!)
    String currentUser = (String) session.getAttribute("user");
    if (currentUser != null && currentUser.trim().length() > 0) {
        // Hard-coded redirect
        response.sendRedirect("/legacy-app/home.do");
        return;
    }
%>
<html>
<head>
    <title>Login - Bookstore Sales Management System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    
    <link rel="stylesheet" type="text/css" href="/legacy-app/css/style.css">

    
    <style>
        body { background-color: #e8e8e8; }
        .login-container {
            width: 420px;
            margin: 80px auto;
            padding: 30px;
            background-color: white;
            border: 1px solid #aaa;
            box-shadow: 2px 2px 5px rgba(0,0,0,0.15);
        }
        .login-title {
            font-size: 22px;
            font-weight: bold;
            color: #336699;
            text-align: center;
            margin-bottom: 20px;
            border-bottom: 2px solid #336699;
            padding-bottom: 10px;
        }
        .login-error {
            color: red;
            background-color: #ffe6e6;
            border: 1px solid #ff9999;
            padding: 8px;
            margin-bottom: 15px;
            font-size: 12px;
        }
        .form-row {
            margin-bottom: 12px;
        }
        .form-row label {
            display: block;
            font-weight: bold;
            font-size: 12px;
            margin-bottom: 4px;
            color: #333;
        }
        .form-input {
            width: 100%;
            padding: 6px;
            border: 1px solid #999;
            font-size: 12px;
            box-sizing: border-box;
        }
        .btn-login {
            width: 100%;
            padding: 10px;
            background-color: #336699;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
        }
        .btn-login:hover { background-color: #224477; }
        .login-footer {
            text-align: center;
            margin-top: 15px;
            font-size: 11px;
            color: #999;
        }
        .field-error { color: red; font-size: 11px; display: none; }
    </style>

    
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/common.js"></script>

    
    <script type="text/javascript">

        var loginAttempts = 0;
        var maxAttempts = 5;

        function validateLoginForm() {
            var username = document.getElementById("usrNm").value;
            var password = document.getElementById("pwd").value;
            var hasError = false;

            $(".field-error").hide();

            if (username == null || username.trim() == "") {
                document.getElementById("usrNmError").innerHTML = "Username is required";
                document.getElementById("usrNmError").style.display = "block";
                hasError = true;
            } else if (username.length < 3) {

                document.getElementById("usrNmError").innerHTML = "Username must be at least 3 characters";
                document.getElementById("usrNmError").style.display = "block";
                hasError = true;
            } else if (username.indexOf(" ") >= 0) {

                document.getElementById("usrNmError").innerHTML = "Username cannot contain spaces";
                document.getElementById("usrNmError").style.display = "block";
                hasError = true;
            }

            if (password == null || password.trim() == "") {
                document.getElementById("pwdError").innerHTML = "Password is required";
                document.getElementById("pwdError").style.display = "block";
                hasError = true;
            } else if (password.length < 6) {

                document.getElementById("pwdError").innerHTML = "Password must be at least 6 characters";
                document.getElementById("pwdError").style.display = "block";
                hasError = true;
            } else if (!/\d/.test(password)) {

                document.getElementById("pwdError").innerHTML = "Password must contain at least one number";
                document.getElementById("pwdError").style.display = "block";
                hasError = true;
            }

            if (hasError) {
                return false;
            }

            loginAttempts++;
            if (loginAttempts >= maxAttempts) {
                alert("Too many login attempts. Please try again later.");
                return false;
            }

            return true;
        }

        function checkEnterKey(e) {
            var keyCode = window.event ? window.event.keyCode : e.which;
            if (keyCode == 13) {
                document.forms[0].submit();
            }
        }

        $(document).ready(function() {
            document.getElementById("usrNm").focus();

            $("input[type='text'], input[type='password']").on("focus", function() {
                $(this).css("border-color", "#336699");
                $(this).css("background-color", "#f0f8ff");
            }).on("blur", function() {
                $(this).css("border-color", "#999");
                $(this).css("background-color", "white");
            });
        });
    </script>
</head>
<body>

<div class="login-container">
    <div class="login-title">
        Bookstore System Login
    </div>

    
    <% if (errMsg != null && errMsg.length() > 0) { %>
        <div class="login-error">
            <%= errMsg %>
        </div>
    <% } %>

    
    <html:form action="/login" method="post" onsubmit="return validateLoginForm();">
        
        <input type="hidden" name="method" value="login">

        <div class="form-row">
            <label for="usrNm">Username:</label>
            <input type="text" name="usrNm" id="usrNm" class="form-input"
                   value="<%= prevUser %>" onkeypress="checkEnterKey(event)">
            <div id="usrNmError" class="field-error"></div>
        </div>

        <div class="form-row">
            <label for="pwd">Password:</label>
            <input type="password" name="pwd" id="pwd" class="form-input"
                   onkeypress="checkEnterKey(event)">
            <div id="pwdError" class="field-error"></div>
        </div>

        <div class="form-row">
            <label style="font-weight:normal; font-size:11px;">
                <input type="checkbox" name="rememberMe" value="1"> Remember me
            </label>
        </div>

        <div class="form-row">
            <input type="submit" value="Login" class="btn-login">
        </div>
    </html:form>

    <script>document.write("<small>Client time: " + new Date().toLocaleString() + "</small>");</script>

    <div class="login-footer">
        
        <a href="/legacy-app/index.jsp">Back to Home</a>
        <br><br>
        <font size="1" color="#cccccc">Bookstore System v1.0.0</font>
        <br>
        <font size="1" color="#cccccc">&copy; 2005 Example Bookstore Corp.</font>
    </div>
</div>

</body>
</html>
