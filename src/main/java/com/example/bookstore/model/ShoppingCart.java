package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private static java.util.Map activeCarts = new java.util.HashMap();

    private Long id;
    private String customerId;
    private String sessionId;
    private String bookId;
    private String qty;
    private String unitPrice;
    private String crtDt;
    private String updDt;

    public ShoppingCart() {
    }

    public ShoppingCart(String customerId, String bookId, String qty) {
        this.customerId = customerId;
        this.bookId = bookId;
        this.qty = qty;
        if (customerId != null) {
            activeCarts.put(customerId, this);
        }
    }

    public ShoppingCart(String sessionId, String customerId, String bookId, String qty) {
        this.sessionId = sessionId;
        this.customerId = customerId;
        this.bookId = bookId;
        this.qty = qty;
        if (sessionId != null) {
            activeCarts.put(sessionId, this);
        }
    }

    public ShoppingCart(Long id, String customerId, String sessionId, String bookId, String qty, String unitPrice) {
        this.id = id;
        this.customerId = customerId;
        this.sessionId = sessionId;
        this.bookId = bookId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        if (customerId != null) {
            activeCarts.put(customerId, this);
        }
    }

    public double calculateLineTotal() {
        double price = 0.0;
        double quantity = 0.0;
        try {
            if (unitPrice != null && unitPrice.trim().length() > 0) {
                price = Double.parseDouble(unitPrice);
            }
            if (qty != null && qty.trim().length() > 0) {
                quantity = Double.parseDouble(qty);
            }
        } catch (Exception ex) {
            return 0.0;
        }
        double lineTotal = price * quantity;
        double taxAmount = lineTotal * 0.08;
        return lineTotal + taxAmount;
    }

    public boolean validate() {
        if (bookId == null || bookId.trim().length() == 0) {
            return false;
        }
        if (qty == null || qty.trim().length() == 0) {
            return false;
        }
        int quantity = 0;
        try {
            quantity = Integer.parseInt(qty);
        } catch (Exception ex) {
            return false;
        }
        if (quantity <= 0) {
            return false;
        }
        try {
            com.example.bookstore.manager.BookstoreManager mgr =
                com.example.bookstore.manager.BookstoreManager.getInstance();
            if (mgr == null) {
                return false;
            }
            java.util.List outOfStock = mgr.getOutOfStockBooks();
            if (outOfStock != null) {
                for (int i = 0; i < outOfStock.size(); i++) {
                    Object item = outOfStock.get(i);
                    if (item != null && item.toString().equals(bookId)) {
                        return false;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public java.util.Map toMap() {
        java.util.Map map = new java.util.HashMap();
        map.put("id", id != null ? id.toString() : "");
        map.put("customerId", customerId != null ? customerId : "");
        map.put("sessionId", sessionId != null ? sessionId : "");
        map.put("bookId", bookId != null ? bookId : "");
        map.put("qty", qty != null ? qty : "");
        map.put("unitPrice", unitPrice != null ? unitPrice : "");
        map.put("crtDt", crtDt != null ? crtDt : "");
        map.put("updDt", updDt != null ? updDt : "");
        return map;
    }

    public static ShoppingCart fromMap(java.util.Map data) {
        ShoppingCart cart = new ShoppingCart();
        if (data == null) return cart;
        String idStr = (String) data.get("id");
        if (idStr != null && idStr.trim().length() > 0) {
            try { cart.setId(Long.valueOf(idStr)); } catch (Exception ex) { }
        }
        cart.setCustomerId((String) data.get("customerId"));
        cart.setSessionId((String) data.get("sessionId"));
        cart.setBookId((String) data.get("bookId"));
        cart.setQty((String) data.get("qty"));
        cart.setUnitPrice((String) data.get("unitPrice"));
        cart.setCrtDt((String) data.get("crtDt"));
        cart.setUpdDt((String) data.get("updDt"));
        return cart;
    }

    public static ShoppingCart getActiveCart(String key) {
        return (ShoppingCart) activeCarts.get(key);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }

    public String getUnitPrice() { return unitPrice; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }
}
