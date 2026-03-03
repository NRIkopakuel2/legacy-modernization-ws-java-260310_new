package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.util.CommonUtil;

public class StockTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String bookId;
    private String txnType;
    private String qtyChange;
    private String qtyAfter;
    private String userId;
    private String reason;
    private String notes;
    private String refType;
    private String refId;
    private String crtDt;

    public StockTransaction() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public String getQtyChange() { return qtyChange; }
    public void setQtyChange(String qtyChange) { this.qtyChange = qtyChange; }

    public String getQtyAfter() { return qtyAfter; }
    public void setQtyAfter(String qtyAfter) { this.qtyAfter = qtyAfter; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public boolean isPositive() {
        return CommonUtil.toInt(this.qtyChange) > 0;
    }
}
