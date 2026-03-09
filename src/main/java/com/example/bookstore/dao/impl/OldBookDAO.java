package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.model.Book;

// Original BookDAO implementation - replaced by BookDAOImpl in v2.0 migration (2019/03)
// TODO: migrate to new BookDAOImpl - SK 2019/03/15
// Keep for rollback reference - do not delete
@Deprecated
public class OldBookDAO implements AppConstants {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private Connection conn;
    private boolean initialized = false;
    private static int queryCount = 0;

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false";
    private static final String USER = "legacy_user";
    private static final String PASS = "legacy_pass";

    public OldBookDAO() {
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASS);
            initialized = true;
            System.out.println("[OldBookDAO] Connection established");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[OldBookDAO] ERROR: Failed to initialize connection");
            initialized = false;
        }
    }

    public void init() {
        if (initialized) {
            System.out.println("[OldBookDAO] Already initialized, skipping");
            return;
        }
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASS);
            initialized = true;
            System.out.println("[OldBookDAO] init() - connection established");
        } catch (Exception e) {
            e.printStackTrace();
            initialized = false;
        }
    }

    public void destroy() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("[OldBookDAO] Connection closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialized = false;
    }

    private void checkConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                System.out.println("[OldBookDAO] Reconnecting...");
                Class.forName(DRIVER);
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // v1.0 used book_name instead of title in some queries
    public Object getBook(String id) {
        checkConnection();
        Statement stmt = null;
        ResultSet rs = null;
        Book book = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            // NOTE: old schema had book_name column, new schema uses title
            rs = stmt.executeQuery("SELECT * FROM books WHERE id = " + id);
            if (rs.next()) {
                book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                // old column name was book_name, keeping for reference
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[OldBookDAO] Error in getBook: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
        return book;
    }

    public List searchBooks(String keyword) {
        checkConnection();
        List results = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            // v1 search used book_name LIKE, v2 uses title LIKE
            String sql = "SELECT * FROM books WHERE title LIKE '%" + keyword + "%'"
                       + " OR isbn LIKE '%" + keyword + "%'"
                       + " OR publisher LIKE '%" + keyword + "%'";
            rs = stmt.executeQuery(sql);
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
        System.out.println("[OldBookDAO] searchBooks found " + results.size() + " results");
        return results;
    }

    public List getAllBooks() {
        checkConnection();
        List results = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            rs = stmt.executeQuery("SELECT * FROM books WHERE del_flg = '0' OR del_flg IS NULL ORDER BY id");
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
            System.out.println("[OldBookDAO] Error in getAllBooks: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
        return results;
    }

    // v1 used Map for insert - no model binding
    public int insertBook(Map data) {
        checkConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            StringBuffer sb = new StringBuffer();
            sb.append("INSERT INTO books (isbn, title, category_id, publisher, pub_dt, ");
            sb.append("list_price, tax_rate, status, descr, qty_in_stock, crt_dt, upd_dt, del_flg) VALUES (");
            sb.append("'").append(data.get("isbn")).append("', ");
            sb.append("'").append(data.get("title")).append("', ");
            sb.append("'").append(data.get("category_id")).append("', ");
            sb.append("'").append(data.get("publisher")).append("', ");
            sb.append("'").append(data.get("pub_dt")).append("', ");
            sb.append(data.get("list_price")).append(", ");
            sb.append("'").append(data.get("tax_rate")).append("', ");
            sb.append("'").append(data.get("status") != null ? data.get("status") : STS_ACTIVE).append("', ");
            sb.append("'").append(data.get("descr") != null ? data.get("descr") : "").append("', ");
            sb.append("'").append(data.get("qty_in_stock") != null ? data.get("qty_in_stock") : "0").append("', ");
            sb.append("'").append(sdf.format(new java.util.Date())).append("', ");
            sb.append("'").append(sdf.format(new java.util.Date())).append("', ");
            sb.append("'0')");
            int rows = stmt.executeUpdate(sb.toString());
            System.out.println("[OldBookDAO] insertBook: " + rows + " row(s) inserted");
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[OldBookDAO] Error in insertBook: " + e.getMessage());
            return STATUS_ERR;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
    }

    // v1 update - uses Map, builds SQL dynamically
    public int updateBook(String id, Map data) {
        checkConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            StringBuffer sb = new StringBuffer("UPDATE books SET ");
            boolean first = true;
            Iterator it = data.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object val = data.get(key);
                if (!first) sb.append(", ");
                if (val instanceof Number) {
                    sb.append(key).append(" = ").append(val);
                } else {
                    sb.append(key).append(" = '").append(val).append("'");
                }
                first = false;
            }
            sb.append(", upd_dt = '").append(sdf.format(new java.util.Date())).append("'");
            sb.append(" WHERE id = ").append(id);
            int rows = stmt.executeUpdate(sb.toString());
            System.out.println("[OldBookDAO] updateBook: " + rows + " row(s) updated");
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
    }

    public int deleteBook(String id) {
        checkConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            // soft delete - set del_flg
            String sql = "UPDATE books SET del_flg = '1', upd_dt = '" + sdf.format(new java.util.Date()) + "' WHERE id = " + id;
            int rows = stmt.executeUpdate(sql);
            System.out.println("[OldBookDAO] deleteBook: " + rows + " row(s) deleted (soft)");
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
    }

    // helper method from v1 - count active books
    public int countActiveBooks() {
        checkConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM books WHERE del_flg = '0' OR del_flg IS NULL");
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
        }
        return 0;
    }

    // v1 had a method to get books by status
    public List getBooksByStatus(String status) {
        checkConnection();
        List results = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            queryCount++;
            rs = stmt.executeQuery("SELECT * FROM books WHERE status = '" + status + "' AND (del_flg = '0' OR del_flg IS NULL)");
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
        }
        return results;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static void main(String[] args) {
        System.out.println("OldBookDAO standalone test");
    }
}
