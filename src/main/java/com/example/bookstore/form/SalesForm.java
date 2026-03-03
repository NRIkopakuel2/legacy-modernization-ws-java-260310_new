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

public class SalesForm extends ActionForm implements AppConstants {

    private String isbn;
    private String title;
    private String authorNm;
    private String catId;
    private String searchKeyword;
    private String searchMode;

    private String bookId;
    private String qty;
    private String cartItemId;
    private String cartAction;
    private String selectedBookId;

    private String customerEmail;
    private String payMethod;
    private String shipName;
    private String shipAddr1;
    private String ship_addr2;
    private String shipCity;
    private String shipState;
    private String shipZip;
    private String shipCountry;
    private String shipPhone;
    private String notes;

    private String custFirstName;
    private String custLastName;
    private String custPhone;
    private String custDob;
    private String custEmail;

    private String mode;
    private String step;
    private String editFlg;
    private String tmpFlg;
    private String procSts;
    private String bkupFlg;
    private String confirmFlg;
    private String guestFlg;

    private String field1;
    private String field2;
    private String field3;

    private String page;
    private String pageSize;
    private String sortBy;
    private String sortDir;
    private String displayMode;

    private String couponCode;
    private String discountPct;
    private String orderNotes;
    private String referralCode;
    private String agreeTerms;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorNm() { return authorNm; }
    public void setAuthorNm(String authorNm) { this.authorNm = authorNm; }
    public String getCatId() { return catId; }
    public void setCatId(String catId) { this.catId = catId; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public String getSearchMode() { return searchMode; }
    public void setSearchMode(String searchMode) { this.searchMode = searchMode; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    public String getCartAction() { return cartAction; }
    public void setCartAction(String cartAction) { this.cartAction = cartAction; }
    public String getSelectedBookId() { return selectedBookId; }
    public void setSelectedBookId(String selectedBookId) { this.selectedBookId = selectedBookId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }
    public String getShipAddr1() { return shipAddr1; }
    public void setShipAddr1(String shipAddr1) { this.shipAddr1 = shipAddr1; }
    public String getShip_addr2() { return ship_addr2; }
    public void setShip_addr2(String ship_addr2) { this.ship_addr2 = ship_addr2; }
    public String getShipCity() { return shipCity; }
    public void setShipCity(String shipCity) { this.shipCity = shipCity; }
    public String getShipState() { return shipState; }
    public void setShipState(String shipState) { this.shipState = shipState; }
    public String getShipZip() { return shipZip; }
    public void setShipZip(String shipZip) { this.shipZip = shipZip; }
    public String getShipCountry() { return shipCountry; }
    public void setShipCountry(String shipCountry) { this.shipCountry = shipCountry; }
    public String getShipPhone() { return shipPhone; }
    public void setShipPhone(String shipPhone) { this.shipPhone = shipPhone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String custFirstName) { this.custFirstName = custFirstName; }
    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String custLastName) { this.custLastName = custLastName; }
    public String getCustPhone() { return custPhone; }
    public void setCustPhone(String custPhone) { this.custPhone = custPhone; }
    public String getCustDob() { return custDob; }
    public void setCustDob(String custDob) { this.custDob = custDob; }
    public String getCustEmail() { return custEmail; }
    public void setCustEmail(String custEmail) { this.custEmail = custEmail; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }
    public String getEditFlg() { return editFlg; }
    public void setEditFlg(String editFlg) { this.editFlg = editFlg; }
    public String getTmpFlg() { return tmpFlg; }
    public void setTmpFlg(String tmpFlg) { this.tmpFlg = tmpFlg; }
    public String getProcSts() { return procSts; }
    public void setProcSts(String procSts) { this.procSts = procSts; }
    public String getBkupFlg() { return bkupFlg; }
    public void setBkupFlg(String bkupFlg) { this.bkupFlg = bkupFlg; }
    public String getConfirmFlg() { return confirmFlg; }
    public void setConfirmFlg(String confirmFlg) { this.confirmFlg = confirmFlg; }
    public String getGuestFlg() { return guestFlg; }
    public void setGuestFlg(String guestFlg) { this.guestFlg = guestFlg; }

    public String getField1() { return field1; }
    public void setField1(String field1) { this.field1 = field1; }
    public String getField2() { return field2; }
    public void setField2(String field2) { this.field2 = field2; }
    public String getField3() { return field3; }
    public void setField3(String field3) { this.field3 = field3; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getPageSize() { return pageSize; }
    public void setPageSize(String pageSize) { this.pageSize = pageSize; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public String getDisplayMode() { return displayMode; }
    public void setDisplayMode(String displayMode) { this.displayMode = displayMode; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getDiscountPct() { return discountPct; }
    public void setDiscountPct(String discountPct) { this.discountPct = discountPct; }
    public String getOrderNotes() { return orderNotes; }
    public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public String getAgreeTerms() { return agreeTerms; }
    public void setAgreeTerms(String agreeTerms) { this.agreeTerms = agreeTerms; }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ("3".equals(step) || "submitCheckout".equals(request.getParameter("method"))) {
            if (customerEmail == null || customerEmail.trim().length() == 0) {
                errors.add("customerEmail", new ActionMessage("errors.required", "Email"));
            } else if (!customerEmail.matches(".*@.*\\..*")) {

                errors.add("customerEmail", new ActionMessage("error.customer.email.invalid"));
            }

            if (qty != null && qty.trim().length() > 0) {
                try {
                    int q = Integer.parseInt(qty);
                    if (q <= 0 || q > 999) {
                        errors.add("qty", new ActionMessage("errors.range", "Quantity", "1", "999"));
                    }
                } catch (NumberFormatException e) {
                    errors.add("qty", new ActionMessage("errors.integer", "Quantity"));
                }
            }
        }

        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {

        this.customerEmail = null;
        this.payMethod = null;
        this.shipName = null;
        this.shipAddr1 = null;
        this.ship_addr2 = null;
        this.shipCity = null;
        this.shipState = null;
        this.shipZip = null;
        this.shipCountry = null;
        this.shipPhone = null;
        this.notes = null;
        this.confirmFlg = null;
        this.agreeTerms = null;

        this.custFirstName = null;
        this.custLastName = null;
        this.custPhone = null;
        this.custDob = null;

    }
}
