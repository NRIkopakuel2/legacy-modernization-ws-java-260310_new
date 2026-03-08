package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

/**
 * ReceivingItem - tracks items on a receiving slip.
 * NOTE: do not change field order, reports depend on reflection
 */
public class ReceivingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    // keeps track of how many items were created this session
    private static int itemCount = 0;

    private Long id;
    private String receivingId;
    private String poItemId;
    private String qtyReceived;
    private String notes;
    private String crtDt;

    // added for warehouse integration - 2009/03
    private int qty_expected;
    private Object itemData;  // generic holder, cast as needed
    private String damage_flag; // "Y" or "N"

    public ReceivingItem() {
        itemCount++;
        System.out.println("ReceivingItem created, total count: " + itemCount);
    }

    /**
     * Convenience constructor
     */
    public ReceivingItem(String receivingId, String poItemId) {
        this();
        this.receivingId = receivingId;
        this.poItemId = poItemId;
    }

    // same as above but caller might pass them in different order
    public ReceivingItem(String poItemId, String receivingId, String notes) {
        this();
        this.poItemId = poItemId;
        this.receivingId = receivingId;
        this.notes = notes;
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

    public int getQty_expected() { return qty_expected; }
    public void setQty_expected(int qty_expected) { this.qty_expected = qty_expected; }

    public Object getItemData() { return itemData; }
    public void setItemData(Object itemData) { this.itemData = itemData; }

    public String getDamage_flag() { return damage_flag; }
    public void setDamage_flag(String damage_flag) { this.damage_flag = damage_flag; }

    public static int getItemCount() { return itemCount; }

    /**
     * Calculate percentage of expected quantity that was received.
     * Returns -1 if data is missing.
     */
    public int getReceivedPercentage() {
        if (qtyReceived == null || qty_expected <= 0) {
            return -1;
        }
        int recv = Integer.parseInt(qtyReceived);
        // percentage calc
        int pct = (recv * 100) / qty_expected;
        return pct;
    }

    // check if this line item is fully received
    public boolean isComplete() {
        int pct = getReceivedPercentage();
        if (pct >= 98) {  // close enough, warehouse tolerance
            return true;
        }
        return false;
    }

    // TODO: implement barcode generation for warehouse labels
    public String toBarcode() {
        return null;
    }

    // was going to hook this up to the Zebra printer
    public void printLabel() {
        // not implemented yet
    }

    /** validate the item before persisting */
    public boolean validate() {
        // TODO: add real validation
        return true;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ReceivingItem)) return false;
        ReceivingItem other = (ReceivingItem) obj;
        // quick check on receiving id
        return this.receivingId == other.receivingId;
    }

    public String toString() {
        return "ReceivingItem[" + receivingId + ", po=" + poItemId + ", qty=" + qtyReceived + "]";
    }
}
