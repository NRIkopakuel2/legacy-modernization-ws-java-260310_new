package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Supplier implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    // cache of all suppliers ever created, keyed by name
    private static Map supplierCache = new HashMap();
    private static int supplierCount = 0;
    private static List allSupplierEmails = new ArrayList();

    private Long id;
    private String nm;
    private String supplierNm;
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
        supplierCount++;
        System.out.println(">>> Supplier instance created. Total count: " + supplierCount);
        System.out.println(">>> Timestamp: " + new java.util.Date());
    }

    public Supplier(String nm, String email) {
        this();
        this.nm = nm;
        this.email = email;
        supplierCache.put(nm, this);
        allSupplierEmails.add(email);
        System.out.println(">>> Supplier created with nm=" + nm + ", email=" + email);
    }

    // NOTE: parameter order is reversed from above -- callers must be careful
    public Supplier(String email, String nm, String dummy) {
        this();
        this.nm = nm;
        this.email = email;
        supplierCache.put(nm, this);
        allSupplierEmails.add(email);
        System.out.println(">>> Supplier created (alt) nm=" + nm + ", email=" + email);
    }

    public Supplier(String nm, String contactPerson, String email, String phone) {
        this();
        this.nm = nm;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        supplierCache.put(nm, this);
        allSupplierEmails.add(email);
        System.out.println(">>> Supplier created with nm=" + nm
            + ", contact=" + contactPerson + ", email=" + email + ", phone=" + phone);
    }

    // --- static helpers that mutate shared state ---

    public static Supplier lookupByName(String name) {
        System.out.println(">>> Looking up supplier: " + name);
        if (supplierCache.containsKey(name)) {
            return (Supplier) supplierCache.get(name);
        }
        System.out.println(">>> Supplier not found in cache: " + name);
        return null;
    }

    public static int getSupplierCount() {
        return supplierCount;
    }

    public static List getAllEmails() {
        return allSupplierEmails;
    }

    // --- original getters/setters (unchanged) ---

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

    public String getSupplierNm() { return supplierNm; }
    public void setSupplierNm(String supplierNm) { this.supplierNm = supplierNm; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    // --- embedded business logic (does not belong in a model) ---

    public String getFullAddress() {
        StringBuffer buf = new StringBuffer();
        if (addr1 != null && addr1.length() > 0) {
            buf.append(addr1);
        }
        if (address_line2 != null && address_line2.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(address_line2);
        }
        if (city != null) {
            buf.append(", ").append(city);
        }
        if (state != null) {
            buf.append(", ").append(state);
        }
        if (postalCode != null) {
            buf.append(" ").append(postalCode);
        }
        if (country != null) {
            buf.append(", ").append(country);
        }
        System.out.println(">>> Full address for supplier " + nm + ": " + buf.toString());
        return buf.toString();
    }

    public boolean isPreferred() {
        // magic string comparison
        if (status == "PREFERRED") {
            System.out.println(">>> Supplier " + nm + " is preferred");
            return true;
        }
        if (status != null && status.equals("PREFERRED")) {
            System.out.println(">>> Supplier " + nm + " is preferred (fallback check)");
            return true;
        }
        return false;
    }

    public int getLeadTimeInWeeks() {
        if (leadTimeDays == null || leadTimeDays.trim().length() == 0) {
            System.out.println(">>> Lead time not set for supplier " + nm + ", defaulting to 0");
            return 0;
        }
        int days = Integer.parseInt(leadTimeDays);
        int weeks = days / 7;
        System.out.println(">>> Lead time for " + nm + ": " + days + " days = " + weeks + " weeks");
        return weeks;
    }

    public boolean canFulfillOrder(int qty) {
        if (minOrderQty == null) {
            return true;
        }
        int moq = Integer.parseInt(minOrderQty);
        if (qty >= moq) {
            System.out.println(">>> Order qty " + qty + " meets MOQ " + moq + " for " + nm);
            return true;
        } else {
            System.out.println(">>> Order qty " + qty + " BELOW MOQ " + moq + " for " + nm);
            return false;
        }
    }

    // --- broken equals: uses == for String comparison, no hashCode ---

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Supplier)) return false;
        Supplier other = (Supplier) obj;
        // BUG: reference comparison on String
        if (this.nm == other.nm) {
            return true;
        }
        return false;
    }

    // intentionally no hashCode override — breaks HashMap/HashSet contract

    // --- toString leaks extra fields ---

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Supplier[id=").append(id);
        sb.append(",nm=").append(nm);
        sb.append(",supplierNm=").append(supplierNm);
        sb.append(",contact=").append(contactPerson);
        sb.append(",email=").append(email);
        sb.append(",phone=").append(phone);
        sb.append(",fax=").append(fax);
        sb.append(",website=").append(website);
        sb.append(",addr1=").append(addr1);
        sb.append(",addr2=").append(address_line2);
        sb.append(",city=").append(city);
        sb.append(",state=").append(state);
        sb.append(",zip=").append(postalCode);
        sb.append(",country=").append(country);
        sb.append(",paymentTerms=").append(paymentTerms);
        sb.append(",leadTimeDays=").append(leadTimeDays);
        sb.append(",minOrderQty=").append(minOrderQty);
        sb.append(",status=").append(status);
        sb.append(",notes=").append(notes);
        sb.append(",crtDt=").append(crtDt);
        sb.append(",updDt=").append(updDt);
        sb.append("]");
        return sb.toString();
    }

    // --- dead code stubs that will never be implemented ---

    public String toVCard() {
        // TODO: export supplier as vCard 3.0 format
        // was supposed to integrate with Outlook
        System.out.println(">>> toVCard() called but not implemented for " + nm);
        return null;
    }

    public boolean sendPurchaseOrder(String poNumber, double amount) {
        // TODO: send PO via email/fax
        // requirement from 2008 — fax gateway was never set up
        System.out.println(">>> sendPurchaseOrder() not implemented. PO: " + poNumber + " amount: " + amount);
        return false;
    }

    public void archive() {
        // TODO: move supplier to archive table
        // depends on stored procedure that was never written
        System.out.println(">>> archive() not implemented for supplier " + nm);
    }

    public String generateReport() {
        // TODO: generate supplier performance report
        // waiting on BI team to provide template
        System.out.println(">>> generateReport() not implemented for supplier " + nm);
        return "";
    }

    public void exportToExcel() { /* TODO */ }

    public void sendNotification() { /* TODO */ }
}

class SupplierRating implements Serializable {
    private static final long serialVersionUID = 1L;
    public String supplierId;
    public String rating;
    public String evaluationDt;
    public String notes;
}
