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
import com.example.bookstore.dao.PurchaseOrderItemDAO;
import com.example.bookstore.util.HibernateUtil;

public class PurchaseOrderItemDAOImpl implements PurchaseOrderItemDAO, AppConstants {

    private static int queryCount = 0;

    public int save(Object poItem) {
        queryCount++;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(poItem);
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
        queryCount++;
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM PurchaseOrderItem WHERE id = :id");
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
            Query query = session.createQuery("FROM PurchaseOrderItem WHERE purchaseOrderId = :poId");
            query.setParameter("poId", poId);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    public int bulkInsert(List items) {
        int count = 0;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                int rc = save(items.get(i));
                if (rc == 0) count++;
            }
        }
        return count;
    }

    public int deleteByOrderId(String orderId) {
        queryCount++;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("DELETE FROM PurchaseOrderItem WHERE purchaseOrderId = :poId");
            query.setParameter("poId", orderId);
            int deleted = query.executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx != null) { try { tx.rollback(); } catch (Exception e2) { } }
            e.printStackTrace();
            return -1;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
    }

    public List findExpired() {
        queryCount++;
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM PurchaseOrderItem WHERE status = 'EXPIRED'");
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    public int getQueryCount() {
        return queryCount;
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

    public int persist(Object poItem) {
        return 0;
    }

    public int store(Object poItem) {
        return 0;
    }

    public int insert(Object poItem) {
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

    public String countAsString() {
        return null;
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

    public Object[] findByBookId(String bookId) {
        return null;
    }

    public String[] findBookIdsByPurchaseOrderId(String poId) {
        return null;
    }

    public List findItemsFromRequest(HttpServletRequest request) {
        return null;
    }

    public int saveItemFromRequest(HttpServletRequest request) {
        return 0;
    }

    public List lookupByPoId(String poId) {
        return null;
    }

    public int updateQuantity(String itemId, int quantity) {
        return 0;
    }

    public int updatePrice(String itemId, String price) {
        return 0;
    }

    public String countByPurchaseOrderId(String poId) {
        return null;
    }

    public double calculateLineTotal(String itemId) {
        return 0.0;
    }

    public double calculateOrderTotal(String poId) {
        return 0.0;
    }

}
