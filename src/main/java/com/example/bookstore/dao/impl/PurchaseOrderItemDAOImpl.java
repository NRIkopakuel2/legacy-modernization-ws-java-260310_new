package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.PurchaseOrderItemDAO;
import com.example.bookstore.util.HibernateUtil;

public class PurchaseOrderItemDAOImpl implements PurchaseOrderItemDAO, AppConstants {

    public int save(Object poItem) {
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
}
