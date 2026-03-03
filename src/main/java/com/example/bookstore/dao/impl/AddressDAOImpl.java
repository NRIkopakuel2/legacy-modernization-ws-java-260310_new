package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.AddressDAO;
import com.example.bookstore.model.Address;
import com.example.bookstore.util.HibernateUtil;

public class AddressDAOImpl implements AddressDAO, AppConstants {

    public int save(Object address) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(address);
            tx.commit();
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

    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            result = session.get(Address.class, new Long(id));
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
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Address WHERE customerId = :custId");
            query.setParameter("custId", customerId);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    
    public Object findDefaultByCustomerId(String customerId) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Address WHERE customerId = :custId AND isDefault = '1'");
            query.setParameter("custId", customerId);
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
}
