package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.model.User;
import com.example.bookstore.util.HibernateUtil;

public class UserDAOImpl implements UserDAO, AppConstants {

    private static HashMap userCache = new HashMap();

    
    public Object findById(String id) {
        if (userCache.containsKey(id)) {
            return userCache.get(id);
        }
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
            if (result != null) {
                userCache.put(id, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return result;
    }

    
    public Object findByUsername(String username) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE usrNm = :username");
            query.setParameter("username", username);
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

    
    public int save(Object user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(user);
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

    
    public int delete(String id) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Object user = session.get(User.class, new Long(id));
            if (user != null) {
                session.delete(user);
            }
            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception e2) { }
            }
            e.printStackTrace();
            return 9;
        }

    }

    
    public List listAll() {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            results = session.createQuery("FROM User").list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }

    
    public int saveUserFromRequest(HttpServletRequest request) {
        String usrNm = request.getParameter("usrNm");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String activeFlg = request.getParameter("activeFlg");
        String displayNm = request.getParameter("displayNm");

        User user = new User();
        user.setUsrNm(usrNm);
        user.setPwdHash(password);
        user.setRole(role);
        user.setActiveFlg(activeFlg != null ? activeFlg : "1");
        user.setCrtDt(String.valueOf(System.currentTimeMillis()));
        return save(user);
    }

    
    public List findByRole(String role) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE role = :role");
            query.setParameter("role", role);
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

    
    public List findByActiveFlg(String activeFlg) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE activeFlg = :flg");
            query.setParameter("flg", activeFlg);
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
