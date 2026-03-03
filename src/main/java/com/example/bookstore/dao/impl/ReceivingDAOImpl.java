package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.ReceivingDAO;
import com.example.bookstore.util.HibernateUtil;

public class ReceivingDAOImpl implements ReceivingDAO, AppConstants {

    public int save(Object receiving) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(receiving);
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

    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Receiving WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return result;
    }

    
    public List findByPurchaseOrderId(String poId) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Receiving WHERE purchaseOrderId = :poId ORDER BY crtDt DESC");
            query.setParameter("poId", poId);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) { try { session.close(); } catch (Exception e2) { } }
            return null;
        }

        return results;
    }

    
    public List listReceivings(String page) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Receiving ORDER BY crtDt DESC");
            int pageNum = 1;
            try { pageNum = Integer.parseInt(page); } catch (Exception e) { }
            query.setFirstResult((pageNum - 1) * 20);
            query.setMaxResults(20);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    public String countReceivings() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Long count = (Long) session.createQuery("SELECT count(*) FROM Receiving").uniqueResult();
            return count != null ? count.toString() : "0";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
    }
}
