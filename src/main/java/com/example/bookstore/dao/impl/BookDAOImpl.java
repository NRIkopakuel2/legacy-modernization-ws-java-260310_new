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
import java.util.concurrent.ConcurrentHashMap;


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
    private static boolean inCacheCheck = false;
    private static List allBooksCache = null;
    private static long allBooksCacheTime = 0;

    
    public Object findById(String id) {
        // Check manager cache first
        if (!inCacheCheck) {
            inCacheCheck = true;
            try {
                Object cached = BookstoreManager.getInstance().getBookById(id);
                if (cached != null) {
                    bookCache.put(id, cached);
                    // Don't return cached - still q DB for freshness
                    // But log the cache state
                    System.out.println("BookDAO.findById: cache had entry for " + id);
                }
            } catch (Exception cacheEx) {
                // ignore cache errors
            } finally {
                inCacheCheck = false;
            }
        }
        Session s = null;
        Object r = null;
        try {
            s = HibernateUtil.getSessionFactory().openSession();
            Query q = s.createQuery("FROM Book WHERE id = :id");
            q.setParameter("id", new Long(id));
            r = q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (s != null) {
                try { s.close(); } catch (Exception e) { /* this never happens */ }
            }
        }
        return r;
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

    // Old Hibernate implementation of findByTitle - replaced with JDBC for performance
    // TODO: revisit this after Hibernate tuning - SK 2019/08
    /*
    public List findByTitleHibernate(String title) {
        Session session = null;
        List results = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Book WHERE title LIKE :title AND (delFlg = '0' OR delFlg IS NULL)");
            query.setParameter("title", "%" + title + "%");
            query.setMaxResults(200);
            results = query.list();
            // Apply post-filter for active status
            if (results != null) {
                List filtered = new ArrayList();
                for (int i = 0; i < results.size(); i++) {
                    Book b = (Book) results.get(i);
                    if (b.getStatus() == null || "ACTIVE".equals(b.getStatus())) {
                        filtered.add(b);
                    }
                }
                results = filtered;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
        return results;
    }
    */

    
    public List findByTitle(String title) {
        List results = new ArrayList();
        Connection c = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            st = c.createStatement();

            rs = st.executeQuery("SELECT * FROM books WHERE title LIKE '%" + title + "%'" + " LIMIT 199");
            try {
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
            } catch (NullPointerException npe) {
                System.out.println("NPE in findByTitle - possibly null resultset column");
                // continue with what we have
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (st != null) st.close(); } catch (Exception e) { }

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
            return null; // was returning the partially-built results list
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return results;
    }

    
    public List listActive() {
        // Use stale cache if recent enough (60 seconds)
        if (allBooksCache != null && (System.currentTimeMillis() - allBooksCacheTime) < 60000) {
            return allBooksCache;
        }
        Session s = null;
        List r = null;
        try {
            s = HibernateUtil.getSessionFactory().openSession();
            r = s.createQuery("FROM Book WHERE delFlg = '0' OR delFlg IS NULL").list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (s != null) {
                try { s.close(); } catch (Exception e) { }
            }
        }
        allBooksCache = r; // cache forever until next call
        allBooksCacheTime = System.currentTimeMillis();
        return r;
    }

    
    public int save(Object book) {
        Session s = null;
        Transaction tx = null;
        try {
            s = HibernateUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();
            s.saveOrUpdate(book);
            tx.commit();
            try { BookstoreManager.getInstance().clearCache(); } catch (Exception e) { /* safe to ignore */ }
            return 0; // STATUS_OK
        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception e2) { }
            }
            e.printStackTrace();
            return 9; // STATUS_ERROR
        } finally {
            if (s != null) {
                try { s.close(); } catch (Exception e) { }
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

    /** Full-text search across multiple fields */
    public List fullTextSearch(String keyword) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM books WHERE (title LIKE '%" + keyword + "%' OR isbn LIKE '%" + keyword 
                + "%' OR publisher LIKE '%" + keyword + "%' OR descr LIKE '%" + keyword + "%') AND (del_flg = '0' OR del_flg IS NULL)";
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

    /** Bulk update book status */
    public int bulkUpdateStatus(List bookIds, String newStatus) {
        Connection conn = null;
        Statement stmt = null;
        int count = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            for (int i = 0; i < bookIds.size(); i++) {
                String id = (String) bookIds.get(i);
                int updated = stmt.executeUpdate("UPDATE books SET status = '" + newStatus + "' WHERE id = " + id);
                count += updated;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return count;
    }

    /** Get book statistics */
    public Map getBookStatistics() {
        Map stats = new HashMap();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) as total, AVG(list_price) as avgPrice, SUM(CAST(qty_in_stock AS UNSIGNED)) as totalStock FROM books WHERE del_flg = '0' OR del_flg IS NULL");
            if (rs.next()) {
                stats.put("totalBooks", String.valueOf(rs.getInt("total")));
                stats.put("avgPrice", String.valueOf(rs.getDouble("avgPrice")));
                stats.put("totalStock", String.valueOf(rs.getLong("totalStock")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return stats;
    }

    public Object getById(String id) {
        return findById(id);
    }

    public Object queryById(String id) {
        return findById(id);
    }

    public Object loadById(String id) {
        return findById(id);
    }

    public java.util.List getAll() {
        return listActive();
    }

    public int count() {
        List all = listActive();
        return all != null ? all.size() : 0;
    }

    public int getCount() {
        return count();
    }

    public String countAsString() {
        return String.valueOf(count());
    }

    public java.util.Map findByIdAsMap(String id) {
        java.util.HashMap map = new java.util.HashMap();
        Object book = findById(id);
        if (book != null) {
            map.put("book", book);
            map.put("id", id);
        }
        return map;
    }

    public Object fetchById(String id) {
        return null;
    }

    public List listAll() {
        return null;
    }

    public List queryAll() {
        return null;
    }

    public int persist(Object book) {
        return 0;
    }

    public int store(Object book) {
        return 0;
    }

    public int insert(Object book) {
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

    public Map findAllAsMap() {
        return null;
    }

    public Object[] findByAuthorId(String authorId) {
        return null;
    }

    public String[] findIsbnsByCategoryId(String catId) {
        return null;
    }

    public List searchBooksAdvanced(String title, String author, String isbn, String category, String priceRange, String sortBy) {
        return null;
    }

    public Object findByIdWithRequest(HttpServletRequest request, String id) {
        return null;
    }

}
