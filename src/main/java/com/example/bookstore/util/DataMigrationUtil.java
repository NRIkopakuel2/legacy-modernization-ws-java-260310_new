package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;

import com.example.bookstore.constant.AppConstants;

// Data migration utility for v1.0 → v2.0 schema migration
// Run once during v2.0 migration - keep for reference and possible rollback
// Last executed: 2019-03-20 by SK
public class DataMigrationUtil implements AppConstants {

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    private static boolean DRY_RUN = true;
    private static int errorCount = 0;
    private static int successCount = 0;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static void log(String msg) {
        System.out.println("[MIGRATION " + sdf.format(new java.util.Date()) + "] " + msg);
    }

    private static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Migrate orders from v1 format (single table) to v2 format (orders + order_items)
    public static void migrateOrdersV1toV2() {
        log("=== Starting order migration v1 -> v2 ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            log("Reading v1 order data...");
            rs = stmt.executeQuery("SELECT * FROM orders_v1 ORDER BY order_date");
            int count = 0;
            while (rs.next()) {
                count++;
                String orderId = rs.getString("order_id");
                String custId = rs.getString("customer_id");
                String orderDate = rs.getString("order_date");
                String bookId = rs.getString("book_id");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("unit_price");
                double total = rs.getDouble("total_amount");
                String status = rs.getString("status");

                log("  Processing order #" + orderId + " (customer=" + custId + ", book=" + bookId + ")");

                if (!DRY_RUN) {
                    Statement ins = conn.createStatement();
                    ins.executeUpdate("INSERT IGNORE INTO orders (id, customer_id, order_date, total_amount, status, crt_dt, upd_dt, del_flg) "
                        + "VALUES (" + orderId + ", " + custId + ", '" + orderDate + "', " + total + ", '" + status + "', NOW(), NOW(), '0')");
                    ins.executeUpdate("INSERT INTO order_items (order_id, book_id, quantity, unit_price, subtotal) "
                        + "VALUES (" + orderId + ", " + bookId + ", " + qty + ", " + price + ", " + (qty * price) + ")");
                    ins.close();
                }
                successCount++;
            }
            log("Order migration complete: " + count + " records processed");
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in migrateOrdersV1toV2: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Find and merge duplicate customer records (same email, different IDs)
    public static void fixDuplicateCustomers() {
        log("=== Starting duplicate customer fix ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            log("Finding duplicate emails...");
            rs = stmt.executeQuery("SELECT email, COUNT(*) as cnt, MIN(id) as keep_id FROM customer GROUP BY email HAVING cnt > 1");
            int dupeCount = 0;
            while (rs.next()) {
                dupeCount++;
                String email = rs.getString("email");
                int keepId = rs.getInt("keep_id");
                log("  Duplicate found: " + email + " (keeping ID=" + keepId + ")");

                if (!DRY_RUN) {
                    Statement upd = conn.createStatement();
                    upd.executeUpdate("UPDATE orders SET customer_id = " + keepId + " WHERE customer_id IN "
                        + "(SELECT id FROM customer WHERE email = '" + email + "' AND id != " + keepId + ")");
                    upd.executeUpdate("DELETE FROM customer WHERE email = '" + email + "' AND id != " + keepId);
                    upd.close();
                }
                successCount++;
            }
            log("Duplicate customer fix complete: " + dupeCount + " duplicates found");
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in fixDuplicateCustomers: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Rebuild full-text search index table
    public static void rebuildSearchIndex() {
        log("=== Rebuilding search index ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            if (!DRY_RUN) {
                log("Dropping old search index...");
                stmt.executeUpdate("DELETE FROM book_search_index");
            }

            log("Reading books for indexing...");
            rs = stmt.executeQuery("SELECT id, isbn, title, publisher, descr FROM books WHERE del_flg = '0' OR del_flg IS NULL");
            int count = 0;
            while (rs.next()) {
                count++;
                long bookId = rs.getLong("id");
                String searchText = rs.getString("title") + " " + rs.getString("isbn") + " "
                    + (rs.getString("publisher") != null ? rs.getString("publisher") : "") + " "
                    + (rs.getString("descr") != null ? rs.getString("descr") : "");
                searchText = searchText.toLowerCase().trim();

                if (!DRY_RUN) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO book_search_index (book_id, search_text, upd_dt) VALUES (?, ?, NOW())");
                    ps.setLong(1, bookId);
                    ps.setString(2, searchText);
                    ps.executeUpdate();
                    ps.close();
                }
                if (count % 100 == 0) {
                    log("  Indexed " + count + " books...");
                }
            }
            log("Search index rebuild complete: " + count + " books indexed");
            successCount++;
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in rebuildSearchIndex: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Re-hash passwords from plain text to MD5
    // NOTE: MD5 is not secure - this was a temporary measure during v2.0 migration
    public static void migrateUserPasswords() {
        log("=== Starting password migration (plaintext -> MD5) ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            log("Reading user passwords...");
            rs = stmt.executeQuery("SELECT id, login_id, password FROM users WHERE password_hash IS NULL");
            int count = 0;
            while (rs.next()) {
                count++;
                int userId = rs.getInt("id");
                String loginId = rs.getString("login_id");
                String plainPassword = rs.getString("password");

                if (plainPassword != null && plainPassword.length() > 0) {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] digest = md.digest(plainPassword.getBytes("UTF-8"));
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < digest.length; i++) {
                        String hex = Integer.toHexString(0xff & digest[i]);
                        if (hex.length() == 1) sb.append('0');
                        sb.append(hex);
                    }
                    String md5Hash = sb.toString();
                    log("  User " + loginId + ": hashing password (length=" + plainPassword.length() + ")");

                    if (!DRY_RUN) {
                        PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ?, password = NULL WHERE id = ?");
                        ps.setString(1, md5Hash);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                        ps.close();
                    }
                    successCount++;
                }
            }
            log("Password migration complete: " + count + " users processed");
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in migrateUserPasswords: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Create audit log entries for historical data that predates the audit_log table
    public static void backfillAuditLog() {
        log("=== Starting audit log backfill ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            log("Backfilling from orders...");
            rs = stmt.executeQuery("SELECT id, customer_id, order_date, total_amount FROM orders WHERE crt_dt < '2019-01-01'");
            int count = 0;
            while (rs.next()) {
                count++;
                long orderId = rs.getLong("id");
                String custId = rs.getString("customer_id");
                String orderDate = rs.getString("order_date");

                if (!DRY_RUN) {
                    Statement ins = conn.createStatement();
                    ins.executeUpdate("INSERT INTO audit_log (entity_type, entity_id, action, performed_by, performed_at, details) "
                        + "VALUES ('ORDER', " + orderId + ", 'CREATE', 'SYSTEM_BACKFILL', '" + orderDate + "', "
                        + "'Backfilled during v2.0 migration - customer_id=" + custId + "')");
                    ins.close();
                }
                if (count % 500 == 0) {
                    log("  Backfilled " + count + " audit records...");
                }
            }
            log("Audit log backfill complete: " + count + " records created");
            successCount++;
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in backfillAuditLog: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Validate that migration was successful by running counts and checksums
    public static void validateMigration() {
        log("=== Starting migration validation ===");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int checks = 0;
        int passed = 0;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // Check 1: order count matches
            checks++;
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM orders");
            rs.next();
            int orderCount = rs.getInt("cnt");
            rs.close();
            log("  Check " + checks + ": Order count = " + orderCount);
            if (orderCount > 0) { passed++; log("    PASSED"); } else { log("    FAILED - no orders found"); }

            // Check 2: no null customer_ids in orders
            checks++;
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM orders WHERE customer_id IS NULL");
            rs.next();
            int nullCust = rs.getInt("cnt");
            rs.close();
            log("  Check " + checks + ": Null customer_ids = " + nullCust);
            if (nullCust == 0) { passed++; log("    PASSED"); } else { log("    FAILED - " + nullCust + " orders with null customer"); }

            // Check 3: no duplicate emails in customer table
            checks++;
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM (SELECT email FROM customer GROUP BY email HAVING COUNT(*) > 1) t");
            rs.next();
            int dupes = rs.getInt("cnt");
            rs.close();
            log("  Check " + checks + ": Duplicate customer emails = " + dupes);
            if (dupes == 0) { passed++; log("    PASSED"); } else { log("    FAILED - " + dupes + " duplicate emails remain"); }

            // Check 4: all passwords hashed
            checks++;
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM users WHERE password IS NOT NULL AND password != ''");
            rs.next();
            int plainPwd = rs.getInt("cnt");
            rs.close();
            log("  Check " + checks + ": Users with plain text password = " + plainPwd);
            if (plainPwd == 0) { passed++; log("    PASSED"); } else { log("    FAILED - " + plainPwd + " users still have plain passwords"); }

            // Check 5: search index populated
            checks++;
            rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM book_search_index");
            rs.next();
            int idxCount = rs.getInt("cnt");
            rs.close();
            log("  Check " + checks + ": Search index entries = " + idxCount);
            if (idxCount > 0) { passed++; log("    PASSED"); } else { log("    FAILED - search index is empty"); }

            log("=== Validation complete: " + passed + "/" + checks + " checks passed ===");
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
            log("ERROR in validateMigration: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    public static void main(String[] args) {
        log("========================================");
        log("  Bookstore v1.0 -> v2.0 Data Migration");
        log("  DRY_RUN = " + DRY_RUN);
        log("========================================");

        if (args.length > 0 && "execute".equals(args[0])) {
            DRY_RUN = false;
            log("*** LIVE MODE - changes will be committed ***");
        } else {
            log("*** DRY RUN MODE - no changes will be made ***");
            log("*** Pass 'execute' argument to run for real ***");
        }

        migrateOrdersV1toV2();
        fixDuplicateCustomers();
        rebuildSearchIndex();
        migrateUserPasswords();
        backfillAuditLog();
        validateMigration();

        log("========================================");
        log("  Migration finished");
        log("  Success: " + successCount + "  Errors: " + errorCount);
        log("========================================");
    }
}
