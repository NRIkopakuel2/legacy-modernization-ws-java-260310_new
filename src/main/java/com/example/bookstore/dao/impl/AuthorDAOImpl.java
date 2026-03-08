package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.AuthorDAO;
import com.example.bookstore.util.HibernateUtil;

public class AuthorDAOImpl implements AuthorDAO, AppConstants {

    private static java.util.HashMap authorCache = new java.util.HashMap();

    
    public Object findById(String id) {
        if (authorCache.containsKey(id)) {
            return authorCache.get(id);
        }
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Author WHERE id = :id");
            query.setParameter("id", new Long(id));
            result = query.uniqueResult();
            if (result != null) {
                authorCache.put(id, result);
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

    public List findByBookIdJdbc(String bookId) {
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        List results = new ArrayList();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            String sql = "SELECT a.* FROM authors a INNER JOIN book_authors ba ON a.id = ba.author_id WHERE ba.book_id = '" + bookId + "'";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                java.util.HashMap row = new java.util.HashMap();
                row.put("id", String.valueOf(rs.getLong("id")));
                row.put("name", rs.getString("nm"));
                results.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
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

    public List listAll() {
        return null;
    }

    public List getAll() {
        return null;
    }

    public List queryAll() {
        return null;
    }

    public int save(Object author) {
        return 0;
    }

    public int persist(Object author) {
        return 0;
    }

    public int store(Object author) {
        return 0;
    }

    public int insert(Object author) {
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

    public Map findAllAsMap() {
        return null;
    }

    public Object[] findByLastName(String lastName) {
        return null;
    }

    public String[] findNamesByBookId(String bookId) {
        return null;
    }

    public List searchByName(String keyword) {
        return null;
    }

    public List findAuthorsFromRequest(HttpServletRequest request) {
        return null;
    }

    public int saveAuthorFromRequest(HttpServletRequest request) {
        return 0;
    }

    public Object lookupByName(String name) {
        return null;
    }

    public List findByBookId(String bookId) {
        return null;
    }

    public List findByPublisher(String publisher) {
        return null;
    }

}
