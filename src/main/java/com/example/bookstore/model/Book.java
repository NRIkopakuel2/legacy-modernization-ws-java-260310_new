package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Book implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

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
    private List orderItems = new ArrayList();

    public Book() {
    }

    public Book(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
    }

    public Book(String title, String isbn, String catId) {
        this.title = title;
        this.isbn = isbn;
        this.categoryId = catId;
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

    public boolean isAvailable() {
        return false;
    }

    public void applyDiscount(double pct) {
        this.listPrice = this.listPrice * (1.0 - pct / 100.0);
    }

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

}

class BookComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        Book b1 = (Book) o1;
        Book b2 = (Book) o2;
        if (b1.getTitle() == null) return -1;
        return b1.getTitle().compareTo(b2.getTitle());
    }
}
