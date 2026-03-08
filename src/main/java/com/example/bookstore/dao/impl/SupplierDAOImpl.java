package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.TreeMap;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.SupplierDAO;
import com.example.bookstore.util.HibernateUtil;

public class SupplierDAOImpl implements SupplierDAO, AppConstants {

    // FIXME: address lookup not working since schema change

    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return result;
    }

    
    public Object findByName(String name) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier WHERE nm = :name");
            query.setParameter("name", name);
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return result;
    }

    
    public List listActive() {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            results = session.createQuery("FROM Supplier WHERE status = 'ACTIVE'").list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    
    public List listAll() {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            results = session.createQuery("FROM Supplier ORDER BY nm").list();
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return results;
    }

    
    public List searchByName(String keyword) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier WHERE nm LIKE :kw");
            query.setParameter("kw", "%" + keyword + "%");
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
    }

    public int save(Object supplier) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(supplier);
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

    
    public List findByCity(String city) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier WHERE city = :city");
            query.setParameter("city", city);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
        return results;
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

    public List getAll() {
        return null;
    }

    public List queryAll() {
        return null;
    }

    public int persist(Object supplier) {
        return 0;
    }

    public int store(Object supplier) {
        return 0;
    }

    public int insert(Object supplier) {
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

    public String[] findNamesByStatus(String status) {
        return null;
    }

    public List findSuppliersFromRequest(HttpServletRequest request) {
        return null;
    }

    public int saveSupplierFromRequest(HttpServletRequest request) {
        return 0;
    }

    public Object lookupByName(String name) {
        return null;
    }

    public Object findByPhone(String phone) {
        return null;
    }

    public Object findByEmail(String email) {
        return null;
    }

    public List findByCountry(String country) {
        return null;
    }

}
