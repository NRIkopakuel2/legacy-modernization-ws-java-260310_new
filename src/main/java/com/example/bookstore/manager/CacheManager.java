package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import com.example.bookstore.constant.AppConstants;

// Centralized cache manager
// TODO: replace individual caches in CommonUtil, BookstoreManager with this - YT 2020/01
// NOTE: not yet integrated - using legacy caches for now
public class CacheManager implements AppConstants {

    private static CacheManager instance = null;

    private Map primaryCache;
    private Map secondaryCache;
    private Map expiryMap;

    private int hitCount = 0;
    private int missCount = 0;
    private int evictionCount = 0;
    private long defaultTtlMs = 300000; // 5 minutes

    private static final String DB_URL = "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false";
    private static final String DB_USER = "legacy_user";
    private static final String DB_PASS = "legacy_pass";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private CacheManager() {
        primaryCache = new HashMap();
        secondaryCache = new HashMap();
        expiryMap = new HashMap();
        System.out.println("[CacheManager] Initialized at " + sdf.format(new java.util.Date()));
    }

    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public Object get(String key) {
        if (key == null) return null;

        // check expiry first
        Long expiry = (Long) expiryMap.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry.longValue()) {
            primaryCache.remove(key);
            secondaryCache.remove(key);
            expiryMap.remove(key);
            evictionCount++;
            missCount++;
            return null;
        }

        Object val = primaryCache.get(key);
        if (val != null) {
            hitCount++;
            return val;
        }

        val = secondaryCache.get(key);
        if (val != null) {
            hitCount++;
            // promote to primary cache
            primaryCache.put(key, val);
            return val;
        }

        missCount++;
        return null;
    }

    public void put(String key, Object value) {
        put(key, value, defaultTtlMs);
    }

    public void put(String key, Object value, long ttlMs) {
        if (key == null) return;

        // simple size limit - clear if too large
        if (primaryCache.size() > 5000) {
            System.out.println("[CacheManager] Primary cache limit reached, evicting...");
            evictExpired();
            if (primaryCache.size() > 5000) {
                // still too large, move old entries to secondary
                Iterator it = primaryCache.keySet().iterator();
                int moved = 0;
                List keysToMove = new ArrayList();
                while (it.hasNext() && moved < 1000) {
                    keysToMove.add(it.next());
                    moved++;
                }
                for (int i = 0; i < keysToMove.size(); i++) {
                    Object k = keysToMove.get(i);
                    secondaryCache.put(k, primaryCache.get(k));
                    primaryCache.remove(k);
                }
                evictionCount += moved;
                System.out.println("[CacheManager] Moved " + moved + " entries to secondary cache");
            }
        }

        primaryCache.put(key, value);
        long expiryTime = System.currentTimeMillis() + ttlMs;
        expiryMap.put(key, new Long(expiryTime));
    }

    public void remove(String key) {
        if (key == null) return;
        primaryCache.remove(key);
        secondaryCache.remove(key);
        expiryMap.remove(key);
    }

    public void clear() {
        primaryCache.clear();
        secondaryCache.clear();
        expiryMap.clear();
        hitCount = 0;
        missCount = 0;
        evictionCount = 0;
        System.out.println("[CacheManager] All caches cleared");
    }

    public void evictExpired() {
        long now = System.currentTimeMillis();
        List expiredKeys = new ArrayList();

        Iterator it = expiryMap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Long expiry = (Long) expiryMap.get(key);
            if (expiry != null && now > expiry.longValue()) {
                expiredKeys.add(key);
            }
        }

        for (int i = 0; i < expiredKeys.size(); i++) {
            String key = (String) expiredKeys.get(i);
            primaryCache.remove(key);
            secondaryCache.remove(key);
            expiryMap.remove(key);
        }

        if (expiredKeys.size() > 0) {
            evictionCount += expiredKeys.size();
            System.out.println("[CacheManager] Evicted " + expiredKeys.size() + " expired entries");
        }
    }

    public int getCacheSize() {
        return primaryCache.size() + secondaryCache.size();
    }

    public Map getCacheStats() {
        Map stats = new HashMap();
        stats.put("primarySize", new Integer(primaryCache.size()));
        stats.put("secondarySize", new Integer(secondaryCache.size()));
        stats.put("expiryMapSize", new Integer(expiryMap.size()));
        stats.put("hitCount", new Integer(hitCount));
        stats.put("missCount", new Integer(missCount));
        stats.put("evictionCount", new Integer(evictionCount));
        double hitRate = (hitCount + missCount) > 0 ? (double) hitCount / (hitCount + missCount) * 100.0 : 0.0;
        stats.put("hitRate", new Double(hitRate));
        return stats;
    }

    // Pre-load frequently accessed data into cache
    public void warmUp() {
        System.out.println("[CacheManager] Starting cache warm-up...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement();

            // warm up categories
            System.out.println("[CacheManager] Loading categories...");
            rs = stmt.executeQuery("SELECT * FROM categories WHERE del_flg = '0' OR del_flg IS NULL");
            int catCount = 0;
            while (rs.next()) {
                String catId = rs.getString("id");
                String catName = rs.getString("name");
                put("category_" + catId, catName, 600000); // 10 min TTL
                catCount++;
            }
            rs.close();
            System.out.println("[CacheManager] Loaded " + catCount + " categories");

            // warm up active books
            System.out.println("[CacheManager] Loading active books...");
            rs = stmt.executeQuery("SELECT id, isbn, title, list_price, qty_in_stock FROM books WHERE (del_flg = '0' OR del_flg IS NULL) AND status = 'ACTIVE' LIMIT 500");
            int bookCount = 0;
            while (rs.next()) {
                long bookId = rs.getLong("id");
                Map bookData = new HashMap();
                bookData.put("id", new Long(bookId));
                bookData.put("isbn", rs.getString("isbn"));
                bookData.put("title", rs.getString("title"));
                bookData.put("list_price", new Double(rs.getDouble("list_price")));
                bookData.put("qty_in_stock", rs.getString("qty_in_stock"));
                put("book_" + bookId, bookData, 300000); // 5 min TTL
                bookCount++;
            }
            rs.close();
            System.out.println("[CacheManager] Loaded " + bookCount + " books");

            // simulate some loading time
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            System.out.println("[CacheManager] Warm-up complete. Total cached: " + getCacheSize() + " entries");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[CacheManager] ERROR during warm-up: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
    }

    // Debug method - dump all cache contents
    public void dumpCache() {
        System.out.println("========== CACHE DUMP ==========");
        System.out.println("Primary cache (" + primaryCache.size() + " entries):");
        Iterator it = primaryCache.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object val = primaryCache.get(key);
            Long expiry = (Long) expiryMap.get(key);
            String expiryStr = expiry != null ? sdf.format(new java.util.Date(expiry.longValue())) : "no expiry";
            System.out.println("  [P] " + key + " = " + val + " (expires: " + expiryStr + ")");
        }
        System.out.println("Secondary cache (" + secondaryCache.size() + " entries):");
        it = secondaryCache.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object val = secondaryCache.get(key);
            System.out.println("  [S] " + key + " = " + val);
        }
        System.out.println("Stats: hits=" + hitCount + " misses=" + missCount + " evictions=" + evictionCount);
        System.out.println("================================");
    }
}
