package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Supplier implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nm;
    private String contactPerson;
    private String email;
    private String phone;
    private String addr1;
    private String address_line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String fax;
    private String website;
    private String paymentTerms;
    private String leadTimeDays;
    private String minOrderQty;
    private String status;
    private String notes;
    private String crtDt;
    private String updDt;

    public Supplier() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddr1() { return addr1; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }

    public String getAddress_line2() { return address_line2; }
    public void setAddress_line2(String address_line2) { this.address_line2 = address_line2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(String leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public String getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(String minOrderQty) { this.minOrderQty = minOrderQty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }
}

class SupplierRating implements Serializable {
    private static final long serialVersionUID = 1L;
    public String supplierId;
    public String rating;
    public String evaluationDt;
    public String notes;
}
