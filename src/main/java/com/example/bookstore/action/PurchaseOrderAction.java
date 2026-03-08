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
import com.example.bookstore.manager.CommonHelper;

public class PurchaseOrderAction extends DispatchAction implements AppConstants {

    private static java.util.Map supplierCache = new java.util.HashMap();
    private static java.util.Map sessionTracker = new java.util.HashMap();
    private String lastViewedPoId;
    private int viewCount = 0;
    private int requestCount = 0;

    
    public ActionForward supplierList(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        viewCount++;
        requestCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String currentUser = (String) session.getAttribute(USER);
            sessionTracker.put(currentUser, new java.util.Date().toString());
            System.out.println("PurchaseOrderAction.supplierList request #" + requestCount
                + " by user=" + currentUser);

            CommonHelper helper = CommonHelper.getInstance();
            List suppliers = helper.listSuppliers();

            // Direct DB access for supplier list
            // NOTE: CommonHelper.listSuppliers() returns stale data sometimes - MT 2020/02
            java.sql.Connection conn = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                    "legacy_user", "legacy_pass");
                stmt = conn.createStatement();

                rs = stmt.executeQuery(
                    "SELECT supplier_id, supplier_name, contact_name, phone, email, "
                    + "address, status FROM suppliers WHERE status = 'ACTIVE' "
                    + "ORDER BY supplier_name");
                java.util.List freshSuppliers = new java.util.ArrayList();
                while (rs.next()) {
                    java.util.Map sup = new java.util.HashMap();
                    sup.put("supplierId", rs.getString("supplier_id"));
                    sup.put("supplierName", rs.getString("supplier_name"));
                    sup.put("contactName", rs.getString("contact_name"));
                    sup.put("phone", rs.getString("phone"));
                    sup.put("email", rs.getString("email"));
                    sup.put("address", rs.getString("address"));
                    sup.put("status", rs.getString("status"));
                    freshSuppliers.add(sup);
                }
                rs.close();

                // Get PO counts per supplier for the list view
                rs = stmt.executeQuery(
                    "SELECT supplier_id, COUNT(*) as po_count FROM purchase_orders "
                    + "GROUP BY supplier_id");
                java.util.Map poCountMap = new java.util.HashMap();
                while (rs.next()) {
                    poCountMap.put(rs.getString("supplier_id"),
                        String.valueOf(rs.getInt("po_count")));
                }
                session.setAttribute("supplierPoCounts", poCountMap);

                supplierCache.put("suppliers", freshSuppliers);
                supplierCache.put("lastRefresh", new java.util.Date().toString());
                session.setAttribute("freshSuppliers", freshSuppliers);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Supplier direct DB access failed: " + ex.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ex) { }
                try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
                try { if (conn != null) conn.close(); } catch (Exception ex) { }
            }

            session.setAttribute("suppliers", suppliers);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading suppliers");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward poList(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        viewCount++;
        requestCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String currentUser = (String) session.getAttribute(USER);
            sessionTracker.put(currentUser + "_poList", new java.util.Date().toString());
            System.out.println("PurchaseOrderAction.poList request #" + requestCount
                + " by user=" + currentUser);

            CommonHelper helper = CommonHelper.getInstance();

            String statusFilter = request.getParameter("status");
            List purchaseOrders;
            if (statusFilter != null && statusFilter.trim().length() > 0) {
                purchaseOrders = helper.listPurchaseOrdersByStatus(statusFilter);
            } else {
                purchaseOrders = helper.listPurchaseOrders();
            }

            List suppliers = helper.listSuppliers();

            // Inline JDBC for PO listing with status summary
            // FIXME: should use DAO pattern but need quick fix for status dashboard - MT 2020/03
            java.sql.Connection conn = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                    "legacy_user", "legacy_pass");
                stmt = conn.createStatement();

                // PO status summary for dashboard widgets
                rs = stmt.executeQuery(
                    "SELECT status, COUNT(*) as cnt, COALESCE(SUM(total_amount), 0) as total_amt "
                    + "FROM purchase_orders GROUP BY status ORDER BY cnt DESC");
                java.util.List statusSummary = new java.util.ArrayList();
                while (rs.next()) {
                    java.util.Map row = new java.util.HashMap();
                    row.put("status", rs.getString("status"));
                    row.put("count", String.valueOf(rs.getInt("cnt")));
                    row.put("totalAmount", String.valueOf(rs.getDouble("total_amt")));
                    statusSummary.add(row);
                }
                session.setAttribute("poStatusSummary", statusSummary);
                rs.close();

                // Overdue POs
                rs = stmt.executeQuery(
                    "SELECT po_no, supplier_id, expected_dt, status FROM purchase_orders "
                    + "WHERE expected_dt < CURDATE() AND status NOT IN ('RECEIVED', 'CLOSED', 'CANCELLED') "
                    + "ORDER BY expected_dt");
                java.util.List overduePOs = new java.util.ArrayList();
                while (rs.next()) {
                    java.util.Map po = new java.util.HashMap();
                    po.put("poNo", rs.getString("po_no"));
                    po.put("supplierId", rs.getString("supplier_id"));
                    po.put("expectedDate", rs.getString("expected_dt"));
                    po.put("status", rs.getString("status"));
                    overduePOs.add(po);
                }
                session.setAttribute("overduePOs", overduePOs);
                rs.close();

                // Recent PO activity
                rs = stmt.executeQuery(
                    "SELECT po_no, status, total_amount, order_dt FROM purchase_orders "
                    + "ORDER BY order_dt DESC LIMIT 10");
                java.util.List recentPOs = new java.util.ArrayList();
                while (rs.next()) {
                    java.util.Map po = new java.util.HashMap();
                    po.put("poNo", rs.getString("po_no"));
                    po.put("status", rs.getString("status"));
                    po.put("totalAmount", String.valueOf(rs.getDouble("total_amount")));
                    po.put("orderDate", rs.getString("order_dt"));
                    recentPOs.add(po);
                }
                session.setAttribute("recentPOs", recentPOs);

                supplierCache.put("lastPoListRefresh", new java.util.Date().toString());

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("PO inline query failed: " + ex.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ex) { }
                try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
                try { if (conn != null) conn.close(); } catch (Exception ex) { }
            }

            session.setAttribute("purchaseOrders", purchaseOrders);
            session.setAttribute("suppliers", suppliers);

            System.out.println("PurchaseOrderAction.poList completed, viewCount=" + viewCount
                + " tracked sessions=" + sessionTracker.size());

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading purchase orders");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    private String formatSupplierSummary() {
        StringBuffer sb = new StringBuffer();
        sb.append("Supplier Cache Summary\n");
        java.util.Iterator it = supplierCache.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        sb.append("Sessions tracked: ");
        sb.append(sessionTracker.size());
        sb.append("\n");
        sb.append("Total requests: ");
        sb.append(requestCount);
        sb.append("\n");
        return sb.toString();
    }
}
