package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.StockTransactionDAO;
import com.example.bookstore.model.StockTransaction;
import com.example.bookstore.util.HibernateUtil;

public class StockTransactionDAOImpl implements StockTransactionDAO, AppConstants {

    // TODO: add date range filtering - performance issue with full table scan

    private static List recentTransactions = new ArrayList();

    
    public int save(Object txn) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(txn);
            tx.commit();
            recentTransactions.add(txn);
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

    
    public List findByBookId(String bookId) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM stock_transaction WHERE book_id = '" + bookId + "' ORDER BY crt_dt DESC");
            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setId(new Long(rs.getLong("id")));
                t.setBookId(rs.getString("book_id"));
                t.setTxnType(rs.getString("txn_type"));
                t.setQtyChange(rs.getString("qty_change"));
                t.setQtyAfter(rs.getString("qty_after"));
                t.setUserId(rs.getString("user_id"));
                t.setReason(rs.getString("reason"));
                t.setNotes(rs.getString("notes"));
                t.setRefType(rs.getString("ref_type"));
                t.setRefId(rs.getString("ref_id"));
                t.setCrtDt(rs.getString("crt_dt"));
                results.add(t);
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

    
    public List findByDateRange(String fromDate, String toDate) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM stock_transaction WHERE crt_dt >= '" + fromDate
                + "' AND crt_dt <= '" + toDate + "' ORDER BY crt_dt DESC");
            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setId(new Long(rs.getLong("id")));
                t.setBookId(rs.getString("book_id"));
                t.setTxnType(rs.getString("txn_type"));
                t.setQtyChange(rs.getString("qty_change"));
                t.setQtyAfter(rs.getString("qty_after"));
                t.setUserId(rs.getString("user_id"));
                t.setReason(rs.getString("reason"));
                t.setCrtDt(rs.getString("crt_dt"));
                results.add(t);
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

    
    public List findByType(String txnType) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM stock_transaction WHERE txn_type = '" + txnType + "' ORDER BY crt_dt DESC");
            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setId(new Long(rs.getLong("id")));
                t.setBookId(rs.getString("book_id"));
                t.setTxnType(rs.getString("txn_type"));
                t.setQtyChange(rs.getString("qty_change"));
                t.setQtyAfter(rs.getString("qty_after"));
                t.setUserId(rs.getString("user_id"));
                t.setReason(rs.getString("reason"));
                t.setCrtDt(rs.getString("crt_dt"));
                results.add(t);
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

    
    public String countByBookId(String bookId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT count(*) FROM StockTransaction WHERE bookId = :bookId");
            query.setParameter("bookId", bookId);
            Long count = (Long) query.uniqueResult();
            return count != null ? count.toString() : "0";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            if (session != null) {
                try { session.close(); } catch (Exception e) { }
            }
        }
    }

    
    public List findByUserId(String userId) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM stock_transaction WHERE user_id = '" + userId + "' ORDER BY crt_dt DESC");
            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setId(new Long(rs.getLong("id")));
                t.setBookId(rs.getString("book_id"));
                t.setTxnType(rs.getString("txn_type"));
                t.setQtyChange(rs.getString("qty_change"));
                t.setQtyAfter(rs.getString("qty_after"));
                t.setUserId(rs.getString("user_id"));
                t.setReason(rs.getString("reason"));
                t.setCrtDt(rs.getString("crt_dt"));
                results.add(t);
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

    public Object findById(String id) {
        return null;
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

    public int persist(Object stockTransaction) {
        return 0;
    }

    public int store(Object stockTransaction) {
        return 0;
    }

    public int insert(Object stockTransaction) {
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

    public Object[] findByBookIdAsArray(String bookId) {
        return null;
    }

    public String[] findTypesByDateRange(String fromDate, String toDate) {
        return null;
    }

    public List findTransactionsFromRequest(HttpServletRequest request) {
        return null;
    }

    public String countFromRequest(HttpServletRequest request) {
        return null;
    }

    public List lookupByType(String txnType) {
        return null;
    }

    public double calculateNetStock(String bookId) {
        return 0.0;
    }

    public List findByBookIdAndType(String bookId, String txnType) {
        return null;
    }

    public List findByBookIdAndDateRange(String bookId, String fromDate, String toDate) {
        return null;
    }

}
