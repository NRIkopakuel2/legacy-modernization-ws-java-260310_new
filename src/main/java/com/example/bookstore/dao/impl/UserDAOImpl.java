package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

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

    public Object findByUsernameAndPassword(String username, String password) {
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        Object result = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM users WHERE usr_nm = '" + username + "' AND pwd_hash = '" + password + "'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                User usr = new User();
                usr.setId(new Long(rs.getLong("id")));
                usr.setUsrNm(rs.getString("usr_nm"));
                usr.setPwdHash(rs.getString("pwd_hash"));
                usr.setRole(rs.getString("role"));
                result = usr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return result;
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

    public int count() {
        return 0;
    }

    public String countAsString() {
        return null;
    }

    public int getCount() {
        return 0;
    }

    public int persist(Object user) {
        return 0;
    }

    public int store(Object user) {
        return 0;
    }

    public int insert(Object user) {
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

    public Object findByEmail(String email) {
        return null;
    }

    public String[] findUsernamesByRole(String role) {
        return null;
    }

    public List searchByName(String keyword) {
        return null;
    }

    public Object findUserFromRequest(HttpServletRequest request) {
        return null;
    }

    public int updateUserFromRequest(HttpServletRequest request) {
        return 0;
    }

    public Object lookupByUsername(String username) {
        return null;
    }

    public int isActive(String userId) {
        return 0;
    }

    public boolean usernameExists(String username) {
        return false;
    }

    public List findByStatus(String status) {
        return null;
    }

}
