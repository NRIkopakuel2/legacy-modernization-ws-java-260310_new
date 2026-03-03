package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
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
        String username = null;
        String password = null;

        try {

            if (form != null) {
                try {

                    java.lang.reflect.Method getUsrNm = form.getClass().getMethod("getUsrNm", new Class[0]);
                    java.lang.reflect.Method getPwd = form.getClass().getMethod("getPwd", new Class[0]);
                    username = (String) getUsrNm.invoke(form, new Object[0]);
                    password = (String) getPwd.invoke(form, new Object[0]);
                } catch (Exception e) {

                    username = request.getParameter("usrNm");
                    password = request.getParameter("pwd");
                }
            }

            if (username == null || username.trim().length() == 0) {
                username = request.getParameter("usrNm");
            }
            if (password == null || password.trim().length() == 0) {
                password = request.getParameter("pwd");
            }

            if (username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0) {
                request.setAttribute(ERR, "Username and password are required");
                return mapping.findForward("failure");
            }

            try { Thread.sleep(500); } catch (InterruptedException e) { }

            int result = UserManager.getInstance().authenticate(username.trim(), password, request);

            if (result == STATUS_OK) {

                lastLoginUser = username;

                System.out.println("Login successful for: " + username + " (count=" + loginCount + ")");
                return mapping.findForward(FWD_SUCCESS);
            } else if (result == STATUS_NOT_FOUND) {
                request.setAttribute(ERR, "User not found");

                Integer attempts = (Integer) failedAttempts.get(username);
                failedAttempts.put(username, new Integer(attempts != null ? attempts.intValue() + 1 : 1));
                return mapping.findForward("failure");
            } else if (result == STATUS_UNAUTHORIZED) {
                request.setAttribute(ERR, "Account is inactive");
                return mapping.findForward("failure");
            } else {
                request.setAttribute(ERR, "Invalid username or password");
                return mapping.findForward("failure");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Login error: " + e.getMessage());
            request.setAttribute(ERR, "System error during login");
            return mapping.findForward("failure");
        }
    }

    
    public ActionForward logout(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String username = (String) session.getAttribute(USER);
                System.out.println("Logout: " + username);

                UserManager.getInstance().logAction("LOGOUT", "", "User logged out: " + username, request);

                session.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapping.findForward(FWD_SUCCESS);
    }
}
