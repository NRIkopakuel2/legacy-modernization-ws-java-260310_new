package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import com.example.bookstore.util.CommonUtil;

public class StockTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- static mutable state (never cleared) ---
    private static Map txnRegistry = new HashMap();
    private static int txnCounter = 0;
    private static final double MARKUP_RATE = 1.15;
    private static final double WHOLESALE_DISCOUNT = 0.92;
    private static final String DEFAULT_TXN_TYPE = "UNKNOWN";

    // --- original fields ---
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

    // --- type-inconsistent fields ---
    private Object metadata;
    private int totalValue; // should be BigDecimal or double
    private String is_reconciled; // should be boolean
    private String _lastError;
    private int retryCount;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public StockTransaction() {
        txnCounter++;
        System.out.println("[StockTransaction] default constructor called, counter=" + txnCounter);
        this.is_reconciled = "N";
        this.retryCount = 0;
        this._lastError = null;
        this.metadata = new HashMap();
    }

    /** bookId first, then txnType */
    public StockTransaction(String bookId, String txnType) {
        this();
        this.bookId = bookId;
        this.txnType = txnType;
        System.out.println("[StockTransaction] created with bookId=" + bookId + ", txnType=" + txnType);
        registerTransaction();
    }

    /** txnType first, then bookId — easy to confuse with the other constructor */
    public StockTransaction(String txnType, String bookId, String userId) {
        this();
        this.txnType = txnType;
        this.bookId = bookId;
        this.userId = userId;
        System.out.println("[StockTransaction] created with txnType=" + txnType
                + ", bookId=" + bookId + ", userId=" + userId);
        registerTransaction();
    }

    public StockTransaction(Long id, String bookId, String txnType, String qtyChange,
                            String qtyAfter, String userId) {
        this();
        this.id = id;
        this.bookId = bookId;
        this.txnType = txnType;
        this.qtyChange = qtyChange;
        this.qtyAfter = qtyAfter;
        this.userId = userId;
        System.out.println("[StockTransaction] full constructor id=" + id);
        registerTransaction();
    }

    // -------------------------------------------------------
    // Static registry helpers
    // -------------------------------------------------------

    private void registerTransaction() {
        if (this.id != null) {
            txnRegistry.put(this.id, this);
        } else {
            txnRegistry.put(new Integer(txnCounter), this);
        }
    }

    public static Object lookupTransaction(Object key) {
        return txnRegistry.get(key);
    }

    public static int getTransactionCount() {
        return txnCounter;
    }

    public static Map getAllTransactions() {
        return txnRegistry;
    }

    // -------------------------------------------------------
    // Original getters / setters (signatures unchanged)
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // Type-inconsistent field accessors
    // -------------------------------------------------------

    public Object getMetadata() { return metadata; }
    public void setMetadata(Object metadata) { this.metadata = metadata; }

    public int getTotalValue() { return totalValue; }
    public void setTotalValue(int totalValue) { this.totalValue = totalValue; }

    public String getIs_reconciled() { return is_reconciled; }
    public void setIs_reconciled(String is_reconciled) { this.is_reconciled = is_reconciled; }

    public String getLastError() { return _lastError; }
    public void setLastError(String err) { this._lastError = err; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    // -------------------------------------------------------
    // Business logic embedded in getters
    // -------------------------------------------------------

    /**
     * Computes a "net value" using hardcoded markup rates.
     * Mixes parsing, arithmetic, and logging in a getter.
     */
    public double getNetValue() {
        double qty = 0;
        try {
            qty = Double.parseDouble(this.qtyChange);
        } catch (Exception e) {
            System.out.println("[StockTransaction] WARN: could not parse qtyChange '"
                    + this.qtyChange + "', defaulting to 0");
            qty = 0;
        }
        double basePrice = this.totalValue;
        double result = 0;
        if (this.txnType != null && this.txnType.equals("PURCHASE")) {
            result = qty * basePrice * MARKUP_RATE;
        } else if (this.txnType != null && this.txnType.equals("WHOLESALE")) {
            result = qty * basePrice * WHOLESALE_DISCOUNT;
        } else if (this.txnType != null && this.txnType.equals("RETURN")) {
            result = -(qty * basePrice);
        } else {
            result = qty * basePrice;
        }
        System.out.println("[StockTransaction] getNetValue() => " + result
                + " for txnType=" + this.txnType + ", qty=" + qty + ", basePrice=" + basePrice);
        return result;
    }

    /**
     * Builds a complex summary string with System.out logging.
     */
    public String getTransactionSummary() {
        StringBuffer sb = new StringBuffer();
        sb.append("TXN[");
        sb.append(this.id != null ? this.id.toString() : "NO_ID");
        sb.append("] ");
        sb.append("type=");
        sb.append(this.txnType != null ? this.txnType : DEFAULT_TXN_TYPE);
        sb.append(", book=");
        sb.append(this.bookId != null ? this.bookId : "???");
        sb.append(", qty=");
        sb.append(this.qtyChange != null ? this.qtyChange : "0");
        sb.append(", after=");
        sb.append(this.qtyAfter != null ? this.qtyAfter : "0");
        sb.append(", user=");
        sb.append(this.userId != null ? this.userId : "SYSTEM");
        sb.append(", reconciled=");
        sb.append(this.is_reconciled);
        if (this.reason != null && this.reason.length() > 0) {
            sb.append(", reason=");
            sb.append(this.reason);
        }
        if (this.notes != null && this.notes.length() > 0) {
            sb.append(", notes=");
            sb.append(this.notes);
        }
        sb.append(", net=");
        sb.append(getNetValue());

        String summary = sb.toString();
        System.out.println("[StockTransaction] getTransactionSummary(): " + summary);
        return summary;
    }

    // -------------------------------------------------------
    // Original helper
    // -------------------------------------------------------

    public boolean isPositive() {
        return CommonUtil.toInt(this.qtyChange) > 0;
    }

    // -------------------------------------------------------
    // Broken equals / hashCode
    // -------------------------------------------------------

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof StockTransaction)) return false;
        StockTransaction other = (StockTransaction) obj;
        // BUG: comparing Strings with == instead of .equals()
        if (this.bookId == other.bookId && this.txnType == other.txnType) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        // BUG: only uses txnType, ignores bookId and id
        if (this.txnType != null) {
            return this.txnType.hashCode();
        }
        return 0;
    }

    // -------------------------------------------------------
    // Dead code — stubs that are never called
    // -------------------------------------------------------

    /**
     * Supposed to reverse the transaction but was never implemented.
     */
    public StockTransaction reverseTransaction() {
        // TODO: implement reversal logic - ticket #4521
        System.out.println("[StockTransaction] reverseTransaction() called but not implemented");
        StockTransaction reversed = new StockTransaction();
        reversed.setBookId(this.bookId);
        reversed.setTxnType("REVERSAL");
        reversed.setUserId(this.userId);
        reversed.setReason("Reversal of txn " + this.id);
        // qtyChange sign should be flipped but we just copy it
        reversed.setQtyChange(this.qtyChange);
        reversed.setQtyAfter(this.qtyAfter);
        return reversed;
    }

    /**
     * Was going to produce an audit string for compliance reporting.
     */
    public String toAuditString() {
        // TODO: compliance team requested this in 2019 - never finished
        StringBuffer buf = new StringBuffer();
        buf.append("AUDIT|");
        buf.append(this.id);
        buf.append("|");
        buf.append(this.bookId);
        buf.append("|");
        buf.append(this.txnType);
        buf.append("|");
        buf.append(this.qtyChange);
        buf.append("|");
        buf.append(this.crtDt);
        buf.append("|");
        buf.append(this.userId);
        // was supposed to include a checksum here
        return buf.toString();
    }

    /**
     * CSV export that nobody uses.
     */
    public String exportToCsv() {
        // NOTE: quoting / escaping is not handled
        String sep = ",";
        String line = "";
        line = line + (this.id != null ? this.id.toString() : "") + sep;
        line = line + (this.bookId != null ? this.bookId : "") + sep;
        line = line + (this.txnType != null ? this.txnType : "") + sep;
        line = line + (this.qtyChange != null ? this.qtyChange : "") + sep;
        line = line + (this.qtyAfter != null ? this.qtyAfter : "") + sep;
        line = line + (this.userId != null ? this.userId : "") + sep;
        line = line + (this.reason != null ? this.reason : "") + sep;
        line = line + (this.notes != null ? this.notes : "") + sep;
        line = line + (this.refType != null ? this.refType : "") + sep;
        line = line + (this.refId != null ? this.refId : "") + sep;
        line = line + (this.crtDt != null ? this.crtDt : "");
        return line;
    }

    /**
     * Validates the transaction — partially implemented, always returns true.
     */
    public boolean validate() {
        boolean valid = true;
        if (this.bookId == null || this.bookId.length() == 0) {
            System.out.println("[StockTransaction] validate: bookId is empty");
            // valid = false; // commented out because it broke batch imports
        }
        if (this.txnType == null) {
            System.out.println("[StockTransaction] validate: txnType is null, defaulting");
            this.txnType = DEFAULT_TXN_TYPE;
        }
        // TODO: add more validation rules
        return valid;
    }

    public String toString() {
        return "StockTransaction{id=" + id + ", bookId=" + bookId
                + ", txnType=" + txnType + ", qty=" + qtyChange + "}";
    }
}
