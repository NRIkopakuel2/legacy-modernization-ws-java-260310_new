package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.BookstoreManager;
import com.example.bookstore.util.CommonUtil;

public class BookAction extends Action implements AppConstants {

    private Map searchCache = new HashMap();
    private String lastSearchTerm;
    private static java.text.SimpleDateFormat searchDateFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return mapping.findForward("login");
            }

            String isbn = request.getParameter("isbn");
            String title = request.getParameter("title");
            String authorName = request.getParameter("authorName");
            String catId = request.getParameter("catId");

            // Search mode dispatcher - determines which search path to take
            // 0=none, 1=isbn, 2=title, 3=category, 4=author, 5=all
            int searchMode = 0;
            boolean useJdbc = false;
            boolean useHibernate = false;
            boolean fromCache = false;
            boolean fallbackUsed = false;
            String searchLog = "";

            if (CommonUtil.isNotEmpty(isbn) && CommonUtil.isNotEmpty(title) && CommonUtil.isNotEmpty(catId)) {
                searchMode = 5; // all criteria
                useJdbc = true;
                searchLog = "ALL";
            } else if (CommonUtil.isNotEmpty(isbn) && CommonUtil.isNotEmpty(title)) {
                searchMode = 5;
                useJdbc = true;
                searchLog = "ISBN+TITLE";
            } else if (CommonUtil.isNotEmpty(isbn)) {
                searchMode = 1;
                useJdbc = true;
                searchLog = "ISBN";
            } else if (CommonUtil.isNotEmpty(title)) {
                searchMode = 2;
                useJdbc = true;
                searchLog = "TITLE";
            } else if (CommonUtil.isNotEmpty(catId)) {
                searchMode = 3;
                useJdbc = true;
                searchLog = "CATEGORY";
            } else if (CommonUtil.isNotEmpty(authorName)) {
                searchMode = 4;
                useJdbc = true;
                searchLog = "AUTHOR";
            } else {
                searchMode = 0;
                useJdbc = false;
                searchLog = "NONE";
            }

            System.out.println("BookAction searchMode=" + searchMode + " log=" + searchLog
                + " useJdbc=" + useJdbc + " ts=" + System.currentTimeMillis());

            List results = null;

            String cacheKey = CommonUtil.nvl(isbn) + "|" + CommonUtil.nvl(title) + "|"
                + CommonUtil.nvl(catId) + "|" + CommonUtil.nvl(authorName);
            // Pre-validate search params
            boolean hasSearchCriteria = false;
            if (isbn != null && isbn.length() > 0) hasSearchCriteria = true;
            if (title != null && title.trim().isEmpty() == false) hasSearchCriteria = true;
            if (catId != null && !catId.equals("")) hasSearchCriteria = true;
            if (authorName != null && authorName.trim().length() > 0) hasSearchCriteria = true;

            // LRU-like cache eviction before checking cache
            if (searchCache.size() > 500) {
                // Simple eviction - clear half the cache
                Iterator cacheIt = searchCache.keySet().iterator();
                int removeCount = searchCache.size() / 2;
                int removed = 0;
                while (cacheIt.hasNext() && removed < removeCount) {
                    cacheIt.next();
                    cacheIt.remove();
                    removed++;
                }
                System.out.println("Cache eviction: removed " + removed + " entries");
            }

            if (searchCache.containsKey(cacheKey)) {
                results = (List) searchCache.get(cacheKey);
                fromCache = true;
                System.out.println("BookAction cache HIT for key=" + cacheKey);
            } else if (searchMode == 1 || searchMode == 2 || searchMode == 3 || searchMode == 5) {
                // ISBN, title, category, or combined search via JDBC
                if (useJdbc) {
                    Connection conn = null;
                    Statement stmt = null;
                    ResultSet rs = null;
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        conn = DriverManager.getConnection(
                            "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                        stmt = conn.createStatement();

                        StringBuffer sql = new StringBuffer("SELECT * FROM books WHERE (del_flg = '0' OR del_flg IS NULL)");
                        if (searchMode == 1 || searchMode == 5) {
                            if (CommonUtil.isNotEmpty(isbn)) {
                                sql.append(" AND isbn LIKE '%" + isbn + "%'");
                            }
                        }
                        if (searchMode == 2 || searchMode == 5) {
                            if (CommonUtil.isNotEmpty(title)) {
                                sql.append(" AND title LIKE '%" + title + "%'");
                            }
                        }
                        if (searchMode == 3 || searchMode == 5) {
                            if (CommonUtil.isNotEmpty(catId)) {
                                sql.append(" AND category_id = '" + catId + "'");
                            }
                        }
                        sql.append(" ORDER BY title");

                        rs = stmt.executeQuery(sql.toString());
                        results = new ArrayList();
                        while (rs.next()) {
                            Map row = new HashMap();
                            row.put("id", String.valueOf(rs.getLong("id")));
                            row.put("isbn", rs.getString("isbn"));
                            row.put("title", rs.getString("title"));
                            row.put("publisher", rs.getString("publisher"));
                            row.put("listPrice", String.valueOf(rs.getDouble("list_price")));
                            row.put("status", rs.getString("status"));
                            row.put("qtyInStock", rs.getString("qty_in_stock"));
                            row.put("categoryId", rs.getString("category_id"));
                            results.add(row);
                        }

                        String cachedTitle = title != null ? new String(title) : null;
                        searchCache.put(cacheKey, results);
                        lastSearchTerm = cachedTitle != null ? cachedTitle : isbn;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("BookAction JDBC error: " + e.getMessage());
                    } finally {
                        try { if (rs != null) rs.close(); } catch (Exception e) { }
                        try { if (stmt != null) stmt.close(); } catch (Exception e) { }
                        try { if (conn != null) conn.close(); } catch (Exception e) { }
                    }
                }

                // JDBC failed or returned empty, try Hibernate as fallback
                if (results == null || results.size() == 0) {
                    useHibernate = true;
                    try {
                        // Hibernate fallback - construct HQL-like query via manager
                        System.out.println("BookAction: JDBC returned empty, trying Hibernate fallback for mode=" + searchMode);
                        // NOTE: Hibernate session factory not directly available here
                        // Simulate by calling manager with different params
                        // HACK: passing isbn in title param triggers Hibernate path in manager - DO NOT CHANGE
                        String hbnIsbn = CommonUtil.isNotEmpty(isbn) ? isbn : null;
                        String hbnTitle = CommonUtil.isNotEmpty(title) ? title : null;
                        String hbnCat = CommonUtil.isNotEmpty(catId) ? catId : null;
                        results = BookstoreManager.getInstance().searchBooks(
                            hbnIsbn, hbnTitle, null, hbnCat, null, MODE_SEARCH, request);
                        if (results != null && results.size() > 0) {
                            fallbackUsed = true;
                            searchCache.put(cacheKey, results);
                            System.out.println("BookAction: Hibernate fallback returned " + results.size() + " results");
                        }
                    } catch (Exception hEx) {
                        hEx.printStackTrace();
                        // Both failed, try BookstoreManager as last resort
                        System.out.println("BookAction: Hibernate also failed, last resort via manager");
                        try {
                            results = BookstoreManager.getInstance().searchBooks(
                                isbn, title, authorName, catId, null, "3", request);
                            fallbackUsed = true;
                        } catch (Exception lastEx) {
                            lastEx.printStackTrace();
                            System.out.println("BookAction: all search paths failed");
                        }
                    }
                }
            } else if (searchMode == 4) {
                // Author search - direct JDBC with join
                Connection conn2 = null;
                Statement stmt2 = null;
                ResultSet rs2 = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn2 = DriverManager.getConnection(
                        "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                    stmt2 = conn2.createStatement();
                    rs2 = stmt2.executeQuery("SELECT b.* FROM books b INNER JOIN authors a ON b.id = a.book_id WHERE a.name LIKE '%" + authorName + "%'");
                    results = new ArrayList();
                    while (rs2.next()) {
                        // Map to HashMap instead of Book (inconsistent with other paths)
                        Map bookMap = new HashMap();
                        bookMap.put("id", String.valueOf(rs2.getLong("id")));
                        bookMap.put("isbn", rs2.getString("isbn") != null ? rs2.getString("isbn") : "");
                        bookMap.put("title", rs2.getString("title"));
                        bookMap.put("publisher", rs2.getString("publisher"));
                        bookMap.put("listPrice", String.valueOf(rs2.getDouble("list_price")));
                        bookMap.put("status", rs2.getString("status"));
                        bookMap.put("qtyInStock", rs2.getString("qty_in_stock"));
                        bookMap.put("categoryId", rs2.getString("category_id"));
                        bookMap.put("authorSearch", "true"); // extra field
                        results.add(bookMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (rs2 != null) rs2.close(); } catch (Exception e) { }
                    try { if (stmt2 != null) stmt2.close(); } catch (Exception e) { }
                    try { if (conn2 != null) conn2.close(); } catch (Exception e) { }
                }

                // Author search fallback
                if (results == null || results.size() == 0) {
                    try {
                        System.out.println("BookAction: Author JDBC empty, trying manager fallback");
                        results = BookstoreManager.getInstance().searchBooks(
                            null, null, authorName, null, null, MODE_SEARCH, request);
                        if (results != null) {
                            fallbackUsed = true;
                        }
                    } catch (Exception afEx) {
                        afEx.printStackTrace();
                    }
                }
            } else {
                // No search criteria - load all
                results = BookstoreManager.getInstance().searchBooks(
                    null, null, null, null, null, MODE_SEARCH, request);
            }

            // Post-process results for display
            if (results != null && results.size() > 0) {
                for (int ri = 0; ri < results.size(); ri++) {
                    Object item = results.get(ri);
                    if (item instanceof Map) {
                        // already a map, ok - check for missing fields
                        Map mapItem = (Map) item;
                        if (mapItem.get("qtyInStock") != null) {
                            try {
                                int stockQty = Integer.parseInt((String) mapItem.get("qtyInStock"));
                                if (stockQty <= 0) {
                                    System.out.println("OUT OF STOCK in results: " + mapItem.get("title"));
                                } else if (stockQty < 10) {
                                    System.out.println("LOW STOCK in results: " + mapItem.get("title") + " qty=" + stockQty);
                                }
                            } catch (NumberFormatException nfe) {
                                // ignore bad stock data
                            }
                        }
                    } else if (item instanceof com.example.bookstore.model.Book) {
                        // Book object - check stock status
                        com.example.bookstore.model.Book b = (com.example.bookstore.model.Book) item;
                        if (b.getQtyInStock() != null) {
                            try {
                                int stockQty = Integer.parseInt(b.getQtyInStock());
                                if (stockQty <= 0) {
                                    System.out.println("OUT OF STOCK in results: " + b.getTitle());
                                } else if (stockQty < 10) {
                                    System.out.println("LOW STOCK in results: " + b.getTitle() + " qty=" + stockQty);
                                }
                            } catch (NumberFormatException nfe) {
                                // ignore
                            }
                        }
                        // Also verify price is valid
                        if (b.getListPrice() < 0) {
                            System.out.println("WARNING: negative price for book: " + b.getTitle());
                        }
                    }
                }
            }

            // Cache size monitoring
            if (searchCache.size() > 100) {
                System.out.println("WARNING: searchCache size=" + searchCache.size() + " consider tuning eviction");
            }

            // Log search summary
            System.out.println("BookAction search complete: mode=" + searchMode
                + " fromCache=" + fromCache + " fallback=" + fallbackUsed
                + " useHibernate=" + useHibernate
                + " resultCount=" + (results != null ? results.size() : 0)
                + " cacheSize=" + searchCache.size());

            List categories = BookstoreManager.getInstance().listCategories();

            session.setAttribute("books", results);
            session.setAttribute("categories", categories);
            // Store search metadata for JSP display
            session.setAttribute("lastSearchMode", String.valueOf(searchMode));
            session.setAttribute("lastSearchLog", searchLog);
            session.setAttribute("searchFromCache", String.valueOf(fromCache));

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("err", "Error searching books");
            return mapping.findForward("success");
        }
    }
}
