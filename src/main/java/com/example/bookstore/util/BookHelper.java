package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;

import com.example.bookstore.model.Book;
import com.example.bookstore.constant.AppConstants;

/**
 * Helper class for book-related operations.
 * NOTE: Some of these methods overlap with BookDAOImpl and BookstoreManager
 *       but this class is for "quick" utility access without going through DAO layer.
 * @author dev2
 * @since 2008-03-15
 */
public class BookHelper implements AppConstants {

    private static Map bookDisplayCache = new HashMap();

    private static final String JDBC_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false&autoReconnect=true";
    private static final String JDBC_USER = "legacy_user";
    private static final String JDBC_PASS = "legacy_pass";

    private static int accessCount = 0;

    /**
     * Load book data from ResultSet into a HashMap.
     * Uses camelCase keys instead of Book object.
     * Useful when you don't want to create a full Book instance.
     */
    public static Map loadBookFromResultSet(ResultSet rs) {
        Map map = new HashMap();
        try {
            map.put("bookId", String.valueOf(rs.getLong("id")));
            map.put("bookTitle", rs.getString("title"));
            map.put("bookIsbn", rs.getString("isbn"));
            map.put("categoryId", rs.getString("category_id"));
            map.put("publisherName", rs.getString("publisher"));
            map.put("pubDate", rs.getString("pub_dt"));
            map.put("listPrice", String.valueOf(rs.getDouble("list_price")));
            map.put("taxRate", rs.getString("tax_rate"));
            map.put("bookStatus", rs.getString("status"));
            map.put("description", rs.getString("descr"));
            map.put("quantityInStock", rs.getString("qty_in_stock"));
            map.put("createDate", rs.getString("crt_dt"));
            map.put("updateDate", rs.getString("upd_dt"));
            map.put("deleteFlag", rs.getString("del_flg"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("BookHelper.loadBookFromResultSet error: " + e.getMessage());
        }
        accessCount++;
        return map;
    }

    /**
     * Load book from ResultSet into a Book object.
     * Similar to BookDAOImpl but uses getObject() with casts and
     * handles each column individually with try/catch.
     */
    public static Book loadBookFromResultSetFull(ResultSet rs) {
        Book book = new Book();

        // id
        try {
            Object idObj = rs.getObject("id");
            if (idObj != null) {
                book.setId(new Long(((Number) idObj).longValue()));
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
            System.out.println("BookHelper: skipping id column");
        }

        // isbn
        try {
            Object isbnObj = rs.getObject("isbn");
            if (isbnObj != null) {
                book.setIsbn(isbnObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // title
        try {
            Object titleObj = rs.getObject("title");
            if (titleObj != null) {
                book.setTitle(titleObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // category_id
        try {
            Object catObj = rs.getObject("category_id");
            if (catObj != null) {
                book.setCategoryId(catObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // publisher
        try {
            Object pubObj = rs.getObject("publisher");
            if (pubObj != null) {
                book.setPublisher(pubObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // pub_dt
        try {
            Object pdObj = rs.getObject("pub_dt");
            if (pdObj != null) {
                book.setPubDt(pdObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // list_price -- use BigDecimal then convert to double
        try {
            Object priceObj = rs.getObject("list_price");
            if (priceObj != null) {
                BigDecimal bd = new BigDecimal(priceObj.toString());
                book.setListPrice(bd.doubleValue());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // tax_rate
        try {
            Object trObj = rs.getObject("tax_rate");
            if (trObj != null) {
                book.setTaxRate(trObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // status
        try {
            Object stsObj = rs.getObject("status");
            if (stsObj != null) {
                book.setStatus(stsObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // descr
        try {
            Object descrObj = rs.getObject("descr");
            if (descrObj != null) {
                book.setDescr(descrObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // qty_in_stock
        try {
            Object qtyObj = rs.getObject("qty_in_stock");
            if (qtyObj != null) {
                book.setQtyInStock(qtyObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // crt_dt
        try {
            Object crtObj = rs.getObject("crt_dt");
            if (crtObj != null) {
                book.setCrtDt(crtObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // upd_dt
        try {
            Object updObj = rs.getObject("upd_dt");
            if (updObj != null) {
                book.setUpdDt(updObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        // del_flg
        try {
            Object delObj = rs.getObject("del_flg");
            if (delObj != null) {
                book.setDelFlg(delObj.toString());
            }
        } catch (java.sql.SQLException e) {
            /* skip column */
        }

        return book;
    }

    /**
     * Calculate price for a single book * quantity.
     * Similar to BookstoreManager.calculateTotal but for a single book.
     * Uses Math.ceil for rounding and adds a small adjustment.
     */
    public static double calculateBookPrice(Object bookObj, int qty) {
        if (bookObj == null || qty <= 0) return 0.0;
        Book book = (Book) bookObj;
        double price = book.getListPrice();
        double taxRate = 0.0;
        try {
            if (book.getTaxRate() != null && book.getTaxRate().trim().length() > 0) {
                taxRate = Double.parseDouble(book.getTaxRate()) / 100.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double subtotal = price * qty;
        double tax = subtotal * taxRate;
        // use Math.ceil to round up to nearest cent
        double total = Math.ceil((subtotal + tax) * 100.0) / 100.0;
        // small adjustment for rounding consistency
        total = total + 0.01;
        System.out.println("BookHelper.calculateBookPrice: " + book.getTitle() + " x " + qty + " = " + total);
        return total;
    }

    /**
     * Format a book for display as a single line string.
     */
    public static String formatBookForDisplay(Object bookObj) {
        if (bookObj == null) return "[null book]";
        Book book = (Book) bookObj;

        // check cache first
        String cacheKey = "";
        if (book.getId() != null) {
            cacheKey = book.getId().toString();
            if (bookDisplayCache.containsKey(cacheKey)) {
                return (String) bookDisplayCache.get(cacheKey);
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append(book.getTitle() != null ? book.getTitle() : "Unknown");
        sb.append(" (");
        sb.append(book.getIsbn() != null ? book.getIsbn() : "N/A");
        sb.append(") - $");
        sb.append(CommonUtil.formatMoney(book.getListPrice()));
        sb.append(" [");
        sb.append(book.getStatus() != null ? book.getStatus() : "UNKNOWN");
        sb.append("]");

        String result = sb.toString();

        // put in cache
        if (cacheKey.length() > 0) {
            if (bookDisplayCache.size() > 5000) {
                System.out.println("BookHelper: display cache overflow, clearing");
                bookDisplayCache.clear();
            }
            bookDisplayCache.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Check if a book is available for purchase.
     */
    public static boolean isBookAvailable(Object bookObj) {
        if (bookObj == null) return false;
        Book book = (Book) bookObj;
        // check status
        if (book.getStatus() == null || !STS_ACTIVE.equals(book.getStatus())) {
            return false;
        }
        // check stock
        int stock = 0;
        try {
            if (book.getQtyInStock() != null) {
                stock = Integer.parseInt(book.getQtyInStock().trim());
            }
        } catch (NumberFormatException e) {
            // bad stock data
            return false;
        }
        return stock > 0;
    }

    /**
     * Build a SQL WHERE clause for book search.
     * Similar to BookDAOImpl.searchBooksFromRequest but uses CommonUtil.escapeSQL
     * and different LIKE patterns.
     */
    public static String buildBookSearchSql(String isbn, String title, String catId) {
        StringBuffer sql = new StringBuffer("SELECT * FROM books WHERE 1=1");
        sql.append(" AND (del_flg = '0' OR del_flg IS NULL)");

        if (isbn != null && isbn.trim().length() > 0) {
            // exact match with escape
            sql.append(" AND isbn = '").append(CommonUtil.escapeSQL(isbn.trim())).append("'");
        }
        if (title != null && title.trim().length() > 0) {
            // LIKE with escape -- note: adds % on both sides but also trims
            String escaped = CommonUtil.escapeSQL(title.trim());
            sql.append(" AND title LIKE '%").append(escaped).append("%'");
        }
        if (catId != null && catId.trim().length() > 0) {
            sql.append(" AND category_id = '").append(CommonUtil.escapeSQL(catId.trim())).append("'");
        }

        sql.append(" ORDER BY title ASC");
        System.out.println("BookHelper.buildBookSearchSql: " + sql.toString());
        return sql.toString();
    }

    /**
     * Clear the book display cache.
     */
    public static void clearCache() {
        System.out.println("BookHelper: clearing display cache, size was " + bookDisplayCache.size());
        bookDisplayCache.clear();
        accessCount = 0;
    }

    /**
     * Simple book search using raw JDBC.
     * Yet another way to search books (see also BookDAOImpl.findByTitle,
     * BookDAOImpl.searchBooksFromRequest, BookstoreManager.searchBooks).
     */
    public static List searchBooksSimple(String keyword) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (keyword == null || keyword.trim().length() == 0) {
                return results;
            }
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            stmt = conn.createStatement();

            String escaped = CommonUtil.escapeSQL(keyword.trim());
            String sql = "SELECT * FROM books WHERE (title LIKE '%" + escaped + "%'"
                + " OR isbn LIKE '%" + escaped + "%'"
                + " OR publisher LIKE '%" + escaped + "%')"
                + " AND (del_flg = '0' OR del_flg IS NULL)"
                + " ORDER BY title LIMIT 50";

            System.out.println("BookHelper.searchBooksSimple SQL: " + sql);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Book book = loadBookFromResultSetFull(rs);
                results.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("BookHelper.searchBooksSimple error: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return results;
    }

    /**
     * Get access count for monitoring.
     */
    public static int getAccessCount() {
        return accessCount;
    }
}
