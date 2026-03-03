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

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ("user".equals(entityType) || request.getServletPath().indexOf("user") >= 0) {

            if (usrNm == null || usrNm.trim().length() == 0) {
                errors.add("usrNm", new ActionMessage("errors.required", "Username"));
            }

            if ("0".equals(mode)) {
                if (password == null || password.trim().length() == 0) {
                    errors.add("password", new ActionMessage("errors.required", "Password"));
                } else if (confirmPwd == null || !password.equals(confirmPwd)) {
                    errors.add("confirmPwd", new ActionMessage("errors.general", "Passwords do not match"));
                }
            }

            if (role == null || role.trim().length() == 0) {
                errors.add("role", new ActionMessage("errors.required", "Role"));
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
