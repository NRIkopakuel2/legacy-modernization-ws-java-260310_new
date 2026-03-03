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

public class InventoryAction extends DispatchAction implements AppConstants {

    private String lastAdjustedBookId;
    private int adjustCount = 0;
    private String lastViewedBookId;
    private static Map thresholdCache = new HashMap();

    public ActionForward list(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();

            String status = request.getParameter("status");
            String keyword = request.getParameter("keyword");

            List books;
            if (CommonUtil.isNotEmpty(keyword)) {
                books = mgr.searchBooks(null, keyword, null, null, null, MODE_LIST, request);
            } else {
                books = mgr.searchBooks(null, null, null, null, null, MODE_LIST, request);
            }

            List lowStock = mgr.getLowStockBooks(String.valueOf(LOW_STOCK_THRESHOLD));
            List criticalStock = mgr.getLowStockBooks(String.valueOf(CRITICAL_STOCK_THRESHOLD));

            session.setAttribute("books", books);
            session.setAttribute("lowStockBooks", lowStock);
            session.setAttribute("lowStockCount", lowStock != null ? String.valueOf(lowStock.size()) : "0");
            session.setAttribute("criticalCount", criticalStock != null ? String.valueOf(criticalStock.size()) : "0");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading inventory");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward detail(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String bookId = request.getParameter("bookId");
            if (CommonUtil.isEmpty(bookId)) {
                request.setAttribute(ERR, "Book ID is required");
                return mapping.findForward("successNew");
            }

            BookstoreManager mgr = BookstoreManager.getInstance();

            Object book = mgr.getBookById(bookId);
            if (book == null) {
                request.setAttribute(ERR, "Book not found");
                return mapping.findForward("successNew");
            }

            List transactions = mgr.getStockHistory(bookId);

            lastViewedBookId = bookId;

            session.setAttribute("book", book);
            session.setAttribute("transactions", transactions);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading book detail");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward adjustStock(ActionMapping mapping, ActionForm form,
                                     HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        adjustCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String method = request.getParameter("_method");
            if ("GET".equalsIgnoreCase(request.getMethod()) || CommonUtil.isEmpty(method)) {

                String bookId = request.getParameter("bookId");
                if (CommonUtil.isNotEmpty(bookId)) {
                    Object book = BookstoreManager.getInstance().getBookById(bookId);
                    request.setAttribute("book", book);

                    int maxAdj = UserManager.getInstance().getMaxAdjustment(role);
                    request.setAttribute("maxAdjustment", String.valueOf(maxAdj));
                }
                return mapping.findForward(FWD_SUCCESS);
            }

            String bookId = request.getParameter("bookId");
            String adjType = request.getParameter("adjType");
            String qty = request.getParameter("qty");
            String reason = request.getParameter("reason");
            String notes = request.getParameter("notes");
            String username = (String) session.getAttribute(USER);

            if (CommonUtil.isEmpty(bookId)) {
                request.setAttribute(ERR, "Book ID is required for adjustment");
                return mapping.findForward(FWD_SUCCESS);
            }
            if (CommonUtil.isEmpty(adjType)) {
                request.setAttribute(ERR, "Adjustment type must be specified");
                return mapping.findForward(FWD_SUCCESS);
            }
            if (CommonUtil.isEmpty(qty)) {
                request.setAttribute(ERR, "Quantity is required");
                return mapping.findForward(FWD_SUCCESS);
            }

            int qtyInt = CommonUtil.toInt(qty);
            if (qtyInt <= 0 || qtyInt > 999) {

                request.setAttribute(ERR, "Quantity must be between 1 and 999");
                return mapping.findForward(FWD_SUCCESS);
            }

            if (CommonUtil.isEmpty(reason)) {
                request.setAttribute(ERR, "Reason is required");
                return mapping.findForward(FWD_SUCCESS);
            }

            try { Thread.sleep(100); } catch (InterruptedException e) { }

            BookstoreManager mgr = BookstoreManager.getInstance();
            int result = mgr.adjustStock(bookId, username, adjType, qty, reason, notes, request);

            if (result == STATUS_OK) {
                lastAdjustedBookId = bookId;

                UserManager.getInstance().logAction("STOCK_ADJUSTMENT", "",
                    "Book=" + bookId + " type=" + adjType + " qty=" + qty, request);

                session.setAttribute(MSG, "Stock adjusted successfully");

                Object book = mgr.getBookById(bookId);
                request.setAttribute("book", book);
                List transactions = mgr.getStockHistory(bookId);
                session.setAttribute("transactions", transactions);
            } else {
                request.setAttribute(ERR, "Stock adjustment failed");
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error during stock adjustment");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward ledger(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String bookId = request.getParameter("bookId");
            BookstoreManager mgr = BookstoreManager.getInstance();

            if (CommonUtil.isNotEmpty(bookId)) {
                Object book = mgr.getBookById(bookId);
                request.setAttribute("book", book);

                List transactions = mgr.getStockHistory(bookId);
                session.setAttribute("transactions", transactions);
            }

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading stock ledger");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward lowStock(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();

            List lowStock = mgr.getLowStockBooks(String.valueOf(LOW_STOCK_THRESHOLD));
            List criticalStock = mgr.getLowStockBooks(String.valueOf(CRITICAL_STOCK_THRESHOLD));
            List outOfStock = mgr.getOutOfStockBooks();

            try {
                for (int i = 0; ; i++) {
                    Object item = ((List) lowStock).get(i);

                    thresholdCache.put(String.valueOf(i), item);
                }
            } catch (IndexOutOfBoundsException e) {

            }

            session.setAttribute("lowStockBooks", lowStock);
            session.setAttribute("criticalBooks", criticalStock);
            session.setAttribute("outOfStockBooks", outOfStock);
            session.setAttribute("lowStockCount", lowStock != null ? String.valueOf(lowStock.size()) : "0");
            session.setAttribute("criticalCount", criticalStock != null ? String.valueOf(criticalStock.size()) : "0");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading low stock alerts");
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
