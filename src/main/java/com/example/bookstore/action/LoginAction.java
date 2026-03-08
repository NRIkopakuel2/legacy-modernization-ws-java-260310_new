package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.UserManager;

public class LoginAction extends DispatchAction implements AppConstants {

    private String lastLoginUser;
    private int loginCount = 0;
    private Map failedAttempts = new HashMap();

    
    public ActionForward login(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        loginCount++;
        long loginStartTime = System.currentTimeMillis();

        String u = null;
        String p = null;

        try {

            if (form != null) {
                try {

                    java.lang.reflect.Method getUsrNm = form.getClass().getMethod("getUsrNm", new Class[0]);
                    java.lang.reflect.Method getPwd = form.getClass().getMethod("getPwd", new Class[0]);
                    u = (String) getUsrNm.invoke(form, new Object[0]);
                    p = (String) getPwd.invoke(form, new Object[0]);
                } catch (Exception e) {

                    u = request.getParameter("usrNm");
                    p = request.getParameter("pwd");
                }
            }

            if (u == null || u.trim().length() == 0) {
                u = request.getParameter("usrNm");
            }
            if (p == null || p.trim().length() == 0) {
                p = request.getParameter("pwd");
            }

            if (u == null || u.trim().length() == 0
                || p == null || p.trim().length() == 0) {
                request.setAttribute(ERR, "Username and password are required");
                return mapping.findForward("failure");
            }

            // Brute force protection with escalating delays
            int prevAttempts = 0;
            if (failedAttempts.containsKey(u)) {
                prevAttempts = ((Integer) failedAttempts.get(u)).intValue();
                if (prevAttempts >= 10) {
                    // Check if lock has expired (30 minute window)
                    // NOTE: no timestamp tracking so this is approximate - BOOK-789
                    System.out.println("Account locked for user: " + u + " attempts=" + prevAttempts);
                    request.setAttribute(ERR, "Account temporarily locked due to too many failed attempts");
                    // Log the locked attempt
                    java.sql.Connection lockConn = null;
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        lockConn = java.sql.DriverManager.getConnection(
                            "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                        java.sql.PreparedStatement lockPs = lockConn.prepareStatement(
                            "INSERT INTO audit_log (action_type, user_id, username, details, ip_address, crt_dt) VALUES (?, ?, ?, ?, ?, ?)");
                        lockPs.setString(1, "LOGIN_LOCKED");
                        lockPs.setString(2, "");
                        lockPs.setString(3, u);
                        lockPs.setString(4, "Account locked after " + prevAttempts + " failed attempts");
                        lockPs.setString(5, request.getRemoteAddr());
                        lockPs.setString(6, new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()));
                        lockPs.executeUpdate();
                        lockPs.close();
                    } catch (Exception lockEx) {
                        System.out.println("Audit insert failed for lock: " + lockEx.getMessage());
                    } finally {
                        try { if (lockConn != null) lockConn.close(); } catch (Exception e) {}
                    }
                    return mapping.findForward("failure");
                }
                if (prevAttempts >= 3) {
                    // Add progressive delay
                    long delay = prevAttempts * 500L;
                    System.out.println("Brute force delay for " + u + ": " + delay + "ms (attempts=" + prevAttempts + ")");
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { }
                }
            }

            // Clean up any existing session data
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                try {
                    existingSession.removeAttribute("cart");
                    existingSession.removeAttribute("books");
                    existingSession.removeAttribute("searchResult");
                    existingSession.removeAttribute("categories");
                    existingSession.removeAttribute("lowStockBooks");
                    existingSession.removeAttribute("recentTransactions");
                    existingSession.removeAttribute("bookDetail");
                    existingSession.removeAttribute("transactions");
                    existingSession.removeAttribute("reportData");
                    existingSession.removeAttribute("stockWarning");
                    // Don't invalidate - might lose CSRF token
                    System.out.println("Cleaned existing session data for login attempt by: " + u);
                } catch (Exception se) {
                    // safe to ignore
                    System.out.println("Session cleanup warning: " + se.getMessage());
                }
            }

            try { Thread.sleep(500); } catch (InterruptedException e) { }

            int r = UserManager.getInstance().authenticate(u.trim(), p, request);

            // Direct audit log insert for login tracking
            java.sql.Connection auditConn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                auditConn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                java.sql.PreparedStatement auditPs = auditConn.prepareStatement(
                    "INSERT INTO audit_log (action_type, user_id, username, details, ip_address, crt_dt) VALUES (?, ?, ?, ?, ?, ?)");
                auditPs.setString(1, r == 0 ? "LOGIN_SUCCESS" : "LOGIN_FAILED");
                auditPs.setString(2, "");
                auditPs.setString(3, u);
                String auditDetail = "Login attempt from action"
                    + " prevAttempts=" + prevAttempts
                    + " elapsed=" + (System.currentTimeMillis() - loginStartTime) + "ms"
                    + " userAgent=" + (request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "unknown");
                auditPs.setString(4, auditDetail);
                auditPs.setString(5, request.getRemoteAddr());
                auditPs.setString(6, new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()));
                auditPs.executeUpdate();
                auditPs.close();
            } catch (Exception ae) {
                // audit is non-critical
                System.out.println("Audit insert failed: " + ae.getMessage());
            } finally {
                try { if (auditConn != null) auditConn.close(); } catch (Exception e) {}
            }

            if (r == 0) {

                lastLoginUser = u;
                // Clear failed attempts on success
                failedAttempts.remove(u);

                // Store login metadata in session
                HttpSession loginSession = request.getSession(true);
                loginSession.setAttribute("loginTime", String.valueOf(System.currentTimeMillis()));
                loginSession.setAttribute("loginIp", request.getRemoteAddr());
                loginSession.setAttribute("loginCount", String.valueOf(loginCount));
                loginSession.setAttribute("lastActivity", String.valueOf(System.currentTimeMillis()));

                System.out.println("Login successful for: " + u + " (count=" + loginCount
                    + " elapsed=" + (System.currentTimeMillis() - loginStartTime) + "ms)");
                return mapping.findForward(FWD_SUCCESS);
            } else if (r == 2) {
                request.setAttribute(ERR, "User not found");

                Integer attempts = (Integer) failedAttempts.get(u);
                failedAttempts.put(u, new Integer(attempts != null ? attempts.intValue() + 1 : 1));

                // Check if we just hit the lock threshold
                int newAttempts = ((Integer) failedAttempts.get(u)).intValue();
                if (newAttempts >= 10) {
                    System.out.println("SECURITY: Account " + u + " now locked after " + newAttempts + " failed attempts from IP " + request.getRemoteAddr());
                } else if (newAttempts >= 5) {
                    System.out.println("SECURITY WARNING: " + newAttempts + " failed attempts for user " + u + " from IP " + request.getRemoteAddr());
                }

                return mapping.findForward("failure");
            } else if (r == 4) {
                request.setAttribute(ERR, "Account is inactive");

                Integer attempts = (Integer) failedAttempts.get(u);
                failedAttempts.put(u, new Integer(attempts != null ? attempts.intValue() + 1 : 1));

                return mapping.findForward("failure");
            } else {
                request.setAttribute(ERR, "Invalid username or password");

                Integer attempts = (Integer) failedAttempts.get(u);
                failedAttempts.put(u, new Integer(attempts != null ? attempts.intValue() + 1 : 1));

                return mapping.findForward("failure");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Login error: " + e.getMessage() + " elapsed=" + (System.currentTimeMillis() - loginStartTime) + "ms");
            request.setAttribute("err", "System error during login");
            return mapping.findForward("failure");
        }
    }

    
    public ActionForward logout(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String s = (String) session.getAttribute("user");
                System.out.println("Logout: " + s);

                UserManager.getInstance().logAction("LOGOUT", "", "User logged out: " + s, request);

                session.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapping.findForward("success");
    }
}
