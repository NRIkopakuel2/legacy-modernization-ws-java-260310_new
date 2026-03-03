package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.CustomerDAO;
import com.example.bookstore.model.Customer;
import com.example.bookstore.util.HibernateUtil;
import com.example.bookstore.manager.UserManager;

public class CustomerDAOImpl implements CustomerDAO, AppConstants {

    private static java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("yyyy/MM/dd");

    
    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM Customer WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    
    public Object findByEmail(String email) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Customer WHERE email = :email");
            query.setParameter("email", email);
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

    
    public int save(Object customer) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(customer);
            tx.commit();
            try { UserManager.getInstance().logAction("CUSTOMER_SAVE", "system", "Customer saved: " + customer); } catch (Exception e) { }
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

    
    public List findByStatus(String status) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Customer WHERE status = :status");
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

    
    public List searchByName(String keyword) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer WHERE firstName LIKE :kw OR lastName LIKE :kw");
            query.setParameter("kw", "%" + keyword + "%");
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }

    
    public boolean emailExists(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Customer WHERE email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count != null && count.longValue() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    
    public List findByPhone(String phone) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Customer WHERE phone = :phone");
            query.setParameter("phone", phone);
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
