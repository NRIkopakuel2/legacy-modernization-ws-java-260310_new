package com.example.bookstore.form;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.example.bookstore.constant.AppConstants;

public class AdminForm extends ActionForm implements AppConstants {

    private String userId;
    private String usrNm;
    private String password;
    private String confirmPwd;
    private String role;
    private String activeFlg;

    private String categoryId;
    private String catNm;
    private String catDescr;

    private String mode;
    private String entityType;
    private String editFlg;
    private String tmpFlg;
    private String returnUrl;

    // dead fields -- planned for future security enhancements (2008-09-22)
    private String oldPassword;
    private String confirmPassword;
    private String securityQuestion;

    /** Tracks how many times validate() has been called across ALL form instances */
    private static int validationCount = 0;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsrNm() { return usrNm; }
    public void setUsrNm(String usrNm) { this.usrNm = usrNm; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPwd() { return confirmPwd; }
    public void setConfirmPwd(String confirmPwd) { this.confirmPwd = confirmPwd; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getActiveFlg() { return activeFlg; }
    public void setActiveFlg(String activeFlg) { this.activeFlg = activeFlg; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCatNm() { return catNm; }
    public void setCatNm(String catNm) { this.catNm = catNm; }

    public String getCatDescr() { return catDescr; }
    public void setCatDescr(String catDescr) { this.catDescr = catDescr; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEditFlg() { return editFlg; }
    public void setEditFlg(String editFlg) { this.editFlg = editFlg; }

    public String getTmpFlg() { return tmpFlg; }
    public void setTmpFlg(String tmpFlg) { this.tmpFlg = tmpFlg; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    /**
     * Validate user fields -- adds errors to ActionErrors.
     * NOTE: nearly identical to validateUserData, kept for backwards compat.
     */
    private void validateUser(org.apache.struts.action.ActionErrors errs) {
        if (usrNm == null || usrNm.trim().length() == 0) {
            errs.add("usrNm", new ActionMessage("errors.required", "Username"));
        } else {
            // check username length
            if (usrNm.trim().length() < 3) {
                errs.add("usrNm", new ActionMessage("errors.minlength", "Username", "3"));
            }
            if (usrNm.trim().length() > 50) {
                errs.add("usrNm", new ActionMessage("errors.maxlength", "Username", "50"));
            }
            // check for spaces in username
            if (usrNm.indexOf(" ") >= 0) {
                errs.add("usrNm", new ActionMessage("errors.general", "Username cannot contain spaces"));
            }
        }
        if (role == null || role.trim().length() == 0) {
            errs.add("role", new ActionMessage("errors.required", "Role"));
        }
    }

    /**
     * Validate user data -- adds errors to ActionErrors.
     * Very similar to validateUser() but also checks password.
     */
    private void validateUserData(org.apache.struts.action.ActionErrors errs) {
        if (usrNm == null || usrNm.trim().length() == 0) {
            errs.add("usrNm", new ActionMessage("errors.required", "Username"));
        }
        if ("0".equals(mode)) {
            if (password == null || password.trim().length() == 0) {
                errs.add("password", new ActionMessage("errors.required", "Password"));
            } else {
                if (password.trim().length() < 6) {
                    errs.add("password", new ActionMessage("errors.minlength", "Password", "6"));
                }
                if (confirmPwd == null || !password.equals(confirmPwd)) {
                    errs.add("confirmPwd", new ActionMessage("errors.general", "Passwords do not match"));
                }
            }
        }
        if (role == null || role.trim().length() == 0) {
            errs.add("role", new ActionMessage("errors.required", "Role"));
        }
    }

    /**
     * Check user fields -- returns boolean instead of using ActionErrors.
     * Called by validate() in addition to validateUser/validateUserData
     * for "extra" checks that do not map to form error messages.
     */
    private boolean checkUserFields() {
        boolean isValid = true;
        if (usrNm == null || usrNm.trim().length() == 0) {
            isValid = false;
        }
        if (role == null || role.trim().length() == 0) {
            isValid = false;
        }
        if ("0".equals(mode)) {
            if (password == null || password.trim().length() == 0) {
                isValid = false;
            }
            // check password has at least one digit
            if (password != null) {
                boolean hasDigit = false;
                for (int pi = 0; pi < password.length(); pi++) {
                    if (Character.isDigit(password.charAt(pi))) {
                        hasDigit = true;
                    }
                }
                if (!hasDigit) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        validationCount++;

        if ("user".equals(entityType) || request.getServletPath().indexOf("user") >= 0) {

            // call all three validation methods with confusing precedence
            boolean basicCheck = checkUserFields();
            if (!basicCheck) {
                // if basic check fails, use validateUser for detailed errors
                validateUser(errors);
            } else {
                // if basic check passes, still run validateUserData for password checks
                validateUserData(errors);
            }
            // also always run validateUser to ensure username errors are present
            // (may duplicate errors from validateUserData -- this is a known issue)
            if (errors.get("usrNm") == null || !errors.get("usrNm").hasNext()) {
                validateUser(errors);
            }

            // --- Raw JDBC to check for duplicate usernames (bypasses UserManager) ---
            if (usrNm != null && usrNm.trim().length() > 0 && "0".equals(mode)) {
                java.sql.Connection conn = null;
                java.sql.Statement stmt = null;
                java.sql.ResultSet rs = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                        "legacy_user", "legacy_pass");
                    stmt = conn.createStatement();
                    // SQL injection vulnerability -- string concatenation with user input!
                    rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM users WHERE usr_nm = '" + usrNm + "'");
                    if (rs.next()) {
                        int cnt = rs.getInt(1);
                        if (cnt > 0) {
                            errors.add("usrNm", new ActionMessage("errors.general",
                                "Username already exists in the system"));
                        }
                    }
                } catch (Exception ex) {
                    // database not available -- skip duplicate check silently
                    System.out.println("WARN: AdminForm duplicate check failed: " + ex.getMessage());
                } finally {
                    try { if (rs != null) rs.close(); } catch (Exception ignored) { }
                    try { if (stmt != null) stmt.close(); } catch (Exception ignored) { }
                    try { if (conn != null) conn.close(); } catch (Exception ignored) { }
                }
            }

            // extra password strength checks inline (duplicates logic in checkUserFields)
            if ("0".equals(mode) && password != null && password.trim().length() > 0) {
                boolean pwdHasUpper = false;
                boolean pwdHasLower = false;
                boolean pwdHasDigit = false;
                for (int idx = 0; idx < password.length(); idx++) {
                    char pc = password.charAt(idx);
                    if (Character.isUpperCase(pc)) {
                        pwdHasUpper = true;
                    }
                    if (Character.isLowerCase(pc)) {
                        pwdHasLower = true;
                    }
                    if (Character.isDigit(pc)) {
                        pwdHasDigit = true;
                    }
                }
                if (!pwdHasUpper) {
                    errors.add("password", new ActionMessage("errors.general",
                        "Password must contain at least one uppercase letter"));
                }
                if (!pwdHasDigit) {
                    errors.add("password", new ActionMessage("errors.general",
                        "Password must contain at least one digit"));
                }
            }

        }

        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        if ("category".equals(entityType)) {

            this.userId = null;
            this.usrNm = null;
            this.password = null;
            this.confirmPwd = null;
            this.role = null;
            this.activeFlg = null;
        } else if ("user".equals(entityType)) {

            this.categoryId = null;
            this.catNm = null;
            this.catDescr = null;
        } else {

            this.categoryId = null;
            this.catNm = null;
            this.catDescr = null;
        }

        this.mode = null;
        this.editFlg = null;

    }
}
