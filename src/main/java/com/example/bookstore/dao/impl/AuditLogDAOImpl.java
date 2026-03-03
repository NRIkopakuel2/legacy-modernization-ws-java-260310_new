package com.example.bookstore.dao.impl;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.AuditLogDAO;
import com.example.bookstore.model.AuditLog;
import com.example.bookstore.util.HibernateUtil;

public class AuditLogDAOImpl implements AuditLogDAO, AppConstants {

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    
    public int save(Object auditLog) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(auditLog);
            tx.commit();
            return 0;
        } catch (Exception e) {
            if (tx != null) { try { tx.rollback(); } catch (Exception e2) { } }
            e.printStackTrace();
            return 9;
        } finally {
            if (session != null) { try { session.close(); } catch (Exception e) { } }
        }
    }

    
    public List findByFilters(String startDate, String endDate, String actionType,
                              String userId, String entityType, String searchText, String page) {
        List results = new ArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement();

            StringBuffer sql = new StringBuffer("SELECT * FROM audit_log WHERE 1=1");

            if (startDate != null && startDate.trim().length() > 0) {
                sql.append(" AND crt_dt >= '" + startDate + "'");
            }
            if (endDate != null && endDate.trim().length() > 0) {
                sql.append(" AND crt_dt <= '" + endDate + "'");
            }
            if (actionType != null && actionType.trim().length() > 0) {
                sql.append(" AND action_type = '" + actionType + "'");
            }
            if (userId != null && userId.trim().length() > 0) {
                sql.append(" AND user_id = '" + userId + "'");
            }
            if (entityType != null && entityType.trim().length() > 0) {
                sql.append(" AND entity_type = '" + entityType + "'");
            }
            if (searchText != null && searchText.trim().length() > 0) {
                sql.append(" AND (action_details LIKE '%" + searchText + "%' OR username LIKE '%" + searchText + "%')");
            }

            sql.append(" ORDER BY crt_dt DESC");

            int pageNum = 1;
            try { pageNum = Integer.parseInt(page); } catch (Exception e) { }
            int offset = (pageNum - 1) * 20;
            sql.append(" LIMIT 20 OFFSET " + offset);

            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(new Long(rs.getLong("id")));
                log.setActionType(rs.getString("action_type"));
                log.setUserId(rs.getString("user_id"));
                log.setUsername(rs.getString("username"));
                log.setEntityType(rs.getString("entity_type"));
                log.setEntityId(rs.getString("entity_id"));
                log.setActionDetails(rs.getString("action_details"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setUserAgent(rs.getString("user_agent"));
                log.setCrtDt(rs.getString("crt_dt"));
                results.add(log);
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

    
    public String countByFilters(String startDate, String endDate, String actionType,
                                 String userId, String entityType, String searchText) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement();

            StringBuffer sql = new StringBuffer("SELECT count(*) AS cnt FROM audit_log WHERE 1=1");

            if (startDate != null && startDate.trim().length() > 0) {
                sql.append(" AND crt_dt >= '" + startDate + "'");
            }
            if (endDate != null && endDate.trim().length() > 0) {
                sql.append(" AND crt_dt <= '" + endDate + "'");
            }
            if (actionType != null && actionType.trim().length() > 0) {
                sql.append(" AND action_type = '" + actionType + "'");
            }
            if (userId != null && userId.trim().length() > 0) {
                sql.append(" AND user_id = '" + userId + "'");
            }
            if (entityType != null && entityType.trim().length() > 0) {
                sql.append(" AND entity_type = '" + entityType + "'");
            }
            if (searchText != null && searchText.trim().length() > 0) {
                sql.append(" AND (action_details LIKE '%" + searchText + "%' OR username LIKE '%" + searchText + "%')");
            }

            rs = stmt.executeQuery(sql.toString());
            if (rs.next()) {
                return rs.getString("cnt");
            }
            return "0";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }
}
