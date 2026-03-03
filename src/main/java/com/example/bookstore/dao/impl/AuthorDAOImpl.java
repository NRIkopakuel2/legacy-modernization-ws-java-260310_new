package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.AuthorDAO;
import com.example.bookstore.util.HibernateUtil;

public class AuthorDAOImpl implements AuthorDAO, AppConstants {

    
    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Author WHERE id = :id");
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

    
    public Object findByName(String name) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Author WHERE nm = :name");
            query.setParameter("name", name);
            result = query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
