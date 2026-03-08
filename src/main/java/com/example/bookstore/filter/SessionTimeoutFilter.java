// Session timeout filter - checks if session has expired and redirects to login
// Added by MT 2019/03 for BOOK-312
// DISABLED in web.xml: causes redirect loops when login.do itself triggers the filter
// TODO: fix by excluding login.do from filter mapping - never done
package com.example.bookstore.filter;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SessionTimeoutFilter implements Filter {

    private static final int SESSION_TIMEOUT_SECONDS = 1800; // 30 minutes
    private static final String LOGIN_PAGE = "/login.do";
    private String contextPath;

    public void init(FilterConfig filterConfig) throws ServletException {
        contextPath = filterConfig.getServletContext().getContextPath();
        System.out.println("[SessionTimeoutFilter] Initialized with contextPath=" + contextPath);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // BUG: This check uses contains() not equals(), so /login.do?expired=true
        // still matches, but /loginAdmin.do also matches — too broad
        if (requestURI.contains("login") || requestURI.contains("/css/")
                || requestURI.contains("/js/") || requestURI.contains("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            System.out.println("[SessionTimeoutFilter] No session, redirecting to login");
            // BUG: redirect loop — redirects to login.do which creates a new session,
            // but this filter runs again before login page renders
            httpResponse.sendRedirect(contextPath + LOGIN_PAGE + "?expired=true");
            return;
        }

        // Check session age
        long now = System.currentTimeMillis();
        long created = session.getCreationTime();
        long elapsed = (now - created) / 1000;

        // BUG: checks creation time, not last accessed time
        // So even active sessions get timed out after SESSION_TIMEOUT_SECONDS
        if (elapsed > SESSION_TIMEOUT_SECONDS) {
            System.out.println("[SessionTimeoutFilter] Session expired: id=" + session.getId()
                + " age=" + elapsed + "s");
            session.invalidate();
            httpResponse.sendRedirect(contextPath + LOGIN_PAGE + "?expired=true");
            return;
        }

        // Set a flag that the filter ran (for debugging redirect loops)
        session.setAttribute("_filterRan", String.valueOf(now));

        chain.doFilter(request, response);
    }

    public void destroy() {
        System.out.println("[SessionTimeoutFilter] Destroyed");
    }
}
