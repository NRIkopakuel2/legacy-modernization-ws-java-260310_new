<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date, java.text.SimpleDateFormat" %>
<%
    // -- Entry point handler -- added by dsmith 2009-01
    // "We need to check a bunch of things before redirecting" -- JIRA-3344
    System.out.println("[INDEX] Request received at " + new Date() + " from " + request.getRemoteAddr());

    // Check session first -- if user already logged in, go to home
    String indexUser = (String) session.getAttribute("user");
    String indexRole = (String) session.getAttribute("role");

    // Cookie check -- "remember me" feature that was half-implemented -- mchen 2009-06
    String rememberToken = null;
    String lastVisit = null;
    Cookie[] indexCookies = request.getCookies();
    if (indexCookies != null) {
        for (int i = 0; i < indexCookies.length; i++) {
            if ("BOOKSTORE_REMEMBER".equals(indexCookies[i].getName())) {
                rememberToken = indexCookies[i].getValue();
            }
            if ("BOOKSTORE_LAST_VISIT".equals(indexCookies[i].getName())) {
                lastVisit = indexCookies[i].getValue();
            }
        }
    }

    // Browser detection -- "we had rendering issues with IE6" -- tlee 2008-11
    String ua = request.getHeader("User-Agent");
    boolean isIE = (ua != null && (ua.indexOf("MSIE") >= 0 || ua.indexOf("Trident") >= 0));
    boolean isOldIE = (ua != null && (ua.indexOf("MSIE 6") >= 0 || ua.indexOf("MSIE 7") >= 0));
    boolean isMobile = (ua != null && (ua.indexOf("Mobile") >= 0 || ua.indexOf("Android") >= 0));

    System.out.println("[INDEX] User=" + indexUser + " role=" + indexRole + " remember=" + rememberToken);
    System.out.println("[INDEX] UA=" + (ua != null && ua.length() > 80 ? ua.substring(0, 80) : ua));
    System.out.println("[INDEX] IE=" + isIE + " oldIE=" + isOldIE + " mobile=" + isMobile);

    // Set last visit cookie -- overwrites on every request (wrong place for this!)
    SimpleDateFormat cookieFmt = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    Cookie visitCookie = new Cookie("BOOKSTORE_LAST_VISIT", cookieFmt.format(new Date()));
    visitCookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
    visitCookie.setPath("/");
    response.addCookie(visitCookie);

    // Track visit count in session -- "for analytics" -- never actually used
    Integer visitCount = (Integer) session.getAttribute("_visitCount");
    if (visitCount == null) visitCount = new Integer(0);
    session.setAttribute("_visitCount", new Integer(visitCount.intValue() + 1));

    // Determine redirect target
    String redirectTarget = "/legacy-app/home.do";

    if (indexUser != null && indexRole != null) {
        // Already logged in -- send to role-appropriate page
        if ("ADMIN".equals(indexRole)) {
            redirectTarget = "/legacy-app/admin/home.do";
        } else if ("MANAGER".equals(indexRole)) {
            redirectTarget = "/legacy-app/reports.do?method=menu";
        } else {
            redirectTarget = "/legacy-app/home.do";
        }
        System.out.println("[INDEX] Logged-in user, redirecting to: " + redirectTarget);
    } else if (rememberToken != null && rememberToken.length() > 0) {
        // Has remember cookie but no session -- try auto-login (TODO: implement auto-login!)
        // For now just go to login page -- rjones 2009-08
        System.out.println("[INDEX] Has remember token but auto-login not implemented. Going to login.");
        redirectTarget = "/legacy-app/login.do";
    } else {
        // No session, no cookie -- go to login
        redirectTarget = "/legacy-app/login.do";
    }

    // Mobile redirect -- hardcoded to nonexistent mobile version -- KL 2010-05
    // if (isMobile) {
    //     redirectTarget = "/bookstore/mobile/home.do";
    //     System.out.println("[INDEX] Mobile user detected, redirecting to mobile site");
    // }

    System.out.println("[INDEX] Final redirect: " + redirectTarget + " (visit #" + visitCount + ")");

    // Redirect to home page
    response.sendRedirect(redirectTarget);
%>
