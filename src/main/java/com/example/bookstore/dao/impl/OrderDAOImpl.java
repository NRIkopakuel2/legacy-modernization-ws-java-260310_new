package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.OrderDAO;
import com.example.bookstore.model.Order;
import com.example.bookstore.util.HibernateUtil;
import com.example.bookstore.manager.UserManager;

public class OrderDAOImpl implements OrderDAO, AppConstants {

    private static int queryCount = 0;
    private static Map recentOrders = new HashMap();

    
    public Object findById(String id) {
        queryCount++;
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return result;
    }

    
    public Object findByOrderNumber(String orderNo) {
        queryCount++;
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE orderNo = :orderNo");
            query.setParameter("orderNo", orderNo);
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return result;
    }

    
    public List findByCustomerId(String customerId) {
        queryCount++;
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE customerId = :custId");
            query.setParameter("custId", customerId);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }

    
    public List findByStatus(String status) {
        queryCount++;
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE status = :status");
            query.setParameter("status", status);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }

    
    public List findByDateRange(String fromDate, String toDate) {
        queryCount++;
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();

            String sql = "SELECT * FROM orders WHERE order_dt >= '" + fromDate + "' AND order_dt <= '" + toDate + "' ORDER BY order_dt DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Order order = new Order();
                order.setId(new Long(rs.getLong("id")));
                order.setCustomerId(rs.getString("customer_id"));
                order.setOrderNo(rs.getString("order_no"));
                order.setOrderDt(rs.getString("order_dt"));
                order.setStatus(rs.getString("status"));
                order.setSubtotal(rs.getDouble("subtotal"));
                order.setTax(rs.getDouble("tax"));
                order.setTotal(rs.getDouble("total"));
                order.setPaymentSts(rs.getString("payment_sts"));
                results.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return results;
    }

    
    public int save(Object order) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(order);
            tx.commit();
            recentOrders.put(String.valueOf(System.currentTimeMillis()), order);
            try { UserManager.getInstance().logAction("ORDER_SAVE", "system", "Order saved: " + order); } catch (Exception e) { }
            return 0;
        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception e2) { }
            }
            e.printStackTrace();
            return 9;
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
    }

    
    public List findOrdersFromSession(HttpServletRequest request) {
        queryCount++;
        String customerId = (String) request.getSession().getAttribute("customerId");
        if (customerId == null) {
            return new ArrayList();
        }
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE customerId = :custId ORDER BY orderDt DESC");
            query.setParameter("custId", customerId);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }

    
    public List findByPaymentStatus(String payStatus) {
        queryCount++;
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order WHERE paymentSts = :paySts");
            query.setParameter("paySts", payStatus);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }
}
