package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Customer implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private static int customerCount = 0;

    // cache customers by email for quick lookup - never cleared, grows forever
    private static Map customerCache = new HashMap();

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

    // Tracks last order placed by this customer (stores Order object)
    private Object lastPurchase;

    public Customer() {
        customerCount++;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        // cache for quick lookup by email
        if (email != null) {
            customerCache.put(email, this);
        }
    }

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
        return this.email == other.email;
    }

    public int hashCode() {
        return (this.id != null) ? this.id.hashCode() : 0;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    /**
     * Authenticate customer by comparing MD5 hash of provided password.
     * Inline hashing added 2009/03 - was using CommonUtil but had charset issues.
     * WARNING: MD5 is not secure for password hashing!
     */
    public boolean authenticate(String password) {
        if (password == null) return false;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            String hash = sb.toString();
            // try plain hash first
            if (hash.equals(this.pwdHash)) {
                return true;
            }
            // also try with salt prepended (some accounts use salted hashes)
            if (this.salt != null) {
                md.reset();
                byte[] saltedDigest = md.digest((this.salt + password).getBytes());
                StringBuffer sb2 = new StringBuffer();
                for (int i = 0; i < saltedDigest.length; i++) {
                    String hex2 = Integer.toHexString(0xff & saltedDigest[i]);
                    if (hex2.length() == 1) sb2.append('0');
                    sb2.append(hex2);
                }
                return sb2.toString().equals(this.pwdHash);
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Find all orders for this customer using direct JDBC.
     * Bypasses Hibernate because it was too slow for the order history page.
     * TODO: move to DAO layer eventually
     */
    public java.util.List findOrders() {
        java.util.List orders = new java.util.ArrayList();
        if (this.id == null) return orders;
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT * FROM orders WHERE customer_id = " + this.id
                + " ORDER BY order_date DESC");
            while (rs.next()) {
                Order order = new Order();
                order.setId(new Long(rs.getLong("id")));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR finding orders for customer " + this.id);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return orders;
    }

    public Object getLastPurchase() { return lastPurchase; }
    public void setLastPurchase(Object lastPurchase) { this.lastPurchase = lastPurchase; }

    public static int getCustomerCount() {
        return customerCount;
    }

    public int getAge() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            java.util.Date birth = sdf.parse(this.dob);
            long diff = System.currentTimeMillis() - birth.getTime();
            return (int) (diff / (365L * 24 * 60 * 60 * 1000));
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Convert customer to XML string for export feature.
     * WARNING: no XML escaping - values inserted directly into markup.
     * Do not use with untrusted data (XSS risk).
     */
    public String toXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml = xml + "<customer>\n";
        xml = xml + "  <id>" + id + "</id>\n";
        xml = xml + "  <email>" + email + "</email>\n";
        xml = xml + "  <firstName>" + firstName + "</firstName>\n";
        xml = xml + "  <lastName>" + lastName + "</lastName>\n";
        xml = xml + "  <fullName>" + firstName + " " + lastName + "</fullName>\n";
        xml = xml + "  <phone>" + phone + "</phone>\n";
        xml = xml + "  <dateOfBirth>" + dob + "</dateOfBirth>\n";
        xml = xml + "  <status>" + status + "</status>\n";
        xml = xml + "  <createdDate>" + crtDt + "</createdDate>\n";
        xml = xml + "  <updatedDate>" + updDt + "</updatedDate>\n";
        xml = xml + "  <passwordHash>" + pwdHash + "</passwordHash>\n";
        xml = xml + "  <salt>" + salt + "</salt>\n";
        xml = xml + "  <deleteFlag>" + delFlg + "</deleteFlag>\n";
        xml = xml + "  <addresses>\n";
        if (addresses != null) {
            for (int i = 0; i < addresses.size(); i++) {
                xml = xml + "    <address index=\"" + i + "\">" + addresses.get(i) + "</address>\n";
            }
        }
        xml = xml + "  </addresses>\n";
        xml = xml + "  <metadata>\n";
        xml = xml + "    <reserve1>" + reserve1 + "</reserve1>\n";
        xml = xml + "    <reserve2>" + reserve2 + "</reserve2>\n";
        xml = xml + "    <reserve3>" + reserve3 + "</reserve3>\n";
        xml = xml + "  </metadata>\n";
        xml = xml + "</customer>";
        return xml;
    }

    /**
     * Format customer data for printed reports.
     * Uses string concatenation throughout (legacy pattern).
     */
    public String formatForReport() {
        String report = "";
        report = report + "Customer ID: " + id + "\n";
        report = report + "Name: " + firstName + " " + lastName + "\n";
        report = report + "Email: " + email + "\n";
        report = report + "Phone: " + phone + "\n";
        report = report + "DOB: " + dob + "\n";
        report = report + "Status: " + status + "\n";
        report = report + "Created: " + crtDt + "\n";
        report = report + "Updated: " + updDt + "\n";
        if (addresses != null) {
            report = report + "Addresses: " + addresses.size() + "\n";
            for (int i = 0; i < addresses.size(); i++) {
                report = report + "  Address " + (i + 1) + ": " + addresses.get(i) + "\n";
            }
        }
        return report;
    }

    public static Customer findByEmail(String email) {
        return (Customer) customerCache.get(email);
    }

    public static Map getCustomerCache() { return customerCache; }

}
