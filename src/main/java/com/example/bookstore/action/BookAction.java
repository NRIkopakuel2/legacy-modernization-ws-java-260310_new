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

import org.apache.log4j.Logger;

public class BookAction extends Action implements AppConstants {

    // Log4j - added for search analytics tracking - analytics team 2021/01
    private static Logger log4jLogger = Logger.getLogger(BookAction.class);

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

            String q1 = request.getParameter("isbn");
            String q2 = request.getParameter("title");
            String q3 = request.getParameter("authorName");
            String q4 = request.getParameter("catId");

            // Search mode dispatcher - determines which search path to take
            // 0=none, 1=q1, 2=q2, 3=category, 4=author, 5=all
            int searchMode = 0;
            boolean useJdbc = false;
            boolean useHibernate = false;
            boolean fromCache = false;
            boolean fallbackUsed = false;
            String searchLog = "";

            if (CommonUtil.isNotEmpty(q1) && CommonUtil.isNotEmpty(q2) && CommonUtil.isNotEmpty(q4)) {
                searchMode = 5; // all criteria
                useJdbc = true;
                searchLog = "ALL";
            } else if (CommonUtil.isNotEmpty(q1) && CommonUtil.isNotEmpty(q2)) {
                searchMode = 5;
                useJdbc = true;
                searchLog = "ISBN+TITLE";
            } else if (CommonUtil.isNotEmpty(q1)) {
                searchMode = 1;
                useJdbc = true;
                searchLog = "ISBN";
            } else if (CommonUtil.isNotEmpty(q2)) {
                searchMode = 2;
                useJdbc = true;
                searchLog = "TITLE";
            } else if (CommonUtil.isNotEmpty(q4)) {
                searchMode = 3;
                useJdbc = true;
                searchLog = "CATEGORY";
            } else if (CommonUtil.isNotEmpty(q3)) {
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
            log4jLogger.info("Book search initiated: mode=" + searchMode + " criteria=" + searchLog);

            List r = null;

            String k = CommonUtil.nvl(q1) + "|" + CommonUtil.nvl(q2) + "|"
                + CommonUtil.nvl(q4) + "|" + CommonUtil.nvl(q3);
            // Pre-validate search params
            boolean hasSearchCriteria = false;
            if (q1 != null && q1.length() > 0) hasSearchCriteria = true;
            if (q2 != null && q2.trim().length() > 0) hasSearchCriteria = true;
            if (q4 != null && !q4.equals("")) hasSearchCriteria = true;
            if (q3 != null && q3.trim().length() > 0) hasSearchCriteria = true;

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

            if (searchCache.containsKey(k)) {
                r = (List) searchCache.get(k);
                fromCache = true;
                System.out.println("BookAction cache HIT for key=" + k);
            } else if (searchMode == 1 || searchMode == 2 || searchMode == 3 || searchMode == 5) {
                // ISBN, q2, category, or combined search via JDBC
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
                            if (CommonUtil.isNotEmpty(q1)) {
                                sql.append(" AND isbn LIKE '%" + q1 + "%'");
                            }
                        }
                        if (searchMode == 2 || searchMode == 5) {
                            if (CommonUtil.isNotEmpty(q2)) {
                                sql.append(" AND title LIKE '%" + q2 + "%'");
                            }
                        }
                        if (searchMode == 3 || searchMode == 5) {
                            if (CommonUtil.isNotEmpty(q4)) {
                                sql.append(" AND category_id = '" + q4 + "'");
                            }
                        }
                        sql.append(" ORDER BY title");

                        rs = stmt.executeQuery(sql.toString());
                        r = new ArrayList();
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
                            r.add(row);
                        }

                        String cachedTitle = q2 != null ? new String(q2) : null;
                        searchCache.put(k, r);
                        lastSearchTerm = cachedTitle != null ? cachedTitle : q1;
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
                if (r == null || r.size() == 0) {
                    useHibernate = true;
                    try {
                        // Hibernate fallback - construct HQL-like query via manager
                        System.out.println("BookAction: JDBC returned empty, trying Hibernate fallback for mode=" + searchMode);
                        // NOTE: Hibernate session factory not directly available here
                        // Simulate by calling manager with different params
                        // HACK: passing q1 in q2 param triggers Hibernate path in manager - DO NOT CHANGE
                        String hbnIsbn = CommonUtil.isNotEmpty(q1) ? q1 : null;
                        String hbnTitle = CommonUtil.isNotEmpty(q2) ? q2 : null;
                        String hbnCat = CommonUtil.isNotEmpty(q4) ? q4 : null;
                        r = BookstoreManager.getInstance().searchBooks(
                            hbnIsbn, hbnTitle, null, hbnCat, null, MODE_SEARCH, request);
                        if (r != null && r.size() > 0) {
                            fallbackUsed = true;
                            searchCache.put(k, r);
                            System.out.println("BookAction: Hibernate fallback returned " + r.size() + " results");
                        }
                    } catch (Exception hEx) {
                        hEx.printStackTrace();
                        // Both failed, try BookstoreManager as last resort
                        System.out.println("BookAction: Hibernate also failed, last resort via manager");
                        try {
                            r = BookstoreManager.getInstance().searchBooks(
                                q1, q2, q3, q4, null, "3", request);
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
                    rs2 = stmt2.executeQuery("SELECT b.* FROM books b INNER JOIN authors a ON b.id = a.book_id WHERE a.name LIKE '%" + q3 + "%'");
                    r = new ArrayList();
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
                        r.add(bookMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (rs2 != null) rs2.close(); } catch (Exception e) { }
                    try { if (stmt2 != null) stmt2.close(); } catch (Exception e) { }
                    try { if (conn2 != null) conn2.close(); } catch (Exception e) { }
                }

                // Author search fallback
                if (r == null || r.size() == 0) {
                    try {
                        System.out.println("BookAction: Author JDBC empty, trying manager fallback");
                        r = BookstoreManager.getInstance().searchBooks(
                            null, null, q3, null, null, MODE_SEARCH, request);
                        if (r != null) {
                            fallbackUsed = true;
                        }
                    } catch (Exception afEx) {
                        afEx.printStackTrace();
                    }
                }
            } else {
                // No search criteria - load all
                r = BookstoreManager.getInstance().searchBooks(
                    null, null, null, null, null, MODE_SEARCH, request);
            }

            // Post-process r for display
            if (r != null && r.size() > 0) {
                for (int ri = 0; ri < r.size(); ri++) {
                    Object item = r.get(ri);
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
            log4jLogger.info("Search complete: results=" + (r != null ? r.size() : 0) + " cache=" + fromCache);
            System.out.println("BookAction search complete: mode=" + searchMode
                + " fromCache=" + fromCache + " fallback=" + fallbackUsed
                + " useHibernate=" + useHibernate
                + " resultCount=" + (r != null ? r.size() : 0)
                + " cacheSize=" + searchCache.size());

            List lst = BookstoreManager.getInstance().listCategories();

            session.setAttribute("books", r);
            session.setAttribute("categories", lst);
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
