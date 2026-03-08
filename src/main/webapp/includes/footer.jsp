<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date, java.text.SimpleDateFormat" %>
<%
    // -- footer debug -- added by dsmith 2009-03-14
    System.out.println("[FOOTER] Page rendered at: " + new Date() + " session=" + session.getId());
    System.out.println("[FOOTER] Server: " + application.getServerInfo() + " | JVM: " + System.getProperty("java.version"));

    // Cache the formatted date (not thread-safe but who cares -- KL 2011-06)
    SimpleDateFormat footerDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    String renderedAt = footerDateFmt.format(new Date());
%>
<br>
<hr>
<!-- TODO: remove debug info before go-live -- jparker 2008-11 -->
<div class="footer" style="background-color:#f0f0f0; padding:8px; border-top:2px solid #999; font-family:Verdana,sans-serif;">
    <font size="1" color="#999999">
        Copyright &copy; 2005-2019 Example Bookstore Corp. All rights reserved.
        <br>
        Bookstore Sales Management System v1.0.0
    </font>
    <br>
    <font size="1" color="#aaaaaa">
        Page generated: <%= renderedAt %> |
        Server: <%= application.getServerInfo() %> |
        JVM: <%= System.getProperty("java.version") %> |
        OS: <%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %>
    </font>
    <br>
    <!-- server diagnostics -- REMOVE BEFORE PROD (added by mchen 2012-01-20) -->
    <font size="1" color="#cccccc">
        Free Mem: <%= Runtime.getRuntime().freeMemory() / 1024 %>KB |
        Total Mem: <%= Runtime.getRuntime().totalMemory() / 1024 %>KB |
        Session: <%= session.getId().substring(0, Math.min(session.getId().length(), 16)) %>...
    </font>
</div>

<%-- Analytics snippet -- added by marketing team 2010-07 (tracking ID is probably wrong) --%>
<script type="text/javascript">
    // Google Analytics - old UA tracking code (deprecated)
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-XXXXX-1']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();
    // Also log to console for debugging -- remove later
    if (typeof console !== 'undefined') {
        console.log("Footer loaded. Server time: <%= renderedAt %>");
        console.log("Session fragment: <%= session.getId().substring(0, Math.min(session.getId().length(), 8)) %>");
    }
</script>
