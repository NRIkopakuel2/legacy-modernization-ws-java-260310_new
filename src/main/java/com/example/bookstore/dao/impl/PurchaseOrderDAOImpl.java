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
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.PurchaseOrderDAO;
import com.example.bookstore.util.HibernateUtil;

public class PurchaseOrderDAOImpl implements PurchaseOrderDAO, AppConstants {

    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM PurchaseOrder WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return result;
    }

    
    public Object findByPoNumber(String poNumber) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM PurchaseOrder WHERE poNumber = :poNum");
            query.setParameter("poNum", poNumber);
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return result;
    }

    public List listAll() {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            results = session.createQuery("FROM PurchaseOrder ORDER BY crtDt DESC").list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    
    public List listByStatus(String status) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM PurchaseOrder WHERE status = :sts ORDER BY crtDt DESC");
            query.setParameter("sts", status);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    public int save(Object po) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(po);
            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) { try { tx.rollback(); } catch (Exception e2) { } }
            e.printStackTrace();
            return 9;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
    }

    
    public String generatePoNumber() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT MAX(po_number) AS max_po FROM purchase_order");
            if (rs.next()) {
                String maxPo = rs.getString("max_po");
                if (maxPo != null && maxPo.startsWith("PO-")) {
                    int num = Integer.parseInt(maxPo.substring(3)) + 1;
                    return "PO-" + String.valueOf(10000 + num).substring(1);
                }
            }
            return "PO-0001";
        } catch (Exception e) {
            e.printStackTrace();
            return "PO-" + System.currentTimeMillis();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }
}
