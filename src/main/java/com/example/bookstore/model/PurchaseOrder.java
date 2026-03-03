package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.constant.AppConstants;

public class PurchaseOrder implements Serializable, AppConstants {

    private static final long serialVersionUID = 1L;

    private Long id;
    private List poItems = new ArrayList();
    private String poNumber;
    private String supplierId;
    private String orderDt;
    private String submittedAt;
    private String submittedBy;
    private String expectedDeliveryDt;
    private String status;
    private double subtotal;
    private double tax;
    private double shippingCost;
    private double total;
    private String notes;
    private String cancellationReason;
    private String cancellationNotes;
    private String createdBy;
    private String crtDt;
    private String updDt;
    private String reserve1;
    private String reserve2;

    public PurchaseOrder() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getOrderDt() { return orderDt; }
    public void setOrderDt(String orderDt) { this.orderDt = orderDt; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public String getExpectedDeliveryDt() { return expectedDeliveryDt; }
    public void setExpectedDeliveryDt(String expectedDeliveryDt) { this.expectedDeliveryDt = expectedDeliveryDt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getShippingCost() { return shippingCost; }
    public void setShippingCost(double shippingCost) { this.shippingCost = shippingCost; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getCancellationNotes() { return cancellationNotes; }
    public void setCancellationNotes(String cancellationNotes) { this.cancellationNotes = cancellationNotes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    public String getReserve1() { return reserve1; }
    public void setReserve1(String reserve1) { this.reserve1 = reserve1; }

    public String getReserve2() { return reserve2; }
    public void setReserve2(String reserve2) { this.reserve2 = reserve2; }

    public List getPoItems() { return this.poItems; }
    public void setPoItems(List poItems) { this.poItems = poItems; }
}
