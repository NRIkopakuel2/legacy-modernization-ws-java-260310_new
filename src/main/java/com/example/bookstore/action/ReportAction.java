package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
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

public class ReportAction extends DispatchAction implements AppConstants {

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
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward dailySales(ActionMapping mapping, ActionForm form,
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
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List data = mgr.getDailySalesReport(startDate, endDate);

            lastReportType = "daily";
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
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
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

            return mapping.findForward(FWD_SUCCESS);
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
                topN = String.valueOf(DEFAULT_TOP_N);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            List data = mgr.getTopBooksReport(startDate, endDate, rankBy, topN);

            lastReportType = "topbooks";
            request.setAttribute("reportData", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error generating top books report");
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

            java.sql.Connection conn = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            String orderTotal = "0";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) as cnt, SUM(total_amount) as total FROM orders WHERE order_date BETWEEN '" + startDate + "' AND '" + endDate + "'");
                if (rs.next()) {
                    orderTotal = String.valueOf(rs.getDouble("total"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ex) { }
                try { if (stmt != null) stmt.close(); } catch (Exception ex) { }

            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            String csvContent = "";

            if ("daily".equals(reportType)) {
                csvContent = mgr.exportDailySalesCsv(startDate, endDate);
            } else if ("bybook".equals(reportType)) {
                csvContent = mgr.exportSalesByBookCsv(startDate, endDate, catId, sortBy);
            } else if ("topbooks".equals(reportType)) {
                csvContent = mgr.exportTopBooksCsv(startDate, endDate, rankBy, topN);
            } else {
                request.setAttribute(ERR, "Unknown report type");
                return mapping.findForward(FWD_ERROR);
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
