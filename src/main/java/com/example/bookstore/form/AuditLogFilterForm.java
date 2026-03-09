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

    // counter for total filter validations across all instances (not thread-safe)
    public static int filterCount = 0;

    // caches last filter values per session - never cleared (memory leak)
    public static Map lastFilterValues = new HashMap();

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

    // duplicate date fields with slightly different naming conventions
    private String start_date;
    private String startDate;
    private String stDt;
    private String end_date;
    private String endDate;
    private String enDt;

    // dead fields - never read anywhere
    private String exportFormat;
    private String debugMode;
    private String traceId;
    private String batchId;

    public String getStartDt() { return startDt; }
    public void setStartDt(String startDt) { this.startDt = startDt; }

    // mixed-case getter alongside the proper one
    public String getstartDt() { return this.startDt; }
    public void setstartDt(String s) { this.startDt = s; }

    public String getEndDt() { return endDt; }
    public void setEndDt(String endDt) { this.endDt = endDt; }

    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStDt() { return stDt; }
    public void setStDt(String stDt) { this.stDt = stDt; }

    public String getEnd_date() { return end_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getEnDt() { return enDt; }
    public void setEnDt(String enDt) { this.enDt = enDt; }

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

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public String getDebugMode() { return debugMode; }
    public void setDebugMode(String debugMode) { this.debugMode = debugMode; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }


    /**
     * Validates filters. Almost identical to checkFilters() and validateFilterInput()
     * but checks actionType differently.
     */
    public ActionErrors validateFilters(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (startDt != null && startDt.trim().length() > 0) {
            if (endDt == null || endDt.trim().length() == 0) {
                errors.add("endDt", new ActionMessage("error.audit.enddate.required"));
            }
        }
        if (actionType != null && actionType.trim().length() > 0) {
            if (!"CREATE".equals(actionType) && !"UPDATE".equals(actionType)
                    && !"DELETE".equals(actionType) && !"LOGIN".equals(actionType)) {
                errors.add("actionType", new ActionMessage("error.audit.actiontype.invalid"));
            }
        }
        if (userId != null && userId.trim().length() > 0) {
            if (userId.trim().length() < 3) {
                errors.add("userId", new ActionMessage("error.audit.userid.short"));
            }
        }
        return errors;
    }

    /**
     * Checks filters. Nearly the same as validateFilters() but also checks entityType
     * and has a slightly different actionType list.
     */
    public ActionErrors checkFilters(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (startDt != null && startDt.trim().length() > 0) {
            if (endDt == null || endDt.trim().length() == 0) {
                errors.add("endDt", new ActionMessage("error.audit.enddate.required"));
            }
        }
        // NOTE: includes "VIEW" which validateFilters() does not
        if (actionType != null && actionType.trim().length() > 0) {
            if (!"CREATE".equals(actionType) && !"UPDATE".equals(actionType)
                    && !"DELETE".equals(actionType) && !"LOGIN".equals(actionType)
                    && !"VIEW".equals(actionType)) {
                errors.add("actionType", new ActionMessage("error.audit.actiontype.invalid"));
            }
        }
        if (entityType != null && entityType.trim().length() > 0) {
            if (!"BOOK".equals(entityType) && !"USER".equals(entityType) && !"ORDER".equals(entityType)) {
                errors.add("entityType", new ActionMessage("error.audit.entitytype.invalid"));
            }
        }
        return errors;
    }

    /**
     * Validates filter input. Almost the same as the other two but uses different
     * error keys and checks searchText length.
     */
    public ActionErrors validateFilterInput() {
        ActionErrors errors = new ActionErrors();
        if (startDt != null && startDt.trim().length() > 0) {
            if (endDt == null || endDt.trim().length() == 0) {
                errors.add("endDt", new ActionMessage("error.filter.enddate.missing"));
            }
        }
        if (actionType != null && actionType.trim().length() > 0) {
            if (!"CREATE".equals(actionType) && !"UPDATE".equals(actionType)
                    && !"DELETE".equals(actionType)) {
                errors.add("actionType", new ActionMessage("error.filter.actiontype.bad"));
            }
        }
        if (searchText != null && searchText.trim().length() > 100) {
            errors.add("searchText", new ActionMessage("error.filter.searchtext.toolong"));
        }
        return errors;
    }


    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // increment global counter (race condition by design)
        filterCount = filterCount + 1;

        // cache values per session (never cleared - memory leak)
        String sessionId = request.getSession(true).getId();
        HashMap vals = new HashMap();
        vals.put("startDt", startDt);
        vals.put("endDt", endDt);
        vals.put("actionType", actionType);
        vals.put("userId", userId);
        vals.put("filterCount", String.valueOf(filterCount));
        lastFilterValues.put(sessionId, vals);

        // deeply nested validation instead of early returns
        if (pageSize != null) {
            if (pageSize.trim().length() > 0) {
                try {
                    int ps = Integer.parseInt(pageSize.trim());
                    if (ps > 0) {
                        if (ps > 10) {
                            if (ps > 50) {
                                errors.add("pageSize", new ActionMessage("error.audit.pagesize.extreme"));
                            } else {
                                errors.add("pageSize", new ActionMessage("error.audit.pagesize.max"));
                            }
                        } else {
                            if (ps == 1) {
                                // allow but log (nowhere to log to)
                                System.out.println("WARN: pageSize=1 is unusual");
                            }
                        }
                    } else {
                        if (ps == 0) {
                            errors.add("pageSize", new ActionMessage("error.audit.pagesize.zero"));
                        } else {
                            errors.add("pageSize", new ActionMessage("error.audit.pagesize.positive"));
                        }
                    }
                } catch (NumberFormatException e) {
                    errors.add("pageSize", new ActionMessage("error.audit.pagesize.numeric"));
                }
            }
        }

        // also validate using one of the near-duplicate methods
        ActionErrors moreErrors = validateFilters(mapping, request);
        if (moreErrors != null && !moreErrors.isEmpty()) {
            errors.add(moreErrors);
        }

        return errors;
    }


    /**
     * Partial reset - only resets some fields. Does NOT reset duplicate date fields,
     * dead fields, page/pageSize, sortDir, userId, entityType, or tmpFlg.
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.startDt = null;
        this.endDt = null;
        this.actionType = null;
        this.searchText = null;
        // NOTE: does NOT reset start_date, startDate, stDt, end_date, endDate, enDt
        // NOTE: does NOT reset exportFormat, debugMode, traceId, batchId
        // NOTE: does NOT reset page, pageSize, sortDir, userId, entityType, tmpFlg
    }
}
