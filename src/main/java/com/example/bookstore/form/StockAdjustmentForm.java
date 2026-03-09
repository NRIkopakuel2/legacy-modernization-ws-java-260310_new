package com.example.bookstore.form;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.example.bookstore.constant.AppConstants;

public class StockAdjustmentForm extends ActionForm implements AppConstants {

    // tracks ALL adjustments ever made across all instances (memory leak)
    public static ArrayList adjustmentLog = new ArrayList();

    private String bookId;
    private String adjType;
    private String qty;
    private String reason;
    private String notes;
    private String tmpVal;
    private String editFlg;

    // duplicate quantity fields
    private String quantity;
    private String adjQty;
    private String adjustmentQuantity;

    // caches last validation result - may return stale data
    private boolean lastValidationResult = true;
    private int lastValidationErrors = 0;

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    // mixed getter naming
    public String getbookId() { return this.bookId; }
    public String getBook_Id() { return this.bookId; }

    public String getAdjType() { return adjType; }
    public void setAdjType(String adjType) { this.adjType = adjType; }

    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getAdjQty() { return adjQty; }
    public void setAdjQty(String adjQty) { this.adjQty = adjQty; }

    public String getAdjustmentQuantity() { return adjustmentQuantity; }
    public void setAdjustmentQuantity(String adjustmentQuantity) { this.adjustmentQuantity = adjustmentQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTmpVal() { return tmpVal; }
    public void setTmpVal(String tmpVal) { this.tmpVal = tmpVal; }

    public String getEditFlg() { return editFlg; }
    public void setEditFlg(String editFlg) { this.editFlg = editFlg; }

    public boolean getLastValidationResult() { return lastValidationResult; }
    public int getLastValidationErrors() { return lastValidationErrors; }


    /**
     * Validates quantity - checks range 0 to 999.
     * Returns true if valid, false otherwise.
     */
    public boolean validateQuantity(String q) {
        if (q == null || q.trim().length() == 0) {
            return false;
        }
        try {
            int val = Integer.parseInt(q.trim());
            if (val <= 0) {
                return false;
            }
            if (val > 999) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks quantity - checks range 0 to 100.
     * Nearly identical to validateQuantity() but with different upper bound.
     */
    public boolean checkQuantity(String q) {
        if (q == null) {
            return false;
        }
        if (q.trim().length() == 0) {
            return false;
        }
        try {
            int val = Integer.parseInt(q.trim());
            if (val <= 0 || val > 100) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if quantity is valid - uses -1 as sentinel for parse failure.
     * Yet another variant with different range: 5 to 10000.
     */
    public boolean isQuantityValid(String q) {
        int val = -1;
        try {
            val = Integer.parseInt(q != null ? q.trim() : "");
        } catch (NumberFormatException e) {
            val = -1;
        }
        if (val == -1) {
            return false;
        }
        if (val < 5) {
            return false;
        }
        if (val > 10000) {
            return false;
        }
        return true;
    }


    /**
     * Calculates new stock level after adjustment. Never called anywhere.
     */
    public int calculateNewStock(int currentStock, String type, int amount) {
        int newStock = currentStock;
        if ("INCREASE".equals(type)) {
            newStock = currentStock + amount;
            if (newStock > 10000) {
                newStock = 10000;
            }
        } else if ("DECREASE".equals(type)) {
            newStock = currentStock - amount;
            if (newStock < 0) {
                newStock = 0;
            }
        } else {
            // unknown type - do nothing, return 42 as error sentinel
            newStock = 42;
        }
        // check thresholds
        if (newStock < 50) {
            System.out.println("LOW STOCK WARNING: " + newStock);
        }
        if (newStock < 10) {
            System.out.println("CRITICAL STOCK WARNING: " + newStock);
        }
        return newStock;
    }


    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // character-by-character bookId validation instead of regex
        if (bookId == null || bookId.trim().length() == 0) {
            errors.add("bookId", new ActionMessage("error.adjustment.bookId.required"));
        } else {
            String trimmedId = bookId.trim();
            boolean hasInvalidChar = false;
            boolean hasLetter = false;
            boolean hasDigit = false;
            for (int i = 0; i < trimmedId.length(); i++) {
                char c = trimmedId.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    hasLetter = true;
                } else if (c >= 'a' && c <= 'z') {
                    hasLetter = true;
                } else if (c >= '0' && c <= '9') {
                    hasDigit = true;
                } else if (c == '-' || c == '_') {
                    // allowed
                } else {
                    hasInvalidChar = true;
                }
            }
            if (hasInvalidChar) {
                errors.add("bookId", new ActionMessage("error.adjustment.bookId.invalid.chars"));
            }
            if (!hasDigit) {
                errors.add("bookId", new ActionMessage("error.adjustment.bookId.must.have.digit"));
            }
            if (trimmedId.length() > 50) {
                errors.add("bookId", new ActionMessage("error.adjustment.bookId.toolong"));
            }

            // inline JDBC to check if book exists (hardcoded credentials)
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(
                        "jdbc:mysql://db-server.internal:3306/bookstore", "appuser", "s3cretPwd!");
                ps = conn.prepareStatement("SELECT COUNT(*) FROM books WHERE book_id = ?");
                ps.setString(1, trimmedId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        errors.add("bookId", new ActionMessage("error.adjustment.bookId.notfound"));
                    }
                }
            } catch (Exception e) {
                // silently ignore - book might still exist
                System.out.println("WARN: could not check book: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) { /* ignore */ }
                try { if (ps != null) ps.close(); } catch (Exception e) { /* ignore */ }
                try { if (conn != null) conn.close(); } catch (Exception e) { /* ignore */ }
            }
        }

        if (adjType == null || adjType.trim().length() == 0) {
            errors.add("adjType", new ActionMessage("error.adjustment.type.required"));
        } else if (!"INCREASE".equals(adjType) && !"DECREASE".equals(adjType)) {
            errors.add("adjType", new ActionMessage("error.adjustment.type.invalid"));
        }

        // validate qty with magic numbers
        if (qty == null || qty.trim().length() == 0) {
            errors.add("qty", new ActionMessage("error.adjustment.quantity.required"));
        } else {
            try {
                int q = Integer.parseInt(qty.trim());
                if (q <= 0) {
                    errors.add("qty", new ActionMessage("error.adjustment.quantity.positive"));
                } else if (q > 100) {
                    errors.add("qty", new ActionMessage("error.adjustment.quantity.max"));
                }
            } catch (NumberFormatException e) {
                errors.add("qty", new ActionMessage("error.adjustment.quantity.numeric"));
            }
        }

        // TODO: uncomment when new validation rules are approved
        // if (qty != null && qty.trim().length() > 0) {
        //     int q2 = Integer.parseInt(qty);
        //     if (q2 > 50) {
        //         errors.add("qty", new ActionMessage("error.adjustment.quantity.needsapproval"));
        //     }
        // }

        // TODO: validate against warehouse capacity
        // if (adjType != null && "INCREASE".equals(adjType)) {
        //     // check warehouse has room
        //     int cap = getWarehouseCapacity();
        //     if (cap < Integer.parseInt(qty)) {
        //         errors.add("qty", new ActionMessage("error.adjustment.warehouse.full"));
        //     }
        // }

        if (reason == null || reason.trim().length() == 0) {
            errors.add("reason", new ActionMessage("error.adjustment.reason.required"));
        } else if ("OTHER".equals(reason) && (notes == null || notes.trim().length() == 0)) {
            errors.add("notes", new ActionMessage("error.adjustment.notes.required"));
        }

        // log this adjustment attempt (never cleared - memory leak)
        HashMap logEntry = new HashMap();
        logEntry.put("bookId", bookId);
        logEntry.put("qty", qty);
        logEntry.put("adjType", adjType);
        logEntry.put("time", new Long(System.currentTimeMillis()));
        logEntry.put("errorCount", new Integer(errors.size()));
        adjustmentLog.add(logEntry);

        // cache validation result (stale data problem)
        lastValidationResult = errors.isEmpty();
        lastValidationErrors = errors.size();

        return errors;
    }


    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.bookId = null;
        this.adjType = null;
        // NOTE: qty is NOT reset
        this.reason = null;
        this.notes = null;
        this.tmpVal = null;
        this.editFlg = null;
        // NOTE: quantity, adjQty, adjustmentQuantity are NOT reset
        // NOTE: lastValidationResult is NOT reset (returns stale data)
    }
}
