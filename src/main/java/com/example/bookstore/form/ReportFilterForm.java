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

public class ReportFilterForm extends ActionForm implements AppConstants {

    private String startDt;
    private String endDt;
    private String catId;
    private String sortBy;
    private String rankBy;
    private String topN;
    private String bkupDt;
    private String procSts;
    private String reportType;
    private String exportFormat;
    private String includeDeleted;
    private String groupBy;

    public String getStartDt() { return startDt; }
    public void setStartDt(String startDt) { this.startDt = startDt; }

    public String getEndDt() { return endDt; }
    public void setEndDt(String endDt) { this.endDt = endDt; }

    public String getCatId() { return catId; }
    public void setCatId(String catId) { this.catId = catId; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getRankBy() { return rankBy; }
    public void setRankBy(String rankBy) { this.rankBy = rankBy; }

    public String getTopN() { return topN; }
    public void setTopN(String topN) { this.topN = topN; }

    public String getBkupDt() { return bkupDt; }
    public void setBkupDt(String bkupDt) { this.bkupDt = bkupDt; }

    public String getProcSts() { return procSts; }
    public void setProcSts(String procSts) { this.procSts = procSts; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public String getIncludeDeleted() { return includeDeleted; }
    public void setIncludeDeleted(String includeDeleted) { this.includeDeleted = includeDeleted; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (startDt == null || startDt.trim().length() == 0) {
            errors.add("startDt", new ActionMessage("error.report.startdate.required"));
        }

        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.startDt = null;
        this.endDt = null;
        this.reportType = null;

    }
}
