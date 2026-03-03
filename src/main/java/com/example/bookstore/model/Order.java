package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Order implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerId;
    private String guestEmail;
    private String orderNo;
    private String orderDt;
    private List items = new ArrayList();
    private String status;
    private double subtotal;
    private double tax;
    private double shippingFee;
    private double total;
    private String paymentMethod;
    private String paymentSts;
    private String shippingName;
    private String shippingAddr1;
    private String shippingAddr2;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String shippingPhone;
    private String notes;
    private String crtDt;
    private String updDt;

    public Order() {
    }

    public Order(String customerId, String orderNo) {
        this.customerId = customerId;
        this.orderNo = orderNo;
    }

    public Order(String orderNo, String customerId, String status) {
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.status = status;
    }

    public String getFormattedTotal() {
        return "$" + this.total;
    }

    public boolean isPaid() {
        return "PAID".equals(this.paymentSts);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getOrderDt() { return orderDt; }
    public void setOrderDt(String orderDt) { this.orderDt = orderDt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentSts() { return paymentSts; }
    public void setPaymentSts(String paymentSts) { this.paymentSts = paymentSts; }

    public String getShippingName() { return shippingName; }
    public void setShippingName(String shippingName) { this.shippingName = shippingName; }

    public String getShippingAddr1() { return shippingAddr1; }
    public void setShippingAddr1(String shippingAddr1) { this.shippingAddr1 = shippingAddr1; }

    public String getShippingAddr2() { return shippingAddr2; }
    public void setShippingAddr2(String shippingAddr2) { this.shippingAddr2 = shippingAddr2; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public String getShippingZip() { return shippingZip; }
    public void setShippingZip(String shippingZip) { this.shippingZip = shippingZip; }

    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }

    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    public List getItems() { return this.items; }
    public void setItems(List items) { this.items = items; }
}

class OrderSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    public String orderNo;
    public String customerName;
    public double total;
    public String status;
}
