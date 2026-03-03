package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.AuditLogDAO;
import com.example.bookstore.dao.CustomerDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.dao.impl.AuditLogDAOImpl;
import com.example.bookstore.dao.impl.CustomerDAOImpl;
import com.example.bookstore.dao.impl.UserDAOImpl;
import com.example.bookstore.manager.BookstoreManager;
import com.example.bookstore.model.AuditLog;
import com.example.bookstore.model.Customer;
import com.example.bookstore.model.User;
import com.example.bookstore.util.CommonUtil;

public class UserManager implements AppConstants {

    private static UserManager instance = new UserManager();

    private UserDAO userDAO = new UserDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    private String lastAuthUser;
    private Map loginCache = new HashMap();
    private int loginAttemptCount = 0;
    private int successCount = 0;

    private UserManager() {
    }

    public static UserManager getInstance() {
        return instance;
    }

    
    public int authenticate(String username, String password, HttpServletRequest request) {
        loginAttemptCount++;
        if (loginAttemptCount > 5) {
            System.out.println("WARNING: Too many login attempts");
        }

        try {

            try { Thread.sleep(1000); } catch (InterruptedException e) { }

            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return STATUS_ERR;
            }

            Object userObj = userDAO.findByUsername(username);
            if (userObj == null) {
                System.out.println("Login failed: user not found: " + username);
                return STATUS_NOT_FOUND;
            }

            User user = (User) userObj;

            if (!FLG_ON.equals(user.getActiveFlg())) {
                System.out.println("Login failed: user inactive: " + username);
                return STATUS_UNAUTHORIZED;
            }

            String hashedPassword = CommonUtil.md5Hash(password);
            if (!hashedPassword.equals(user.getPwdHash())) {
                System.out.println("Login failed: wrong password for: " + username);
                return STATUS_ERR;
            }

            if (request != null) {
                HttpSession session = request.getSession();

                session.setAttribute(USER, username);
                session.setAttribute(ROLE, user.getRole());
                session.setAttribute(LOGIN_TIME, CommonUtil.getCurrentDateTimeStr());
            }

            lastAuthUser = username;
            loginCache.put(username, CommonUtil.getCurrentDateTimeStr());
            successCount++;

            logAction("LOGIN_SUCCESS", user.getId() != null ? user.getId().toString() : "",
                      "User logged in: " + username, request);

            System.out.println("Login successful: " + username + " role=" + user.getRole());

            try { BookstoreManager.getInstance().clearCache(); } catch (Exception ex) {  }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int createUser(String username, String password, String role,
                          HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return STATUS_ERR;
            }

            Object existing = userDAO.findByUsername(username);
            if (existing != null) {
                return STATUS_DUPLICATE;
            }

            if (CommonUtil.isEmpty(role)) {
                role = ROLE_CLERK;
            }

            User user = new User();
            user.setUsrNm(username);
            user.setPwdHash(CommonUtil.md5Hash(password));
            user.setSalt("");
            user.setRole(role);
            user.setActiveFlg(FLG_ON);
            user.setCrtDt(CommonUtil.getCurrentDateStr());
            user.setUpdDt(CommonUtil.getCurrentDateStr());

            int result = userDAO.save(user);
            if (result == STATUS_OK) {
                logAction("USER_CREATED", "", "User created: " + username, request);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int changePassword(String username, String oldPassword, String newPassword) {
        try {
            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(oldPassword)
                || CommonUtil.isEmpty(newPassword)) {
                return STATUS_ERR;
            }

            Object userObj = userDAO.findByUsername(username);
            if (userObj == null) {
                return STATUS_NOT_FOUND;
            }

            User user = (User) userObj;

            String oldHash = CommonUtil.md5Hash(oldPassword);
            if (!oldHash.equals(user.getPwdHash())) {
                return STATUS_ERR;
            }

            user.setPwdHash(CommonUtil.md5Hash(newPassword));
            user.setUpdDt(CommonUtil.getCurrentDateStr());

            return userDAO.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int registerCustomer(String email, String password, String firstName,
                                String lastName, String phone, String dob,
                                String status, HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(email) || CommonUtil.isEmpty(password)) {
                return STATUS_ERR;
            }

            if (customerDAO.emailExists(email)) {
                return STATUS_DUPLICATE;
            }

            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setPwdHash(CommonUtil.md5Hash(password));
            customer.setSalt("");
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPhone(phone);
            customer.setDob(dob);
            customer.setStatus(CommonUtil.isEmpty(status) ? STS_ACTIVE : status);
            customer.setDelFlg(FLG_OFF);
            customer.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            customer.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = customerDAO.save(customer);
            if (result == STATUS_OK) {
                logAction("CUSTOMER_REGISTERED", "",
                          "Customer registered: " + email, request);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int authenticateCustomer(String email, String password, HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(email) || CommonUtil.isEmpty(password)) {
                return STATUS_ERR;
            }

            Object custObj = customerDAO.findByEmail(email);
            if (custObj == null) {
                return STATUS_NOT_FOUND;
            }

            Customer customer = (Customer) custObj;

            if (!STS_ACTIVE.equals(customer.getStatus())) {
                return STATUS_UNAUTHORIZED;
            }

            String hashedPassword = CommonUtil.md5Hash(password);
            if (!hashedPassword.equals(customer.getPwdHash())) {
                return STATUS_ERR;
            }

            if (request != null) {
                HttpSession session = request.getSession();
                session.setAttribute("customer", customer);
                session.setAttribute("customerEmail", email);
            }

            logAction("CUSTOMER_LOGIN", "",
                      "Customer logged in: " + email, request);

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public Object findCustomerByEmail(String email) {
        return customerDAO.findByEmail(email);
    }

    
    public List searchCustomers(String keyword) {
        return customerDAO.searchByName(keyword);
    }

    
    public List listUsers() {
        return userDAO.listAll();
    }

    
    public Object getUserById(String id) {
        return userDAO.findById(id);
    }

    
    public int saveUser(String id, String username, String password, String role,
                        String activeFlg, HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(username)) {
                return STATUS_ERR;
            }

            User user;
            if (CommonUtil.isNotEmpty(id)) {

                Object existing = userDAO.findById(id);
                if (existing == null) {
                    return STATUS_NOT_FOUND;
                }
                user = (User) existing;
                user.setUsrNm(username);
                if (CommonUtil.isNotEmpty(password)) {
                    user.setPwdHash(CommonUtil.md5Hash(password));
                }
                user.setRole(role);
                user.setActiveFlg(activeFlg);
                user.setUpdDt(CommonUtil.getCurrentDateStr());
            } else {

                Object dup = userDAO.findByUsername(username);
                if (dup != null) {
                    return STATUS_DUPLICATE;
                }
                user = new User();
                user.setUsrNm(username);
                user.setPwdHash(CommonUtil.md5Hash(password));
                user.setSalt("");
                user.setRole(role);
                user.setActiveFlg(CommonUtil.isEmpty(activeFlg) ? FLG_ON : activeFlg);
                user.setCrtDt(CommonUtil.getCurrentDateStr());
                user.setUpdDt(CommonUtil.getCurrentDateStr());
            }

            int result = userDAO.save(user);
            if (result == STATUS_OK) {
                logAction("USER_SAVED", id != null ? id : "",
                          "User saved: " + username, request);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int toggleUserActive(String userId, HttpServletRequest request) {
        try {
            Object userObj = userDAO.findById(userId);
            if (userObj == null) {
                return STATUS_NOT_FOUND;
            }

            User user = (User) userObj;
            if (FLG_ON.equals(user.getActiveFlg())) {
                user.setActiveFlg(FLG_OFF);
            } else {
                user.setActiveFlg(FLG_ON);
            }
            user.setUpdDt(CommonUtil.getCurrentDateStr());

            int result = userDAO.save(user);
            if (result == STATUS_OK) {
                logAction("USER_TOGGLE_ACTIVE", userId,
                          "User active toggled: " + user.getUsrNm(), request);

                try { BookstoreManager.getInstance().clearCache(); } catch (Exception ex) {  }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public void logAction(String actionType, String userId, String details,
                          HttpServletRequest request) {
        try {
            String username = "";
            String ipAddress = "";
            String userAgent = "";

            if (request != null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    username = (String) session.getAttribute(USER);
                    if (username == null) username = "";
                }
                ipAddress = request.getRemoteAddr();
                userAgent = request.getHeader("User-Agent");
            }

            AuditLog log = new AuditLog(
                actionType,
                userId,
                username,
                "",
                "",
                details,
                ipAddress != null ? ipAddress : "",
                userAgent != null ? userAgent : "",
                CommonUtil.getCurrentDateTimeStr()
            );

            auditLogDAO.save(log);

            System.out.println("[AUDIT] " + CommonUtil.getCurrentDateTimeStr()
                + " action=" + actionType + " user=" + username + " detail=" + details);
        } catch (Exception e) {

            System.err.println("Audit logging failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public void logAction(String actionType, String userId, String details) {
        logAction(actionType, userId, details, null);
    }

    
    public List getAuditLogs(String startDate, String endDate, String actionType,
                             String userId, String entityType, String searchText, String page) {
        return auditLogDAO.findByFilters(startDate, endDate, actionType,
                                         userId, entityType, searchText, page);
    }

    
    public String countAuditLogs(String startDate, String endDate, String actionType,
                                 String userId, String entityType, String searchText) {
        return auditLogDAO.countByFilters(startDate, endDate, actionType,
                                          userId, entityType, searchText);
    }

    
    public boolean hasRole(String userRole, String requiredRole) {
        if (userRole == null || requiredRole == null) return false;
        if (ROLE_ADMIN.equals(userRole)) return true;
        if (ROLE_MANAGER.equals(requiredRole) && ROLE_MANAGER.equals(userRole)) return true;
        if (ROLE_CLERK.equals(requiredRole)) return true;
        return false;
    }

    
    public int getMaxAdjustment(String role) {
        if (ROLE_ADMIN.equals(role)) return 9999;
        if (ROLE_MANAGER.equals(role)) return 100;
        return 0;
    }

    public int purgeInactiveUsers(int daysInactive) { return 0; }

    public int resetAllPasswords() { return STATUS_ERR; }

    public void migrateUserRoles() { System.out.println("migrateUserRoles - not implemented"); }
}
