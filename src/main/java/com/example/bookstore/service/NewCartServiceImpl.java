// BOOK-234: New cart implementation. DO NOT ENABLE until v2.0 launch
// Author: MT 2020/03
// Status: Feature-flagged behind AppConstants.USE_NEW_CART (currently false)
// NOTE: This was tested in staging but rolled back after QA found issues with
//       concurrent session handling. The Hibernate integration here is actually
//       cleaner than the live code — ironic.
package com.example.bookstore.service;

import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Query;

import com.example.bookstore.util.HibernateUtil;
import com.example.bookstore.model.ShoppingCart;
import com.example.bookstore.model.Book;

public class NewCartServiceImpl {

    private SessionFactory sessionFactory;

    public NewCartServiceImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public List getCartItems(String sessionId) {
        Session session = null;
        List items = new ArrayList();
        try {
            session = sessionFactory.openSession();
            Query query = session.createQuery("from ShoppingCart where sessionId = :sid and qty != '0'");
            query.setParameter("sid", sessionId);
            items = query.list();
        } catch (Exception e) {
            System.out.println("[NewCartService] Error getting cart items: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return items;
    }

    public int addToCart(String sessionId, String bookId, int quantity) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            // Check if item already exists in cart
            Query existsQuery = session.createQuery(
                "from ShoppingCart where sessionId = :sid and bookId = :bid and qty != '0'");
            existsQuery.setParameter("sid", sessionId);
            existsQuery.setParameter("bid", bookId);
            List existing = existsQuery.list();

            if (existing != null && existing.size() > 0) {
                // Update quantity
                ShoppingCart cart = (ShoppingCart) existing.get(0);
                int currentQty = Integer.parseInt(cart.getQty());
                cart.setQty(String.valueOf(currentQty + quantity));
                cart.setUpdDt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                session.update(cart);
            } else {
                // Create new cart entry
                ShoppingCart cart = new ShoppingCart();
                cart.setSessionId(sessionId);
                cart.setBookId(bookId);
                cart.setQty(String.valueOf(quantity));
                cart.setCrtDt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                session.save(cart);
            }

            tx.commit();
            return 0; // success
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("[NewCartService] Error adding to cart: " + e.getMessage());
            return 9; // error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public int removeFromCart(String cartId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            ShoppingCart cart = (ShoppingCart) session.get(ShoppingCart.class, Long.valueOf(cartId));
            if (cart != null) {
                // Soft delete by setting qty to 0
                cart.setQty("0");
                cart.setUpdDt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                session.update(cart);
            }

            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("[NewCartService] Error removing from cart: " + e.getMessage());
            return 9;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public double calculateTotal(String sessionId) {
        List items = getCartItems(sessionId);
        double total = 0.0;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            for (int i = 0; i < items.size(); i++) {
                ShoppingCart cartItem = (ShoppingCart) items.get(i);
                Book book = (Book) session.get(Book.class, Long.valueOf(cartItem.getBookId()));
                if (book != null) {
                    int qty = Integer.parseInt(cartItem.getQty());
                    total += book.getListPrice() * qty;
                }
            }
        } catch (Exception e) {
            System.out.println("[NewCartService] Error calculating total: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public int checkout(String sessionId, String customerId, String paymentMethod) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            List items = getCartItems(sessionId);
            if (items == null || items.size() == 0) {
                return 2; // not found
            }

            // Mark all cart items as checked out by zeroing qty
            for (int i = 0; i < items.size(); i++) {
                ShoppingCart cartItem = (ShoppingCart) items.get(i);
                cartItem.setQty("0"); // Mark as processed
                cartItem.setUpdDt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                session.update(cartItem);
            }

            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("[NewCartService] Checkout error: " + e.getMessage());
            return 9;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public int clearCart(String sessionId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            Query query = session.createQuery(
                "update ShoppingCart set qty = '0' where sessionId = :sid and qty != '0'");
            query.setParameter("sid", sessionId);
            query.executeUpdate();

            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return 9;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
