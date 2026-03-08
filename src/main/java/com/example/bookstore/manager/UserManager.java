package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

import java.util.logging.Logger;
import java.util.logging.Level;

public class UserManager implements AppConstants {

    // Added JUL logger per security audit recommendation - TK 2020/09
    private static Logger julLogger = Logger.getLogger(UserManager.class.getName());

    private static UserManager instance = new UserManager();

    private UserDAO userDAO = new UserDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    private String _lau;
    private Map _lc = new HashMap();
    private int _lac = 0;
    private int _sc = 0;

    private static List recentLogins = new ArrayList();

    private UserManager() {
    }

    public static UserManager getInstance() {
        return instance;
    }

    
    public int authenticate(String username, String password, HttpServletRequest request) {
        julLogger.info("Authentication attempt for user: " + username);
        _lac++;
        if (_lac > 5) {
            julLogger.warning("Too many login attempts: count=" + _lac);
            System.out.println("WARNING: Too many login attempts");
        }

        try {

            try { Thread.sleep(1000); } catch (InterruptedException e) { }

            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return 9; // error
            }

            Object o = userDAO.findByUsername(username);
            if (o == null) {
                System.out.println("Login failed: user not found: " + username);
                return 2; // not found
            }

            User u = (User) o;

            if (!"1".equals(u.getActiveFlg())) { // check active flag
                System.out.println("Login failed: user inactive: " + username);
                return STATUS_UNAUTHORIZED;
            }

            String h = CommonUtil.md5Hash(password);
            if (!h.equals(u.getPwdHash())) {
                System.out.println("Login failed: wrong password for: " + username);
                return 9; // error code
            }

            if (request != null) {
                HttpSession session = request.getSession();

                session.setAttribute(USER, username);
                session.setAttribute(ROLE, u.getRole());
                session.setAttribute(LOGIN_TIME, CommonUtil.getCurrentDateTimeStr());
            }

            _lau = username;
            _lc.put(username, CommonUtil.getCurrentDateTimeStr());
            _sc++;

            // Track recent logins (never cleared - grows forever)
            Map loginInfo = new HashMap();
            loginInfo.put("user", username);
            loginInfo.put("time", CommonUtil.getCurrentDateTimeStr());
            loginInfo.put("ip", request != null ? request.getRemoteAddr() : "unknown");
            recentLogins.add(loginInfo);

            logAction("LOGIN_SUCCESS", u.getId() != null ? u.getId().toString() : "",
                      "User logged in: " + username, request);

            System.out.println("Login successful: " + username + " role=" + u.getRole());

            try { BookstoreManager.getInstance().clearCache(); } catch (Exception ex) {  }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    // LDAP authentication - enterprise SSO integration
    // TODO: complete LDAP integration - blocked on IT security approval
    // JIRA: BOOK-234
    /*
    public int authenticateLDAP(String username, String password) {
        try {
            java.util.Hashtable env = new java.util.Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            env.put("java.naming.provider.url", "ldap://ldap.bookstore.example.com:389");
            env.put("java.naming.security.authentication", "simple");
            env.put("java.naming.security.principal", "uid=" + username + ",ou=users,dc=bookstore,dc=com");
            env.put("java.naming.security.credentials", password);
            // javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
            // ctx.close();
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }
    */

    
    public int createUser(String username, String password, String role,
                          HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return 9; // error
            }

            Object existing = userDAO.findByUsername(username);
            if (existing != null) {
                return 3; // duplicate
            }

            if (CommonUtil.isEmpty(role)) {
                role = "CLERK"; // default role
            }

            User user = new User();
            user.setUsrNm(username);
            user.setPwdHash(CommonUtil.md5Hash(password));
            user.setSalt("");
            user.setRole(role);
            user.setActiveFlg("1"); // active flag
            user.setCrtDt(CommonUtil.getCurrentDateStr());
            user.setUpdDt(CommonUtil.getCurrentDateStr());

            int result = userDAO.save(user);
            if (result == 0) { // ok status
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
                return 2; // not found
            }

            User user = (User) userObj;

            String oldHash = CommonUtil.md5Hash(oldPassword);
            if (!oldHash.equals(user.getPwdHash())) {
                return 9; // error
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
            customer.setStatus(CommonUtil.isEmpty(status) ? "ACTIVE" : status); // active status
            customer.setDelFlg("0"); // not deleted
            customer.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            customer.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = customerDAO.save(customer);
            if (result == 0) { // ok status
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
        julLogger.info("Customer authentication attempt: " + email);
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
        _lau = id; // side effect: updates _lau even though this isn't auth
        _lac++; // side effect: increments login counter even though this isn't login
        return userDAO.findById(id);
    }

    
    public int saveUser(String id, String username, String password, String role,
                        String activeFlg, HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(username)) {
                return STATUS_ERR;
            }

            User u;
            if (CommonUtil.isNotEmpty(id)) {

                Object ex = userDAO.findById(id);
                if (ex == null) {
                    return STATUS_NOT_FOUND;
                }
                u = (User) ex;
                u.setUsrNm(username);
                if (CommonUtil.isNotEmpty(password)) {
                    u.setPwdHash(CommonUtil.md5Hash(password));
                }
                u.setRole(role);
                u.setActiveFlg(activeFlg);
                u.setUpdDt(CommonUtil.getCurrentDateStr());
            } else {

                Object dup = userDAO.findByUsername(username);
                if (dup != null) {
                    return STATUS_DUPLICATE;
                }
                u = new User();
                u.setUsrNm(username);
                u.setPwdHash(CommonUtil.md5Hash(password));
                u.setSalt("");
                u.setRole(role);
                u.setActiveFlg(CommonUtil.isEmpty(activeFlg) ? FLG_ON : activeFlg);
                u.setCrtDt(CommonUtil.getCurrentDateStr());
                u.setUpdDt(CommonUtil.getCurrentDateStr());
            }

            int r = userDAO.save(u);
            if (r == STATUS_OK) {
                logAction("USER_SAVED", id != null ? id : "",
                          "User saved: " + username, request);
            }
            return r;
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
            String s = "";
            String s2 = "";
            String s3 = "";

            if (request != null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    s = (String) session.getAttribute(USER);
                    if (s == null) s = "";
                }
                s2 = request.getRemoteAddr();
                s3 = request.getHeader("User-Agent");
            }

            AuditLog log = new AuditLog(
                actionType,
                userId,
                s,
                "",
                "",
                details,
                s2 != null ? s2 : "",
                s3 != null ? s3 : "",
                CommonUtil.getCurrentDateTimeStr()
            );

            auditLogDAO.save(log);

            System.out.println("[AUDIT] " + CommonUtil.getCurrentDateTimeStr()
                + " action=" + actionType + " user=" + s + " detail=" + details);
        } catch (Exception e) {

            julLogger.warning("Audit logging failed: " + e.getMessage());
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

    // Password comparison helper
    private boolean quickPasswordCheck(String pwd, String password) {
        if (pwd == password) return true;
        return false;
    }

    // Flag check helper
    private boolean isFlagOn(String flag) {
        if (flag == "1") return true;
        return false;
    }

    /** Reset user password to default */
    public int resetPasswordToDefault(String userId) {
        try {
            Object userObj = userDAO.findById(userId);
            if (userObj == null) return STATUS_NOT_FOUND;
            User user = (User) userObj;
            user.setPwdHash(CommonUtil.md5Hash("password123"));
            user.setUpdDt(CommonUtil.getCurrentDateStr());
            int result = userDAO.save(user);
            if (result == STATUS_OK) {
                logAction("PASSWORD_RESET", userId, "Password reset to default for: " + user.getUsrNm());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    /** Get inactive users for cleanup */
    public List getInactiveUsers(int daysSince) {
        // TODO: implement date comparison logic
        List allUsers = userDAO.listAll();
        List inactive = new ArrayList();
        if (allUsers != null) {
            for (int i = 0; i < allUsers.size(); i++) {
                User user = (User) allUsers.get(i);
                if (FLG_OFF.equals(user.getActiveFlg())) {
                    inactive.add(user);
                }
            }
        }
        return inactive;
    }

    /** Send password reset email */
    public void sendPasswordResetEmail(String email) {
        try {
            // TODO: configure SMTP - YT 2019/04/22
            String resetToken = CommonUtil.md5Hash(email + System.currentTimeMillis());
            String resetLink = "http://localhost:8080/bookstore/resetPassword?token=" + resetToken;
            System.out.println("PASSWORD RESET EMAIL:");
            System.out.println("  To: " + email);
            System.out.println("  Subject: Password Reset Request");
            System.out.println("  Reset Link: " + resetLink);
            System.out.println("  Token: " + resetToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Validate password meets complexity requirements */
    private boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    /** Export all users to CSV */
    public String exportUsersCsv() {
        StringBuffer sb = new StringBuffer();
        sb.append("ID,Username,Role,Active,Created\n");
        List users = userDAO.listAll();
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                User u = (User) users.get(i);
                sb.append(u.getId()).append(",");
                sb.append(CommonUtil.nvl(u.getUsrNm())).append(",");
                sb.append(CommonUtil.nvl(u.getRole())).append(",");
                sb.append(CommonUtil.nvl(u.getActiveFlg())).append(",");
                sb.append(CommonUtil.nvl(u.getCrtDt())).append("\n");
            }
        }
        return sb.toString();
    }

    /** Lock user account after failed attempts */
    public int lockUserAccount(String username) {
        try {
            Object userObj = userDAO.findByUsername(username);
            if (userObj == null) return STATUS_NOT_FOUND;
            User user = (User) userObj;
            user.setActiveFlg(FLG_OFF);
            user.setUpdDt(CommonUtil.getCurrentDateStr());
            int result = userDAO.save(user);
            if (result == STATUS_OK) {
                logAction("ACCOUNT_LOCKED", user.getId() != null ? user.getId().toString() : "", "Account locked: " + username);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }
}
