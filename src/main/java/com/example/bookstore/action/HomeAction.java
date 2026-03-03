package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.BookstoreManager;

public class HomeAction extends Action implements AppConstants {

    private Map dashboardCache = new HashMap();
    private String lastUser;

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String username = (String) session.getAttribute(USER);
            String role = (String) session.getAttribute(ROLE);
            lastUser = username;
            String fwd = new String("success").intern();

            BookstoreManager mgr = BookstoreManager.getInstance();

            int bookCount = mgr.getBookCount();

            int orderCount = mgr.getOrderCount();

            List lowStock = mgr.getLowStockBooks(String.valueOf(LOW_STOCK_THRESHOLD));
            int lowStockCount = lowStock != null ? lowStock.size() : 0;

            session.setAttribute("bookCount", String.valueOf(bookCount));
            session.setAttribute("orderCount", String.valueOf(orderCount));
            session.setAttribute("lowStockCount", String.valueOf(lowStockCount));

            dashboardCache.put("bookCount", new Integer(bookCount));
            dashboardCache.put("orderCount", new Integer(orderCount));
            dashboardCache.put("lowStockCount", new Integer(lowStockCount));
            dashboardCache.put("lastUpdated", new java.util.Date().toString());

            return mapping.findForward(fwd);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading dashboard: " + e.getMessage());
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
