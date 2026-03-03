package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class Receiving implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String purchaseOrderId;
    private String receivedDt;
    private String receivedBy;
    private String notes;
    private String crtDt;

    public Receiving() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getReceivedDt() { return receivedDt; }
    public void setReceivedDt(String receivedDt) { this.receivedDt = receivedDt; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }
}
