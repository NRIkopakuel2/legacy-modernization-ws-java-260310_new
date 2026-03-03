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
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            String isbn = request.getParameter("isbn");
            String title = request.getParameter("title");
            String authorName = request.getParameter("authorName");
            String catId = request.getParameter("catId");

            List results = null;

            String cacheKey = CommonUtil.nvl(isbn) + "|" + CommonUtil.nvl(title) + "|" + CommonUtil.nvl(catId);
            if (searchCache.containsKey(cacheKey)) {
                results = (List) searchCache.get(cacheKey);
            } else if (CommonUtil.isNotEmpty(isbn) || CommonUtil.isNotEmpty(title) || CommonUtil.isNotEmpty(catId)) {

                Connection conn = null;
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection(
                        "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
                    stmt = conn.createStatement();

                    StringBuffer sql = new StringBuffer("SELECT * FROM books WHERE (del_flg = '0' OR del_flg IS NULL)");
                    if (CommonUtil.isNotEmpty(isbn)) {
                        sql.append(" AND isbn LIKE '%" + isbn + "%'");
                    }
                    if (CommonUtil.isNotEmpty(title)) {
                        sql.append(" AND title LIKE '%" + title + "%'");
                    }
                    if (CommonUtil.isNotEmpty(catId)) {
                        sql.append(" AND category_id = '" + catId + "'");
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
            } else {

                results = BookstoreManager.getInstance().searchBooks(
                    null, null, null, null, null, MODE_SEARCH, request);
            }

            List categories = BookstoreManager.getInstance().listCategories();

            session.setAttribute("books", results);
            session.setAttribute("categories", categories);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error searching books");
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
