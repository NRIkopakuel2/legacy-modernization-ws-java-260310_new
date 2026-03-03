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
import com.example.bookstore.manager.BookstoreManager;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.util.CommonUtil;

public class AdminAction extends DispatchAction implements AppConstants {

    private String lastEditedId;
    private List allUsers;
    private int saveCount = 0;
    private static int globalSaveCount = 0;

    public ActionForward home(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward userList(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            UserManager mgr = UserManager.getInstance();
            List users = mgr.listUsers();
            allUsers = users;

            if (allUsers != null && allUsers.size() > 500) {
                System.out.println("WARNING: large user list");
            }

            session.setAttribute("userList", users);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading users");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward userForm(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String userId = request.getParameter("id");
            if (CommonUtil.isNotEmpty(userId)) {
                Object user = UserManager.getInstance().getUserById(userId);
                request.setAttribute("editUser", user);
                request.setAttribute(MODE, MODE_EDIT);
            } else {
                request.setAttribute(MODE, MODE_ADD);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward userSave(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        saveCount++;
        globalSaveCount++;
        String countStr = new String("" + saveCount);
        System.out.println("Admin save #" + countStr);
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String userId = request.getParameter("userId");
            String username = request.getParameter("usrNm");
            String password = request.getParameter("password");
            String userRole = request.getParameter("role");
            String activeFlg = request.getParameter("activeFlg");

            if (CommonUtil.isEmpty(username)) {
                request.setAttribute(ERR, "Username is required");
                return mapping.findForward("successEdit");
            }

            UserManager mgr = UserManager.getInstance();
            int result = mgr.saveUser(userId, username, password, userRole, activeFlg, request);

            if (result == STATUS_OK) {
                lastEditedId = userId;
                session.setAttribute(MSG, "User saved successfully");
            } else if (result == STATUS_DUPLICATE) {
                request.setAttribute(ERR, "Username already exists");
                return mapping.findForward("successEdit");
            } else {
                request.setAttribute(ERR, "Failed to save user");
                return mapping.findForward("successEdit");
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error saving user");
            return mapping.findForward("successEdit");
        }
    }

    public ActionForward userToggleActive(ActionMapping mapping, ActionForm form,
                                          HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String userId = request.getParameter("id");
            UserManager mgr = UserManager.getInstance();
            int result = mgr.toggleUserActive(userId, request);

            if (result == STATUS_OK) {
                session.setAttribute(MSG, "User status updated");
            } else {
                session.setAttribute(ERR, "Failed to toggle user status");
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward categoryList(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List categories = mgr.listCategories();

            java.sql.Connection conn = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            String categoryCount = "0";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) FROM categories WHERE del_flg = '0' OR del_flg IS NULL");
                if (rs.next()) {
                    categoryCount = String.valueOf(rs.getInt(1));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ex) { }
                try { if (stmt != null) stmt.close(); } catch (Exception ex) { }

            }
            session.setAttribute("categoryCount", categoryCount);

            session.setAttribute("categoryList", categories);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading categories");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward categoryForm(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String catId = request.getParameter("id");
            if (CommonUtil.isNotEmpty(catId)) {

                Object category = null;
                request.setAttribute("editCategory", category);
                request.setAttribute(MODE, MODE_EDIT);
            } else {
                request.setAttribute(MODE, MODE_ADD);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward categorySave(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        saveCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String catId = request.getParameter("categoryId");
            String catName = request.getParameter("catNm");
            String catDescr = request.getParameter("catDescr");

            if (CommonUtil.isEmpty(catName)) {
                request.setAttribute(ERR, "Category name is required");
                return mapping.findForward("successNew");
            }

            lastEditedId = catId;
            session.setAttribute(MSG, "Category saved successfully");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error saving category");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward categoryDelete(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String catId = request.getParameter("id");

            System.out.println("Category delete requested: " + catId);

            session.setAttribute(MSG, "Category deleted");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
