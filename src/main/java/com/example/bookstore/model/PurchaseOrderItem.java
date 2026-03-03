package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String purchaseOrderId;
    private String bookId;
    private String qtyOrdered;
    private String qtyReceived;
    private double unitPrice;
    private double lineSubtotal;
    private String notes;
    private String crtDt;

    public PurchaseOrderItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getQtyOrdered() { return qtyOrdered; }
    public void setQtyOrdered(String qtyOrdered) { this.qtyOrdered = qtyOrdered; }

    public String getQtyReceived() { return qtyReceived; }
    public void setQtyReceived(String qtyReceived) { this.qtyReceived = qtyReceived; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getLineSubtotal() { return lineSubtotal; }
    public void setLineSubtotal(double lineSubtotal) { this.lineSubtotal = lineSubtotal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }
}
