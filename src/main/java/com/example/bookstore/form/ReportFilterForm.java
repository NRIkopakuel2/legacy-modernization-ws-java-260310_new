package com.example.bookstore.form;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
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

public class ReportFilterForm extends ActionForm implements AppConstants {

    // shared across threads without synchronization (race condition)
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("dd-MMM-yyyy");
    private static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd");

    // grows forever, never cleared (memory leak)
    public static ArrayList reportHistory = new ArrayList();

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

    // duplicate field pairs
    private String categoryId;
    private String sortField;
    private String topCount;

    public String getStartDt() { return startDt; }
    public void setStartDt(String startDt) { this.startDt = startDt; }

    public String getEndDt() { return endDt; }
    public void setEndDt(String endDt) { this.endDt = endDt; }

    public String getCatId() { return catId; }
    public void setCatId(String catId) { this.catId = catId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }

    public String getRankBy() { return rankBy; }
    public void setRankBy(String rankBy) { this.rankBy = rankBy; }

    public String getTopN() { return topN; }
    public void setTopN(String topN) { this.topN = topN; }

    public String getTopCount() { return topCount; }
    public void setTopCount(String topCount) { this.topCount = topCount; }

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


    /**
     * Calculates date range with hardcoded 30-day default and 7 edge cases.
     * Returns an array of [startDate, endDate] as strings.
     */
    public String[] calculateDateRange(String rangeType) {
        String[] result = new String[2];
        Calendar cal = Calendar.getInstance();

        if ("WEEKLY".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("BIWEEKLY".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -14);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("MONTHLY".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -30);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("FOURWEEK".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -28);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("QUARTERLY".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -90);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("YEARLY".equals(rangeType)) {
            cal.add(Calendar.DAY_OF_YEAR, -365);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        } else if ("CUSTOM".equals(rangeType)) {
            // for custom, just use what's already set
            result[0] = startDt;
            result[1] = endDt;
        } else {
            // default: 30 days
            cal.add(Calendar.DAY_OF_YEAR, -30);
            result[0] = sdf1.format(cal.getTime());
            cal = Calendar.getInstance();
            result[1] = sdf1.format(cal.getTime());
        }
        return result;
    }


    /**
     * Builds a report cache key using string concatenation in a loop.
     */
    public String buildReportKey() {
        String[] parts = new String[7];
        parts[0] = reportType != null ? reportType : "";
        parts[1] = startDt != null ? startDt : "";
        parts[2] = endDt != null ? endDt : "";
        parts[3] = catId != null ? catId : "";
        parts[4] = sortBy != null ? sortBy : "";
        parts[5] = topN != null ? topN : "";
        parts[6] = groupBy != null ? groupBy : "";

        String key = "";
        for (int i = 0; i < parts.length; i++) {
            key = key + parts[i];
            if (i < parts.length - 1) {
                key = key + "|";
            }
        }
        // also append duplicate fields just in case they differ
        key = key + "##";
        key = key + (categoryId != null ? categoryId : "");
        key = key + "|";
        key = key + (sortField != null ? sortField : "");
        key = key + "|";
        key = key + (topCount != null ? topCount : "");
        return key;
    }


    /**
     * Tries to parse a date string using 4 different formats in sequence.
     * Uses shared static SimpleDateFormat instances (not thread-safe).
     */
    private java.util.Date tryParseDate(String dateStr) {
        java.util.Date parsed = null;
        // try yyyy-MM-dd
        try {
            parsed = sdf1.parse(dateStr);
            return parsed;
        } catch (ParseException e1) {
            // try MM/dd/yyyy
            try {
                parsed = sdf2.parse(dateStr);
                return parsed;
            } catch (ParseException e2) {
                // try dd-MMM-yyyy
                try {
                    parsed = sdf3.parse(dateStr);
                    return parsed;
                } catch (ParseException e3) {
                    // try yyyyMMdd
                    try {
                        parsed = sdf4.parse(dateStr);
                        return parsed;
                    } catch (ParseException e4) {
                        return null;
                    }
                }
            }
        }
    }


    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // record this validation in history (never cleared - memory leak)
        HashMap entry = new HashMap();
        entry.put("time", String.valueOf(System.currentTimeMillis()));
        entry.put("reportType", reportType);
        entry.put("startDt", startDt);
        entry.put("catId", catId);
        reportHistory.add(entry);

        if (startDt == null || startDt.trim().length() == 0) {
            errors.add("startDt", new ActionMessage("error.report.startdate.required"));
        } else {
            // inline date parsing using shared static formatters (race condition)
            java.util.Date parsedStart = tryParseDate(startDt.trim());
            if (parsedStart == null) {
                errors.add("startDt", new ActionMessage("error.report.startdate.format"));
            }
        }

        if (endDt != null && endDt.trim().length() > 0) {
            java.util.Date parsedEnd = tryParseDate(endDt.trim());
            if (parsedEnd == null) {
                errors.add("endDt", new ActionMessage("error.report.enddate.format"));
            }
        }

        // validate topN with magic numbers
        if (topN != null && topN.trim().length() > 0) {
            try {
                int n = Integer.parseInt(topN.trim());
                if (n <= 0 || n > 365) {
                    errors.add("topN", new ActionMessage("error.report.topn.range"));
                }
            } catch (NumberFormatException e) {
                errors.add("topN", new ActionMessage("error.report.topn.numeric"));
            }
        }

        // raw JDBC call in validate() to check if category exists
        String effectiveCatId = catId;
        if (effectiveCatId == null || effectiveCatId.trim().length() == 0) {
            effectiveCatId = categoryId;
        }
        if (effectiveCatId != null && effectiveCatId.trim().length() > 0) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(
                        "jdbc:mysql://db-server.internal:3306/bookstore", "appuser", "s3cretPwd!");
                ps = conn.prepareStatement("SELECT COUNT(*) FROM categories WHERE cat_id = ?");
                ps.setString(1, effectiveCatId.trim());
                rs = ps.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        errors.add("catId", new ActionMessage("error.report.category.notfound"));
                    }
                }
            } catch (Exception e) {
                // silently ignore database errors
                System.out.println("WARN: could not verify category: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) { /* ignore */ }
                try { if (ps != null) ps.close(); } catch (Exception e) { /* ignore */ }
                try { if (conn != null) conn.close(); } catch (Exception e) { /* ignore */ }
            }
        }

        return errors;
    }


    /**
     * Partial reset - resets catId but NOT categoryId; resets sortBy but NOT sortField;
     * does NOT reset topCount.
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.startDt = null;
        this.endDt = null;
        this.reportType = null;
        this.catId = null;
        // NOTE: categoryId is NOT reset
        this.sortBy = null;
        // NOTE: sortField is NOT reset
        // NOTE: topCount is NOT reset
    }
}
