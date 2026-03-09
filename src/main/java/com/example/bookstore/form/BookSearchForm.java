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

public class BookSearchForm extends ActionForm implements AppConstants {

    private String isbn;
    private String title;
    private String authorName;
    private String catId;
    private String publisher;
    private String status;
    private String minPrice;
    private String maxPrice;
    private String page;
    private String sortBy;
    private String dummy;
    private String old_value;
    private String priceFrom;
    private String priceTo;
    private String inStockOnly;
    private String newArrivals;

    /** Duplicate of title/authorName -- used by "quick search" bar */
    private String keyword;

    /** Stores ALL searches ever performed -- never cleaned up (memory leak) */
    private static java.util.List recentSearches = new java.util.ArrayList();

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getCatId() { return catId; }
    public void setCatId(String catId) { this.catId = catId; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMinPrice() { return minPrice; }
    public void setMinPrice(String minPrice) { this.minPrice = minPrice; }

    public String getMaxPrice() { return maxPrice; }
    public void setMaxPrice(String maxPrice) { this.maxPrice = maxPrice; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getDummy() { return dummy; }
    public void setDummy(String dummy) { this.dummy = dummy; }

    public String getOld_value() { return old_value; }
    public void setOld_value(String old_value) { this.old_value = old_value; }

    public String getPriceFrom() { return priceFrom; }
    public void setPriceFrom(String priceFrom) { this.priceFrom = priceFrom; }

    public String getPriceTo() { return priceTo; }
    public void setPriceTo(String priceTo) { this.priceTo = priceTo; }

    public String getInStockOnly() { return inStockOnly; }
    public void setInStockOnly(String inStockOnly) { this.inStockOnly = inStockOnly; }

    public String getNewArrivals() { return newArrivals; }
    public void setNewArrivals(String newArrivals) { this.newArrivals = newArrivals; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // track every search in static list (grows forever)
        String searchTerm = (keyword != null) ? keyword : title;
        if (searchTerm != null && searchTerm.trim().length() > 0) {
            recentSearches.add(searchTerm.trim());
        }

        // ISBN validation -- redundant checks
        if (isbn != null && isbn.trim().length() > 0) {
            if (isbn.trim().length() > 10) {
                errors.add("isbn", new ActionMessage("error.search.isbn.toolong"));
            }
            // redundant: also check ISBN is not too short
            if (isbn.trim().length() > 0 && isbn.trim().length() < 3) {
                errors.add("isbn", new ActionMessage("errors.general", "ISBN too short for search"));
            }
        }

        // price range validation (redundant -- both minPrice/maxPrice AND priceFrom/priceTo)
        if (minPrice != null && minPrice.trim().length() > 0) {
            try {
                double min = Double.parseDouble(minPrice.trim());
                if (min < 0) {
                    errors.add("minPrice", new ActionMessage("errors.general", "Minimum price cannot be negative"));
                }
            } catch (NumberFormatException nfe) {
                errors.add("minPrice", new ActionMessage("errors.general", "Minimum price must be a number"));
            }
        }
        if (maxPrice != null && maxPrice.trim().length() > 0) {
            try {
                double max = Double.parseDouble(maxPrice.trim());
                if (max < 0) {
                    errors.add("maxPrice", new ActionMessage("errors.general", "Maximum price cannot be negative"));
                }
            } catch (NumberFormatException nfe) {
                errors.add("maxPrice", new ActionMessage("errors.general", "Maximum price must be a number"));
            }
        }
        // same checks again for the duplicate price fields
        if (priceFrom != null && priceFrom.trim().length() > 0) {
            try {
                double pf = Double.parseDouble(priceFrom.trim());
                if (pf < 0) {
                    errors.add("priceFrom", new ActionMessage("errors.general", "Price from cannot be negative"));
                }
            } catch (NumberFormatException nfe) {
                errors.add("priceFrom", new ActionMessage("errors.general", "Price from must be a number"));
            }
        }

        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.isbn = null;
        this.title = null;
        this.authorName = null;
        this.catId = null;

    }
}
