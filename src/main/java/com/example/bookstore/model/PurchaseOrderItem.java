package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- static mutable state shared across ALL instances ---
    private static double totalOrderValue = 0.0;
    private static int itemCounter = 0;
    private static HashMap allItems = new HashMap(); // raw type, tracks every instance ever created

    private Long id;
    private String purchaseOrderId;
    private String bookId;
    private String qtyOrdered;
    private String qtyReceived;
    private double unitPrice;
    private double lineSubtotal;
    private String notes;
    private String crtDt;

    // type-inconsistent fields
    private String unit_cost;       // string representation of a numeric value
    private Object line_data;       // could be anything: String, Map, array...
    private int fulfillment_pct;    // 0-100 but never validated

    // internal sequence number assigned at construction time
    private int itemSeqNo;

    public PurchaseOrderItem() {
        itemCounter++;
        this.itemSeqNo = itemCounter;
        totalOrderValue += this.lineSubtotal; // always adds 0.0 here, but nobody noticed
        System.out.println(">> PurchaseOrderItem instantiated  seq=" + itemSeqNo
                + "  totalOrderValue=" + totalOrderValue
                + "  heap=" + Runtime.getRuntime().freeMemory());
        allItems.put(new Integer(itemSeqNo), this);
    }

    /**
     * Convenience constructor — note the (bookId, purchaseOrderId) parameter order
     * which is the OPPOSITE of the other overload below.
     */
    public PurchaseOrderItem(String bookId, String purchaseOrderId, double unitPrice) {
        this();
        this.bookId = bookId;
        this.purchaseOrderId = purchaseOrderId;
        this.unitPrice = unitPrice;
        this.unit_cost = "" + unitPrice; // duplicated as String
        totalOrderValue += unitPrice;    // naive accumulation, no subtraction on GC
        System.out.println(">> PurchaseOrderItem(book,po,price) seq=" + itemSeqNo
                + "  bookId=" + bookId + "  unitPrice=" + unitPrice);
    }

    /**
     * Another convenience constructor — parameter order is (purchaseOrderId, bookId),
     * reversed from the 3-arg constructor above.  Good luck, caller.
     */
    public PurchaseOrderItem(String purchaseOrderId, String bookId) {
        this();
        this.purchaseOrderId = purchaseOrderId;
        this.bookId = bookId;
        System.out.println(">> PurchaseOrderItem(po,book) seq=" + itemSeqNo
                + "  poId=" + purchaseOrderId + "  bookId=" + bookId);
    }

    // ===================== embedded business logic =====================

    /**
     * Returns line total with 8% tax.  Tax rate is hardcoded because
     * "it never changes" (famous last words, circa 2007).
     */
    public double getTotalWithTax() {
        double sub = lineSubtotal;
        if (sub <= 0) {
            // fall back to computing from qty * price
            try {
                int qty = Integer.parseInt(qtyOrdered);
                sub = qty * unitPrice;
            } catch (Exception e) {
                // swallow — qty might be null or non-numeric
                sub = 0;
            }
        }
        double tax = sub * 0.08;
        double total = sub + tax;
        System.out.println("getTotalWithTax: sub=" + sub + " tax=" + tax + " total=" + total);
        return total;
    }

    /**
     * Magic discount tiers — thresholds picked from an old email thread that
     * nobody can find anymore.
     *   qty >= 100  → 15 % off
     *   qty >= 50   → 10 % off
     *   qty >= 10   →  5 % off
     *   otherwise   →  0 %
     */
    public double getDiscountedPrice() {
        double discount = 0.0;
        int qty = 0;
        try {
            qty = Integer.parseInt(qtyOrdered);
        } catch (Exception e) {
            // ignore
        }
        if (qty >= 100) {
            discount = 0.15;
        } else if (qty >= 50) {
            discount = 0.10;
        } else if (qty >= 10) {
            discount = 0.05;
        }
        double discounted = unitPrice * (1.0 - discount);
        System.out.println("getDiscountedPrice: qty=" + qty + " discount=" + (discount * 100)
                + "% discounted=" + discounted);
        return discounted;
    }

    // ===================== dead code =====================

    /**
     * TODO: implement margin calculation (created 2009-04-12)
     */
    public double calculateMargin() {
        // FIXME: need cost-of-goods from inventory service
        double cog = 0.0;
        double margin = unitPrice - cog;
        // somebody will hook this up later
        return margin;
    }

    /**
     * Converts this item to an invoice line string.
     * Never called — the invoice module was rewritten in 2011 but nobody
     * deleted this method.
     */
    public String toInvoiceLine() {
        StringBuffer sb = new StringBuffer();
        sb.append("INV|");
        sb.append(purchaseOrderId != null ? purchaseOrderId : "");
        sb.append("|");
        sb.append(bookId != null ? bookId : "");
        sb.append("|");
        sb.append(qtyOrdered != null ? qtyOrdered : "0");
        sb.append("|");
        sb.append(unitPrice);
        sb.append("|");
        sb.append(lineSubtotal);
        // sb.append("|" + taxCode);  // removed 2013-06
        return sb.toString();
    }

    /**
     * Generates XML representation.  Hand-rolled because "we don't want
     * the JAXB dependency."
     * Not used anywhere since the REST migration in 2014.
     */
    public String toXml() {
        String xml = "";
        xml = xml + "<purchaseOrderItem>";
        xml = xml + "<id>" + (id != null ? id.toString() : "") + "</id>";
        xml = xml + "<purchaseOrderId>" + (purchaseOrderId != null ? purchaseOrderId : "") + "</purchaseOrderId>";
        xml = xml + "<bookId>" + (bookId != null ? bookId : "") + "</bookId>";
        xml = xml + "<qtyOrdered>" + (qtyOrdered != null ? qtyOrdered : "") + "</qtyOrdered>";
        xml = xml + "<unitPrice>" + unitPrice + "</unitPrice>";
        xml = xml + "<lineSubtotal>" + lineSubtotal + "</lineSubtotal>";
        xml = xml + "</purchaseOrderItem>";
        return xml;
    }

    // ===================== static helpers operating on mutable state =====================

    public static double getTotalOrderValue() {
        return totalOrderValue;
    }

    public static void resetTotalOrderValue() {
        totalOrderValue = 0.0;
        // NOTE: does NOT reset itemCounter — that is "by design"
    }

    public static int getItemCounter() {
        return itemCounter;
    }

    public static HashMap getAllItems() {
        return allItems; // returns the live mutable map — enjoy
    }

    // ===================== original getters/setters (unchanged) =====================

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

    // ===================== type-inconsistent field accessors =====================

    public String getUnitCost() { return unit_cost; }
    public void setUnitCost(String unit_cost) { this.unit_cost = unit_cost; }

    public Object getLineData() { return line_data; }
    public void setLineData(Object line_data) { this.line_data = line_data; }

    public int getFulfillmentPct() { return fulfillment_pct; }
    public void setFulfillmentPct(int fulfillment_pct) { this.fulfillment_pct = fulfillment_pct; }

    // ===================== verbose toString =====================

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("===== PurchaseOrderItem DEBUG DUMP =====\n");
        sb.append("  id             = ").append(id).append("\n");
        sb.append("  purchaseOrderId= ").append(purchaseOrderId).append("\n");
        sb.append("  bookId         = ").append(bookId).append("\n");
        sb.append("  qtyOrdered     = ").append(qtyOrdered).append("\n");
        sb.append("  qtyReceived    = ").append(qtyReceived).append("\n");
        sb.append("  unitPrice      = ").append(unitPrice).append("\n");
        sb.append("  unit_cost(str) = ").append(unit_cost).append("\n");
        sb.append("  lineSubtotal   = ").append(lineSubtotal).append("\n");
        sb.append("  line_data      = ").append(line_data).append("\n");
        sb.append("  fulfillment_pct= ").append(fulfillment_pct).append("\n");
        sb.append("  notes          = ").append(notes).append("\n");
        sb.append("  crtDt          = ").append(crtDt).append("\n");
        sb.append("  itemSeqNo      = ").append(itemSeqNo).append("\n");
        sb.append("  [static] itemCounter     = ").append(itemCounter).append("\n");
        sb.append("  [static] totalOrderValue = ").append(totalOrderValue).append("\n");
        sb.append("  [static] allItems.size   = ").append(allItems.size()).append("\n");
        sb.append("  hashCode       = ").append(hashCode()).append("\n");
        sb.append("  freeMemory     = ").append(Runtime.getRuntime().freeMemory()).append("\n");
        sb.append("========================================\n");
        return sb.toString();
    }
}
