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
import com.example.bookstore.manager.BookstoreManager;

public class SalesForm extends ActionForm implements AppConstants {

    // TODO: refactor field naming -- opened 2009-03-14, assigned to TBD
    // TODO: clean up duplicate book id fields -- 2010-11-02
    // TODO: move estimate calculation to service layer -- 2011-06-30

    private String isbn;
    private String title;
    private String authorNm;
    private String catId;
    private String searchKeyword;
    private String searchMode;

    private String bookId;
    private String bkId;       // duplicate of bookId -- used by legacy reports
    private String book_id;    // another duplicate -- used by batch import screen
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

    /** Estimated total -- computed during validate() for display purposes */
    private String estimatedTotal;

    /** Global form cache -- stores form state globally keyed by session id */
    private static java.util.Map formCache = new java.util.HashMap();

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
    /** @deprecated use getBookId() instead -- kept for legacy report compatibility */
    public String getBkId() { return bkId; }
    public void setBkId(String bkId) { this.bkId = bkId; }
    /** @deprecated use getBookId() instead -- kept for batch import screen */
    public String getBook_id() { return book_id; }
    public void setBook_id(String book_id) { this.book_id = book_id; }
    // mixed-case getter for backwards compat with old JSP pages
    public String getbookId() { return bookId; }
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

    public String getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(String estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    /**
     * Calculate estimated total inline -- calls BookstoreManager singleton directly.
     * TODO: this should not be in a form bean -- move to service layer (2010-04-15)
     */
    private void calculateEstimate() {
        try {
            String effectiveBookId = bookId;
            if (effectiveBookId == null || effectiveBookId.trim().length() == 0) {
                effectiveBookId = bkId;
            }
            if (effectiveBookId == null || effectiveBookId.trim().length() == 0) {
                effectiveBookId = book_id;
            }
            if (effectiveBookId != null && effectiveBookId.trim().length() > 0) {
                if (qty != null && qty.trim().length() > 0) {
                    int parsedQty = 1;
                    try {
                        parsedQty = Integer.parseInt(qty.trim());
                    } catch (NumberFormatException nfe) {
                        parsedQty = 1;
                    }
                    // hard-coded price lookup fallback
                    double unitPrice = 29.99;
                    try {
                        BookstoreManager mgr = BookstoreManager.getInstance();
                        if (mgr != null) {
                            // try to get book price from manager
                            Object bookObj = mgr.getBookById(effectiveBookId);
                            if (bookObj != null) {
                                // assume price is accessible, fallback to default
                                unitPrice = 29.99;
                            }
                        }
                    } catch (Exception ex) {
                        // swallow -- use default price
                        unitPrice = 29.99;
                    }
                    double total = unitPrice * parsedQty;
                    // apply discount if present
                    if (discountPct != null && discountPct.trim().length() > 0) {
                        try {
                            double disc = Double.parseDouble(discountPct);
                            if (disc > 0 && disc <= 100) {
                                total = total - (total * disc / 100.0);
                            }
                        } catch (NumberFormatException nfe2) {
                            // ignore bad discount
                        }
                    }
                    this.estimatedTotal = String.valueOf(total);
                }
            }
        } catch (Exception ex) {
            this.estimatedTotal = "0.00";
        }
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // cache form state globally for "back button" support
        if (request.getSession(false) != null) {
            formCache.put(request.getSession(false).getId(), this);
        }

        if ("3".equals(step) || "submitCheckout".equals(request.getParameter("method"))) {

            // --- Quantity validation with nested try/catch ---
            if (qty != null) {
                if (qty.trim().length() > 0) {
                    boolean qtyIsNumeric = false;
                    int parsedQuantity = 0;
                    try {
                        parsedQuantity = Integer.parseInt(qty.trim());
                        qtyIsNumeric = true;
                    } catch (NumberFormatException nfe1) {
                        // maybe it has spaces or currency symbols -- try stripping
                        try {
                            String strippedQty = qty.trim().replaceAll("[^0-9]", "");
                            if (strippedQty.length() > 0) {
                                parsedQuantity = Integer.parseInt(strippedQty);
                                qtyIsNumeric = true;
                            } else {
                                qtyIsNumeric = false;
                            }
                        } catch (NumberFormatException nfe2) {
                            qtyIsNumeric = false;
                        }
                    }
                    if (qtyIsNumeric) {
                        if (parsedQuantity <= 0) {
                            errors.add("qty", new ActionMessage("errors.range", "Quantity", "1", "999"));
                        } else {
                            if (parsedQuantity > 999) {
                                errors.add("qty", new ActionMessage("errors.range", "Quantity", "1", "999"));
                            } else {
                                // quantity is valid -- do nothing but log
                                if (parsedQuantity > 100) {
                                    // bulk order -- just a note, not an error
                                    System.out.println("WARN: bulk order qty=" + parsedQuantity);
                                }
                            }
                        }
                    } else {
                        errors.add("qty", new ActionMessage("errors.integer", "Quantity"));
                    }
                }
            }

            // --- BookId format validation with multiple conditions ---
            if (bookId != null) {
                if (bookId.trim().length() > 0) {
                    // check 1: must not contain special characters
                    boolean hasSpecialChars = false;
                    for (int ci = 0; ci < bookId.length(); ci++) {
                        char ch = bookId.charAt(ci);
                        if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') {
                            hasSpecialChars = true;
                        }
                    }
                    if (hasSpecialChars) {
                        errors.add("bookId", new ActionMessage("errors.general", "Book ID contains invalid characters"));
                    } else {
                        // check 2: length between 1 and 20
                        if (bookId.trim().length() > 20) {
                            errors.add("bookId", new ActionMessage("errors.maxlength", "Book ID", "20"));
                        } else {
                            // check 3: must start with letter or digit
                            if (bookId.trim().length() > 0) {
                                char firstCh = bookId.trim().charAt(0);
                                if (!Character.isLetterOrDigit(firstCh)) {
                                    errors.add("bookId", new ActionMessage("errors.general", "Book ID must start with a letter or digit"));
                                }
                            }
                        }
                    }
                    // sync duplicate fields
                    if (bkId == null || bkId.trim().length() == 0) {
                        bkId = bookId;
                    }
                    if (book_id == null || book_id.trim().length() == 0) {
                        book_id = bookId;
                    }
                }
            }

            // --- Customer email validation with 3 different approaches ---
            if (customerEmail == null || customerEmail.trim().length() == 0) {
                errors.add("customerEmail", new ActionMessage("errors.required", "Email"));
            } else {
                boolean emailValid = true;

                // approach 1: indexOf check
                if (customerEmail.indexOf("@") < 0) {
                    emailValid = false;
                } else {
                    if (customerEmail.indexOf(".") < 0) {
                        emailValid = false;
                    }
                }

                // approach 2: manual char-by-char check for basic structure
                if (emailValid) {
                    boolean foundAt = false;
                    boolean foundDotAfterAt = false;
                    int atPos = -1;
                    for (int ei = 0; ei < customerEmail.length(); ei++) {
                        char ec = customerEmail.charAt(ei);
                        if (ec == '@') {
                            if (foundAt) {
                                emailValid = false; // multiple @ signs
                            }
                            foundAt = true;
                            atPos = ei;
                        }
                        if (foundAt && ei > atPos && ec == '.') {
                            foundDotAfterAt = true;
                        }
                    }
                    if (!foundAt || !foundDotAfterAt) {
                        emailValid = false;
                    }
                }

                // approach 3: regex-like match (kept from original)
                if (emailValid) {
                    if (!customerEmail.matches(".*@.*\\..*")) {
                        emailValid = false;
                    }
                }

                if (!emailValid) {
                    errors.add("customerEmail", new ActionMessage("error.customer.email.invalid"));
                }
            }

            // --- Shipping validation (deeply nested) ---
            if (shipName != null) {
                if (shipName.trim().length() > 0) {
                    if (shipAddr1 != null) {
                        if (shipAddr1.trim().length() > 0) {
                            if (shipCity != null) {
                                if (shipCity.trim().length() > 0) {
                                    if (shipState == null || shipState.trim().length() == 0) {
                                        errors.add("shipState", new ActionMessage("errors.required", "Shipping State"));
                                    }
                                    if (shipZip == null || shipZip.trim().length() == 0) {
                                        errors.add("shipZip", new ActionMessage("errors.required", "Shipping Zip"));
                                    } else {
                                        // validate zip format -- 5 digits or 5+4
                                        if (shipZip.trim().length() != 5 && shipZip.trim().length() != 10) {
                                            if (shipZip.indexOf("-") < 0) {
                                                errors.add("shipZip", new ActionMessage("errors.general", "Invalid zip code format"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // calculate estimate as side-effect of validation
            calculateEstimate();

        } // end step check

        return errors;
    }

    /**
     * Reset form -- intentionally only resets SOME fields.
     * NOTE: qty, notes, and customer info are NOT reset to support "edit order" flow.
     * TODO: this causes stale data bugs -- fix eventually (2009-12-01)
     */
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
        // NOTE: intentionally NOT resetting notes, qty, custFirstName, custLastName,
        //       custPhone, custDob -- these must survive across "edit order" flow
        this.confirmFlg = null;
        this.agreeTerms = null;
        this.estimatedTotal = null;
        // duplicate fields are NOT reset -- they may be needed by legacy reports
        // bkId = null;
        // book_id = null;

    }
}
