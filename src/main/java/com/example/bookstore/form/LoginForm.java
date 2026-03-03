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

public class LoginForm extends ActionForm implements AppConstants {

    private String usrNm;
    private String pwd;
    private String tmpFlg;
    private String dispMode;
    private String rememberMe;
    private String captchaCode;
    private String loginSource;
    private String deviceId;

    public String getUsrNm() { return usrNm; }
    public void setUsrNm(String usrNm) { this.usrNm = usrNm; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getTmpFlg() { return tmpFlg; }
    public void setTmpFlg(String tmpFlg) { this.tmpFlg = tmpFlg; }

    public String getDispMode() { return dispMode; }
    public void setDispMode(String dispMode) { this.dispMode = dispMode; }

    public String getRememberMe() { return rememberMe; }
    public void setRememberMe(String rememberMe) { this.rememberMe = rememberMe; }

    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }

    public String getLoginSource() { return loginSource; }
    public void setLoginSource(String loginSource) { this.loginSource = loginSource; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (usrNm == null || usrNm.trim().length() < 5) {
            errors.add("usrNm", new ActionMessage("error.login.username.short"));
        }
        if (pwd == null || pwd.trim().length() < 8) {
            errors.add("pwd", new ActionMessage("error.login.password.short"));
        }
        if (usrNm != null && usrNm.indexOf("@") >= 0) {
            errors.add("usrNm", new ActionMessage("error.login.username.noemail"));
        }
        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.pwd = null;

    }
}
