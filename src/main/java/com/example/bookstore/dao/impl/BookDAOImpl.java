package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.HibernateUtil;
import com.example.bookstore.manager.BookstoreManager;

public class BookDAOImpl implements BookDAO, AppConstants {

    private static java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
    private static HashMap bookCache = new HashMap();

    
    public Object findById(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Book WHERE id = :id");
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

    
    public Object findByIsbn(String isbn) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Book WHERE isbn = :isbn");
            query.setParameter("isbn", isbn);
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

    
    public List findByTitle(String title) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM books WHERE title LIKE '%" + title + "%'" + " LIMIT 200");
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                book.setPubDt(rs.getString("pub_dt"));
                book.setListPrice(rs.getDouble("list_price"));
                book.setTaxRate(rs.getString("tax_rate"));
                book.setStatus(rs.getString("status"));
                book.setDescr(rs.getString("descr"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                book.setCrtDt(rs.getString("crt_dt"));
                book.setUpdDt(rs.getString("upd_dt"));
                book.setDelFlg(rs.getString("del_flg"));
                results.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }

        }
        return results;
    }

    
    public List findByCategoryId(String catId) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books WHERE category_id = '" + catId + "'");
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                book.setPubDt(rs.getString("pub_dt"));
                book.setListPrice(rs.getDouble("list_price"));
                book.setTaxRate(rs.getString("tax_rate"));
                book.setStatus(rs.getString("status"));
                book.setDescr(rs.getString("descr"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                book.setCrtDt(rs.getString("crt_dt"));
                book.setUpdDt(rs.getString("upd_dt"));
                book.setDelFlg(rs.getString("del_flg"));
                results.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in findByCategoryId: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return results;
    }

    
    public List listActive() {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            results = session.createQuery("FROM Book WHERE delFlg = '0' OR delFlg IS NULL").list();
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

    
    public int save(Object book) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(book);
            tx.commit();
            try { BookstoreManager.getInstance().clearCache(); } catch (Exception e) {  }
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

    
    public Object findByIdForUpdate(String id) {
        Session session = null;
        Object result = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            result = session.get(Book.class, new Long(id), LockMode.UPGRADE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    
    public List findLowStock(String threshold) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books WHERE qty_in_stock <= '" + threshold + "' AND (del_flg = '0' OR del_flg IS NULL)");
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                book.setStatus(rs.getString("status"));
                results.add(book);
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

    
    public List searchBooksFromRequest(HttpServletRequest request) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String title = request.getParameter("title");
            String category = request.getParameter("categoryId");
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM books WHERE 1=1";
            if (title != null && title.length() > 0) {
                sql = sql + " AND title LIKE '%" + title + "%'";
            }
            if (category != null && category.length() > 0) {
                sql = sql + " AND category_id = '" + category + "'";
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                book.setListPrice(rs.getDouble("list_price"));
                book.setStatus(rs.getString("status"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                results.add(book);
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

    
    public List findByPublisher(String publisher) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books WHERE publisher LIKE '%" + publisher + "%'");
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                book.setPubDt(rs.getString("pub_dt"));
                book.setListPrice(rs.getDouble("list_price"));
                book.setTaxRate(rs.getString("tax_rate"));
                book.setStatus(rs.getString("status"));
                book.setDescr(rs.getString("descr"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                book.setCrtDt(rs.getString("crt_dt"));
                book.setUpdDt(rs.getString("upd_dt"));
                book.setDelFlg(rs.getString("del_flg"));
                results.add(book);
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

    
    public List findByPriceRange(String minPrice, String maxPrice) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books WHERE list_price >= " + minPrice + " AND list_price <= " + maxPrice);
            while (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                book.setPubDt(rs.getString("pub_dt"));
                book.setListPrice(rs.getDouble("list_price"));
                book.setTaxRate(rs.getString("tax_rate"));
                book.setStatus(rs.getString("status"));
                book.setDescr(rs.getString("descr"));
                book.setQtyInStock(rs.getString("qty_in_stock"));
                book.setCrtDt(rs.getString("crt_dt"));
                book.setUpdDt(rs.getString("upd_dt"));
                book.setDelFlg(rs.getString("del_flg"));
                results.add(book);
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
}
