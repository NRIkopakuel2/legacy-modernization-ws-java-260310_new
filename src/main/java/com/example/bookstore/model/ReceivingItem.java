package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class ReceivingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String receivingId;
    private String poItemId;
    private String qtyReceived;
    private String notes;
    private String crtDt;

    public ReceivingItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceivingId() { return receivingId; }
    public void setReceivingId(String receivingId) { this.receivingId = receivingId; }

    public String getPoItemId() { return poItemId; }
    public void setPoItemId(String poItemId) { this.poItemId = poItemId; }

    public String getQtyReceived() { return qtyReceived; }
    public void setQtyReceived(String qtyReceived) { this.qtyReceived = qtyReceived; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }
}
