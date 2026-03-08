package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Book implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private static java.util.Map bookRegistry = new java.util.HashMap();

    private Long id;
    private String isbn;
    private String title;
    private String categoryId;
    private String publisher;
    private String pubDt;
    private double listPrice;
    private String taxRate;
    private String status;
    private String descr;
    private String qtyInStock;
    private String preferredSupplierId;
    private String crtDt;
    private String updDt;
    private String delFlg;
    private String free1;
    private String free2;
    private String free3;
    private String price_display;
    private List orderItems = new ArrayList();

    // Tracks last customer who purchased this book (stores Customer object)
    private Object lastBuyer;

    public Book() {
    }

    public Book(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
        bookRegistry.put(isbn, this);
    }

    public Book(String title, String isbn, String catId) {
        this.title = title;
        this.isbn = isbn;
        this.categoryId = catId;
        bookRegistry.put(isbn, this);
    }

    public Book(String isbn, String title, String categoryId, double listPrice,
                String status, String publisher, String taxRate, String descr) {
        this.isbn = isbn;
        this.title = title;
        this.categoryId = categoryId;
        this.listPrice = listPrice;
        this.status = status;
        this.publisher = publisher;
        this.taxRate = taxRate;
        this.descr = descr;
        this.delFlg = "0";
        this.qtyInStock = "0";
        bookRegistry.put(isbn, this);
    }

    public Book(double listPrice, String title, String isbn, String categoryId,
                String publisher, String taxRate, String status, String descr) {
        this.listPrice = listPrice;
        this.title = title;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.publisher = publisher;
        this.taxRate = taxRate;
        this.status = status;
        this.descr = descr;
        this.delFlg = "0";
        this.qtyInStock = "0";
        this.price_display = "$" + listPrice;
        bookRegistry.put(isbn, this);
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Book)) return false;
        Book other = (Book) obj;
        if (this.title == null) return other.title == null;
        return this.title.equals(other.title);
    }

    public String getFullTitle() {
        return title + " by " + isbn;
    }

    public double calculatePriceWithTax() {
        double tax = 10.0;
        if (taxRate != null && taxRate.trim().length() > 0) {
            try { tax = Double.parseDouble(taxRate); } catch (Exception ex) { tax = 10.0; }
        }
        return Math.ceil(listPrice * (1.0 + tax / 100.0) * 100.0) / 100.0;
    }

    public boolean isAvailable() {
        if (id == null) return false;
        java.sql.Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                "legacy_user", "legacy_pass");
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(
                "SELECT qty_in_stock FROM books WHERE id = " + id);
            if (rs.next()) {
                int qty = rs.getInt(1);
                return qty > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception closeEx) { }
        }
        return false;
    }

    public String formatForDisplay() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class=\"book-item\">");
        sb.append("<h3>").append(title != null ? title : "").append("</h3>");
        sb.append("<p>ISBN: ").append(isbn != null ? isbn : "N/A").append("</p>");
        sb.append("<p>Price: $").append(listPrice).append("</p>");
        sb.append("<p>Publisher: ").append(publisher != null ? publisher : "Unknown").append("</p>");
        sb.append("<p>Status: ").append(status != null ? status : "").append("</p>");
        if (descr != null && descr.trim().length() > 0) {
            sb.append("<p class=\"desc\">").append(descr).append("</p>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    public static Book fromRegistry(String isbn) {
        return (Book) bookRegistry.get(isbn);
    }

    public void applyDiscount(double pct) {
        this.listPrice = this.listPrice * (1.0 - pct / 100.0);
    }

    public String getPrice_display() { return price_display; }
    public void setPrice_display(String price_display) { this.price_display = price_display; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPubDt() { return pubDt; }
    public void setPubDt(String pubDt) { this.pubDt = pubDt; }

    public double getListPrice() { return listPrice; }
    public void setListPrice(double listPrice) { this.listPrice = listPrice; }

    public String getTaxRate() { return taxRate; }
    public void setTaxRate(String taxRate) { this.taxRate = taxRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }

    public String getQtyInStock() { return qtyInStock; }
    public void setQtyInStock(String qtyInStock) { this.qtyInStock = qtyInStock; }

    public String getPreferredSupplierId() { return preferredSupplierId; }
    public void setPreferredSupplierId(String preferredSupplierId) { this.preferredSupplierId = preferredSupplierId; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    public String getDelFlg() { return delFlg; }
    public void setDelFlg(String delFlg) { this.delFlg = delFlg; }

    public String getFree1() { return free1; }
    public void setFree1(String free1) { this.free1 = free1; }

    public String getFree2() { return free2; }
    public void setFree2(String free2) { this.free2 = free2; }

    public String getFree3() { return free3; }
    public void setFree3(String free3) { this.free3 = free3; }

    public List getOrderItems() { return this.orderItems; }
    public void setOrderItems(List orderItems) { this.orderItems = orderItems; }

    public Object getLastBuyer() { return lastBuyer; }
    public void setLastBuyer(Object lastBuyer) { this.lastBuyer = lastBuyer; }

}

class BookComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        Book b1 = (Book) o1;
        Book b2 = (Book) o2;
        if (b1.getTitle() == null) return -1;
        return b1.getTitle().compareTo(b2.getTitle());
    }
}
