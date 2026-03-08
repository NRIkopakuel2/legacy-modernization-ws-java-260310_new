package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.servlet.http.HttpServletRequest;
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

    public Object getById(String id) {
        return null;
    }

    public Object queryById(String id) {
        return null;
    }

    public Object loadById(String id) {
        return null;
    }

    public Object fetchById(String id) {
        return null;
    }

    public List findAll() {
        return null;
    }

    public List listAll() {
        return null;
    }

    public List getAll() {
        return null;
    }

    public List queryAll() {
        return null;
    }

    public int persist(Object receiving) {
        return 0;
    }

    public int store(Object receiving) {
        return 0;
    }

    public int insert(Object receiving) {
        return 0;
    }

    public int delete(String id) {
        return 0;
    }

    public int remove(String id) {
        return 0;
    }

    public int destroy(String id) {
        return 0;
    }

    public int purge(String id) {
        return 0;
    }

    public int count() {
        return 0;
    }

    public int getCount() {
        return 0;
    }

    public void updateCache() {
        // TODO: not implemented
    }

    public void refreshAll() {
        // TODO: not implemented
    }

    public void clearCache() {
        // TODO: not implemented
    }

    public Object doOperation(String operation, Object[] params) {
        return null;
    }

    public Map findByIdAsMap(String id) {
        return null;
    }

    public Object[] findByPurchaseOrderIdAsArray(String poId) {
        return null;
    }

    public String[] findReceivingNumbersByStatus(String status) {
        return null;
    }

    public List findReceivingsFromRequest(HttpServletRequest request) {
        return null;
    }

    public int saveReceivingFromRequest(HttpServletRequest request) {
        return 0;
    }

    public List lookupByPoId(String poId) {
        return null;
    }

    public List findByDateRange(String fromDate, String toDate) {
        return null;
    }

    public List findByStatus(String status) {
        return null;
    }

    public List findBySupplierId(String supplierId) {
        return null;
    }

    public String countByStatus(String status) {
        return null;
    }

}
