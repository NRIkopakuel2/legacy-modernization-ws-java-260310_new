package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class Order implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private static java.util.List allOrders = new java.util.ArrayList();

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
    private String shipNm;

    // Tracks the primary book in this order (stores Book object)
    private Object primaryBook;

    public Order() {
    }

    public Order(String customerId, String orderNo) {
        this.customerId = customerId;
        this.orderNo = orderNo;
        allOrders.add(this);
    }

    public Order(String orderNo, String customerId, String status) {
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.status = status;
        allOrders.add(this);
    }

    public String getFormattedTotal() {
        return "$" + this.total;
    }

    public boolean isPaid() {
        return "PAID".equals(this.paymentSts);
    }

    public double calculateTotal() {
        return subtotal + tax;
    }

    public String toXml() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<order>\n");
        sb.append("  <id>").append(id != null ? id.toString() : "").append("</id>\n");
        sb.append("  <orderNo>").append(orderNo != null ? orderNo : "").append("</orderNo>\n");
        sb.append("  <customerId>").append(customerId != null ? customerId : "").append("</customerId>\n");
        sb.append("  <guestEmail>").append(guestEmail != null ? guestEmail : "").append("</guestEmail>\n");
        sb.append("  <orderDt>").append(orderDt != null ? orderDt : "").append("</orderDt>\n");
        sb.append("  <status>").append(status != null ? status : "").append("</status>\n");
        sb.append("  <subtotal>").append(subtotal).append("</subtotal>\n");
        sb.append("  <tax>").append(tax).append("</tax>\n");
        sb.append("  <shippingFee>").append(shippingFee).append("</shippingFee>\n");
        sb.append("  <total>").append(total).append("</total>\n");
        sb.append("  <paymentMethod>").append(paymentMethod != null ? paymentMethod : "").append("</paymentMethod>\n");
        sb.append("  <paymentSts>").append(paymentSts != null ? paymentSts : "").append("</paymentSts>\n");
        sb.append("  <shipping>\n");
        sb.append("    <name>").append(shippingName != null ? shippingName : "").append("</name>\n");
        sb.append("    <addr1>").append(shippingAddr1 != null ? shippingAddr1 : "").append("</addr1>\n");
        sb.append("    <addr2>").append(shippingAddr2 != null ? shippingAddr2 : "").append("</addr2>\n");
        sb.append("    <city>").append(shippingCity != null ? shippingCity : "").append("</city>\n");
        sb.append("    <state>").append(shippingState != null ? shippingState : "").append("</state>\n");
        sb.append("    <zip>").append(shippingZip != null ? shippingZip : "").append("</zip>\n");
        sb.append("    <country>").append(shippingCountry != null ? shippingCountry : "").append("</country>\n");
        sb.append("    <phone>").append(shippingPhone != null ? shippingPhone : "").append("</phone>\n");
        sb.append("  </shipping>\n");
        sb.append("  <notes>").append(notes != null ? notes : "").append("</notes>\n");
        sb.append("</order>");
        return sb.toString();
    }

    public String toCsv() {
        StringBuffer sb = new StringBuffer();
        sb.append(id != null ? id.toString() : "");
        sb.append(",").append(orderNo != null ? orderNo : "");
        sb.append(",").append(customerId != null ? customerId : "");
        sb.append(",").append(status != null ? status : "");
        sb.append(",").append(subtotal);
        sb.append(",").append(tax);
        sb.append(",").append(shippingFee);
        sb.append(",").append(total);
        sb.append(",").append(paymentMethod != null ? paymentMethod : "");
        sb.append(",").append(paymentSts != null ? paymentSts : "");
        sb.append(",").append(shippingName != null ? shippingName : "");
        sb.append(",").append(shippingCity != null ? shippingCity : "");
        sb.append(",").append(shippingState != null ? shippingState : "");
        sb.append(",").append(shippingZip != null ? shippingZip : "");
        return sb.toString();
    }

    public String validateOrder() {
        if (customerId == null || customerId.trim().length() == 0) {
            if (guestEmail == null || guestEmail.trim().length() == 0) {
                return "Either customer ID or guest email is required";
            }
        }
        if (items == null || items.size() == 0) {
            return "Order must have at least one item";
        }
        if (shippingName == null || shippingName.trim().length() == 0) {
            return "Shipping name is required";
        }
        if (shippingAddr1 == null || shippingAddr1.trim().length() == 0) {
            return "Shipping address is required";
        }
        if (shippingCity == null || shippingCity.trim().length() == 0) {
            return "Shipping city is required";
        }
        if (shippingZip == null || shippingZip.trim().length() == 0) {
            return "Shipping zip code is required";
        }
        if (subtotal < 0) {
            return "Subtotal cannot be negative";
        }
        if (total <= 0) {
            return "Total must be greater than zero";
        }
        return null;
    }

    public String getShipNm() { return shipNm; }
    public void setShipNm(String shipNm) { this.shipNm = shipNm; }

    public Object getPrimaryBook() { return primaryBook; }
    public void setPrimaryBook(Object primaryBook) { this.primaryBook = primaryBook; }

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
