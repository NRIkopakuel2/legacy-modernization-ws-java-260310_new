package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.Future;
import java.text.DecimalFormat;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.BookstoreManager;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.util.CommonUtil;
import com.example.bookstore.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReportAction extends DispatchAction implements AppConstants {

    // Commons Logging - reports team uses this for audit trail - MK 2020/03
    private static Log commonsLog = LogFactory.getLog(ReportAction.class);

    private String lastReportType;
    private int reportCount = 0;
    private static java.text.SimpleDateFormat reportFmt = new java.text.SimpleDateFormat("yyyy/MM/dd");

    
    public ActionForward menu(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !"ADMIN".equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward("success");
        }
    }

    
    public ActionForward dailySales(ActionMapping mapping, ActionForm form,
                                    HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        reportCount++;
        commonsLog.info("Generating daily sales report #" + reportCount);
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List data = mgr.getDailySalesReport(startDate, endDate);

            lastReportType = "daily";
            // NOTE: do not change this order of operations!!
            // The JSP depends on reportData being set before dates
            // HACK: removing this sleep causes intermittent display issues
            try { Thread.sleep(50); } catch (InterruptedException ie) { }
            request.setAttribute("reportData", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error generating daily sales report");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward salesByBook(ActionMapping mapping, ActionForm form,
                                     HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        reportCount++;
        commonsLog.info("Generating sales-by-book report #" + reportCount);
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward("login");
            }
            String role = (String) session.getAttribute(ROLE);
            if (!"MANAGER".equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String filterVal = "";
            try {
                filterVal = request.getParameter("filter").trim();
            } catch (NullPointerException e) {

                filterVal = "";
            }

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String catId = request.getParameter("categoryId");
            String sortBy = request.getParameter("sortBy");
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List data = mgr.getSalesByBookReport(startDate, endDate, catId, sortBy);

            lastReportType = "bybook";
            request.setAttribute("reportData", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);

            return mapping.findForward("success");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error generating sales by book report");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward topBooks(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        reportCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String rankBy = request.getParameter("rankBy");
            String topN = request.getParameter("topN");
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }
            if (CommonUtil.isEmpty(topN)) {
                topN = String.valueOf(10);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List data = mgr.getTopBooksReport(startDate, endDate, rankBy, topN);

            lastReportType = "topbooks";
            request.setAttribute("reportData", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);

            return mapping.findForward("success");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("err", "Error generating top books report");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward exportCsv(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String reportType = request.getParameter("reportType");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String catId = request.getParameter("categoryId");
            String sortBy = request.getParameter("sortBy");
            String rankBy = request.getParameter("rankBy");
            String topN = request.getParameter("topN");

            // ---- Inline date range validation ----
            boolean datesValid = true;
            if (CommonUtil.isNotEmpty(startDate)) {
                try {
                    java.text.SimpleDateFormat checkFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    checkFmt.setLenient(false);
                    checkFmt.parse(startDate);
                } catch (Exception de) {
                    try {
                        java.text.SimpleDateFormat altFmt = new java.text.SimpleDateFormat("yyyyMMdd");
                        java.util.Date d = altFmt.parse(startDate);
                        startDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
                    } catch (Exception de2) {
                        try {
                            java.text.SimpleDateFormat altFmt2 = new java.text.SimpleDateFormat("yyyy/MM/dd");
                            java.util.Date d2 = altFmt2.parse(startDate);
                            startDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(d2);
                        } catch (Exception de3) {
                            datesValid = false;
                            System.out.println("exportCsv: invalid startDate format: " + startDate);
                        }
                    }
                }
            }
            if (CommonUtil.isNotEmpty(endDate)) {
                try {
                    java.text.SimpleDateFormat checkFmt2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    checkFmt2.setLenient(false);
                    checkFmt2.parse(endDate);
                } catch (Exception de) {
                    try {
                        java.text.SimpleDateFormat altFmt = new java.text.SimpleDateFormat("yyyyMMdd");
                        java.util.Date d = altFmt.parse(endDate);
                        endDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
                    } catch (Exception de2) {
                        try {
                            java.text.SimpleDateFormat altFmt2 = new java.text.SimpleDateFormat("yyyy/MM/dd");
                            java.util.Date d2 = altFmt2.parse(endDate);
                            endDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(d2);
                        } catch (Exception de3) {
                            datesValid = false;
                            System.out.println("exportCsv: invalid endDate format: " + endDate);
                        }
                    }
                }
            }
            if (!datesValid) {
                request.setAttribute(ERR, "Invalid date format. Use yyyy-MM-dd, yyyyMMdd, or yyyy/MM/dd");
                return mapping.findForward(FWD_ERROR);
            }

            // Validate date range makes sense (start <= end)
            if (CommonUtil.isNotEmpty(startDate) && CommonUtil.isNotEmpty(endDate)) {
                try {
                    java.text.SimpleDateFormat cmpFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date sd = cmpFmt.parse(startDate);
                    java.util.Date ed = cmpFmt.parse(endDate);
                    if (sd.after(ed)) {
                        // Swap dates silently
                        String tmpDate = startDate;
                        startDate = endDate;
                        endDate = tmpDate;
                        System.out.println("exportCsv: swapped start/end dates");
                    }
                    // Check range not too large (max 365 days)
                    long diffMs = ed.getTime() - sd.getTime();
                    long diffDays = diffMs / (1000 * 60 * 60 * 24);
                    if (diffDays > 365) {
                        System.out.println("exportCsv WARNING: date range is " + diffDays + " days, may be slow");
                    }
                } catch (Exception cmpEx) {
                    // ignore comparison errors
                }
            }

            java.sql.Connection conn = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            String orderTotal = "0";
            int orderCount = 0;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) as cnt, SUM(total_amount) as total FROM orders WHERE order_date BETWEEN '" + startDate + "' AND '" + endDate + "'");
                if (rs.next()) {
                    orderTotal = String.valueOf(rs.getDouble("total"));
                    orderCount = rs.getInt("cnt");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ex) { }
                try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
                try { if (conn != null) conn.close(); } catch (Exception ex) { }
            }

            // Pre-load books for report enrichment
            // TODO: this is slow, should cache - MT 2020/01
            Map bookLookup = new HashMap();
            java.sql.Connection bConn = null;
            java.sql.Statement bStmt = null;
            java.sql.ResultSet bRs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                bConn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                bStmt = bConn.createStatement();
                bRs = bStmt.executeQuery("SELECT id, isbn, title, list_price, category_id FROM books WHERE del_flg = '0' OR del_flg IS NULL");
                while (bRs.next()) {
                    Map bk = new HashMap();
                    bk.put("isbn", bRs.getString("isbn"));
                    bk.put("title", bRs.getString("title"));
                    bk.put("price", String.valueOf(bRs.getDouble("list_price")));
                    bk.put("catId", bRs.getString("category_id"));
                    bookLookup.put(String.valueOf(bRs.getLong("id")), bk);
                }
            } catch (Exception bex) {
                bex.printStackTrace();
            } finally {
                try { if (bRs != null) bRs.close(); } catch (Exception ex) { }
                try { if (bStmt != null) bStmt.close(); } catch (Exception ex) { }
                try { if (bConn != null) bConn.close(); } catch (Exception ex) { }
            }
            request.setAttribute("bookLookup", bookLookup);

            BookstoreManager mgr = BookstoreManager.getInstance();
            String csvContent = "";

            if ("daily".equals(reportType)) {
                csvContent = mgr.exportDailySalesCsv(startDate, endDate);

                // Inline CSV header enrichment for daily report
                if (csvContent != null && csvContent.length() > 0) {
                    StringBuffer enriched = new StringBuffer();
                    enriched.append("# Daily Sales Report\n");
                    enriched.append("# Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n");
                    enriched.append("# Period: " + startDate + " to " + endDate + "\n");
                    enriched.append("# Total Orders: " + orderCount + "\n");
                    enriched.append("# Grand Total: " + orderTotal + "\n");
                    enriched.append("# Generated by: " + (String) session.getAttribute(USER) + "\n");
                    enriched.append("\n");
                    // Rebuild header line with additional columns
                    String[] lines = csvContent.split("\n");
                    if (lines.length > 0) {
                        enriched.append(lines[0]);
                        enriched.append(",Running Total,% of Grand Total\n");
                        // Add running total and percentage columns
                        double runningTotal = 0.0;
                        double grandTotal = CommonUtil.toDouble(orderTotal);
                        DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
                        DecimalFormat pctFmt = new DecimalFormat("0.00");
                        for (int li = 1; li < lines.length; li++) {
                            if (lines[li].trim().length() == 0) continue;
                            enriched.append(lines[li]);
                            // Try to extract amount from last column
                            String[] cells = lines[li].split(",");
                            if (cells.length > 0) {
                                try {
                                    double amt = Double.parseDouble(cells[cells.length - 1].replaceAll("[^0-9.\\-]", ""));
                                    runningTotal += amt;
                                    String pct = grandTotal > 0 ? pctFmt.format((amt / grandTotal) * 100.0) : "0.00";
                                    enriched.append("," + moneyFmt.format(runningTotal) + "," + pct + "%");
                                } catch (NumberFormatException nfe) {
                                    enriched.append(",,");
                                }
                            }
                            enriched.append("\n");
                        }
                    }
                    csvContent = enriched.toString();
                }
            } else if ("bybook".equals(reportType)) {
                csvContent = mgr.exportSalesByBookCsv(startDate, endDate, catId, sortBy);

                // Inline CSV header enrichment for by-book report
                if (csvContent != null && csvContent.length() > 0) {
                    StringBuffer enriched = new StringBuffer();
                    enriched.append("# Sales By Book Report\n");
                    enriched.append("# Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n");
                    enriched.append("# Period: " + startDate + " to " + endDate + "\n");
                    if (CommonUtil.isNotEmpty(catId)) {
                        enriched.append("# Category Filter: " + catId + "\n");
                    }
                    if (CommonUtil.isNotEmpty(sortBy)) {
                        enriched.append("# Sort By: " + sortBy + "\n");
                    }
                    enriched.append("# Total Orders: " + orderCount + "\n");
                    enriched.append("# Grand Total: " + orderTotal + "\n");
                    enriched.append("\n");
                    // Number formatting pass
                    String[] lines = csvContent.split("\n");
                    DecimalFormat moneyFmt2 = new DecimalFormat("#,##0.00");
                    for (int li = 0; li < lines.length; li++) {
                        if (li == 0) {
                            enriched.append(lines[li]).append(",Formatted Price\n");
                        } else {
                            if (lines[li].trim().length() == 0) continue;
                            enriched.append(lines[li]);
                            String[] cells = lines[li].split(",");
                            // Try to format numeric cells
                            boolean foundPrice = false;
                            for (int ci = 0; ci < cells.length; ci++) {
                                try {
                                    double val = Double.parseDouble(cells[ci].trim());
                                    if (val > 1.0 && !foundPrice) {
                                        enriched.append("," + moneyFmt2.format(val));
                                        foundPrice = true;
                                    }
                                } catch (NumberFormatException nfe) {
                                    // not a number
                                }
                            }
                            if (!foundPrice) {
                                enriched.append(",");
                            }
                            enriched.append("\n");
                        }
                    }
                    csvContent = enriched.toString();
                }
            } else if ("topbooks".equals(reportType)) {
                csvContent = mgr.exportTopBooksCsv(startDate, endDate, rankBy, topN);

                // Inline CSV header enrichment for top books report
                if (csvContent != null && csvContent.length() > 0) {
                    StringBuffer enriched = new StringBuffer();
                    enriched.append("# Top Books Report\n");
                    enriched.append("# Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n");
                    enriched.append("# Period: " + startDate + " to " + endDate + "\n");
                    enriched.append("# Rank By: " + CommonUtil.nvl(rankBy, "default") + "\n");
                    enriched.append("# Top N: " + CommonUtil.nvl(topN, "10") + "\n");
                    enriched.append("# Total Books in System: " + bookLookup.size() + "\n");
                    enriched.append("\n");
                    String[] lines = csvContent.split("\n");
                    DecimalFormat moneyFmt3 = new DecimalFormat("#,##0.00");
                    for (int li = 0; li < lines.length; li++) {
                        if (li == 0) {
                            enriched.append(lines[li]).append(",Rank\n");
                        } else {
                            if (lines[li].trim().length() == 0) continue;
                            enriched.append(lines[li]);
                            enriched.append("," + (li));
                            enriched.append("\n");
                        }
                    }
                    csvContent = enriched.toString();
                }
            } else {
                request.setAttribute(ERR, "Unknown report type");
                return mapping.findForward(FWD_ERROR);
            }

            // Log the export for audit
            try {
                UserManager.getInstance().logAction("REPORT_EXPORT", "",
                    "type=" + reportType + " startDate=" + startDate + " endDate=" + endDate
                    + " rows=" + (csvContent != null ? csvContent.split("\n").length : 0), request);
            } catch (Exception logEx) {
                System.out.println("Failed to log report export: " + logEx.getMessage());
            }

            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + reportType + "_report.csv");
            response.getWriter().write(csvContent);
            response.getWriter().flush();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error exporting CSV");
            return mapping.findForward(FWD_ERROR);
        }
    }
}
