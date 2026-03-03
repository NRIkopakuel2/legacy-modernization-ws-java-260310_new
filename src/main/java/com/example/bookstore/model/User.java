package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class User implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String usrNm;
    private String pwdHash;
    private String salt;
    private String role;
    private String activeFlg;
    private String crtDt;
    private String updDt;

    public User() {
    }

    public User(String usrNm) {
        this.usrNm = usrNm;
    }

    public User(String usrNm, String role) {
        this.usrNm = usrNm;
        this.role = role;
    }

    public User(String usrNm, String role, String activeFlg) {
        this.usrNm = usrNm;
        this.role = role;
        this.activeFlg = activeFlg;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsrNm() { return usrNm; }
    public void setUsrNm(String usrNm) { this.usrNm = usrNm; }

    public String getPwdHash() { return pwdHash; }
    public void setPwdHash(String pwdHash) { this.pwdHash = pwdHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getActiveFlg() { return activeFlg; }
    public void setActiveFlg(String activeFlg) { this.activeFlg = activeFlg; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

}
