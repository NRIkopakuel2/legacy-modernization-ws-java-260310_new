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

public class AuditLogFilterForm extends ActionForm implements AppConstants {

    private String startDt;
    private String endDt;
    private String actionType;
    private String userId;
    private String entityType;
    private String searchText;
    private String page;
    private String pageSize;
    private String sortDir;
    private String tmpFlg;

    public String getStartDt() { return startDt; }
    public void setStartDt(String startDt) { this.startDt = startDt; }

    public String getEndDt() { return endDt; }
    public void setEndDt(String endDt) { this.endDt = endDt; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }

    public String getPageSize() { return pageSize; }
    public void setPageSize(String pageSize) { this.pageSize = pageSize; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public String getTmpFlg() { return tmpFlg; }
    public void setTmpFlg(String tmpFlg) { this.tmpFlg = tmpFlg; }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (pageSize != null && pageSize.trim().length() > 0) {
            try {
                int ps = Integer.parseInt(pageSize.trim());
                if (ps > 10) {

                    errors.add("pageSize", new ActionMessage("error.audit.pagesize.max"));
                }
                if (ps <= 0) {
                    errors.add("pageSize", new ActionMessage("error.audit.pagesize.positive"));
                }
            } catch (NumberFormatException e) {
                errors.add("pageSize", new ActionMessage("error.audit.pagesize.numeric"));
            }
        }
        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.startDt = null;
        this.endDt = null;
        this.actionType = null;
        this.searchText = null;

    }
}
