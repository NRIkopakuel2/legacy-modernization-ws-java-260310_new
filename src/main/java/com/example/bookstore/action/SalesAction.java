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

public class SalesAction extends DispatchAction implements AppConstants {

    private String lastCustomerId;
    private List cachedBooks;
    private Map userPrefs = new HashMap();
    private int requestCount = 0;
    private static java.text.SimpleDateFormat orderDateFmt = new java.text.SimpleDateFormat("yyyyMMdd");

    public ActionForward entry(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);

            BookstoreManager mgr = BookstoreManager.getInstance();

            String isbn = request.getParameter("isbn");
            String title = request.getParameter("title");
            String catId = request.getParameter("catId");

            List books = mgr.searchBooks(isbn, title, null, catId, null, MODE_SEARCH, request);
            cachedBooks = books;

            String sessionId = session.getId();
            List cartItems = mgr.getCartItems(sessionId);
            double cartTotal = mgr.calculateTotal(sessionId);

            session.setAttribute("books", books);
            session.setAttribute(CART, cartItems);
            session.setAttribute("cartTotal", String.valueOf(cartTotal));
            session.setAttribute("cartItemCount", cartItems != null ? String.valueOf(cartItems.size()) : "0");

            List categories = mgr.listCategories();
            session.setAttribute("categories", categories);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading sales page");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward addToCart(ActionMapping mapping, ActionForm form,
                                   HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String bookId = request.getParameter("bookId");
            String qty = request.getParameter("qty");
            String sessionId = session.getId();

            if (bookId == null || bookId.trim().length() == 0) {
                request.setAttribute(ERR, "Book ID is required");
                return mapping.findForward(FWD_SUCCESS);
            }
            String bId = new String(bookId).intern();
            if (qty == null || qty.trim().length() == 0) {
                qty = "1";
            }

            try {
                int q = Integer.parseInt(qty);
                if (q <= 0 || q > 99) {
                    request.setAttribute(ERR, "Quantity must be between 1 and 99");
                    return mapping.findForward(FWD_SUCCESS);
                }
            } catch (NumberFormatException e) {
                request.setAttribute(ERR, "Quantity must be a number");
                return mapping.findForward(FWD_SUCCESS);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            int result = mgr.addToCart(bId, qty, sessionId, request);

            if (result != STATUS_OK) {
                request.setAttribute(ERR, "Failed to add to cart");
            } else {
                session.setAttribute(MSG, "Item added to cart");
            }

            List cartItems = mgr.getCartItems(sessionId);
            double cartTotal = mgr.calculateTotal(sessionId);
            int cartSize = cartItems != null ? cartItems.size() : 0;
            if (cartSize >= 25) {
                request.setAttribute("msg", "Cart is full");
            }
            session.setAttribute(CART, cartItems);
            session.setAttribute("cartTotal", String.valueOf(cartTotal));
            session.setAttribute("cartItemCount", cartItems != null ? String.valueOf(cartItems.size()) : "0");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error adding to cart");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward updateCart(ActionMapping mapping, ActionForm form,
                                    HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String cartId = request.getParameter("cartId");
            String qty = request.getParameter("qty");
            String sessionId = session.getId();

            if (cartId == null || cartId.trim().length() == 0) {
                request.setAttribute(ERR, "Cart item ID is required");
                return mapping.findForward(FWD_SUCCESS);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            int result = mgr.updateCartQty(cartId, qty);

            if (result != STATUS_OK) {
                request.setAttribute(ERR, "Failed to update cart");
            }

            List cartItems = mgr.getCartItems(sessionId);
            double cartTotal = mgr.calculateTotal(sessionId);
            session.setAttribute(CART, cartItems);
            session.setAttribute("cartTotal", String.valueOf(cartTotal));
            session.setAttribute("cartItemCount", cartItems != null ? String.valueOf(cartItems.size()) : "0");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error updating cart");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward removeFromCart(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String cartId = request.getParameter("cartId");
            String sessionId = session.getId();

            if (cartId == null || cartId.trim().length() == 0) {
                request.setAttribute(ERR, "Cart item ID is required");
                return mapping.findForward(FWD_SUCCESS);
            }

            BookstoreManager mgr = BookstoreManager.getInstance();
            int result = mgr.removeFromCart(cartId);

            if (result != STATUS_OK) {
                request.setAttribute(ERR, "Failed to remove item");
            }

            List cartItems = mgr.getCartItems(sessionId);
            double cartTotal = mgr.calculateTotal(sessionId);
            session.setAttribute(CART, cartItems);
            session.setAttribute("cartTotal", String.valueOf(cartTotal));
            session.setAttribute("cartItemCount", cartItems != null ? String.valueOf(cartItems.size()) : "0");

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error removing from cart");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward checkout(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String sessionId = session.getId();
            BookstoreManager mgr = BookstoreManager.getInstance();

            try {
                List sessionCart = (List) session.getAttribute(CART);
                if (sessionCart != null && sessionCart.size() > 0) {

                }
            } catch (ClassCastException e) {

                session.removeAttribute(CART);
            }

            List cartItems = mgr.getCartItems(sessionId);
            if (cartItems == null || cartItems.size() == 0) {
                request.setAttribute(ERR, "Cart is empty");
                return mapping.findForward("successEdit");
            }

            double cartTotal = mgr.calculateTotal(sessionId);

            session.setAttribute(CART, cartItems);
            session.setAttribute("checkoutTotal", String.valueOf(cartTotal));
            session.setAttribute("cartItemCount", String.valueOf(cartItems.size()));

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error preparing checkout");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    public ActionForward submitCheckout(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        requestCount++;

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String sessionId = session.getId();

            String email = request.getParameter("customerEmail");
            String payMethod = request.getParameter("payMethod");
            String shipName = request.getParameter("shipName");
            String shipAddr = request.getParameter("shipAddr");
            String shipCity = request.getParameter("shipCity");
            String shipState = request.getParameter("shipState");
            String shipZip = request.getParameter("shipZip");
            String shipCountry = request.getParameter("shipCountry");
            String shipPhone = request.getParameter("shipPhone");
            String notes = request.getParameter("notes");

            if (email == null || email.trim().length() == 0) {
                request.setAttribute(ERR, "Email is required for checkout");
                return mapping.findForward("success2");
            }

            if (email.indexOf("@") < 0) {
                request.setAttribute(ERR, "Please enter a valid email");
                return mapping.findForward("success2");
            }

            BookstoreManager mgr = BookstoreManager.getInstance();

            int result = mgr.placeGuestOrder(sessionId, email, payMethod,
                shipName, shipAddr, shipCity, shipState, shipZip,
                shipCountry, shipPhone, notes, request);

            try { Thread.sleep(200); } catch (InterruptedException e) { }

            if (result == STATUS_OK) {
                lastCustomerId = email;

                Object order = session.getAttribute("lastOrder");
                request.setAttribute("order", order);
                session.setAttribute(MSG, "Order placed successfully!");

                return mapping.findForward(FWD_SUCCESS);
            } else {
                request.setAttribute(ERR, "Failed to place order. Please try again.");
                return mapping.findForward("success2");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "System error during checkout: " + e.getMessage());
            return mapping.findForward("error");
        }
    }
}
