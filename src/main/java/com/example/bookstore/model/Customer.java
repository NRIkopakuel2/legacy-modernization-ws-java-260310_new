package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Customer implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private Long id;
    private List addresses = new ArrayList();
    private String email;
    private String pwdHash;
    private String salt;
    private String firstName;
    private String lastName;
    private String phone;
    private String dob;
    private String status;
    private String crtDt;
    private String updDt;
    private String delFlg;
    private String reserve1;
    private String reserve2;
    private String reserve3;

    public Customer() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPwdHash() { return pwdHash; }
    public void setPwdHash(String pwdHash) { this.pwdHash = pwdHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    public String getDelFlg() { return delFlg; }
    public void setDelFlg(String delFlg) { this.delFlg = delFlg; }

    public String getReserve1() { return reserve1; }
    public void setReserve1(String reserve1) { this.reserve1 = reserve1; }

    public String getReserve2() { return reserve2; }
    public void setReserve2(String reserve2) { this.reserve2 = reserve2; }

    public String getReserve3() { return reserve3; }
    public void setReserve3(String reserve3) { this.reserve3 = reserve3; }

    public List getAddresses() { return this.addresses; }
    public void setAddresses(List addresses) { this.addresses = addresses; }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Customer)) return false;
        Customer other = (Customer) obj;
        if (this.email == null) return other.email == null;
        return this.email.equals(other.email);
    }

    public int hashCode() {
        return (this.id != null) ? this.id.hashCode() : 0;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public int getAge() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            java.util.Date birth = sdf.parse(this.dob);
            long diff = System.currentTimeMillis() - birth.getTime();
            return (int) (diff / (365L * 24 * 60 * 60 * 1000));
        } catch (Exception e) {
            return 0;
        }
    }
}
