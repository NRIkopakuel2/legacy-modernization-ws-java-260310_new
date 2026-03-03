package com.example.bookstore.form;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.example.bookstore.constant.AppConstants;

public class StockAdjustmentForm extends ActionForm implements AppConstants {

    private String bookId;
    private String adjType;
    private String qty;
    private String reason;
    private String notes;
    private String tmpVal;
    private String editFlg;

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getAdjType() { return adjType; }
    public void setAdjType(String adjType) { this.adjType = adjType; }

    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTmpVal() { return tmpVal; }
    public void setTmpVal(String tmpVal) { this.tmpVal = tmpVal; }

    public String getEditFlg() { return editFlg; }
    public void setEditFlg(String editFlg) { this.editFlg = editFlg; }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (bookId == null || bookId.trim().length() == 0) {
            errors.add("bookId", new ActionMessage("error.adjustment.bookId.required"));
        }

        if (adjType == null || adjType.trim().length() == 0) {
            errors.add("adjType", new ActionMessage("error.adjustment.type.required"));
        } else if (!"INCREASE".equals(adjType) && !"DECREASE".equals(adjType)) {
            errors.add("adjType", new ActionMessage("error.adjustment.type.invalid"));
        }

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

        if (reason == null || reason.trim().length() == 0) {
            errors.add("reason", new ActionMessage("error.adjustment.reason.required"));
        } else if ("OTHER".equals(reason) && (notes == null || notes.trim().length() == 0)) {
            errors.add("notes", new ActionMessage("error.adjustment.notes.required"));
        }

        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.bookId = null;
        this.adjType = null;

        this.reason = null;
        this.notes = null;
        this.tmpVal = null;
        this.editFlg = null;
    }
}
